-- 移除成就系统。先删除用户解锁记录，再删除成就配置。

DROP TABLE IF EXISTS user_achievements;
DROP TABLE IF EXISTS achievements;
