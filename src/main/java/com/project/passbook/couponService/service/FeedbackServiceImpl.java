package com.project.passbook.couponService.service;

import com.project.passbook.couponService.entities.Feedback;
import com.project.passbook.couponService.managers.FeedbackManager;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.responses.Response;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

  @Autowired
  private FeedbackManager feedbackManager;

  @Override
  @SneakyThrows
  public Response createFeedback(Feedback feedback) {
    feedback.setFeedbackId(RowKeyGenUtil.generateFeedBackRowKey(feedback));
    return new Response(feedbackManager.createFeedback(feedback));
  }

  @Override
  @SneakyThrows
  public Response getFeedback(String customerId) {
    return new Response(feedbackManager.getFeedback(customerId));
  }
}
