package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.ApplicationRegistration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 应用注册信息DAO
 */
@Mapper
public interface ApplicationRegistrationDao {

    /**
     * 根据应用Code查询注册节点列表
     *
     * @param appCode 应用Code
     * @return 注册节点列表
     */
    List<ApplicationRegistration> selectByAppCode(@Param("appCode") String appCode);
}

