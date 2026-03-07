<script setup lang="tsx">
import {reactive} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {NButton, NTag} from 'naive-ui';
import {defaultTransform, useNaivePaginatedTable} from '@/hooks/common/table';
import {fetchGetScheduleAlarmEvents} from '@/service/api';
import {useAppStore} from '@/store/modules/app';

const appStore = useAppStore();
const route = useRoute();
const router = useRouter();

function resolveJobId() {
  const queryValue = route.query.jobId;
  const raw = Array.isArray(queryValue) ? queryValue[0] : queryValue;
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

const searchParams: Api.SystemManage.JobAlarmEventSearchParams = reactive({
  current: 1,
  size: 10,
  jobId: resolveJobId(),
  alarmType: null,
  notifyStatus: null
});

const { columns, data, loading, getDataByPage, mobilePagination } = useNaivePaginatedTable({
  api: () => fetchGetScheduleAlarmEvents(searchParams),
  transform: response => defaultTransform(response),
  onPaginationParamsChange: params => {
    searchParams.current = params.page;
    searchParams.size = params.pageSize;
  },
  columns: () => [
    { key: 'id', title: '事件ID', align: 'center', width: 90 },
    { key: 'alarmType', title: '告警类型', align: 'center', width: 140 },
    {
      key: 'notifyStatus',
      title: '通知状态',
      align: 'center',
      width: 120,
      render: row => {
        const type: NaiveUI.ThemeColor = row.notifyStatus === 'SUCCESS' ? 'success' : row.notifyStatus === 'FAILED' ? 'error' : 'warning';
        return <NTag type={type}>{row.notifyStatus}</NTag>;
      }
    },
    {
      key: 'notifyChannels',
      title: '通知方式',
      align: 'center',
      width: 180,
      render: row => row.notifyChannels || '-'
    },
    {
      key: 'triggerTime',
      title: '触发时间',
      align: 'center',
      width: 180,
      render: row => new Date(row.triggerTime).toLocaleString('zh-CN')
    },
    {
      key: 'alarmContent',
      title: '告警内容',
      align: 'left',
      minWidth: 360,
      ellipsis: { tooltip: true }
    },
    {
      key: 'fireLogId',
      title: '调度日志ID',
      align: 'center',
      width: 140,
      render: row => {
        return (
          <NButton text type="primary" onClick={() => router.push(`/schedule/log?logId=${row.fireLogId}`)}>
            {row.fireLogId}
          </NButton>
        );
      }
    }
  ]
});

const alarmTypeOptions = [
  { label: '失败告警', value: 'FAIL' },
  { label: '超时告警', value: 'TIMEOUT' },
  { label: '连续失败告警', value: 'CONSECUTIVE_FAIL' }
];

const notifyStatusOptions = [
  { label: '待发送', value: 'PENDING' },
  { label: '发送成功', value: 'SUCCESS' },
  { label: '发送失败', value: 'FAILED' }
];

function resetSearch() {
  searchParams.current = 1;
  searchParams.size = 10;
  searchParams.jobId = null;
  searchParams.alarmType = null;
  searchParams.notifyStatus = null;
  getDataByPage();
}
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard :title="searchParams.jobId ? `任务 ${searchParams.jobId} 告警事件` : '告警事件'" :bordered="false"
           size="small" class="card-wrapper">
      <NForm :model="searchParams" label-placement="left" :label-width="90">
        <NGrid responsive="screen" item-responsive>
          <NFormItemGi span="24 s:12 m:6" label="任务ID" class="pr-24px">
            <NInputNumber v-model:value="searchParams.jobId" :min="1" clearable class="w-full"/>
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="告警类型" class="pr-24px">
            <NSelect v-model:value="searchParams.alarmType" :options="alarmTypeOptions" clearable />
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="通知状态" class="pr-24px">
            <NSelect v-model:value="searchParams.notifyStatus" :options="notifyStatusOptions" clearable />
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
        :scroll-x="1400"
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
