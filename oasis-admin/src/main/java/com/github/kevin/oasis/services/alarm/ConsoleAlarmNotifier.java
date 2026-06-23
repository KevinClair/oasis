package com.github.kevin.oasis.services.alarm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 控制台告警通知器（默认实现）。
 * 将告警信息输出到日志，生产环境可替换为邮件/微信/Webhook 等实现。
 */
@Component
@Slf4j
public class ConsoleAlarmNotifier implements AlarmNotifier {

    @Override
    public String channel() {
        return "CONSOLE";
    }

    @Override
    public boolean send(String receiver, String title, String content) {
        log.warn("===== ALARM [{}] =====\nReceiver: {}\nTitle: {}\nContent: {}\n====================",
                channel(), receiver, title, content);
        return true;
    }
}
