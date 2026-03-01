# 应用管理菜单修复报告

## 问题描述

1. **菜单显示问题**：在系统管理 > 菜单管理中，应用管理菜单显示的是 `route.application` 而不是翻译后的"应用管理"
2. **路由错误**：点击应用管理菜单时页面报错，无法正常打开

## 问题原因

初始化SQL脚本 `init_application_menu.sql` 中的配置与实际路由配置不匹配：

### 错误的配置
```sql
route_name: 'application'          -- 应该是 'manage_application'
route_path: '/application'         -- 应该是 '/manage/application'
component: 'layout.base$view.manage_application'  -- 应该是 'view.manage_application'
i18n_key: 'route.application'      -- 应该是 'route.manage_application'
```

### 正确的配置（参考 routes.ts）
```typescript
name: 'manage_application',
path: '/manage/application',
component: 'view.manage_application',
meta: {
  title: 'manage_application',
  i18nKey: 'route.manage_application'
}
```

## 修复方案

### 1. 修复了初始化SQL文件
文件：`oasis-admin/src/main/resources/sql/init_application_menu.sql`

修改内容：
- `route_name`: `'application'` → `'manage_application'`
- `route_path`: `'/application'` → `'/manage/application'`
- `component`: `'layout.base$view.manage_application'` → `'view.manage_application'`
- `i18n_key`: `'route.application'` → `'route.manage_application'`

### 2. 创建了更新SQL脚本
文件：`oasis-admin/src/main/resources/sql/update_application_menu.sql`

此脚本用于修复已经插入到数据库中的错误数据。

### 3. 国际化配置验证

验证了国际化文件已经包含正确的配置：

**zh-cn.ts**:
```typescript
route: {
  manage_application: '应用管理'
}
```

**en-us.ts**:
```typescript
route: {
  manage_application: 'Application Manage'
}
```

**app.d.ts**:
类型定义通过 `Record<I18nRouteKey, string>` 自动包含了所有路由的国际化key，无需手动添加。

## 执行步骤

1. ✅ 已修复初始化SQL文件
2. ✅ 已创建更新SQL脚本
3. ⏳ 需要执行更新SQL脚本来修复数据库中的现有数据

请执行以下SQL来修复数据库：
```sql
UPDATE `menu`
SET
    `route_name` = 'manage_application',
    `route_path` = '/manage/application',
    `component` = 'view.manage_application',
    `i18n_key` = 'route.manage_application',
    `update_by` = 'system',
    `update_time` = NOW()
WHERE `route_name` = 'application'
   OR `i18n_key` = 'route.application';
```

## 验证方法

执行SQL后：
1. 刷新浏览器页面
2. 检查菜单管理界面，应用管理的国际化key应该正确显示为 `route.manage_application`
3. 切换语言：
   - 中文：应显示"应用管理"
   - 英文：应显示"Application Manage"
4. 点击应用管理菜单，应能正常打开应用管理页面

## 技术细节

### 动态路由组件路径说明

在动态路由模式下，组件路径格式为：
- 一级菜单（带layout）：`layout.base$view.xxx`
- 二级菜单（不带layout）：`view.xxx`

应用管理虽然是一级菜单，但它是在 `/manage` 路径下的子路由，所以使用 `view.manage_application` 而不是 `layout.base$view.manage_application`。

### 路由匹配规则

路由系统会自动匹配：
- `route_name` → Vue Router 的 `name`
- `route_path` → Vue Router 的 `path`
- `component` → 组件路径（elegant-router会自动解析）
- `i18n_key` → 国际化key（必须在 zh-cn.ts 和 en-us.ts 中定义）

## 修复完成时间

2026-03-01

