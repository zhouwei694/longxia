package com.example.longxia.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 卡券状态枚举
 *
 * @author zhouwei
 */
@Getter
public enum CouponStatusEnum {

    INACTIVE(0, "未激活"),
    ACTIVE(1, "已激活"),
    VERIFIED(2, "已核销");

    private final Integer value;

    private final String text;

    CouponStatusEnum(Integer value, String text) {
        this.value = value;
        this.text = text;
    }

    /**
     * 根据 value 获取枚举
     */
    public static CouponStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (CouponStatusEnum anEnum : CouponStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
