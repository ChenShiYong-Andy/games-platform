package com.gamesplatform.school.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.school.pet.entity.PetUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户宠物数据访问接口。
 */
@Mapper
public interface PetUserMapper extends BaseMapper<PetUser> {
}
