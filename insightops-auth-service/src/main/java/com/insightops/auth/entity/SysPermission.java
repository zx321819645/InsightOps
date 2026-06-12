package com.insightops.auth.entity;

import com.insightops.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends BaseEntity {
    /**
     * 主键
     */
    private Long id;

    /**
     * 权限编码
     */
    private String permCode;

    /**
     * 权限名称
     */
    private String permName;

    /**
     * 类型：1-菜单 2-按钮 3-API
     */
    private Integer permType;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 路径
     */
    private String path;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 排序
     */
    private Integer sortOrder;


}
