package com.example.longxia.model.dto.coupon;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 卡券生成请求
 *
 * @author zhouwei
 */
@Data
public class CouponGenerateRequest implements Serializable {

    /**
     * 卡券名称
     */
    private String couponName;

    /**
     * 卡券展示金额
     */
    private BigDecimal displayAmount;

    /**
     * 卡券实际金额
     */
    private BigDecimal actualAmount;

    /**
     * 卡券图片地址
     */
    private String couponUrl;

    /**
     * 生成数量（1~200）
     */
    private Integer count;

    private static final long serialVersionUID = 1L;
}
