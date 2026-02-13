# 角色菜单授权界面优化 - 仅显示启用的非常量路由

## 修改内容

调整角色管理中修改角色菜单界面的请求，只查询启用的非常量路由，避免显示常量路由（如登录页、404等公共页面）和已禁用的菜单。

## 修改的文件

### 1. system-manage.ts (前端 API)

**文件路径：** `oasis-web/src/service/api/system-manage.ts`

**修改前：**
```typescript
export function fetchGetMenuList() {
  return request<Api.SystemManage.MenuList>({
    url: '/systemManage/menu/getMenuList',
    method: 'get'
  });
}
```

**修改后：**
```typescript
export function fetchGetMenuList(params?: { constant?: boolean; status?: boolean }) {
  return request<Api.SystemManage.MenuList>({
    url: '/systemManage/menu/getMenuList',
    method: 'get',
    params
  });
}
```

**改动说明：**
- 添加可选参数 `params`，支持 `constant` 和 `status` 筛选
- 通过 `params` 传递查询参数

### 2. menu-auth-modal.vue (角色菜单授权弹窗)

**文件路径：** `oasis-web/src/views/manage/role/modules/menu-auth-modal.vue`

**修改前：**
```typescript
/**
 * 获取菜单树数据
 */
async function getMenuTree() {
  loading.value = true;
  try {
    const { data, error } = await fetchGetMenuList();
    if (!error && data) {
      menuTreeData.value = convertToTree(data.records || []);
    }
  } finally {
    loading.value = false;
  }
}
```

**修改后：**
```typescript
/**
 * 获取菜单树数据（仅查询启用的非常量路由）
 */
async function getMenuTree() {
  loading.value = true;
  try {
    // 只查询启用的非常量路由（constant=false, status=true）
    const { data, error } = await fetchGetMenuList({ constant: false, status: true });
    if (!error && data) {
      menuTreeData.value = convertToTree(data.records || []);
    }
  } finally {
    loading.value = false;
  }
}
```

**改动说明：**
- 调用 `fetchGetMenuList` 时传入参数 `{ constant: false, status: true }`
- `constant: false` - 只查询动态路由（排除常量路由）
- `status: true` - 只查询启用的菜单（排除禁用的菜单）

## 功能效果

### 修改前
角色菜单授权界面显示：
- ✅ 动态路由（业务菜单）
- ❌ 常量路由（登录页、404、403等公共页面）
- ❌ 已禁用的菜单

### 修改后
角色菜单授权界面显示：
- ✅ 动态路由（业务菜单）
- ✅ 仅显示启用状态的菜单
- ❌ 不显示常量路由
- ❌ 不显示已禁用的菜单

## 业务逻辑

### 为什么要过滤常量路由？
- **常量路由**（如登录页、404、403等）是所有用户都可以访问的公共页面
- 不需要通过角色授权来控制访问权限
- 在角色菜单授权中显示这些页面没有意义

### 为什么要过滤禁用的菜单？
- 已禁用的菜单不应该被授权给任何角色
- 避免管理员误操作将禁用的菜单授权给角色
- 保持授权界面的清晰和准确

## 后端支持

此功能依赖于后端接口 `/systemManage/menu/getMenuList` 的筛选参数支持：

```java
@GetMapping("/getMenuList")
public Response<MenuListResponse> getMenuList(
        @RequestParam(required = false) Boolean constant,
        @RequestParam(required = false) Boolean status) {
    // ...
}
```

后端 SQL 查询逻辑：
```xml
<select id="selectMenuList" resultMap="BaseResultMap">
    SELECT
    <include refid="Base_Column_List"/>
    FROM menu
    <where>
        <if test="request.constant != null">
            AND constant = #{request.constant}
        </if>
        <if test="request.status != null">
            AND status = #{request.status}
        </if>
    </where>
    ORDER BY parent_id ASC, `order` ASC, create_time ASC
</select>
```

## 测试验证

1. 登录系统，进入"角色管理"页面
2. 选择任意角色，点击"菜单授权"按钮
3. 验证菜单树中：
   - ✅ 只显示业务菜单（动态路由）
   - ✅ 只显示启用状态的菜单
   - ❌ 不显示登录页、404、403等常量路由
   - ❌ 不显示已禁用的菜单

## 相关文档

- 菜单列表筛选功能：`MENU_LIST_FILTER_IMPLEMENTATION.md`
- 快速参考：`MENU_LIST_FILTER_QUICK_REF.md`

## 注意事项

- 此修改只影响角色菜单授权界面，不影响其他地方的菜单列表查询
- 如果需要在其他地方使用不同的筛选条件，可以传入不同的参数
- 保持了 API 的向后兼容性（参数可选）

