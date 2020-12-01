package com.project.passbook.couponService.mappers;

import com.project.passbook.couponService.constants.Constants.FeedbackTable;
import com.project.passbook.couponService.entities.Feedback;
import com.spring4all.spring.boot.starter.hbase.api.RowMapper;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class FeedbackRowMapper implements RowMapper<Feedback> {

  private static byte[] FAMILY_INFORMATION = FeedbackTable.FAMILY_INFORMATION.getBytes();
  private static byte[] CUSTOMER_ID = FeedbackTable.CUSTOMER_ID.getBytes();
  private static byte[] TYPE = FeedbackTable.TYPE.getBytes();
  private static byte[] TEMPLATE_ID = FeedbackTable.TEMPLATE_ID.getBytes();
  private static byte[] COMMENT = FeedbackTable.COMMENT.getBytes();

  @Override
  public Feedback mapRow(Result result, int rowNum) throws Exception {
    String rowKey = Bytes.toString(result.getRow());
    String customerId = Bytes.toString(result.getValue(FAMILY_INFORMATION, CUSTOMER_ID));
    String type = Bytes.toString(result.getValue(FAMILY_INFORMATION, TYPE));
    String templateId = Bytes.toString(result.getValue(FAMILY_INFORMATION, TEMPLATE_ID));
    String comment = Bytes.toString(result.getValue(FAMILY_INFORMATION, COMMENT));
    return new Feedback(rowKey, customerId, type, templateId, comment);
  }
}
