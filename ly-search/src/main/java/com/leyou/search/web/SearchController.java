package com.leyou.search.web;

import com.leyou.common.vo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SearchController {

    @Autowired
    private SearchService service;
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestParam SearchRequest searchRequest){
        return ResponseEntity.ok(service.search(searchRequest));
    };
}
