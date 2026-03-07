package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.JobAlarmEvent;
import com.github.kevin.oasis.models.vo.schedule.JobAlarmEventListRequest;
import com.github.kevin.oasis.models.vo.schedule.JobAlarmEventVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobAlarmEventDao {

    List<JobAlarmEventVO> selectEventList(@Param("jobId") Long jobId, @Param("request") JobAlarmEventListRequest request);

    Long countEventList(@Param("jobId") Long jobId, @Param("request") JobAlarmEventListRequest request);

    List<JobAlarmEventVO> selectEventListByRequest(@Param("request") JobAlarmEventListRequest request);

    Long countEventListByRequest(@Param("request") JobAlarmEventListRequest request);

    JobAlarmEvent selectById(@Param("id") Long id);

    int insert(JobAlarmEvent event);
}
