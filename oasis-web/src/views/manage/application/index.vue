<script setup lang="tsx">
import { reactive, ref } from 'vue';
import { NButton, NPopconfirm, NTag } from 'naive-ui';
import { defaultTransform, useNaivePaginatedTable, useTableOperate } from '@/hooks/common/table';
import { $t } from '@/locales';
import { fetchDeleteApplications, fetchGetApplicationList } from '@/service/api';
import { useAppStore } from '@/store/modules/app';
import ApplicationOperateDrawer from './modules/application-operate-drawer.vue';
import ApplicationSearch from './modules/application-search.vue';
import RegistrationNodesModal from './modules/registration-nodes-modal.vue';

const appStore = useAppStore();

const searchParams: Api.SystemManage.ApplicationSearchParams = reactive({
  current: 1,
  size: 10,
  status: null,
  appCode: null,
  appName: null
});

const nodesModalVisible = ref(false);
const selectedAppCode = ref<string>('');
const { columns, columnChecks, data, getData, getDataByPage, loading, mobilePagination } = useNaivePaginatedTable({
  api: () => fetchGetApplicationList(searchParams),
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
      key: 'appCode',
      title: $t('page.manage.application.appCode'),
      align: 'center',
      minWidth: 120
    },
    {
      key: 'appName',
      title: $t('page.manage.application.appName'),
      align: 'center',
      minWidth: 150
    },
    {
      key: 'description',
      title: $t('page.manage.application.description'),
      align: 'center',
      minWidth: 200,
      ellipsis: {
        tooltip: true
      }
    },
    {
      key: 'adminUserNames',
      title: $t('page.manage.application.adminUser'),
      align: 'center',
      minWidth: 200,
      render: row => {
        if (!row.adminUserNames || row.adminUserNames.length === 0) {
          return <span class="text-gray-400">-</span>;
        }

        return (
          <div class="flex flex-wrap justify-center gap-4px">
            {row.adminUserNames.map((name, index) => {
              const account = row.adminUserAccounts?.[index] || '';
              const label = account ? `${name}(${account})` : name;
              return (
                <NTag key={index} type="success" size="small">
                  {label}
                </NTag>
              );
            })}
          </div>
        );
      }
    },
    {
      key: 'developerNames',
      title: $t('page.manage.application.developers'),
      align: 'center',
      minWidth: 200,
      render: row => {
        if (!row.developerNames || row.developerNames.length === 0) {
          return <span class="text-gray-400">-</span>;
        }

        return (
          <div class="flex flex-wrap justify-center gap-4px">
            {row.developerNames.map((name, index) => {
              const account = row.developerAccounts?.[index] || '';
              const label = account ? `${name}(${account})` : name;
              return (
                <NTag key={index} type="info" size="small">
                  {label}
                </NTag>
              );
            })}
          </div>
        );
      }
    },
    {
      key: 'status',
      title: $t('page.manage.application.status'),
      align: 'center',
      width: 100,
      render: row => {
        if (row.status === null || row.status === undefined) {
          return null;
        }

        const isEnabled = row.status === true;
        const tagType: NaiveUI.ThemeColor = isEnabled ? 'success' : 'warning';
        const labelKey = isEnabled ? 'page.manage.common.status.enable' : 'page.manage.common.status.disable';

        return <NTag type={tagType}>{$t(labelKey)}</NTag>;
      }
    },
    {
      key: 'createByName',
      title: $t('page.manage.common.createBy'),
      align: 'center',
      width: 120
    },
    {
      key: 'createTime',
      title: $t('page.manage.common.createTime'),
      align: 'center',
      width: 180,
      render: row => {
        if (!row.createTime) return '-';
        return new Date(row.createTime).toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        }).replace(/\//g, '-');
      }
    },
    {
      key: 'operate',
      title: $t('common.operate'),
      align: 'center',
      width: 260,
      render: row => (
        <div class="flex-center gap-8px">
          <NButton type="info" ghost size="small" onClick={() => viewNodes(row.appCode)}>
            {$t('page.manage.application.viewRegistrationNodes')}
          </NButton>
          <NButton type="primary" ghost size="small" onClick={() => edit(row.id)}>
            {$t('common.edit')}
          </NButton>
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

const { drawerVisible, operateType, editingData, handleAdd, handleEdit, checkedRowKeys, onBatchDeleted, onDeleted } =
  useTableOperate(data, 'id', getData);

async function handleBatchDelete() {
  const ids = checkedRowKeys.value.map(key => Number(key));

  const { error } = await fetchDeleteApplications(ids);

  if (!error) {
    onBatchDeleted();
  }
}

async function handleDelete(id: number) {
  const { error } = await fetchDeleteApplications([id]);

  if (!error) {
    onDeleted();
  }
}

function edit(id: number) {
  handleEdit(id);
}

function viewNodes(appCode: string) {
  selectedAppCode.value = appCode;
  nodesModalVisible.value = true;
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <ApplicationSearch v-model:model="searchParams" @search="getDataByPage" />
    <NCard
      :title="$t('page.manage.application.title')"
      :bordered="false"
      size="small"
      class="card-wrapper sm:flex-1-hidden"
    >
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
        :scroll-x="1200"
        :loading="loading"
        remote
        :row-key="row => row.id"
        :pagination="mobilePagination"
        class="sm:h-full"
      />
      <ApplicationOperateDrawer
        v-model:visible="drawerVisible"
        :operate-type="operateType"
        :row-data="editingData"
        @submitted="getDataByPage"
      />
      <RegistrationNodesModal
        v-model:visible="nodesModalVisible"
        :app-code="selectedAppCode"
      />
    </NCard>
  </div>
</template>

<style scoped></style>

