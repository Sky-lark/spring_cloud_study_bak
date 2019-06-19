package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    INVALID_FILE_TYPE(400, "文件类型不支持！"),
    Brand_NOT_FOUND(404, "品牌没有找到!"),
    CTAGEGORY_NOT_FOUND(404, "商品分类没有找到！"),
    SPEC_GROUP_NOT_FOUND(404, "商品规格组没有找到"),
    SPEC_PARAMS_NOT_FOUND(404, "商品规格参数没有找到"),

    GOODS_NOT_FOUND(404, "商品没有找到"),
    SPUDETAIL_NOT_FOUND(404, "商品详情没有找到!"),
    SKU_NOT_FOUND(404, "商品SKU没有找到!"),
    BRAND_SAVE_ERROE(500, "新增品牌失败！"),
    UPLOAD_FILE_ERROE(500, "文件上传失败！"),
    GOODS_SAVE_ERROE(500, "新增商品失败！"),;
    private int code;
    private String msg;
}
