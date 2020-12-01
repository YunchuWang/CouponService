package com.project.passbook.couponService.managers;

import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.couponService.entities.CouponDetail;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.entities.Merchant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class CouponInventoryManager {

  @Value("${merchant-service.url}")
  private String merchantServiceUrl;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private CouponTemplateManager couponTemplateManager;

  @Autowired
  private CouponManager couponManager;

  // Limit > 0 and active and not owned by customer
  @SneakyThrows
  public List<CouponTemplate> getAvailableSystemCouponTemplates(String customerId) {
    List<String> customerCouponTemplateIds = couponManager.getCoupons(customerId).stream().map(
        coupon -> coupon.getTemplateId()).collect(Collectors.toList());

    List<CouponTemplate> couponTemplates = couponTemplateManager.getCouponTemplatesInStock().stream().filter(
        couponTemplate -> couponTemplateManager.isCouponTemplateActive(couponTemplate)
            && !customerCouponTemplateIds.contains(RowKeyGenUtil.generateCouponTemplateRowKey(couponTemplate))).collect(
        Collectors.toList());

    return couponTemplates;
  }

  @SneakyThrows
  public List<CouponTemplate> getExpiredCustomerCouponTemplates(String customerId) {
    List<String> customerCouponTemplateIds = couponManager.getCoupons(customerId).stream().map(
        coupon -> coupon.getTemplateId()).collect(Collectors.toList());
    List<CouponTemplate> expiredCustomerCouponTemplates;
    try {
      expiredCustomerCouponTemplates = couponTemplateManager.getCouponTemplates(
          customerCouponTemplateIds).stream().filter(
          couponTemplate -> !couponTemplateManager.isCouponTemplateActive(couponTemplate)).collect(
          Collectors.toList());
    } catch (Exception e) {
      log.info("No expired coupon templates for customer %s", customerId);
      return Collections.EMPTY_LIST;
    }

    return expiredCustomerCouponTemplates;
  }

  @SneakyThrows
  public List<CouponDetail> getActiveCustomerCoupons(String customerId) {
    // Find unused customer coupons
    List<Coupon> customerCoupons = couponManager.getCoupons(customerId).stream().filter(
        coupon -> coupon.getRedeemedDate() == null).collect(Collectors.toList());
    List<String> couponTemplateIds = customerCoupons.stream().map(coupon -> coupon.getTemplateId())
                                                .collect(Collectors.toList());
    // Find active customer coupon templates
    Map<String, CouponTemplate> activeCouponTemplatesMap;
    try {
      activeCouponTemplatesMap = couponTemplateManager.getCouponTemplates(
          couponTemplateIds).stream().filter(
          couponTemplate -> couponTemplateManager.isCouponTemplateActive(couponTemplate))
                                                                                  .collect(Collectors.toMap(
                                                                                      (couponTemplate -> RowKeyGenUtil.generateCouponTemplateRowKey(
                                                                                          couponTemplate)),
                                                                                      Function.identity()));
    } catch (Exception e) {
      log.warn(e.getMessage());
      return Collections.EMPTY_LIST;
    }

    // Find unused active customer coupons
    List<Coupon> activeCoupons = customerCoupons.stream().filter(coupon -> activeCouponTemplatesMap.containsKey(coupon.getTemplateId())).collect(
        Collectors.toList());

    // Find merchants
    List<Integer> merchantIds = activeCouponTemplatesMap.values().stream().map(
        couponTemplate -> couponTemplate.getBasicInfo().getMerchantId()).collect(Collectors.toList());

    Map<Integer, Merchant> merchantMap = buildMerchantsMap(merchantIds);

    return activeCoupons.stream().map(coupon -> {
      CouponTemplate couponTemplate = activeCouponTemplatesMap.get(coupon.getTemplateId());
      Merchant merchant = merchantMap.get(couponTemplate.getBasicInfo().getMerchantId());
      return CouponDetail.builder().couponTemplate(couponTemplate).coupon(coupon).merchant(merchant).build();
    }).collect(Collectors.toList());
  }


  private Map<Integer, Merchant> buildMerchantsMap(List<Integer> merchantIds) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("merchantToken", "token");
    HttpEntity<List<Integer>> requestEntity = new HttpEntity<>(merchantIds, headers);
    ParameterizedTypeReference<List<Merchant>> responseType = new ParameterizedTypeReference<List<Merchant>>() {};
    ResponseEntity<List<Merchant>> resp = restTemplate.exchange(merchantServiceUrl, HttpMethod.POST, requestEntity, responseType);
    return resp.getBody().stream().collect(Collectors.toMap(Merchant::getId, Function.identity()));
  }
}
