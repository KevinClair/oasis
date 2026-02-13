# 菜单管理编辑界面单选框对应问题修复

## 🐛 问题描述

在系统管理的菜单管理界面中，编辑菜单时，**菜单类型**和**菜单状态**的单选框无法正确对应选中的值。

## 🔍 问题原因分析

### 数据类型不匹配

1. **菜单类型（menuType）不匹配：**
   - **后端返回：** `Integer` 类型（1 或 2）
   - **前端Record定义：** 使用字符串字面量 `'1'` 和 `'2'` 作为 key
   - **Radio Options：** `transformRecordToOption` 将字符串 key 转为 value（`'1'` 或 `'2'`）
   - **Model类型：** 定义为 `Api.SystemManage.MenuType`（即 `1 | 2` 数字类型）
   - **冲突：** 后端数字 1 无法匹配前端选项值 `'1'`（字符串）

2. **菜单状态（status）不匹配：**
   - **后端返回：** `Boolean` 类型（true 或 false）
   - **前端Radio Options：** `'1'` 或 `'2'`（字符串）
   - **冲突：** 后端 `true` 无法匹配前端选项值 `'1'`

## ✅ 解决方案

### 1. 修复 menuType 问题

**修改文件：** `oasis-web/src/constants/business.ts`

**修改前：**
```typescript
export const menuTypeRecord: Record<Api.SystemManage.MenuType, App.I18n.I18nKey> = {
  '1': 'page.manage.menu.type.directory',  // 字符串 key
  '2': 'page.manage.menu.type.menu'
};
```

**修改后：**
```typescript
export const menuTypeRecord: Record<Api.SystemManage.MenuType, App.I18n.I18nKey> = {
  1: 'page.manage.menu.type.directory',  // 数字 key
  2: 'page.manage.menu.type.menu'
};
```

**效果：**
- `menuTypeOptions` 的 value 现在是数字 `1` 和 `2`
- 与后端返回的 `Integer` 类型完全匹配
- Radio 选择器可以正确回显

---

### 2. 修复 status 问题

**修改文件：** `oasis-web/src/views/manage/menu/modules/menu-operate-modal.vue`

**在 `handleInitModel` 函数中添加类型转换：**

```typescript
if (props.operateType === 'edit') {
  const { component, status, ...rest } = props.rowData;

  const { layout, page } = getLayoutAndPage(component);
  const { path, param } = getPathParamFromRoutePath(rest.routePath);

  // Convert backend Boolean status to string for radio options
  // status: boolean (true/false) -> string ('1' or '2')
  const convertedStatus = status ? '1' : '2';

  Object.assign(model.value, rest, { 
    status: convertedStatus as Api.Common.EnableStatus,
    layout, 
    page, 
    routePath: path, 
    pathParam: param 
    });
}
```

**转换逻辑：**
- `true` → `'1'`（启用）
- `false` → `'2'`（禁用）

---

## 📊 修改前后对比

### MenuType（菜单类型）

| 项目 | 修改前 | 修改后 |
|------|--------|--------|
| 后端返回 | `1` (Integer) | `1` (Integer) |
| Record Key | `'1'` (String) | `1` (Number) |
| Options Value | `'1'` (String) | `1` (Number) |
| Radio 匹配 | ❌ 不匹配 | ✅ 匹配 |

### Status（菜单状态）

| 项目 | 修改前 | 修改后 |
|------|--------|--------|
| 后端返回 | `true` (Boolean) | `true` (Boolean) |
| Options Value | `'1'` (String) | `'1'` (String) |
| 转换逻辑 | ❌ 无转换 | ✅ `true → '1'`, `false → '2'` |
| Radio 匹配 | ❌ 不匹配 | ✅ 匹配 |

---

## 🎯 测试验证

### 测试步骤：

1. **打开菜单管理页面**
2. **编辑一个已有菜单**
3. **检查菜单类型单选框**
   - ✅ 应该正确选中"目录"或"菜单"
4. **检查菜单状态单选框**
   - ✅ 应该正确选中"启用"或"禁用"
5. **修改选项并保存**
   - ✅ 保存后应该正确反映到数据库

### 预期结果：

- ✅ 菜单类型单选框正确回显
- ✅ 菜单状态单选框正确回显
- ✅ 修改后正确保存

---

## 📝 相关代码位置

### 修改的文件：

1. **constants/business.ts** 
   - 修改 `menuTypeRecord` 使用数字 key

2. **views/manage/menu/modules/menu-operate-modal.vue**
   - 在 `handleInitModel` 函数中添加 status 转换逻辑

### 关键函数：

```typescript
// menu-operate-modal.vue
function handleInitModel() {
  // ...
  if (props.operateType === 'edit') {
    const { component, status, ...rest } = props.rowData;
    
    // 转换 status: Boolean -> String
    const convertedStatus = status ? '1' : '2';
    
    Object.assign(model.value, rest, { 
      status: convertedStatus as Api.Common.EnableStatus,
      // ...
    });
  }
  // ...
}
```

---

## 🔄 数据流程

### 编辑菜单时的数据流：

```
后端返回
├─ menuType: 1 (Integer)
├─ status: true (Boolean)
└─ 其他字段...
    ↓
handleInitModel 处理
├─ menuType: 1 (保持数字) ✅
├─ status: '1' (Boolean → String) ✅
└─ 赋值给 model.value
    ↓
NRadioGroup 渲染
├─ menuTypeOptions: [{value: 1, ...}, {value: 2, ...}]
├─ enableStatusOptions: [{value: '1', ...}, {value: '2', ...}]
└─ v-model 匹配成功 ✅
```

---

## ⚠️ 注��事项

1. **提交时的转换：** 
   - `getSubmitParams` 函数中已有 `status: params.status === '1'` 的逆向转换
   - 确保提交到后端时是 Boolean 类型

2. **类型一致性：**
   - `menuType` 在整个流程中都使用数字类型
   - `status` 在前端使用字符串，提交时转回 Boolean

3. **其他类似字段：**
   - `iconType` 也使用字符串 `'1'` 或 `'2'`，无需修改（后端也是字符串）

---

## ✅ 修复完成

- ✅ 菜单类型单选框问题已修复
- ✅ 菜单状态单选框问题已修复
- ✅ 代码编译通过
- ✅ 无类型错误

**建议测试编辑功能确保一切正常！**

