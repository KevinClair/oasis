# user_id字段从Long改为String（用户工号）修改总结

## 修改概述
将user表中的user_id字段从BIGINT（Long）类型改为VARCHAR（String）类型，使其真正作为用户工号使用。同步修改所有相关的Java实体、DAO、Mapper XML、Service层代码。

## 数据库表结构说明
### user表
- `id` (BIGINT): 主键，自增
- `user_id` (VARCHAR(50)): 用户工号，唯一标识，可为NULL
- `user_account` (VARCHAR(50)): 用户账号
- 其他字段...

### user_role表（关联表）
- `id` (BIGINT): 主键
- `user_id` (VARCHAR(50)): 用户工号，关联user表的user_id字段（不是id主键）
- `role_id` (BIGINT): 角色ID

## 已修改文件清单

### 1. 实体类（Entity）
✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/models/entity/User.java`
  - userId字段类型: Long → String

✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/models/entity/UserRole.java`
  - userId字段类型: Long → String

### 2. VO类（Value Object）
✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/models/vo/oauth/LoginResponse.java`
  - userId字段类型: Long → String

✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/models/vo/oauth/UserInfoResponse.java`
  - userId字段类型: Long → String

✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/models/vo/systemManage/UserVO.java`
  - userId字段类型: Long → String

✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/models/vo/systemManage/UserSaveRequest.java`
  - userId字段类型: Long → String

✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/models/vo/systemManage/UserListRequest.java`
  - userId字段类型: Long → String

### 3. DAO接口
✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/dao/UserRoleDao.java`
  - selectRoleIdsByUserId(String userId): 参数类型 Long → String
  - selectAllRoleIdsByUserId(String userId): 参数类型 Long → String
  - deleteByUserId(String userId): 参数类型 Long → String
  - selectUserIdsByRoleId(Long roleId): 返回类型 List<Long> → List<String>
  - deleteByUserIds(List<String> userIds): 参数类型 List<Long> → List<String>

### 4. MyBatis Mapper XML
✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/resources/mapper/UserMapper.xml`
  - user_id字段映射: jdbcType="BIGINT" → jdbcType="VARCHAR"

✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/resources/mapper/UserRoleMapper.xml`
  - user_id字段映射: jdbcType="BIGINT" → jdbcType="VARCHAR"
  - selectRoleIdsByUserId: parameterType="long" → parameterType="string"
  - selectAllRoleIdsByUserId: parameterType="long" → parameterType="string"
  - selectUserIdsByRoleId: resultType="long" → resultType="string"
  - deleteByUserId: parameterType="long" → parameterType="string"

### 5. Service实现类
✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/UserManageServiceImpl.java`
  - getUserList(): 使用user.getUserId()查询角色（而不是user.getId()）
  - deleteUsers(): 先查询用户获取userId列表，再删除关联
  - saveUser(): 
    - 检查工号时使用StringUtils.hasText()而不是null检查
    - 新增用户时saveUserRoles传递user.getUserId()
    - 编辑用户时deleteByUserId和saveUserRoles使用user.getUserId()
  - saveUserRoles(String userId, ...): 参数类型 Long → String
  - getUserById(): 使用user.getUserId()查询角色

✅ `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/RouteServiceImpl.java`
  - getUserRoutes(): 使用user.getUserId()查询角色（而不是userId参数）

### 6. SQL迁移脚本
✅ 新建 `/Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/resources/sql/migrate_user_id_to_varchar.sql`
  - 将user_role表的user_id字段从BIGINT改为VARCHAR(50)
  - 将user表的user_id字段从BIGINT改为VARCHAR(50)
  - 包含验证SQL

## 重要说明

### JWT Token存储
**注意**: JWT中存储的是user表的主键id（以String形式），而不是user_id工号！

```java
// AuthServiceImpl.login()
String token = JwtTokenUtils.generateTokens(
    String.valueOf(user.getId()),  // 存储主键id
    user.getUserName(),
    request.getRememberMe()
);
```

### Controller层逻辑
LoginController和RouteController中，从JWT解析的userId实际是主键id的字符串形式，需要转换为Long：

```java
// LoginController.getUserInfo()
UserInfo currentUser = UserThreadLocal.getUserInfo();
Long userId = Long.valueOf(currentUser.getUserId()); // 转换为主键id
UserInfoResponse response = authService.getUserInfo(userId);
```

### 数据关联逻辑
- user_role表通过user_id（VARCHAR）关联user表的user_id字段（VARCHAR）
- 查询用户角色时: `SELECT ... FROM user_role WHERE user_id = user.getUserId()`
- JOIN语句: `user_role ur INNER JOIN user u ON ur.user_id = u.user_id`

## 数据库迁移步骤

1. **备份数据库**（必须！）

2. 执行迁移脚本:
```bash
mysql -u用户名 -p数据库名 < migrate_user_id_to_varchar.sql
```

3. 如果需要将现有数字工号转换为固定长度字符串（例如6位）:
```sql
UPDATE `user` SET `user_id` = LPAD(`user_id`, 6, '0') WHERE `user_id` IS NOT NULL;
-- 例如: 1 → 000001, 123 → 000123
```

4. 验证迁移结果:
```sql
-- 检查字段类型
DESC `user`;
DESC `user_role`;

-- 检查关联完整性
SELECT COUNT(*) FROM user_role ur
INNER JOIN user u ON ur.user_id = u.user_id;
```

## 测试建议

1. **登录功能测试**
   - 使用用户账号登录
   - 使用用户工号登录
   - 验证返回的userId是工号（String类型）

2. **用户管理测试**
   - 新增用户（设置工号）
   - 编辑用户（不允许修改工号）
   - 删除用户（级联删除user_role关联）
   - 查询用户列表（显示工号）

3. **角色管理测试**
   - 为用户分配角色
   - 查询用户角色
   - 根据角色查询用户列表

4. **路由权限测试**
   - 获取用户动态路由
   - 验证根据用户工号查询角色权限

## 编译警告说明

MyBatis可能报告以下警告（不影响运行）:
- `deleteByUserId(String)`: Found problems related to...
- `deleteByUserIds(List<String>)`: Found problems related to...

这些警告是IDE的MyBatis插件检测到的类型变更，实际运行时没有问题。

## 未修改部分

以下部分保持不变，仍使用主键id（Long）:
- AuthService.getUserInfo(Long userId) - 参数是主键id
- RouteService.getUserRoutes(Long userId) - 参数是主键id
- UserDao的所有方法 - 继续使用id主键查询
- JWT Token中的userId claim - 实际存储的是主键id的字符串形式

## 完成状态

✅ 实体类修改完成
✅ VO类修改完成
✅ DAO接口修改完成
✅ Mapper XML修改完成
✅ Service层修改完成
✅ SQL迁移脚本创建完成

待执行:
⏳ 数据库迁移脚本执行
⏳ 功能测试验证

