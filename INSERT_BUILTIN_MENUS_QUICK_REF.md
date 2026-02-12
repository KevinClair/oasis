# 菜单数据SQL - 快速参考

## ✅ 已创建文件

- **SQL文件：** `oasis-admin/src/main/resources/sql/insert_builtin_menus.sql`
- **说明文档：** `INSERT_BUILTIN_MENUS_README.md`

## 📋 菜单清单

### 常量路由（5个）- constant=1

所有用户可访问，无需权限配置

| 路由名称 | 路径 | 说明 |
|---------|------|------|
| 403 | /403 | 无权限页面 |
| 404 | /404 | 页面不存在 |
| 500 | /500 | 服务器错误 |
| login | /login | 登录页面 |
| iframe-page | /iframe-page/:url | iframe内嵌页面 |

### 动态路由（5个）- constant=0

需要权限控制，通过角色菜单关联配置

| 路由名称 | 路径 | 层级 | 说明 |
|---------|------|------|------|
| home | /home | 1级 | 首页 |
| manage | /manage | 1级 | 系统管理目录 |
| manage_user | /manage/user | 2级 | 用户管理 |
| manage_role | /manage/role | 2级 | 角色管理 |
| manage_menu | /manage/menu | 2级 | 菜单管理 |

## 🚀 使用步骤

### 1. 执行SQL

```bash
cd oasis-admin/src/main/resources/sql
mysql -u root -p oasis < insert_builtin_menus.sql
```

### 2. 验证数据

```sql
-- 查看所有菜单
SELECT menu_name, route_name, constant FROM menu;

-- 查看常量路由
SELECT menu_name, route_name FROM menu WHERE constant = 1;

-- 查看动态路由
SELECT menu_name, route_name FROM menu WHERE constant = 0;
```

### 3. 配置角色权限（重要！）

动态路由需要分配给角色才能访问：

```sql
-- 示例：为超级管理员（ID=1）分配所有动态路由
INSERT INTO role_menu (role_id, menu_id, create_by)
SELECT 1, id, 'system'
FROM menu
WHERE constant = 0 AND status = 1;
```

### 4. 测试访问

启动前端后访问：
- `/403` - 应该能直接访问（常量路由）
- `/404` - 应该能直接访问（常量路由）
- `/500` - 应该能直接访问（常量路由）
- `/login` - 应该能直接访问（常量路由）
- `/home` - 需要登录且有权限（动态路由）

## 📊 数据结构

### 常量路由特征
```sql
constant = 1        -- 标记为常量路由
hide_in_menu = 1   -- 在菜单中隐藏
status = 1         -- 启用状态
```

### 动态路由特征
```sql
constant = 0        -- 标记为动态路由
hide_in_menu = 0   -- 在菜单中显示
status = 1         -- 启用状态
keep_alive = 1     -- 开启缓存（可选）
```

## 🎨 图标示例

系统使用 iconify 图标：

```sql
-- Material Design Icons
'mdi:home'           -- 首页
'mdi:login'          -- 登录
'mdi:account'        -- 账户

-- Material Icons  
'ic:baseline-block'  -- 禁止
'ic:baseline-error'  -- 错误

-- Carbon Design
'carbon:user-role'   -- 角色
'carbon:cloud-service-management'  -- 管理
```

查找图标：https://icones.js.org/

## ⚠️ 注意事项

1. **route_name 必须唯一** - 避免路由冲突
2. **parent_id 正确设置** - 确保菜单层级关系
3. **动态路由需要权限** - 记得配置 role_menu 关联
4. **常量路由自动可访问** - 无需配置权限

## 🔧 常见问题

### Q: 为什么访问动态路由提示403？
A: 需要为用户角色配置菜单权限，在 role_menu 表中添加关联。

### Q: 如何添加新菜单？
A: 参考 SQL 文件中的格式，复制一条记录并修改相应字段。

### Q: 菜单图标不显示？
A: 检查 icon 字段格式是否正确，访问 icones.js.org 查找正确的图标名称。

### Q: 如何修改首页？
A: 修改路由响应中的 home 字段，或在菜单中将其他路由设为首页。

## 📝 完整示例

### 添加新的动态菜单

```sql
-- 添加一个"关于"页面
INSERT INTO menu (
    parent_id, menu_type, menu_name, route_name, route_path, 
    component, icon_type, icon, i18n_key, `order`, 
    keep_alive, constant, hide_in_menu, status, create_by
) VALUES (
    0, 2, '关于', 'about', '/about',
    'layout.base$view.about', '1', 'mdi:information', 'route.about', 10,
    0, 0, 0, 1, 'system'
);

-- 为角色分配此菜单
INSERT INTO role_menu (role_id, menu_id, create_by)
VALUES (1, LAST_INSERT_ID(), 'system');
```

## 📚 相关文档

- 详细说明：`INSERT_BUILTIN_MENUS_README.md`
- 动态路由实现：`DYNAMIC_ROUTE_IMPLEMENTATION.md`
- 角色菜单权限：`ROLE_MENU_PERMISSION_IMPLEMENTATION.md`

## 版本

- 创建日期：2026-02-12
- 状态：✅ 可用

