# 菜单编辑单选框修复 - 快速参考

## 🐛 问题

菜单编辑时，**菜单类型**和**菜单状态**单选框无法正确回显选中值。

## 🔧 修复内容

### 1. 修复 menuType（菜单类型）

**文件：** `constants/business.ts`

**修改：** Record key 从字符串改为数字

```typescript
// 修改前
export const menuTypeRecord = {
  '1': 'page.manage.menu.type.directory',
  '2': 'page.manage.menu.type.menu'
};

// 修改后
export const menuTypeRecord = {
  1: 'page.manage.menu.type.directory',
  2: 'page.manage.menu.type.menu'
};
```

---

### 2. 修复 status（菜单状态）

**文件：** `menu-operate-modal.vue`

**修改：** 在 `handleInitModel` 中添加转换

```typescript
if (props.operateType === 'edit') {
  const { component, status, ...rest } = props.rowData;
  
  // Boolean → String 转换
  const convertedStatus = status ? '1' : '2';
  
  Object.assign(model.value, rest, { 
    status: convertedStatus as Api.Common.EnableStatus,
    // ...
  });
}
```

---

## 📊 转换逻辑

| 后端类型 | 后端值 | 前端值 | Radio匹配 |
|---------|--------|--------|----------|
| menuType (Integer) | 1 或 2 | 1 或 2 | ✅ |
| status (Boolean) | true/false | '1'/'2' | ✅ |

---

## ✅ 验证

1. 编辑菜单
2. 检查单选框是否正确选中
3. 修改并保存，确认数据正确

---

## 📄 详细文档

完整说明：`MENU_EDIT_RADIO_FIX.md`

