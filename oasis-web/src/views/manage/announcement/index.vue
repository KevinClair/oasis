<script setup lang="tsx">
import type { Ref } from 'vue';
import { reactive, ref } from 'vue';
import { NButton, NPopconfirm, NTag } from 'naive-ui';
import { useBoolean } from '@sa/hooks';
import { fetchDeleteAnnouncements, fetchGetAnnouncementList } from '@/service/api';
import { useAppStore } from '@/store/modules/app';
import { defaultTransform, useNaivePaginatedTable, useTableOperate } from '@/hooks/common/table';
import { $t } from '@/locales';
import AnnouncementOperateDrawer from './modules/announcement-operate-drawer.vue';

const appStore = useAppStore();

const { bool: visible, setTrue: openDrawer } = useBoolean();

const wrapperRef = ref<HTMLElement | null>(null);

// 搜索参数
const searchParams: Api.SystemManage.AnnouncementSearchParams = reactive({
  current: 1,
  size: 10,
  title: null,
  type: null
});

const { columns, columnChecks, data, loading, mobilePagination, getData, getDataByPage } = useNaivePaginatedTable({
  api: () => fetchGetAnnouncementList(searchParams),
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
      key: 'id',
      title: $t('page.manage.announcement.id' as App.I18n.I18nKey),
      align: 'center',
      width: 80
    },
    {
      key: 'title',
      title: $t('page.manage.announcement.announcementTitle' as App.I18n.I18nKey),
      align: 'center',
      minWidth: 200
    },
    {
      key: 'type',
      title: $t('page.manage.announcement.announcementType' as App.I18n.I18nKey),
      align: 'center',
      width: 120,
      render: (row: Api.SystemManage.Announcement) => {
        const typeMap: Record<
          Api.SystemManage.AnnouncementType,
          { label: App.I18n.I18nKey; type: NaiveUI.ThemeColor }
        > = {
          normal: { label: 'page.manage.announcement.type.normal' as App.I18n.I18nKey, type: 'success' },
          warning: { label: 'page.manage.announcement.type.warning' as App.I18n.I18nKey, type: 'warning' },
          important: { label: 'page.manage.announcement.type.important' as App.I18n.I18nKey, type: 'error' }
        };

        const config = typeMap[row.type];

        return <NTag type={config.type}>{$t(config.label)}</NTag>;
      }
    },
    {
      key: 'content',
      title: $t('page.manage.announcement.announcementContent' as App.I18n.I18nKey),
      align: 'center',
      minWidth: 300,
      ellipsis: {
        tooltip: true
      }
    },
    {
      key: 'createBy',
      title: $t('page.manage.announcement.createBy' as App.I18n.I18nKey),
      align: 'center',
      width: 120
    },
    {
      key: 'createTime',
      title: $t('page.manage.announcement.createTime' as App.I18n.I18nKey),
      align: 'center',
      width: 180
    },
    {
      key: 'updateBy',
      title: $t('page.manage.announcement.updateBy' as App.I18n.I18nKey),
      align: 'center',
      width: 120
    },
    {
      key: 'updateTime',
      title: $t('page.manage.announcement.updateTime' as App.I18n.I18nKey),
      align: 'center',
      width: 180
    },
    {
      key: 'operate',
      title: $t('common.operate'),
      align: 'center',
      width: 200,
      fixed: 'right',
      render: (row: Api.SystemManage.Announcement) => (
        <div class="flex-center gap-8px">
          <NButton type="primary" ghost size="small" onClick={() => handleEdit(row)}>
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

const { checkedRowKeys, onBatchDeleted, onDeleted } = useTableOperate(data, 'id', getData);

const operateType = ref<NaiveUI.TableOperateType>('add');

function handleAdd() {
  operateType.value = 'add';
  openDrawer();
}

async function handleBatchDelete() {
  const ids = checkedRowKeys.value.map(key => Number(key));
  const { error } = await fetchDeleteAnnouncements(ids);

  if (!error) {
    window.$message?.success($t('common.deleteSuccess'));
    onBatchDeleted();
  }
}

async function handleDelete(id: number) {
  const { error } = await fetchDeleteAnnouncements([id]);

  if (!error) {
    window.$message?.success($t('common.deleteSuccess'));
    onDeleted();
  }
}

const editingData: Ref<Api.SystemManage.Announcement | null> = ref(null);

function handleEdit(item: Api.SystemManage.Announcement) {
  operateType.value = 'edit';
  editingData.value = { ...item };

  openDrawer();
}
</script>

<template>
  <div ref="wrapperRef" class="flex-col-stretch gap-16px overflow-hidden lt-sm:overflow-auto">
    <NCard
      :title="$t('page.manage.announcement.title' as App.I18n.I18nKey)"
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
        />
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
      <AnnouncementOperateDrawer
        v-model:visible="visible"
        :operate-type="operateType"
        :row-data="editingData"
        @submitted="getDataByPage"
      />
    </NCard>
  </div>
</template>

<style scoped></style>

