# 角色管理功能实现文档

## 概述

本文档记录了角色管理（Role Management）功能的完整实现，包括后端API和前端界面。

## 一、数据库设计

### 1. 角色表（role）

**SQL脚本位置**: `oasis-admin/src/main/resources/sql/init_role.sql`

**表结构**:

- `id` - BIGINT - 主键ID（自增）
- `role_name` - VARCHAR(50) - 角色名称
- `role_code` - VARCHAR(50) - 角色编码（唯一索引）
- `role_desc` - VARCHAR(200) - 角色描述
- `status` - TINYINT(1) - 状态（1=启用/true，0=禁用/false）
- `create_by` - VARCHAR(50) - 创建人
- `create_time` - DATETIME - 创建时间
- `update_by` - VARCHAR(50) - 更新人
- `update_time` - DATETIME - 更新时间

### 2. 初始化数据

- 超级管理员（SUPER_ADMIN）- 启用
- 管理员（ADMIN）- 启用
- 普通用户（USER）- 启用
- 访客（GUEST）- 禁用

## 二、后端实现

### 1. 实体类

- `Role.java` - 角色实体

### 2. VO类

- `RoleListRequest.java` - 列表查询请求
- `RoleListResponse.java` - 列表查询响应
- `RoleSaveRequest.java` - 保存请求（新增/编辑）
- `RoleDeleteRequest.java` - 删除请求
- `RoleToggleStatusRequest.java` - 状态切换请求
- `RoleVO.java` - 角色值对象

### 3. 数据访问层

- `RoleDao.java` - 数据访问接口
- `RoleMapper.xml` - MyBatis映射文件

**主要方法**:

- `selectRoleList` - 分页查询角色列表
- `countRoleList` - 查询角色总数
- `selectById` - 根据ID查询
- `selectByRoleCode` - 根据编码查询
- `insert` - 新增角色
- `update` - 更新角色
- `deleteRolesByIds` - 批量删除
- `toggleRoleStatus` - 切换状态（使用 NOT status）

### 4. 服务层

- `RoleManageService.java` - 服务接口
- `RoleManageServiceImpl.java` - 服务实现

**业务逻辑**:

- 新增时检查角色编码唯一性
- 编辑时验证角色是否存在
- 支持批量删除和批量状态切换

### 5. 控制层

- `RoleManageController.java` - REST API控制器

**API端点** (Base URL: `/systemManage/role`):

- `POST /getRoleList` - 获取角色列表
- `POST /saveRole` - 保存角色（新增/编辑）
- `POST /deleteRoles` - 批量删除角色
- `POST /toggleRoleStatus` - 批量切换状态
- `GET /getRoleById/{id}` - 获取角色详情

## 三、前端实现

### 1. API接口

**文件**: `oasis-web/src/service/api/system-manage.ts`

- `fetchGetRoleList` - 获取列表
- `fetchSaveRole` - 保存角色
- `fetchDeleteRoles` - 删除角色
- `fetchToggleRoleStatus` - 切换状态
- `fetchGetRoleById` - 获取详情

### 2. 类型定义

**文件**: `oasis-web/src/typings/api/system-manage.d.ts`

- `Role` - 角色类型
- `RoleSearchParams` - 搜索参数
- `RoleList` - 列表类型
- `RoleEdit` - 编辑类型

### 3. 页面组件

#### 主页面

**文件**: `oasis-web/src/views/manage/role/index.vue`

**功能**:

- 角色列表展示（移除序号列）
- 状态标签显示（绿色=启用，黄色=禁用）
- 操作列按钮：
    - 编辑
    - 启用/禁用（动态显示）
      **版本**: 1.0.0
      **创建日期**: 2026-02-01

---

6. 增加导出功能
5. 增加操作日志记录
4. 增加用户-角色关联
3. 增加按钮权限配置
2. 增加菜单权限配置
1. 增加角色权限关联

## 九、后续优化建议

- [ ] 国际化切换正常
- [ ] 表单验证正常
- [ ] 批量按钮在正确位置
- [ ] 操作按钮显示正确
- [ ] 状态标签颜色正确
- [ ] 列表无序号列

### UI测试

- [ ] 搜索过滤（各个条件组合）
- [ ] 状态切换（单个、批量）
- [ ] 删除角色（单个、批量）
- [ ] 编辑角色（成功、角色不存在）
- [ ] 新增角色（成功、编码重复）
- [ ] 角色列表查询（空数据、有数据、分页）

### 功能测试

## 八、测试建议

5. **按钮位置**：批量禁用/启用按钮在新增按钮右边
4. **分页计算**：后端使用 OFFSET/LIMIT，前端传入页码和每页大小
3. **角色编码唯一性**：新增/编辑时需要校验
2. **状态切换SQL**：使用 `NOT status` 实现状态反转
1. **数据库字段类型**：status 在数据库中使用 TINYINT(1)，在Java中使用 Boolean 类型

## 七、注意事项

- Pinia
- Vite
- Naive UI
- TypeScript
- Vue 3

### 前端

- Jakarta Validation
- Lombok
- MySQL
- MyBatis
- Spring Boot 3.x

### 后端

## 六、技术栈

```
http://localhost:5173/manage/role
```

3. 访问角色管理页面：

```
pnpm dev
```bash
2. 启动开发服务器：

```

pnpm install
cd oasis-web

```bash
1. 安装依赖：

### 前端运行

```

mvn spring-boot:run
cd oasis-admin

```bash
2. 启动Spring Boot应用：

```

source oasis-admin/src/main/resources/sql/init_role.sql
-- 运行 init_role.sql 创建表和初始化数据

```sql
1. 执行数据库脚本：

### 后端部署

## 五、使用说明

- ✅ 请求参数验证（@Valid）
- ✅ 所有接口需要 @Permission 注解
### 5. 权限控制

- ✅ 数据库使用 NOT status 切换
- ✅ 动态按钮显示
- ✅ 批量启用/禁用
- ✅ 单个启用/禁用
### 4. 状态管理

- ✅ 删除确认提示
- ✅ 批量删除
- ✅ 单个删除
### 3. 删除功能

- ✅ 表单验证
- ✅ 角色编码唯一性校验
- ✅ 统一的保存接口
### 2. 新增/编辑

- ✅ 分页支持
- ✅ 状态使用布尔值（Boolean）
- ✅ 显示角色名称、编码、描述、状态
- ✅ 移除序号列
### 1. 列表展示

## 四、功能特性

- `page.manage.role.batchToggleStatus` - 批量禁用/启用
- `page.manage.role.confirmDisable` - 确定禁用该角色吗？
- `page.manage.role.confirmEnable` - 确定启用该角色吗？
- `page.manage.role.disable` - 禁用
- `page.manage.role.enable` - 启用
**新增翻译**:

- `oasis-web/src/locales/langs/en-us.ts` - 英文
- `oasis-web/src/locales/langs/zh-cn.ts` - 中文
**文件**: 
### 4. 国际化

- 角色描述（选填）
- 角色状态（必填，单选）
- 角色编码（必填）
- 角色名称（必填）
**表单字段**:

**文件**: `oasis-web/src/views/manage/role/modules/role-operate-drawer.vue`
#### 编辑抽屉

- 角色状态（下拉选择：启用/禁用）
- 角色编码（模糊搜索）
- 角色名称（模糊搜索）
**搜索条件**:

**文件**: `oasis-web/src/views/manage/role/modules/role-search.vue`
#### 搜索组件

  - 刷新
  - 批量删除
  - 批量禁用/启用
  - 新增
- 批量操作：
  - 删除

