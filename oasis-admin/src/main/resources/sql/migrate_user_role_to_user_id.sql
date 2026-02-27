-- 迁移user_role表的user_id字段，从关联user表的id改为关联user表的user_id
-- 执行此脚本前请先备份数据！

-- 步骤1: 创建临时表存储迁移数据
CREATE TEMPORARY TABLE temp_user_role AS
SELECT
    ur.id,
    u.user_id as new_user_id,
    ur.role_id,
    ur.create_by,
    ur.create_time
FROM user_role ur
INNER JOIN user u ON ur.user_id = u.id
WHERE u.user_id IS NOT NULL;

-- 步骤2: 清空原user_role表
TRUNCATE TABLE user_role;

-- 步骤3: 将迁移后的数据插入user_role表
INSERT INTO user_role (id, user_id, role_id, create_by, create_time)
SELECT id, new_user_id, role_id, create_by, create_time
FROM temp_user_role;

-- 步骤4: 删除临时表
DROP TEMPORARY TABLE temp_user_role;

-- 验证迁移结果
SELECT COUNT(*) as total_records FROM user_role;
SELECT COUNT(*) as valid_records
FROM user_role ur
INNER JOIN user u ON ur.user_id = u.user_id;

