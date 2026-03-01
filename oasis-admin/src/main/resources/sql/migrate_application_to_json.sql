-- 修改应用表字段为JSON类型，并重命名字段
-- 执行前请备份数据！

-- 1. 添加新的JSON类型列
ALTER TABLE `application`
ADD COLUMN `admin_user_ids_json` JSON COMMENT '应用管理员工号列表（JSON数组）' AFTER `description`,
ADD COLUMN `developer_user_ids` JSON COMMENT '应用开发者工号列表（JSON数组）' AFTER `admin_user_ids_json`;

-- 2. 迁移 admin_user_ids 数据（从TEXT转JSON）
UPDATE `application`
SET `admin_user_ids_json` = CAST(`admin_user_ids` AS JSON)
WHERE `admin_user_ids` IS NOT NULL AND `admin_user_ids` != '';

-- 3. 迁移 developer_ids 数据（从TEXT转JSON，并重命名）
UPDATE `application`
SET `developer_user_ids` = CAST(`developer_ids` AS JSON)
WHERE `developer_ids` IS NOT NULL AND `developer_ids` != '';

-- 4. 删除旧的TEXT列（谨慎操作，建议先验证数据）
-- ALTER TABLE `application` DROP COLUMN `admin_user_ids`;
-- ALTER TABLE `application` DROP COLUMN `developer_ids`;

-- 5. 重命名新列（删除旧列后执行）
-- ALTER TABLE `application` CHANGE COLUMN `admin_user_ids_json` `admin_user_ids` JSON COMMENT '应用管理员工号列表（JSON数组）';

-- 验证数据迁移
SELECT
    id,
    app_code,
    admin_user_ids,
    admin_user_ids_json,
    developer_ids,
    developer_user_ids,
    create_by
FROM `application`
LIMIT 10;

-- ========================================
-- 完整迁移步骤（分步执行）
-- ========================================

-- 步骤1: 添加新列并迁移数据（上面已完成）

-- 步骤2: 验证数据正确性
-- SELECT * FROM application WHERE JSON_LENGTH(admin_user_ids_json) != JSON_LENGTH(admin_user_ids);

-- 步骤3: 确认无误后，删除旧列
-- ALTER TABLE `application` DROP COLUMN `admin_user_ids`;
-- ALTER TABLE `application` DROP COLUMN `developer_ids`;

-- 步骤4: 重命名新列
-- ALTER TABLE `application` CHANGE COLUMN `admin_user_ids_json` `admin_user_ids` JSON COMMENT '应用管理员工号列表（JSON数组）';

-- 最终表结构
-- DESCRIBE `application`;

