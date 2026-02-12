# 动态路由 403 修复 - 快速参考

## 一句话总结
在动态路由模式下，后端已过滤权限，前端路由守卫跳过角色检查，避免 403 错误。

## 修复内容

**文件：** `src/router/guard/route.ts`

**修改前：**
```typescript
const hasAuth = authStore.isStaticSuper || !routeRoles.length || hasRole;
```

**修改后：**
```typescript
// In dynamic mode, backend already filters routes by permissions, so skip role checking
const authRouteMode = import.meta.env.VITE_AUTH_ROUTE_MODE;
const isDynamicMode = authRouteMode === 'dynamic';

const routeRoles = to.meta.roles || [];
const hasRole = authStore.userInfo.roles.some(role => routeRoles.includes(role));
// In dynamic mode, if route exists in router, user has permission (backend filtered it)
// In static mode, check roles as before
const hasAuth = isDynamicMode ? true : authStore.isStaticSuper || !routeRoles.length || hasRole;
```

## 原理

### 动态模式（当前使用）
- ✅ 后端 `/route/getUserRoutes` 返回已授权的路由
- ✅ 前端信任后端过滤结果，不再检查角色
- ✅ 避免因前后端角色数据不一致导致的 403

### 静态模式
- 🔒 前端自行根据角色过滤路由
- 🔒 路由守卫继续检查角色权限

## 验证方法

1. 登录系统
2. 访问从 `/route/getUserRoutes` 返回的任何页面
3. 应该正常显示，不会跳转到 403 页面

## 相关配置

`.env` 文件中的路由模式设置：
```env
VITE_AUTH_ROUTE_MODE=dynamic
```

