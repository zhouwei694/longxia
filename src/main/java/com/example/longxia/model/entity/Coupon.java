package com.example.longxia.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.io.Serial;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卡券表 实体类。
 *
 * @author zhouwei
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("coupon")
public class Coupon implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 卡券名称
     */
    private String couponName;

    /**
     * 卡券号，从00001开始
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
     * 修改时间
     */
    private LocalDateTime updateTime;

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

}
