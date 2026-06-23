package com.github.kevin.oasis.services.alarm;

/**
 * 告警通知通道策略接口。
 * 实现类需注册为 Spring Bean，由 AlarmNotifierRegistry 自动发现。
 */
public interface AlarmNotifier {

    /** 通道名称，如 "CONSOLE"、"EMAIL"、"WECHAT"、"WEBHOOK" */
    String channel();

    /**
     * 发送告警通知。
     *
     * @param receiver  接收人/接收地址
     * @param title     告警标题
     * @param content   告警内容
     * @return 是否发送成功
     */
    boolean send(String receiver, String title, String content);
}
