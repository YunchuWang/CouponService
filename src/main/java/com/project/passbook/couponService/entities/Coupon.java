package com.project.passbook.couponService.entities;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {
    // use as row key
    private String couponId;

    private String customerId;
    private String templateId;
    private String token;
    private Date assignedDate;
    private Date redeemedDate;
}