# 动态路由 403 权限问题修复

## 问题描述

当使用动态路由模式（`VITE_AUTH_ROUTE_MODE=dynamic`）时，用户登录后 `/route/getUserRoutes` 接口已经返回了有权限的页面，但打开这些页面时仍然提示 403 无权限错误。

## 问题原因

路由守卫（`src/router/guard/route.ts`）中的权限检查逻辑存在问题：

```typescript
const routeRoles = to.meta.roles || [];
const hasRole = authStore.userInfo.roles.some(role => routeRoles.includes(role));
const hasAuth = authStore.isStaticSuper || !routeRoles.length || hasRole;
```

在**动态路由模式**下：
- 后端 `/route/getUserRoutes` 接口已经根据用户权限过滤了路由，只返回用户有权访问的路由
- 前端将这些路由注册到 Vue Router 后，这些路由理论上用户都有权访问
- 但是前端路由守卫仍然在进行角色检查（`hasRole`），导致即使路由已经被后端授权，前端仍然可能因为角色检查失败而跳转到 403 页面

## 解决方案

修改路由守卫逻辑，区分静态路由模式和动态路由模式：

### 修改文件：`src/router/guard/route.ts`

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

### 核心逻辑

1. **动态模式（dynamic）**：
   - 后端已经完成了权限过滤
   - 前端路由守卫不再检查角色权限（`hasAuth = true`）
   - 只要路由存在于 Vue Router 中，用户就有权访问

2. **静态模式（static）**：
   - 保持原有的角色检查逻辑
   - 前端根据用户角色过滤路由
   - 路由守卫继续检查用户角色权限

## 测试验证

1. 确保 `.env` 中设置为动态路由模式：
   ```env
   VITE_AUTH_ROUTE_MODE=dynamic
   ```

2. 登录系统后，访问通过 `/route/getUserRoutes` 返回的任何路由

3. 应该能够正常访问，不会再出现 403 错误

## 注意事项

- 此修复仅适用于动态路由模式
- 在动态路由模式下，**必须确保后端正确实现了权限过滤**
- 后端应该只返回用户有权访问的路由
- 如果后端权限过滤不正确，可能会导致安全问题

## 相关文件

- `src/router/guard/route.ts` - 路由守卫（已修改）
- `src/store/modules/route/index.ts` - 路由状态管理
- `src/service/api/route.ts` - 路由相关 API
- `.env` - 环境配置（路由模式设置）

