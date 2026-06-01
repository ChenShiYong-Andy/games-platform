package com.gamesplatform.points.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.points.entity.PointTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分流水数据访问接口。
 */
@Mapper
public interface PointTransactionMapper extends BaseMapper<PointTransaction> {
}
