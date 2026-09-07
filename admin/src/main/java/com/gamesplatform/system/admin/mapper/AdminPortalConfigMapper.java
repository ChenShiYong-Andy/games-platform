package com.gamesplatform.system.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.system.admin.entity.AdminPortalConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 独立管理后台安全配置数据访问组件。
 */
@Mapper
public interface AdminPortalConfigMapper extends BaseMapper<AdminPortalConfig> {
}
