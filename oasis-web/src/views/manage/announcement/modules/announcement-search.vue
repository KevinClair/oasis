<script setup lang="ts">
import { $t } from '@/locales';

defineOptions({
  name: 'AnnouncementSearch'
});

interface Emits {
  (e: 'reset'): void;
  (e: 'search'): void;
}

const emit = defineEmits<Emits>();

const model = defineModel<Api.SystemManage.AnnouncementSearchParams>('model', { required: true });

const announcementTypeOptions = [
  { value: 'normal', label: '普通' },
  { value: 'warning', label: '警告' },
  { value: 'important', label: '重要通知' }
];


function reset() {
  emit('reset');
}

function search() {
  emit('search');
}
</script>

<template>
  <NCard :title="$t('common.search')" :bordered="false" size="small" class="card-wrapper">
    <NForm :model="model" label-placement="left" :label-width="80">
      <NGrid responsive="screen" item-responsive>
        <NFormItemGi span="24 s:12 m:6" label="公告标题" path="title" class="pr-24px">
          <NInput v-model:value="model.title" placeholder="请输入公告标题" clearable />
        </NFormItemGi>
        <NFormItemGi span="24 s:12 m:6" label="公告类型" path="type" class="pr-24px">
          <NSelect v-model:value="model.type" :options="announcementTypeOptions" placeholder="请选择公告类型" clearable />
        </NFormItemGi>
        <NFormItemGi span="24 s:12 m:6" class="pr-24px">
          <NSpace class="w-full" justify="end">
            <NButton @click="reset">
              <template #icon>
                <icon-ic-round-refresh class="text-icon" />
              </template>
              {{ $t('common.reset') }}
            </NButton>
            <NButton type="primary" ghost @click="search">
              <template #icon>
                <icon-ic-round-search class="text-icon" />
              </template>
              {{ $t('common.search') }}
            </NButton>
          </NSpace>
        </NFormItemGi>
      </NGrid>
    </NForm>
  </NCard>
</template>

<style scoped></style>

