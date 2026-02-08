-- 为用户表添加角色列表字段
ALTER TABLE `user`
    ADD COLUMN `user_roles` JSON NULL COMMENT '用户角色列表（角色编码数组）' AFTER `status`;

-- 更新现有数据，给用户添加默认角色
-- UPDATE `user` SET `user_roles` = JSON_ARRAY('USER') WHERE `user_roles` IS NULL;

