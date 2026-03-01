import { request } from '../request';

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

