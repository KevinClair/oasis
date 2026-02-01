declare namespace Api {
  /**
   * namespace Auth
   *
   * backend api module: "auth"
   */
  namespace Auth {
    interface LoginToken {
      token: string;
      refreshToken: string;
    }

    /** Backend UserInfo Response */
    interface UserInfoResponse {
      id: number;
      userId: number;
      userAccount: string;
      userName: string;
      email?: string;
      phone?: string;
      status?: number;
      createTime?: string;
      updateTime?: string;
    }

    /** Frontend UserInfo Store */
    interface UserInfo {
      userId: string;
      userName: string;
      roles: string[];
      buttons: string[];
    }
  }
}
