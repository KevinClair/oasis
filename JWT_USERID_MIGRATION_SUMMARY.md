# JWT存储工号修改总结

## 修改概述
将JWT Token中存储的内容从**用户主键id**改为**用户工号user_id**，同步修改所有相关的Service、Controller和DAO调用。

## 核心改动

### 之前的逻辑
```java
// 登录时生成Token - 存储主键id
String token = JwtTokenUtils.generateTokens(
    String.valueOf(user.getId()),  // 存储主键id
    user.getUserName(),
    rememberMe
);

// 从Token解析后，需要转换为Long查询用户
Long userId = Long.valueOf(currentUser.getUserId());
User user = userDao.selectById(userId);
```

### 修改后的逻辑
```java
// 登录时生成Token - 存储用户工号
String token = JwtTokenUtils.generateTokens(
    user.getUserId(),  // 存储用户工号（String类型）
    user.getUserName(),
    rememberMe
);

// 从Token解析后，直接使用工号查询用户
String userId = currentUser.getUserId();
User user = userDao.selectByUserAccountOrUserId(userId);
```

## 已修改文件清单

### 1. 模型类
✅ **UserInfo.java**
- 更新userId字段注释: "用户工号（存储在JWT中）"

### 2. Service接口
✅ **AuthService.java**
- `getUserInfo(String userId)`: 参数从 `Long userId` 改为 `String userId`
- 注释更新: "用户ID" → "用户工号"

✅ **RouteService.java**
- `getUserRoutes(String userId)`: 参数从 `Long userId` 改为 `String userId`
- 注释更新: "用户ID" → "用户工号"

### 3. Service实现类
✅ **AuthServiceImpl.java**
- `login()`: JWT生成使用 `user.getUserId()` 而不是 `user.getId()`
- `getUserInfo(String userId)`: 
  - 参数类型从 `Long` 改为 `String`
  - 使用 `userDao.selectByUserAccountOrUserId(userId)` 查询用户
  - 不再使用 `userDao.selectById(id)`

✅ **RouteServiceImpl.java**
- `getUserRoutes(String userId)`:
  - 参数类型从 `Long` 改为 `String`
  - 使用 `userDao.selectByUserAccountOrUserId(userId)` 查询用户
  - 不再使用 `userDao.selectById(id)`

### 4. Controller层
✅ **LoginController.java**
- `getUserInfo()`: 
  - 移除 `Long.valueOf()` 转换
  - 直接传递工号: `authService.getUserInfo(currentUser.getUserId())`

✅ **RouteController.java**
- `getUserRoutes()`:
  - 移除 `Long.valueOf()` 转换
  - 直接传递工号: `routeService.getUserRoutes(currentUser.getUserId())`

## 数据流程

### 登录流程
```
1. 用户输入账号/工号和密码
   ↓
2. AuthServiceImpl.login()
   - 查询用户: userDao.selectByUserAccountOrUserIdAndPassword()
   - 生成JWT: JwtTokenUtils.generateTokens(user.getUserId(), ...)
   ↓
3. 返回LoginResponse
   - token中包含用户工号
   - response.userId 是用户工号（String）
```

### 获取用户信息流程
```
1. 前端请求 GET /auth/getUserInfo，携带JWT Token
   ↓
2. 拦截器解析JWT，获取工号，存入ThreadLocal
   ↓
3. LoginController.getUserInfo()
   - 从ThreadLocal获取: UserInfo currentUser
   - 调用Service: authService.getUserInfo(currentUser.getUserId())
   ↓
4. AuthServiceImpl.getUserInfo(String userId)
   - 根据工号查询: userDao.selectByUserAccountOrUserId(userId)
   - 返回UserInfoResponse
```

### 获取用户路由流程
```
1. 前端请求 GET /route/getUserRoutes，携带JWT Token
   ↓
2. 拦截器解析JWT，获取工号，存入ThreadLocal
   ↓
3. RouteController.getUserRoutes()
   - 从ThreadLocal获取: UserInfo currentUser
   - 调用Service: routeService.getUserRoutes(currentUser.getUserId())
   ↓
4. RouteServiceImpl.getUserRoutes(String userId)
   - 根据工号查询用户: userDao.selectByUserAccountOrUserId(userId)
   - 根据user.getUserId()查询角色: userRoleDao.selectRoleIdsByUserId(user.getUserId())
   - 返回UserRouteResponse
```

## JWT Token结构

### JWT Claims内容
```json
{
  "sub": "user",
  "userId": "EMP001",        // 用户工号（String类型）
  "userName": "张三",
  "iat": 1709193600,
  "exp": 1709280000
}
```

### 解析后的UserInfo
```java
UserInfo {
    userId: "EMP001",        // 用户工号
    userName: "张三",
    roles: null,             // Controller从数据库查询
    permissions: null        // Controller从数据库查询
}
```

## 重要说明

### ✅ 优势
1. **语义一致**: JWT中的userId就是真实的用户工号，不需要额外转换
2. **简化逻辑**: 不需要在Controller层进行Long类型转换
3. **统一标识**: 系统中统一使用工号标识用户身份
4. **安全性**: 工号是业务标识，不暴露数据库主键结构

### ⚠️ 注意事项
1. **工号不能为空**: 如果用户没有设置工号，JWT生成会失败（user.getUserId()为null）
2. **查询性能**: `selectByUserAccountOrUserId()` 会先匹配user_account，再匹配user_id，需要确保索引优化
3. **数据迁移**: 现有Token失效，所有用户需要重新登录
4. **工号唯一性**: 必须确保user_id字段有唯一索引

### 🔧 依赖的UserDao方法
```java
// 使用这个方法根据工号查询用户
User selectByUserAccountOrUserId(@Param("user") String user);
```

该方法支持：
- 传入账号，查询user_account字段
- 传入工号，查询user_id字段

对应的SQL:
```xml
<select id="selectByUserAccountOrUserId" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM user
    WHERE user_account = #{user} OR user_id = #{user}
    LIMIT 1
</select>
```

## 测试验证

### 1. 登录测试
```bash
# 使用工号登录
POST /auth/login
{
  "user": "EMP001",
  "password": "123456"
}

# 响应
{
  "code": 200,
  "data": {
    "userId": "EMP001",    # 应该是工号
    "token": "eyJhbGc..."
  }
}
```

### 2. 解析Token验证
```javascript
// 解析JWT Token的payload
const payload = jwt.decode(token);
console.log(payload.userId);  // 应该输出 "EMP001"，不是主键id
```

### 3. 获取用户信息测试
```bash
GET /auth/getUserInfo
Authorization: Bearer <token>

# 应该正常返回用户信息
{
  "code": 200,
  "data": {
    "id": 1,
    "userId": "EMP001",
    "userName": "张三"
  }
}
```

### 4. 获取路由测试
```bash
GET /route/getUserRoutes
Authorization: Bearer <token>

# 应该正常返回用户路由
{
  "code": 200,
  "data": {
    "routes": [...],
    "home": "home"
  }
}
```

## 数据库要求

### 必须存在的索引
```sql
-- user表的user_id字段必须有唯一索引
ALTER TABLE `user` ADD UNIQUE KEY `uk_user_id` (`user_id`);

-- 或者组合索引（如果user_account和user_id都用于登录）
CREATE INDEX `idx_user_account_user_id` ON `user` (`user_account`, `user_id`);
```

### 数据完整性检查
```sql
-- 检查是否有用户工号为空
SELECT COUNT(*) FROM user WHERE user_id IS NULL OR user_id = '';

-- 如果有，需要为这些用户分配工号
UPDATE user SET user_id = CONCAT('EMP', LPAD(id, 6, '0')) WHERE user_id IS NULL;
```

## 影响范围

### ✅ 已修改（本次）
- JWT生成逻辑（存储工号）
- 用户信息查询（使用工号）
- 用户路由查询（使用工号）

### ✅ 已完成（之前）
- user表user_id字段类型: BIGINT → VARCHAR(50)
- user_role表user_id字段类型: BIGINT → VARCHAR(50)
- 所有实体类userId字段: Long → String
- user_role关联查询使用工号

### ⏸️ 不影响
- 用户管理CRUD（使用主键id操作）
- 角色管理（不直接使用userId）
- 菜单管理（不涉及用户）

## 完成状态

✅ JWT生成修改完成
✅ Service接口修改完成
✅ Service实现修改完成
✅ Controller层修改完成
✅ 文档更新完成

待执行:
⏳ 清除现有JWT Token（所有用户重新登录）
⏳ 功能测试验证
⏳ 性能测试（工号查询性能）

## 回滚方案

如果需要回滚到存储主键id的方案：

```java
// 1. AuthServiceImpl.login() - 改回存储主键id
String token = JwtTokenUtils.generateTokens(
    String.valueOf(user.getId()),  // 改回主键id
    user.getUserName(),
    rememberMe
);

// 2. AuthService.getUserInfo() - 改回Long参数
UserInfoResponse getUserInfo(Long userId);

// 3. AuthServiceImpl.getUserInfo() - 改回使用selectById
User user = userDao.selectById(userId);

// 4. RouteService.getUserRoutes() - 改回Long参数
UserRouteResponse getUserRoutes(Long userId);

// 5. RouteServiceImpl.getUserRoutes() - 改回使用selectById
User user = userDao.selectById(userId);

// 6. LoginController.getUserInfo() - 加回转换
Long userId = Long.valueOf(currentUser.getUserId());
authService.getUserInfo(userId);

// 7. RouteController.getUserRoutes() - 加回转换
Long userId = Long.valueOf(currentUser.getUserId());
routeService.getUserRoutes(userId);
```

## 总结

本次修改实现了JWT中存储用户工号而不是主键id，使得系统更加符合业务逻辑，用户标识更加统一。所有相关的Service和Controller都已经适配新的逻辑，可以直接使用工号进行用户查询和权限验证。

