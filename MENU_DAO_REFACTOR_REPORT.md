# MenuDao selectMenuList 参数调整实施报告

## 📋 修改概述

将 `MenuDao.selectMenuList` 方法的参数从对象参数改为单独的参数形式，并删除了 `selectConstantMenus` 和 `selectNotConstantMenus` 方法，在所有使用这些方法的地方替换为 `selectMenuList`。

## 🔄 修改内容

### 1. MenuDao.java 接口

**文件：** `oasis-admin/src/main/java/com/github/kevin/oasis/dao/MenuDao.java`

#### 修改前：
```java
import com.github.kevin.oasis.models.vo.systemManage.MenuListRequest;

List<Menu> selectMenuList(@Param("request") MenuListRequest request);

List<Menu> selectConstantMenus();

List<Menu> selectNotConstantMenus();
```

#### 修改后：
```java
// 移除 MenuListRequest 导入

List<Menu> selectMenuList(@Param("constant") Boolean constant, @Param("status") Boolean status);

// 删除 selectConstantMenus() 方法
// 删除 selectNotConstantMenus() 方法
```

### 2. MenuMapper.xml

**文件：** `oasis-admin/src/main/resources/mapper/MenuMapper.xml`

#### 修改前：
```xml
<select id="selectMenuList" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
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

<select id="selectConstantMenus" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM menu
    WHERE constant = 1 AND status = 1
    ORDER BY parent_id ASC, `order` ASC, create_time ASC
</select>

<select id="selectNotConstantMenus" resultMap="BaseResultMap">
    SELECT <include refid="Base_Column_List"/>
    FROM menu
    WHERE constant = 0 AND status = 1
    ORDER BY parent_id ASC, `order` ASC, create_time ASC
</select>
```

#### 修改后：
```xml
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

<!-- 删除 selectConstantMenus 查询 -->
<!-- 删除 selectNotConstantMenus 查询 -->
```

### 3. RouteServiceImpl.java

**文件：** `oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/RouteServiceImpl.java`

#### getConstantRoutes() 方法

**修改前：**
```java
List<Menu> constantMenus = menuDao.selectConstantMenus();
```

**修改后：**
```java
// 查询所有constant=true且status=true的菜单
List<Menu> constantMenus = menuDao.selectMenuList(true, true);
```

#### getUserRoutes() 方法

**修改前：**
```java
// 查询所有菜单
List<Menu> allMenus = menuDao.selectNotConstantMenus();
```

**修改后：**
```java
// 查询所有非常量且启用的菜单
List<Menu> allMenus = menuDao.selectMenuList(false, true);
```

### 4. MenuManageServiceImpl.java

**文件：** `oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/MenuManageServiceImpl.java`

#### getMenuList() 方法

**修改前：**
```java
List<Menu> allMenus = menuDao.selectMenuList(request);
```

**修改后：**
```java
// 如果没有传参数，创建默认请求（查询所有）
if (request == null) {
    request = MenuListRequest.builder().build();
}

// 查询菜单列表（根据参数筛选）
List<Menu> allMenus = menuDao.selectMenuList(request.getConstant(), request.getStatus());
```

#### getAllPages() 方法

**修改前：**
```java
// 查询所有菜单
List<Menu> allMenus = menuDao.selectNotConstantMenus();
```

**修改后：**
```java
// 查询所有非常量且启用的菜单
List<Menu> allMenus = menuDao.selectMenuList(false, true);
```

## 📊 方法替换对照表

| 原方法调用 | 替换为 | 说明 |
|-----------|--------|------|
| `menuDao.selectConstantMenus()` | `menuDao.selectMenuList(true, true)` | 查询常量路由且启用的菜单 |
| `menuDao.selectNotConstantMenus()` | `menuDao.selectMenuList(false, true)` | 查询非常量路由且启用的菜单 |
| `menuDao.selectMenuList(request)` | `menuDao.selectMenuList(request.getConstant(), request.getStatus())` | 传递单独参数 |

## 🎯 修改优势

### 1. **参数更加灵活**
- 使用独立参数，调用更直观
- 不需要构建完整的 Request 对象

### 2. **代码更简洁**
- 删除了两个冗余方法
- 统一使用 `selectMenuList` 方法
- 减少代码维护成本

### 3. **MyBatis 映射更清晰**
- 直接使用 `@Param` 注解的参数名
- 不需要通过对象属性访问（`request.constant`）
- 更符合 MyBatis 最佳实践

### 4. **向后兼容**
- `selectMenuList` 支持参数为 null
- 可以实现原有方法的所有功能

## 🔍 调用示例

### 查询所有菜单
```java
List<Menu> allMenus = menuDao.selectMenuList(null, null);
```

### 查询启用的常量路��
```java
List<Menu> constantMenus = menuDao.selectMenuList(true, true);
```

### 查询启用的动态路由
```java
List<Menu> dynamicMenus = menuDao.selectMenuList(false, true);
```

### 查询禁用的菜单
```java
List<Menu> disabledMenus = menuDao.selectMenuList(null, false);
```

### 查询所有常量路由（包括禁用）
```java
List<Menu> allConstantMenus = menuDao.selectMenuList(true, null);
```

## ✅ 验证结果

- ✅ MenuDao 接口更新完成
- ✅ MenuMapper.xml SQL 更新完成
- ✅ RouteServiceImpl 两处调用已更新
- ✅ MenuManageServiceImpl 两处调用已更新
- ✅ 移除了未使用的导入
- ✅ 删除了冗余方法
- ✅ 无编译错误（仅有预存在的警告）

## 📝 注意事项

1. **参数顺序：** `constant` 在前，`status` 在后
2. **null 值处理：** 参数为 null 时，对应的 WHERE 条件不会添加
3. **Boolean 类型：** 使用包装类型 Boolean，支持 null 值
4. **MyBatis 参数绑定：** 使用 `@Param` 注解确保参数名正确映射

## 🎉 总结

本次调整成功将 `selectMenuList` 方法改为更灵活的独立参数形式，并统一了菜单查询接口，删除了冗余方法。所有使用旧方法的地方都已正确替换，系统功能保持一致。

