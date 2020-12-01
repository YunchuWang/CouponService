package com.project.passbook.couponService.entities;

import com.project.passbook.merchantService.model.entities.Merchant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponDetail {
  private CouponTemplate couponTemplate;
  private Coupon coupon;
  private Merchant merchant;
}
