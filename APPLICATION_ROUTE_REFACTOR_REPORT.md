# 应用管理路由重构总结报告

## 修改日期
2026-03-01

## 修改目标
将应用管理从系统管理的子菜单改为与首页同级的一级菜单，路由名称从 `manage_application` 简化为 `application`。

## 修改内容

### 1. 后端 SQL 文件修改

#### 1.1 初始化 SQL (`init_application_menu.sql`)
修改字段：
- `route_name`: `'manage_application'` → `'application'`
- `route_path`: `'/manage/application'` → `'/application'`
- `component`: `'view.manage_application'` → `'view.application'`
- `i18n_key`: `'route.manage_application'` → `'route.application'`

#### 1.2 更新 SQL (`update_application_menu.sql`)
生成数据库更新语句：
```sql
UPDATE `menu`
SET
    `route_name` = 'application',
    `route_path` = '/application',
    `component` = 'view.application',
    `i18n_key` = 'route.application',
    `update_by` = 'system',
    `update_time` = NOW()
WHERE `route_name` = 'manage_application'
   OR `i18n_key` = 'route.manage_application';
```

### 2. 前端路由配置修改

#### 2.1 路由定义 (`routes.ts`)
修改内容：
- 从 `manage` 路由的 children 中移除 `manage_application`
- 添加新的顶级路由 `application`：
```typescript
{
  name: 'application',
  path: '/application',
  component: 'layout.base$view.application',
  meta: {
    title: 'application',
    i18nKey: 'route.application',
    icon: 'mdi:application-cog',
    order: 2
  }
}
```

#### 2.2 视图文件夹重组
```bash
mv src/views/manage/application src/views/application
```
将应用管理的视图文件夹从 `views/manage/application/` 移动到 `views/application/`

### 3. 国际化文件修改

#### 3.1 中文国际化 (`zh-cn.ts`)
在 `route` 对象中添加：
```typescript
application: '应用管理'
```

#### 3.2 英文国际化 (`en-us.ts`)
在 `route` 对象中添加：
```typescript
application: 'Application Manage'
```

**注意**：保留了 `manage_application` 的国际化配置以保持向后兼容。

### 4. 类型定义 (`app.d.ts`)
无需修改。类型系统通过 `Record<I18nRouteKey, string>` 自动从 routes.ts 生成，会自动包含新的 `application` 路由。

## 文件变更清单

### 修改的文件
1. `/oasis-admin/src/main/resources/sql/init_application_menu.sql`
2. `/oasis-admin/src/main/resources/sql/update_application_menu.sql`
3. `/oasis-web/src/router/elegant/routes.ts`
4. `/oasis-web/src/locales/langs/zh-cn.ts`
5. `/oasis-web/src/locales/langs/en-us.ts`

### 移动的文件夹
- `/oasis-web/src/views/manage/application/` → `/oasis-web/src/views/application/`

## 部署步骤

### 1. 数据库更新
执行更新 SQL：
```bash
mysql -u [username] -p [database_name] < oasis-admin/src/main/resources/sql/update_application_menu.sql
```

或者在数据库客户端中执行：
```sql
UPDATE `menu`
SET
    `route_name` = 'application',
    `route_path` = '/application',
    `component` = 'view.application',
    `i18n_key` = 'route.application',
    `update_by` = 'system',
    `update_time` = NOW()
WHERE `route_name` = 'manage_application'
   OR `i18n_key` = 'route.manage_application';
```

### 2. 前端部署
```bash
cd oasis-web
pnpm install  # 如果有新依赖
pnpm build    # 构建生产版本
```

### 3. 清除缓存
- 清除浏览器缓存
- 清除后端路由缓存（如果有）
- 重新登录系统

## 验证步骤

1. **数据库验证**
   ```sql
   SELECT * FROM `menu` WHERE `route_name` = 'application';
   ```
   确认记录已更新

2. **菜单显示验证**
   - 登录系统
   - 查看左侧菜单栏
   - 应用管理应该显示为与"首页"同级的一级菜单
   - 排序应该在首页之后（order: 2）

3. **国际化验证**
   - 切换到中文：应显示"应用管理"
   - 切换到英文：应显示"Application Manage"

4. **功能验证**
   - 点击应用管理菜单
   - 应该能正常打开应用管理页面（路径：/application）
   - 页面功能正常使用

5. **路由验证**
   - 直接访问 `/application` 应该能正常打开页面
   - 旧路径 `/manage/application` 应该不再有效

## 注意事项

1. **向后兼容**：国际化文件中保留了 `manage_application` 的配置，以防某些地方仍在使用

2. **图标**：使用 `mdi:application-cog` 图标，确保 iconify 库已正确加载

3. **权限**：如果有权限控制，需要同步更新权限配置中的路由名称

4. **收藏夹/书签**：用户之前收藏的 `/manage/application` 路径将失效，需要重新收藏新路径

## 影响范围

- **菜单结构**：应用管理从二级菜单变为一级菜单
- **URL 变化**：`/manage/application` → `/application`
- **组件路径**：`view.manage_application` → `layout.base$view.application`
- **国际化 key**：`route.manage_application` → `route.application`

## 回滚方案

如果需要回滚，执行以下 SQL：
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

并将前端代码回滚到之前的版本。

