# 用户角色管理 - 快速启动指南

## 一、执行数据库脚本

```sql
-- 为用户表添加角色字段
ALTER TABLE `user`
    ADD COLUMN `user_roles` JSON NULL COMMENT '用户角色列表（角色编码数组）' AFTER `status`;

-- 测试数据（可选）
-- 给现有用户分配角色
UPDATE `user`
SET `user_roles` = JSON_ARRAY('ADMIN', 'USER')
WHERE id = 1;
```

## 二、功能说明

### 1. 新增用户时选择角色

1. 打开用户管理页面
2. 点击"新增"按钮
3. 填写用户基本信息
4. 在"用户角色"下拉框中选择角色（支持多选）
5. 保存用户

### 2. 编辑用户时修改角色

1. 在用户列表中点击"编辑"按钮
2. 修改用户信息
3. 在"用户角色"下拉框中修改角色选择
4. 保存更改

### 3. 用户列表显示角色

用户列表查询时自动返回用户的角色信息，可以在表格中展示用户拥有的角色。

## 三、API接口

### 获取所有启用的角色

**接口**: `GET /systemManage/role/getAllEnabledRoles`

**响应**:

```json
{
  "code": "0000",
  "msg": "success",
  "data": [
    {
      "id": 1,
      "roleName": "超级管理员",
      "roleCode": "SUPER_ADMIN",
      "roleDesc": "拥有系统所有权限",
      "status": true
    },
    {
      "id": 2,
      "roleName": "管理员",
      "roleCode": "ADMIN",
      "roleDesc": "拥有系统管理权限",
      "status": true
    }
  ]
}
```

## 四、数据结构

### 用户表user_roles字段

**类型**: JSON  
**存储**: 角色编码数组  
**示例**:

```json
[
  "ADMIN",
  "USER"
]
```

### 前端用户对象

```typescript
{
  id: 1,
  userName: "张三",
  userRoles: ["ADMIN", "USER"],
  // ... 其他字段
}
```

## 五、技术要点

### 1. JSON字段处理

**后端**: 使用自定义JsonTypeHandler处理JSON与List<String>的转换

**MyBatis配置**:

```xml
<result column="user_roles" property="userRoles" 
        typeHandler="com.github.kevin.oasis.config.JsonTypeHandler"/>
```

### 2. 多选下拉框

**前端**: 使用NSelect组件的multiple属性

```vue
<NSelect
  v-model:value="model.userRoles"
  multiple
  :options="roleOptions"
  :placeholder="$t('page.manage.user.form.userRole')"
/>
```

### 3. 角色选项加载

打开用户编辑抽屉时自动加载所有启用的角色，填充到下拉选项中。

## 六、常见问题

### Q1: 数据库报错JSON类型不支持？

**A**: 确保MySQL版本 >= 5.7.8，早期版本不支持JSON类型。
可以使用VARCHAR类型替代：

```sql
ALTER TABLE `user`
    ADD COLUMN `user_roles` VARCHAR(500) NULL;
```

### Q2: 用户没有角色时如何处理？

**A**:

- 数据库: 存储NULL或空数组`[]`
- Java: 返回空List
- 前端: 显示为空选择

### Q3: 如何在列表中显示用户角色？

**A**: 用户列表查询时已包含userRoles字段，可以在表格列中显示：

```typescript
{
  key: 'userRoles',
  title: '角色',
  render: row => row.userRoles?.join(', ') || '-'
}
```

### Q4: 删除角色后已分配的用户怎么办？

**A**: 当前实现中用户存储的是角色编码，删除角色后：

- 用户的userRoles字段仍然保留该编码
- 建议在删除角色前检查是否有用户使用
- 或者提供角色迁移功能

## 七、测试用例

### 1. 新增用户并分配角色

```bash
# 前置条件: 系统中有启用的角色
# 操作步骤:
1. 打开用户管理页面
2. 点击新增按钮
3. 填写用户名: "测试用户"
4. 选择角色: ADMIN, USER
5. 点击确认

# 预期结果:
- 用户创建成功
- 数据库user_roles字段为: ["ADMIN", "USER"]
```

### 2. 编辑用户角色

```bash
# 前置条件: 用户已存在且有角色
# 操作步骤:
1. 点击编辑按钮
2. 角色下拉框回显当前角色
3. 修改角色选择
4. 点击确认

# 预期结果:
- 角色更新成功
- 数据库user_roles字段已更新
```

### 3. 用户列表查询

```bash
# 操作步骤:
1. 打开用户管理页面
2. 查看用户列表

# 预期结果:
- 用户列表正常显示
- 每个用户的userRoles字段包含角色编码数组
```

## 八、下一步

完成用户角色管理后，可以继续实现：

1. 在用户列表中显示角色标签
2. 按角色筛选用户
3. 批量分配角色功能
4. 基于角色的权限控制

---

**需要帮助？** 查看详细文档：`USER_ROLE_MANAGEMENT_IMPLEMENTATION.md`

