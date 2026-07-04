package com.gamesplatform.pet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.pet.entity.PetUserBenefit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户宠物权益数据访问接口。
 */
@Mapper
public interface PetUserBenefitMapper extends BaseMapper<PetUserBenefit> {
}
