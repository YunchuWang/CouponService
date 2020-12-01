package com.project.passbook.couponService.consumers;

import com.project.passbook.couponService.constants.Constants;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.service.CouponTemplateService;
import com.project.passbook.couponService.utils.ParsingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponTemplateConsumer {

  private final CouponTemplateService couponTemplateService;

  @KafkaListener(topics = {Constants.TEMPLATE_TOPIC})
  public void listenForCouponTemplateRecord(@Payload String message,
      @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String key,
      @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    log.info("Received the coupon template {}", message);
    try {
      CouponTemplate couponTemplate = ParsingUtils.parser.fromJson(message, CouponTemplate.class);
      log.info("Adding the coupon template {}", couponTemplate);
      couponTemplateService.addCouponTemplate(couponTemplate);
    } catch (Exception e) {
      log.error("Parsing the coupon template error: {}", message);
    }
  }
}
