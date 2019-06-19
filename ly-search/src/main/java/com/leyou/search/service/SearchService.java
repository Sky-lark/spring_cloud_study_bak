package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
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
            map.put("images", StringUtils.split(sku.getImages(), ","));
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
                if(param.getNumeric()){
                    // 如果是，处理成区间断
                    value = chooseSegment(value.toString(),param);
                }
                value = genericSpec.get(param.getId());
            }else {
                value = specialSpec.get(param.getId());
            }
            specMap.put(key, value);
        }
        goods.setSpecs(specMap);
        return goods;
    }

    /**
     * 判断区间
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
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        int page = request.getPage();
        int size = request.getSize();
        // 创建查询构建器
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        // 结果过滤
        nativeSearchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        // 分页
        nativeSearchQueryBuilder.withPageable(PageRequest.of(page, size));
        // 过滤
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("all",request.getKey()));
        // 查询
        Page<Goods> result = goodsRepository.search(nativeSearchQueryBuilder.build());
        // 解析
        int totalPages = result.getTotalPages();
        long totalElements = result.getTotalElements();
        List<Goods> goodsList = result.getContent();
        return new PageResult<>(totalElements,  totalPages, goodsList);
    }
}
