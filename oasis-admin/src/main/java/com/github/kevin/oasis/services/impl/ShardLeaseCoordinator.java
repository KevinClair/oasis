package com.github.kevin.oasis.services.impl;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.SchedulerNodeDao;
import com.github.kevin.oasis.dao.ShardLeaseDao;
import com.github.kevin.oasis.models.entity.SchedulerNode;
import com.github.kevin.oasis.models.entity.ShardLease;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 分片租约协调器：
 * 1) 上报 scheduler_node 心跳
 * 2) 计算当前节点应持有的分片
 * 3) 基于 shard_lease 进行续租/抢占
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ShardLeaseCoordinator {

    private final SchedulerRuntimeProperties runtimeProperties;
    private final SchedulerNodeDao schedulerNodeDao;
    private final ShardLeaseDao shardLeaseDao;

    private String localNodeId;
    private String localHost;

    private volatile List<Integer> ownedShardIds = Collections.emptyList();

    @PostConstruct
    public void init() {
        localHost = resolveHost();
        if (runtimeProperties.getSchedulerNodeId() != null && !runtimeProperties.getSchedulerNodeId().isBlank()) {
            localNodeId = runtimeProperties.getSchedulerNodeId().trim();
            return;
        }
        // 默认使用 host:port 作为稳定节点标识，避免节点每次重启都写入新 nodeId。
        localNodeId = localHost + ":" + runtimeProperties.getSchedulerNodePort();
    }

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.scheduler-node-heartbeat-interval-ms:1000}")
    public void heartbeatNode() {
        if (!runtimeProperties.isEnabled()) {
            return;
        }

        SchedulerNode node = SchedulerNode.builder()
                .nodeId(localNodeId)
                .host(localHost)
                .port(runtimeProperties.getSchedulerNodePort())
                .status("ONLINE")
                .lastHeartbeatTime(System.currentTimeMillis())
                .build();

        SchedulerNode exist = schedulerNodeDao.selectByNodeId(localNodeId);
        if (exist == null) {
            schedulerNodeDao.insert(node);
        } else {
            schedulerNodeDao.updateHeartbeat(node);
        }
    }

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.scheduler-node-heartbeat-interval-ms:1000}")
    public void refreshNodeStatus() {
        long heartbeatBefore = System.currentTimeMillis() - runtimeProperties.getSchedulerNodeHeartbeatTimeoutMs();
        int updated = schedulerNodeDao.markOfflineByHeartbeatBefore(heartbeatBefore);
        if (updated > 0) {
            log.info("mark offline scheduler nodes done, count={}", updated);
        }
    }

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.shard-lease-renew-interval-ms:1000}")
    public void rebalanceAndRenew() {
        if (!runtimeProperties.isEnabled()) {
            return;
        }

        if (!runtimeProperties.isShardLeaseEnabled()) {
            ownedShardIds = IntStream.range(0, runtimeProperties.getShardCount()).boxed().toList();
            return;
        }

        long now = System.currentTimeMillis();
        long heartbeatAfter = now - runtimeProperties.getSchedulerNodeHeartbeatTimeoutMs();
        List<SchedulerNode> aliveNodes = schedulerNodeDao.selectAliveNodes(heartbeatAfter);
        if (aliveNodes == null || aliveNodes.isEmpty()) {
            ownedShardIds = Collections.emptyList();
            return;
        }

        List<String> sortedNodeIds = aliveNodes.stream()
                .map(SchedulerNode::getNodeId)
                .sorted()
                .collect(Collectors.toList());
        int nodeCount = sortedNodeIds.size();

        for (int shardId = 0; shardId < runtimeProperties.getShardCount(); shardId++) {
            String targetNodeId = sortedNodeIds.get(shardId % nodeCount);
            if (!localNodeId.equals(targetNodeId)) {
                continue;
            }
            tryAcquireOrRenewShard(shardId, now);
        }

        ownedShardIds = shardLeaseDao.selectOwnedShardIds(localNodeId, now);
    }

    public List<Integer> getOwnedShardIds() {
        return ownedShardIds;
    }

    private void tryAcquireOrRenewShard(int shardId, long now) {
        long expireAt = now + runtimeProperties.getShardLeaseDurationMs();
        ShardLease lease = shardLeaseDao.selectByShardId(shardId);
        if (lease == null) {
            try {
                shardLeaseDao.insert(ShardLease.builder()
                        .shardId(shardId)
                        .ownerNodeId(localNodeId)
                        .leaseExpireAt(expireAt)
                        .version(0L)
                        .build());
            } catch (Exception ignore) {
                // 并发插入冲突属于正常竞争，下一轮继续尝试即可。
            }
            return;
        }

        if (localNodeId.equals(lease.getOwnerNodeId())) {
            shardLeaseDao.renewIfOwner(shardId, localNodeId, expireAt, lease.getVersion());
            return;
        }

        if (lease.getLeaseExpireAt() < now) {
            shardLeaseDao.takeOverExpired(shardId, localNodeId, expireAt, now, lease.getVersion());
        }
    }

    private String resolveHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    @PreDestroy
    public void onShutdown() {
        if (localNodeId == null || localNodeId.isBlank()) {
            return;
        }
        try {
            // drain：主动释放租约并下线节点，减少等待租约超时带来的接管延迟。
            shardLeaseDao.releaseByOwner(localNodeId, System.currentTimeMillis() - 1);
            schedulerNodeDao.markOfflineByNodeId(localNodeId);
            log.info("scheduler node drain complete, nodeId={}", localNodeId);
        } catch (Exception e) {
            log.warn("scheduler node drain failed, nodeId={}, msg={}", localNodeId, e.getMessage());
        }
    }
}
