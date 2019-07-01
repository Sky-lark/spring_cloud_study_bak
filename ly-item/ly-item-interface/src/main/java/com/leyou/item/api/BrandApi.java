package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface BrandApi {

    @GetMapping("brand/cid/{cid}")
    List<Brand> queryBrandByCid(@PathVariable("cid") Long cid);


    @GetMapping("brand/{id}")
    Brand queryBrandByid(@PathVariable("id") Long id);

    @GetMapping("brand/brands")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);
}
