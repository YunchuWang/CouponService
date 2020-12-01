package com.project.passbook.couponService.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FeedbackType {
    COUPON(1, "Coupon comment"),
    APP(2, "App comment");

    private Integer code;
    private String desc;
}
