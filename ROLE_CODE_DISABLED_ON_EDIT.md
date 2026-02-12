# 角色管理 - 编辑时角色编码置灰不可编辑

## 修改内容

在角色管理的编辑功能中，将角色编码字段设置为禁用状态（置灰），防止在编辑角色时修改角色编码。

## 修改原因

角色编码通常作为系统中的唯一标识符，在创建后不应该被修改，以保证系统的数据一致性和安全性。因此在编辑模式下应该禁用该字段。

## 修改的文件

### role-operate-drawer.vue

**文件路径：** `oasis-web/src/views/manage/role/modules/role-operate-drawer.vue`

**修改位置：** 第 130 行

**修改前：**
```vue
<NFormItem :label="$t('page.manage.role.roleCode')" path="roleCode">
  <NInput v-model:value="model.roleCode" :placeholder="$t('page.manage.role.form.roleCode')" />
</NFormItem>
```

**修改后：**
```vue
<NFormItem :label="$t('page.manage.role.roleCode')" path="roleCode">
  <NInput v-model:value="model.roleCode" :placeholder="$t('page.manage.role.form.roleCode')" :disabled="isEdit" />
</NFormItem>
```

## 实现逻辑

利用已有的 `isEdit` 计算属性来判断当前操作类型：

```typescript
const isEdit = computed(() => props.operateType === 'edit');
```

- **新增角色**（`operateType === 'add'`）：`isEdit = false`，角色编码输入框可编辑
- **编辑角色**（`operateType === 'edit'`）：`isEdit = true`，角色编码输入框禁用（置灰）

## 效果展示

### 新增角色
- ✅ 角色名称：可编辑
- ✅ 角色编码：可编辑
- ✅ 角色状态：可选择
- ✅ 角色描述：可编辑

### 编辑角色
- ✅ 角色名称：可编辑
- 🔒 **角色编码：禁用（置灰）**
- ✅ 角色状态：可选择
- ✅ 角色描述：可编辑

## 验证方法

1. 登录系统，进入"角色管理"页面
2. 点击"新增"按钮，查看角色编码输入框是否可编辑（正常状态）
3. 关闭抽屉，选择某个角色点击"编辑"按钮
4. 查看角色编码输入框是否显示为禁用状态（置灰且不可编辑）
5. 确认其他字段仍然可以正常编辑

## 技术说明

使用 Naive UI 的 `NInput` 组件的 `:disabled` 属性来控制输入框的禁用状态。该属性绑定到 `isEdit` 计算属性，实现动态控制。

