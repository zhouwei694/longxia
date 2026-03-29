package com.example.longxia.mapper;

import com.mybatisflex.core.BaseMapper;
import com.example.longxia.model.entity.Coupon;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 卡券表 映射层。
 *
 * @author zhouwei
 */
public interface CouponMapper extends BaseMapper<Coupon> {

    @Select("SELECT COALESCE(SUM(display_amount), 0) FROM coupon WHERE status = #{status}")
    BigDecimal sumDisplayAmountByStatus(@Param("status") int status);

    @Select("SELECT COALESCE(SUM(actual_amount), 0) FROM coupon WHERE status = #{status}")
    BigDecimal sumActualAmountByStatus(@Param("status") int status);
}
