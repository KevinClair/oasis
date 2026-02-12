# 内置菜单数据SQL说明文档

## 文件信息

**文件名：** `insert_builtin_menus.sql`  
**位置：** `/oasis-admin/src/main/resources/sql/`  
**用途：** 初始化系统内置页面菜单数据

## 菜单列表

### 一、常量路由（constant=1）

常量路由是系统内置路由，所有用户都可以访问，不需要权限控制。这些路由通常用于错误页面、登录页面等。

| 菜单名称 | 路由名称 | 路由路径 | 组件 | 是否隐藏 | 说明 |
|---------|---------|---------|------|---------|------|
| 403 | 403 | /403 | layout.base$view.403 | 是 | 无权限页面 |
| 404 | 404 | /404 | layout.base$view.404 | 是 | 页面不存在 |
| 500 | 500 | /500 | layout.base$view.500 | 是 | 服务器错误 |
| 登录 | login | /login | layout.blank$view.login | 是 | 登录页面 |
| iframe页面 | iframe-page | /iframe-page/:url | layout.base$view.iframe-page | 是 | 内嵌页面 |

**特点：**
- `constant` = 1（常量路由）
- `hide_in_menu` = 1（在菜单中隐藏）
- 不需要配置权限，所有用户可访问
- 用于系统异常处理和用户认证

### 二、动态路由（constant=0）

动态路由需要根据用户权限动态加载，通过角色菜单关联表控制访问权限。

| 菜单名称 | 路由名称 | 路由路径 | 组件 | 父级 | 排序 | 说明 |
|---------|---------|---------|------|------|------|------|
| 首页 | home | /home | layout.base$view.home | 根 | 1 | 系统首页 |
| 系统管理 | manage | /manage | layout.base | 根 | 2 | 一级目录 |
| 用户管理 | manage_user | /manage/user | view.manage_user | 系统管理 | 1 | 二级菜单 |
| 角色管理 | manage_role | /manage/role | view.manage_role | 系统管理 | 2 | 二级菜单 |
| 菜单管理 | manage_menu | /manage/menu | view.manage_menu | 系统管理 | 3 | 二级菜单 |

**特点：**
- `constant` = 0（动态路由）
- `hide_in_menu` = 0（在菜单中显示）
- 需要配置权限，通过角色菜单关联控制
- 支持多级菜单结构

## 字段说明

### 核心字段

| 字段 | 类型 | 说明 | 示例 |
|-----|------|------|------|
| parent_id | BIGINT | 父级菜单ID，0表示根菜单 | 0 |
| menu_type | TINYINT | 菜单类型：1-目录，2-菜单 | 2 |
| menu_name | VARCHAR | 菜单名称 | "首页" |
| route_name | VARCHAR | 路由名称（唯一） | "home" |
| route_path | VARCHAR | 路由路径 | "/home" |
| component | VARCHAR | 组件路径 | "layout.base$view.home" |

### 图标字段

| 字段 | 类型 | 说明 | 示例 |
|-----|------|------|------|
| icon_type | VARCHAR | 图标类型：1-iconify，2-本地 | "1" |
| icon | VARCHAR | iconify图标名称 | "mdi:home" |
| local_icon | VARCHAR | 本地图标名称 | null |

### 元信息字段

| 字段 | 类型 | 说明 | 默认值 |
|-----|------|------|--------|
| i18n_key | VARCHAR | 国际化key | "route.home" |
| order | INT | 排序（越小越靠前） | 0 |
| keep_alive | TINYINT | 是否缓存：1-是，0-否 | 0 |
| constant | TINYINT | 是否常量路由：1-是，0-否 | 0 |
| hide_in_menu | TINYINT | 是否隐藏：1-是，0-否 | 0 |
| multi_tab | TINYINT | 支持多标签：1-是，0-否 | 0 |
| status | TINYINT | 状态：1-启用，0-禁用 | 1 |

## 使用方法

### 1. 执行SQL

```bash
# 方式一：命令行执行
mysql -u username -p database_name < insert_builtin_menus.sql

# 方式二：MySQL客户端执行
mysql> source /path/to/insert_builtin_menus.sql;

# 方式三：在IDE中直接执行
```

### 2. 验证数据

执行完成后，SQL文件末尾包含了验证查询：

```sql
-- 查询所有菜单
SELECT id, menu_name, route_name, constant FROM menu;

-- 查询常量路由
SELECT menu_name, route_name FROM menu WHERE constant = 1;

-- 查询动态路由
SELECT menu_name, route_name FROM menu WHERE constant = 0;
```

### 3. 配置角色权限

动态路由需要配置角色权限才能访问：

```sql
-- 示例：为超级管理员分配所有动态路由
INSERT INTO role_menu (role_id, menu_id, create_by)
SELECT 1, id, 'system'
FROM menu
WHERE constant = 0;
```

## 组件路径说明

### 布局组件

| 组件路径 | 说明 |
|---------|------|
| layout.base | 基础布局（包含侧边栏、顶栏） |
| layout.blank | 空白布局（无侧边栏、顶栏） |

### 页面组件

| 组件路径 | 说明 |
|---------|------|
| layout.base$view.home | 基础布局 + 首页视图 |
| layout.base$view.403 | 基础布局 + 403视图 |
| view.manage_user | 用户管理视图（自动使用父级布局） |

### 命名规则

- `layout.xxx` - 布局组件
- `view.xxx` - 视图组件
- `$` - 分隔符，表示布局+视图的组合
- `_` - 下划线表示路由层级（如 manage_user）

## 图标选择

### Iconify 图标

系统使用 iconify 图标库，可以从以下网站选择：
- https://icon-sets.iconify.design/
- https://icones.js.org/

常用图标集：
- `mdi:` - Material Design Icons
- `ic:` - Material Icons
- `carbon:` - Carbon Design System
- `material-symbols:` - Material Symbols

### 示例

```sql
-- Material Design Icons
icon = 'mdi:home'
icon = 'mdi:account'

-- Material Icons
icon = 'ic:round-manage-accounts'
icon = 'ic:baseline-block'

-- Carbon Design
icon = 'carbon:user-role'
icon = 'carbon:cloud-service-management'
```

## 注意事项

### 1. 路由名称唯一性

确保 `route_name` 字段唯一，否则会导致路由冲突。

### 2. 常量路由 vs 动态路由

- **常量路由**：系统内置，所有用户可访问，通常用于错误页面、登录页面
- **动态路由**：需要权限控制，通过角色菜单关联表配置访问权限

### 3. 菜单类型

- **类型1（目录）**：只用于分组，不对应具体页面
- **类型2（菜单）**：对应具体页面，可点击访问

### 4. parent_id 设置

- `parent_id = 0`：根菜单
- `parent_id = xxx`：子菜单，xxx为父菜单的ID

使用 `LAST_INSERT_ID()` 获取刚插入的父菜单ID：

```sql
INSERT INTO menu (...) VALUES (...);
SET @parent_id = LAST_INSERT_ID();
INSERT INTO menu (parent_id, ...) VALUES (@parent_id, ...);
```

### 5. 清空数据

如果需要重新初始化，可以使用：

```sql
-- 注意：这会删除所有菜单数据，谨慎使用！
TRUNCATE TABLE menu;
```

## 扩展建议

### 1. 添加更多页面

参考现有格式添加新页面：

```sql
INSERT INTO menu (
    parent_id, menu_type, menu_name, route_name, route_path, 
    component, icon_type, icon, i18n_key, order, 
    keep_alive, constant, hide_in_menu, status, create_by
) VALUES (
    0, 2, '关于', 'about', '/about',
    'layout.base$view.about', '1', 'mdi:information', 'route.about', 10,
    0, 0, 0, 1, 'system'
);
```

### 2. 配置外部链接

```sql
INSERT INTO menu (
    parent_id, menu_type, menu_name, route_name, route_path, 
    href, icon_type, icon, i18n_key, order, 
    hide_in_menu, status, create_by
) VALUES (
    0, 2, '文档', 'doc', '/doc',
    'https://docs.example.com', '1', 'mdi:book', 'route.doc', 20,
    0, 1, 'system'
);
```

### 3. 配置多级菜单

```sql
-- 一级目录
INSERT INTO menu (...) VALUES (...);
SET @level1_id = LAST_INSERT_ID();

-- 二级目录
INSERT INTO menu (parent_id, ...) VALUES (@level1_id, ...);
SET @level2_id = LAST_INSERT_ID();

-- 三级菜单
INSERT INTO menu (parent_id, ...) VALUES (@level2_id, ...);
```

## 测试验证

### 1. 检查数据完整性

```sql
-- 检查菜单总数
SELECT COUNT(*) FROM menu;

-- 检查常量路由数量（应该是5个）
SELECT COUNT(*) FROM menu WHERE constant = 1;

-- 检查动态路由数量（应该是5个）
SELECT COUNT(*) FROM menu WHERE constant = 0;

-- 检查是否有重复的route_name
SELECT route_name, COUNT(*) 
FROM menu 
GROUP BY route_name 
HAVING COUNT(*) > 1;
```

### 2. 测试路由访问

前端启动后，访问以下路由：
- http://localhost:5173/403
- http://localhost:5173/404
- http://localhost:5173/500
- http://localhost:5173/login
- http://localhost:5173/home（需要登录）

## 总结

本SQL文件提供了系统所需的基础菜单数据，包括：
- ✅ 5个常量路由（403、404、500、login、iframe-page）
- ✅ 5个动态路由（home、manage及其3个子菜单）
- ✅ 完整的字段配置
- ✅ 树形菜单结构
- ✅ 图标和国际化支持

执行此SQL后，系统即可正常运行动态路由功能。

