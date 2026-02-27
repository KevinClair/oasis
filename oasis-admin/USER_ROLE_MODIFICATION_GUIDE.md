# user_role表关联字段修改说明

## 修改概述
将`user_role`表的`user_id`字段从关联`user`表的`id`字段（主键）改为关联`user`表的`user_id`字段（用户工号）。

## 修改的文件

### 1. 数据库相关文件

#### /oasis-admin/src/main/resources/sql/create_user_role_table.sql
- 更新了`user_id`字段的注释，明确说明关联的是`user`表的`user_id`字段（用户工号）

#### /oasis-admin/src/main/resources/sql/migrate_user_role_to_user_id.sql（新增）
- 创建了数据迁移脚本，用于将现有数据从关联`user.id`改为关联`user.user_id`
- **重要：执行此脚本前请先备份数据！**

#### /oasis-admin/src/main/resources/mapper/UserRoleMapper.xml
- 修改了`selectUserIdsByRoleId`方法的SQL语句
- 将JOIN条件从`ur.user_id = u.id`改为`ur.user_id = u.user_id`

### 2. Java代码文件

#### /oasis-admin/src/main/java/com/github/kevin/oasis/models/entity/UserRole.java
- 更新了`userId`字段的注释，明确说明存储的是用户工号，关联`user`表的`user_id`字段

#### /oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/UserManageServiceImpl.java
修改了以下方法中的调用：

1. **getUserList方法（第55行）**
   - 从 `selectRoleIdsByUserId(user.getId())` 改为 `selectRoleIdsByUserId(user.getUserId())`

2. **deleteUsers方法（第100-128行）**
   - 重构了删除逻辑：先查询用户的工号列表，再删除用户角色关联
   - 确保`deleteByUserIds`接收的是用户工号列表而不是主键ID列表

3. **saveUser方法 - 新增用户（第188行）**
   - 从 `saveUserRoles(user.getId(), ...)` 改为 `saveUserRoles(user.getUserId(), ...)`

4. **saveUser方法 - 更新用户（第217-218行）**
   - 从 `deleteByUserId(user.getId())` 改为 `deleteByUserId(user.getUserId())`
   - 从 `saveUserRoles(user.getId(), ...)` 改为 `saveUserRoles(user.getUserId(), ...)`

5. **getUserById方法（第270行）**
   - 从 `selectAllRoleIdsByUserId(user.getId())` 改为 `selectAllRoleIdsByUserId(user.getUserId())`

## 注意事项

### 1. 数据迁移
- 如果已有生产数据，必须执行`migrate_user_role_to_user_id.sql`脚本进行数据迁移
- 迁移前务必备份数据库
- 验证迁移结果，确保所有记录都正确关联

### 2. 业务逻辑影响
- 现在`user_role`表的`user_id`字段存储的是用户工号（`user.user_id`），而不是主键ID（`user.id`）
- 在新增用户时，必须确保`user.user_id`字段有值
- 所有涉及用户角色关联的查询和操作都已更新为使用用户工号

### 3. 未修改的部分
- `UserRoleDao.java`接口定义没有修改，因为参数类型和返回值类型都是正确的
- `RouteServiceImpl.java`中的`getUserRoutes`方法已经使用的是`userId`（用户工号），无需修改
- `RoleManageServiceImpl.java`中的`deleteByRoleIds`方法按角色ID删除，无需修改

## 验证建议

1. 测试新增用户并分配角色
2. 测试编辑用户并修改角色
3. 测试删除用户（包括批量删除）
4. 测试获取用户列表，验证角色信息是否正确显示
5. 测试获取用户路由，验证角色权限是否正确
6. 测试按角色ID查询用户列表

## 潜在风险

1. 如果`user`表中存在`user_id`为NULL的记录，可能导致关联失败
2. 如果系统中有其他直接操作`user_role`表的SQL语句，需要一并检查和修改
3. 确保所有新增用户时都正确设置了`user_id`字段

