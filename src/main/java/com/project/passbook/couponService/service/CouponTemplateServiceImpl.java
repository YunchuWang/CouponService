package com.project.passbook.couponService.service;

import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.managers.CouponTemplateManager;
import com.project.passbook.merchantService.model.responses.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponTemplateServiceImpl implements CouponTemplateService {

  @Autowired
  private CouponTemplateManager couponTemplateManager;

  @Override
  @SneakyThrows
  public Response addCouponTemplate(CouponTemplate couponTemplate) {
    return new Response(couponTemplateManager.addCouponTemplate(couponTemplate));
  }

  @Override
  public Response getCouponTemplates(List<String> merchantIds) {
    return new Response(couponTemplateManager.getCouponTemplates(merchantIds));
  }
}
