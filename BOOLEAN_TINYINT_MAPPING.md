# Boolean类型在Java和MySQL中的映射说明

## 类型映射关系

### Java → MySQL 类型映射

| Java类型 | MySQL类型 | 存储值 | 说明 |
|---------|----------|-------|------|
| `Boolean` / `boolean` | `TINYINT(1)` | `1` / `0` | MyBatis自动映射 |
| `true` | `1` | 1字节 | 启用/激活 |
| `false` | `0` | 1字节 | 禁用/非激活 |
| `null` | `NULL` | - | 允许空值时 |

## 为什么使用TINYINT(1)而不是其他类型？

### 1. MySQL没有真正的Boolean类型
MySQL中的 `BOOLEAN` 实际上是 `TINYINT(1)` 的同义词：
```sql
-- 以下两种定义完全等价
CREATE TABLE test (flag BOOLEAN);
CREATE TABLE test (flag TINYINT(1));
```

### 2. TINYINT(1)的优势
- **存储效率**: 仅占用1字节
- **标准映射**: MyBatis、Hibernate等ORM框架标准映射
- **索引友好**: 数值类型索引效率高
- **兼容性好**: 所有MySQL版本都支持

### 3. 其他类型对比

| 类型 | 存储空间 | Java映射 | 推荐度 |
|-----|---------|---------|-------|
| `TINYINT(1)` | 1字节 | Boolean | ⭐⭐⭐⭐⭐ |
| `CHAR(1)` | 1字符 | String | ⭐⭐ (需要转换) |
| `VARCHAR(1)` | 1-2字节 | String | ⭐ (浪费空间) |
| `BIT(1)` | 1位 | Boolean | ⭐⭐⭐ (部分工具不友好) |
| `ENUM('true','false')` | 1-2字节 | String | ⭐⭐ (可读性好但效率低) |

## 数据库定义示例

### 标准定义（推荐）
```sql
CREATE TABLE `user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态：1-启用(true)，0-禁用(false)',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 允许NULL的定义
```sql
`status` TINYINT(1) NULL DEFAULT NULL COMMENT '状态：1-启用(true)，0-禁用(false)，NULL-未设置'
```

### 使用BOOLEAN关键字（等效）
```sql
`status` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '状态'
-- MySQL会自动将其转换为 TINYINT(1)
```

## MyBatis自动映射

### MyBatis ResultMap配置
```xml
<resultMap id="BaseResultMap" type="com.example.entity.User">
    <result column="status" property="status" jdbcType="BOOLEAN"/>
    <!-- 或者使用 jdbcType="TINYINT" 效果相同 -->
</resultMap>
```

### Java实体类
```java
public class User {
    private Boolean status;  // 自动映射 TINYINT(1)
    
    // 使用基本类型（不推荐，因为不能为null）
    // private boolean status;
}
```

## 查询和插入操作

### 插入数据
```sql
-- 使用数值
INSERT INTO user (username, status) VALUES ('test1', 1);    -- true
INSERT INTO user (username, status) VALUES ('test2', 0);    -- false

-- 使用布尔值（MySQL会自动转换）
INSERT INTO user (username, status) VALUES ('test3', TRUE);  -- 转为1
INSERT INTO user (username, status) VALUES ('test4', FALSE); -- 转为0
```

### 查询数据
```sql
-- 数值比较
SELECT * FROM user WHERE status = 1;     -- 查询启用的用户
SELECT * FROM user WHERE status = 0;     -- 查询禁用的用户

-- 布尔比较（效果相同）
SELECT * FROM user WHERE status = TRUE;  -- 查询启用的用户
SELECT * FROM user WHERE status = FALSE; -- 查询禁用的用户

-- NULL检查
SELECT * FROM user WHERE status IS NULL;
SELECT * FROM user WHERE status IS NOT NULL;
```

### 更新数据
```sql
-- 更新为启用
UPDATE user SET status = 1 WHERE id = 1;
UPDATE user SET status = TRUE WHERE id = 1;  -- 效果相同

-- 更新为禁用
UPDATE user SET status = 0 WHERE id = 2;
UPDATE user SET status = FALSE WHERE id = 2; -- 效果相同
```

## Java代码示例

### 使用示例
```java
// 检查用户是否启用
if (user.getStatus() != null && user.getStatus()) {
    // 用户已启用
}

// 检查用户是否禁用
if (user.getStatus() != null && !user.getStatus()) {
    // 用户已禁用
}

// 设置状态
user.setStatus(true);   // 启用
user.setStatus(false);  // 禁用
user.setStatus(null);   // 清除状态（如果字段允许NULL）
```

### Spring JPA示例
```java
@Entity
@Table(name = "user")
public class User {
    @Column(name = "status", columnDefinition = "TINYINT(1)")
    private Boolean status;
}
```

## MyBatis动态SQL

### 正确的判断方式
```xml
<!-- 检查Boolean类型是否为null -->
<if test="status != null">
    AND status = #{status}
</if>

<!-- 错误方式（Boolean不能用空字符串判断） -->
<!-- <if test="status != null and status != ''"> 这是错误的！ -->
```

### 示例查询
```xml
<select id="selectUserList" resultMap="BaseResultMap">
    SELECT * FROM user
    <where>
        <if test="status != null">
            AND status = #{status}
        </if>
    </where>
</select>
```

## 前端JSON格式

### 请求参数
```json
{
  "status": true   // 布尔值，不是字符串
}
```

### 响应数据
```json
{
  "id": 1,
  "username": "admin",
  "status": true   // 布尔值，不是 "1" 或 1
}
```

## 注意事项

### 1. NULL值处理
```java
// 错误：可能导致NullPointerException
if (user.getStatus()) { ... }

// 正确：先检查null
if (user.getStatus() != null && user.getStatus()) { ... }

// 或使用基本类型boolean（但不能表示NULL）
private boolean status;
```

### 2. 数据库默认值
```sql
-- 推荐：设置默认值
`status` TINYINT(1) NOT NULL DEFAULT 1

-- 允许NULL时
`status` TINYINT(1) NULL DEFAULT NULL
```

### 3. 索引优化
```sql
-- 为经常查询的Boolean字段添加索引
CREATE INDEX idx_status ON user(status);

-- 复合索引
CREATE INDEX idx_status_create_time ON user(status, create_time);
```

### 4. 字段注释规范
```sql
-- 推荐格式（说明值的含义）
COMMENT '状态：1-启用(true)，0-禁用(false)'

-- 或更简洁
COMMENT '是否启用：1-是，0-否'
```

## 迁移检查清单

- [x] 数据库字段定义为 `TINYINT(1)`
- [x] Java实体类定义为 `Boolean`
- [x] MyBatis Mapper XML 使用 `jdbcType="BOOLEAN"` 或 `jdbcType="TINYINT"`
- [x] 动态SQL中不使用字符串比较（`!= ''`）
- [x] Java代码中检查null值
- [x] 前端使用布尔值（true/false）而非字符串
- [x] 数据库字段设置合理的默认值
- [x] 更新相关的测试用例

## 性能对比

| 操作 | TINYINT(1) | VARCHAR(1) | 性能差异 |
|-----|-----------|-----------|---------|
| 存储空间 | 1字节 | 1-2字节 | TINYINT更优 |
| 索引大小 | 更小 | 更大 | TINYINT更优 |
| 比较速度 | 整数比较 | 字符串比较 | TINYINT更快 |
| 排序速度 | 整数排序 | 字符串排序 | TINYINT更快 |

## 总结

在Java和MySQL的集成中：
- ✅ Java使用 `Boolean` 类型
- ✅ MySQL使用 `TINYINT(1)` 类型
- ✅ 值映射：`true` ↔ `1`, `false` ↔ `0`, `null` ↔ `NULL`
- ✅ MyBatis自动处理类型转换
- ✅ 性能优秀，语义清晰

这是业界标准做法，被广泛使用和验证！

