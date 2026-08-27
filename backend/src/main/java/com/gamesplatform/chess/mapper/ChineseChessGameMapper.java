package com.gamesplatform.chess.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.chess.entity.ChineseChessGame;
import org.apache.ibatis.annotations.Mapper;

/** 中国象棋对局数据访问组件。 */
@Mapper
public interface ChineseChessGameMapper extends BaseMapper<ChineseChessGame> {
}
