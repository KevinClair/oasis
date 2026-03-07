declare namespace Api {
  /**
   * namespace SystemManage
   *
   * backend api module: "systemManage"
   */
  namespace SystemManage {
    type CommonSearchParams = Pick<Common.PaginatingCommonParams, 'current' | 'size'>;

    /** role */
    type Role = Common.CommonRecord<{
      /** role name */
      roleName: string;
      /** role code */
      roleCode: string;
      /** role description */
      roleDesc: string;
    }>;

    /** role search params */
    type RoleSearchParams = CommonType.RecordNullable<
      Pick<Api.SystemManage.Role, 'roleName' | 'roleCode' | 'status'> & CommonSearchParams
    >;

    /** role list */
    type RoleList = Common.PaginatingQueryRecord<Role>;

    /** role edit */
    type RoleEdit = {
      /** role id (required for edit) */
      id?: number;
      /** role name */
      roleName: string;
      /** role code */
      roleCode: string;
      /** role description */
      roleDesc?: string;
      /** status */
      status?: boolean;
    };

    /** all role */
    type AllRole = Pick<Role, 'id' | 'roleName' | 'roleCode'>;

    /**
     * user gender
     *
     * - "1": "male"
     * - "2": "female"
     */
    type UserGender = '1' | '2';

    /** user */
    type User = Common.CommonRecord<{
      /** user ID (work number) */
      userId: string;
      /** user account */
      userAccount: string;
      /** user name */
      userName: string;
      /** user gender */
      userGender: UserGender | null;
      /** user nick name */
      nickName: string;
      /** user phone */
      userPhone: string;
      /** user email */
      userEmail: string;
      /** user role code collection */
      userRoles: string[];
    }>;

    /** user search params */
    type UserSearchParams = CommonType.RecordNullable<
      Pick<Api.SystemManage.User, 'userId' | 'userAccount' | 'userName' | 'userGender' | 'nickName' | 'userPhone' | 'userEmail' | 'status'> &
        CommonSearchParams
    >;

    /** user list */
    type UserList = Common.PaginatingQueryRecord<User>;

    /** user edit */
    type UserEdit = {
      /** user id (required for edit) */
      id?: number;
      /** user ID (work number) */
      userId?: string;
      /** user account */
      userAccount?: string;
      /** user name */
      userName: string;
      /** password (required for add) */
      password?: string;
      /** user gender */
      userGender?: UserGender | null;
      /** user nick name */
      nickName?: string;
      /** user phone */
      phone?: string;
      /** user email */
      email?: string;
      /** status */
      status?: boolean;
      /** user role code collection */
      userRoles?: string[];
    };

    /** all user */
    type AllUser = Pick<User, 'id' | 'userId' | 'userName' | 'userAccount'>;

    /**
     * menu type
     *
     * - 1: directory
     * - 2: menu
     */
    type MenuType = 1 | 2;

    type MenuButton = {
      /**
       * button code
       *
       * it can be used to control the button permission
       */
      code: string;
      /** button description */
      desc: string;
    };

    /**
     * icon type
     *
     * - "1": iconify icon
     * - "2": local icon
     */
    type IconType = '1' | '2';

    type MenuPropsOfRoute = Pick<
      import('vue-router').RouteMeta,
      | 'i18nKey'
      | 'keepAlive'
      | 'constant'
      | 'order'
      | 'href'
      | 'hideInMenu'
      | 'activeMenu'
      | 'multiTab'
      | 'fixedIndexInTab'
      | 'query'
    >;

    type Menu = Common.CommonRecord<{
      /** parent menu id */
      parentId: number;
      /** menu type */
      menuType: MenuType;
      /** menu name */
      menuName: string;
      /** route name */
      routeName: string;
      /** route path */
      routePath: string;
      /** component */
      component?: string;
      /** iconify icon name or local icon name */
      icon: string;
      /** icon type */
      iconType: IconType;
      /** buttons */
      buttons?: MenuButton[] | null;
      /** children menu */
      children?: Menu[] | null;
    }> &
      MenuPropsOfRoute;

    /** menu list */
    type MenuList = Common.PaginatingQueryRecord<Menu>;

    /** menu edit */
    type MenuEdit = {
      /** menu id (required for edit) */
      id?: number;
      /** parent menu id */
      parentId?: number;
      /** menu type */
      menuType: MenuType;
      /** menu name */
      menuName: string;
      /** route name */
      routeName: string;
      /** route path */
      routePath: string;
      /** path params */
      pathParam?: string;
      /** component */
      component?: string;
      /** icon type */
      iconType?: IconType;
      /** icon */
      icon?: string;
      /** local icon */
      localIcon?: string;
      /** i18n key */
      i18nKey?: string;
      /** order */
      order?: number;
      /** keep alive */
      keepAlive?: boolean;
      /** constant */
      constant?: boolean;
      /** href */
      href?: string;
      /** hide in menu */
      hideInMenu?: boolean;
      /** active menu */
      activeMenu?: string;
      /** multi tab */
      multiTab?: boolean;
      /** fixed index in tab */
      fixedIndexInTab?: number;
      /** status */
      status?: boolean;
    };

    type MenuTree = {
      id: number;
      label: string;
      pId: number;
      children?: MenuTree[];
    };

    /**
     * announcement type
     *
     * - "normal": normal
     * - "warning": warning
     * - "important": important notice
     */
    type AnnouncementType = 'normal' | 'warning' | 'important';

    /** announcement */
    type Announcement = Common.CommonRecord<{
      /** announcement title */
      title: string;
      /** announcement content */
      content: string;
      /** announcement type */
      type: AnnouncementType;
    }>;

    /** announcement search params */
    type AnnouncementSearchParams = CommonType.RecordNullable<
      Pick<Api.SystemManage.Announcement, 'title' | 'type'> & CommonSearchParams
    >;

    /** announcement list */
    type AnnouncementList = Common.PaginatingQueryRecord<Announcement>;

    /** announcement edit */
    type AnnouncementEdit = {
      /** announcement id (required for edit) */
      id?: number;
      /** announcement title */
      title: string;
      /** announcement content */
      content: string;
      /** announcement type */
      type: AnnouncementType;
    };

    /** application */
    type Application = Common.CommonRecord<{
      /** application code */
      appCode: string;
      /** application name */
      appName: string;
      /** application key */
      appKey: string;
      /** description */
      description: string;
      /** admin user ids */
      adminUserIds: string[];
      /** admin user names */
      adminUserNames?: string[];
      /** admin user accounts */
      adminUserAccounts?: string[];
      /** developer user ids */
      developerUserIds: string[];
      /** developer names */
      developerNames?: string[];
      /** developer accounts */
      developerAccounts?: string[];
      /** status */
      status: boolean;
    }>;

    /** application search params */
    type ApplicationSearchParams = CommonType.RecordNullable<
      Pick<Api.SystemManage.Application, 'appCode' | 'appName' | 'status'> & CommonSearchParams
    >;

    /** application list */
    type ApplicationList = Common.PaginatingQueryRecord<Application>;

    /** application edit */
    type ApplicationEdit = {
      /** application id (required for edit) */
      id?: number;
      /** application code */
      appCode: string;
      /** application name */
      appName: string;
      /** description */
      description: string;
      /** admin user ids */
      adminUserIds?: string[];
      /** developer user ids */
      developerUserIds?: string[];
      /** status */
      status?: boolean;
    };

    /** registration node */
    type RegistrationNode = {
      /** node id */
      id: number;
      /** application code */
      appCode: string;
      /** IP address */
      ipAddress: string;
      /** machine tag */
      machineTag?: string;
      /** register time */
      registerTime: string;
      /** extra info (JSON format) */
      extraInfo?: string;
    };

    /** schedule job */
    type ScheduleJob = Common.CommonRecord<{
      appCode: string;
      jobName: string;
      handlerName: string;
      scheduleType: string;
      scheduleConf: string;
      routeStrategy: string;
      blockStrategy: string;
      retryCount: number;
      timeoutSeconds: number;
      alarmInheritApp: boolean;
      nextTriggerTime?: number | null;
    }>;

    type ScheduleJobSearchParams = CommonType.RecordNullable<
      Pick<Api.SystemManage.ScheduleJob, 'appCode' | 'jobName' | 'handlerName' | 'scheduleType' | 'status'> &
        CommonSearchParams
    >;

    type ScheduleJobList = Common.PaginatingQueryRecord<ScheduleJob>;

    type ScheduleJobEdit = {
      id?: number;
      appCode: string;
      jobName: string;
      handlerName: string;
      scheduleType: string;
      scheduleConf: string;
      routeStrategy?: string;
      blockStrategy?: string;
      retryCount?: number;
      timeoutSeconds?: number;
      status: boolean;
      alarmInheritApp?: boolean;
    };

    type ScheduleLog = {
      id: number;
      jobId: number;
      appCode: string;
      jobName: string;
      triggerTime: number;
      finishTime?: number | null;
      triggerType: string;
      executorAddress?: string | null;
      status: string;
      attemptNo: number;
      errorMessage?: string | null;
      traceId?: string | null;
    };

    type ScheduleLogSearchParams = CommonType.RecordNullable<
      {
        logId: number;
        jobId: number;
        appCode: string;
        status: string;
        startTriggerTime: number;
        endTriggerTime: number;
      } & CommonSearchParams
    >;

    type ScheduleLogList = Common.PaginatingQueryRecord<ScheduleLog>;

    type DispatchQueueOverview = {
      pendingCount: number;
      processingCount: number;
      successCount: number;
      deadCount: number;
      duePendingCount: number;
    };

    type DispatchQueue = {
      id: number;
      fireLogId: number;
      jobId: number;
      targetAddress?: string | null;
      status: string;
      retryCount: number;
      nextRetryTime?: number | null;
      payload?: string | null;
      createTime?: string | null;
      updateTime?: string | null;
    };

    type DispatchQueueSearchParams = CommonType.RecordNullable<
      {
        status: string;
        fireLogId: number;
        jobId: number;
      } & CommonSearchParams
    >;

    type DispatchQueueList = Common.PaginatingQueryRecord<DispatchQueue>;

    type AppAlarmTemplate = {
      id?: number;
      appCode: string;
      receivers: string[];
      channels: string[];
      quietPeriodMinutes: number;
      failThreshold: number;
      timeoutSeconds: number;
      enabled: boolean;
    };

    type JobAlarmPolicy = {
      id?: number;
      jobId: number;
      inheritAppTemplate: boolean;
      receivers: string[];
      channels: string[];
      quietPeriodMinutes?: number | null;
      failThreshold?: number | null;
      timeoutSeconds?: number | null;
      enabled: boolean;
    };

    type JobAlarmEvent = {
      id: number;
      jobId: number;
      fireLogId: number;
      alarmType: string;
      alarmContent: string;
      notifyStatus: string;
      triggerTime: number;
      notifyChannels?: string | null;
    };

    type JobAlarmEventSearchParams = CommonType.RecordNullable<
      Pick<Api.SystemManage.JobAlarmEvent, 'alarmType' | 'notifyStatus'> & { jobId: number } & CommonSearchParams
    >;

    type JobAlarmEventList = Common.PaginatingQueryRecord<JobAlarmEvent>;

    type AlarmNotifyRecord = {
      id: number;
      alarmEventId: number;
      channel: string;
      receiver: string;
      sendStatus: string;
      responseMessage?: string | null;
      sendTime?: number | null;
    };

    type JobAlarmEventDetail = Omit<JobAlarmEvent, 'notifyChannels'> & {
      notifyRecords: AlarmNotifyRecord[];
    };
  }
}
