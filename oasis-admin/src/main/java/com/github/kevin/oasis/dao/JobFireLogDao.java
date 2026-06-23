package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.JobFireLog;
import com.github.kevin.oasis.models.vo.schedule.JobLogListRequest;
import com.github.kevin.oasis.models.vo.schedule.JobLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobFireLogDao {

    int insert(JobFireLog log);

    List<JobLogVO> selectLogList(@Param("request") JobLogListRequest request);

    Long countLogList(@Param("request") JobLogListRequest request);

    JobLogVO selectLogDetailById(@Param("id") Long id);

    JobFireLog selectById(@Param("id") Long id);

    int updateResult(@Param("log") JobFireLog log);

    int updateDispatch(@Param("log") JobFireLog log);

    String selectAppCodeByFireLogId(@Param("fireLogId") Long fireLogId);

    /** 查询指定任务正在执行的日志（用于阻塞策略判断） */
    List<JobFireLog> selectRunningByJobId(@Param("jobId") Long jobId);

    /** 取消指定任务的运行中日志（COVER_EARLY 策略） */
    int cancelRunningByJobId(@Param("jobId") Long jobId);
}
