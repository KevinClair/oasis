-- 将user_id字段从BIGINT改为VARCHAR类型（用户工号）
-- 注意：执行前请务必备份数据库！

-- 步骤1: 修改user_role表的user_id字段类型为VARCHAR
ALTER TABLE `user_role` MODIFY COLUMN `user_id` VARCHAR(50) NOT NULL COMMENT '用户工号（关联user表的user_id字段）';

-- 步骤2: 修改user表的user_id字段类型为VARCHAR
ALTER TABLE `user` MODIFY COLUMN `user_id` VARCHAR(50) NULL COMMENT '用户工号';

-- 步骤3: 验证修改结果
DESC `user`;
DESC `user_role`;

-- 步骤4: 如果user表的user_id字段有唯一索引，需要重建
-- ALTER TABLE `user` DROP INDEX `uk_user_id`;
-- ALTER TABLE `user` ADD UNIQUE KEY `uk_user_id` (`user_id`);

-- 示例：更新现有数据（根据实际情况调整）
-- 如果需要将现有的数字工号转换为字符串格式：
-- UPDATE `user` SET `user_id` = LPAD(`user_id`, 6, '0') WHERE `user_id` IS NOT NULL;
-- 例如：将工号1转换为000001，工号123转换为000123

-- 验证user_role表的关联是否正确
SELECT COUNT(*) as total_user_roles FROM user_role;
SELECT COUNT(*) as valid_user_roles
FROM user_role ur
INNER JOIN user u ON ur.user_id = u.user_id;

