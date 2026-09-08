package com.gamesplatform.system.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.system.admin.entity.AdminAccount;
import org.apache.ibatis.annotations.Mapper;

/** 管理员账户数据访问组件。 */
@Mapper
public interface AdminAccountMapper extends BaseMapper<AdminAccount> {
}
