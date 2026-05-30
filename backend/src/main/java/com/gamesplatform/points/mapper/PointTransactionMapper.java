package com.gamesplatform.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.points.entity.PointTransaction;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PointTransactionMapper extends BaseMapper<PointTransaction> {
}
