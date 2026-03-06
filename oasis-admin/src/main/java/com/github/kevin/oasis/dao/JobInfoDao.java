package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.JobInfo;
import com.github.kevin.oasis.models.vo.schedule.JobListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobInfoDao {

    List<JobInfo> selectJobList(@Param("request") JobListRequest request);

    Long countJobList(@Param("request") JobListRequest request);

    JobInfo selectById(@Param("id") Long id);

    int insert(JobInfo jobInfo);

    int update(JobInfo jobInfo);

    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") Boolean status, @Param("updateBy") String updateBy);
}
