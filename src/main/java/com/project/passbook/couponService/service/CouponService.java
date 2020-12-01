package com.project.passbook.couponService.service;

import com.project.passbook.couponService.constants.CouponStatus;
import com.project.passbook.couponService.constants.CouponTemplateStatus;
import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.merchantService.model.responses.Response;

public interface CouponService {
  Response claimCoupon(String customerId, String couponTemplateId);
  Response getCoupons(String customerId, CouponStatus couponStatus, CouponTemplateStatus couponTemplateStatus);
  Response getRedeemedCoupons(String customerId);
  Response redeemCoupon(Coupon coupon);
}
