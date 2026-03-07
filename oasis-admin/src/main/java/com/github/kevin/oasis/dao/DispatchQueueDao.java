package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.DispatchQueue;
import com.github.kevin.oasis.models.vo.schedule.DispatchQueueListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DispatchQueueDao {

    int insert(DispatchQueue queue);

    List<DispatchQueue> selectDueRecords(@Param("nowTime") Long nowTime, @Param("limit") Integer limit);

    int recoverStuckProcessing(@Param("stuckBefore") Long stuckBefore, @Param("nextRetryTime") Long nextRetryTime);

    int claimProcessing(@Param("id") Long id);

    int markSuccess(@Param("id") Long id);

    int reschedule(@Param("id") Long id,
                   @Param("retryCount") Integer retryCount,
                   @Param("nextRetryTime") Long nextRetryTime,
                   @Param("payload") String payload);

    int markDead(@Param("id") Long id, @Param("payload") String payload);

    Long countByStatus(@Param("status") String status);

    Long countDuePending(@Param("nowTime") Long nowTime);

    List<DispatchQueue> selectList(@Param("request") DispatchQueueListRequest request);

    Long countList(@Param("request") DispatchQueueListRequest request);
}
