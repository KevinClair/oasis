<script setup lang="tsx">
import { reactive } from 'vue';
import { NButton, NPopconfirm, NTag } from 'naive-ui';
import {fetchDeleteRoles, fetchGetRoleList, fetchToggleRoleStatus} from '@/service/api';
import { useAppStore } from '@/store/modules/app';
import { defaultTransform, useNaivePaginatedTable, useTableOperate } from '@/hooks/common/table';
import { $t } from '@/locales';
import RoleOperateDrawer from './modules/role-operate-drawer.vue';
import RoleSearch from './modules/role-search.vue';

const appStore = useAppStore();

const searchParams: Api.SystemManage.RoleSearchParams = reactive({
  current: 1,
  size: 10,
  roleName: null,
  roleCode: null,
  status: null
});

const { columns, columnChecks, data, loading, getData, getDataByPage, mobilePagination } = useNaivePaginatedTable({
  api: () => fetchGetRoleList(searchParams),
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
      key: 'roleName',
      title: $t('page.manage.role.roleName'),
      align: 'center',
      minWidth: 120
    },
    {
      key: 'roleCode',
      title: $t('page.manage.role.roleCode'),
      align: 'center',
      minWidth: 120
    },
    {
      key: 'roleDesc',
      title: $t('page.manage.role.roleDesc'),
      minWidth: 120
    },
    {
      key: 'status',
      title: $t('page.manage.role.roleStatus'),
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
                  ? $t('page.manage.role.confirmDisable' as App.I18n.I18nKey)
                  : $t('page.manage.role.confirmEnable' as App.I18n.I18nKey),
              trigger: () => (
                <NButton type={row.status ? 'warning' : 'success'} ghost size="small">
                  {row.status
                    ? $t('page.manage.role.disable' as App.I18n.I18nKey)
                    : $t('page.manage.role.enable' as App.I18n.I18nKey)}
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

const {drawerVisible, operateType, editingData, handleAdd, handleEdit, checkedRowKeys, onBatchDeleted, onDeleted} =
  useTableOperate(data, 'id', getData);

async function handleBatchDelete() {
  const ids = checkedRowKeys.value.map(key => Number(key));

  const {error} = await fetchDeleteRoles(ids);

  if (!error) {
    onBatchDeleted();
  }
}

async function handleDelete(id: number) {
  const {error} = await fetchDeleteRoles([id]);

  if (!error) {
    onDeleted();
  }
}

async function handleBatchToggleStatus() {
  const ids = checkedRowKeys.value.map(key => Number(key));

  const {error} = await fetchToggleRoleStatus(ids);

  if (!error) {
    await getData();
  }
}

async function handleToggleStatus(id: number) {
  const {error} = await fetchToggleRoleStatus([id]);

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
    <RoleSearch v-model:model="searchParams" @search="getDataByPage" />
    <NCard :title="$t('page.manage.role.title')" :bordered="false" size="small" class="card-wrapper sm:flex-1-hidden">
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
                <icon-ic-round-plus class="text-icon"/>
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
              {{ $t('page.manage.role.batchToggleStatus') }}
            </NButton>
            <NPopconfirm @positive-click="handleBatchDelete">
              <template #trigger>
                <NButton size="small" ghost type="error" :disabled="checkedRowKeys.length === 0">
                  <template #icon>
                    <icon-ic-round-delete class="text-icon"/>
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
        :scroll-x="902"
        :loading="loading"
        remote
        :row-key="row => row.id"
        :pagination="mobilePagination"
        class="sm:h-full"
      />
      <RoleOperateDrawer
        v-model:visible="drawerVisible"
        :operate-type="operateType"
        :row-data="editingData"
        @submitted="getDataByPage"
      />
    </NCard>
  </div>
</template>

<style scoped></style>
