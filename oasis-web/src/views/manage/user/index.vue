<script setup lang="tsx">
import { reactive } from 'vue';
import { NButton, NPopconfirm, NTag } from 'naive-ui';
import { userGenderRecord } from '@/constants/business';
import { defaultTransform, useNaivePaginatedTable, useTableOperate } from '@/hooks/common/table';
import { $t } from '@/locales';
import {fetchDeleteUsers, fetchGetUserList, fetchToggleUserStatus} from '@/service/api';
import {useAppStore} from '@/store/modules/app';
import UserOperateDrawer from './modules/user-operate-drawer.vue';
import UserSearch from './modules/user-search.vue';

const appStore = useAppStore();

const searchParams: Api.SystemManage.UserSearchParams = reactive({
  current: 1,
  size: 10,
  status: null,
  userId: null,
  userAccount: null,
  userName: null,
  userGender: null,
  nickName: null,
  userPhone: null,
  userEmail: null
});

const { columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination } = useNaivePaginatedTable({
  api: () => fetchGetUserList(searchParams),
  transform: response => defaultTransform(response),
  onPaginationParamsChange: params => {
    searchParams.current = params.page;
    searchParams.size = params.pageSize;
  },
  columns: () => [
    {
      type: 'selection',
      align: 'center',
      width: 48
    },
    {
      key: 'userId',
      title: $t('page.manage.user.userId' as App.I18n.I18nKey),
      align: 'center',
      width: 100
    },
    {
      key: 'userAccount',
      title: $t('page.manage.user.userAccount' as App.I18n.I18nKey),
      align: 'center',
      minWidth: 120
    },
    {
      key: 'userName',
      title: $t('page.manage.user.userName'),
      align: 'center',
      minWidth: 100
    },
    {
      key: 'userGender',
      title: $t('page.manage.user.userGender'),
      align: 'center',
      width: 100,
      render: row => {
        if (row.userGender === null) {
          return null;
        }

        const tagMap: Record<Api.SystemManage.UserGender, NaiveUI.ThemeColor> = {
          1: 'primary',
          2: 'error'
        };

        const label = $t(userGenderRecord[row.userGender]);

        return <NTag type={tagMap[row.userGender]}>{label}</NTag>;
      }
    },
    {
      key: 'nickName',
      title: $t('page.manage.user.nickName'),
      align: 'center',
      minWidth: 100
    },
    {
      key: 'userPhone',
      title: $t('page.manage.user.userPhone'),
      align: 'center',
      width: 120
    },
    {
      key: 'userEmail',
      title: $t('page.manage.user.userEmail'),
      align: 'center',
      minWidth: 200
    },
    {
      key: 'userRoles',
      title: $t('page.manage.user.userRole'),
      align: 'center',
      minWidth: 150,
      render: row => {
        if (!row.userRoles || row.userRoles.length === 0) {
          return <span class="text-gray-400">-</span>;
        }

        return (
          <div class="flex flex-wrap justify-center gap-8px">
            {row.userRoles.map(roleCode => (
              <NTag key={roleCode} type="info" size="small">
                {roleCode}
              </NTag>
            ))}
          </div>
        );
      }
    },
    {
      key: 'status',
      title: $t('page.manage.user.userStatus'),
      align: 'center',
      width: 100,
      render: row => {
        if (row.status === null || row.status === undefined) {
          return null;
        }

        // Boolean type: true = enabled, false = disabled
        const isEnabled = row.status === true;
        const tagType: NaiveUI.ThemeColor = isEnabled ? 'success' : 'warning';
        const labelKey = isEnabled ? 'page.manage.common.status.enable' : 'page.manage.common.status.disable';

        return <NTag type={tagType}>{$t(labelKey)}</NTag>;
      }
    },
    {
      key: 'operate',
      title: $t('common.operate'),
      align: 'center',
      width: 230,
      render: row => (
        <div class="flex-center gap-8px">
          <NButton type="primary" ghost size="small" onClick={() => edit(row.id)}>
            {$t('common.edit')}
          </NButton>
          <NPopconfirm onPositiveClick={() => handleToggleStatus(row.id)}>
            {{
              default: () =>
                row.status
                  ? $t('page.manage.user.confirmDisable' as App.I18n.I18nKey)
                  : $t('page.manage.user.confirmEnable' as App.I18n.I18nKey),
              trigger: () => (
                <NButton type={row.status ? 'warning' : 'success'} ghost size="small">
                  {row.status
                    ? $t('page.manage.user.disable' as App.I18n.I18nKey)
                    : $t('page.manage.user.enable' as App.I18n.I18nKey)}
                </NButton>
              )
            }}
          </NPopconfirm>
          <NPopconfirm onPositiveClick={() => handleDelete(row.id)}>
            {{
              default: () => $t('common.confirmDelete'),
              trigger: () => (
                <NButton type="error" ghost size="small">
                  {$t('common.delete')}
                </NButton>
              )
            }}
          </NPopconfirm>
        </div>
      )
    }
  ]
});

const {
  drawerVisible,
  operateType,
  editingData,
  handleAdd,
  handleEdit,
  checkedRowKeys,
  onBatchDeleted,
  onDeleted
  // closeDrawer
} = useTableOperate(data, 'id', getData);

async function handleBatchDelete() {
  const ids = checkedRowKeys.value.map(key => Number(key));

  const { error } = await fetchDeleteUsers(ids);

  if (!error) {
    onBatchDeleted();
  }
}

async function handleDelete(id: number) {
  const { error } = await fetchDeleteUsers([id]);

  if (!error) {
    onDeleted();
  }
}

async function handleBatchToggleStatus() {
  const ids = checkedRowKeys.value.map(key => Number(key));

  const { error } = await fetchToggleUserStatus(ids);

  if (!error) {
    await getData();
  }
}

async function handleToggleStatus(id: number) {
  const { error } = await fetchToggleUserStatus([id]);

  if (!error) {
    await getData();
  }
}

function edit(id: number) {
  handleEdit(id);
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <UserSearch v-model:model="searchParams" @search="getDataByPage" />
    <NCard :title="$t('page.manage.user.title')" :bordered="false" size="small" class="card-wrapper sm:flex-1-hidden">
      <template #header-extra>
        <TableHeaderOperation
          v-model:columns="columnChecks"
          :disabled-delete="checkedRowKeys.length === 0"
          :loading="loading"
          @add="handleAdd"
          @delete="handleBatchDelete"
          @refresh="getData"
        >
          <template #default>
            <NButton size="small" ghost type="primary" @click="handleAdd">
              <template #icon>
                <icon-ic-round-plus class="text-icon" />
              </template>
              {{ $t('common.add') }}
            </NButton>
            <NButton
              size="small"
              type="warning"
              ghost
              :disabled="checkedRowKeys.length === 0"
              @click="handleBatchToggleStatus"
            >
              <template #icon>
                <icon-ic-round-swap-horiz class="text-icon"/>
              </template>
              {{ $t('page.manage.user.batchToggleStatus') }}
            </NButton>
            <NPopconfirm @positive-click="handleBatchDelete">
              <template #trigger>
                <NButton size="small" ghost type="error" :disabled="checkedRowKeys.length === 0">
                  <template #icon>
                    <icon-ic-round-delete class="text-icon" />
                  </template>
                  {{ $t('common.batchDelete') }}
                </NButton>
              </template>
              {{ $t('common.confirmDelete') }}
            </NPopconfirm>
          </template>
        </TableHeaderOperation>
      </template>
      <NDataTable
        v-model:checked-row-keys="checkedRowKeys"
        :columns="columns"
        :data="data"
        size="small"
        :flex-height="!appStore.isMobile"
        :scroll-x="962"
        :loading="loading"
        remote
        :row-key="row => row.id"
        :pagination="mobilePagination"
        class="sm:h-full"
      />
      <UserOperateDrawer
        v-model:visible="drawerVisible"
        :operate-type="operateType"
        :row-data="editingData"
        @submitted="getDataByPage"
      />
    </NCard>
  </div>
</template>

<style scoped></style>
