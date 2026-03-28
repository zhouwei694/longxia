package com.example.longxia.controller;

import com.example.longxia.annotation.AuthCheck;
import com.example.longxia.common.BaseResponse;
import com.example.longxia.common.ResultUtils;
import com.example.longxia.constant.UserConstant;
import com.example.longxia.exception.ErrorCode;
import com.example.longxia.exception.ThrowUtils;
import com.example.longxia.model.dto.coupon.CouponActivateRequest;
import com.example.longxia.model.dto.coupon.CouponGenerateRequest;
import com.example.longxia.model.dto.coupon.CouponQueryRequest;
import com.example.longxia.model.dto.coupon.CouponVerifyRequest;
import com.example.longxia.model.entity.Coupon;
import com.example.longxia.model.vo.CouponVO;
import com.example.longxia.service.CouponService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 卡券 控制层。
 *
 * @author zhouwei
 */
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * 生成卡券（管理员）
     * 一次性生成100个默认未激活的卡券
     */
    @PostMapping("/generate")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> generateCoupons(@RequestBody CouponGenerateRequest couponGenerateRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(couponGenerateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = couponService.generateCoupons(couponGenerateRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 卡券列表（管理员）
     * 分页查询，支持按卡券号、卡券名称、卡券状态、核销人筛选
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CouponVO>> listCouponByPage(@RequestBody CouponQueryRequest couponQueryRequest) {
        ThrowUtils.throwIf(couponQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = couponQueryRequest.getPageNum();
        long pageSize = couponQueryRequest.getPageSize();
        Page<Coupon> couponPage = couponService.page(Page.of(pageNum, pageSize),
                couponService.getQueryWrapper(couponQueryRequest));
        Page<CouponVO> couponVOPage = new Page<>(pageNum, pageSize, couponPage.getTotalRow());
        List<CouponVO> couponVOList = couponService.getCouponVOList(couponPage.getRecords());
        couponVOPage.setRecords(couponVOList);
        return ResultUtils.success(couponVOPage);
    }

    /**
     * 卡券激活（管理员）
     * 支持批量激活，只有未激活状态的才可以激活
     */
    @PostMapping("/activate")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> activateCoupons(@RequestBody CouponActivateRequest couponActivateRequest) {
        ThrowUtils.throwIf(couponActivateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = couponService.activateCoupons(couponActivateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 卡券核销（管理员）
     * 通过卡券密码核销，必须是已激活状态
     */
    @PostMapping("/verify")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> verifyCoupon(@RequestBody CouponVerifyRequest couponVerifyRequest,
                                              HttpServletRequest request) {
        ThrowUtils.throwIf(couponVerifyRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = couponService.verifyCoupon(couponVerifyRequest, request);
        return ResultUtils.success(result);
    }
}
