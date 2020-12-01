package com.project.passbook.couponService.managers;

import com.project.passbook.merchantService.model.exceptions.types.NotFoundException;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
public class TokenManager {

  @Autowired
  private StringRedisTemplate redisTemplate;

  @SneakyThrows
  public String getCouponToken(String couponTemplateId) {
    String token = redisTemplate.opsForSet().pop(couponTemplateId);
    if (token == null) {
      throw new NotFoundException(String.format("Coupon token unavailable for %s", couponTemplateId));
    }
    return token;
  }

  // This is not thread safe but shall be executed by one thread only
  public void populateTokensForCouponTemplate(String couponTemplateId, long limit) {
    while (redisTemplate.opsForSet().size(couponTemplateId) < limit) {
      redisTemplate.opsForSet().add(couponTemplateId, UUID.randomUUID().toString());
    }
  }
}
