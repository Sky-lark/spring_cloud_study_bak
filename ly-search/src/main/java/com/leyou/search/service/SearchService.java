package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SearchService {


    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    /**
     * 构建goods
     *
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);
        goods.setSubTitle(spu.getSubTitle());
        // 搜索字段
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CTAGEGORY_NOT_FOUND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        Brand brand = brandClient.queryBrandByid(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.Brand_NOT_FOUND);
        }
        String all = spu.getTitle() + StringUtils.join(names, " ") + brand.getName();
        goods.setAll(all); // 标题，分类，品牌，规格等
        // 查询sku
        List<Sku> skus = goodsClient.querySkuBySpuId(spuId);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        // 对sku处理
        List<Map<String, Object>> skuList = new ArrayList<>();
        Set<Long> priceSet = new HashSet<>();
        for (Sku sku : skus) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            skuList.add(map);
            priceSet.add(sku.getPrice());
        }
        goods.setPrice(priceSet);
        goods.setSkus(JsonUtils.serialize(skuList));
        // 规格处理
        // 查询规格参数
        List<SpecParam> params = specificationClient.querySpecParamsList(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.SPEC_PARAMS_NOT_FOUND);
        }
        // 查询规格详情
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spuId);
        // 通用的规格参数
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
        // 特有的规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        // key是规格参数的名字，value是规格参数的值
        Map<String, Object> specMap = new HashMap<>();
        for (SpecParam param : params) {
            // 规格名称
            String key = param.getName();
            Object value = "";
            if (param.getGeneric()) {
                // 判断是否是数字类型
                if (param.getNumeric()) {
                    // 如果是，处理成区间断
                    value = chooseSegment(value.toString(), param);
                }
                value = genericSpec.get(param.getId());
            } else {
                value = specialSpec.get(param.getId());
            }
            specMap.put(key, value);
        }
        goods.setSpecs(specMap);
        return goods;
    }

    /**
     * 判断区间
     *
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {
        int page = request.getPage() - 1;
        int size = request.getSize();
        // 创建查询构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 结果过滤
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 分页
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page, size));
        // 过滤
        QueryBuilder basicQuery = buildBasicQuery(request);
        nativeSearchQueryBuilder.withQuery(basicQuery);
        // ----聚合分类和品牌信息
        // 聚合分类
        String categoryAggName = "category_agg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        String brandAggName = "brand_agg";
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        // 查询
        // Page<Goods> result = goodsRepository.search(nativeSearchQueryBuilder.build());
        AggregatedPage<Goods> result = template.queryForPage(nativeSearchQueryBuilder.build(), Goods.class);
        // 解析
        int totalPages = result.getTotalPages();
        long totalElements = result.getTotalElements();
        List<Goods> goodsList = result.getContent();
        //return new PageResult<>(totalElements, totalPages, goodsList);
        // 解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));
        List<Map<String, Object>> specs = null;
        if (categories != null && categories.size() == 1) {
            // 商品分类为1 开始聚合规格参数
            specs = buildSpecificationAgg(categories.get(0).getId(), basicQuery);
        }
        return new SearchResult(totalElements, totalPages, goodsList, categories, brands, specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            if (!"cid3".equals(key) && !"brandId".equals(key)) {
                key = "specs." + key + ".keyword";
            }
            String value = entry.getValue();
            queryBuilder.filter(QueryBuilders.termQuery(key, value));
        }
        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String, Object>> specs = new ArrayList<>();
        // 查询需要聚合的规格参数
        List<SpecParam> specParams = specificationClient.querySpecParamsList(null, cid, true);
        // 聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 带上查询条件
        queryBuilder.withQuery(basicQuery);
        // 聚合 specParams 每个参数进行聚合
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(
                    AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }
        // 获取聚合结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();
        for (SpecParam param : specParams) {
            String name = param.getName();
            StringTerms terms = aggregations.get(name);
            // build map
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", terms.getBuckets().stream()
                    .map(b -> b.getKeyAsString()).collect(Collectors.toList()));
            // add list
            specs.add(map);
        }
        return specs;
    }


    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> collect = terms.getBuckets()
                    .stream().map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(collect);
            return brands;
        } catch (Exception e) {
            log.error("【搜索服务器异常】", e);
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> collect = terms.getBuckets()
                    .stream().map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(collect);
            return categories;
        } catch (Exception e) {
            log.error("【搜索服务器异常】", e);
            return null;
        }
    }

    public void createOrUpdateIndex(Long spuId) {
        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 构建goods对象
        Goods goods = buildGoods(spu);
        // 存入索引库
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
