package com.project.passbook.couponService.mappers;

import com.project.passbook.couponService.constants.Constants.CouponTemplateTable;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.spring4all.spring.boot.starter.hbase.api.RowMapper;
import java.util.Date;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class CouponTemplateRowMapper implements RowMapper<CouponTemplate> {

  private static byte[] FAMILY_BASIC_INFO = CouponTemplateTable.FAMILY_BASIC_INFO.getBytes();
  private static byte[] FAMILY_CONSTRAINT = CouponTemplateTable.FAMILY_CONSTRAINTS.getBytes();
  private static byte[] MERCHANT_ID = CouponTemplateTable.MERCHANT_ID.getBytes();
  private static byte[] TITLE = CouponTemplateTable.TITLE.getBytes();
  private static byte[] SUMMARY = CouponTemplateTable.SUMMARY.getBytes();
  private static byte[] DESC = CouponTemplateTable.DESC.getBytes();
  private static byte[] HAS_TOKEN = CouponTemplateTable.HAS_TOKEN.getBytes();
  private static byte[] LIMIT = CouponTemplateTable.LIMIT.getBytes();
  private static byte[] BACKGROUND = CouponTemplateTable.BACKGROUND.getBytes();
  private static byte[] START_TIME = CouponTemplateTable.START.getBytes();
  private static byte[] END_TIME = CouponTemplateTable.END.getBytes();

  @Override
  public CouponTemplate mapRow(Result result, int rowKey) throws Exception {
    Integer merchantId = Bytes.toInt(result.getValue(FAMILY_BASIC_INFO, MERCHANT_ID));
    String title = Bytes.toString(result.getValue(FAMILY_BASIC_INFO, TITLE));
    String summary = Bytes.toString(result.getValue(FAMILY_BASIC_INFO, SUMMARY));
    String description = Bytes.toString(result.getValue(FAMILY_BASIC_INFO, DESC));
    Integer background = Bytes.toInt(result.getValue(FAMILY_BASIC_INFO, BACKGROUND));
    Boolean hasToken = Bytes.toBoolean(result.getValue(FAMILY_BASIC_INFO, HAS_TOKEN));
    Long limit = Bytes.toLong(result.getValue(FAMILY_CONSTRAINT, LIMIT));

    Date startTime = DateUtils
        .parseDate(Bytes.toString(result.getValue(FAMILY_CONSTRAINT, START_TIME)), DateFormatUtils.ISO_DATE_FORMAT.getPattern());
    Date endTime = DateUtils
        .parseDate(Bytes.toString(result.getValue(FAMILY_CONSTRAINT, END_TIME)), DateFormatUtils.ISO_DATE_FORMAT.getPattern());

    CouponTemplate.BasicInfo basicInfo = new CouponTemplate.BasicInfo(merchantId, title, summary, description, hasToken, background);
    CouponTemplate.Constraint constraint = new CouponTemplate.Constraint(limit, startTime, endTime);

    return new CouponTemplate(basicInfo, constraint);
  }
}
