package com.project.passbook.couponService.managers;

import com.project.passbook.couponService.constants.FeedbackType;
import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.entities.CouponTemplate.BasicInfo;
import com.project.passbook.couponService.entities.CouponTemplate.Constraint;
import com.project.passbook.couponService.entities.Feedback;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.entities.Customer;
import com.project.passbook.merchantService.model.entities.Customer.BaseInfo;
import com.project.passbook.merchantService.model.entities.Customer.ContactInfo;
import com.project.passbook.merchantService.model.entities.Merchant;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.RandomUtils;

public class ManagerTestUtils {

  public static Customer createTestCustomer()
  {
    return Customer.builder()
               .id(UUID.randomUUID().toString())
               .baseInfo(BaseInfo.builder()
                                 .age(RandomUtils.nextInt(0, 130))
                                 .name(RandomStringUtils.randomAlphabetic(12))
                                 .sex(RandomStringUtils.random(6))
                                 .build())
               .contactInfo(ContactInfo.builder()
                                       .address(RandomStringUtils.randomAlphanumeric(12))
                                       .phone(RandomStringUtils.randomNumeric(9))
                                       .build())
               .build();
  }

  public static Coupon createTestCoupon(String customerId, String couponTemplateId)
  {
    Coupon coupon = Coupon.builder()
                 .customerId(customerId)
                 .templateId(couponTemplateId)
                 .assignedDate(new Date())
                 .token("-1")
                 .build();
    coupon.setCouponId(RowKeyGenUtil.generateCouponRowKey(coupon));

    return coupon;
  }

  public static Merchant createTestMerchant() {
    return Merchant.builder().address(RandomStringUtils.random(12)).businessLicenseUrl(RandomStringUtils.random(12)).id(
        RandomUtils.nextInt()).isAudit(false).logoUrl(RandomStringUtils.random(12)).phone(
        RandomStringUtils.randomNumeric(9)).name(RandomStringUtils.random(12)).build();
  }

  public static Feedback createTestFeedback() {
    return Feedback.builder().comment(RandomStringUtils.randomAlphanumeric(12)).templateId(
        RandomStringUtils.randomAlphanumeric(12)).type(FeedbackType.APP.toString()).customerId(
        RandomStringUtils.randomAlphanumeric(12)).build();
  }

  public static CouponTemplate createTestCouponTemplate(boolean hasToken)
  {
    CouponTemplate couponTemplate =
        CouponTemplate.builder()
                      .basicInfo(
                          BasicInfo.builder()
                                   .background(1)
                                   .desc(RandomStringUtils.random(12))
                                   .hasToken(hasToken)
                                   .merchantId(RandomUtils.nextInt(1, Integer.MAX_VALUE))
                                   .summary(RandomStringUtils.random(12))
                                   .title(RandomStringUtils.random(12))
                                   .build())
                      .constraint(Constraint.builder()
                                            .limit(1L)
                                            .startTime(
                                                DateUtils.truncate(Date.from(Instant.now()),
                                                                   java.util.Calendar.DAY_OF_MONTH))
                                            .endTime(
                                                DateUtils.truncate(
                                                    Date.from(Instant.now().plus(RandomUtils.nextInt(30, 60), ChronoUnit.DAYS)),
                                                    java.util.Calendar.DAY_OF_MONTH))
                                            .build())
                      .build();

    return couponTemplate;
  }
}
