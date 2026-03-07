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

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_TIMEOUT = "TIMEOUT";

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
    public boolean callbackResult(String appCode, ExecutorCallbackResultRequest request) {
        verifyFireLogOwnership(appCode, request.getFireLogId());

        JobFireLog current = jobFireLogDao.selectById(request.getFireLogId());
        if (current == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "执行日志不存在");
        }

        int incomingAttempt = request.getAttemptNo() == null ? 1 : request.getAttemptNo();
        int currentAttempt = current.getAttemptNo() == null ? 1 : current.getAttemptNo();
        if (incomingAttempt < currentAttempt) {
            // 旧 attempt 的迟到回调直接忽略，避免覆盖新 attempt 的最终状态。
            log.info("ignore stale callback result, fireLogId={}, incomingAttempt={}, currentAttempt={}",
                    request.getFireLogId(), incomingAttempt, currentAttempt);
            return true;
        }
        if (incomingAttempt == currentAttempt && isTerminalStatus(current.getStatus())) {
            // 同 attempt 的重复回调按幂等处理。
            return true;
        }

        JobFireLog log = JobFireLog.builder()
                .id(request.getFireLogId())
                .status(request.getStatus())
                .attemptNo(incomingAttempt)
                .finishTime(request.getFinishTime() == null ? System.currentTimeMillis() : request.getFinishTime())
                .executorAddress(request.getExecutorAddress())
                .errorMessage(request.getErrorMessage())
                .build();
        return jobFireLogDao.updateResult(log) > 0;
    }

    @Override
    public boolean callbackLog(String appCode, ExecutorCallbackLogRequest request) {
        verifyFireLogOwnership(appCode, request.getFireLogId());

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

    private void verifyFireLogOwnership(String appCode, Long fireLogId) {
        // 鉴权关闭场景下 appCode 为空，跳过归属校验（仅用于本地调试/兼容）。
        if (appCode == null || appCode.isBlank()) {
            return;
        }
        String fireLogAppCode = jobFireLogDao.selectAppCodeByFireLogId(fireLogId);
        if (fireLogAppCode == null) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "执行日志不存在");
        }
        if (!fireLogAppCode.equals(appCode)) {
            throw new BusinessException(ResponseStatusEnums.FAIL.getCode(), "无权限回调该执行日志");
        }
    }

    private boolean isTerminalStatus(String status) {
        return STATUS_SUCCESS.equals(status) || STATUS_FAILED.equals(status) || STATUS_TIMEOUT.equals(status);
    }
}
