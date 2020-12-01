package com.project.passbook.couponService.service;

import com.project.passbook.couponService.constants.CouponStatus;
import com.project.passbook.couponService.constants.CouponTemplateStatus;
import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.managers.CouponManager;
import com.project.passbook.couponService.managers.CouponTemplateManager;
import com.project.passbook.merchantService.model.responses.Response;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CouponServiceImpl implements CouponService {

  @Autowired
  private CouponManager couponManager;

  @Autowired
  private CouponTemplateManager couponTemplateManager;

  @Override
  @SneakyThrows
  public Response<Coupon> claimCoupon(String customerId, String couponTemplateId) {
    return new Response<>(couponManager.claimCoupon(customerId, couponTemplateId));
  }

  @Override
  public Response getCoupons(String customerId, CouponStatus couponStatus, CouponTemplateStatus couponTemplateStatus) {
    //
    List<Coupon> filteredCoupons = couponManager.getCoupons(customerId);
    couponTemplateManager.
    return null;
  }

  @Override
  public Response<List<Coupon>> getCoupons(String customerId) {
    return new Response<>(couponManager.getCoupons(customerId));
  }

  @Override
  public Response<List<Coupon>> getRedeemedCoupons(String customerId) {
    return new Response<>(couponManager.getRedeemedCoupons(customerId));
  }

  @Override
  public Response<Coupon> redeemCoupon(Coupon coupon) {
    return new Response<>(couponManager.redeemCoupon(coupon));
  }
}
