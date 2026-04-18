package com.example.longxia.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 卡券视图对象
 *
 * @author zhouwei
 */
@Data
public class CouponVO implements Serializable {

    /**
     * 卡券id
     */
    private Long id;

    /**
     * 卡券名称
     */
    private String couponName;

    /**
     * 卡券号
     */
    private String couponNo;

    /**
     * 卡券密码
     */
    private String couponPassword;

    /**
     * 卡券展示金额
     */
    private BigDecimal displayAmount;

    /**
     * 卡券实际金额
     */
    private BigDecimal actualAmount;

    /**
     * 卡券状态：0-未激活，1-已激活，2-已核销
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 核销人
     */
    private String verifyBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 核销时间
     */
    private LocalDateTime verifyTime;

    /**
     * 收件人电话
     */
    private String recipientPhone;

    /**
     * 收件人快递号
     */
    private String recipientExpressNo;

    /**
     * 卡券图片地址
     */
    private String couponUrl;

    private static final long serialVersionUID = 1L;
}
