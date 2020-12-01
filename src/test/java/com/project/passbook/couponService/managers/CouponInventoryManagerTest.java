package com.project.passbook.couponService.managers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.couponService.entities.CouponDetail;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.entities.Merchant;
import com.project.passbook.merchantService.model.exceptions.types.NotFoundException;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CouponInventoryManagerTest {

  private CouponTemplate TEST_COUPON_TEMPLATE_AVAILABLE;
  private CouponTemplate TEST_COUPON_TEMPLATE_EXPIRED;
  private CouponDetail TEST_COUPON_DETAIL_ACTIVE;
  private Coupon TEST_COUPON_REDEEMED;
  private Coupon TEST_COUPON_ACTIVE;
  private Merchant TEST_MERCHANT;

  private static final String TEST_CUSTOMER_ID = RandomStringUtils.randomAlphanumeric(12);
  private static final String TEST_MERCHANT_SERVICE_URL = RandomStringUtils.randomAlphanumeric(12);

  private CouponInventoryManager couponInventoryManager;

  @Mock
  private CouponManager couponManager;

  @Mock
  private CouponTemplateManager couponTemplateManager;

  @Mock
  private RestTemplate restTemplate;


  @BeforeClass
  public void setUp() {
    TEST_MERCHANT = ManagerTestUtils.createTestMerchant();

    TEST_COUPON_TEMPLATE_AVAILABLE = ManagerTestUtils.createTestCouponTemplate(false);
    TEST_COUPON_TEMPLATE_AVAILABLE.getBasicInfo().setMerchantId(TEST_MERCHANT.getId());

    TEST_COUPON_TEMPLATE_EXPIRED = ManagerTestUtils.createTestCouponTemplate(false);
    TEST_COUPON_TEMPLATE_EXPIRED.getBasicInfo().setMerchantId(TEST_MERCHANT.getId());
    TEST_COUPON_TEMPLATE_EXPIRED.getConstraint().setEndTime(new Date());

    TEST_COUPON_ACTIVE = ManagerTestUtils.createTestCoupon(TEST_CUSTOMER_ID, RowKeyGenUtil.generateCouponTemplateRowKey(
        TEST_COUPON_TEMPLATE_AVAILABLE));

    TEST_COUPON_REDEEMED = ManagerTestUtils.createTestCoupon(TEST_CUSTOMER_ID, RowKeyGenUtil.generateCouponTemplateRowKey(
        TEST_COUPON_TEMPLATE_EXPIRED));
    TEST_COUPON_REDEEMED.setRedeemedDate(new Date());

    TEST_COUPON_DETAIL_ACTIVE = CouponDetail.builder().coupon(TEST_COUPON_ACTIVE).couponTemplate(
        TEST_COUPON_TEMPLATE_AVAILABLE).merchant(TEST_MERCHANT).build();
  }

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    couponInventoryManager = new CouponInventoryManager(TEST_MERCHANT_SERVICE_URL, restTemplate, couponTemplateManager,
                                                        couponManager);
    when(couponTemplateManager.isCouponTemplateActive(any())).thenCallRealMethod();
  }


  @Test
  public void givenCouponTemplates_whenGetAvailableCouponTemplatesInSystem_thenSucceed() {
    when(couponManager.getCoupons(TEST_CUSTOMER_ID)).thenReturn(ImmutableList.of(TEST_COUPON_REDEEMED));
    when(couponTemplateManager.getCouponTemplatesInStock()).thenReturn(
        ImmutableList.of(TEST_COUPON_TEMPLATE_AVAILABLE));

    List<CouponTemplate> couponTemplates = couponInventoryManager.getAvailableSystemCouponTemplates(TEST_CUSTOMER_ID);

    assertEquals(couponTemplates.size(), 1);
    assertEquals(couponTemplates.get(0), TEST_COUPON_TEMPLATE_AVAILABLE);
  }

  @Test
  public void givenCouponTemplates_whenGetExpiredCustomerCouponTemplates_thenSucceed() {
    when(couponManager.getCoupons(TEST_CUSTOMER_ID)).thenReturn(ImmutableList.of(TEST_COUPON_ACTIVE));
    when(couponTemplateManager.getCouponTemplates(any())).thenReturn(ImmutableList.of(TEST_COUPON_TEMPLATE_EXPIRED));

    assertEquals(1, couponInventoryManager.getExpiredCustomerCouponTemplates(TEST_CUSTOMER_ID).size());
  }

  @Test
  public void givenActiveCouponTemplates_whenGetExpiredCustomerCouponTemplates_thenSucceed() {
    when(couponManager.getCoupons(TEST_CUSTOMER_ID)).thenReturn(ImmutableList.of(TEST_COUPON_ACTIVE));
    when(couponTemplateManager.getCouponTemplates(any())).thenReturn(ImmutableList.of(TEST_COUPON_TEMPLATE_EXPIRED));

    assertEquals(couponInventoryManager.getExpiredCustomerCouponTemplates(TEST_CUSTOMER_ID).size(), 1);
    assertEquals(couponInventoryManager.getExpiredCustomerCouponTemplates(TEST_CUSTOMER_ID).get(0),
                 TEST_COUPON_TEMPLATE_EXPIRED);
  }

  @Test
  public void givenRedeemedCoupons_whenGetRedeemedCoupons_thenSucceed() {
    when(couponManager.getCoupons(any())).thenReturn(ImmutableList.of(TEST_COUPON_REDEEMED, TEST_COUPON_ACTIVE));

    List<Coupon> coupons = couponInventoryManager.getRedeemedCoupons(TEST_CUSTOMER_ID);

    assertEquals(coupons.size(), 1);
    assertEquals(coupons.get(0), TEST_COUPON_REDEEMED);
  }

  @Test
  public void givenActiveCustomerCoupons_whenGetActiveCustomerCoupons_thenSucceed() {
    when(couponManager.getCoupons(TEST_CUSTOMER_ID)).thenReturn(ImmutableList.of(TEST_COUPON_ACTIVE, TEST_COUPON_REDEEMED));
    when(couponTemplateManager.getCouponTemplates(any())).thenReturn(
        ImmutableList.of(TEST_COUPON_TEMPLATE_EXPIRED, TEST_COUPON_TEMPLATE_AVAILABLE));
    when(restTemplate.exchange(eq(TEST_MERCHANT_SERVICE_URL), eq(HttpMethod.POST), Mockito.<HttpEntity>any(),
                               Mockito.<ParameterizedTypeReference<List<Merchant>>>any())).thenReturn(
        ResponseEntity.ok(ImmutableList.of(TEST_MERCHANT)));

    List<CouponDetail> coupons = couponInventoryManager.getActiveCustomerCoupons(TEST_CUSTOMER_ID);

    assertEquals(coupons.size(), 1);
    assertEquals(coupons.get(0), TEST_COUPON_DETAIL_ACTIVE);
  }

  @Test
  public void givenNoCouponTemplates_whenGetActiveCustomerCoupons_thenEmptyResult() {
    when(couponManager.getCoupons(TEST_CUSTOMER_ID)).thenReturn(ImmutableList.of(TEST_COUPON_ACTIVE, TEST_COUPON_REDEEMED));
    doThrow(NotFoundException.class).when(couponTemplateManager).getCouponTemplates(any());

    List<CouponDetail> coupons = couponInventoryManager.getActiveCustomerCoupons(TEST_CUSTOMER_ID);

    assertEquals(coupons.size(), 0);
  }
}
