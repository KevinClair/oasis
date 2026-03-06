<script setup lang="tsx">
import { reactive } from 'vue';
import { NButton, NPopconfirm, NTag } from 'naive-ui';
import { useRouter } from 'vue-router';
import { defaultTransform, useNaivePaginatedTable, useTableOperate } from '@/hooks/common/table';
import { $t } from '@/locales';
import {
  fetchEnableScheduleJobs,
  fetchGetScheduleJobList,
  fetchTriggerScheduleJob
} from '@/service/api';
import { useAppStore } from '@/store/modules/app';
import JobOperateDrawer from './modules/job-operate-drawer.vue';

const appStore = useAppStore();
const router = useRouter();

const searchParams: Api.SystemManage.ScheduleJobSearchParams = reactive({
  current: 1,
  size: 10,
  appCode: null,
  jobName: null,
  handlerName: null,
  scheduleType: null
});

const { columns, data, loading, getData, getDataByPage, columnChecks, mobilePagination } = useNaivePaginatedTable({
  api: () => fetchGetScheduleJobList(searchParams),
  transform: response => defaultTransform(response),
  onPaginationParamsChange: params => {
    searchParams.current = params.page;
    searchParams.size = params.pageSize;
  },
  columns: () => [
    { type: 'selection', align: 'center', width: 48 },
    { key: 'id', title: 'ID', align: 'center', width: 80 },
    { key: 'appCode', title: '应用编码', align: 'center', width: 140 },
    { key: 'jobName', title: '任务名称', align: 'center', minWidth: 160 },
    { key: 'handlerName', title: 'Handler', align: 'center', minWidth: 160 },
    { key: 'scheduleType', title: '调度类型', align: 'center', width: 120 },
    { key: 'scheduleConf', title: '调度配置', align: 'center', minWidth: 200 },
    {
      key: 'status',
      title: '状态',
      align: 'center',
      width: 100,
      render: row => <NTag type={row.status ? 'success' : 'warning'}>{row.status ? '启用' : '停用'}</NTag>
    },
    {
      key: 'nextTriggerTime',
      title: '下次触发时间',
      align: 'center',
      width: 180,
      render: row => (row.nextTriggerTime ? new Date(row.nextTriggerTime).toLocaleString('zh-CN') : '-')
    },
    {
      key: 'operate',
      title: $t('common.operate'),
      align: 'center',
      width: 320,
      render: row => (
        <div class="flex-center gap-8px">
          <NButton type="primary" ghost size="small" onClick={() => handleEdit(row.id)}>
            {$t('common.edit')}
          </NButton>
          <NButton type="info" ghost size="small" onClick={() => handleTrigger(row.id)}>
            立即触发
          </NButton>
          <NButton type="default" ghost size="small" onClick={() => viewAlarmEvents(row.id)}>
            告警事件
          </NButton>
          <NPopconfirm onPositiveClick={() => handleToggleStatus(row.id, !row.status)}>
            {{
              default: () => (row.status ? '确认停用该任务吗？' : '确认启用该任务吗？'),
              trigger: () => (
                <NButton type={row.status ? 'warning' : 'success'} ghost size="small">
                  {row.status ? '停用' : '启用'}
                </NButton>
              )
            }}
          </NPopconfirm>
        </div>
      )
    }
  ]
});

const { drawerVisible, operateType, editingData, handleAdd, handleEdit, checkedRowKeys } = useTableOperate(
  data,
  'id',
  getData
);

const scheduleTypeOptions = [
  { label: 'CRON', value: 'CRON' },
  { label: 'FIXED_DELAY', value: 'FIXED_DELAY' },
  { label: 'ONCE', value: 'ONCE' }
];

async function handleToggleStatus(id: number, status: boolean) {
  const { error } = await fetchEnableScheduleJobs([id], status);
  if (!error) {
    await getData();
  }
}

async function handleBatchEnable(status: boolean) {
  const ids = checkedRowKeys.value.map(key => Number(key));
  if (!ids.length) return;
  const { error } = await fetchEnableScheduleJobs(ids, status);
  if (!error) {
    await getData();
  }
}

async function handleTrigger(id: number) {
  const { error } = await fetchTriggerScheduleJob(id);
  if (!error) {
    window.$message?.success('触发成功');
  }
}

function viewAlarmEvents(jobId: number) {
  router.push(`/schedule/job/alarm-events/${jobId}`);
}

function resetSearch() {
  searchParams.current = 1;
  searchParams.size = 10;
  searchParams.appCode = null;
  searchParams.jobName = null;
  searchParams.handlerName = null;
  searchParams.scheduleType = null;
  getDataByPage();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="任务查询" :bordered="false" size="small" class="card-wrapper">
      <NForm :model="searchParams" label-placement="left" :label-width="90">
        <NGrid responsive="screen" item-responsive>
          <NFormItemGi span="24 s:12 m:6" label="应用编码" path="appCode" class="pr-24px">
            <NInput v-model:value="searchParams.appCode" placeholder="请输入应用编码" clearable />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="任务名称" path="jobName" class="pr-24px">
            <NInput v-model:value="searchParams.jobName" placeholder="请输入任务名称" clearable />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="Handler" path="handlerName" class="pr-24px">
            <NInput v-model:value="searchParams.handlerName" placeholder="请输入Handler" clearable />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="调度类型" path="scheduleType" class="pr-24px">
            <NSelect v-model:value="searchParams.scheduleType" :options="scheduleTypeOptions" clearable />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" class="pr-24px">
            <NSpace class="w-full" justify="end">
              <NButton @click="resetSearch">{{ $t('common.reset') }}</NButton>
              <NButton type="primary" ghost @click="getDataByPage()">{{ $t('common.search') }}</NButton>
            </NSpace>
          </NFormItemGi>
        </NGrid>
      </NForm>
    </NCard>

    <NCard title="任务管理" :bordered="false" size="small" class="card-wrapper sm:flex-1-hidden">
      <template #header-extra>
        <TableHeaderOperation
          v-model:columns="columnChecks"
          :disabled-delete="checkedRowKeys.length === 0"
          :loading="loading"
          @add="handleAdd"
          @refresh="getData"
        >
          <template #default>
            <NButton size="small" ghost type="primary" @click="handleAdd">
              <template #icon>
                <icon-ic-round-plus class="text-icon" />
              </template>
              新增任务
            </NButton>
            <NButton size="small" ghost type="success" :disabled="checkedRowKeys.length === 0" @click="handleBatchEnable(true)">
              批量启用
            </NButton>
            <NButton size="small" ghost type="warning" :disabled="checkedRowKeys.length === 0" @click="handleBatchEnable(false)">
              批量停用
            </NButton>
          </template>
        </TableHeaderOperation>
      </template>

      <NDataTable
        v-model:checked-row-keys="checkedRowKeys"
        :columns="columns"
        :data="data"
        size="small"
        :flex-height="!appStore.isMobile"
        :scroll-x="1480"
        :loading="loading"
        remote
        :row-key="row => row.id"
        :pagination="mobilePagination"
        class="sm:h-full"
      />

      <JobOperateDrawer
        v-model:visible="drawerVisible"
        :operate-type="operateType"
        :row-data="editingData"
        @submitted="getDataByPage"
      />
    </NCard>
  </div>
</template>

<style scoped></style>
