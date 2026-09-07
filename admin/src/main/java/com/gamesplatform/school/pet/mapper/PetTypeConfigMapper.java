package com.gamesplatform.school.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.school.pet.entity.PetTypeConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物类型配置数据访问接口。
 */
@Mapper
public interface PetTypeConfigMapper extends BaseMapper<PetTypeConfig> {
}
