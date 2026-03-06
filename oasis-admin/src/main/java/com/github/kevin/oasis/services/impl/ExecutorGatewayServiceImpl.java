package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.common.BusinessException;
import com.github.kevin.oasis.common.ResponseStatusEnums;
import com.github.kevin.oasis.dao.ApplicationDao;
import com.github.kevin.oasis.dao.ExecutorNodeDao;
import com.github.kevin.oasis.dao.JobFireLogDao;
import com.github.kevin.oasis.dao.JobLogChunkDao;
import com.github.kevin.oasis.models.entity.Application;
import com.github.kevin.oasis.models.entity.ExecutorNode;
import com.github.kevin.oasis.models.entity.JobFireLog;
import com.github.kevin.oasis.models.entity.JobLogChunk;
import com.github.kevin.oasis.models.vo.executor.ExecutorCallbackLogRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorCallbackResultRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorHeartbeatRequest;
import com.github.kevin.oasis.models.vo.executor.ExecutorRegisterRequest;
import com.github.kevin.oasis.services.ExecutorGatewayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 执行器网关服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutorGatewayServiceImpl implements ExecutorGatewayService {

    private final ApplicationDao applicationDao;
    private final ExecutorNodeDao executorNodeDao;
    private final JobFireLogDao jobFireLogDao;
    private final JobLogChunkDao jobLogChunkDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(ExecutorRegisterRequest request) {
        verifyApplication(request.getAppCode(), request.getAppKey());

        ExecutorNode exist = executorNodeDao.selectByAppCodeAndAddress(request.getAppCode(), request.getAddress());
        ExecutorNode node = ExecutorNode.builder()
                .appCode(request.getAppCode())
                .address(request.getAddress())
                .machineTag(request.getMachineTag())
                .status("ONLINE")
                .lastHeartbeatTime(System.currentTimeMillis())
                .metaJson(request.getMetaJson())
                .build();

        if (exist == null) {
            executorNodeDao.insert(node);
        } else {
            node.setId(exist.getId());
            executorNodeDao.updateHeartbeat(node);
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean heartbeat(ExecutorHeartbeatRequest request) {
        verifyApplication(request.getAppCode(), request.getAppKey());

        ExecutorNode exist = executorNodeDao.selectByAppCodeAndAddress(request.getAppCode(), request.getAddress());
        ExecutorNode node = ExecutorNode.builder()
                .appCode(request.getAppCode())
                .address(request.getAddress())
                .status("ONLINE")
                .lastHeartbeatTime(System.currentTimeMillis())
                .metaJson(request.getMetaJson())
                .build();

        if (exist == null) {
            node.setMachineTag("auto-register");
            executorNodeDao.insert(node);
            return true;
        }

        node.setId(exist.getId());
        executorNodeDao.updateHeartbeat(node);
        return true;
    }

    @Override
    public boolean callbackResult(ExecutorCallbackResultRequest request) {
        JobFireLog log = JobFireLog.builder()
                .id(request.getFireLogId())
                .status(request.getStatus())
                .attemptNo(request.getAttemptNo() == null ? 1 : request.getAttemptNo())
                .finishTime(request.getFinishTime() == null ? System.currentTimeMillis() : request.getFinishTime())
                .executorAddress(request.getExecutorAddress())
                .errorMessage(request.getErrorMessage())
                .build();
        return jobFireLogDao.updateResult(log) > 0;
    }

    @Override
    public boolean callbackLog(ExecutorCallbackLogRequest request) {
        JobLogChunk chunk = JobLogChunk.builder()
                .fireLogId(request.getFireLogId())
                .seqNo(request.getSeqNo())
                .logTime(request.getLogTime())
                .logContent(request.getLogContent())
                .build();
        return jobLogChunkDao.upsert(chunk) > 0;
    }

    private void verifyApplication(String appCode, String appKey) {
        Application app = applicationDao.selectByAppCode(appCode);
        if (app == null || Boolean.FALSE.equals(app.getStatus())) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "应用不存在或已禁用");
        }

        if (app.getAppKey() == null || !app.getAppKey().equals(appKey)) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "应用密钥不匹配");
        }
    }
}
