import { transformRecordToOption } from '@/utils/common';

export const enableStatusRecord: Record<Api.Common.EnableStatus, App.I18n.I18nKey> = {
  '1': 'page.manage.common.status.enable',
  '2': 'page.manage.common.status.disable'
};

export const enableStatusOptions = transformRecordToOption(enableStatusRecord);

// Boolean status options for new API
export const enableStatusBooleanOptions: CommonType.Option<boolean>[] = [
  { value: true, label: 'page.manage.common.status.enable' },
  { value: false, label: 'page.manage.common.status.disable' }
];

export const userGenderRecord: Record<Api.SystemManage.UserGender, App.I18n.I18nKey> = {
  '1': 'page.manage.user.gender.male',
  '2': 'page.manage.user.gender.female'
};

export const userGenderOptions = transformRecordToOption(userGenderRecord);

export const menuTypeRecord: Record<Api.SystemManage.MenuType, App.I18n.I18nKey> = {
  1: 'page.manage.menu.type.directory',
  2: 'page.manage.menu.type.menu'
};

// Create options with number values manually (Object.entries converts number keys to strings)
export const menuTypeOptions: CommonType.Option<Api.SystemManage.MenuType, App.I18n.I18nKey>[] = [
  { value: 1, label: 'page.manage.menu.type.directory' },
  { value: 2, label: 'page.manage.menu.type.menu' }
];

export const menuIconTypeRecord: Record<Api.SystemManage.IconType, App.I18n.I18nKey> = {
  '1': 'page.manage.menu.iconType.iconify',
  '2': 'page.manage.menu.iconType.local'
};

export const menuIconTypeOptions = transformRecordToOption(menuIconTypeRecord);
