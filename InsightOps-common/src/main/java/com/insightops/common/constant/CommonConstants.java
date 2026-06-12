package com.insightops.common.constant;

/**
 * 跨模块公共常量。
 * <p>
 * 存放各微服务共用的魔法数字，避免在代码中硬编码分散的 1、10、100 等值。
 * </p>
 */
public final class CommonConstants {

    /** 私有构造，禁止实例化工具类 */
    private CommonConstants() {
    }

    /** 分页查询默认起始页码（从第 1 页开始） */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 分页查询默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 分页查询允许的最大每页条数（防止一次查过多数据） */
    public static final int MAX_PAGE_SIZE = 100;

    /** 逻辑删除标记：记录有效，未删除 */
    public static final int NOT_DELETED = 0;

    /** 逻辑删除标记：记录已软删除，查询时默认过滤 */
    public static final int DELETED = 1;
}
