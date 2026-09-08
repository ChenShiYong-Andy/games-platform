package com.gamesplatform.system.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.system.admin.entity.AdminUserRelation;
import org.apache.ibatis.annotations.Mapper;

/** 管理员用户关联数据访问组件。 */
@Mapper
public interface AdminUserRelationMapper extends BaseMapper<AdminUserRelation> {
}
