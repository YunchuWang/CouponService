package com.project.passbook.couponService.managers;

import static org.junit.Assert.assertEquals;

import com.project.passbook.couponService.CouponServiceApplication;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.filters.KafkaListenersTypeExcludeFilter;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.exceptions.types.ConditionalUpdateFailureException;
import com.project.passbook.merchantService.model.exceptions.types.ConflictException;
import com.project.passbook.merchantService.model.exceptions.types.NotFoundException;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest(classes = CouponServiceApplication.class)
@TypeExcludeFilters(KafkaListenersTypeExcludeFilter.class)
public class CouponTemplateManagerTest extends AbstractTestNGSpringContextTests {

  private static final String NON_EXISTING_COUPON_TEMPLATE_ID_1 = "NO_ID_1";
  private static final String NON_EXISTING_COUPON_TEMPLATE_ID_2 = "NO_ID_2";
  private static final String ADD_COUPON_TEMPLATE_TESTS = "AddCouponTemplates";

  @Autowired
  private CouponTemplateManager couponTemplateManager;

  private CouponTemplate TEST_COUPON_TEMPLATE_1;
  private CouponTemplate TEST_COUPON_TEMPLATE_2;

  @BeforeClass
  public void setUp() {
    TEST_COUPON_TEMPLATE_1 = ManagerTestUtils.createTestCouponTemplate(false);
    TEST_COUPON_TEMPLATE_2 = ManagerTestUtils.createTestCouponTemplate(false);
  }

  @AfterMethod
  public void cleanCouponTemplates() {
    couponTemplateManager.deleteCouponTemplate(TEST_COUPON_TEMPLATE_1);
    couponTemplateManager.deleteCouponTemplate(TEST_COUPON_TEMPLATE_2);
  }

  @Test(groups = ADD_COUPON_TEMPLATE_TESTS)
  public void givenCouponTemplate_whenAddCouponTemplate_thenSucceed() throws InterruptedException {
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_1);
    CouponTemplate foundCouponTemplate = couponTemplateManager.getCouponTemplate(RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_1));
    assertEquals(TEST_COUPON_TEMPLATE_1, foundCouponTemplate);
  }

  @Test(expectedExceptions = ConflictException.class, groups = ADD_COUPON_TEMPLATE_TESTS)
  void givenExistingCouponTemplate_whenAddCouponTemplate_thenThrowException() {
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_1);
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_1);
  }

  @Test(expectedExceptions = NotFoundException.class, groups = ADD_COUPON_TEMPLATE_TESTS)
  void givenNonExistingCouponTemplateId_whenGetCouponTemplate_thenThrowException() {
    couponTemplateManager.getCouponTemplate(NON_EXISTING_COUPON_TEMPLATE_ID_1);
  }

  @Test(dependsOnGroups = ADD_COUPON_TEMPLATE_TESTS)
  void givenCouponTemplateIds_whenGetCouponTemplates_thenSucceed() {
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_1);
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_2);

    List<CouponTemplate> couponTemplates = couponTemplateManager.getCouponTemplates(Arrays.asList(RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_1),
                                                                                                  RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_2)));

    assertEquals(2, couponTemplates.size());
    assertEquals(RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_1), RowKeyGenUtil.generateCouponTemplateRowKey(couponTemplates.get(0)));
    assertEquals(RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_2), RowKeyGenUtil.generateCouponTemplateRowKey(couponTemplates.get(1)));
  }

  @Test(expectedExceptions = NotFoundException.class)
  void givenNonExistingCouponTemplateIds_whenGetCouponTemplate_thenThrowException() {
    List<CouponTemplate> couponTemplates = couponTemplateManager.getCouponTemplates(Arrays.asList(NON_EXISTING_COUPON_TEMPLATE_ID_1,
                                                                                                  NON_EXISTING_COUPON_TEMPLATE_ID_2));
  }

  @Test(dependsOnGroups = ADD_COUPON_TEMPLATE_TESTS)
  void givenCouponTemplateId_whenDecreaseCouponTemplateLimit_thenSucceed() {
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_1);
    couponTemplateManager.decreaseCouponTemplateLimit(TEST_COUPON_TEMPLATE_1);

    CouponTemplate updatedCouponTemplate = couponTemplateManager.getCouponTemplate(RowKeyGenUtil.generateCouponTemplateRowKey(TEST_COUPON_TEMPLATE_1));

    assertEquals(Long.valueOf(TEST_COUPON_TEMPLATE_1.getConstraint().getLimit() - 1), updatedCouponTemplate.getConstraint().getLimit());
  }

  @Test(expectedExceptions = NotFoundException.class)
  void givenNonExistentCouponTemplateId_whenDecreaseCouponTemplateLimit_thenThrowException() {
    CouponTemplate template = ManagerTestUtils.createTestCouponTemplate(false);
    couponTemplateManager.decreaseCouponTemplateLimit(template);
  }

  @Test(expectedExceptions = ConditionalUpdateFailureException.class)
  void givenCouponTemplate_whenDecreaseCouponTemplateLimitBelowZero_thenLimitZero() {
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_2);
    couponTemplateManager.decreaseCouponTemplateLimit(TEST_COUPON_TEMPLATE_2);
    couponTemplateManager.decreaseCouponTemplateLimit(TEST_COUPON_TEMPLATE_2);
  }

  @Test
  void givenCouponTemplates_whenFindCouponTemplatesInStock_thenSucceed() {
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_1);
    couponTemplateManager.addCouponTemplate(TEST_COUPON_TEMPLATE_2);
    couponTemplateManager.decreaseCouponTemplateLimit(TEST_COUPON_TEMPLATE_2);

    List<CouponTemplate> couponTemplates = couponTemplateManager.getCouponTemplatesInStock();

    assertEquals(1, couponTemplates.size());
    assertEquals(TEST_COUPON_TEMPLATE_1, couponTemplates.get(0));
  }
}

