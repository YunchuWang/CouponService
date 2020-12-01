package com.project.passbook.couponService.managers;

import com.google.common.annotations.VisibleForTesting;
import com.project.passbook.couponService.constants.Constants.CouponTable;
import com.project.passbook.couponService.entities.Coupon;
import com.project.passbook.couponService.entities.CouponTemplate;
import com.project.passbook.couponService.mappers.CouponRowMapper;
import com.project.passbook.couponService.utils.RowKeyGenUtil;
import com.project.passbook.merchantService.model.exceptions.types.BadRequestException;
import com.project.passbook.merchantService.model.exceptions.types.ConflictException;
import com.project.passbook.merchantService.model.exceptions.types.NotFoundException;
import com.spring4all.spring.boot.starter.hbase.api.HbaseTemplate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class CouponManager {

  private static final String CLAIM_COUPON_LOCK = "claimCouponLock";
  private static final String REDEEM_COUPON_LOCK = "redeemCouponLock";

  @Autowired
  private TokenManager tokenManager;

  @Autowired
  private CouponTemplateManager couponTemplateManager;

  @Autowired
  private HbaseTemplate hbaseTemplate;

  @Autowired
  private RedissonClient redissonClient;

  @SneakyThrows
  public Coupon claimCoupon(String customerId, String couponTemplateId) {
    try {
      // TODO: Check if customer exists calling customerService
//      customerManager.checkCustomerExistence(customerId);
      couponTemplateManager.checkCouponTemplateExistence(couponTemplateId);
    } catch (Exception e) {
      throw new BadRequestException(e.getMessage());
    }

    Coupon coupon = Coupon.builder()
                          .customerId(customerId)
                          .templateId(couponTemplateId)
                          .build();

    CouponTemplate couponTemplate = couponTemplateManager.getCouponTemplate(coupon.getTemplateId());

    RLock lock = redissonClient.getLock(CLAIM_COUPON_LOCK);
    try {
      lock.lock();
      couponTemplateManager.decreaseCouponTemplateLimit(couponTemplate);
      coupon.setToken(
          couponTemplate.getBasicInfo().getHasToken() ? tokenManager.getCouponToken(couponTemplateId) : "-1");
    } finally {
      lock.unlock();
    }

    // Save claimed coupon
    coupon.setAssignedDate(new Date());
    coupon.setCouponId(RowKeyGenUtil.generateCouponRowKey(coupon));
    return addCoupon(coupon);
  }

  @SneakyThrows
  public Coupon redeemCoupon(Coupon coupon) {
    RLock lock = redissonClient.getLock(REDEEM_COUPON_LOCK);
    try {
      lock.lock();
      Coupon foundCoupon = getCoupon(coupon);
      if (foundCoupon.getRedeemedDate() != null) {
        throw new BadRequestException(String.format("Coupon with id %s already redeemed", foundCoupon.getCouponId()));
      }
      coupon.setRedeemedDate(new Date());

      Put put = new Put(Bytes.toBytes(coupon.getCouponId()));

      put.addColumn(Bytes.toBytes(CouponTable.FAMILY_INFORMATION),
                    Bytes.toBytes(CouponTable.REDEEMED_DATE),
                    Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(new Date())));

      hbaseTemplate.saveOrUpdate(CouponTable.TABLE_NAME, put);
    } finally {
      lock.unlock();
    }

    return coupon;
  }

  @SneakyThrows
  public List<Coupon> getCoupons(String customerId) {
    byte[] couponRowPrefix = StringUtils.reverse(customerId).getBytes();

    Scan scan = new Scan();
    scan.setFilter(new PrefixFilter(couponRowPrefix));
    return hbaseTemplate.find(CouponTable.TABLE_NAME, scan, new CouponRowMapper());
  }

  private Coupon getCoupon(Coupon coupon) {
    try {
      return hbaseTemplate.get(CouponTable.TABLE_NAME,
                               coupon.getCouponId(),
                               new CouponRowMapper());
    } catch (Exception e) {
      throw new NotFoundException(String.format("Coupon with id %s cant be found",
                                                coupon.getCouponId()));
    }
  }

  @SneakyThrows
  public List<Coupon> getRedeemedCoupons(String customerId) {
    return getCoupons(customerId).stream().filter(
        coupon -> coupon.getRedeemedDate() != null).collect(Collectors.toList());
  }

  @VisibleForTesting
  Coupon addCoupon(Coupon coupon) {
    Put put = new Put(Bytes.toBytes(coupon.getCouponId()));

    put.addColumn(Bytes.toBytes(CouponTable.FAMILY_INFORMATION),
                  Bytes.toBytes(CouponTable.TEMPLATE_ID),
                  Bytes.toBytes(coupon.getTemplateId()));

    put.addColumn(Bytes.toBytes(CouponTable.FAMILY_INFORMATION),
                  Bytes.toBytes(CouponTable.CUSTOMER_ID),
                  Bytes.toBytes(coupon.getCustomerId()));

    put.addColumn(Bytes.toBytes(CouponTable.FAMILY_INFORMATION),
                  Bytes.toBytes(CouponTable.TOKEN),
                  Bytes.toBytes(coupon.getToken()));

    put.addColumn(Bytes.toBytes(CouponTable.FAMILY_INFORMATION),
                  Bytes.toBytes(CouponTable.ASSIGNED_DATE),
                  Bytes.toBytes(DateFormatUtils.ISO_DATE_FORMAT.format(coupon.getAssignedDate())));

    put.addColumn(Bytes.toBytes(CouponTable.FAMILY_INFORMATION),
                  Bytes.toBytes(CouponTable.REDEEMED_DATE), Bytes.toBytes("-1"));

    hbaseTemplate.saveOrUpdate(CouponTable.TABLE_NAME, put);

    return coupon;
  }

  @SneakyThrows
  void deleteCoupon(final Coupon coupon) {
    Delete delete = new Delete(Bytes.toBytes(coupon.getCouponId()));
    hbaseTemplate.saveOrUpdate(CouponTable.TABLE_NAME,
                               delete);
  }

  @SneakyThrows
  private void checkCouponConflict(String rowKey) {
    if (hbaseTemplate
        .getConnection()
        .getTable(TableName.valueOf(CouponTable.TABLE_NAME))
        .exists(new Get(Bytes.toBytes(rowKey))))
    {
      log.warn("Coupon {} already exist!", rowKey);
      throw new ConflictException(String.format("Coupon %s already exist", rowKey));
    }
  }

  @SneakyThrows
  private void checkCouponExistence(String rowKey) {
    if (!hbaseTemplate
        .getConnection()
        .getTable(TableName.valueOf(CouponTable.TABLE_NAME))
        .exists(new Get(Bytes.toBytes(rowKey))))
    {
      log.warn("Coupon {} does not exist!", rowKey);
      throw new NotFoundException(String.format("Coupon %s does not exist", rowKey));
    }
  }
}
