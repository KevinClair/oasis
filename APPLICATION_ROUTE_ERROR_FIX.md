# 应用管理路由错误修复

## 问题描述
打开应用管理界面时出现错误：
```
Error transforming route "application": Error: Layout component "view.application" not found
```

## 问题原因
组件路径配置错误。应用管理是一级菜单（顶级路由），需要使用 `layout.base$view.application` 而不是 `view.application`。

在 elegant-router 的路由系统中：
- **顶级路由**（一级菜单）：需要包含 layout，格式为 `layout.base$view.xxx`
- **子路由**（二级菜单）：不需要 layout，格式为 `view.xxx`

## 修复内容

### 1. SQL 文件修复
已修复以下文件使用正确的组件路径：

#### `init_application_menu.sql`
```sql
component: 'layout.base$view.application'  -- ✅ 正确
```

#### `update_application_menu.sql`
```sql
UPDATE `menu`
SET
    component = 'layout.base$view.application'
WHERE `route_name` = 'manage_application'
   OR `i18n_key` = 'route.manage_application'
   OR `route_name` = 'application';
```

### 2. 前端路由配置
`routes.ts` 中的配置已经正确：
```typescript
{
  name: 'application',
  path: '/application',
  component: 'layout.base$view.application',  // ✅ 正确
  meta: {
    title: 'application',
    i18nKey: 'route.application',
    icon: 'mdi:application-cog',
    order: 2
  }
}
```

## 执行更新

请立即执行以下 SQL 更新数据库中的菜单配置：

```sql
UPDATE `menu`
SET
    `route_name` = 'application',
    `route_path` = '/application',
    `component` = 'layout.base$view.application',
    `i18n_key` = 'route.application',
    `update_by` = 'system',
    `update_time` = NOW()
WHERE `route_name` = 'manage_application'
   OR `i18n_key` = 'route.manage_application'
   OR `route_name` = 'application';
```

## 验证步骤

执行 SQL 后：
1. **清除浏览器缓存**或使用无痕模式
2. **重新登录系统**
3. **点击应用管理菜单**
4. 应该能正常打开，不再出现 "Layout component not found" 错误

## 组件路径对照表

| 路由类型 | Parent ID | Component 格式 | 示例 |
|---------|-----------|---------------|------|
| 一级菜单 | 0 | `layout.base$view.xxx` | `layout.base$view.application` |
| 二级菜单 | >0 | `view.xxx` | `view.manage_user` |
| 空白页面 | - | `layout.blank$view.xxx` | `layout.blank$view.login` |

## 相关文件

- ✅ `/oasis-admin/src/main/resources/sql/init_application_menu.sql`
- ✅ `/oasis-admin/src/main/resources/sql/update_application_menu.sql`
- ✅ `/oasis-web/src/router/elegant/routes.ts`
- ✅ `/oasis-web/src/views/application/index.vue`

## 修复时间
2026-03-01

