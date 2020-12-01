package com.project.passbook.couponService.service;

import com.project.passbook.merchantService.model.responses.Response;

public interface CouponInventoryService {
  Response getAvailableCouponTemplates(String customerId);
  Response getExpiredCouponTemplates(String customerId);
  Response getCouponDetails(String customerId);
}
