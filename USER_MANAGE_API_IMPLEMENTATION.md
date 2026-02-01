# 用户管理接口实现文档

## 概述
基于 `/systemManage/user/getUserList` 接口需求，在服务端新增了完整的用户管理功能。

## 接口信息

### 1. 接口地址
- **类路径**: `@RequestMapping("/systemManage/user")`
- **方法路径**: `@PostMapping("/getUserList")`
- **完整URL**: `POST /systemManage/user/getUserList`

### 2. 请求方式
- **方法**: POST
- **参数传递**: RequestBody (JSON格式)

### 3. 请求参数 (UserListRequest)

```json
{
  "current": 1,           // 当前页码（必填）
  "size": 10,             // 每页大小（必填）
  "userName": "admin",    // 用户名（可选，支持模糊查询）
  "userGender": "1",      // 性别：1-男，2-女（可选）
  "nickName": "管理员",    // 昵称（可选，支持模糊查询）
  "userPhone": "138",     // 手机号（可选，支持模糊查询）
  "userEmail": "admin",   // 邮箱（可选，支持模糊查询）
  "status": "1"           // 状态：1-启用，2-禁用（可选）
}
```

### 4. 响应结果 (UserListResponse)

```json
{
  "code": "0000",
  "msg": "success",
  "data": {
    "current": 1,
    "size": 10,
    "total": 100,
    "records": [
      {
        "id": 1,
        "userName": "admin",
        "nickName": "管理员",
        "userGender": "1",
        "userPhone": "13800138000",
        "userEmail": "admin@example.com",
        "status": "1",
        "userRoles": ["R_ADMIN"],
        "createBy": "system",
        "createTime": "2024-01-01 00:00:00",
        "updateBy": "admin",
        "updateTime": "2024-01-31 10:00:00"
      }
    ]
  }
}
```

## 实现详情

### 1. 数据库变更

扩展了 `user` 表结构，新增以下字段：

| 字段名 | 类型 | 说明 |
|--------|------|------|
| nick_name | VARCHAR(50) | 昵称 |
| user_gender | VARCHAR(1) | 性别：1-男，2-女 |
| status | VARCHAR(1) | 状态：1-启用，2-禁用（类型从INTEGER改为VARCHAR） |
| create_by | VARCHAR(50) | 创建人 |
| update_by | VARCHAR(50) | 更新人 |

**执行脚本**: `src/main/resources/sql/update_user_table.sql`

### 2. 后端实现

#### 2.1 实体类 (User.java)
- 位置: `models/entity/User.java`
- 修改: 添加了 `nickName`, `userGender`, `createBy`, `updateBy` 字段
- 修改: `status` 类型从 `Integer` 改为 `String`

#### 2.2 VO类
- **UserListRequest.java**: 用户列表查询请求参数
- **UserVO.java**: 用户信息展示对象
- **UserListResponse.java**: 用户列表响应对象

位置: `models/vo/systemManage/`

#### 2.3 DAO层 (UserDao.java)
新增方法:
```java
List<User> selectUserList(@Param("request") UserListRequest request);
Long countUserList(@Param("request") UserListRequest request);
```

#### 2.4 Mapper XML (UserMapper.xml)
- 更新了 resultMap 映射，包含新增字段
- 新增 `selectUserList` 查询，支持多条件查询和分页
- 新增 `countUserList` 统计总数

#### 2.5 Service层
- **接口**: `UserManageService.java`
- **实现**: `UserManageServiceImpl.java`
  - 实现分页查询逻辑
  - User 实体转换为 UserVO
  - 支持多条件模糊查询

#### 2.6 Controller层 (UserManageController.java)
- 路径: `/systemManage/user`
- 方法: `getUserList()`
- 注解: `@PostMapping("/getUserList")` + `@Permission`
- 参数: `@RequestBody UserListRequest request`

### 3. 前端配置

#### 3.1 API调用 (system-manage.ts)
```typescript
export function fetchGetUserList(params?: Api.SystemManage.UserSearchParams) {
  return request<Api.SystemManage.UserList>({
    url: '/systemManage/user/getUserList',
    method: 'post',  // 使用POST方法
    data: params     // 使用data传递参数
  });
}
```

#### 3.2 类型定义
前端已有完整的类型定义在 `typings/api/system-manage.d.ts`

## 查询功能说明

### 支持的查询条件
1. **精确匹配**: `userGender`, `status`
2. **模糊查询**: `userName`, `nickName`, `userPhone`, `userEmail`
3. **分页**: `current` (页码), `size` (每页大小)

### 查询示例

#### 示例1：查询所有启用的用户
```json
{
  "current": 1,
  "size": 10,
  "status": "1"
}
```

#### 示例2：模糊查询用户名包含"admin"的用户
```json
{
  "current": 1,
  "size": 10,
  "userName": "admin"
}
```

#### 示例3：组合查询（启用的男性用户）
```json
{
  "current": 1,
  "size": 10,
  "status": "1",
  "userGender": "1"
}
```

## 测试步骤

### 1. 数据库准备
```sql
-- 执行数据库更新脚本
source src/main/resources/sql/update_user_table.sql
```

### 2. 启动后端服务
```bash
cd oasis-admin
mvn spring-boot:run
```

### 3. 测试接口
使用 Postman 或 curl 测试:

```bash
curl -X POST http://localhost:8080/systemManage/user/getUserList \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "current": 1,
    "size": 10,
    "status": "1"
  }'
```

### 4. 前端测试
启动前端项目，访问用户管理页面，验证列表查询功能。

## 注意事项

1. **权限控制**: 接口使用了 `@Permission` 注解，需要登录后访问
2. **分页计算**: MyBatis 使用 LIMIT OFFSET，offset = (current - 1) * size
3. **用户角色**: UserVO 中的 `userRoles` 字段目前返回空数组，需要后续实现用户角色关联表查询
4. **数据迁移**: 如果已有数据，需要执行 SQL 脚本中注释的 UPDATE 语句更新 status 字段值

## 文件清单

### 后端新增/修改文件
```
oasis-admin/
├── src/main/java/com/github/kevin/oasis/
│   ├── controller/
│   │   └── UserManageController.java          (新增)
│   ├── dao/
│   │   └── UserDao.java                        (修改)
│   ├── models/
│   │   ├── entity/
│   │   │   └── User.java                       (修改)
│   │   └── vo/systemManage/
│   │       ├── UserListRequest.java            (新增)
│   │       ├── UserListResponse.java           (新增)
│   │       └── UserVO.java                     (新增)
│   └── services/
│       ├── UserManageService.java              (新增)
│       └── impl/
│           └── UserManageServiceImpl.java      (新增)
├── src/main/resources/
│   ├── mapper/
│   │   └── UserMapper.xml                      (修改)
│   └── sql/
│       └── update_user_table.sql               (新增)
```

### 前端修改文件
```
oasis-web/
└── src/service/api/
    └── system-manage.ts                         (修改)
```

## 后续扩展建议

1. **用户角色关联**: 实现用户角色关系查询，填充 `userRoles` 字段
2. **批量操作**: 添加批量启用/禁用、批量删除接口
3. **用户详情**: 添加根据ID查询用户详情接口
4. **用户CRUD**: 添加新增、编辑、删除用户接口
5. **导出功能**: 实现用户列表导出Excel功能
6. **数据验证**: 增强请求参数验证，如手机号、邮箱格式验证

