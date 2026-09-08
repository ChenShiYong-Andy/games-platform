package com.gamesplatform.system.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.system.admin.entity.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

/** 管理员操作审计日志数据访问组件。 */
@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}
