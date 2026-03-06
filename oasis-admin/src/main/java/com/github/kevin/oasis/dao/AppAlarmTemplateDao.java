package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.AppAlarmTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppAlarmTemplateDao {

    AppAlarmTemplate selectByAppCode(@Param("appCode") String appCode);

    int insert(AppAlarmTemplate template);

    int updateByAppCode(AppAlarmTemplate template);
}
