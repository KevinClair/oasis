# 数据表Status状态判断分析报告

## 📊 数据表结构分析

### 主表（有status字段）
1. **user表** - 用户表（有status字段：启用/禁用）
2. **role表** - 角色表（有status字段：启用/禁用）
3. **menu表** - 菜单表（有status字段：启用/禁用）

### 关联表（无status字段）
1. **user_role表** - 用户角色关联表（无status字段）
2. **role_menu表** - 角色菜单关联表（无status字段）

## 🔍 SQL查询Status判断需求分析

### 原则：
1. **关联表本身无status** - 不需要判断关联表的status
2. **需要JOIN主表时** - 必须判断主表的status，避免返回禁用的数据
3. **业务逻辑要求** - 根据具体业务场景决定是否需要status判断

---

## ✅ 需要添加Status判断的SQL

### 1. RoleMenuMapper.xml - selectMenuIdsByRoleId

**当前代码：**
```xml
<select id="selectMenuIdsByRoleId" resultType="long">
    SELECT menu_id
    FROM role_menu
    WHERE role_id = #{roleId}
</select>
```

**问题：** 可能返回已禁用菜单的ID

**应该修改为：**
```xml
<select id="selectMenuIdsByRoleId" resultType="long">
    SELECT rm.menu_id
    FROM role_menu rm
    INNER JOIN menu m ON rm.menu_id = m.id
    WHERE rm.role_id = #{roleId}
    AND m.status = 1
</select>
```

**原因：** 角色菜单授权时，应该只返回启用的菜单ID，避免用户访问到已禁用的菜单。

---

### 2. UserRoleMapper.xml - selectUserIdsByRoleId

**当前代码：**
```xml
<select id="selectUserIdsByRoleId" parameterType="long" resultType="long">
    SELECT user_id
    FROM user_role
    WHERE role_id = #{roleId}
</select>
```

**问题：** 可能返回已禁用用户的ID

**应该修改为：**
```xml
<select id="selectUserIdsByRoleId" parameterType="long" resultType="long">
    SELECT ur.user_id
    FROM user_role ur
    INNER JOIN user u ON ur.user_id = u.id
    WHERE ur.role_id = #{roleId}
    AND u.status = 1
</select>
```

**原因：** 根据角色查询用户时，应该只返回启用状态的用户ID。

---

### 3. UserMapper.xml - selectByUserAccountOrUserIdAndPassword

**当前代码：**
```xml
<select id="selectByUserAccountOrUserIdAndPassword" resultMap="BaseResultMap">
    SELECT
    <include refid="Base_Column_List"/>
    FROM user
    WHERE (user_account = #{user} OR user_id = #{user}) AND password = #{password}
    AND stauts = 1  <!-- 注意：这里有拼写错误 stauts 应该是 status -->
    LIMIT 1
</select>
```

**问题：** 拼写错误 `stauts` 应该是 `status`

**应该修改为：**
```xml
<select id="selectByUserAccountOrUserIdAndPassword" resultMap="BaseResultMap">
    SELECT
    <include refid="Base_Column_List"/>
    FROM user
    WHERE (user_account = #{user} OR user_id = #{user}) AND password = #{password}
    AND status = 1
    LIMIT 1
</select>
```

**原因：** 修复拼写错误，确保只能使用启用状态的用户登录。

---

## ⚠️ 可选添加Status判断的SQL（根据业务需求）

### 1. UserMapper.xml - selectById

**当前代码：**
```xml
<select id="selectById" parameterType="long" resultMap="BaseResultMap">
    SELECT
    <include refid="Base_Column_List"/>
    FROM user
    WHERE id = #{id}
</select>
```

**建议：** 根据业务需求决定
- 如果用于用户详情查看/编辑：**不需要**添加status判断（需要查看禁用用户信息）
- 如果用于验证用户有效性：**需要**添加 `AND status = 1`

**当前建议：保持不变**（通常用于详情查看）

---

### 2. RoleMapper.xml - selectById

**当前代码：**
```xml
<select id="selectById" parameterType="long" resultMap="BaseResultMap">
    SELECT
    <include refid="Base_Column_List"/>
    FROM role
    WHERE id = #{id}
</select>
```

**建议：保持不变**（通常用于详情查看/编辑，需要查看禁用角色信息）

---

### 3. RoleMapper.xml - selectByRoleCode

**当前代码：**
```xml
<select id="selectByRoleCode" parameterType="string" resultMap="BaseResultMap">
    SELECT
    <include refid="Base_Column_List"/>
    FROM role
    WHERE role_code = #{roleCode}
</select>
```

**建议：保持不变**（用于查重和编辑，需要能查询到禁用的角色）

---

## ✅ 已正确处理Status的SQL

### 1. UserRoleMapper.xml - selectRoleIdsByUserId ✓
```xml
<!-- 已经正确JOIN了role表并判断status -->
<select id="selectRoleIdsByUserId" parameterType="long" resultType="long">
    SELECT ur.role_id
    FROM user_role ur
    INNER JOIN role r ON ur.role_id = r.id
    WHERE ur.user_id = #{userId}
    AND r.status = 1
</select>
```

### 2. MenuMapper.xml - selectMenuList ✓
```xml
<!-- 已经支持status参数筛选 -->
<select id="selectMenuList" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM menu
    <where>
        <if test="constant != null">
            AND constant = #{constant}
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
    </where>
    ORDER BY parent_id ASC, `order` ASC, create_time ASC
</select>
```

---

## 📋 修改清单

### 必须修改（3处）

1. ✅ **RoleMenuMapper.xml** - `selectMenuIdsByRoleId`
   - 添加 JOIN menu 表和 status 判断

2. ✅ **UserRoleMapper.xml** - `selectUserIdsByRoleId`
   - 添加 JOIN user 表和 status 判断

3. ✅ **UserMapper.xml** - `selectByUserAccountOrUserIdAndPassword`
   - 修复拼写错误 `stauts` → `status`

### 可选修改（根据业务需求）
- selectById 系列方法：根据具体使用场景决定

---

## 🎯 修改建议优先级

| 优先级 | 文件 | 方法 | 原因 |
|-------|------|------|------|
| 🔴 高 | UserMapper.xml | selectByUserAccountOrUserIdAndPassword | 拼写错误，影响登录功能 |
| 🔴 高 | RoleMenuMapper.xml | selectMenuIdsByRoleId | 避免返回禁用菜单，影响权限控制 |
| 🟡 中 | UserRoleMapper.xml | selectUserIdsByRoleId | 避免返回禁用用户，影响数据准确性 |

---

## 📝 总结

需要添加status判断的主要原则：
1. **关联查询时**：JOIN主表后必须判断主表的status
2. **权限相关查询**：必须过滤禁用的数据
3. **详情查询**：通常不需要status判断（需要查看禁用数据）
4. **列表查询**：根据业务需求，通常支持status参数筛选

