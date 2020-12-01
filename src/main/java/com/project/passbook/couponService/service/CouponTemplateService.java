package com.project.passbook.couponService.service;

import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.merchantService.model.responses.Response;
import java.util.List;

public interface CouponTemplateService {
  Response addCouponTemplate(CouponTemplate couponTemplate);
  Response getCouponTemplates(List<String> merchantIds);
}
