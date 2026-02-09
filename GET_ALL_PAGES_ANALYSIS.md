# getAllPages 接口分析文档

## 概述

`systemManage/getAllPages` 接口在菜单管理功能中扮演着关键角色，用于获取项目中所有可用的页面组件列表。

## 接口定义

### 前端API调用

**位置**: `/oasis-web/src/service/api/system-manage.ts`

```typescript
/** get all pages */
export function fetchGetAllPages() {
    return request<string[]>({
        url: '/systemManage/getAllPages',
        method: 'get'
    });
}
```

**返回类型**: `string[]` - 字符串数组，包含所有页面组件的名称

## 使用场景

### 1. 菜单管理页面初始化

**位置**: `/oasis-web/src/views/manage/menu/index.vue`

```vue
const allPages = ref
<string[]>([]);

async function getAllPages() {
const { data: pages } = await fetchGetAllPages();
allPages.value = pages || [];
}

function init() {
getAllPages();
}

// 页面加载时自动调用
init();
```

**作用**: 在菜单管理页面打开时，立即调用该接口获取所有可用的页面组件列表。

### 2. 传递给菜单操作Modal

```vue

<MenuOperateModal
    v-model:visible="visible"
    :operate-type="operateType"
    :row-data="editingData"
    :all-pages="allPages"
    @submitted="getDataByPage"
/>
```

**作用**: 将获取到的页面列表传递给菜单操作对话框。

### 3. 在菜单操作Modal中使用

**位置**: `/oasis-web/src/views/manage/menu/modules/menu-operate-modal.vue`

```typescript
const pageOptions = computed(() => {
    const allPages = [...props.allPages];

    // 如果当前路由名称不在列表中，则添加到最前面
    if (model.value.routeName && !allPages.includes(model.value.routeName)) {
        allPages.unshift(model.value.routeName);
    }

    // 转换为下拉选项格式
    const opts: CommonType.Option[] = allPages.map(page => ({
        label: page,
        value: page
    }));

    return opts;
});
```

**作用**: 构建"页面组件"下拉选择框的选项列表。

## 业务流程

### 完整流程图

```
1. 用户打开菜单管理页面
   ↓
2. 自动调用 fetchGetAllPages()
   ↓
3. 后端返回所有页面组件列表
   例如: ["home", "manage_user", "manage_role", "manage_menu", ...]
   ↓
4. 存储到 allPages 变量中
   ↓
5. 用户点击"新增菜单"或"编辑菜单"
   ↓
6. 打开 MenuOperateModal 对话框
   ↓
7. 根据菜单类型显示不同的表单字段:
   - menuType === 1 (目录): 显示 Layout 字段
   - menuType === 2 (菜单): 显示 Page 字段
   ↓
8. Page 字段的下拉选项就是从 allPages 生成的
   ↓
9. 用户选择一个页面组件
   ↓
10. 提交时，将 layout 和 page 转换为 component 字段
    格式: "layout.base$view.manage_user"
```

## 页面组件（Page）的作用

### 1. **页面组件选择**

在新增/编辑菜单时，如果菜单类型是"菜单"（menuType = 2），会显示"页面组件"下拉选择框：

```vue

<NFormItemGi v-if="showPage" span="24 m:12" :label="$t('page.manage.menu.page')" path="page">
  <NSelect
      v-model:value="model.page"
      :options="pageOptions"
      :placeholder="$t('page.manage.menu.form.page')"
  />
</NFormItemGi>
```

**显示条件**: `showPage = computed(() => model.value.menuType === 2)`

### 2. **组件路径生成**

选择的页面组件会被转换为完整的组件路径：

```typescript
export function transformLayoutAndPageToComponent(layout: string, page: string) {
    const hasLayout = Boolean(layout);
    const hasPage = Boolean(page);

    if (hasLayout && hasPage) {
        // 一级菜单: layout.base$view.home
        return `${LAYOUT_PREFIX}${layout}${FIRST_LEVEL_ROUTE_COMPONENT_SPLIT}${VIEW_PREFIX}${page}`;
    }

    if (hasLayout) {
        // 只有layout: layout.base
        return `${LAYOUT_PREFIX}${layout}`;
    }

    if (hasPage) {
        // 只有page: view.manage_user
        return `${VIEW_PREFIX}${page}`;
    }

    return '';
}
```

**组件格式规则**:

- 前缀: `layout.` 表示布局组件，`view.` 表示页面组件
- 分隔符: `$` 用于分隔布局和页面
- 示例:
    - 顶级菜单（有布局）: `layout.base$view.home`
    - 子菜单（无布局）: `view.manage_user`
    - 纯目录: `layout.base`

### 3. **页面路由映射**

这个 component 字段会被用于动态路由生成，告诉前端路由系统应该加载哪个Vue组件。

## 实际应用示例

### 示例1: 创建顶级菜单

```javascript
{
    menuType: 1,          // 目录
        menuName
:
    "系统管理",
        routeName
:
    "manage",
        layout
:
    "base",       // 选择 base 布局
        page
:
    "",             // 目录不需要页面
        component
:
    "layout.base"  // 最终生成
}
```

### 示例2: 创建二级菜单

```javascript
{
    menuType: 2,          // 菜单
        menuName
:
    "用户管理",
        routeName
:
    "manage_user",
        parentId
:
    1,          // 属于"系统管理"
        layout
:
    "",           // 子菜单不需要布局
        page
:
    "manage_user",  // 从 allPages 中选择
        component
:
    "view.manage_user"  // 最终生成
}
```

### 示例3: 页面组件列表示例

假设 `getAllPages()` 返回：

```javascript
[
    "home",
    "manage_user",
    "manage_role",
    "manage_menu",
    "about",
    // ... 更多页面
]
```

这些就是项目中 `/src/views/` 目录下所有可用的页面组件。

## 后端实现建议

### 需要实现的接口

**位置**: `/oasis-admin/src/main/java/com/github/kevin/oasis/controller/SystemManageController.java`

```java
/**
 * 获取所有页面组件列表
 *
 * @return 页面组件名称列表
 */
@GetMapping("/getAllPages")
@Permission
public Response<List<String>> getAllPages() {
    // 实现方式1: 硬编码返回
    List<String> pages = Arrays.asList(
            "home",
            "manage_user",
            "manage_role",
            "manage_menu",
            "about"
    );

    // 实现方式2: 从文件系统扫描（推荐）
    // List<String> pages = scanViewComponents();

    // 实现方式3: 从数据库配置表读取
    // List<String> pages = pageService.getAllPages();

    return Response.success(pages);
}
```

### 页面组件命名规则

页面组件名称应该与：

1. **路由名称**对应: `manage_user` → 路由名
2. **文件路径**对应: `src/views/manage/user/index.vue` → `manage_user`
3. **下划线分隔**: 多层级使用下划线连接

## 关键点总结

### 1. **为什么需要这个接口？**

- 提供页面组件的可选列表，方便管理员在创建菜单时选择
- 避免手动输入错误的组件名称
- 确保选择的页面组件实际存在于项目中

### 2. **何时调用？**

- 菜单管理页面加载时自动调用
- 只调用一次，结果缓存在前端

### 3. **数据如何使用？**

- 转换为下拉选择框的选项
- 仅在创建/编辑"菜单"类型（menuType=2）时显示
- 选择的page会与layout组合生成最终的component字段

### 4. **component字段的重要性**

- 决定了路由访问时加载哪个Vue组件
- 支持布局嵌套（一级路由有layout，子路由只有view）
- 遵循固定的命名格式：`layout.xxx$view.xxx` 或 `view.xxx`

## 当前状态

⚠️ **注意**: 目前后端尚未实现 `/systemManage/getAllPages` 接口，需要添加实现。

**建议实现方案**:

1. 简单版本：返回硬编码的页面列表
2. 完整版本：扫描 `src/views/` 目录，自动生成页面列表
3. 数据库版本：从配置表中读取可用页面列表

---

**文档日期**: 2026-02-09  
**相关文件**:

- `/oasis-web/src/service/api/system-manage.ts`
- `/oasis-web/src/views/manage/menu/index.vue`
- `/oasis-web/src/views/manage/menu/modules/menu-operate-modal.vue`
- `/oasis-web/src/views/manage/menu/modules/shared.ts`

