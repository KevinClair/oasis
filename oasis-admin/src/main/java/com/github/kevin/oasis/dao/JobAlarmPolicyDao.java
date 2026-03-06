package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.JobAlarmPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JobAlarmPolicyDao {

    JobAlarmPolicy selectByJobId(@Param("jobId") Long jobId);

    int insert(JobAlarmPolicy policy);

    int updateByJobId(JobAlarmPolicy policy);
}
