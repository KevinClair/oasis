-- V3 20260307 runtime enhance for scheduler_node / shard_lease / dispatch_queue
-- 目标：
-- 1) 明确补齐 scheduler_node / shard_lease / dispatch_queue 表（幂等创建）
-- 2) 增加运行期查询索引，支撑分片租约与补偿队列监控

CREATE TABLE IF NOT EXISTS `scheduler_node`
(
    `id`                  BIGINT       NOT NULL AUTO_INCREMENT,
    `node_id`             VARCHAR(128) NOT NULL,
    `host`                VARCHAR(128) NOT NULL,
    `port`                INT          NOT NULL,
    `status`              VARCHAR(16)  NOT NULL,
    `last_heartbeat_time` BIGINT       NOT NULL,
    `create_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_node_id` (`node_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `shard_lease`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `shard_id`        INT          NOT NULL,
    `owner_node_id`   VARCHAR(128) NOT NULL,
    `lease_expire_at` BIGINT       NOT NULL,
    `version`         BIGINT       NOT NULL DEFAULT 0,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shard_id` (`shard_id`),
    KEY `idx_lease_expire_at` (`lease_expire_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `dispatch_queue`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `fire_log_id`     BIGINT       NOT NULL,
    `job_id`          BIGINT       NOT NULL,
    `target_address`  VARCHAR(255) NOT NULL,
    `payload`         MEDIUMTEXT   NOT NULL,
    `status`          VARCHAR(16)  NOT NULL,
    `retry_count`     INT          NOT NULL DEFAULT 0,
    `next_retry_time` BIGINT                DEFAULT NULL,
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_status_next_retry` (`status`, `next_retry_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 补充运行期索引（按存在性判断，避免重复执行报错）
SET @idx_exists := (SELECT COUNT(1)
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'scheduler_node'
                      AND index_name = 'idx_status_heartbeat');
SET @sql_stmt := IF(@idx_exists = 0, 'CREATE INDEX idx_status_heartbeat ON scheduler_node(status, last_heartbeat_time)',
                    'SELECT 1');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (SELECT COUNT(1)
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'shard_lease'
                      AND index_name = 'idx_owner_expire');
SET @sql_stmt :=
        IF(@idx_exists = 0, 'CREATE INDEX idx_owner_expire ON shard_lease(owner_node_id, lease_expire_at)', 'SELECT 1');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (SELECT COUNT(1)
                    FROM information_schema.statistics
                    WHERE table_schema = DATABASE()
                      AND table_name = 'dispatch_queue'
                      AND index_name = 'idx_status_update_time');
SET @sql_stmt :=
        IF(@idx_exists = 0, 'CREATE INDEX idx_status_update_time ON dispatch_queue(status, update_time)', 'SELECT 1');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
