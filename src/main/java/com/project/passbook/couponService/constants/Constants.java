package com.project.passbook.couponService.constants;

/**
 * <h1>常量定义</h1>
 * Created by Qinyi.
 */
public class Constants {

    /** 商户优惠券 Kafka Topic */
    public static final String TEMPLATE_TOPIC = "coupon-template";

    /** token 文件存储目录 */
    public static final String TOKEN_DIR = "/tmp/token/";

    /** 已使用的 token 文件名后缀 */
    public static final String USED_TOKEN_SUFFIX = "_";

    /** 用户数的 redis key */
    public static final String USE_COUNT_REDIS_KEY = "coupon-customer-count";

    /**
     * <h2>CouponTemplate HBase Table</h2>
     * */
    public class CouponTemplateTable {

        /** PassTemplate HBase 表名 */
        public static final String TABLE_NAME = "passbook:coupontemplate";

        /** 基本信息列族 */
        public static final String FAMILY_BASIC_INFO = "BasicInformation";

        /** 商户 id */
        public static final String MERCHANT_ID = "merchant_id";

        /** 优惠券标题 */
        public static final String TITLE = "title";

        /** 优惠券摘要信息 */
        public static final String SUMMARY = "summary";

        /** 优惠券详细信息 */
        public static final String DESC = "desc";

        /** 优惠券是否有 token */
        public static final String HAS_TOKEN = "has_token";

        /** 优惠券背景色 */
        public static final String BACKGROUND = "background";

        /** 约束信息列族 */
        public static final String FAMILY_CONSTRAINTS = "Constraints";

        /** 最大个数限制 */
        public static final String LIMIT = "limit";

        /** 优惠券开始时间 */
        public static final String START = "start";

        /** 优惠券结束时间 */
        public static final String END = "end";
    }

    /**
     * <h2>Coupon HBase Table</h2>
     * */
    public class CouponTable {
        /** Coupon HBase 表名 */
        public static final String TABLE_NAME = "passbook:coupon";

        /** 信息列族 */
        public static final String FAMILY_INFORMATION = "Information";

        /** 用户 id */
        public static final String CUSTOMER_ID = "customer_id";

        /** 优惠券 id */
        public static final String TEMPLATE_ID = "template_id";

        /** 优惠券识别码 */
        public static final String TOKEN = "token";

        /** 领取日期 */
        public static final String ASSIGNED_DATE = "assigned_date";

        /** 消费日期 */
        public static final String REDEEMED_DATE = "redeemed_date";
    }

    /**
     * <h2>Feedback Hbase Table</h2>
     * */
    public class FeedbackTable {

        /** Feedback HBase 表名 */
        public static final String TABLE_NAME = "passbook:feedback";

        /** 信息列族 */
        public static final String FAMILY_INFORMATION = "Information";

        /** 用户 id */
        public static final String CUSTOMER_ID = "customer_id";

        /** 评论类型 */
        public static final String TYPE = "type";

        /** PassTemplate RowKey, 如果是 app 评论, 则是 -1 */
        public static final String TEMPLATE_ID = "template_id";

        /** 评论内容 */
        public static final String COMMENT = "comment";
    }
}
