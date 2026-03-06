<script setup lang="tsx">
import { reactive } from 'vue';
import { useRoute } from 'vue-router';
import { NTag } from 'naive-ui';
import { defaultTransform, useNaivePaginatedTable } from '@/hooks/common/table';
import { fetchGetScheduleLogList } from '@/service/api';
import { useAppStore } from '@/store/modules/app';

const appStore = useAppStore();
const route = useRoute();

const searchParams: Api.SystemManage.ScheduleLogSearchParams = reactive({
  current: 1,
  size: 10,
  logId: route.query.logId ? Number(route.query.logId) : null,
  jobId: null,
  appCode: null,
  status: null,
  startTriggerTime: null,
  endTriggerTime: null
});

const statusOptions = [
  { label: 'PENDING', value: 'PENDING' },
  { label: 'RUNNING', value: 'RUNNING' },
  { label: 'SUCCESS', value: 'SUCCESS' },
  { label: 'FAILED', value: 'FAILED' },
  { label: 'TIMEOUT', value: 'TIMEOUT' }
];

const { columns, data, loading, getDataByPage, mobilePagination } = useNaivePaginatedTable({
  api: () => fetchGetScheduleLogList(searchParams),
  transform: response => defaultTransform(response),
  onPaginationParamsChange: params => {
    searchParams.current = params.page;
    searchParams.size = params.pageSize;
  },
  columns: () => [
    { key: 'id', title: '日志ID', align: 'center', width: 100 },
    { key: 'jobId', title: '任务ID', align: 'center', width: 100 },
    { key: 'appCode', title: '应用编码', align: 'center', width: 130 },
    { key: 'jobName', title: '任务名称', align: 'center', minWidth: 150 },
    {
      key: 'triggerTime',
      title: '触发时间',
      align: 'center',
      width: 180,
      render: row => new Date(row.triggerTime).toLocaleString('zh-CN')
    },
    {
      key: 'finishTime',
      title: '结束时间',
      align: 'center',
      width: 180,
      render: row => (row.finishTime ? new Date(row.finishTime).toLocaleString('zh-CN') : '-')
    },
    { key: 'triggerType', title: '触发类型', align: 'center', width: 120 },
    {
      key: 'status',
      title: '状态',
      align: 'center',
      width: 100,
      render: row => {
        const type: NaiveUI.ThemeColor = row.status === 'SUCCESS' ? 'success' : row.status === 'FAILED' ? 'error' : 'warning';
        return <NTag type={type}>{row.status}</NTag>;
      }
    },
    { key: 'attemptNo', title: '重试次数', align: 'center', width: 90 },
    {
      key: 'executorAddress',
      title: '执行节点',
      align: 'center',
      minWidth: 200,
      render: row => row.executorAddress || '-'
    },
    {
      key: 'errorMessage',
      title: '错误信息',
      align: 'left',
      minWidth: 260,
      ellipsis: { tooltip: true },
      render: row => row.errorMessage || '-'
    },
    { key: 'traceId', title: 'TraceId', align: 'center', minWidth: 240 }
  ]
});

function resetSearch() {
  searchParams.current = 1;
  searchParams.size = 10;
  searchParams.logId = null;
  searchParams.jobId = null;
  searchParams.appCode = null;
  searchParams.status = null;
  searchParams.startTriggerTime = null;
  searchParams.endTriggerTime = null;
  getDataByPage();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="调度日志查询" :bordered="false" size="small" class="card-wrapper">
      <NForm :model="searchParams" label-placement="left" :label-width="90">
        <NGrid responsive="screen" item-responsive>
          <NFormItemGi span="24 s:12 m:6" label="日志ID" class="pr-24px">
            <NInputNumber v-model:value="searchParams.logId" :min="1" clearable class="w-full" />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="任务ID" class="pr-24px">
            <NInputNumber v-model:value="searchParams.jobId" :min="1" clearable class="w-full" />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="应用编码" class="pr-24px">
            <NInput v-model:value="searchParams.appCode" clearable />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="状态" class="pr-24px">
            <NSelect v-model:value="searchParams.status" :options="statusOptions" clearable />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" class="pr-24px">
            <NSpace class="w-full" justify="end">
              <NButton @click="resetSearch">重置</NButton>
              <NButton type="primary" ghost @click="getDataByPage()">查询</NButton>
            </NSpace>
          </NFormItemGi>
        </NGrid>
      </NForm>

      <NDataTable
        :columns="columns"
        :data="data"
        size="small"
        :flex-height="!appStore.isMobile"
        :scroll-x="1800"
        :loading="loading"
        remote
        :row-key="row => row.id"
        :pagination="mobilePagination"
        class="sm:h-full"
      />
    </NCard>
  </div>
</template>

<style scoped></style>
