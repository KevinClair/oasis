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

    List<JobSchedule> selectDueSchedules(@Param("nowTime") Long nowTime, @Param("limit") Integer limit);

    List<JobSchedule> selectDueSchedulesByShards(@Param("nowTime") Long nowTime,
                                                 @Param("limit") Integer limit,
                                                 @Param("shardIds") List<Integer> shardIds);

    int claimAndUpdateNextTrigger(@Param("jobId") Long jobId,
                                  @Param("expectedVersion") Long expectedVersion,
                                  @Param("expectedNextTriggerTime") Long expectedNextTriggerTime,
                                  @Param("nextTriggerTime") Long nextTriggerTime,
                                  @Param("triggerStatus") Boolean triggerStatus);
}
