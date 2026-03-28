package com.example.longxia.service;

import com.example.longxia.model.dto.coupon.CouponActivateRequest;
import com.example.longxia.model.dto.coupon.CouponGenerateRequest;
import com.example.longxia.model.dto.coupon.CouponQueryRequest;
import com.example.longxia.model.dto.coupon.CouponVerifyRequest;
import com.example.longxia.model.vo.CouponVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.example.longxia.model.entity.Coupon;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 卡券表 服务层。
 *
 * @author zhouwei
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 批量生成卡券（一次100张）
     *
     * @param generateRequest 生成请求
     * @param request         HTTP请求（获取当前登录用户）
     * @return 是否成功
     */
    boolean generateCoupons(CouponGenerateRequest generateRequest, HttpServletRequest request);

    /**
     * 批量激活卡券
     *
     * @param activateRequest 激活请求
     * @return 是否成功
     */
    boolean activateCoupons(CouponActivateRequest activateRequest);

    /**
     * 核销卡券
     *
     * @param verifyRequest 核销请求
     * @param request       HTTP请求（获取当前登录用户）
     * @return 是否成功
     */
    boolean verifyCoupon(CouponVerifyRequest verifyRequest, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param queryRequest 查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(CouponQueryRequest queryRequest);

    /**
     * 实体转视图对象
     */
    CouponVO getCouponVO(Coupon coupon);

    /**
     * 实体列表转视图对象列表
     */
    List<CouponVO> getCouponVOList(List<Coupon> couponList);
}
