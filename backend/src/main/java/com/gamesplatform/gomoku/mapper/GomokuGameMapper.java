package com.gamesplatform.gomoku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.gomoku.entity.GomokuGame;
import org.apache.ibatis.annotations.Mapper;

/** 五子棋对局数据访问组件。 */
@Mapper
public interface GomokuGameMapper extends BaseMapper<GomokuGame> {
}
