package com.example.longxia.model.dto.coupon;

import com.example.longxia.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 卡券查询请求
 *
 * @author zhouwei
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CouponQueryRequest extends PageRequest implements Serializable {

    /**
     * 卡券号
     */
    private String couponNo;

    /**
     * 卡券名称
     */
    private String couponName;

    /**
     * 卡券状态：0-未激活，1-已激活，2-已核销
     */
    private Integer status;

    /**
     * 核销人
     */
    private String verifyBy;

    private static final long serialVersionUID = 1L;
}
