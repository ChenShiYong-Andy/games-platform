ALTER TABLE gomoku_games
    ADD COLUMN game_mode VARCHAR(10) NOT NULL DEFAULT 'FRIEND' AFTER finish_reason,
    ADD COLUMN human_color VARCHAR(10) DEFAULT NULL AFTER game_mode;

ALTER TABLE chinese_chess_games
    ADD COLUMN game_mode VARCHAR(10) NOT NULL DEFAULT 'FRIEND' AFTER finish_reason,
    ADD COLUMN human_color VARCHAR(10) DEFAULT NULL AFTER game_mode;
