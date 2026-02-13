package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.dao.MenuDao;
import com.github.kevin.oasis.dao.RoleMenuDao;
import com.github.kevin.oasis.models.entity.Menu;
import com.github.kevin.oasis.models.vo.systemManage.*;
import com.github.kevin.oasis.services.MenuManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuManageServiceImpl implements MenuManageService {

    private final MenuDao menuDao;
    private final RoleMenuDao roleMenuDao;

    @Override
    public MenuListResponse getMenuList(MenuListRequest request) {
        log.info("查询菜单列表，参数：{}", request);

        // 如果没有传参数，创建默认请求（查询所有）
        if (request == null) {
            request = MenuListRequest.builder().build();
        }

        // 查询菜单列表（根据参数筛选）
        List<Menu> allMenus = menuDao.selectMenuList(request.getConstant(), request.getStatus());

        // 转换为VO并构建树形结构
        List<MenuVO> menuVOList = buildMenuTree(allMenus);

        MenuListResponse response = MenuListResponse.builder()
                .current(1)
                .size(menuVOList.size())
                .total((long) menuVOList.size())
                .records(menuVOList)
                .build();

        log.info("查询菜单列表成功，顶层菜单数量：{}���总菜单数：{}", menuVOList.size(), allMenus.size());

        return response;
    }

    /**
     * 构建菜单树形结构
     *
     * @param allMenus 所有菜单列表
     * @return 树形菜单列表（只返回顶级菜单，子菜单在children中）
     */
    private List<MenuVO> buildMenuTree(List<Menu> allMenus) {
        // 转换所有菜单为VO
        List<MenuVO> allMenuVOs = allMenus.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建树形结构（只返回顶级菜单）
        return allMenuVOs.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .peek(menu -> buildChildren(menu, allMenuVOs))
                .collect(Collectors.toList());
    }

    /**
     * 递归构建子菜单
     *
     * @param parent   父菜单
     * @param allMenus 所有菜单列表
     */
    private void buildChildren(MenuVO parent, List<MenuVO> allMenus) {
        List<MenuVO> children = allMenus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId().equals(parent.getId()))
                .peek(child -> buildChildren(child, allMenus))
                .collect(Collectors.toList());

        // 设置子菜单列表（如果没有子菜单则设为null）
        parent.setChildren(children.isEmpty() ? null : children);
    }

    /**
     * 转换Menu实体为MenuVO
     */
    private MenuVO convertToVO(Menu menu) {
        return MenuVO.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .menuType(menu.getMenuType())
                .menuName(menu.getMenuName())
                .routeName(menu.getRouteName())
                .routePath(menu.getRoutePath())
                .pathParam(menu.getPathParam())
                .component(menu.getComponent())
                .iconType(menu.getIconType())
                .icon(menu.getIcon())
                .localIcon(menu.getLocalIcon())
                .i18nKey(menu.getI18nKey())
                .order(menu.getOrder())
                .keepAlive(menu.getKeepAlive())
                .constant(menu.getConstant())
                .href(menu.getHref())
                .hideInMenu(menu.getHideInMenu())
                .activeMenu(menu.getActiveMenu())
                .multiTab(menu.getMultiTab())
                .fixedIndexInTab(menu.getFixedIndexInTab())
                .status(menu.getStatus())
                .createBy(menu.getCreateBy())
                .createTime(menu.getCreateTime())
                .updateBy(menu.getUpdateBy())
                .updateTime(menu.getUpdateTime())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveMenu(MenuSaveRequest request) {
        log.info("保存菜单，参数：{}", request);

        // 检查路由名称是否已存在
        Menu existingMenu = menuDao.selectByRouteName(request.getRouteName());
        if (existingMenu != null && !existingMenu.getId().equals(request.getId())) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "路由名称已存在");
        }

        if (request.getId() == null) {
            // 新增
            Menu menu = Menu.builder()
                    .parentId(request.getParentId() != null ? request.getParentId() : 0L)
                    .menuType(request.getMenuType())
                    .menuName(request.getMenuName())
                    .routeName(request.getRouteName())
                    .routePath(request.getRoutePath())
                    .pathParam(request.getPathParam())
                    .component(request.getComponent())
                    .iconType(request.getIconType())
                    .icon(request.getIcon())
                    .localIcon(request.getLocalIcon())
                    .i18nKey(request.getI18nKey())
                    .order(request.getOrder() != null ? request.getOrder() : 0)
                    .keepAlive(request.getKeepAlive() != null ? request.getKeepAlive() : false)
                    .constant(request.getConstant() != null ? request.getConstant() : false)
                    .href(request.getHref())
                    .hideInMenu(request.getHideInMenu() != null ? request.getHideInMenu() : false)
                    .activeMenu(request.getActiveMenu())
                    .multiTab(request.getMultiTab() != null ? request.getMultiTab() : false)
                    .fixedIndexInTab(request.getFixedIndexInTab())
                    .status(request.getStatus() != null ? request.getStatus() : true)
                    .createBy("admin") // TODO: 从当前登录用户获取
                    .updateBy("admin")
                    .build();

            menuDao.insert(menu);
            log.info("新增菜单成功，菜单ID：{}", menu.getId());
            return menu.getId();
        } else {
            // 编辑
            Menu menu = menuDao.selectById(request.getId());
            if (menu == null) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "菜单不存在");
            }

            menu.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
            menu.setMenuType(request.getMenuType());
            menu.setMenuName(request.getMenuName());
            menu.setRouteName(request.getRouteName());
            menu.setRoutePath(request.getRoutePath());
            menu.setPathParam(request.getPathParam());
            menu.setComponent(request.getComponent());
            menu.setIconType(request.getIconType());
            menu.setIcon(request.getIcon());
            menu.setLocalIcon(request.getLocalIcon());
            menu.setI18nKey(request.getI18nKey());
            menu.setOrder(request.getOrder());
            menu.setKeepAlive(request.getKeepAlive());
            menu.setConstant(request.getConstant());
            menu.setHref(request.getHref());
            menu.setHideInMenu(request.getHideInMenu());
            menu.setActiveMenu(request.getActiveMenu());
            menu.setMultiTab(request.getMultiTab());
            menu.setFixedIndexInTab(request.getFixedIndexInTab());
            menu.setStatus(request.getStatus());
            menu.setUpdateBy("admin"); // TODO: 从当前登录用户获取

            menuDao.update(menu);
            log.info("更新菜单成功，菜单ID：{}", menu.getId());
            return menu.getId();
        }
    }

    @Override
    public MenuVO getMenuById(Long id) {
        log.info("查询菜单详情，菜单ID：{}", id);

        Menu menu = menuDao.selectById(id);
        if (menu == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "菜单不存在");
        }

        MenuVO menuVO = convertToVO(menu);

        log.info("查询菜单详情成功");

        return menuVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMenus(MenuDeleteRequest request) {
        log.info("删除菜单，参数：{}", request);

        if (request.getIds() == null || request.getIds().isEmpty()) {
            log.warn("删除菜单失败：菜单ID列表为空");
            return 0;
        }

        // 检查是否有子菜单
        for (Long id : request.getIds()) {
            List<Menu> children = menuDao.selectByParentId(id);
            if (!children.isEmpty()) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "存在子菜单，无法删除");
            }
        }

        // 先删除角色菜单关联
        roleMenuDao.deleteByMenuIds(request.getIds());
        log.info("删除菜单关联成功，菜单ID列表：{}", request.getIds());

        // 批量删除菜单
        int deletedCount = menuDao.deleteMenusByIds(request.getIds());

        log.info("删除菜单成功，删除数量：{}", deletedCount);

        return deletedCount;
    }

    @Override
    public List<String> getAllPages() {
        log.info("获取所有菜单路由路径");

        // 查询所有非常量且启用的菜单
        List<Menu> allMenus = menuDao.selectMenuList(null, null);

        // 提取所有路由路径并平铺返回
        List<String> pages = allMenus.stream()
                .filter(menu -> menu.getRoutePath() != null && !menu.getRoutePath().isEmpty())
                .map(Menu::getRouteName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        log.info("获取所有菜单路由路径成功，数量：{}", pages.size());

        return pages;
    }
}

