package com.gamesplatform.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.pet.entity.PetBenefitUseLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宠物权益使用日志数据访问接口。
 */
@Mapper
public interface PetBenefitUseLogMapper extends BaseMapper<PetBenefitUseLog> {
}
