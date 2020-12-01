package com.project.passbook.couponService.managers;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.project.passbook.couponService.CouponServiceApplication;
import com.project.passbook.couponService.filters.KafkaListenersTypeExcludeFilter;
import com.project.passbook.merchantService.model.exceptions.types.NotFoundException;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest(classes = CouponServiceApplication.class)
@TypeExcludeFilters(KafkaListenersTypeExcludeFilter.class)
public class TokenManagerTest extends AbstractTestNGSpringContextTests {

  private static final String TEST_COUPON_TEMPLATE_ID = RandomStringUtils.randomAlphanumeric(12);

  private TokenManager tokenManager;

  @Autowired
  private StringRedisTemplate stringRedisTemplate;

  @BeforeClass
  public void setUp() {
    tokenManager = new TokenManager(stringRedisTemplate);
  }

  @AfterMethod
  public void cleanUp() {
    stringRedisTemplate.delete(TEST_COUPON_TEMPLATE_ID);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void givenNoCouponTokensInStock_whenGetCouponToken_thenThrowsException() {
    tokenManager.getCouponToken(TEST_COUPON_TEMPLATE_ID);
  }

  @Test
  public void givenCouponTokensInStock_whenGetCouponToken_thenSucceed() {
    tokenManager.populateTokensForCouponTemplate(TEST_COUPON_TEMPLATE_ID, 1);

    assertNotNull(tokenManager.getCouponToken(TEST_COUPON_TEMPLATE_ID));
  }

  @Test
  public void givenNoTokens_whenPopulateTokens_thenSucceed() {
    tokenManager.populateTokensForCouponTemplate(TEST_COUPON_TEMPLATE_ID, 2);

    assertEquals(stringRedisTemplate.opsForSet().size(TEST_COUPON_TEMPLATE_ID).intValue(), 2);
  }
}