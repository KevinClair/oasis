# 菜单树形结构测试指南

## 测试目标

验证菜单列表返回的树形结构，确保子菜单正确嵌套在父级菜单的 `children` 字段中。

## 测试前准备

### 1. 确保数据库中有测试数据

```sql
-- 查看菜单数据
SELECT id, parent_id, menu_name, route_name, menu_type
FROM menu
ORDER BY parent_id, `order`;

-- 预期数据结构：
-- id=1, parent_id=0, menu_name="系统管理", menu_type=1 (目录)
-- id=2, parent_id=1, menu_name="用户管理", menu_type=2 (菜单)
-- id=3, parent_id=1, menu_name="角色管理", menu_type=2 (菜单)
-- id=4, parent_id=1, menu_name="菜单管理", menu_type=2 (菜单)
```

### 2. 重启后端服务

应用最新的代码更改。

## 测试步骤

### 测试1：API接口返回验证

**请求：**

```bash
curl -X GET http://localhost:8080/systemManage/menu/getMenuList \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**预期响应：**

```json
{
  "code": "0000",
  "msg": "success",
  "data": {
    "current": 1,
    "size": 1,
    "total": 1,
    "records": [
      {
        "id": 1,
        "parentId": 0,
        "menuType": 1,
        "menuName": "系统管理",
        "routeName": "manage",
        "routePath": "/manage",
        "icon": "carbon:cloud-service-management",
        "iconType": "1",
        "order": 1,
        "status": true,
        "children": [
          {
            "id": 2,
            "parentId": 1,
            "menuType": 2,
            "menuName": "用户管理",
            "routeName": "manage_user",
            "routePath": "/manage/user",
            "icon": "ic:round-manage-accounts",
            "iconType": "1",
            "order": 1,
            "status": true,
            "children": null
          },
          {
            "id": 3,
            "parentId": 1,
            "menuType": 2,
            "menuName": "角色管理",
            "routeName": "manage_role",
            "routePath": "/manage/role",
            "icon": "carbon:user-role",
            "iconType": "1",
            "order": 2,
            "status": true,
            "children": null
          },
          {
            "id": 4,
            "parentId": 1,
            "menuType": 2,
            "menuName": "菜单管理",
            "routeName": "manage_menu",
            "routePath": "/manage/menu",
            "icon": "material-symbols:route",
            "iconType": "1",
            "order": 3,
            "status": true,
            "children": null
          }
        ]
      }
    ]
  }
}
```

**验证点：**

- ✅ `records` 数组只包含顶层菜单（parentId为0）
- ✅ 顶层菜单的 `children` 字段包含所有子菜单
- ✅ 子菜单的 `children` 字段为 `null`（如果没有子菜单）
- ✅ `size` 和 `total` 等于顶层菜单数量（本例为1）

### 测试2：前端页面显示验证

**访问：** `http://localhost:5173/manage/menu`

**验证点：**

1. ✅ 页面加载成功，无报错
2. ✅ 菜单列表以树形结构展示
3. ✅ 可以展开/收起父级菜单
4. ✅ 子菜单显示在父级菜单下方，有缩进效果
5. ✅ 菜单图标正常显示
6. ✅ 菜单操作按钮正常工作（新增、编辑、删除）

### 测试3：多层级嵌套验证

如果有三层或更多层级的菜单结构，验证递归构建是否正常：

**测试数据：**

```sql
-- 添加一个三级菜单
INSERT INTO menu (parent_id, menu_type, menu_name, route_name, route_path,
                  component, icon_type, icon, i18n_key, `order`, status, create_by)
VALUES (2, 2, '用户详情', 'manage_user_detail', '/manage/user/detail',
        'view.manage_user_detail', '1', 'mdi:account-details', 'route.manage_user_detail',
        1, 1, 'system');
```

**预期结果：**

```json
{
  "id": 1,
  "menuName": "系统管理",
  "children": [
    {
      "id": 2,
      "menuName": "用户管理",
      "children": [
        {
          "id": 5,
          "menuName": "用户详情",
          "children": null
        }
      ]
    }
  ]
}
```

### 测试4：边界情况测试

#### 4.1 没有子菜单的情况

**场景：** 顶层菜单没有任何子菜单

**预期：**

- `children` 字段为 `null`
- 前端不显示展开/收起图标

#### 4.2 空菜单列表

**场景：** 数据库中没有任何菜单

**预期：**

```json
{
  "current": 1,
  "size": 0,
  "total": 0,
  "records": []
}
```

#### 4.3 孤立的子菜单

**场景：** 子菜单的 parentId 指向不存在的父菜单

**预期：**

- 该菜单不会出现在返回结果中
- 或者作为顶层菜单显示（取决于业务需求）

### 测试5：性能测试

**测试数据规模：**

- 10个顶层菜单
- 每个顶层菜单下有5个子菜单
- 部分子菜单下有3级菜单
- 总计约80个菜单节点

**预期：**

- 接口响应时间 < 500ms
- 前端渲染时间 < 200ms
- 树形结构构建正确

## 常见问题排查

### 问题1：children字段为空数组而不是null

**原因：** buildChildren方法未正确处理空子菜单列表

**解决：** 确保代码中有以下逻辑

```java
parent.setChildren(children.isEmpty() ?null:children);
```

### 问题2：顶层菜单统计不正确

**原因：** size和total统计了所有菜单而不是只统计顶层菜单

**解决：** 确保只统计顶层菜单数量

```java
.size(menuVOList.size())  // menuVOList只包含顶层菜单
        .

total((long) menuVOList.

size())
```

### 问题3：递归导致栈溢出

**原因：** 菜单数据中存在循环引用（A->B->A）

**排查：**

```sql
-- 检查是否有循环引用
SELECT m1.id, m1.menu_name, m1.parent_id, m2.menu_name as parent_name
FROM menu m1
         LEFT JOIN menu m2 ON m1.parent_id = m2.id
WHERE m1.parent_id = m1.id -- 自己指向自己
   OR (m1.parent_id IS NOT NULL AND m2.id IS NULL); -- 父菜单不存在
```

### 问题4：前端展开/收起不工作

**原因：** 前端表格组件的 `row-key` 配置不正确

**检查：** 确保前端表格配置了正确的主键

```vue

<NDataTable
    :data="data"
    :columns="columns"
    row-key="id"
    default-expand-all
/>
```

## 回归测试检查清单

- [ ] 菜单列表接口返回正确的树形结构
- [ ] 顶层菜单数量统计正确
- [ ] 子菜单正确嵌套在 children 字段中
- [ ] 没有子菜单时 children 为 null
- [ ] 前端页面正常显示树形结构
- [ ] 展开/收起功能正常
- [ ] 新增菜单功能正常
- [ ] 编辑菜单功能正常
- [ ] 删除菜单功能正常（有子菜单时阻止删除）
- [ ] 批量删除功能正常

---

**测试日期：** 2026-02-08  
**测试环境：** 开发环境  
**测试人员：** 开发团队

