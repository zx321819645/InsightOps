package com.insightops.auth.mapper;

import com.insightops.auth.dto.response.PermissionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysPermissionMapper {

    List<PermissionVO> selectByUserId(@Param("userId") Long userId);
}
