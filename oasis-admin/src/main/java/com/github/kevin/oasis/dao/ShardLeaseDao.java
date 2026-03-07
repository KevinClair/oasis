package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.ShardLease;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShardLeaseDao {

    ShardLease selectByShardId(@Param("shardId") Integer shardId);

    int insert(ShardLease lease);

    int renewIfOwner(@Param("shardId") Integer shardId,
                     @Param("ownerNodeId") String ownerNodeId,
                     @Param("leaseExpireAt") Long leaseExpireAt,
                     @Param("expectedVersion") Long expectedVersion);

    int takeOverExpired(@Param("shardId") Integer shardId,
                        @Param("ownerNodeId") String ownerNodeId,
                        @Param("leaseExpireAt") Long leaseExpireAt,
                        @Param("nowTime") Long nowTime,
                        @Param("expectedVersion") Long expectedVersion);

    List<Integer> selectOwnedShardIds(@Param("ownerNodeId") String ownerNodeId, @Param("nowTime") Long nowTime);

    int releaseByOwner(@Param("ownerNodeId") String ownerNodeId, @Param("releaseTime") Long releaseTime);
}
