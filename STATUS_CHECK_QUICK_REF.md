# Status状态判断SQL完善 - 快速参考

## ✅ 已完成修改（3处）

### 1. RoleMenuMapper.xml - selectMenuIdsByRoleId
```xml
<!-- 添加了 INNER JOIN menu 和 m.status = 1 -->
SELECT rm.menu_id
FROM role_menu rm
INNER JOIN menu m ON rm.menu_id = m.id
WHERE rm.role_id = #{roleId}
AND m.status = 1
```
**效果：** 只返回启用的菜单ID

---

### 2. UserRoleMapper.xml - selectUserIdsByRoleId
```xml
<!-- 添加了 INNER JOIN user 和 u.status = 1 -->
SELECT ur.user_id
FROM user_role ur
INNER JOIN user u ON ur.user_id = u.id
WHERE ur.role_id = #{roleId}
AND u.status = 1
```
**效果：** 只返回启用的用户ID

---

### 3. UserMapper.xml - selectByUserAccountOrUserIdAndPassword
```xml
<!-- 修复拼写错误：stauts → status -->
WHERE (user_account = #{user} OR user_id = #{user}) AND password = #{password}
AND status = 1  <!-- 之前是 stauts = 1 -->
```
**效果：** 🔴 **修复严重bug** - 禁用用户无法登录

---

## 📊 修改影响

| 修改 | 影响功能 | 业务效果 |
|------|---------|---------|
| RoleMenuMapper | 角色菜单权限 | 禁用菜单不会出现在权限列表 |
| UserRoleMapper | 角色用户查询 | 禁用用户不会出现在角色用户列表 |
| UserMapper | 用户登录 | 禁用用户无法登录系统 |

---

## 📋 Status判断原则

| 场景 | 是否判断 | 说明 |
|------|---------|------|
| 权限查询 | ✅ | 必须过滤禁用数据 |
| 关联查询（JOIN） | ✅ | 必须判断主表status |
| 登录验证 | ✅ | 禁用用户不能登录 |
| 详情查看 | ❌ | 需要查看禁用数据 |
| 唯一性校验 | ❌ | 需要检查所有数据 |

---

## ✅ 已正确处理（无需修改）

- `UserRoleMapper.selectRoleIdsByUserId` - 已有 `r.status = 1`
- `MenuMapper.selectMenuList` - 已支持 status 参数

---

## 📝 测试要点

1. ✅ 禁用菜单不会出现在用户权限列表
2. ✅ 禁用用户无法登录
3. ✅ 禁用角色的权限不会生效

---

## 📄 详细文档

- 完整分析：`STATUS_CHECK_ANALYSIS.md`
- 修改总结：`STATUS_CHECK_FIX_SUMMARY.md`

