package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.JobAlarmNotifyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobAlarmNotifyRecordDao {

    List<JobAlarmNotifyRecord> selectByAlarmEventId(@Param("alarmEventId") Long alarmEventId);

    int insert(JobAlarmNotifyRecord record);
}
