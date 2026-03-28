package com.example.longxia.model.dto.coupon;

import lombok.Data;

import java.io.Serializable;

/**
 * 卡券核销请求
 *
 * @author zhouwei
 */
@Data
public class CouponVerifyRequest implements Serializable {

    /**
     * 卡券密码（必传）
     */
    private String couponPassword;

    private static final long serialVersionUID = 1L;
}
