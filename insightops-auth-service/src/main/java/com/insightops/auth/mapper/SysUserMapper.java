package com.insightops.auth.mapper;

import com.insightops.auth.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface SysUserMapper {

    SysUser selectByUsername(@Param("username") String username);

    SysUser selectById(@Param("id") Long id);

    int insert(SysUser user);

    int updateLastLogin(@Param("id") Long id,
                        @Param("lastLoginAt") LocalDateTime lastLoginAt,
                        @Param("lastLoginIp") String lastLoginIp);
}
