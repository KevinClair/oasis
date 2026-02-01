# User Status字段类型修改文档

## 修改概述

将 `User` 实体类及相关VO类中的 `status` 字段从 `String` 类型修改为 `Boolean` 类型。

### 修改原因
- 更符合业务语义：启用/禁用是二元状态，Boolean类型更直观
- 类型安全：避免字符串比较的错误（如 "1", "2" 等）
- 简化判断逻辑：使用 `!user.getStatus()` 比 `user.getStatus() == 0` 更清晰

## 字段定义变更

### 修改前
```java
/**
 * 状态：1-启用，2-禁用
 */
private String status;
```

### 修改后
```java
/**
 * 状态：true-启用，false-禁用
 */
private Boolean status;
```

## 受影响的文件

### 1. 实体类和VO类

#### User.java (实体类)
- **路径**: `models/entity/User.java`
- **修改**: `private String status` → `private Boolean status`

#### UserVO.java
- **路径**: `models/vo/systemManage/UserVO.java`
- **修改**: `private String status` → `private Boolean status`

#### UserListRequest.java
- **路径**: `models/vo/systemManage/UserListRequest.java`
- **修改**: `private String status` → `private Boolean status`

### 2. Service层逻辑修改

#### AuthServiceImpl.java
**位置**: `services/impl/AuthServiceImpl.java`

##### login() 方法
修改前：
```java
if (user.getStatus() != null && user.getStatus() == 0) {
    log.warn("登录失败：用户已被禁用, userName={}", request.getUserName());
    throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户已被禁用");
}
```

修改后：
```java
if (user.getStatus() != null && !user.getStatus()) {
    log.warn("登录失败：用户已被禁用, userName={}", request.getUserName());
    throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户已被禁用");
}
```

##### getUserInfo() 方法
修改前：
```java
if (user.getStatus() != null && user.getStatus() == 0) {
    log.warn("查询用户信息失败：用户已被禁用, userId={}", userId);
    throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户已被禁用");
}
```

修改后：
```java
if (user.getStatus() != null && !user.getStatus()) {
    log.warn("查询用户信息失败：用户已被禁用, userId={}", userId);
    throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "用户已被禁用");
}
```

##### 类型转换处理
为保持API兼容性，UserInfoResponse仍使用Integer类型，需要转换：

```java
.status(user.getStatus() != null && user.getStatus() ? 1 : 0)
```

### 3. MyBatis Mapper修改

#### UserMapper.xml

##### ResultMap更新
修改前：
```xml
<result column="status" property="status" jdbcType="VARCHAR"/>
```

修改后：
```xml
<result column="status" property="status" jdbcType="BOOLEAN"/>
```

##### 查询条件更新
修改前：
```xml
<if test="request.status != null and request.status != ''">
    AND status = #{request.status}
</if>
```

修改后：
```xml
<if test="request.status != null">
    AND status = #{request.status}
</if>
```

## 数据库迁移

### MySQL表结构变更

执行脚本: `src/main/resources/sql/migrate_status_to_boolean.sql`

#### 迁移步骤

1. **添加临时列**
```sql
ALTER TABLE `user` ADD COLUMN `status_new` TINYINT(1) NULL AFTER `status`;
```

2. **数据迁移**
```sql
UPDATE `user` SET `status_new` = CASE 
    WHEN `status` = '1' THEN 1
    WHEN `status` = '2' THEN 0
    ELSE NULL
END;
```

3. **删除旧列并重命名**
```sql
ALTER TABLE `user` DROP COLUMN `status`;
ALTER TABLE `user` CHANGE COLUMN `status_new` `status` TINYINT(1) NULL;
```

4. **设置默认值**
```sql
ALTER TABLE `user` MODIFY COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1;
```

### 数据库类型映射

| Java类型 | MySQL类型 | 说明 |
|---------|----------|------|
| Boolean | TINYINT(1) | true=1, false=0 |
| null | NULL | 允许空值 |

## 判断逻辑对照表

### 启用状态判断

| 修改前 | 修改后 |
|-------|--------|
| `status.equals("1")` | `status == true` 或 `status` |
| `"1".equals(status)` | `Boolean.TRUE.equals(status)` |

### 禁用状态判断

| 修改前 | 修改后 |
|-------|--------|
| `status.equals("2")` | `status == false` 或 `!status` |
| `"2".equals(status)` | `Boolean.FALSE.equals(status)` |
| `status == 0` | `!status` |

### 空值检查

两种方式都需要检查null：
```java
// 修改前
if (status != null && status.equals("1"))

// 修改后
if (status != null && status)
```

## API接口影响

### 前端请求参数变化

#### UserListRequest

修改前：
```json
{
  "status": "1"  // 字符串类型
}
```

修改后：
```json
{
  "status": true  // 布尔类型
}
```

### 前端响应数据变化

#### UserVO

修改前：
```json
{
  "status": "1"  // 字符串
}
```

修改后：
```json
{
  "status": true  // 布尔值
}
```

#### UserInfoResponse（保持兼容）

仍然返回Integer类型：
```json
{
  "status": 1  // 整数：1表示启用，0表示禁用
}
```

## 测试要点

### 1. 单元测试
- 测试status为true时的逻辑
- 测试status为false时的逻辑
- 测试status为null时的处理

### 2. 集成测试
- 测试登录时禁用用户的拦截
- 测试用户列表查询的status过滤
- 测试getUserInfo接口的status返回

### 3. 数据库测试
```sql
-- 测试插入
INSERT INTO user (username, password, status) VALUES ('test', '123', 1);

-- 测试查询
SELECT * FROM user WHERE status = 1;
SELECT * FROM user WHERE status = 0;

-- 测试更新
UPDATE user SET status = 0 WHERE username = 'test';
```

## 回滚方案

如果需要回滚，执行以下步骤：

1. **添加临时VARCHAR列**
```sql
ALTER TABLE `user` ADD COLUMN `status_varchar` VARCHAR(1) NULL AFTER `status`;
```

2. **转换数据**
```sql
UPDATE `user` SET `status_varchar` = CASE 
    WHEN `status` = 1 THEN '1'
    WHEN `status` = 0 THEN '2'
    ELSE NULL
END;
```

3. **替换列**
```sql
ALTER TABLE `user` DROP COLUMN `status`;
ALTER TABLE `user` CHANGE COLUMN `status_varchar` `status` VARCHAR(1);
```

## 注意事项

1. **数据库备份**：执行迁移前务必备份数据库
2. **空值处理**：代码中要注意null检查，避免NullPointerException
3. **API兼容性**：UserInfoResponse保持Integer类型以维持向后兼容
4. **前端更新**：前端TypeScript类型定义需要同步更新
5. **测试验证**：迁移后需要全面测试各个功能点

## 前端类型定义更新建议

```typescript
// 前端类型定义需要更新
// typings/api/system-manage.d.ts
type User = Common.CommonRecord<{
  userName: string;
  userGender: UserGender | null;
  nickName: string;
  userPhone: string;
  userEmail: string;
  userRoles: string[];
  status: boolean;  // 从 EnableStatus 改为 boolean
}>;
```

## 总结

此次修改将status字段从String改为Boolean类型，主要优势：
- ✅ 类型更加语义化
- ✅ 代码可读性提高
- ✅ 减少字符串比较错误
- ✅ 数据库存储更高效（1字节 vs 可变长度）
- ✅ JSON API更加标准化

所有修改已完成，代码无编译错误，请执行数据库迁移脚本后进行测试。

