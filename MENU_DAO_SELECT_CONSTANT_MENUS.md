# MenuDao 新增 selectConstantMenus 方法

## 修改内容

为了正确查询常量路由，在 `MenuDao` 中新增了 `selectConstantMenus()` 方法，并更新了 `RouteServiceImpl` 中的 `getConstantRoutes()` 方法来使用这个新方法。

## 修改的文件

### 1. MenuDao.java

**文件路径：** `oasis-admin/src/main/java/com/github/kevin/oasis/dao/MenuDao.java`

**新增方法：**
```java
/**
 * 查询所有常量菜单列表
 *
 * @return 菜单列表
 */
List<Menu> selectConstantMenus();
```

### 2. MenuMapper.xml

**文件路径：** `oasis-admin/src/main/resources/mapper/MenuMapper.xml`

**新增 SQL 查询：**
```xml
<!-- 查询所有常量菜单列表 -->
<select id="selectConstantMenus" resultMap="BaseResultMap">
    SELECT
    <include refid="Base_Column_List"/>
    FROM menu
    WHERE constant = 1 AND status = 1
    ORDER BY parent_id ASC, `order` ASC, create_time ASC
</select>
```

**查询逻辑：**
- 查询 `constant = 1` 的菜单（常量路由）
- 只查询状态为启用的菜单（`status = 1`）
- 按父级ID、排序号和创建时间排序

### 3. RouteServiceImpl.java

**文件路径：** `oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/RouteServiceImpl.java`

**修改前：**
```java
List<Menu> constantMenus = menuDao.selectNotConstantMenus(); // 错误：这是查询非常量菜单
```

**修改后：**
```java
List<Menu> constantMenus = menuDao.selectConstantMenus(); // 正确：查询常量菜单
```

## 问题修复

**原问题：** `getConstantRoutes()` 方法中错误地调用了 `selectNotConstantMenus()` 来查询常量路由，这是一个逻辑错误。

**解��方案：** 
1. 新增专门的 `selectConstantMenus()` 方法来查询 `constant = 1` 的菜单
2. 更新 `getConstantRoutes()` 使用正确的方法

## 数据库字段说明

- `constant = 1`：常量路由（不需要权限控制的公共路由，如登录页、404页等）
- `constant = 0`：动态路由（需要权限控制的业务路由）
- `status = 1`：启用状态
- `status = 0`：禁用状态

## 验证方法

1. 启动后端服务
2. 调用 `/route/getConstantRoutes` 接口
3. 应该返回数据库中 `constant = 1 AND status = 1` 的菜单列表
4. 确认返回的都是常量路由（如登录页、404、403等公共页面）

