package com.gamesplatform.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.system.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
