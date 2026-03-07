<script setup lang="tsx">
import {onMounted, reactive, ref} from 'vue';
import {NButton, NTag} from 'naive-ui';
import {defaultTransform, useNaivePaginatedTable} from '@/hooks/common/table';
import {fetchGetDispatchQueueList, fetchGetDispatchQueueOverview} from '@/service/api';
import {useAppStore} from '@/store/modules/app';

const appStore = useAppStore();

const statusOptions = [
  {label: 'PENDING', value: 'PENDING'},
  {label: 'PROCESSING', value: 'PROCESSING'},
  {label: 'SUCCESS', value: 'SUCCESS'},
  {label: 'DEAD', value: 'DEAD'}
];

const searchParams: Api.SystemManage.DispatchQueueSearchParams = reactive({
  current: 1,
  size: 10,
  status: null,
  fireLogId: null,
  jobId: null
});

const overview = ref<Api.SystemManage.DispatchQueueOverview>({
  pendingCount: 0,
  processingCount: 0,
  successCount: 0,
  deadCount: 0,
  duePendingCount: 0
});

const {columns, data, loading, getDataByPage, mobilePagination} = useNaivePaginatedTable({
  api: () => fetchGetDispatchQueueList(searchParams),
  transform: response => defaultTransform(response),
  onPaginationParamsChange: params => {
    searchParams.current = params.page;
    searchParams.size = params.pageSize;
  },
  columns: () => [
    {key: 'id', title: '队列ID', align: 'center', width: 90},
    {key: 'fireLogId', title: '日志ID', align: 'center', width: 90},
    {key: 'jobId', title: '任务ID', align: 'center', width: 90},
    {
      key: 'status',
      title: '状态',
      align: 'center',
      width: 110,
      render: row => {
        let type: NaiveUI.ThemeColor = 'default';
        if (row.status === 'SUCCESS') type = 'success';
        else if (row.status === 'DEAD') type = 'error';
        else if (row.status === 'PROCESSING') type = 'warning';
        else if (row.status === 'PENDING') type = 'info';
        return <NTag type={type}>{row.status}</NTag>;
      }
    },
    {key: 'retryCount', title: '已重试次数', align: 'center', width: 110},
    {
      key: 'nextRetryTime',
      title: '下次重试时间',
      align: 'center',
      width: 180,
      render: row => formatTime(row.nextRetryTime)
    },
    {
      key: 'targetAddress',
      title: '目标节点',
      align: 'center',
      minWidth: 180,
      render: row => row.targetAddress || '-'
    },
    {
      key: 'payload',
      title: '重试载荷',
      align: 'left',
      minWidth: 320,
      ellipsis: {tooltip: true},
      render: row => row.payload || '-'
    },
    {
      key: 'updateTime',
      title: '更新时间',
      align: 'center',
      width: 180,
      render: row => formatTime(row.updateTime)
    }
  ]
});

async function loadOverview() {
  const {data, error} = await fetchGetDispatchQueueOverview();
  if (!error && data) {
    overview.value = data;
  }
}

async function handleSearch() {
  await getDataByPage();
  await loadOverview();
}

function resetSearch() {
  searchParams.current = 1;
  searchParams.size = 10;
  searchParams.status = null;
  searchParams.fireLogId = null;
  searchParams.jobId = null;
  void handleSearch();
}

function formatTime(value: number | string | null | undefined) {
  if (value === null || value === undefined || value === '') {
    return '-';
  }
  const date = typeof value === 'number' ? new Date(value) : new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value);
  }
  return date.toLocaleString('zh-CN');
}

onMounted(async () => {
  await loadOverview();
});
</script>

<template>
  <div class="min-h-500px flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard title="补偿队列概览" :bordered="false" size="small" class="card-wrapper">
      <NGrid :cols="5" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
        <NGi span="24 s:12 m:8 l:4">
          <NStatistic label="PENDING" :value="overview.pendingCount"/>
        </NGi>
        <NGi span="24 s:12 m:8 l:4">
          <NStatistic label="PROCESSING" :value="overview.processingCount"/>
        </NGi>
        <NGi span="24 s:12 m:8 l:4">
          <NStatistic label="SUCCESS" :value="overview.successCount"/>
        </NGi>
        <NGi span="24 s:12 m:8 l:4">
          <NStatistic label="DEAD" :value="overview.deadCount"/>
        </NGi>
        <NGi span="24 s:12 m:8 l:4">
          <NStatistic label="到期待重试" :value="overview.duePendingCount"/>
        </NGi>
      </NGrid>
    </NCard>

    <NCard title="补偿队列查询" :bordered="false" size="small" class="card-wrapper">
      <NForm :model="searchParams" label-placement="left" :label-width="100">
        <NGrid responsive="screen" item-responsive>
          <NFormItemGi span="24 s:12 m:6" label="状态" class="pr-24px">
            <NSelect v-model:value="searchParams.status" :options="statusOptions" clearable/>
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="日志ID" class="pr-24px">
            <NInputNumber v-model:value="searchParams.fireLogId" :min="1" clearable class="w-full"/>
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" label="任务ID" class="pr-24px">
            <NInputNumber v-model:value="searchParams.jobId" :min="1" clearable class="w-full"/>
          </NFormItemGi>
          <NFormItemGi span="24 s:12 m:6" class="pr-24px">
            <NSpace class="w-full" justify="end">
              <NButton @click="resetSearch">重置</NButton>
              <NButton type="primary" ghost @click="handleSearch()">查询</NButton>
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
