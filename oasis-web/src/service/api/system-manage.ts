import { request } from '../request';

/** get role list */
export function fetchGetRoleList(params?: Api.SystemManage.RoleSearchParams) {
  return request<Api.SystemManage.RoleList>({
    url: '/systemManage/role/getRoleList',
    method: 'post',
    data: params
  });
}

/** save role (add/edit) */
export function fetchSaveRole(data: Api.SystemManage.RoleEdit) {
  return request<number>({
    url: '/systemManage/role/saveRole',
    method: 'post',
    data
  });
}

/** delete roles (batch delete supported) */
export function fetchDeleteRoles(ids: number[]) {
  return request<number>({
    url: '/systemManage/role/deleteRoles',
    method: 'post',
    data: {ids}
  });
}

/** toggle role status (enable/disable) */
export function fetchToggleRoleStatus(ids: number[]) {
  return request<number>({
    url: '/systemManage/role/toggleRoleStatus',
    method: 'post',
    data: {ids}
  });
}

/** get role by id */
export function fetchGetRoleById(id: number) {
  return request<Api.SystemManage.Role>({
    url: `/systemManage/role/getRoleById/${id}`,
    method: 'get'
  });
}

/**
 * get all enabled roles
 *
 * these roles are all enabled
 */
export function fetchGetAllRoles() {
  return request<Api.SystemManage.AllRole[]>({
    url: '/systemManage/role/getAllEnabledRoles',
    method: 'get'
  });
}

/** get role menu ids */
export function fetchGetRoleMenuIds(roleId: number) {
  return request<number[]>({
    url: `/systemManage/role/getRoleMenuIds/${roleId}`,
    method: 'get'
  });
}

/** save role menus */
export function fetchSaveRoleMenus(data: { roleId: number; menuIds: number[] }) {
  return request<number>({
    url: '/systemManage/role/saveRoleMenus',
    method: 'post',
    data
  });
}

/** get user list */
export function fetchGetUserList(params?: Api.SystemManage.UserSearchParams) {
  return request<Api.SystemManage.UserList>({
    url: '/systemManage/user/getUserList',
    method: 'post',
    data: params
  });
}

/** save user (add/edit) */
export function fetchSaveUser(data: Api.SystemManage.UserEdit) {
  return request<number>({
    url: '/systemManage/user/saveUser',
    method: 'post',
    data
  });
}

/** get user by id */
export function fetchGetUserById(id: number) {
  return request<Api.SystemManage.User>({
    url: `/systemManage/user/getUserById/${id}`,
    method: 'get'
  });
}

/** delete users (batch delete supported) */
export function fetchDeleteUsers(ids: number[]) {
  return request<number>({
    url: '/systemManage/user/deleteUsers',
    method: 'post',
    data: { ids }
  });
}

/** toggle user status (enable/disable) */
export function fetchToggleUserStatus(ids: number[]) {
  return request<number>({
    url: '/systemManage/user/toggleUserStatus',
    method: 'post',
    data: { ids }
  });
}

/** reset user password (batch reset supported) */
export function fetchResetPassword(ids: number[]) {
  return request<number>({
    url: '/systemManage/user/resetPassword',
    method: 'post',
    data: { ids }
  });
}

/**
 * get all enabled users
 *
 * these users are all enabled
 */
export function fetchGetAllUsers() {
  return request<Api.SystemManage.AllUser[]>({
    url: '/systemManage/user/getAllEnabledUsers',
    method: 'get'
  });
}

/** get menu list */
export function fetchGetMenuList(params?: { constant?: boolean; status?: boolean }) {
  return request<Api.SystemManage.MenuList>({
    url: '/systemManage/menu/getMenuList',
    method: 'get',
    params
  });
}

/** save menu (add/edit) */
export function fetchSaveMenu(data: Api.SystemManage.MenuEdit) {
  return request<number>({
    url: '/systemManage/menu/saveMenu',
    method: 'post',
    data
  });
}

/** get menu by id */
export function fetchGetMenuById(id: number) {
  return request<Api.SystemManage.Menu>({
    url: `/systemManage/menu/getMenuById/${id}`,
    method: 'get'
  });
}

/** delete menus (batch delete supported) */
export function fetchDeleteMenus(ids: number[]) {
  return request<number>({
    url: '/systemManage/menu/deleteMenus',
    method: 'post',
    data: {ids}
  });
}

/** get all pages */
export function fetchGetAllPages() {
  return request<string[]>({
    url: '/systemManage/getAllPages',
    method: 'get'
  });
}

/** get menu tree */
export function fetchGetMenuTree() {
  return request<Api.SystemManage.MenuTree[]>({
    url: '/systemManage/getMenuTree',
    method: 'get'
  });
}

/** get announcement list */
export function fetchGetAnnouncementList(params?: Api.SystemManage.AnnouncementSearchParams) {
  return request<Api.SystemManage.AnnouncementList>({
    url: '/systemManage/announcement/getAnnouncementList',
    method: 'post',
    data: params
  });
}

/** save announcement (add/edit) */
export function fetchSaveAnnouncement(data: Api.SystemManage.AnnouncementEdit) {
  return request<number>({
    url: '/systemManage/announcement/saveAnnouncement',
    method: 'post',
    data
  });
}

/** get announcement by id */
export function fetchGetAnnouncementById(id: number) {
  return request<Api.SystemManage.Announcement>({
    url: `/systemManage/announcement/getAnnouncementById/${id}`,
    method: 'get'
  });
}

/** get latest announcement */
export function fetchGetLatestAnnouncement() {
  return request<Api.SystemManage.Announcement>({
    url: '/systemManage/announcement/getLatestAnnouncement',
    method: 'get'
  });
}

/** delete announcements (batch delete supported) */
export function fetchDeleteAnnouncements(ids: number[]) {
  return request<number>({
    url: '/systemManage/announcement/deleteAnnouncements',
    method: 'post',
    data: { ids }
  });
}

/** get application list */
export function fetchGetApplicationList(params?: Api.SystemManage.ApplicationSearchParams) {
  return request<Api.SystemManage.ApplicationList>({
    url: '/systemManage/application/getApplicationList',
    method: 'post',
    data: params
  });
}

/** save application (add/edit) */
export function fetchSaveApplication(data: Api.SystemManage.ApplicationEdit) {
  return request<number>({
    url: '/systemManage/application/saveApplication',
    method: 'post',
    data
  });
}

/** delete applications (batch delete supported) */
export function fetchDeleteApplications(ids: number[]) {
  return request<number>({
    url: '/systemManage/application/deleteApplications',
    method: 'post',
    data: { ids }
  });
}

/** get application by id */
export function fetchGetApplicationById(id: number) {
  return request<Api.SystemManage.Application>({
    url: `/systemManage/application/getApplicationById/${id}`,
    method: 'get'
  });
}

/** get application registration nodes */
export function fetchGetRegistrationNodes(appCode: string) {
  return request<Api.SystemManage.RegistrationNode[]>({
    url: `/systemManage/application/getRegistrationNodes/${appCode}`,
    method: 'get'
  });
}

