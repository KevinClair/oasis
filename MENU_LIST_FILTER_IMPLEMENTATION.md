# 菜单列表接口增加筛选参数功能

## 修改内容

为 `/systemManage/menu/getMenuList` 接口增加了常量数据和状态筛选参数，支持灵活查询不同类型的菜单。

## 修改的文件

### 1. 新建 MenuListRequest.java

**文件路径：** `oasis-admin/src/main/java/com/github/kevin/oasis/models/vo/systemManage/MenuListRequest.java`

**功能：** 菜单列表查询请求参数

```java
public class MenuListRequest {
    /**
     * 常量数据筛选：null-全部，true-仅常量路由，false-仅动态路由
     */
    private Boolean constant;

    /**
     * 状态筛选：null-全部，true-仅启用，false-仅禁用
     */
    private Boolean status;
}
```

### 2. MenuDao.java

**文件路径：** `oasis-admin/src/main/java/com/github/kevin/oasis/dao/MenuDao.java`

**新增方法：**
```java
/**
 * 查询菜单列表（支持常量和状态筛选）
 *
 * @param request 查询参数
 * @return 菜单列表
 */
List<Menu> selectMenuList(@Param("request") MenuListRequest request);
```

### 3. MenuMapper.xml

**文件路径：** `oasis-admin/src/main/resources/mapper/MenuMapper.xml`

**新增 SQL 查询：**
```xml
<!-- 查询菜单列表（支持常量和状态筛选） -->
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

**SQL 特点：**
- 使用动态 SQL，参数为 null 时不添加该条件
- 同时支持 constant 和 status 参数组合查询
- 按父级ID、排序号和创建时间排序

### 4. MenuManageService.java

**文件路径：** `oasis-admin/src/main/java/com/github/kevin/oasis/services/MenuManageService.java`

**修改前：**
```java
MenuListResponse getMenuList();
```

**修改后：**
```java
MenuListResponse getMenuList(MenuListRequest request);
```

### 5. MenuManageServiceImpl.java

**文件路径：** `oasis-admin/src/main/java/com/github/kevin/oasis/services/impl/MenuManageServiceImpl.java`

**主要改动：**
- 接受 `MenuListRequest` 参数
- 如果参数为 null，创建默认请求（查询所有）
- 调用 `menuDao.selectMenuList(request)` 进行筛选查询
- 增加日志输出，显示查询参��和结果统计

### 6. MenuManageController.java

**文件路径：** `oasis-admin/src/main/java/com/github/kevin/oasis/controller/MenuManageController.java`

**修改前：**
```java
@GetMapping("/getMenuList")
public Response<MenuListResponse> getMenuList() {
    MenuListResponse response = menuManageService.getMenuList();
    return Response.success(response);
}
```

**修改后：**
```java
@GetMapping("/getMenuList")
public Response<MenuListResponse> getMenuList(
        @RequestParam(required = false) Boolean constant,
        @RequestParam(required = false) Boolean status) {
    MenuListRequest request = MenuListRequest.builder()
            .constant(constant)
            .status(status)
            .build();
    MenuListResponse response = menuManageService.getMenuList(request);
    return Response.success(response);
}
```

## 接口使用说明

### 接口地址
```
GET /systemManage/menu/getMenuList
```

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| constant | Boolean | 否 | 常量数据筛选：<br>- `null` 或不传：查询所有菜单<br>- `true`：仅查询常量路由（如登录页、404等）<br>- `false`：仅查询动态路由（业务菜单） |
| status | Boolean | 否 | 状态筛选：<br>- `null` 或不传：查询所有状态的菜单<br>- `true`：仅查询启用的菜单<br>- `false`：仅查询禁用的菜单 |

### 请求示例

#### 1. 查询所有菜单
```
GET /systemManage/menu/getMenuList
```

#### 2. 查询所有启用的菜单
```
GET /systemManage/menu/getMenuList?status=true
```

#### 3. 查询常量路由（启用的）
```
GET /systemManage/menu/getMenuList?constant=true&status=true
```

#### 4. 查询动态路由（启用的）
```
GET /systemManage/menu/getMenuList?constant=false&status=true
```

#### 5. 查询所有禁用的菜���
```
GET /systemManage/menu/getMenuList?status=false
```

### 响应结果

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "current": 1,
    "size": 10,
    "total": 10,
    "records": [
      {
        "id": 1,
        "parentId": 0,
        "menuType": 1,
        "menuName": "首页",
        "routeName": "home",
        "routePath": "/home",
        "constant": false,
        "status": true,
        "children": [...]
      }
    ]
  }
}
```

## 功能特点

1. **向后兼容**：不传参数时查询所有菜单，保持原有功能
2. **灵活筛选**：支持按常量类型和状态单独或组合筛选
3. **动态 SQL**：使用 MyBatis 动态 SQL，参数为 null 时自动忽略该条件
4. **树形结构**：返回的菜单列表仍然保持树形结构

## 测试建议

1. 测试不传参数，验证返回所有菜单
2. 测试 `constant=true`，验证只返回常量路由
3. 测试 `constant=false`，验证只返回动态路由
4. 测试 `status=true`，验证只返回启用的菜单
5. 测试 `status=false`，验证只返回禁用的菜单
6. 测试组合参数，如 `constant=false&status=true`，验证返回启用的动态���由

## 注意事项

- 参数类型为 `Boolean`，支持三态（true/false/null）
- 所有参数都是可选的（`required = false`）
- SQL 查询会自动忽略值为 null 的参数
- 返回结果仍然是树形结构，需要前端正确处理父子关系

