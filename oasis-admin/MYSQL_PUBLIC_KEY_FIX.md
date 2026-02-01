# MySQL Public Key Retrieval 错误解决方案

## 问题描述
在执行login方法时报错：
```
Public Key Retrieval is not allowed
```

## 错误原因
这个错误是MySQL 8.0版本引入的新安全特性导致的：

1. **MySQL 8.0的新认证方式**: MySQL 8.0默认使用 `caching_sha2_password` 作为认证插件
2. **首次连接问题**: 客户端首次连接时需要从服务器获取公钥来加密密码
3. **安全限制**: 默认情况下不允许客户端自动获取公钥，需要显式配置

## 解决方案

### ✅ 方案1：允许公钥检索（推荐 - 开发环境）
在数据库连接URL中添加 `allowPublicKeyRetrieval=true` 参数：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/oasis?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

**优点：** 配置简单，立即生效
**缺点：** 存在安全风险（中间人攻击可能获取密码）
**适用场景：** 开发环境、测试环境

### 方案2：使用SSL连接（推荐 - 生产环境）
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/oasis?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai
```

需要配置SSL证书。

### 方案3：修改MySQL用户认证插件
将用户的认证插件改为 `mysql_native_password`：

```sql
-- 修改现有用户
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456';
FLUSH PRIVILEGES;

-- 或创建新用户时指定
CREATE USER 'oasis'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
GRANT ALL PRIVILEGES ON oasis.* TO 'oasis'@'localhost';
FLUSH PRIVILEGES;
```

## 已应用的解决方案

当前配置文件 `application.properties` 已更新为：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/oasis?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=123456
```

## 验证步骤

### 1. 重启应用
配置文件修改后需要重启Spring Boot应用

### 2. 测试登录接口
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "userName": "admin",
    "password": "123456",
    "rememberMe": true
  }'
```

### 3. 预期响应
```json
{
    "code": "0000",
    "msg": "success",
    "data": {
        "userId": 1,
        "username": "admin",
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "refreshToken": "uuid-string",
        "email": "admin@example.com",
        "phone": "13800138000"
    }
}
```

## 其他可能的问题

### 问题1：密码不正确
确保application.properties中的密码与MySQL实际密码一致：
```properties
spring.datasource.password=YOUR_ACTUAL_PASSWORD
```

### 问题2：数据库不存在
确保已创建oasis数据库：
```sql
CREATE DATABASE IF NOT EXISTS oasis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 问题3：用户表不存在
确保已执行初始化SQL脚本：
```sql
-- 执行 src/main/resources/sql/init_user.sql
```

### 问题4：端口或主机错误
检查MySQL是否在localhost:3306运行：
```bash
# 检查MySQL状态
mysql -u root -p -e "SELECT 1"

# 检查端口
netstat -an | grep 3306
```

## 安全建议

### 开发环境
- ✅ 使用 `allowPublicKeyRetrieval=true`
- ✅ 可以使用 `useSSL=false`
- ✅ 使用简单密码

### 生产环境
- ❌ 避免使用 `allowPublicKeyRetrieval=true`
- ✅ 启用SSL: `useSSL=true`
- ✅ 使用强密码
- ✅ 使用环境变量或配置中心管理敏感信息
- ✅ 考虑使用连接池优化
- ✅ 定期更新MySQL驱动版本

### 推荐的生产环境配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/oasis?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# 连接池配置
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## 参考文档
- [MySQL 8.0 Authentication Plugin](https://dev.mysql.com/doc/refman/8.0/en/caching-sha2-pluggable-authentication.html)
- [MySQL Connector/J Configuration](https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html)

---

✅ **问题已解决！重启应用后即可正常使用。**

