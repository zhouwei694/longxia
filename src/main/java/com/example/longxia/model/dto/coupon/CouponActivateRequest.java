package com.example.longxia.model.dto.coupon;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 卡券激活请求
 *
 * @author zhouwei
 */
@Data
public class CouponActivateRequest implements Serializable {

    /**
     * 卡券id列表（必传）
     */
    private List<Long> ids;

    /**
     * 卡券名称（可选）
     */
    private String couponName;

    /**
     * 卡券展示金额（可选）
     */
    private BigDecimal displayAmount;

    /**
     * 卡券实际金额（可选）
     */
    private BigDecimal actualAmount;

    private static final long serialVersionUID = 1L;
}
