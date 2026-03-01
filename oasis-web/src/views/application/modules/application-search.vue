<script setup lang="ts">
import { computed } from 'vue';
import { $t } from '@/locales';
import { enableStatusBooleanOptions } from '@/constants/business';

defineOptions({
  name: 'ApplicationSearch'
});

interface Emits {
  (e: 'reset'): void;
  (e: 'search'): void;
}

const emit = defineEmits<Emits>();

const model = defineModel<Api.SystemManage.ApplicationSearchParams>('model', { required: true });

const statusOptions = computed(() =>
  enableStatusBooleanOptions.map(item => ({
    label: $t(item.label as App.I18n.I18nKey),
    value: item.value as any
  }))
);

function reset() {
  model.value.appCode = null;
  model.value.appName = null;
  model.value.status = null;

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
        <NFormItemGi span="24 s:12 m:6" :label="$t('page.manage.application.appCode')" path="appCode" class="pr-24px">
          <NInput v-model:value="model.appCode" :placeholder="$t('page.manage.application.form.appCode')" />
        </NFormItemGi>
        <NFormItemGi span="24 s:12 m:6" :label="$t('page.manage.application.appName')" path="appName" class="pr-24px">
          <NInput v-model:value="model.appName" :placeholder="$t('page.manage.application.form.appName')" />
        </NFormItemGi>
        <NFormItemGi span="24 s:12 m:6" :label="$t('page.manage.application.status')" path="status" class="pr-24px">
          <NSelect
            v-model:value="model.status"
            :options="statusOptions"
            :placeholder="$t('page.manage.application.form.status')"
            clearable
          />
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

