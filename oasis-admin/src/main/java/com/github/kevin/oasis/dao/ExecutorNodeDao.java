package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.ExecutorNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExecutorNodeDao {

    ExecutorNode selectByAppCodeAndAddress(@Param("appCode") String appCode, @Param("address") String address);

    int insert(ExecutorNode node);

    int updateHeartbeat(ExecutorNode node);
}
