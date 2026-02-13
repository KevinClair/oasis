# getAllPages 接口 - 快速参考

## 🎯 接口作用

获取所有菜单的路由名称列表（平铺数组）

## 📍 使用位置

**前端：** `oasis-web/src/views/manage/menu/index.vue`  
**后端：** `SystemManageController.getAllPages()`

## 🔧 具体用途

### 在菜单管理的新增/编辑弹窗中提供下拉选项：

#### 1. **Page（页面）** 字段
- 选择菜单对应的前端页面组件
- 示例：`user_manage` → `views/manage/user/index.vue`

#### 2. **ActiveMenu（活动菜单）** 字段  
- 隐藏菜单项时，指定哪个菜单应该高亮
- 示例：用户详情页隐藏，但高亮"用户管理"

## 📊 返回数据示例

```json
["home", "user_manage", "role_manage", "menu_manage"]
```

## ❓ 是否可以移除？

### ❌ 不建议移除

**原因：**
- ✅ 提供下拉选择，避免手动输入错误
- ✅ 保证路由名称一致性和准确性
- ✅ `activeMenu` 字段业务逻辑依赖
- ✅ 性能影响极小（< 2KB，< 100ms）

### ✅ 可选优化方案

如果一定要优化，可以：
1. 延迟加载：首次打开弹窗时才请求
2. 复用数据：从菜单列表数据中提取

## 💡 推荐

**保留该接口** - 用户体验与数据准确性 > 微小的性能优化

## 🔍 详细分析

完整分析报告：`GET_ALL_PAGES_ANALYSIS.md`

