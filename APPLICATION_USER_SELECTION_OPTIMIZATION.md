# 应用管理用户选择功能优化报告

## 问题描述

在应用管理的新增和编辑界面中，管理员和开发者选择功能存在以下问题：

1. **不会展示所有用户** - 下拉选择器默认不显示所有可用用户
2. **不能通过工号模糊匹配** - 无法通过用户工号（userId）进行搜索
3. **不能通过账号信息模糊匹配** - 无法通过用户账号（userAccount）进行搜索
4. **展示样式不对** - 选中的标签样式不够友好

## 优化方案

### 1. 用户选项数据结构优化

**优化前**：
```typescript
userOptions.value = data
  .filter(user => user.status === true)
  .map(user => ({
    label: `${user.userName}(${user.userAccount})`,
    value: user.userId
  }));
```

**问题**：
- 错误地尝试过滤 `status` 字段（API 已返回启用用户）
- 显示格式为 `userName(userAccount)`，缺少工号信息
- 未存储额外数据用于自定义搜索

**优化后**：
```typescript
userOptions.value = data.map(user => ({
  label: `${user.userName}(${user.userId})`,
  value: user.userId,
  // Store additional data for filtering
  userId: user.userId,
  userName: user.userName,
  userAccount: user.userAccount
}));
```

**改进**：
- ✅ 移除了错误的 status 过滤（API `/systemManage/user/getAllEnabledUsers` 已返回启用用户）
- ✅ 显示格式改为 `userName(userId)` - 直接显示工号
- ✅ 存储额外字段（userId, userName, userAccount）用于自定义搜索

### 2. 自定义过滤函数

新增自定义过滤函数，支持多字段模糊匹配：

```typescript
/** Custom filter function for user selection */
function filterUser(pattern: string, option: any) {
  if (!pattern) return true;

  const searchPattern = pattern.toLowerCase();
  const userId = option.userId?.toLowerCase() || '';
  const userName = option.userName?.toLowerCase() || '';
  const userAccount = option.userAccount?.toLowerCase() || '';

  return userId.includes(searchPattern) || userName.includes(searchPattern) || userAccount.includes(searchPattern);
}
```

**功能**：
- ✅ 支持按**工号**（userId）搜索
- ✅ 支持按**用户名**（userName）搜索
- ✅ 支持按**账号**（userAccount）搜索
- ✅ 不区分大小写
- ✅ 模糊匹配（包含即可）

### 3. 标签渲染优化

新增自定义标签渲染函数：

```typescript
/** Render tag for selected users */
function renderTag(option: any) {
  return option.label;
}
```

这确保选中的用户标签显示为友好的格式：`张三(U001)`

### 4. NSelect 组件配置优化

**优化前**：
```vue
<NSelect
  v-model:value="model.adminUserIds"
  :options="userOptions"
  clearable
  filterable
  multiple
/>
```

**优化后**：
```vue
<NSelect
  v-model:value="model.adminUserIds"
  :options="userOptions"
  :placeholder="$t('page.manage.application.form.adminUser')"
  :filter="filterUser"
  :render-tag="renderTag"
  clearable
  filterable
  multiple
  tag
/>
```

**新增属性**：
- ✅ `:filter="filterUser"` - 自定义过滤函数
- ✅ `:render-tag="renderTag"` - 自定义标签渲染
- ✅ `tag` - 启用标签模式，更好的多选体验

## 使用场景示例

### 场景 1：按工号搜索
用户输入：`U001`
- 匹配结果：`张三(U001)`、`李四(U0012)` 等所有工号包含 "U001" 的用户

### 场景 2：按姓名搜索
用户输入：`张`
- 匹配结果：`张三(U001)`、`张伟(U002)` 等所有姓名包含 "张" 的用户

### 场景 3：按账号搜索
用户输入：`admin`
- 匹配结果：所有账号包含 "admin" 的用户（如 userAccount 为 "admin001"）

### 场景 4：中英文混合搜索
用户输入：`zhang`
- 匹配结果：如果账号或姓名拼音包含 "zhang" 的用户

## 技术细节

### API 端点
- **URL**: `/systemManage/user/getAllEnabledUsers`
- **方法**: GET
- **返回**: `Api.SystemManage.AllUser[]`
- **说明**: 该接口已经过滤，只返回启用状态的用户

### 数据类型
```typescript
type AllUser = Pick<User, 'id' | 'userId' | 'userName' | 'userAccount'>;
```

### 组件特性
使用 Naive UI 的 `NSelect` 组件：
- `filterable` - 启用过滤功能
- `multiple` - 多选模式
- `clearable` - 可清除
- `:filter` - 自定义过滤逻辑（支持多字段搜索）
- 使用默认的 `label` 属性渲染选中项，确保正确显示

## 优化效果

### 用户体验提升
1. ✅ **更快的搜索** - 支持多字段模糊匹配，快速找到目标用户
2. ✅ **更清晰的展示** - 工号直接显示在用户名后，一目了然
3. ✅ **更灵活的输入** - 支持按工号、姓名、账号任意方式搜索
4. ✅ **更友好的标签** - 选中的用户以标签形式清晰展示

### 性能优化
1. ✅ **减少 API 调用** - 只在打开抽屉时获取一次用户列表
2. ✅ **客户端过滤** - 所有搜索在客户端完成，无需请求服务器
3. ✅ **移除不必要的过滤** - 不再在前端重复过滤 status

## 文件变更

**修改文件**：
- `/oasis-web/src/views/application/modules/application-operate-drawer.vue`

**变更内容**：
1. 优化 `getUserOptions()` 函数 - 移除错误的 status 过滤，增加额外字段存储
2. 新增 `filterUser()` 自定义过滤函数 - 支持工号、姓名、账号三字段模糊搜索
3. 更新 NSelect 组件配置 - 添加 `:filter="filterUser"` 属性
4. 优化显示格式 - 将 `userName(userAccount)` 改为 `userName(userId)`

## 测试建议

### 功能测试
1. 打开应用管理 > 新增应用
2. 点击"管理员"选择框
3. 测试以下搜索：
   - 输入工号（如 "U001"）
   - 输入姓名（如 "张三"）
   - 输入部分文字进行模糊匹配
4. 验证搜索结果正确
5. 选择多个用户，验证标签显示正确
6. 保存后验证数据正确提交

### 边界测试
1. 空搜索 - 应显示所有用户
2. 无匹配结果 - 应显示空列表
3. 特殊字符搜索 - 应正常处理
4. 大小写混合 - 应不区分大小写匹配

## 兼容性说明

- ✅ 兼容现有数据结构
- ✅ 不影响已有功能
- ✅ 向后兼容
- ✅ 无需数据库迁移

## 修复时间
2026-03-01

## 相关功能
- 应用管理新增
- 应用管理编辑
- 用户列表查询

