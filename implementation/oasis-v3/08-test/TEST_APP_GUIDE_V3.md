# TEST_APP_GUIDE_V3

## 1. 目的
本文用于在当前 Oasis 一期实现下，快速搭建一个测试应用（Executor），并验证调度平台是否稳定可用。

## 2. 前置条件
1. 已启动并可访问 `oasis-admin`。
2. MySQL 已初始化核心表（见 `implementation/oasis-v3/03-sql/README.md` 执行顺序）。
3. JDK 17、Maven 3.9+ 可用。

## 3. Admin 侧准备
1. 在“应用管理”创建测试应用：
   - `appCode`: `oasis-test-app`
   - `appKey`: 自定义强随机字符串
   - 状态：启用
2. 记录 `appCode/appKey`，后续测试应用配置会使用。

## 4. 构建 starter 依赖
在 Oasis 仓库根目录执行：

```bash
mvn -q -f oasis-scheduler-spring-boot-starter/pom.xml -DskipTests install
```

## 5. 创建测试应用（Spring Boot）
### 5.1 `pom.xml` 关键依赖
```xml
<dependency>
  <groupId>com.github.kevin</groupId>
  <artifactId>oasis-scheduler-spring-boot-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 5.2 `application.yml` 示例
```yaml
server:
  port: 18080

oasis:
  scheduler:
    enabled: true
    app-code: oasis-test-app
    app-key: ${OASIS_APP_KEY}
    admin:
      base-url: http://127.0.0.1:8080
      connect-timeout-ms: 2000
      read-timeout-ms: 3000
    server:
      port: 19091
      context-path: /oasis-executor
      verify-invoke-signature: true
    heartbeat:
      interval-ms: 2000
    worker:
      core-size: 4
      max-size: 16
      queue-capacity: 1000
    security:
      clock-skew-ms: 30000
      nonce-expire-ms: 120000
      nonce-max-size: 20000
```

### 5.3 Handler 示例
```java
import com.github.kevin.oasis.starter.context.OasisJobContext;
import com.github.kevin.oasis.starter.handler.OasisJobHandler;
import com.github.kevin.oasis.starter.model.OasisJobResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class DemoSuccessHandler implements OasisJobHandler {
    @Override
    public String handlerName() {
        return "demoSuccessHandler";
    }

    @Override
    public OasisJobResult execute(OasisJobContext context) {
        context.log("demoSuccessHandler start, fireLogId=" + context.getFireLogId());
        return OasisJobResult.success("ok");
    }
}

@Component
public class DemoFlakyHandler implements OasisJobHandler {
    @Override
    public String handlerName() {
        return "demoFlakyHandler";
    }

    @Override
    public OasisJobResult execute(OasisJobContext context) throws Exception {
        int n = ThreadLocalRandom.current().nextInt(100);
        if (n < 40) {
            throw new RuntimeException("random failure for retry test");
        }
        if (n < 70) {
            Thread.sleep(3000L);
        }
        context.log("demoFlakyHandler done, n=" + n);
        return OasisJobResult.success("n=" + n);
    }
}
```

## 6. 启动测试应用（建议双实例）
同一应用启动两个执行器实例（不同 `oasis.scheduler.server.port`）：

```bash
java -jar test-app.jar --oasis.scheduler.server.port=19091
java -jar test-app.jar --oasis.scheduler.server.port=19092
```

期望：在 Admin 应用节点列表可看到两个 `ONLINE` 节点。

## 7. 在 Admin 创建测试任务
建议创建 4 类任务：
1. `CRON` 高频成功任务  
   - handler: `demoSuccessHandler`  
   - cron: 每 5 秒  
   - route: `ROUND`
2. `CRON` 波动任务（验证重试/补偿）  
   - handler: `demoFlakyHandler`  
   - retryCount: `3`  
   - route: `FAILOVER`
3. `FIXED_DELAY` 任务  
   - 间隔 2~3 秒  
   - 验证固定延迟模式
4. `ONCE` 任务  
   - 验证单次触发后停用

## 8. 稳定性验证清单
### 8.1 功能链路
1. 任务触发后 `job_fire_log` 状态变化：`PENDING -> RUNNING -> SUCCESS/FAILED`。
2. 执行日志分片回传：`job_log_chunk` 有内容。
3. 波动任务失败后进入 `dispatch_queue`，重试成功或转 `DEAD`。

### 8.2 HA 行为
1. 观察 `scheduler_node` 心跳与 `shard_lease` 持有分片。
2. 停掉一个 admin 节点（多 admin 部署时），其他节点继续调度。
3. 停掉一个 executor 实例，路由可自动切换到存活节点。

### 8.3 安全行为
1. 保持 `verify-invoke-signature=true`。
2. 人工伪造无签名请求调用 `/oasis-executor/invoke` 应被拒绝。

## 9. 推荐验收标准（一期）
1. 连续运行 24 小时，无大面积漏触发。
2. 调度日志失败可被解释（业务失败/超时/路由无节点等）。
3. 执行器异常下线后，心跳窗口内可识别并摘除路由。
4. `dispatch_queue` 不出现长期积压（`PENDING` 持续增长）。

## 10. 常用排查 SQL
```sql
-- 最近调度日志
SELECT id, job_id, status, attempt_no, trigger_time, finish_time, error_message
FROM job_fire_log
ORDER BY id DESC
LIMIT 50;

-- 补偿队列状态
SELECT status, COUNT(*) cnt
FROM dispatch_queue
GROUP BY status;

-- 执行器节点状态
SELECT app_code, address, status, last_heartbeat_time, update_time
FROM executor_node
WHERE app_code = 'oasis-test-app'
ORDER BY id DESC;

-- 调度节点与租约
SELECT node_id, status, last_heartbeat_time FROM scheduler_node ORDER BY update_time DESC;
SELECT shard_id, owner_node_id, lease_expire_at FROM shard_lease ORDER BY shard_id LIMIT 20;
```

## 11. 当前已知边界（一期）
1. `BROADCAST` 仍为单节点降级执行。
2. OpenAPI 动态任务管理未上线（二期预留）。
3. 长连接通道未上线，当前为 Netty HTTP + 心跳模式。
