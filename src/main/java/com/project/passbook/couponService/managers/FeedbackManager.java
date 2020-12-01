package com.project.passbook.couponService.managers;

import com.project.passbook.couponService.constants.Constants.FeedbackTable;
import com.project.passbook.couponService.entities.Feedback;
import com.project.passbook.couponService.mappers.FeedbackRowMapper;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.exceptions.types.BadRequestException;
import com.project.passbook.merchantService.model.exceptions.types.ConflictException;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackManager {

  @Autowired
  private HbaseTemplate hbaseTemplate;

  @SneakyThrows
  public Feedback createFeedback(Feedback feedback) {
    if (!feedback.validate()) {
      log.error("Invalid feedback type : {}", feedback.getType());
      throw new BadRequestException(String.format("Invalid feedback type : %s", feedback.getType()));
    }
    String rowKey = feedback.getFeedbackId();

    if (hbaseTemplate.getConnection().getTable(TableName.valueOf(FeedbackTable.TABLE_NAME)).exists(
        new Get(
            Bytes.toBytes(rowKey)))) {
      log.warn("Feedback {} already exists", rowKey);
      throw new ConflictException(String.format("Feedback %s already exists", rowKey));
    }

    Put put = new Put(Bytes.toBytes(rowKey));

    put.addColumn(Bytes.toBytes(FeedbackTable.FAMILY_INFORMATION),
                  Bytes.toBytes(FeedbackTable.TEMPLATE_ID),
                  Bytes.toBytes(feedback.getTemplateId()));

    put.addColumn(Bytes.toBytes(FeedbackTable.FAMILY_INFORMATION),
                  Bytes.toBytes(FeedbackTable.CUSTOMER_ID),
                  Bytes.toBytes(feedback.getCustomerId()));

    put.addColumn(Bytes.toBytes(FeedbackTable.FAMILY_INFORMATION),
                  Bytes.toBytes(FeedbackTable.COMMENT),
                  Bytes.toBytes(feedback.getComment()));

    put.addColumn(Bytes.toBytes(FeedbackTable.FAMILY_INFORMATION),
                  Bytes.toBytes(FeedbackTable.TYPE),
                  Bytes.toBytes(feedback.getType()));

    hbaseTemplate.saveOrUpdate(FeedbackTable.TABLE_NAME, put);

    return feedback;
  }

  public List<Feedback> getFeedback(String customerId) {
    byte[] rowKeyPrefix = StringUtils.reverse(customerId).getBytes();
    Scan scan = new Scan();
    scan.setFilter(new PrefixFilter(rowKeyPrefix));
    return hbaseTemplate.find(FeedbackTable.TABLE_NAME, scan, new FeedbackRowMapper());
  }

  void deleteFeedback(String feedbackId) {
    Delete delete = new Delete(Bytes.toBytes(feedbackId));
    hbaseTemplate.saveOrUpdate(FeedbackTable.TABLE_NAME, delete);
  }
}
