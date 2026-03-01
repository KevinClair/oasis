package com.github.kevin.oasis.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.dao.ApplicationDao;
import com.github.kevin.oasis.dao.ApplicationRegistrationDao;
import com.github.kevin.oasis.dao.UserDao;
import com.github.kevin.oasis.models.entity.Application;
import com.github.kevin.oasis.models.entity.ApplicationRegistration;
import com.github.kevin.oasis.models.entity.User;
import com.github.kevin.oasis.models.vo.application.*;
import com.github.kevin.oasis.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用管理服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationDao applicationDao;
    private final ApplicationRegistrationDao applicationRegistrationDao;
    private final UserDao userDao;
    private final ObjectMapper objectMapper;

    @Override
    public ApplicationListResponse getApplicationList(ApplicationListRequest request) {
        log.info("查询应用列表，参数：{}", request);

        List<Application> applications = applicationDao.selectApplicationList(request);
        Long total = applicationDao.countApplicationList(request);

        // 转换为VO
        List<ApplicationVO> voList = applications.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return ApplicationListResponse.builder()
                .records(voList)
                .current(request.getCurrent())
                .size(request.getSize())
                .total(total)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveApplication(ApplicationSaveRequest request, String currentUserId) {
        log.info("保存应用，参数：{}", request);

        // 验证应用Code唯一性
        Application existingApp = applicationDao.selectByAppCode(request.getAppCode());
        if (existingApp != null && !existingApp.getId().equals(request.getId())) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "应用Code已存在");
        }

        Application application;

        if (request.getId() != null) {
            // 编辑：检查权限
            if (!applicationDao.checkPermission(request.getId(), currentUserId)) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "无权限修改此应用，只有应用管理员才能修改");
            }

            application = applicationDao.selectById(request.getId());
            if (application == null) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "应用不存在");
            }

            // 更新字段（appKey不允许修改）
            application.setAppName(request.getAppName());
            application.setDescription(request.getDescription());
            application.setAdminUserIds(request.getAdminUserIds() != null && !request.getAdminUserIds().isEmpty()
                    ? convertToJsonString(request.getAdminUserIds()) : application.getAdminUserIds());
            application.setDeveloperIds(convertToJsonString(request.getDeveloperIds()));
            application.setStatus(request.getStatus() != null ? request.getStatus() : true);
            application.setUpdateBy(currentUserId);

            applicationDao.update(application);

        } else {
            // 新增：生成appKey
            String appKey = generateAppKey();

            // 如果没有指定管理员，默认创建人为管理员
            List<String> adminIds = request.getAdminUserIds();
            if (adminIds == null || adminIds.isEmpty()) {
                adminIds = List.of(currentUserId);
            }

            application = Application.builder()
                    .appCode(request.getAppCode())
                    .appName(request.getAppName())
                    .appKey(appKey)
                    .description(request.getDescription())
                    .adminUserIds(convertToJsonString(adminIds))
                    .developerIds(convertToJsonString(request.getDeveloperIds()))
                    .status(request.getStatus() != null ? request.getStatus() : true)
                    .createBy(currentUserId)
                    .updateBy(currentUserId)
                    .build();

            applicationDao.insert(application);
        }

        log.info("保存应用成功，应用ID：{}", application.getId());
        return application.getId();
    }

    @Override
    public ApplicationVO getApplicationById(Long id) {
        log.info("查询应用详情，应用ID：{}", id);

        Application application = applicationDao.selectById(id);
        if (application == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "应用不存在");
        }

        return convertToVO(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteApplications(ApplicationDeleteRequest request, String currentUserId) {
        log.info("删除应用，参数：{}", request);

        // 检查每个应用的权限
        for (Long id : request.getIds()) {
            if (!applicationDao.checkPermission(id, currentUserId)) {
                throw new BusinessException(ResponseStatusEnums.FAIL.getCode(),
                        "无权限删除应用ID=" + id + "，只有应用管理员才能删除");
            }
        }

        int deletedCount = applicationDao.deleteByIds(request.getIds());

        log.info("删除应用成功，删除数量：{}", deletedCount);
        return deletedCount;
    }

    /**
     * 生成appKey（base64编码的UUID）
     */
    private String generateAppKey() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return Base64.getEncoder().encodeToString(uuid.getBytes());
    }

    /**
     * 转换为VO
     */
    private ApplicationVO convertToVO(Application application) {
        List<String> adminUserIds = application.getAdminUserIds() != null ? application.getAdminUserIds() : new ArrayList<>();
        List<String> developerUserIds = application.getDeveloperUserIds() != null ? application.getDeveloperUserIds() : new ArrayList<>();

        return ApplicationVO.builder()
                .id(application.getId())
                .appCode(application.getAppCode())
                .appName(application.getAppName())
                .appKey(application.getAppKey())
                .description(application.getDescription())
                .adminUserIds(adminUserIds)
                .adminUserNames(getUserNames(adminUserIds))
                .adminUserAccounts(getUserAccounts(adminUserIds))
                .developerUserIds(developerUserIds)
                .developerNames(getUserNames(developerUserIds))
                .developerAccounts(getUserAccounts(developerUserIds))
                .status(application.getStatus())
                .createBy(application.getCreateBy())
                .createByName(getUserName(application.getCreateBy()))
                .createTime(application.getCreateTime())
                .updateBy(application.getUpdateBy())
                .updateByName(getUserName(application.getUpdateBy()))
                .updateTime(application.getUpdateTime())
                .build();
    }

    /**
     * 根据工号获取用户名
     */
    private String getUserName(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        User user = userDao.selectByUserAccountOrUserId(userId);
        return user != null ? user.getUserName() : userId;
    }

    /**
     * 根据工号获取用户账号
     */
    private String getUserAccount(String userId) {
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        User user = userDao.selectByUserAccountOrUserId(userId);
        return user != null ? user.getUserAccount() : null;
    }

    /**
     * 批量获取用户名
     */
    private List<String> getUserNames(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userIds.stream()
                .map(this::getUserName)
                .collect(Collectors.toList());
    }

    /**
     * 批量获取用户账号
     */
    private List<String> getUserAccounts(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userIds.stream()
                .map(this::getUserAccount)
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationRegistrationVO> getRegistrationNodes(String appCode) {
        log.info("查询应用注册节点，应用Code：{}", appCode);

        List<ApplicationRegistration> registrations = applicationRegistrationDao.selectByAppCode(appCode);

        return registrations.stream()
                .map(this::convertToRegistrationVO)
                .collect(Collectors.toList());
    }

    /**
     * 转换注册信息为VO
     */
    private ApplicationRegistrationVO convertToRegistrationVO(ApplicationRegistration registration) {
        return ApplicationRegistrationVO.builder()
                .id(registration.getId())
                .appCode(registration.getAppCode())
                .ipAddress(registration.getIpAddress())
                .machineTag(registration.getMachineTag())
                .registerTime(registration.getRegisterTime())
                .extraInfo(registration.getExtraInfo())
                .build();
    }

    /**
     * 转换列表为JSON字符串
     */
    private String convertToJsonString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("转换JSON失败", e);
            return "[]";
        }
    }
}

