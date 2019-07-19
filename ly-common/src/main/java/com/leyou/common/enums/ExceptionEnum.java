package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    INVALID_FILE_TYPE(400, "文件类型不支持！"),
    VERIFY_CODE_ERROR(400, "验证码错误！"),
    USERNAME_OR_PASSWORD_INVALID(400, "验证码错误！"),
    INVALID_USER_DATA_TYPE(400, "无效的用户数据类型！"),
    VERIFY_TOKEN_FAIL(403, "未授权的用户！"),



    Brand_NOT_FOUND(404, "品牌没有找到!"),
    CTAGEGORY_NOT_FOUND(404, "商品分类没有找到！"),
    SPEC_GROUP_NOT_FOUND(404, "商品规格组没有找到"),
    SPEC_PARAMS_NOT_FOUND(404, "商品规格参数没有找到"),

    GOODS_NOT_FOUND(404, "商品没有找到"),
    SPUDETAIL_NOT_FOUND(404, "商品详情没有找到!"),
    SKU_NOT_FOUND(404, "商品SKU没有找到!"),
    GOODS_ID_CAN_NOT_BE_NULL(400,"spu id不能为空"),
    GOODS_UPDATE_ERROR(500,"goods 更新错误"),
    BRAND_SAVE_ERROR(500, "新增品牌失败！"),
    UPLOAD_FILE_ERROR(500, "文件上传失败！"),
    GOODS_SAVE_ERROR(500, "新增商品失败！"),
    CREATE_TOKEN_FAIL(500, "生成用token失败！");
    private int code;
    private String msg;
}
