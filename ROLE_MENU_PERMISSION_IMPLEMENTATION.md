# 角色菜单权限功能实现文档

## 功能概述

完整实现了系统管理-角色管理页面中的菜单权限功能，通过数据库关联表展示角色和菜单之间的多对多关系。

## 实现内容

### 一、数据库设计

#### 1. 创建角色菜单关联表

**文件：** `/oasis-admin/src/main/resources/sql/create_role_menu_table.sql`

```sql
CREATE TABLE `role_menu` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT(20) NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT(20) NOT NULL COMMENT '菜单ID',
  `create_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';
```

**特点：**
- 使用联合唯一索引 `uk_role_menu` 防止重复关联
- 为role_id和menu_id建立索引提升查询性能
- 记录创建人和创建时间用于审计

### 二、后端实现

#### 1. 实体类

**RoleMenu.java** - 角色菜单关联实体
- roleId: 角色ID
- menuId: 菜单ID  
- createBy: 创建人
- createTime: 创建时间

#### 2. VO类

**RoleMenuSaveRequest.java** - 保存请求
```java
{
  "roleId": 1,
  "menuIds": [1, 2, 3, 4, 5]
}
```

#### 3. DAO层

**RoleMenuDao.java** 接口方法：
- `selectMenuIdsByRoleId()` - 查询角色的菜单ID列表
- `deleteByRoleId()` - 删除角色的所有菜单关联
- `batchInsert()` - 批量插入角色菜单关联
- `deleteByRoleIds()` - 批量删除（角色删除时级联删除）

**RoleMenuMapper.xml** - MyBatis映射文件

#### 4. Service层

**RoleManageService** 新增方法：
- `getRoleMenuIds(Long roleId)` - 获取角色绑定的菜单ID列表
- `saveRoleMenus(RoleMenuSaveRequest request)` - 保存角色菜单权限

**RoleManageServiceImpl** 实现：
- 使用 `@Transactional` 保证事务一致性
- 先删除旧关联再插入新关联（replace策略）
- 在删除角色时级联删除菜单关联

#### 5. Controller层

**RoleManageController** 新增接口：

**GET** `/systemManage/role/getRoleMenuIds/{roleId}`
- 功能：获取角色绑定的菜单ID列表
- 返回：`List<Long>`

**POST** `/systemManage/role/saveRoleMenus`
- 功能：保存角色菜单权限
- 请求体：`{ roleId: number, menuIds: number[] }`
- 返回：影响记录数

### 三、前端实现

#### 1. API接口

**system-manage.ts** 新增：
- `fetchGetRoleMenuIds(roleId)` - 获取角色菜单ID列表
- `fetchSaveRoleMenus(data)` - 保存角色菜单权限

#### 2. MenuAuthModal组件

**文件：** `/oasis-web/src/views/manage/role/modules/menu-auth-modal.vue`

**功能特性：**
- 使用NTree组件展示菜单树形结构
- 支持级联选择（选中父节点自动选中子节点）
- 异步加载菜单数据
- 异步加载角色已选菜单
- 保存时调用后端接口更新关联

**核心功能：**
```typescript
// 1. 将菜单列表转换为树形结构
function convertToTree(menus: Menu[]): TreeOption[]

// 2. 获取菜单树数据
async function getMenuTree()

// 3. 获取角色已选菜单
async function getRoleMenus()

// 4. 保存菜单权限
async function handleSave()
```

**UI交互：**
- 弹窗模式（Modal）展示
- 加载时显示Spin loading效果
- 树形复选框支持全选、部分选中
- 默认展开所有节点
- 保存按钮防止重复提交

#### 3. 集成到角色编辑页面

**role-operate-drawer.vue** 已包含：
- "菜单权限"按钮（仅编辑模式显示）
- 点击按钮打开MenuAuthModal组件
- 传递roleId参数

### 四、数据流程

```
┌─────────────────────────────────────────────┐
│  1. 用户点击"编辑角色" → 打开编辑抽屉         │
└──────────────────┬──────────────────────────┘
                   │
┌─────────────────▼──────────────────────────┐
│  2. 用户点击"菜单权限" → 打开菜单权限弹窗      │
└──────────────────┬──────────────────────────┘
                   │
     ┌─────────────┴──────────────┐
     │                            │
┌────▼─────────┐         ┌───────▼────────┐
│ 获取菜单树    │         │ 获取角色已选菜单 │
│ fetchGetMenu  │         │ fetchGetRole    │
│ List()       │         │ MenuIds()       │
└────┬─────────┘         └───────┬────────┘
     │                            │
     └─────────────┬──────────────┘
                   │
┌─────────────────▼──────────────────────────┐
│  3. 展示菜单树，选中已绑定的菜单               │
└──────────────────┬──────────────────────────┘
                   │
┌─────────────────▼──────────────────────────┐
│  4. 用户修改选择，点击"确认"                  │
└──────────────────┬──────────────────────────┘
                   │
┌─────────────────▼──────────────────────────┐
│  5. 调用fetchSaveRoleMenus()保存            │
│     - 先删除旧关联 (DELETE)                  │
│     - 再插入新关联 (BATCH INSERT)            │
└──────────────────┬──────────────────────────┘
                   │
┌─────────────────▼──────────────────────────┐
│  6. 保存成功，关闭弹窗，显示成功提示           │
└─────────────────────────────────────────────┘
```

### 五、使用说明

#### 1. 执行数据库迁移

```bash
mysql -u username -p database < oasis-admin/src/main/resources/sql/create_role_menu_table.sql
```

#### 2. 操作步骤

1. 登录系统，进入"系统管理 > 角色管理"
2. 点击角色列表中的"编辑"按钮
3. 在编辑抽屉中点击"菜单权限"按钮
4. 在弹出的菜单权限弹窗中：
   - 查看菜单树形结构
   - 勾选/取消勾选菜单项
   - 父节点选中时自动选中所有子节点
5. 点击"确认"保存设置
6. 系统提示"更新成功"

#### 3. 数据查询

查询角色的菜单权限：
```sql
SELECT m.* 
FROM menu m
INNER JOIN role_menu rm ON m.id = rm.menu_id
WHERE rm.role_id = 1;
```

查询菜单被哪些角色使用：
```sql
SELECT r.* 
FROM role r
INNER JOIN role_menu rm ON r.id = rm.role_id
WHERE rm.menu_id = 1;
```

### 六、技术特点

#### 1. 数据库设计
- ✅ 使用中间表实现多对多关系
- ✅ 联合唯一索引防止重复关联
- ✅ 索引优化查询性能
- ✅ 级联删除保证数据一致性

#### 2. 后端设计
- ✅ 事务控制保证数据一致性
- ✅ Replace策略（先删后增）简化逻辑
- ✅ 批量操作提升性能
- ✅ 参数校验和异常处理

#### 3. 前端设计
- ✅ Tree组件展示层级结构
- ✅ 级联选择提升用户体验
- ✅ Loading状态反馈
- ✅ 防止重复提交
- ✅ 组件化设计便于维护

### 七、注意事项

1. **权限控制**
   - 所有接口都使用了`@Permission`注解
   - 需要配置相应的权限才能访问

2. **数据一致性**
   - 删除角色时会级联删除菜单关联
   - 使用事务保证操作原子性

3. **性能优化**
   - 批量插入而非逐条插入
   - 建立适当的索引
   - 前端使用树形组件减少DOM数量

4. **用户体验**
   - 默认展开所有节点
   - 支持搜索（可扩展）
   - 加载状态提示

### 八、扩展建议

1. **功能增强**
   - 添加菜单搜索功能
   - 支持快捷全选/反选
   - 添加批量授权功能

2. **性能优化**
   - 菜单数据缓存
   - 虚拟滚动处理大量数据
   - 懒加载子节点

3. **安全增强**
   - 权限变更日志记录
   - 敏感操作二次确认
   - 权限变更审批流程

## 测试建议

### 功能测试
- [ ] 新建角色分配菜单权限
- [ ] 编辑角色修改菜单权限
- [ ] 删除角色验证关联删除
- [ ] 父子节点级联选择
- [ ] 保存后重新打开验证

### 数据测试
- [ ] 空菜单列表处理
- [ ] 大量菜单性能测试
- [ ] 并发保存测试
- [ ] 事务回滚测试

### 边界测试
- [ ] roleId不存在
- [ ] menuId不存在
- [ ] 网络异常处理
- [ ] 重复提交处理

## 相关文件清单

### 后端文件
- `/sql/create_role_menu_table.sql` - 数据库建表脚本
- `/models/entity/RoleMenu.java` - 实体类
- `/models/vo/systemManage/RoleMenuSaveRequest.java` - VO类
- `/dao/RoleMenuDao.java` - DAO接口
- `/mapper/RoleMenuMapper.xml` - MyBatis映射
- `/services/RoleManageService.java` - Service接口（新增方法）
- `/services/impl/RoleManageServiceImpl.java` - Service实现（新增方法）
- `/controller/RoleManageController.java` - Controller（新增接口）

### 前端文件
- `/service/api/system-manage.ts` - API接口（新增）
- `/views/manage/role/modules/menu-auth-modal.vue` - 菜单权限组件（重写）
- `/locales/langs/zh-cn.ts` - 中文国际化（新增）
- `/locales/langs/en-us.ts` - 英文国际化（新增）

## 版本信息

- 实现日期：2026-02-12
- 数据库：MySQL 5.7.8+
- 后端框架：Spring Boot + MyBatis
- 前端框架：Vue 3 + TypeScript + Naive UI

