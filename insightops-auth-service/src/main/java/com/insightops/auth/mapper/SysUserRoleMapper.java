package com.insightops.auth.mapper;

import com.insightops.auth.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserRoleMapper {

    int insert(SysUserRole userRole);
}
