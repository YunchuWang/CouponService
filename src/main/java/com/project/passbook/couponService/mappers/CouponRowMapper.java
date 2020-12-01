package com.project.passbook.couponService.mappers;

import com.project.passbook.couponService.constants.Constants.CouponTable;
import com.project.passbook.couponService.entities.Coupon;
import com.spring4all.spring.boot.starter.hbase.api.RowMapper;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class CouponRowMapper implements RowMapper<Coupon> {

  private static byte[] FAMILY_INFORMATION = CouponTable.FAMILY_INFORMATION.getBytes();
  private static byte[] CUSTOMER_ID = CouponTable.CUSTOMER_ID.getBytes();
  private static byte[] TEMPLATE_ID = CouponTable.TEMPLATE_ID.getBytes();
  private static byte[] TOKEN = CouponTable.TOKEN.getBytes();
  private static byte[] ASSIGNED_DATE = CouponTable.ASSIGNED_DATE.getBytes();
  private static byte[] REDEEMED_DATE = CouponTable.REDEEMED_DATE.getBytes();

  @Override
  public Coupon mapRow(Result result, int rowKey) throws Exception {
    String customerId = Bytes.toString(result.getValue(FAMILY_INFORMATION, CUSTOMER_ID));
    String templateId = Bytes.toString(result.getValue(FAMILY_INFORMATION, TEMPLATE_ID));
    String token = Bytes.toString(result.getValue(FAMILY_INFORMATION, TOKEN));

    Coupon coupon = new Coupon();

    String[] timePattern = new String[] {"yyyy-DD-dd"};
    coupon.setAssignedDate(DateUtils.parseDate(Bytes.toString(result.getValue(FAMILY_INFORMATION, ASSIGNED_DATE)),
                                               timePattern));

    String redeemedDate = Bytes.toString(result.getValue(FAMILY_INFORMATION, REDEEMED_DATE));
    if (redeemedDate.equals("-1")) {
      coupon.setRedeemedDate(null);
    } else {
      coupon.setRedeemedDate(DateUtils.parseDate(redeemedDate, timePattern));
    }
    coupon.setCouponId(Bytes.toString(result.getRow()));
    coupon.setTemplateId(templateId);
    coupon.setCustomerId(customerId);
    coupon.setToken(token);

    return coupon;
  }
}