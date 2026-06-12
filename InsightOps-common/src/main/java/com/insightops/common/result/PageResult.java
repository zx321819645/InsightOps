package com.insightops.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果封装。
 * <p>
 * 通常作为 {@link Result} 的 data 字段内容，用于列表类接口。
 * </p>
 *
 * <pre>
 * 示例：Result.success(PageResult.of(100L, userList))
 * 返回：{ "code": 0, "message": "操作成功", "data": { "total": 100, "records": [...] } }
 * </pre>
 *
 * @param <T> 列表元素类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {

    /** 符合条件的总记录数（用于前端计算总页数） */
    private Long total;

    /** 当前页的数据列表 */
    private List<T> records;

    /**
     * 构建分页结果
     *
     * @param total   总记录数
     * @param records 当前页数据
     */
    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records);
    }

    /**
     * 空分页结果（查询无数据时使用）
     */
    public static <T> PageResult<T> empty() {
        return new PageResult<>(0L, Collections.emptyList());
    }
}
