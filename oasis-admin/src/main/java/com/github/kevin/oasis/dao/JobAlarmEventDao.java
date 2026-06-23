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

    /** 查询指定 job 最近一条告警事件（用于静默期判断） */
    JobAlarmEvent selectLatestByJobId(@Param("jobId") Long jobId);

    /** 查询所有待通知的告警事件 */
    List<JobAlarmEvent> selectPendingEvents();

    /** 更新告警事件通知状态 */
    int updateNotifyStatus(@Param("id") Long id, @Param("notifyStatus") String notifyStatus);
}
