package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        Spu spu = goodsClient.querySpuById(spuId);

        List<Sku> skus = spu.getSkus();

        SpuDetail detail = spu.getSpuDetail();

        Brand brand = brandClient.queryBrandByid(spu.getBrandId());

        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        List<SpecGroup> specs = specificationClient.querySpecGroupByCid(spu.getCid3());
        model.put("spu", spu);
        model.put("skus", skus);
        model.put("detail", detail);
        model.put("brand", brand);
        model.put("categories", categories);
        model.put("specs", specs);
        return model;
    }

    public void createHtml(Long spuid){
        // 上下文
        Context context = new Context();
        context.setVariables(loadModel(spuid));
        // 输出流
        File dest = new File("D:\\Program\\fileouttest", spuid + ".html");
        if (dest.exists()) {
            dest.delete();
        }
        try(PrintWriter write = new PrintWriter(dest, "UTF-8")){
            // 生成html
            templateEngine.process("item",context,write);
        }catch (Exception e){
            log.error("[静态页错误]",e);
        }

    }

    public void deleteHTML(Long spuId) {
        File dest = new File("D:\\Program\\fileouttest", spuId + ".html");
        if (dest.exists()) {
            dest.delete();
        }
    }
}
