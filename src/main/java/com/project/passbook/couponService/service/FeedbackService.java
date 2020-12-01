package com.project.passbook.couponService.service;

import com.project.passbook.couponService.entities.Feedback;
import com.project.passbook.merchantService.model.responses.Response;

public interface FeedbackService {
  Response createFeedback(Feedback feedback);
  Response getFeedback(String customerId);
}
