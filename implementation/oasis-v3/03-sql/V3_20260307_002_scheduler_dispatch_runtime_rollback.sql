-- rollback for V3_20260307_002_scheduler_dispatch_runtime.sql
-- 说明：仅回滚新增索引，不删除业务表，避免误删线上数据。

SET @idx_exists := (SELECT COUNT(1)
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'dispatch_queue'
                      AND index_name = 'idx_status_update_time');
SET @sql_stmt := IF(@idx_exists > 0, 'DROP INDEX idx_status_update_time ON dispatch_queue', 'SELECT 1');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (SELECT COUNT(1)
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'shard_lease'
                      AND index_name = 'idx_owner_expire');
SET @sql_stmt := IF(@idx_exists > 0, 'DROP INDEX idx_owner_expire ON shard_lease', 'SELECT 1');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (SELECT COUNT(1)
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'scheduler_node'
                      AND index_name = 'idx_status_heartbeat');
SET @sql_stmt := IF(@idx_exists > 0, 'DROP INDEX idx_status_heartbeat ON scheduler_node', 'SELECT 1');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
