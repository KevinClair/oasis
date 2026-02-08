# 用户角色管理功能实现文档

## 概述

为用户管理功能添加角色关联，用户的角色列表作为用户表的一个字段（user_roles），存储角色编码数组。

## 一、数据库设计

### 1. 用户表字段扩展

**SQL脚本**: `oasis-admin/src/main/resources/sql/add_user_roles_field.sql`

```sql
-- 为用户表添加角色列表字段
ALTER TABLE `user`
    ADD COLUMN `user_roles` JSON NULL COMMENT '用户角色列表（角色编码数组）' AFTER `status`;
```

**字段说明**:

- `user_roles` - JSON类型 - 存储角色编码数组，例如: `["ADMIN", "USER"]`
- 在status字段后添加
- 允许为NULL

## 二、后端实现

### 1. JSON类型处理器

**文件**: `config/JsonTypeHandler.java`

**功能**: 处理MySQL JSON字段与Java List<String>之间的转换

**关键方法**:

- `setNonNullParameter` - 将List转换为JSON字符串存入数据库
- `getNullableResult` - 从数据库读取JSON并转换为List
- 使用Jackson的ObjectMapper进行序列化/反序列化

### 2. 实体类更新

**文件**: `models/entity/User.java`

添加字段:

```java
/**
 * 用户角色列表（角色编码数组）
 */
private List<String> userRoles;
```

### 3. MyBatis映射更新

**文件**: `mapper/UserMapper.xml`

**ResultMap更新**:

```xml

<result column="user_roles" property="userRoles" jdbcType="VARCHAR"
        typeHandler="com.github.kevin.oasis.config.JsonTypeHandler"/>
```

**字段列表更新**:

```xml

<sql id="Base_Column_List">
    id, user_id, user_account, user_name, password, nick_name, user_gender,
    email, phone, status, user_roles,
    create_by, create_time, update_by, update_time
</sql>
```

### 4. 角色查询接口

#### RoleDao新增方法

**文件**: `dao/RoleDao.java`

```java
/**
 * 查询所有启用的角色
 *
 * @return 角色列表
 */
List<Role> selectAllEnabledRoles();
```

#### RoleMapper.xml新增SQL

**文件**: `mapper/RoleMapper.xml`

```xml

<select id="selectAllEnabledRoles" resultMap="BaseResultMap">
    SELECT id, role_name, role_code, role_desc, status,
    create_by, create_time, update_by, update_time
    FROM role
    WHERE status = TRUE
    ORDER BY create_time ASC
</select>
```

#### Service层新增方法

**文件**: `services/RoleManageService.java`

```java
/**
 * 获取所有启用的角色
 *
 * @return 角色列表
 */
List<RoleVO> getAllEnabledRoles();
```

**文件**: `services/impl/RoleManageServiceImpl.java`

实现查询所有启用角色并转换为VO的逻辑。

#### Controller新增接口

**文件**: `controller/RoleManageController.java`

```java
/**
 * 获取所有启用的角色（用于下拉选择）
 *
 * @return 角色列表
 */
@GetMapping("/getAllEnabledRoles")
@Permission
public Response<List<RoleVO>> getAllEnabledRoles()
```

**接口地址**: `GET /systemManage/role/getAllEnabledRoles`

## 三、前端实现

### 1. API接口更新

**文件**: `service/api/system-manage.ts`

更新`fetchGetAllRoles`函数:

```typescript
export function fetchGetAllRoles() {
    return request<Api.SystemManage.AllRole[]>({
        url: '/systemManage/role/getAllEnabledRoles',
        method: 'get'
    });
}
```

### 2. 用户操作抽屉更新

**文件**: `views/manage/user/modules/user-operate-drawer.vue`

#### 主要改动:

1. **导入更新**:
    - 移除`jsonClone`导入
    - 使用`enableStatusBooleanOptions`替代`enableStatusOptions`

2. **状态选项计算属性**:

```typescript
const statusOptions = computed(() =>
    enableStatusBooleanOptions.map(item => ({
        label: $t(item.label as App.I18n.I18nKey),
        value: item.value as any
    }))
);
```

3. **默认模型更新**:

```typescript
function createDefaultModel(): Model {
    return {
        userName: '',
        userGender: null,
        nickName: '',
        userPhone: '',
        userEmail: '',
        userRoles: [],  // 默认空数组
        status: true    // 默认启用
    };
}
```

4. **角色选项获取**:

```typescript
async function getRoleOptions() {
    const {error, data} = await fetchGetAllRoles();

    if (!error) {
        const options = data.map(item => ({
            label: item.roleName,
            value: item.roleCode
        }));

        roleOptions.value = options;
    }
}
```

5. **模型初始化**:

```typescript
function handleInitModel() {
    model.value = createDefaultModel();

    if (props.operateType === 'edit' && props.rowData) {
        model.value = {
            userName: props.rowData.userName,
            userGender: props.rowData.userGender,
            nickName: props.rowData.nickName,
            userPhone: props.rowData.userPhone,
            userEmail: props.rowData.userEmail,
            userRoles: props.rowData.userRoles || [],  // 确保有默认值
            status: props.rowData.status ?? true
        };
    }
}
```

6. **表单字段**:

```vue

<NFormItem :label="$t('page.manage.user.userRole')" path="roles">
  <NSelect
      v-model:value="model.userRoles"
      multiple
      :options="roleOptions"
      :placeholder="$t('page.manage.user.form.userRole')"
  />
</NFormItem>
```

## 四、数据流程

### 1. 新增用户流程

1. 打开新增用户抽屉
2. 自动调用`fetchGetAllRoles()`获取所有启用的角色
3. 角色列表展示在下拉选择框中（多选）
4. 用户选择角色（可选择多个）
5. 提交时，`userRoles`字段包含选中的角色编码数组
6. 后端接收到数组，通过JsonTypeHandler转换为JSON存入数据库

### 2. 编辑用户流程

1. 打开编辑用户抽屉
2. 加载用户数据，包括`userRoles`字段
3. 从数据库读取JSON，通过JsonTypeHandler转换为List<String>
4. 前端展示时，回显已选择的角色
5. 用户可以修改角色选择
6. 提交时更新数据库中的JSON字段

### 3. 用户列表查询

1. 查询用户列表时，自动包含`user_roles`字段
2. JsonTypeHandler自动将JSON转换为List<String>
3. 前端接收到的数据中包含`userRoles`数组
4. 在用户列表中可以展示用户的角色信息

## 五、数据示例

### 数据库存储示例

```sql
-- 用户表中的user_roles字段存储
INSERT INTO user (user_name, user_roles, ...)
VALUES ('张三', '["ADMIN", "USER"]', ...);

-- 查询结果
SELECT user_name, user_roles
FROM user;
-- 结果: 张三 | ["ADMIN", "USER"]
```

### Java对象示例

```java
User user = new User();
user.

setUserName("张三");
user.

setUserRoles(Arrays.asList("ADMIN", "USER"));
```

### 前端数据示例

```typescript
// 用户对象
{
    userName: '张三',
        userRoles
:
    ['ADMIN', 'USER'],
    // ... 其他字段
}

// 角色选项
roleOptions: [
    {label: '超级管理员', value: 'SUPER_ADMIN'},
    {label: '管理员', value: 'ADMIN'},
    {label: '普通用户', value: 'USER'}
]
```

## 六、完成的功能

### ✅ 已实现功能

1. **数据库设计**
    - ✅ user表添加user_roles字段（JSON类型）
    - ✅ 存储角色编码数组

2. **后端实现**
    - ✅ JsonTypeHandler处理JSON与List的转换
    - ✅ User实体类添加userRoles字段
    - ✅ UserMapper.xml配置JSON字段映射
    - ✅ 获取所有启用角色的接口

3. **前端实现**
    - ✅ 新增用户时可选择角色（多选）
    - ✅ 编辑用户时可修改角色
    - ✅ 用户列表查询返回角色信息
    - ✅ 角色下拉选择框自动加载启用的角色

## 七、使用说明

### 部署步骤

1. **执行数据库脚本**:

```sql
source
oasis-admin/src/main/resources/sql/add_user_roles_field.sql
```

2. **重启后端服务**:

```bash
cd oasis-admin
mvn spring-boot:run
```

3. **前端无需额外操作**，刷新页面即可

### 使用流程

1. 打开用户管理页面
2. 点击"新增"按钮
3. 填写用户信息
4. 在"用户角色"下拉框中选择一个或多个角色
5. 点击确认保存

编辑用户时同样可以修改角色。

## 八、注意事项

1. **JSON字段类型**: MySQL 5.7.8+才支持JSON类型
2. **TypeHandler配置**: 必须在resultMap中指定typeHandler
3. **空值处理**: 允许userRoles为null或空数组
4. **角色编码**: 必须使用角色编码（roleCode），不是角色ID
5. **数据一致性**: 删除角色时需要考虑已分配该角色的用户

## 九、后续优化建议

1. 增加角色编码有效性验证
2. 批量分配角色功能
3. 用户角色历史记录
4. 角色权限联动检查
5. 导入导出时处理角色字段

---

**创建日期**: 2026-02-07  
**版本**: 1.0.0

