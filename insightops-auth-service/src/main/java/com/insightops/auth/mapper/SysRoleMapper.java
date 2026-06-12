package com.insightops.auth.mapper;

import com.insightops.auth.dto.response.RoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    List<RoleVO> selectRolesByUserId(@Param("userId") Long userId);
}
