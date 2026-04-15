package com.example.longxia.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.example.longxia.exception.BusinessException;
import com.example.longxia.exception.ErrorCode;
import com.example.longxia.model.dto.coupon.CouponActivateRequest;
import com.example.longxia.model.dto.coupon.CouponGenerateRequest;
import com.example.longxia.model.dto.coupon.CouponQueryRequest;
import com.example.longxia.model.dto.coupon.CouponVerifyRequest;
import com.example.longxia.model.entity.Coupon;
import com.example.longxia.model.entity.User;
import com.example.longxia.model.enums.CouponStatusEnum;
import com.example.longxia.model.vo.CouponStatisticsVO;
import com.example.longxia.model.vo.CouponVO;
import com.example.longxia.mapper.CouponMapper;
import com.example.longxia.service.CouponService;
import com.example.longxia.service.UserService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 卡券表 服务层实现。
 *
 * @author zhouwei
 */
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private static final int GENERATE_COUNT = 50;
    private static final long COUPON_NO_START = 3202604010001L;
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PASSWORD_LENGTH = 8;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generateCoupons(CouponGenerateRequest generateRequest, HttpServletRequest request) {
        if (generateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String couponName = generateRequest.getCouponName();
        if (StrUtil.isBlank(couponName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "卡券名称不能为空");
        }
        if (generateRequest.getDisplayAmount() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "卡券金额不能为空");
        }
        if (generateRequest.getActualAmount() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "卡券实际金额不能为空");
        }

        User loginUser = userService.getLoginUser(request);
        String createBy = loginUser.getUserName();

        long startNo = getMaxCouponNo() + 1;
        LocalDateTime now = LocalDateTime.now();

        List<Coupon> couponList = new ArrayList<>();
        for (int i = 0; i < GENERATE_COUNT; i++) {
            Coupon coupon = new Coupon();
            coupon.setCouponName(couponName);
            coupon.setCouponNo(String.valueOf(startNo + i));
            coupon.setCouponPassword(generatePassword());
            coupon.setDisplayAmount(generateRequest.getDisplayAmount());
            coupon.setActualAmount(generateRequest.getActualAmount());
            coupon.setStatus(CouponStatusEnum.INACTIVE.getValue());
            coupon.setCreateBy(createBy);
            coupon.setCreateTime(now);
            couponList.add(coupon);
        }

        return this.saveBatch(couponList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activateCoupons(CouponActivateRequest activateRequest) {
        if (activateRequest == null || CollUtil.isEmpty(activateRequest.getIds())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "卡券id不能为空");
        }

        List<Long> ids = activateRequest.getIds();
        List<Coupon> couponList = this.list(QueryWrapper.create().in("id", ids));
        if (CollUtil.isEmpty(couponList)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到对应卡券");
        }

        for (Coupon coupon : couponList) {
            if (!CouponStatusEnum.INACTIVE.getValue().equals(coupon.getStatus())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,
                        "卡券号 " + coupon.getCouponNo() + " 不是未激活状态，无法激活");
            }
        }

        for (Coupon coupon : couponList) {
            coupon.setStatus(CouponStatusEnum.ACTIVE.getValue());
            if (StrUtil.isNotBlank(activateRequest.getCouponName())) {
                coupon.setCouponName(activateRequest.getCouponName());
            }
            if (activateRequest.getDisplayAmount() != null) {
                coupon.setDisplayAmount(activateRequest.getDisplayAmount());
            }
            if (activateRequest.getActualAmount() != null) {
                coupon.setActualAmount(activateRequest.getActualAmount());
            }
        }

        return this.updateBatch(couponList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyCoupon(CouponVerifyRequest verifyRequest, HttpServletRequest request) {
        if (verifyRequest == null || StrUtil.isBlank(verifyRequest.getCouponPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "卡券密码不能为空");
        }

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("coupon_password", verifyRequest.getCouponPassword());
        Coupon coupon = this.getOne(queryWrapper);
        if (coupon == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到对应卡券");
        }

        if (!CouponStatusEnum.ACTIVE.getValue().equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该卡券不是已激活状态，无法核销");
        }

        User loginUser = userService.getLoginUser(request);

        coupon.setStatus(CouponStatusEnum.VERIFIED.getValue());
        coupon.setVerifyBy(loginUser.getUserName());
        coupon.setVerifyTime(LocalDateTime.now());

        return this.updateById(coupon);
    }

    @Override
    public CouponStatisticsVO getCouponStatistics() {
        CouponStatisticsVO vo = new CouponStatisticsVO();
        vo.setInactiveCount(this.count(QueryWrapper.create().eq("status", CouponStatusEnum.INACTIVE.getValue())));
        vo.setActiveCount(this.count(QueryWrapper.create().eq("status", CouponStatusEnum.ACTIVE.getValue())));
        vo.setVerifiedCount(this.count(QueryWrapper.create().eq("status", CouponStatusEnum.VERIFIED.getValue())));
        vo.setActiveDisplayAmount(this.mapper.sumDisplayAmountByStatus(CouponStatusEnum.ACTIVE.getValue()));
        vo.setActiveActualAmount(this.mapper.sumActualAmountByStatus(CouponStatusEnum.ACTIVE.getValue()));
        vo.setVerifiedDisplayAmount(this.mapper.sumDisplayAmountByStatus(CouponStatusEnum.VERIFIED.getValue()));
        vo.setVerifiedActualAmount(this.mapper.sumActualAmountByStatus(CouponStatusEnum.VERIFIED.getValue()));
        return vo;
    }

    @Override
    public QueryWrapper getQueryWrapper(CouponQueryRequest queryRequest) {
        if (queryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String couponNo = StrUtil.blankToDefault(queryRequest.getCouponNo(), null);
        String couponName = StrUtil.blankToDefault(queryRequest.getCouponName(), null);
        Integer status = queryRequest.getStatus();
        String verifyBy = StrUtil.blankToDefault(queryRequest.getVerifyBy(), null);

        return QueryWrapper.create()
                .eq("coupon_no", couponNo)
                .like("coupon_name", couponName)
                .eq("status", status)
                .like("verify_by", verifyBy)
                .orderBy("create_time", false);
    }

    @Override
    public CouponVO getCouponVO(Coupon coupon) {
        if (coupon == null) {
            return null;
        }
        CouponVO couponVO = new CouponVO();
        BeanUtil.copyProperties(coupon, couponVO);
        return couponVO;
    }

    @Override
    public List<CouponVO> getCouponVOList(List<Coupon> couponList) {
        if (CollUtil.isEmpty(couponList)) {
            return new ArrayList<>();
        }
        return couponList.stream().map(this::getCouponVO).collect(Collectors.toList());
    }

    /**
     * 获取当前数据库中最大的卡券号（数字部分）
     */
    private long getMaxCouponNo() {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .orderBy("coupon_no", false)
                .limit(1);
        Coupon coupon = this.getOne(queryWrapper);
        if (coupon == null || StrUtil.isBlank(coupon.getCouponNo())) {
            return COUPON_NO_START - 1;
        }
        long couponNo = Long.parseLong(coupon.getCouponNo());
        return Math.max(couponNo, COUPON_NO_START - 1);
    }

    /**
     * 生成8位随机密码（大写字母+数字）
     */
    private String generatePassword() {
        return RandomUtil.randomString(PASSWORD_CHARS, PASSWORD_LENGTH);
    }
}
