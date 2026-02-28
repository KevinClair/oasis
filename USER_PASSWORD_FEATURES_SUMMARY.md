# 用户管理和密码功能修改总结

## 修改完成时间
2026-02-28

## 修改内容概述

### 需求1: 用户工号字段改为字符串输入
✅ **前端修改**
- `user-operate-drawer.vue`: userId字段从number改为string，使用NInput代替NInputNumber
- `api/system-manage.d.ts`: User和UserEdit类型定义中userId改为string

✅ **后端修改**
- 已在之前完成：所有实体类、DAO、Service中userId都已改为String类型

### 需求2: 编辑用户时工号不允许修改
✅ **前端修改**
- `user-operate-drawer.vue`: 工号输入框在编辑模式(isEdit=true)时设置为disabled

### 需求3: 移除密码展示，增加初始化密码功能
✅ **前端修改**
- `user-operate-drawer.vue`: 编辑模式下不显示密码字段（v-if="!isEdit"）
- `user/index.vue`: 
  - 操作列添加"初始化密码"按钮
  - 工具栏添加"批量初始化密码"按钮
  - 导入fetchResetPassword API
  - 实现handleResetPassword和handleBatchResetPassword方法

✅ **API修改**
- `system-manage.ts`: 添加fetchResetPassword方法

✅ **后端修改**
- `UserManageController.java`: 添加resetPassword接口
- `UserManageService.java`: 添加resetPassword方法声明
- `UserManageServiceImpl.java`: 实现resetPassword方法（默认密码123456）
- `UserDao.java`: 添加resetPassword方法
- `UserMapper.xml`: 添加批量重置密码的SQL

### 需求4: 登录页增加修改密码功能
✅ **前端修改**
- 创建`change-pwd.vue`: 修改密码组件
  - 包含用户账号、旧密码、新密码、确认密码字段
  - 新密码验证：必须包含字母和数字，至少6位
  - 确认密码验证：必须与新密码一致
- `login/index.vue`: 添加change-pwd模块
- `pwd-login.vue`: 添加"修改密码"链接
- `constants/app.ts`: 添加change-pwd到loginModuleRecord
- `union-key.d.ts`: 添加'change-pwd'到LoginModule类型

✅ **API修改**
- `auth.ts`: 添加fetchChangePassword方法

✅ **后端修改**
- 创建`ChangePasswordRequest.java`: 修改密码请求VO
  - userAccount: 用户账号（必填）
  - oldPassword: 旧密码（必填）
  - newPassword: 新密码（必填，正则验证）
- `LoginController.java`: 添加changePassword接口
- `AuthService.java`: 添加changePassword方法声明
- `AuthServiceImpl.java`: 实现changePassword方法
- `UserDao.java`: 添加updatePassword方法
- `UserMapper.xml`: 添加更新密码的SQL

## 文件修改清单

### 前端文件 (9个)
1. `/oasis-web/src/views/manage/user/modules/user-operate-drawer.vue`
2. `/oasis-web/src/views/manage/user/index.vue`
3. `/oasis-web/src/views/_builtin/login/modules/change-pwd.vue` (新建)
4. `/oasis-web/src/views/_builtin/login/modules/pwd-login.vue`
5. `/oasis-web/src/views/_builtin/login/index.vue`
6. `/oasis-web/src/service/api/system-manage.ts`
7. `/oasis-web/src/service/api/auth.ts`
8. `/oasis-web/src/constants/app.ts`
9. `/oasis-web/src/typings/union-key.d.ts`
10. `/oasis-web/src/typings/api/system-manage.d.ts`

### 后端文件 (8个)
1. `/oasis-admin/src/main/java/com/github/kevin/oasis/controller/UserManageController.java`
2. `/oasis-admin/src/main/java/com/github/kevin/oasis/controller/LoginController.java`
3. `/oasis-admin/src/main/java/com/github/kevin/oasis/models/vo/oauth/ChangePasswordRequest.java` (新建)
4. `/oasis-admin/src/main/java/com/github/kevin/oasis/services/UserManageService.java`
5. `/oasis-admin/src/main/java/com/github/kevin/oasis/services/AuthService.java`
6. `/oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/UserManageServiceImpl.java`
7. `/oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/AuthServiceImpl.java`
8. `/oasis-admin/src/main/java/com/github/kevin/oasis/dao/UserDao.java`
9. `/oasis-admin/src/main/resources/mapper/UserMapper.xml`

**总计**: 18个文件修改，2个新建文件

## 核心功能说明

### 1. 用户工号
- **类型**: String（字符串）
- **前端输入**: NInput组件
- **编辑限制**: 编辑时不可修改
- **示例**: "EMP001", "2024001"

### 2. 初始化密码
- **默认密码**: 123456
- **支持批量**: 是
- **权限要求**: 需要登录且有权限
- **操作位置**: 
  - 单个：用户列表每行的操作按钮
  - 批量：列表顶部工具栏

### 3. 修改密码
- **入口**: 登录页密码登录界面
- **验证规则**:
  - 用户账号和旧密码必须正确
  - 新密码必须包含字母和数字
  - 新密码长度至少6位
  - 确认密码必须与新密码一致
- **成功后**: 自动跳转回登录页

## API接口说明

### 重置密码接口
```
POST /systemManage/user/resetPassword
权限: @Permission
请求体: { ids: number[] }
响应: Response<Integer>
功能: 将指定用户的密码重置为123456
```

### 修改密码接口
```
POST /auth/changePassword
权限: 无需登录
请求体: {
  userAccount: string,
  oldPassword: string,
  newPassword: string
}
响应: Response<Boolean>
功能: 验证旧密码后更新为新密码
```

## 数据库变更

### UserMapper.xml 新增SQL

#### 批量重置密码
```xml
<update id="resetPassword">
    UPDATE user
    SET password = #{password},
        update_time = NOW()
    WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</update>
```

#### 修改密码
```xml
<update id="updatePassword">
    UPDATE user
    SET password = #{newPassword},
        update_time = NOW()
    WHERE user_account = #{userAccount}
      AND password = #{oldPassword}
</update>
```

## 密码规则

### 新密码要求
- **最小长度**: 6位
- **必须包含**: 字母和数字
- **允许字符**: 字母、数字、特殊字符(@$!%*?&)
- **正则表达式**: `^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{6,}$`

### 示例
- ✅ 合法: "abc123", "Test2024", "Admin@123"
- ❌ 非法: "123456"(无字母), "abcdef"(无数字), "abc12"(少于6位)

## 用户界面变更

### 用户管理页面
**变更前**:
- 工号：数字输入框
- 密码：编辑时显示，可修改
- 操作：编辑、启用/禁用、删除

**变更后**:
- 工号：文本输入框，编辑时禁用
- 密码：仅新增时显示
- 操作：编辑、初始化密码、启用/禁用、删除
- 批量操作：批量切换状态、批量初始化密码、批量删除

### 登录页面
**新增功能**:
- "修改密码"链接（在"记住我"旁边）
- 修改密码表单页面

## 测试建议

### 功能测试
1. **用户管理**
   - [ ] 新增用户，输入字符串工号
   - [ ] 编辑用户，验证工号不可修改
   - [ ] 编辑用户时密码字段不显示
   - [ ] 单个用户初始化密码
   - [ ] 批量初始化密码
   - [ ] 使用默认密码123456登录

2. **修改密码**
   - [ ] 输入错误的旧密码
   - [ ] 输入不符合规则的新密码
   - [ ] 确认密码与新密码不一致
   - [ ] 成功修改密码
   - [ ] 使用新密码登录

### 边界测试
- [ ] 工号输入特殊字符
- [ ] 工号输入超长字符串
- [ ] 批量操作0个用户
- [ ] 批量操作大量用户（性能）
- [ ] 新密码包含各种特殊字符

### 安全测试
- [ ] 修改密码时SQL注入测试
- [ ] 密码复杂度验证
- [ ] 旧密码验证是否严格

## 注意事项

1. **密码安全**
   - ⚠️ 当前密码未加密存储（标注TODO）
   - 建议后续使用BCrypt等加密算法

2. **默认密码**
   - 初始化密码固定为123456
   - 建议首次登录强制修改密码

3. **数据迁移**
   - 确保所有用户都有工号
   - 工号字段已改为VARCHAR(50)

4. **前后端类型一致**
   - 前端userId: string
   - 后端userId: String
   - 数据库user_id: VARCHAR(50)

## 完成状态

✅ 所有前端代码已修改
✅ 所有后端代码已修改
✅ 所有API已添加
✅ 所有类型定义已更新
✅ 数据库Mapper已更新

**状态**: 全部完成，可以进行测试

**下一步**: 
1. 启动前后端服务
2. 执行功能测试
3. 验证所有修改点

---

*修改完成时间: 2026-02-28*
*修改人: GitHub Copilot*

