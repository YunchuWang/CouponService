package com.project.passbook.couponService.managers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.project.passbook.couponService.CouponServiceApplication;
import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.filters.KafkaListenersTypeExcludeFilter;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.exceptions.types.BadRequestException;
import com.project.passbook.merchantService.model.exceptions.types.ConditionalUpdateFailureException;
import com.project.passbook.merchantService.model.exceptions.types.NotFoundException;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.lang.RandomStringUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SpringBootTest(classes = CouponServiceApplication.class)
@TypeExcludeFilters(KafkaListenersTypeExcludeFilter.class)
public class CouponManagerTest extends AbstractTestNGSpringContextTests {

  private static final String NON_EXISTENT_CUSTOMER_ID = "nonExistentCustomerId";
  private static final String NON_EXISTENT_COUPON_TEMPLATE_ID = "nonExistentCouponTemplateId";
  private static final BlockingQueue<Coupon> COUPON_CLEAN_UP_LIST = new LinkedBlockingDeque<>();
  private static final String TEST_CUSTOMER_ID = RandomStringUtils.randomAlphanumeric(12);

  private CouponTemplate TEST_COUPON_TEMPLATE_WITH_TOKEN;
  private CouponTemplate TEST_COUPON_TEMPLATE_WITHOUT_TOKEN;

  private CouponManager couponManager;

  @Autowired
  private HbaseTemplate hbaseTemplate;

  @Mock
  private CouponTemplateManager couponTemplateManager;

  @Mock
  private TokenManager tokenManager;

  @Mock
  private RedissonClient redissonClient;

  @Mock
  private RLock lock;

  @BeforeClass
  public void setUp() {
    TEST_COUPON_TEMPLATE_WITH_TOKEN = ManagerTestUtils.createTestCouponTemplate(true);
    TEST_COUPON_TEMPLATE_WITHOUT_TOKEN = ManagerTestUtils.createTestCouponTemplate(false);
  }

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    couponManager = new CouponManager(tokenManager, couponTemplateManager, hbaseTemplate, redissonClient);
    when(redissonClient.getLock(any())).thenReturn(lock);
  }

  @AfterMethod
  public void cleanUpCoupons() {
    COUPON_CLEAN_UP_LIST.forEach(coupon -> couponManager.deleteCoupon(coupon));
  }

  // TODO: Change this test
  @Test(expectedExceptions = BadRequestException.class, enabled = false)
  public void givenNonExistentCustomerId_whenClaimCoupon_thenThrowsException() {
//    doThrow(NotFoundException.class).when(customerManager).checkCustomerExistence(NON_EXISTENT_CUSTOMER_ID);
    couponManager.claimCoupon(NON_EXISTENT_CUSTOMER_ID, NON_EXISTENT_COUPON_TEMPLATE_ID);
  }

  @Test(expectedExceptions = BadRequestException.class)
  public void givenNonExistentCouponTemplateId_whenClaimCoupon_thenThrowsException() {
    doThrow(NotFoundException.class).when(couponTemplateManager).checkCouponTemplateExistence(NON_EXISTENT_COUPON_TEMPLATE_ID);
    couponManager.claimCoupon(TEST_CUSTOMER_ID, NON_EXISTENT_COUPON_TEMPLATE_ID);
  }

  @Test(expectedExceptions = ConditionalUpdateFailureException.class)
  public void givenCouponOutOfStock_whenClaimCoupon_thenThrowsException() {
    when(couponTemplateManager.getCouponTemplate(any())).thenReturn(TEST_COUPON_TEMPLATE_WITHOUT_TOKEN);
    doThrow(ConditionalUpdateFailureException.class).when(couponTemplateManager).decreaseCouponTemplateLimit(any());
    when(redissonClient.getLock(any())).thenReturn(lock);
    couponManager.claimCoupon(TEST_CUSTOMER_ID, RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_WITHOUT_TOKEN));
  }

  @Test(expectedExceptions = Exception.class)
  public void givenCouponWithTokenOutOfStock_whenClaimCoupon_thenThrowException() {
    doThrow(Exception.class).when(
        tokenManager).getCouponToken(RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_WITH_TOKEN));
    when(couponTemplateManager.getCouponTemplate(any())).thenReturn(TEST_COUPON_TEMPLATE_WITH_TOKEN);
    couponManager.claimCoupon(TEST_CUSTOMER_ID,
                              RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_WITH_TOKEN));
  }

  @Test
  public void givenCouponWithoutToken_whenClaimCoupon_thenSucceed() {
    when(couponTemplateManager.getCouponTemplate(any())).thenReturn(TEST_COUPON_TEMPLATE_WITHOUT_TOKEN);
    COUPON_CLEAN_UP_LIST.add(couponManager.claimCoupon(TEST_CUSTOMER_ID, RowKeyGenUtil.generateCouponTemplateRowKey(
        TEST_COUPON_TEMPLATE_WITHOUT_TOKEN)));
  }

  @Test
  public void givenCouponWithToken_whenClaimCoupon_thenSucceed() {
    when(tokenManager.getCouponToken(
        RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_WITH_TOKEN))).thenReturn(
        RandomStringUtils.randomAlphanumeric(12));
    when(couponTemplateManager.getCouponTemplate(any())).thenReturn(TEST_COUPON_TEMPLATE_WITH_TOKEN);
    COUPON_CLEAN_UP_LIST.add(couponManager.claimCoupon(TEST_CUSTOMER_ID, RowKeyGenUtil.generateCouponTemplateRowKey(
        TEST_COUPON_TEMPLATE_WITH_TOKEN)));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void givenNonExistentCoupon_whenRedeemCoupon_thenThrowException() {
    Coupon testCoupon = ManagerTestUtils.createTestCoupon(RandomStringUtils.randomAlphanumeric(12),
                                                    RandomStringUtils.randomAlphanumeric(12));
    couponManager.redeemCoupon(testCoupon);
  }

  @Test(expectedExceptions = BadRequestException.class)
  public void givenAlreadyRedeemedCoupon_whenRedeemCoupon_thenThrowException() {
    Coupon testCoupon = ManagerTestUtils.createTestCoupon(RandomStringUtils.randomAlphanumeric(12),
                                                          RandomStringUtils.randomAlphanumeric(12));

    couponManager.addCoupon(testCoupon);
    couponManager.redeemCoupon(testCoupon);
    couponManager.redeemCoupon(testCoupon);
  }

  @Test
  public void givenCoupon_whenRedeemCoupon_thenSucceed() {
    Coupon testCoupon = ManagerTestUtils.createTestCoupon(RandomStringUtils.randomAlphanumeric(12),
                                                          RandomStringUtils.randomAlphanumeric(12));

    COUPON_CLEAN_UP_LIST.add(couponManager.addCoupon(testCoupon));
    couponManager.redeemCoupon(testCoupon);
  }

  @Test
  public void givenNoCoupons_whenFindCouponsByCustomer_thenSucceed() {
    List<Coupon> coupons = couponManager.getCoupons(TEST_CUSTOMER_ID);
    assertEquals(coupons.size(), 0);
  }

  @Test
  public void givenCoupons_whenFindCouponsByCustomer_thenSucceed() {
    Coupon customerCoupon1 = ManagerTestUtils.createTestCoupon(TEST_CUSTOMER_ID,
                                                           RandomStringUtils.randomAlphanumeric(12));
    Coupon customerCoupon2 = ManagerTestUtils.createTestCoupon(TEST_CUSTOMER_ID,
                                                           RandomStringUtils.randomAlphanumeric(12));
    COUPON_CLEAN_UP_LIST.add(couponManager.addCoupon(customerCoupon1));
    COUPON_CLEAN_UP_LIST.add(couponManager.addCoupon(customerCoupon2));
    List<Coupon> coupons = couponManager.getCoupons(TEST_CUSTOMER_ID);
    assertEquals(coupons.size(), 2);
  }
}

