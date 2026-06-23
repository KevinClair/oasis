package com.github.kevin.oasis.services.alarm;

import com.github.kevin.oasis.config.SchedulerRuntimeProperties;
import com.github.kevin.oasis.dao.JobAlarmEventDao;
import com.github.kevin.oasis.dao.JobAlarmNotifyRecordDao;
import com.github.kevin.oasis.dao.JobAlarmPolicyDao;
import com.github.kevin.oasis.dao.AppAlarmTemplateDao;
import com.github.kevin.oasis.models.entity.AppAlarmTemplate;
import com.github.kevin.oasis.models.entity.JobAlarmEvent;
import com.github.kevin.oasis.models.entity.JobAlarmNotifyRecord;
import com.github.kevin.oasis.models.entity.JobAlarmPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 告警通知发送器：消费 PENDING 告警事件，通过注册的通知通道投递。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlarmNotifySender {

    private final SchedulerRuntimeProperties runtimeProperties;
    private final JobAlarmEventDao jobAlarmEventDao;
    private final JobAlarmPolicyDao jobAlarmPolicyDao;
    private final AppAlarmTemplateDao appAlarmTemplateDao;
    private final JobAlarmNotifyRecordDao jobAlarmNotifyRecordDao;
    private final AlarmNotifierRegistry notifierRegistry;

    @Scheduled(fixedDelayString = "${oasis.scheduler.runtime.alarm-notify-interval-ms:10000}")
    public void sendPendingAlarms() {
        if (!runtimeProperties.isEnabled()) {
            return;
        }

        List<JobAlarmEvent> pendingEvents = jobAlarmEventDao.selectPendingEvents();
        if (pendingEvents == null || pendingEvents.isEmpty()) {
            return;
        }

        for (JobAlarmEvent event : pendingEvents) {
            try {
                sendSingleAlarm(event);
            } catch (Exception e) {
                log.error("send alarm failed, eventId={}", event.getId(), e);
            }
        }
    }

    private void sendSingleAlarm(JobAlarmEvent event) {
        // 获取告警策略以确定接收人和通道
        JobAlarmPolicy policy = jobAlarmPolicyDao.selectByJobId(event.getJobId());
        AppAlarmTemplate template = appAlarmTemplateDao.selectByAppCode(event.getJobId().toString());

        // 确定接收人和通道（策略优先，模板兜底）
        String receivers = null;
        String channels = null;
        if (policy != null && Boolean.TRUE.equals(policy.getEnabled())) {
            receivers = policy.getReceivers();
            channels = policy.getChannels();
            if (Boolean.TRUE.equals(policy.getInheritAppTemplate()) && template != null) {
                if (receivers == null) receivers = template.getReceivers();
                if (channels == null) channels = template.getChannels();
            }
        } else if (template != null && Boolean.TRUE.equals(template.getEnabled())) {
            receivers = template.getReceivers();
            channels = template.getChannels();
        }

        if (channels == null || channels.isBlank()) {
            channels = "CONSOLE"; // 默认控制台输出
        }

        String[] channelArray = channels.split(",");
        boolean anySuccess = false;
        StringBuilder response = new StringBuilder();

        String title = "任务执行失败告警";
        String content = event.getAlarmContent();

        for (String channel : channelArray) {
            String ch = channel.trim();
            AlarmNotifier notifier = notifierRegistry.get(ch);
            if (notifier == null) {
                response.append(ch).append(":未知通道; ");
                continue;
            }

            boolean ok = notifier.send(receivers != null ? receivers : "admin", title, content);
            if (ok) {
                anySuccess = true;
                response.append(ch).append(":OK; ");
            } else {
                response.append(ch).append(":FAIL; ");
            }

            // 记录每次发送结果
            jobAlarmNotifyRecordDao.insert(JobAlarmNotifyRecord.builder()
                    .alarmEventId(event.getId())
                    .channel(ch)
                    .receiver(receivers != null ? receivers : "admin")
                    .sendStatus(ok ? "SUCCESS" : "FAILED")
                    .responseMessage(ok ? "OK" : "send failed")
                    .sendTime(System.currentTimeMillis())
                    .build());
        }

        // 更新告警事件通知状态
        String finalStatus = anySuccess ? "NOTIFIED" : "FAILED";
        updateEventStatus(event, finalStatus, response.toString());
    }

    private void updateEventStatus(JobAlarmEvent event, String status, String responseMessage) {
        int updated = jobAlarmEventDao.updateNotifyStatus(event.getId(), status);
        // 这里使用简单的方式：直接更新 entity 并通过 mapper 更新
        log.info("alarm event {} status updated to {}, response: {}", event.getId(), status, responseMessage);
    }
}
