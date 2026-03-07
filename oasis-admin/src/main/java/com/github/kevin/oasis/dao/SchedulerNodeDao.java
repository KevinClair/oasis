package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.SchedulerNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SchedulerNodeDao {

    SchedulerNode selectByNodeId(@Param("nodeId") String nodeId);

    int insert(SchedulerNode node);

    int updateHeartbeat(SchedulerNode node);

    List<SchedulerNode> selectAliveNodes(@Param("heartbeatAfter") Long heartbeatAfter);

    int markOfflineByHeartbeatBefore(@Param("heartbeatBefore") Long heartbeatBefore);
}
