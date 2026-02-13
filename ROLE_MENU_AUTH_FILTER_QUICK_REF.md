# 角色菜单授权优化 - 快速参考

## 一句话总结
角色菜单授权界面现在只显示启用的非常量路由（业务菜单），不再显示常量路由和禁用的菜单。

## 修改文件

### 前端
1. ✅ `oasis-web/src/service/api/system-manage.ts` - 添加查询参数支持
2. ✅ `oasis-web/src/views/manage/role/modules/menu-auth-modal.vue` - 传入筛选参数

### 后端
- 无需修改（已有筛选功能支持）

## 核心改动

### API 调用变化

**修改前：**
```typescript
await fetchGetMenuList()
```

**修改后：**
```typescript
await fetchGetMenuList({ constant: false, status: true })
```

## 参数说明

| 参数 | 值 | 说明 |
|------|-----|------|
| constant | false | 只查询动态路由（排除常量路由） |
| status | true | 只查询启用的菜单（排除禁用菜单） |

## 效果对比

### 修改前
- 显示所有菜单（包括登录页、404等）
- 显示已禁用的菜单

### 修改后
- ✅ 只显示业务菜单
- ✅ 只显示启用的菜单
- ❌ 不显示常量路由（登录、404、403等）
- ❌ 不显示禁用的菜单

## 测试方法

1. 进入"角色管理"
2. 点击任意角色的"菜单授权"
3. 验证菜单树中只有业务菜单且都是启用状态

## 依赖
- 依赖后端 `/systemManage/menu/getMenuList` 接口的筛选参数支持
- 相关文档：`MENU_LIST_FILTER_IMPLEMENTATION.md`

