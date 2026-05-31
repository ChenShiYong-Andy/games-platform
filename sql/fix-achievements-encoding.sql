-- 修复 Docker 初始化时因客户端字符集不正确导致的成就中文乱码
-- 用法: docker exec -i games-platform-mysql mysql -uroot -p<password> games_platform < sql/fix-achievements-encoding.sql

SET NAMES utf8mb4;

USE games_platform;

UPDATE achievements SET name = '初出茅庐', description = '首次完成数独游戏', icon = '🎯' WHERE code = 'FIRST_CLEAR';
UPDATE achievements SET name = '三日之约', description = '连续登录3天', icon = '📅' WHERE code = 'LOGIN_STREAK_3';
UPDATE achievements SET name = '一周达人', description = '连续登录7天', icon = '🗓️' WHERE code = 'LOGIN_STREAK_7';
UPDATE achievements SET name = '数独新手', description = '累计通关10次', icon = '🏅' WHERE code = 'CLEAR_10';
UPDATE achievements SET name = '数独高手', description = '累计通关50次', icon = '🥇' WHERE code = 'CLEAR_50';
UPDATE achievements SET name = '数独大师', description = '累计通关100次', icon = '👑' WHERE code = 'CLEAR_100';
UPDATE achievements SET name = '小高手', description = '完成困难难度数独', icon = '💎' WHERE code = 'HARD_CLEAR';
