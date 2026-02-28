import { request } from '../request';

/**
 * Login
 *
 * @param user User identifier (userId or userAccount)
 * @param password Password
 * @param rememberMe Remember me
 */
export function fetchLogin(user: string, password: string, rememberMe: boolean = false) {
  return request<Api.Auth.LoginToken>({
    url: '/auth/login',
    method: 'post',
    data: {
      user,
      password,
      rememberMe
    }
  });
}

/** Get user info */
export function fetchGetUserInfo() {
  return request<Api.Auth.UserInfoResponse>({ url: '/auth/getUserInfo' });
}

/**
 * Refresh token
 *
 * @param refreshToken Refresh token
 */
export function fetchRefreshToken(refreshToken: string) {
  return request<Api.Auth.LoginToken>({
    url: '/auth/refreshToken',
    method: 'post',
    data: {
      refreshToken
    }
  });
}

/**
 * return custom backend error
 *
 * @param code error code
 * @param msg error message
 */
export function fetchCustomBackendError(code: string, msg: string) {
  return request({ url: '/auth/error', params: { code, msg } });
}

/**
 * Change password
 *
 * @param userAccount User account
 * @param oldPassword Old password
 * @param newPassword New password
 */
export function fetchChangePassword(data: { userAccount: string; oldPassword: string; newPassword: string }) {
  return request<boolean>({
    url: '/auth/changePassword',
    method: 'post',
    data
  });
}

