package com.project.passbook.couponService.entities;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponTemplate {

  private BasicInfo basicInfo;
  private Constraint constraint;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class BasicInfo {
    private Integer merchantId;
    private String title;
    private String summary;
    private String desc;
    private Boolean hasToken;
    private Integer background;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Constraint {

    private Long limit;
    private Date startTime;
    private Date endTime;
  }
}