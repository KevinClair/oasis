-- V3 core tables (no prefix)

CREATE TABLE IF NOT EXISTS `job_info` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `app_code` VARCHAR(100) NOT NULL,
  `job_name` VARCHAR(200) NOT NULL,
  `handler_name` VARCHAR(200) NOT NULL,
  `schedule_type` VARCHAR(32) NOT NULL,
  `schedule_conf` VARCHAR(255) NOT NULL,
  `route_strategy` VARCHAR(32) NOT NULL,
  `block_strategy` VARCHAR(32) NOT NULL,
  `retry_count` INT NOT NULL DEFAULT 0,
  `timeout_seconds` INT NOT NULL DEFAULT 30,
  `status` TINYINT(1) NOT NULL DEFAULT 1,
  `alarm_inherit_app` TINYINT(1) NOT NULL DEFAULT 1,
  `create_by` VARCHAR(64) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64) DEFAULT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_job_name` (`app_code`, `job_name`),
  KEY `idx_status` (`status`),
  KEY `idx_app_code` (`app_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `job_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `shard_id` INT NOT NULL,
  `next_trigger_time` BIGINT NOT NULL,
  `misfire_strategy` VARCHAR(32) NOT NULL DEFAULT 'FIRE_ONCE',
  `trigger_status` TINYINT(1) NOT NULL DEFAULT 1,
  `version` BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_id` (`job_id`),
  KEY `idx_next_trigger_time` (`next_trigger_time`),
  KEY `idx_shard_status` (`shard_id`, `trigger_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `scheduler_node` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `node_id` VARCHAR(128) NOT NULL,
  `host` VARCHAR(128) NOT NULL,
  `port` INT NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `last_heartbeat_time` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_node_id` (`node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `shard_lease` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `shard_id` INT NOT NULL,
  `owner_node_id` VARCHAR(128) NOT NULL,
  `lease_expire_at` BIGINT NOT NULL,
  `version` BIGINT NOT NULL DEFAULT 0,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shard_id` (`shard_id`),
  KEY `idx_lease_expire_at` (`lease_expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `dispatch_queue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `fire_log_id` BIGINT NOT NULL,
  `job_id` BIGINT NOT NULL,
  `target_address` VARCHAR(255) NOT NULL,
  `payload` MEDIUMTEXT NOT NULL,
  `status` VARCHAR(16) NOT NULL,
  `retry_count` INT NOT NULL DEFAULT 0,
  `next_retry_time` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_next_retry` (`status`, `next_retry_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `job_fire_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `trigger_time` BIGINT NOT NULL,
  `finish_time` BIGINT DEFAULT NULL,
  `trigger_type` VARCHAR(32) NOT NULL,
  `executor_address` VARCHAR(255) DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL,
  `attempt_no` INT NOT NULL DEFAULT 1,
  `error_message` VARCHAR(2000) DEFAULT NULL,
  `trace_id` VARCHAR(128) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_trigger` (`job_id`, `trigger_time`),
  KEY `idx_status_trigger` (`status`, `trigger_time`),
  KEY `idx_trace_id` (`trace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `job_log_chunk` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `fire_log_id` BIGINT NOT NULL,
  `seq_no` INT NOT NULL,
  `log_time` BIGINT NOT NULL,
  `log_content` MEDIUMTEXT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fire_seq` (`fire_log_id`, `seq_no`),
  KEY `idx_log_time` (`log_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `executor_node` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `app_code` VARCHAR(100) NOT NULL,
  `address` VARCHAR(255) NOT NULL,
  `machine_tag` VARCHAR(255) DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL,
  `last_heartbeat_time` BIGINT NOT NULL,
  `meta_json` TEXT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_addr` (`app_code`, `address`),
  KEY `idx_last_heartbeat` (`last_heartbeat_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `app_alarm_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `app_code` VARCHAR(100) NOT NULL,
  `receivers` TEXT DEFAULT NULL,
  `channels` VARCHAR(255) NOT NULL,
  `quiet_period_minutes` INT NOT NULL DEFAULT 10,
  `fail_threshold` INT NOT NULL DEFAULT 1,
  `timeout_seconds` INT NOT NULL DEFAULT 30,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_code` (`app_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `job_alarm_policy` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `inherit_app_template` TINYINT(1) NOT NULL DEFAULT 1,
  `receivers` TEXT DEFAULT NULL,
  `channels` VARCHAR(255) DEFAULT NULL,
  `quiet_period_minutes` INT DEFAULT NULL,
  `fail_threshold` INT DEFAULT NULL,
  `timeout_seconds` INT DEFAULT NULL,
  `enabled` TINYINT(1) NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_job_id` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `job_alarm_event` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `job_id` BIGINT NOT NULL,
  `fire_log_id` BIGINT NOT NULL,
  `alarm_type` VARCHAR(32) NOT NULL,
  `alarm_content` TEXT NOT NULL,
  `notify_status` VARCHAR(16) NOT NULL,
  `trigger_time` BIGINT NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_job_trigger` (`job_id`, `trigger_time`),
  KEY `idx_fire_log_id` (`fire_log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `job_alarm_notify_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `alarm_event_id` BIGINT NOT NULL,
  `channel` VARCHAR(32) NOT NULL,
  `receiver` VARCHAR(255) NOT NULL,
  `send_status` VARCHAR(16) NOT NULL,
  `response_message` VARCHAR(2000) DEFAULT NULL,
  `send_time` BIGINT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_alarm_event_id` (`alarm_event_id`),
  KEY `idx_send_status` (`send_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
