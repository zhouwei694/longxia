import request from './request'
import type {
  BaseResponse,
  PageResult,
  CouponVO,
  CouponStatisticsVO,
  CouponQueryRequest,
  CouponGenerateRequest,
  CouponActivateRequest,
  CouponVerifyRequest,
  CouponUpdateRequest,
  CouponVerifiedUpdateRequest,
} from '../types'

export function getCouponStatistics(): Promise<BaseResponse<CouponStatisticsVO>> {
  return request.get('/coupon/statistics')
}

export function listCouponByPage(
  data: CouponQueryRequest,
): Promise<BaseResponse<PageResult<CouponVO>>> {
  return request.post('/coupon/list/page', data)
}

export function generateCoupons(
  data: CouponGenerateRequest,
): Promise<BaseResponse<boolean>> {
  return request.post('/coupon/generate', data)
}

export function activateCoupons(
  data: CouponActivateRequest,
): Promise<BaseResponse<boolean>> {
  return request.post('/coupon/activate', data)
}

export function verifyCoupon(
  data: CouponVerifyRequest,
): Promise<BaseResponse<boolean>> {
  return request.post('/coupon/verify', data)
}

export function exportCoupons(data: CouponQueryRequest): Promise<Blob> {
  return request.postBlob('/coupon/export', data)
}

export function updateCoupon(data: CouponUpdateRequest): Promise<BaseResponse<boolean>> {
  return request.post('/coupon/update', data)
}

export function activateSingleCoupon(id: number): Promise<BaseResponse<boolean>> {
  return request.post('/coupon/activate/single', { id })
}

export function deleteCoupon(id: number): Promise<BaseResponse<boolean>> {
  return request.post('/coupon/delete', { id })
}

export function updateVerifiedCoupon(data: CouponVerifiedUpdateRequest): Promise<BaseResponse<boolean>> {
  return request.post('/coupon/update/verified', data)
}
