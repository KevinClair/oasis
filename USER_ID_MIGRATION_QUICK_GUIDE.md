# user_id字段类型修改 - 快速参考指南

## 核心改动
将user表和user_role表的user_id字段从 **BIGINT(Long)** 改为 **VARCHAR(50)(String)**，使user_id真正作为用户工号使用。

## 数据表关系
```
user表:
  - id (BIGINT, 主键, 自增) 
  - user_id (VARCHAR(50), 用户工号, 唯一)
  
user_role表:
  - id (BIGINT, 主键)
  - user_id (VARCHAR(50), 关联user.user_id)
  - role_id (BIGINT, 关联role.id)
```

## 执行数据库迁移

### 步骤1: 备份数据库
```bash
mysqldump -u用户名 -p数据库名 > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 步骤2: 执行迁移脚本
```bash
cd /Users/kevin/develop/IdeaProjects/Oasis/oasis-admin/src/main/resources/sql
mysql -u用户名 -p数据库名 < migrate_user_id_to_varchar.sql
```

### 步骤3: 验证迁移
```sql
-- 检查字段类型
DESC user;
DESC user_role;

-- 验证数据完整性
SELECT COUNT(*) FROM user_role;
SELECT COUNT(*) FROM user_role ur INNER JOIN user u ON ur.user_id = u.user_id;
-- 两个结果应该相同
```

## 代码修改摘要

### Java实体类 (7个文件)
- User.java: `private String userId;`
- UserRole.java: `private String userId;`
- UserVO.java: `private String userId;`
- UserSaveRequest.java: `private String userId;`
- UserListRequest.java: `private String userId;`
- LoginResponse.java: `private String userId;`
- UserInfoResponse.java: `private String userId;`

### DAO接口 (1个文件)
UserRoleDao.java:
```java
List<Long> selectRoleIdsByUserId(@Param("userId") String userId);
List<Long> selectAllRoleIdsByUserId(@Param("userId") String userId);
int deleteByUserId(@Param("userId") String userId);
List<String> selectUserIdsByRoleId(@Param("roleId") Long roleId);
int deleteByUserIds(@Param("userIds") List<String> userIds);
```

### Mapper XML (2个文件)
- UserMapper.xml: `<result column="user_id" property="userId" jdbcType="VARCHAR"/>`
- UserRoleMapper.xml: `<result column="user_id" property="userId" jdbcType="VARCHAR"/>`

### Service实现 (2个文件)
UserManageServiceImpl.java关键修改:
```java
// 查询角色时使用工号
userRoleDao.selectRoleIdsByUserId(user.getUserId())

// 删除时先获取工号列表
List<String> userIds = new ArrayList<>();
for (Long id : request.getIds()) {
    User user = userDao.selectById(id);
    if (user != null && user.getUserId() != null) {
        userIds.add(user.getUserId());
    }
}
userRoleDao.deleteByUserIds(userIds);

// 保存角色关联使用工号
saveUserRoles(user.getUserId(), roleCodes);
```

RouteServiceImpl.java:
```java
// 获取用户路由时使用工号
userRoleDao.selectRoleIdsByUserId(user.getUserId())
```

## 重要提示

### ⚠️ JWT存储的是主键id，不是工号
```java
// 登录时生成Token
String token = JwtTokenUtils.generateTokens(
    String.valueOf(user.getId()),  // 存储主键id，不是user_id工号
    user.getUserName(),
    rememberMe
);

// Controller中获取用户信息
UserInfo currentUser = UserThreadLocal.getUserInfo();
Long id = Long.valueOf(currentUser.getUserId()); // 这里的userId实际是主键id
User user = userDao.selectById(id);
```

### ⚠️ user_role表通过工号关联
```sql
-- 正确的JOIN方式
SELECT ur.role_id 
FROM user_role ur 
INNER JOIN user u ON ur.user_id = u.user_id  -- 工号关联
WHERE ur.user_id = #{userId}
```

### ⚠️ 编辑用户时不允许修改工号
UserManageServiceImpl.saveUser()方法中，编辑用户时不会更新userId字段。

## 测试清单

- [ ] 使用账号登录成功
- [ ] 使用工号登录成功  
- [ ] 登录响应中userId显示为工号
- [ ] 新增用户并设置工号
- [ ] 编辑用户（工号不变）
- [ ] 为用户分配角色
- [ ] 查询用户列表显示工号
- [ ] 删除用户（级联删除角色关联）
- [ ] 获取用户动态路由（基于角色权限）
- [ ] 根据角色查询用户列表

## 回滚方案

如果需要回滚，执行以下SQL:
```sql
-- 1. 修改user_role表
ALTER TABLE `user_role` MODIFY COLUMN `user_id` BIGINT NOT NULL COMMENT '用户ID';

-- 2. 修改user表
ALTER TABLE `user` MODIFY COLUMN `user_id` BIGINT NULL COMMENT '用户ID';

-- 3. 还原代码到修改前的版本
git checkout <commit-hash>
```

## 联系信息
如有问题，请查看详细文档: `USER_ID_MIGRATION_SUMMARY.md`

