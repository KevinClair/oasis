-- 创建应用管理表
CREATE TABLE IF NOT EXISTS `application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_code` VARCHAR(100) NOT NULL COMMENT '应用Code（全局唯一）',
  `app_name` VARCHAR(200) NOT NULL COMMENT '应用名称',
  `app_key` VARCHAR(500) NOT NULL COMMENT '应用密钥（base64编码）',
  `description` TEXT NOT NULL COMMENT '应用描述',
  `admin_user_id` VARCHAR(50) NULL COMMENT '应用管理员工号',
  `developer_ids` TEXT NULL COMMENT '应用开发者工号列表（JSON数组）',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
  `create_by` VARCHAR(50) NULL COMMENT '创建人工号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(50) NULL COMMENT '更新人工号',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_code` (`app_code`),
  KEY `idx_admin_user_id` (`admin_user_id`),
  KEY `idx_create_by` (`create_by`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用管理表';

-- 创建应用注册信息表
CREATE TABLE IF NOT EXISTS `application_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_code` VARCHAR(100) NOT NULL COMMENT '应用Code',
  `ip_address` VARCHAR(50) NOT NULL COMMENT '注册IP地址',
  `machine_tag` VARCHAR(200) NULL COMMENT '注册机器标签',
  `register_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `extra_info` TEXT NULL COMMENT '额外信息（JSON格式）',
  PRIMARY KEY (`id`),
  KEY `idx_app_code` (`app_code`),
  KEY `idx_register_time` (`register_time`),
  CONSTRAINT `fk_app_registration_code` FOREIGN KEY (`app_code`) REFERENCES `application` (`app_code`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用注册信息表';

