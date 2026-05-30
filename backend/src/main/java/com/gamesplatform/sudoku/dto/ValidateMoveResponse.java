package com.gamesplatform.sudoku.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateMoveResponse {
    private boolean valid;
}
