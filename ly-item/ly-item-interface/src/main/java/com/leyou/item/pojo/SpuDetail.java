package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_spu_detail")
public class SpuDetail {
    @Id
    private Long spuId;
    private String description; // 商品秒速
    private String genericSpec; // 规格名称和可选值模板
    private String packingList; // 全局规格属性
    private String specialSpec;
    private String afterService; // 售后服务
}
