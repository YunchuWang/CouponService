package com.project.passbook.couponService.managers;

import com.google.common.annotations.VisibleForTesting;
import com.project.passbook.couponService.constants.Constants.CouponTemplateTable;
import com.project.passbook.couponService.constants.CouponTemplateStatus;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.mappers.CouponTemplateRowMapper;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.exceptions.types.ConditionalUpdateFailureException;
import com.project.passbook.merchantService.model.exceptions.types.ConflictException;
import com.project.passbook.merchantService.model.exceptions.types.NotFoundException;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import com.spring4all.spring.boot.starter.hbase.api.TableCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.LongComparator;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class CouponTemplateManager {

  private static final String DECREMENT_COUPON_TEMPLATE_LIMIT_LOCK = "DecrementCouponTemplateLimitLock";

  @Autowired
  private HbaseTemplate hbaseTemplate;

  @Autowired
  private TokenManager tokenManager;

  @SneakyThrows
  public CouponTemplate addCouponTemplate(final CouponTemplate couponTemplate) {
    String rowKey = RowKeyGenUtil.generateCouponTemplateRowKey(couponTemplate);
    checkCouponTemplateConflict(rowKey);

    // Add the coupon template
    Put put = new Put(Bytes.toBytes(rowKey));
    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_BASIC_INFO),
                  Bytes.toBytes(CouponTemplateTable.MERCHANT_ID),
                  Bytes.toBytes(couponTemplate
                                    .getBasicInfo()
                                    .getMerchantId()));

    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_BASIC_INFO),
                  Bytes.toBytes(CouponTemplateTable.TITLE),
                  Bytes.toBytes(couponTemplate
                                    .getBasicInfo()
                                    .getTitle()));

    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_BASIC_INFO),
                  Bytes.toBytes(CouponTemplateTable.BACKGROUND),
                  Bytes.toBytes(couponTemplate
                                    .getBasicInfo()
                                    .getBackground()));

    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_BASIC_INFO),
                  Bytes.toBytes(CouponTemplateTable.DESC),
                  Bytes.toBytes(couponTemplate
                                    .getBasicInfo()
                                    .getDesc()));

    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_BASIC_INFO),
                  Bytes.toBytes(CouponTemplateTable.SUMMARY),
                  Bytes.toBytes(couponTemplate
                                    .getBasicInfo()
                                    .getSummary()));

    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_BASIC_INFO),
                  Bytes.toBytes(CouponTemplateTable.HAS_TOKEN),
                  Bytes.toBytes(couponTemplate
                                    .getBasicInfo()
                                    .getHasToken()));

    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_CONSTRAINTS),
                  Bytes.toBytes(CouponTemplateTable.LIMIT),
                  Bytes.toBytes(couponTemplate
                                    .getConstraint()
                                    .getLimit()));

    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_CONSTRAINTS),
                  Bytes.toBytes(CouponTemplateTable.START),
                  Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT
                                    .format(couponTemplate
                                                .getConstraint()
                                                .getStartTime())));

    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_CONSTRAINTS),
                  Bytes.toBytes(CouponTemplateTable.END),
                  Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT
                                    .format(couponTemplate
                                                .getConstraint()
                                                .getEndTime())));

    hbaseTemplate.saveOrUpdate(CouponTemplateTable.TABLE_NAME, put);

    if (couponTemplate.getBasicInfo().getHasToken()) {
      tokenManager.populateTokensForCouponTemplate(RowKeyGenUtil.generateCouponTemplateRowKey(couponTemplate),
                                                   couponTemplate.getConstraint().getLimit());
    }
    return couponTemplate;
  }

  public void checkCouponTemplateConflict(String rowKey) throws IOException {
    if (hbaseTemplate
        .getConnection()
        .getTable(TableName.valueOf(CouponTemplateTable.TABLE_NAME))
        .exists(new Get(Bytes.toBytes(rowKey))))
    {
      log.warn("RowKey {} already exist!",
               rowKey);
      throw new ConflictException(String.format("Coupon template already exist",
                                                rowKey));
    }
  }

  @SneakyThrows
  public void checkCouponTemplateExistence(String couponTemplateId) {
    if (!hbaseTemplate
        .getConnection()
        .getTable(TableName.valueOf(CouponTemplateTable.TABLE_NAME))
        .exists(new Get(Bytes.toBytes(couponTemplateId))))
    {
      log.warn("CouponTemplateId {} cant be found!",
               couponTemplateId);
      throw new NotFoundException(String.format("Coupon template with id %s cant be found",
                                                couponTemplateId));
    }
  }

  // Get all coupon templates added by merchant
  @VisibleForTesting
  @SneakyThrows
  List<CouponTemplate> getCouponTemplates(String merchantId) {
    byte[] couponTemplateRowPrefix = DigestUtils.md5Hex(StringUtils.reverse(merchantId)).getBytes();
    Scan scan = new Scan();

    scan.setFilter(new PrefixFilter(couponTemplateRowPrefix));
    return hbaseTemplate.find(CouponTemplateTable.TABLE_NAME, scan, new CouponTemplateRowMapper());
  }

  @VisibleForTesting
  @SneakyThrows
  void decreaseCouponTemplateLimit(final CouponTemplate couponTemplate) {
    String rowKey = RowKeyGenUtil.generateCouponTemplateRowKey(couponTemplate);
    checkCouponTemplateExistence(rowKey);

    // Get current coupon template
    CouponTemplate currentCouponTemplate = getCouponTemplate(rowKey);
    // If limit <= 0, throw exception
    if (currentCouponTemplate.getConstraint().getLimit() < 1) {
      throw new ConditionalUpdateFailureException(
          String.format("Coupon Template with id %s runs out of stock!", rowKey));
    }
    // otherwise decrement limit and unlock
    Put put = new Put(Bytes.toBytes(rowKey));
    put.addColumn(Bytes.toBytes(CouponTemplateTable.FAMILY_CONSTRAINTS),
                  Bytes.toBytes(CouponTemplateTable.LIMIT),
                  Bytes.toBytes(currentCouponTemplate
                                    .getConstraint()
                                    .getLimit() - 1));
    hbaseTemplate.saveOrUpdate(CouponTemplateTable.TABLE_NAME, put);
  }

  @SneakyThrows
  public CouponTemplate getCouponTemplate(final String couponTemplateId) {
    return hbaseTemplate.get(CouponTemplateTable.TABLE_NAME,
                             couponTemplateId,
                             new CouponTemplateRowMapper());
  }

  public List<CouponTemplate> getCouponTemplates(final List<String> couponTemplateIds, CouponTemplateStatus couponTemplateStatus) {
    List<CouponTemplate> foundTemplates = hbaseTemplate.execute(
        CouponTemplateTable.TABLE_NAME,
        new TableCallback<List<CouponTemplate>>() {
          @Override
          public List<CouponTemplate> doInTable(Table table)
              throws Throwable
          {
            List<CouponTemplate> couponTemplates = new ArrayList<>();
            List<Get> queryRowList = new ArrayList<>();
            couponTemplateIds.forEach(
                couponTemplateId -> queryRowList.add(new Get(Bytes.toBytes(couponTemplateId))));

            Result[] results = table.get(queryRowList);
            CouponTemplateRowMapper mapper = new CouponTemplateRowMapper();
            for (Result result : results) {
              if (result.isEmpty()) {
                continue;
              }
              couponTemplates.add(mapper.mapRow(result, -1));
            }

            return couponTemplates;
          }
        });
    
    return foundTemplates;
  }

  @SneakyThrows
  public void deleteCouponTemplate(final CouponTemplate couponTemplate) {
    Delete delete = new Delete(Bytes.toBytes(RowKeyGenUtil.generateCouponTemplateRowKey(couponTemplate)));
    hbaseTemplate.saveOrUpdate(CouponTemplateTable.TABLE_NAME,
                               delete);
  }

  List<CouponTemplate> getCouponTemplatesInStock() {
    // Get all templates with limit > 0
    Scan scan = new Scan();

    scan.setFilter(
        new SingleColumnValueFilter(Bytes.toBytes(CouponTemplateTable.FAMILY_CONSTRAINTS),
                                    Bytes.toBytes(CouponTemplateTable.LIMIT),
                                    CompareFilter.CompareOp.GREATER, new LongComparator(0L)));

    return hbaseTemplate.find(CouponTemplateTable.TABLE_NAME, scan,
                              new CouponTemplateRowMapper());
  }

  boolean isCouponTemplateActive(CouponTemplate couponTemplate) {
    Date currentDate = new Date();
    Date startDate = couponTemplate.getConstraint().getStartTime();
    Date endDate = couponTemplate.getConstraint().getEndTime();

    return !(currentDate.before(startDate) || currentDate.after(endDate));
  }

  boolean isCouponTemplateInStock(CouponTemplate couponTemplate) {
    return couponTemplate.getConstraint().getLimit() > 0;
  }
}
