package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.dao.MenuDao;
import com.github.kevin.oasis.dao.RoleDao;
import com.github.kevin.oasis.dao.RoleMenuDao;
import com.github.kevin.oasis.dao.UserDao;
import com.github.kevin.oasis.dao.UserRoleDao;
import com.github.kevin.oasis.models.entity.Menu;
import com.github.kevin.oasis.models.entity.Role;
import com.github.kevin.oasis.models.entity.User;
import com.github.kevin.oasis.models.vo.route.MenuRoute;
import com.github.kevin.oasis.models.vo.route.RouteMeta;
import com.github.kevin.oasis.models.vo.route.UserRouteResponse;
import com.github.kevin.oasis.services.RouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 路由服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteServiceImpl implements RouteService {

    private final MenuDao menuDao;
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final RoleMenuDao roleMenuDao;
    private final UserRoleDao userRoleDao;

    @Override
    public List<MenuRoute> getConstantRoutes() {
        log.info("获取常量路由");

        // 查询所有constant=true且status=true的菜单
        List<Menu> constantMenus = menuDao.selectMenuList(true, true);

        // 转换为MenuRoute格式并构建树形结构
        List<MenuRoute> routes = buildMenuRouteTree(constantMenus, null);

        log.info("获取常量路由成功，数量：{}", routes.size());

        return routes;
    }

    @Override
    public UserRouteResponse getUserRoutes(Long userId) {
        log.info("获取用户动态路由，userId={}", userId);

        // 查询用户信息
        User user = userDao.selectById(userId);
        if (user == null) {
            log.warn("用户不存在，userId={}", userId);
            return UserRouteResponse.builder()
                    .routes(new ArrayList<>())
                    .home("home")
                    .build();
        }

        // 从关联表查询用户的角色ID列表（只查询启用的角色）
        List<Long> roleIds = userRoleDao.selectRoleIdsByUserId(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            log.warn("用户没有分配角色或角色已禁用，userId={}", userId);
            return UserRouteResponse.builder()
                    .routes(new ArrayList<>())
                    .home("home")
                    .build();
        }

        // 查询角色信息并获取角色编码（用于前端权限控制）
        List<String> roleCodes = new ArrayList<>();
        for (Long roleId : roleIds) {
            Role role = roleDao.selectById(roleId);
            if (role != null && role.getRoleCode() != null) {
                roleCodes.add(role.getRoleCode());
            }
        }

        // 获取用户所有角色绑定的菜单ID
        Set<Long> menuIds = new HashSet<>();
        for (Long roleId : roleIds) {
            List<Long> roleMenuIds = roleMenuDao.selectMenuIdsByRoleId(roleId);
            menuIds.addAll(roleMenuIds);
        }

        if (menuIds.isEmpty()) {
            log.warn("用户角色没有绑定任何菜单，userId={}", userId);
            return UserRouteResponse.builder()
                    .routes(new ArrayList<>())
                    .home("home")
                    .build();
        }

        // 查询所有非常量且启用的菜单
        List<Menu> allMenus = menuDao.selectMenuList(false, true);

        // 过滤出用户有权限的菜单（包括父级菜单）
        Set<Long> accessibleMenuIds = new HashSet<>(menuIds);
        for (Long menuId : menuIds) {
            addParentMenuIds(allMenus, menuId, accessibleMenuIds);
        }

        List<Menu> userMenus = allMenus.stream()
                .filter(menu -> accessibleMenuIds.contains(menu.getId()))
                .filter(menu -> menu.getStatus() != null && menu.getStatus())
                .filter(menu -> menu.getConstant() == null || !menu.getConstant()) // 排除常量路由
                .collect(Collectors.toList());

        // 转换为MenuRoute格式并构建树形结构
        List<MenuRoute> routes = buildMenuRouteTree(userMenus, roleCodes);

        // 确定首页
        String home = "home"; // 默认首页
        Optional<Menu> homeMenu = userMenus.stream()
                .filter(menu -> "home".equals(menu.getRouteName()))
                .findFirst();
        if (homeMenu.isPresent()) {
            home = homeMenu.get().getRouteName();
        }

        UserRouteResponse response = UserRouteResponse.builder()
                .routes(routes)
                .home(home)
                .build();

        log.info("获取用户动态路由成功，userId={}，路由数量={}", userId, routes.size());

        return response;
    }

    @Override
    public boolean isRouteExist(String routeName) {
        log.info("检查路由是否存在，routeName={}", routeName);

        if (routeName == null || routeName.isEmpty()) {
            return false;
        }

        Menu menu = menuDao.selectByRouteName(routeName);
        boolean exists = menu != null;

        log.info("路由存在性检查结果，routeName={}，exists={}", routeName, exists);

        return exists;
    }

    /**
     * 添加父级菜单ID
     */
    private void addParentMenuIds(List<Menu> allMenus, Long menuId, Set<Long> result) {
        Menu menu = allMenus.stream()
                .filter(m -> m.getId().equals(menuId))
                .findFirst()
                .orElse(null);

        if (menu != null && menu.getParentId() != null && menu.getParentId() > 0) {
            result.add(menu.getParentId());
            addParentMenuIds(allMenus, menu.getParentId(), result);
        }
    }

    /**
     * 构建菜单路由树形结构
     */
    private List<MenuRoute> buildMenuRouteTree(List<Menu> menus, List<String> roles) {
        Map<Long, MenuRoute> routeMap = new HashMap<>();
        List<MenuRoute> rootRoutes = new ArrayList<>();

        // 先创建所有路由节点
        for (Menu menu : menus) {
            MenuRoute route = convertToMenuRoute(menu, roles);
            routeMap.put(menu.getId(), route);
        }

        // 构建树形结构
        for (Menu menu : menus) {
            MenuRoute route = routeMap.get(menu.getId());
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                // 根节点
                rootRoutes.add(route);
            } else {
                // 子节点
                MenuRoute parentRoute = routeMap.get(menu.getParentId());
                if (parentRoute != null) {
                    if (parentRoute.getChildren() == null) {
                        parentRoute.setChildren(new ArrayList<>());
                    }
                    parentRoute.getChildren().add(route);
                } else {
                    // 如果父节点不在列表中，作为根节点处理
                    rootRoutes.add(route);
                }
            }
        }

        // 按order排序
        sortRoutes(rootRoutes);

        return rootRoutes;
    }

    /**
     * 递归排序路由
     */
    private void sortRoutes(List<MenuRoute> routes) {
        if (routes == null || routes.isEmpty()) {
            return;
        }

        routes.sort((r1, r2) -> {
            Integer order1 = r1.getMeta() != null ? r1.getMeta().getOrder() : 0;
            Integer order2 = r2.getMeta() != null ? r2.getMeta().getOrder() : 0;
            if (order1 == null) order1 = 0;
            if (order2 == null) order2 = 0;
            return order1.compareTo(order2);
        });

        for (MenuRoute route : routes) {
            if (route.getChildren() != null && !route.getChildren().isEmpty()) {
                sortRoutes(route.getChildren());
            }
        }
    }

    /**
     * 转换Menu为MenuRoute
     */
    private MenuRoute convertToMenuRoute(Menu menu, List<String> roles) {
        RouteMeta meta = RouteMeta.builder()
                .title(menu.getMenuName())
                .i18nKey(menu.getI18nKey())
                .order(menu.getOrder())
                .icon(menu.getIcon())
                .localIcon(menu.getLocalIcon())
                .keepAlive(menu.getKeepAlive())
                .constant(menu.getConstant())
                .href(menu.getHref())
                .hideInMenu(menu.getHideInMenu())
                .activeMenu(menu.getActiveMenu())
                .multiTab(menu.getMultiTab())
                .fixedIndexInTab(menu.getFixedIndexInTab())
                .roles(roles)
                .build();

        return MenuRoute.builder()
                .id(String.valueOf(menu.getId()))
                .name(menu.getRouteName())
                .path(menu.getRoutePath())
                .component(menu.getComponent())
                .meta(meta)
                .build();
    }
}

