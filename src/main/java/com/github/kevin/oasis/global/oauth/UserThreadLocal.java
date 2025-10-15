package com.github.kevin.oasis.global.oauth;

import com.github.kevin.oasis.models.base.UserInfo;

/**
 * 用户信息线程变量
 */
public class UserThreadLocal {

    private static final ThreadLocal<UserInfo> USER_INFO_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取当前线程的用户信息
     *
     * @return 用户信息
     */
    public static UserInfo getUserInfo() {
        return USER_INFO_THREAD_LOCAL.get();
    }

    /**
     * 设置当前线程的用户信息
     *
     * @param userInfo 用户信息
     */
    public static void setUserInfo(UserInfo userInfo) {
        USER_INFO_THREAD_LOCAL.set(userInfo);
    }

    /**
     * 清除当前线程的用户信息
     */
    public static void clear() {
        USER_INFO_THREAD_LOCAL.remove();
    }
}
