package com.project.passbook.couponService.managers;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.project.passbook.couponService.CouponServiceApplication;
import com.project.passbook.couponService.entities.Feedback;
import com.project.passbook.couponService.filters.KafkaListenersTypeExcludeFilter;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.exceptions.types.BadRequestException;
import com.project.passbook.merchantService.model.exceptions.types.ConflictException;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.filter.TypeExcludeFilters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest(classes = CouponServiceApplication.class)
@TypeExcludeFilters(KafkaListenersTypeExcludeFilter.class)
public class FeedbackManagerTest extends AbstractTestNGSpringContextTests {

  private static final BlockingQueue<Feedback> FEEDBACK_CLEAN_UP_LIST = new LinkedBlockingDeque<>();

  private FeedbackManager feedbackManager;

  @Autowired
  private HbaseTemplate hbaseTemplate;

  @BeforeClass
  public void setUp() {
    feedbackManager = new FeedbackManager(hbaseTemplate);
  }

  @AfterClass
  public void cleanUp() {
    FEEDBACK_CLEAN_UP_LIST.forEach(
        feedback -> feedbackManager.deleteFeedback(RowKeyGenUtil.generateFeedBackRowKey(feedback)));
  }

  @Test(expectedExceptions = BadRequestException.class)
  public void givenInvalidFeedbackType_whenCreateFeedback_thenThrowsException() {
    Feedback testFeedback = ManagerTestUtils.createTestFeedback();
    testFeedback.setType(RandomStringUtils.randomAlphanumeric(12));
    testFeedback.setFeedbackId(RowKeyGenUtil.generateFeedBackRowKey(testFeedback));
    feedbackManager.createFeedback(testFeedback);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void givenFeedbackAlreadyExist_whenCreateFeedback_thenThrowsException() {
    Feedback feedback = ManagerTestUtils.createTestFeedback();
    feedback.setFeedbackId(RowKeyGenUtil.generateFeedBackRowKey(feedback));
    FEEDBACK_CLEAN_UP_LIST.add(feedbackManager.createFeedback(feedback));
    feedbackManager.createFeedback(feedback);
  }

  @Test
  public void givenFeedback_whenGetFeedback_thenSucceed() throws InterruptedException {
    Feedback feedback1 = ManagerTestUtils.createTestFeedback();
    Feedback feedback2 = ManagerTestUtils.createTestFeedback();
    feedback2.setCustomerId(feedback1.getCustomerId());
    feedback1.setFeedbackId(RowKeyGenUtil.generateFeedBackRowKey(feedback1));
    Thread.sleep(1000);
    feedback2.setFeedbackId(RowKeyGenUtil.generateFeedBackRowKey(feedback2));

    FEEDBACK_CLEAN_UP_LIST.add(feedbackManager.createFeedback(feedback1));
    FEEDBACK_CLEAN_UP_LIST.add(feedbackManager.createFeedback(feedback2));

    List<Feedback> feedbackList = feedbackManager.getFeedback(feedback1.getCustomerId());

    assertEquals(feedbackList.size(), 2);
    assertTrue(feedbackList.contains(feedback1));
    assertTrue(feedbackList.contains(feedback2));
  }
}