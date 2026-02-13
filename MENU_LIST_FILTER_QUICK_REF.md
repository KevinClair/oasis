# 菜单列表筛选功能 - 快速参考

## 修改概览

为 `/systemManage/menu/getMenuList` 接口增加常量数据和状态筛选参数。

## 修改的文件

1. ✅ **MenuListRequest.java** (新建) - 查询请求参数类
2. ✅ **MenuDao.java** - 新增 `selectMenuList` 方法
3. ✅ **MenuMapper.xml** - 新增带筛选条件的 SQL 查询
4. ✅ **MenuManageService.java** - 更新接口签名
5. ✅ **MenuManageServiceImpl.java** - 更新实现逻辑
6. ✅ **MenuManageController.java** - 接收查询参数

## 接口使用

### 基本格式
```
GET /systemManage/menu/getMenuList?constant={true|false}&status={true|false}
```

### 参数说明

| 参数 | 说明 | 示例 |
|------|------|------|
| constant | null=全部<br>true=常量路由<br>false=动态路由 | `constant=false` |
| status | null=全部<br>true=启用<br>false=禁用 | `status=true` |

### 常用示例

```bash
# 查询所有菜单
GET /systemManage/menu/getMenuList

# 查询启用的动态路由
GET /systemManage/menu/getMenuList?constant=false&status=true

# 查询常量路由
GET /systemManage/menu/getMenuList?constant=true

# 查询禁用的菜单
GET /systemManage/menu/getMenuList?status=false
```

## SQL 逻辑

```xml
<where>
    <if test="request.constant != null">
        AND constant = #{request.constant}
    </if>
    <if test="request.status != null">
        AND status = #{request.status}
    </if>
</where>
```

- 参数为 null 时，不添加该筛选条件
- 支持单独或组合使用两个参数
- 结果按 `parent_id, order, create_time` 排序

## 注意事项

- ✅ 所有参数都是可选的
- ✅ 返回结果保持树形结构
- ✅ 向后兼容（不传参数返回所有）
- ✅ 无编译错误

