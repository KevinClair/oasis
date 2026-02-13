# 公告管理页面默认加载数据实现说明

## ✅ 功能确认

公告管理页面现在已经正确配置，**打开页面时会自动请求一次列表数据**。

## 🔧 实现原理

### 1. 使用 `useNaivePaginatedTable` Hook

公告管理页面使用了 `useNaivePaginatedTable` hook，该 hook 内部调用 `useTable`，而 `useTable` 有一个关键参数：

```typescript
// packages/hooks/src/use-table.ts
const { immediate = true } = options;

// ...

if (immediate) {
  getData();  // 立即请求数据
}
```

**`immediate` 参数默认值为 `true`**，意味着组件挂载时会自动调用 `getData()` 方法请求数据。

### 2. 修复后的实现

**修改前的问题：**
```typescript
// ❌ 错误：pagination 在初始化时还未定义
const { columns, columnChecks, data, loading, pagination, getData } = useNaivePaginatedTable({
  api: () => fetchGetAnnouncementList({
    current: pagination.page,  // 此时 pagination 还不存在！
    size: pagination.pageSize,
    // ...
  }),
});
```

**修改后的正确实现：**
```typescript
// ✅ 正确：使用独立的 searchParams
const searchParams: Api.SystemManage.AnnouncementSearchParams = reactive({
  current: 1,
  size: 10,
  title: null,
  type: null
});

const { columns, columnChecks, data, loading, mobilePagination, getData } = useNaivePaginatedTable({
  api: () => fetchGetAnnouncementList(searchParams),
  transform: response => defaultTransform(response),
  onPaginationParamsChange: params => {
    // 分页参数变化时更新 searchParams
    searchParams.current = params.page;
    searchParams.size = params.pageSize;
  },
  // ...
});
```

## 📊 数据流程

### 页面加载流程

```
1. 组件挂载
   └─ useNaivePaginatedTable 初始化

2. useTable 执行
   └─ immediate = true (默认)
   └─ 自动调用 getData()

3. getData() 执行
   └─ 调用 api() 函数
   └─ fetchGetAnnouncementList(searchParams)

4. 请求后端接口
   └─ POST /systemManage/announcement/getAnnouncementList
   └─ 参数：{ current: 1, size: 10, title: null, type: null }

5. 接收响应数据
   └─ transform 转换数据
   └─ 更新 data 数组
   └─ 更新 pagination.itemCount

6. 页面渲染
   └─ 显示数据列表
```

### 分页切换流程

```
1. 用户点击分页器
   └─ 触发 onUpdatePage(page)

2. pagination.page 更新
   └─ 触发 onPaginationParamsChange 回调

3. searchParams 更新
   └─ searchParams.current = page

4. 自动重新请求数据
   └─ 调用 getData()
   └─ 使用新的 searchParams
```

## 🎯 关键代码位置

### 文件：`views/manage/announcement/index.vue`

```typescript
// 搜索参数（响应式对象）
const searchParams: Api.SystemManage.AnnouncementSearchParams = reactive({
  current: 1,     // 默认第1页
  size: 10,       // 默认每页10条
  title: null,    // 标题筛选（可选）
  type: null      // 类型筛选（可选）
});

// 使用 hook
const { 
  columns, 
  columnChecks, 
  data, 
  loading, 
  mobilePagination, 
  getData, 
  getDataByPage 
} = useNaivePaginatedTable({
  // API 函数：返回 Promise
  api: () => fetchGetAnnouncementList(searchParams),
  
  // 数据转换：将后端响应转换为表格数据格式
  transform: response => defaultTransform(response),
  
  // 分页参数变化回调：同步更新 searchParams
  onPaginationParamsChange: params => {
    searchParams.current = params.page;
    searchParams.size = params.pageSize;
  },
  
  // 列定义
  columns: () => [
    // ...表格列配置
  ]
});
```

## 📝 API 请求详情

### 请求接口
```
POST /systemManage/announcement/getAnnouncementList
```

### 请求参数（默认）
```json
{
  "current": 1,
  "size": 10,
  "title": null,
  "type": null
}
```

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "current": 1,
    "size": 10,
    "total": 25,
    "records": [
      {
        "id": 1,
        "title": "系统维护通知",
        "content": "系统将于今晚22:00进行维护",
        "type": "warning",
        "createBy": "admin",
        "createTime": "2026-02-13T10:00:00",
        "updateBy": "admin",
        "updateTime": "2026-02-13T10:00:00"
      }
      // ...更多记录
    ]
  }
}
```

## ✅ 验证清单

打开公告管理页面时，应该观察到：

- [ ] 页面立即显示 Loading 状态
- [ ] 自动发起 API 请求（可在浏览器 Network 面板看到）
- [ ] 请求完成后显示数据列表
- [ ] 分页器显示正确的总数和当前页码
- [ ] 点击分页器可以切换页面
- [ ] 每次切换页面都会发起新的请求

## 🔄 对比其他页面

这个实现方式与系统中其他管理页面一致：

| 页面 | searchParams | onPaginationParamsChange | immediate |
|------|-------------|-------------------------|-----------|
| 用户管理 | ✅ | ✅ | ✅ (默认) |
| 角色管理 | ✅ | ✅ | ✅ (默认) |
| 菜单管理 | ✅ | ❌ (无分页) | ✅ (默认) |
| **公告管理** | ✅ | ✅ | ✅ (默认) |

## 🎉 完成状态

- ✅ 页面打开时自动请求列表数据
- ✅ 使用正确的 searchParams 模式
- ✅ 分页功能正常工作
- ✅ 与其他管理页面保持一致
- ✅ 无编译错误
- ✅ 代码结构清晰

**公告管理页面现在会在打开时自动加载数据！** 🎊

---

## 📌 补充说明

### 如果需要禁用自动加载

如果将来某个页面需要禁用自动加载（比如需要用户先选择筛选条件），可以这样：

```typescript
const { ... } = useNaivePaginatedTable({
  // ...其他配置
  immediate: false  // 禁用自动加载
});

// 然后在需要的时候手动调用
onMounted(() => {
  // 或者在某个事件中调用
  getData();
});
```

### 手动刷新数据

如果需要手动刷新数据（比如新增/编辑/删除后），可以调用：

```typescript
// 刷新当前页
await getData();

// 跳转到指定页并刷新
await getDataByPage(1);
```

公告管理页面在操作完成后已经正确调用了这些方法。

