package com.project.passbook.couponService.utils;

import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.entities.Feedback;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

@Slf4j
public class RowKeyGenUtil {

  public static String generateCouponTemplateRowKey(CouponTemplate couponTemplate) {
    String rowKey = DigestUtils.md5Hex(String.valueOf(couponTemplate.getBasicInfo().getMerchantId())) + "_"
        + couponTemplate.getBasicInfo().getTitle();
    log.info("Generating row key for coupon template: {}", rowKey);

    return rowKey;
  }

  public static String generateFeedBackRowKey(Feedback feedback) {
    return new StringBuilder(feedback.getCustomerId()).reverse().toString() + (Long.MAX_VALUE
        - System.currentTimeMillis());
  }

  public static String generateCouponRowKey(Coupon coupon) {
    StringBuilder sb = new StringBuilder();
    sb.append(coupon.getCustomerId()).reverse();
    sb.append(Long.MAX_VALUE - System.currentTimeMillis());
    sb.append(coupon.getTemplateId());

    return sb.toString();
  }
}
