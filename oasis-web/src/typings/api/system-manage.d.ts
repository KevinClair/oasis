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
      userId: number;
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
      userId?: number;
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
  }
}
