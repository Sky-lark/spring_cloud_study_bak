package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface BrandApi {

    @GetMapping("cid/{cid}")
    List<Brand> queryBrandByCid(@PathVariable("cid") Long cid);


    @GetMapping("brand/{id}")
    Brand queryBrandByid(@PathVariable("id") Long id);
}
