package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.ExecutorNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExecutorNodeDao {

    ExecutorNode selectByAppCodeAndAddress(@Param("appCode") String appCode, @Param("address") String address);

    List<ExecutorNode> selectOnlineByAppCode(@Param("appCode") String appCode, @Param("heartbeatAfter") Long heartbeatAfter);

    int insert(ExecutorNode node);

    int updateHeartbeat(ExecutorNode node);

    int markOfflineByHeartbeatBefore(@Param("heartbeatBefore") Long heartbeatBefore);
}
