ALTER TABLE gomoku_games
    ADD COLUMN last_move_row INT DEFAULT NULL AFTER move_count,
    ADD COLUMN last_move_col INT DEFAULT NULL AFTER last_move_row;
