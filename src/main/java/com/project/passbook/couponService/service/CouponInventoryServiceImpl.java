package com.project.passbook.couponService.service;

import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.couponService.entities.CouponDetail;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.managers.CouponInventoryManager;
import com.project.passbook.merchantService.model.responses.Response;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@NoArgsConstructor
public class CouponInventoryServiceImpl implements CouponInventoryService {

  @Autowired
  private CouponInventoryManager couponInventoryManager;

  @Override
  public Response<List<CouponTemplate>> getAvailableCouponTemplates(String customerId) {
    return new Response<>(couponInventoryManager.getAvailableSystemCouponTemplates(customerId));
  }

  @Override
  public Response<List<CouponTemplate>> getExpiredCouponTemplates(String customerId) {
    return new Response<>(couponInventoryManager.getExpiredCustomerCouponTemplates(customerId));
  }

  @Override
  public Response<List<CouponDetail>> getCouponDetails(String customerId) {

    return new Response<>(couponInventoryManager.getActiveCustomerCoupons(customerId));
  }
}
