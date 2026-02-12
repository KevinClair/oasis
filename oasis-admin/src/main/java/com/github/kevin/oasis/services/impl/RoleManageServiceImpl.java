package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.dao.RoleDao;
import com.github.kevin.oasis.dao.RoleMenuDao;
import com.github.kevin.oasis.models.entity.Role;
import com.github.kevin.oasis.models.entity.RoleMenu;
import com.github.kevin.oasis.models.vo.systemManage.*;
import com.github.kevin.oasis.services.RoleManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleManageServiceImpl implements RoleManageService {

    private final RoleDao roleDao;
    private final RoleMenuDao roleMenuDao;

    @Override
    public RoleListResponse getRoleList(RoleListRequest request) {
        log.info("查询角色列表，参数：{}", request);

        // 计算分页参数
        int offset = 0;
        if (request.getCurrent() != null && request.getSize() != null) {
            offset = (request.getCurrent() - 1) * request.getSize();
        }
        request.setCurrent(offset);

        // 查询角色列表
        List<Role> roleList = roleDao.selectRoleList(request);

        // 查询总数
        Long total = roleDao.countRoleList(request);

        // 转换为VO
        List<RoleVO> roleVOList = roleList.stream().map(role ->
                RoleVO.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .roleCode(role.getRoleCode())
                        .roleDesc(role.getRoleDesc())
                        .status(role.getStatus())
                        .createBy(role.getCreateBy())
                        .createTime(role.getCreateTime())
                        .updateBy(role.getUpdateBy())
                        .updateTime(role.getUpdateTime())
                        .build()
        ).collect(Collectors.toList());

        // 构建响应
        RoleListResponse response = RoleListResponse.builder()
                .current(request.getCurrent() / request.getSize() + 1)
                .size(request.getSize())
                .total(total)
                .records(roleVOList)
                .build();

        log.info("查询角色列表成功，总数：{}，当前页：{}，每页大小：{}", total, response.getCurrent(), response.getSize());

        return response;
    }

    @Override
    public Long saveRole(RoleSaveRequest request) {
        log.info("保存角色，参数：{}", request);

        // 检查角色编码是否已存在
        Role existingRole = roleDao.selectByRoleCode(request.getRoleCode());
        if (existingRole != null && !existingRole.getId().equals(request.getId())) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "角色编码已存在");
        }

        if (request.getId() == null) {
            // 新增
            Role role = Role.builder()
                    .roleName(request.getRoleName())
                    .roleCode(request.getRoleCode())
                    .roleDesc(request.getRoleDesc())
                    .status(request.getStatus() != null ? request.getStatus() : true)
                    .createBy("admin") // TODO: 从当前登录用户获取
                    .updateBy("admin")
                    .build();

            roleDao.insert(role);
            log.info("新增角色成功，角色ID：{}", role.getId());
            return role.getId();
        } else {
            // 编辑
            Role role = roleDao.selectById(request.getId());
            if (role == null) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "角色不存在");
            }

            role.setRoleName(request.getRoleName());
            role.setRoleCode(request.getRoleCode());
            role.setRoleDesc(request.getRoleDesc());
            role.setStatus(request.getStatus());
            role.setUpdateBy("admin"); // TODO: 从当前登录用户获取

            roleDao.update(role);
            log.info("更新角色成功，角色ID：{}", role.getId());
            return role.getId();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRoles(RoleDeleteRequest request) {
        log.info("删除角色，参数：{}", request);

        if (request.getIds() == null || request.getIds().isEmpty()) {
            log.warn("删除角色失败：角色ID列表为空");
            return 0;
        }

        // 先删除角色菜单关联
        roleMenuDao.deleteByRoleIds(request.getIds());

        // 批量删除角色
        int deletedCount = roleDao.deleteRolesByIds(request.getIds());

        log.info("删除角色成功，删除数量：{}", deletedCount);

        return deletedCount;
    }

    @Override
    public int toggleRoleStatus(RoleToggleStatusRequest request) {
        log.info("切换角色状态，参数：{}", request);

        if (request.getIds() == null || request.getIds().isEmpty()) {
            log.warn("切换角色状态失败：角色ID列表为空");
            return 0;
        }

        // 批量切换角色状态
        int updatedCount = roleDao.toggleRoleStatus(request.getIds());

        log.info("切换角色状态成功，更新数量：{}", updatedCount);

        return updatedCount;
    }

    @Override
    public RoleVO getRoleById(Long id) {
        log.info("查询角色详情，角色ID：{}", id);

        Role role = roleDao.selectById(id);
        if (role == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "角色不存在");
        }

        RoleVO roleVO = RoleVO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .roleCode(role.getRoleCode())
                .roleDesc(role.getRoleDesc())
                .status(role.getStatus())
                .createBy(role.getCreateBy())
                .createTime(role.getCreateTime())
                .updateBy(role.getUpdateBy())
                .updateTime(role.getUpdateTime())
                .build();

        log.info("查询角色详情成功");

        return roleVO;
    }

    @Override
    public List<RoleVO> getAllEnabledRoles() {
        log.info("查询所有启用的角色");

        List<Role> roles = roleDao.selectAllEnabledRoles();

        List<RoleVO> roleVOList = roles.stream().map(role ->
                RoleVO.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .roleCode(role.getRoleCode())
                        .roleDesc(role.getRoleDesc())
                        .status(role.getStatus())
                        .createBy(role.getCreateBy())
                        .createTime(role.getCreateTime())
                        .updateBy(role.getUpdateBy())
                        .updateTime(role.getUpdateTime())
                        .build()
        ).collect(Collectors.toList());

        log.info("查询所有启用角色成功，数量：{}", roleVOList.size());

        return roleVOList;
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        log.info("查询角色绑定的菜单ID列表，角色ID：{}", roleId);

        List<Long> menuIds = roleMenuDao.selectMenuIdsByRoleId(roleId);

        log.info("查询角色菜单成功，菜单数量：{}", menuIds.size());

        return menuIds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveRoleMenus(RoleMenuSaveRequest request) {
        log.info("保存角色菜单权限，参数：{}", request);

        Long roleId = request.getRoleId();
        List<Long> menuIds = request.getMenuIds();

        // 校验角色是否存在
        Role role = roleDao.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "角色不存在");
        }

        // 先删除该角色的所有菜单关联
        roleMenuDao.deleteByRoleId(roleId);

        // 如果菜单ID列表为空，则不插入
        if (menuIds == null || menuIds.isEmpty()) {
            log.info("菜单ID列表为空，只删除旧关联");
            return 0;
        }

        // 批量插入新的角色菜单关联
        List<RoleMenu> roleMenuList = new ArrayList<>();
        for (Long menuId : menuIds) {
            RoleMenu roleMenu = RoleMenu.builder()
                    .roleId(roleId)
                    .menuId(menuId)
                    .createBy("admin") // TODO: 从当前登录用户获取
                    .build();
            roleMenuList.add(roleMenu);
        }

        int count = roleMenuDao.batchInsert(roleMenuList);

        log.info("保存角色菜单权限成功，插入数量：{}", count);

        return count;
    }
}

