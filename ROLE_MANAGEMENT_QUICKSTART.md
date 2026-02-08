# 角色管理功能 - 快速启动指南

## 步骤一：执行数据库脚本

```sql
-- 创建角色表
CREATE TABLE IF NOT EXISTS `role`
(
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码',
    `role_desc`   VARCHAR(200)          DEFAULT NULL COMMENT '角色描述',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '状态：1-启用(true)，0-禁用(false)',
    `create_by`   VARCHAR(50)           DEFAULT NULL COMMENT '创建人',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(50)           DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_role_name` (`role_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='角色表';

-- 插入测试数据
INSERT INTO `role` (`role_name`, `role_code`, `role_desc`, `status`, `create_by`)
VALUES ('超级管理员', 'SUPER_ADMIN', '拥有系统所有权限', 1, 'system'),
       ('管理员', 'ADMIN', '拥有系统管理权限', 1, 'system'),
       ('普通用户', 'USER', '普通用户权限', 1, 'system'),
       ('访客', 'GUEST', '只读访问权限', 0, 'system');
```

## 步骤二：启动后端服务

```bash
cd oasis-admin
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动

## 步骤三：启动前端服务

```bash
cd oasis-web
pnpm install  # 如果还没安装依赖
pnpm dev
```

前端将在 `http://localhost:5173` 启动

## 步骤四：访问角色管理页面

打开浏览器访问：`http://localhost:5173/manage/role`

## 功能清单

### ✅ 已实现功能

1. **角色列表展示**
    - 显示角色名称、编码、描述、状态
    - 状态使用颜色标签（绿色=启用，黄色=禁用）
    - 分页功能
    - 无序号列

2. **搜索过滤**
    - 按角色名称搜索
    - 按角色编码搜索
    - 按状态筛选

3. **新增角色**
    - 点击"新增"按钮
    - 填写角色信息
    - 角色编码唯一性校验

4. **编辑角色**
    - 点击操作列的"编辑"按钮
    - 修改角色信息

5. **删除角色**
    - 单个删除：点击操作列的"删除"按钮
    - 批量删除：选中多个角色后点击"批量删除"
    - 删除前有确认提示

6. **启用/禁用角色**
    - 单个切换：点击操作列的"启用"或"禁用"按钮
    - 批量切换：选中多个角色后点击"批量禁用/启用"
    - 按钮根据当前状态动态显示

## API接口列表

| 接口     | 方法   | URL                                 | 说明    |
|--------|------|-------------------------------------|-------|
| 获取角色列表 | POST | /systemManage/role/getRoleList      | 分页查询  |
| 保存角色   | POST | /systemManage/role/saveRole         | 新增/编辑 |
| 删除角色   | POST | /systemManage/role/deleteRoles      | 批量删除  |
| 切换状态   | POST | /systemManage/role/toggleRoleStatus | 批量切换  |
| 获取详情   | GET  | /systemManage/role/getRoleById/{id} | 获取单个  |

## 测试数据示例

### 新增角色请求

```json
{
  "roleName": "测试角色",
  "roleCode": "TEST_ROLE",
  "roleDesc": "这是一个测试角色",
  "status": true
}
```

### 查询列表请求

```json
{
  "current": 1,
  "size": 10,
  "roleName": "管理员",
  "roleCode": null,
  "status": true
}
```

### 删除角色请求

```json
{
  "ids": [1, 2, 3]
}
```

### 切换状态请求

```json
{
  "ids": [1, 2]
}
```

## 常见问题

### Q1: 后端报错找不到RoleManageService？

**A**: 这是IDE索引问题，请：

1. 在IDE中点击 "File -> Invalidate Caches / Restart"
2. 或者运行 `mvn clean install` 重新编译

### Q2: 前端页面显示不正常？

**A**: 请检查：

1. 后端服务是否正常启动（端口8080）
2. 浏览器控制台是否有错误
3. API接口是否返回正确数据

### Q3: 状态切换不生效？

**A**: 确保：

1. 数据库中status字段类型为TINYINT(1)
2. 后端使用Boolean类型映射
3. SQL使用 `NOT status` 切换状态

### Q4: 角色编码重复？

**A**: 系统会自动校验，如果编码已存在，会提示错误

## 下一步

完成角色管理后，可以继续实现：

1. 菜单管理
2. 用户-角色关联
3. 角色-菜单权限配置
4. 角色-按钮权限配置

---

**需要帮助？** 查看详细文档：`ROLE_MANAGEMENT_IMPLEMENTATION.md`

