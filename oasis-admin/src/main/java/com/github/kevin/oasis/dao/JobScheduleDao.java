package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.JobSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobScheduleDao {

    JobSchedule selectByJobId(@Param("jobId") Long jobId);

    int insert(JobSchedule schedule);

    int updateByJobId(JobSchedule schedule);

    int updateTriggerStatusByJobIds(@Param("jobIds") List<Long> jobIds, @Param("status") Boolean status);
}
