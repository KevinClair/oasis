# 菜单类型单选框显示问题最终修复

## 🐛 问题描述

菜单编辑时，**菜单类型**单选框无法正确显示选中的值。

## 🔍 根本原因

### JavaScript Object.entries 的行为

即使我们在 Record 中使用数字 key：
```typescript
const menuTypeRecord = {
  1: 'directory',
  2: 'menu'
};
```

当使用 `Object.entries(menuTypeRecord)` 时，JavaScript **总是将数字 key 转换为字符串**：
```typescript
Object.entries(menuTypeRecord)
// 返回: [['1', 'directory'], ['2', 'menu']]
//       ^^^ 注意：数字 1 变成了字符串 '1'
```

### 问题链条

1. **后端返回：** `menuType: 1` (Number)
2. **transformRecordToOption：** 使用 `Object.entries` → 返回 `[{value: '1', ...}, {value: '2', ...}]`
3. **Radio Options：** value 是字符串 `'1'` 和 `'2'`
4. **v-model 绑定：** `model.menuType = 1` (Number)
5. **匹配失败：** 数字 `1` ≠ 字符串 `'1'` ❌

## ✅ 最终解决方案

### 方案：手动创建 menuTypeOptions，不使用 transformRecordToOption

**文件：** `oasis-web/src/constants/business.ts`

```typescript
export const menuTypeRecord: Record<Api.SystemManage.MenuType, App.I18n.I18nKey> = {
  1: 'page.manage.menu.type.directory',
  2: 'page.manage.menu.type.menu'
};

// 手动创建选项，确保 value 是数字类型
export const menuTypeOptions: CommonType.Option<Api.SystemManage.MenuType, App.I18n.I18nKey>[] = [
  { value: 1, label: 'page.manage.menu.type.directory' },
  { value: 2, label: 'page.manage.menu.type.menu' }
];
```

### 为什么这样做？

1. **直接赋值数字：** `value: 1` 确保 value 是 `number` 类型
2. **避免 Object.entries：** 不经过会转换类型的函数
3. **类型匹配：** 数字 `1` === 数字 `1` ✅

## 📊 完整的数据流

```
后端返回
└─ menuType: 1 (Number)
    ↓
前端接收 (props.rowData)
└─ menuType: 1 (Number)
    ↓
handleInitModel
└─ model.value.menuType = 1 (Number)
    ↓
menuTypeOptions
└─ [
     { value: 1, label: '...' },  ← Number 1
     { value: 2, label: '...' }   ← Number 2
   ]
    ↓
NRadioGroup v-model
└─ 匹配成功：1 === 1 ✅
```

## 🔄 与其他字段的对比

| 字段 | 后端类型 | 前端 Options Value | 转换需求 | 解决方式 |
|------|----------|-------------------|---------|---------|
| **menuType** | Number (1/2) | Number (1/2) | ❌ 不需要 | 手动创建 options |
| **status** | Boolean | String ('1'/'2') | ✅ 需要 | handleInitModel 转换 |
| **iconType** | String ('1'/'2') | String ('1'/'2') | ❌ 不需要 | transformRecordToOption |

## ✅ 验证步骤

1. **打开菜单管理**
2. **编辑一个目录类型的菜单**
   - 应该看到"目录"单选框被选中 ✅
3. **编辑一个菜单类型的菜单**
   - 应该看到"菜单"单选框被选中 ✅
4. **修改类型并保存**
   - 保存后再次编辑，应该正确显示新类型 ✅

## 📝 技术要点

### JavaScript 类型转换规则

```javascript
// Number key 在 Object.entries 中总是变成字符串
const obj = { 1: 'a', 2: 'b' };
Object.entries(obj); // [['1', 'a'], ['2', 'b']]

// 严格相等比较
1 === '1'  // false ❌
1 === 1    // true  ✅
```

### TypeScript 类型定义

```typescript
type MenuType = 1 | 2;  // 数字字面量类型

// Options 类型必须匹配
CommonType.Option<MenuType, ...>[] 
// value 必须是 1 | 2 (Number)
```

## 🎯 总结

**问题根源：** `Object.entries` 将数字 key 转为字符串，导致类型不匹配

**解决方案：** 手动创建 `menuTypeOptions`，确保 value 是数字类型

**验证结果：**
- ✅ 菜单类型单选框正确显示
- ✅ 编辑时正确回显
- ✅ 修改后正确保存
- ✅ 无类型错误

---

**修改的文件：**
1. `oasis-web/src/constants/business.ts` - 手动创建 menuTypeOptions

**已创建文档：**
- `MENU_TYPE_RADIO_FINAL_FIX.md` (本文档)

