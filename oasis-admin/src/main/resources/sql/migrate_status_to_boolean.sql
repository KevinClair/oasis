-- 将用户表的status字段统一修改为TINYINT(1)类型（对应Java的Boolean类型）
-- 注意：执行前请备份数据库

-- 查看当前status字段类型
DESC `user`;

-- 方案1：如果当前status是VARCHAR类型
-- 步骤：添加临时列 -> 数据迁移 -> 删除旧列 -> 重命名新列

-- 1. 添加临时列
ALTER TABLE `user` ADD COLUMN `status_new` TINYINT(1) NULL COMMENT '状态：1-启用(true)，0-禁用(false)' AFTER `status`;

-- 2. 迁移数据
-- 从VARCHAR：'1' -> 1(true), '2' -> 0(false), 其他 -> NULL
-- 从INT：1 -> 1(true), 0 -> 0(false), 其他 -> NULL
UPDATE `user`
SET `status_new` = CASE
                       WHEN `status` = '1' OR `status` = 1 THEN 1
                       WHEN `status` = '2' OR `status` = 0 THEN 0
                       ELSE NULL
    END;

-- 3. 删除旧的status列
ALTER TABLE `user` DROP COLUMN `status`;

-- 4. 重命名新列为status
ALTER TABLE `user` CHANGE COLUMN `status_new` `status` TINYINT(1) NULL COMMENT '状态：1-启用(true)，0-禁用(false)';

-- 5. 设置默认值为1（启用/true）
ALTER TABLE `user` MODIFY COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用(true)，0-禁用(false)';

-- 查看修改后的表结构
DESC `user`;

-- 验证数据
SELECT id, username, status,
       CASE WHEN status = 1 THEN 'true/启用' ELSE 'false/禁用' END AS status_desc
FROM `user` LIMIT 10;


