package com.leyou.item.web;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService service;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCid(@PathVariable("cid") Long cid) {
        return ResponseEntity.ok(service.querySpecGroupByCid(cid));
    }

    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParamsByGid(@RequestParam("gid") Long gid) {
        return ResponseEntity.ok(service.querySpecParamsByGid(gid));
    }
}
