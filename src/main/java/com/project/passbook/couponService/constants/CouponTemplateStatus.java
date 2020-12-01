package com.project.passbook.couponService.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CouponTemplateStatus {
    EXPIRED(1, "template expired"),
    ACTIVE(2, "template active"),
    ALL(3, "all templates");

    private Integer code;

    private String desc;

}
