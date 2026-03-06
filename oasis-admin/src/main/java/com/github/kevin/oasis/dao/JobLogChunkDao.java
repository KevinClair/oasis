package com.github.kevin.oasis.dao;

import com.github.kevin.oasis.models.entity.JobLogChunk;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobLogChunkDao {

    int upsert(JobLogChunk chunk);
}
