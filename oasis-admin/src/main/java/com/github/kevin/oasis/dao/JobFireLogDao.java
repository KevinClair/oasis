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

    /**
     * 更新执行结果（带乐观锁：attempt_no 必须 >= 传入值，且当前状态非终态）。
     *
     * @return 影响行数，0 表示被并发回调抢先写入或记录已处于终态
     */
    int updateResult(@Param("log") JobFireLog log);

    int updateDispatch(@Param("log") JobFireLog log);

    String selectAppCodeByFireLogId(@Param("fireLogId") Long fireLogId);

    /**
     * 查询超时的 RUNNING 记录（trigger_time + job_info.timeout_seconds < now）。
     */
    List<JobFireLog> selectTimeoutRunning(@Param("now") Long now);

    /**
     * CAS 将 RUNNING 记录标记为 TIMEOUT。
     *
     * @return 影响行数，0 表示已被其他线程处理
     */
    int updateToTimeout(@Param("id") Long id, @Param("now") Long now);

    /**
     * 查询指定 job 是否有 RUNNING 状态的执行记录。
     */
    boolean hasRunningByJobId(@Param("jobId") Long jobId);
}
