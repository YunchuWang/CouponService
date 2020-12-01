//package com.project.passbook.couponService.controllers;
//
//import com.project.passbook.merchantService.entities.Merchant;
//import com.project.passbook.merchantService.requests.CreateMerchantRequest;
//import com.project.passbook.merchantService.requests.FindMerchantRequest;
//import com.project.passbook.merchantService.responses.Response;
//import com.project.passbook.merchantService.service.MerchantService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RestController
//@RequestMapping("/coupon")
//public class CouponController {
//
//  private CouponService couponService;
//
//  public CouponController(CouponService couponService) {
//    this.couponService = couponService;
//  }
//
//  @PostMapping
//  @ResponseBody
//  public ResponseEntity createMerchant(@RequestBody Coupon coupon) {
//    log.info("Adding coupon: {}", coupon);
//    couponService.addCoupon(coupon);
//    return ResponseEntity.ok().build();
//  }
//}
