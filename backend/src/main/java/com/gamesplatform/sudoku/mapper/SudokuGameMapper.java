package com.gamesplatform.sudoku.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gamesplatform.sudoku.entity.SudokuGame;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SudokuGameMapper extends BaseMapper<SudokuGame> {
}
