# 用户管理和密码功能 - 快速参考

## 🎯 修改内容速览

### 1️⃣ 用户工号改为字符串
- **前端**: NInput组件，编辑时禁用
- **后端**: String类型
- **示例**: "EMP001", "2024001"

### 2️⃣ 编辑用户时隐藏密码
- **新增**: 显示密码输入框
- **编辑**: 不显示密码字段

### 3️⃣ 初始化密码功能
- **默认密码**: 123456
- **单个操作**: 列表行操作按钮
- **批量操作**: 顶部工具栏按钮
- **API**: `POST /systemManage/user/resetPassword`

### 4️⃣ 修改密码功能
- **入口**: 登录页"修改密码"链接
- **验证**: 账号+旧密码验证
- **规则**: 必须包含字母和数字，≥6位
- **API**: `POST /auth/changePassword`

## 📋 新增API

| 接口 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 重置密码 | POST | /systemManage/user/resetPassword | 批量重置为123456 |
| 修改密码 | POST | /auth/changePassword | 验证旧密码后修改 |

## 🔐 密码规则

### 初始化密码
```
默认密码: 123456
```

### 修改密码要求
```
正则: ^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{6,}$
规则:
  ✓ 至少6位
  ✓ 必须包含字母
  ✓ 必须包含数字
  ✓ 可包含特殊字符: @$!%*?&
```

## 🖥️ 前端组件

### 新建文件
```
oasis-web/src/views/_builtin/login/modules/change-pwd.vue
```

### 修改文件
```
✓ user-operate-drawer.vue - 工号字符串输入，密码隐藏
✓ user/index.vue - 初始化密码按钮
✓ pwd-login.vue - 修改密码链接
✓ login/index.vue - 添加change-pwd模块
```

## 💻 后端接口

### 新建文件
```java
ChangePasswordRequest.java - 修改密码请求VO
```

### 新增方法
```java
// UserManageController
POST /systemManage/user/resetPassword

// LoginController  
POST /auth/changePassword

// UserDao
int resetPassword(List<Long> ids, String password)
int updatePassword(String userAccount, String oldPassword, String newPassword)
```

## 📝 使用示例

### 前端调用
```typescript
// 初始化密码
await fetchResetPassword([userId1, userId2])

// 修改密码
await fetchChangePassword({
  userAccount: 'admin',
  oldPassword: '123456',
  newPassword: 'Admin@123'
})
```

### 用户操作流程

#### 管理员重置用户密码
1. 进入用户管理页面
2. 选择要重置的用户
3. 点击"批量初始化密码"或单个"初始化密码"
4. 确认操作
5. 密码重置为123456

#### 用户修改密码
1. 打开登录页
2. 点击"修改密码"链接
3. 输入账号、旧密码、新密码、确认密码
4. 提交修改
5. 跳转回登录页，使用新密码登录

## ⚠️ 注意事项

1. **工号不可修改**: 编辑用户时工号字段被禁用
2. **密码未加密**: 当前存储明文，建议后续加密
3. **默认密码**: 首次登录建议强制修改
4. **类型一致**: 前后端userId都是String类型

## 🧪 测试清单

- [ ] 新增用户，输入字符串工号
- [ ] 编辑用户，验证工号不可修改
- [ ] 编辑时密码字段不显示
- [ ] 初始化单个用户密码
- [ ] 批量初始化密码
- [ ] 使用123456登录
- [ ] 修改密码功能正常
- [ ] 新密码规则验证

## 🚀 部署提示

1. 确保数据库已执行user_id字段类型变更
2. 重新编译前后端代码
3. 清除浏览器缓存
4. 测试所有功能
5. 通知用户密码功能变更

---

快速参考完成 ✨

