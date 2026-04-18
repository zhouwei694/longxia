package com.example.longxia.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.example.longxia.annotation.AuthCheck;
import com.example.longxia.common.BaseResponse;
import com.example.longxia.common.ResultUtils;
import com.example.longxia.constant.UserConstant;
import com.example.longxia.exception.BusinessException;
import com.example.longxia.exception.ErrorCode;
import com.example.longxia.exception.ThrowUtils;
import com.example.longxia.common.DeleteRequest;
import com.example.longxia.model.dto.coupon.CouponActivateRequest;
import com.example.longxia.model.dto.coupon.CouponGenerateRequest;
import com.example.longxia.model.dto.coupon.CouponQueryRequest;
import com.example.longxia.model.dto.coupon.CouponUpdateRequest;
import com.example.longxia.model.dto.coupon.CouponVerifiedUpdateRequest;
import com.example.longxia.model.dto.coupon.CouponVerifyRequest;
import com.example.longxia.model.entity.Coupon;
import com.example.longxia.model.entity.User;
import com.example.longxia.model.enums.CouponStatusEnum;
import com.example.longxia.model.enums.UserRoleEnum;
import com.example.longxia.model.vo.CouponStatisticsVO;
import com.example.longxia.model.vo.CouponExportVO;
import com.example.longxia.model.vo.CouponVO;
import com.example.longxia.service.CouponService;
import com.example.longxia.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    @Autowired
    private UserService userService;

    /**
     * 卡券统计数据（管理员）
     */
    @GetMapping("/statistics")
    public BaseResponse<CouponStatisticsVO> getCouponStatistics() {
        return ResultUtils.success(couponService.getCouponStatistics());
    }

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
     * 卡券列表
     * 分页查询，支持按卡券号、卡券名称、卡券状态、核销人筛选
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<CouponVO>> listCouponByPage(@RequestBody CouponQueryRequest couponQueryRequest) {
        ThrowUtils.throwIf(couponQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = couponQueryRequest.getPageNum();
        long pageSize = couponQueryRequest.getPageSize();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 获取当前用户具有的权限
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，拒绝
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        Page<Coupon> couponPage = couponService.page(Page.of(pageNum, pageSize),
                couponService.getQueryWrapper(couponQueryRequest));
        Page<CouponVO> couponVOPage = new Page<>(pageNum, pageSize, couponPage.getTotalRow());
        List<CouponVO> couponVOList = couponService.getCouponVOList(couponPage.getRecords());
        //权限是用户，并且在未激活，已激活状态下，密码变成******
        if(UserRoleEnum.USER.equals(userRoleEnum) && !Objects.equals(CouponStatusEnum.VERIFIED.getValue(),couponQueryRequest.getStatus())){
            couponVOList = couponVOList.stream().map(x -> {
                CouponVO couponVO = new CouponVO();
                BeanUtil.copyProperties(x, couponVO);
                couponVO.setCouponPassword("******");
                return couponVO;
            }).toList();
        }
        couponVOPage.setRecords(couponVOList);
        return ResultUtils.success(couponVOPage);
    }

    /**
     * 卡券导出
     * 导出所有未激活状态的卡券，只展示id、卡券名称、卡券号、卡券密码
     */
    @PostMapping("/export")
    public void exportCoupons(@RequestBody CouponQueryRequest couponQueryRequest, HttpServletRequest request,
                              HttpServletResponse response) {
        ThrowUtils.throwIf(couponQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 只查询未激活状态的卡券
        List<Coupon> couponList = couponService.list(
                QueryWrapper.create().eq("status", CouponStatusEnum.INACTIVE.getValue()));
        List<CouponExportVO> exportVOList = couponList.stream().map(coupon -> {
            CouponExportVO exportVO = new CouponExportVO();
            BeanUtil.copyProperties(coupon, exportVO);
            return exportVO;
        }).sorted(Comparator.comparing(CouponExportVO::getId)).toList();

        String fileName = URLEncoder.encode("卡券列表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        ExcelWriter writer = ExcelUtil.getWriter(true);
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName + ".xlsx");
            writer.renameSheet("卡券列表");
            writer.addHeaderAlias("id", "ID");
            writer.addHeaderAlias("couponName", "卡券名称");
            writer.addHeaderAlias("couponNo", "卡券号");
            writer.addHeaderAlias("couponPassword", "卡券密码");
            writer.setOnlyAlias(true);
            writer.write(exportVOList, true);
            writer.flush(response.getOutputStream(), false);
        } catch (Exception e) {
            if (!response.isCommitted()) {
                response.reset();
                response.setContentType("application/json;charset=UTF-8");
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "导出失败");
        } finally {
            writer.close();
        }
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
    public BaseResponse<Boolean> verifyCoupon(@RequestBody CouponVerifyRequest couponVerifyRequest,
                                              HttpServletRequest request) {
        ThrowUtils.throwIf(couponVerifyRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = couponService.verifyCoupon(couponVerifyRequest, request);
        return ResultUtils.success(result);
    }

    /**
     * 修改未激活卡券（管理员）
     * 仅允许修改卡券名称、展示金额、实际金额
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCoupon(@RequestBody CouponUpdateRequest couponUpdateRequest) {
        ThrowUtils.throwIf(couponUpdateRequest == null || couponUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long id = couponUpdateRequest.getId();
        Coupon coupon = couponService.getById(id);
        ThrowUtils.throwIf(coupon == null, ErrorCode.NOT_FOUND_ERROR, "卡券不存在");
        if (!CouponStatusEnum.INACTIVE.getValue().equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只能修改未激活状态的卡券");
        }
        if (couponUpdateRequest.getCouponName() != null) {
            coupon.setCouponName(couponUpdateRequest.getCouponName());
        }
        if (couponUpdateRequest.getDisplayAmount() != null) {
            coupon.setDisplayAmount(couponUpdateRequest.getDisplayAmount());
        }
        if (couponUpdateRequest.getActualAmount() != null) {
            coupon.setActualAmount(couponUpdateRequest.getActualAmount());
        }
        return ResultUtils.success(couponService.updateById(coupon));
    }

    /**
     * 单个激活未激活卡券（管理员）
     */
    @PostMapping("/activate/single")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> activateSingleCoupon(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        Coupon coupon = couponService.getById(id);
        ThrowUtils.throwIf(coupon == null, ErrorCode.NOT_FOUND_ERROR, "卡券不存在");
        if (!CouponStatusEnum.INACTIVE.getValue().equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只能激活未激活状态的卡券");
        }
        coupon.setStatus(CouponStatusEnum.ACTIVE.getValue());
        return ResultUtils.success(couponService.updateById(coupon));
    }

    /**
     * 删除未激活卡券（管理员）
     * 物理删除
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCoupon(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long id = deleteRequest.getId();
        Coupon coupon = couponService.getById(id);
        ThrowUtils.throwIf(coupon == null, ErrorCode.NOT_FOUND_ERROR, "卡券不存在");
        if (!CouponStatusEnum.INACTIVE.getValue().equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只能删除未激活状态的卡券");
        }
        return ResultUtils.success(couponService.removeById(id));
    }

    /**
     * 修改已核销卡券（管理员）
     * 仅允许修改收件人电话、收件人快递号
     */
    @PostMapping("/update/verified")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateVerifiedCoupon(@RequestBody CouponVerifiedUpdateRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR);
        Long id = request.getId();
        Coupon coupon = couponService.getById(id);
        ThrowUtils.throwIf(coupon == null, ErrorCode.NOT_FOUND_ERROR, "卡券不存在");
        if (!CouponStatusEnum.VERIFIED.getValue().equals(coupon.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只能修改已核销状态的卡券");
        }
        coupon.setRecipientPhone(request.getRecipientPhone());
        coupon.setRecipientExpressNo(request.getRecipientExpressNo());
        return ResultUtils.success(couponService.updateById(coupon));
    }
}
