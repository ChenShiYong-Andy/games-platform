package com.gamesplatform.sudoku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.sudoku.entity.SudokuGame;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数独游戏记录数据访问接口。
 */
@Mapper
public interface SudokuGameMapper extends BaseMapper<SudokuGame> {
}
