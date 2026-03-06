<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { fetchGetAppAlarmTemplate, fetchSaveAppAlarmTemplate } from '@/service/api';

interface Props {
  visible: boolean;
  appCode: string;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'submitted'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const formModel = reactive<Api.SystemManage.AppAlarmTemplate>({
  appCode: '',
  receivers: [],
  channels: [],
  quietPeriodMinutes: 10,
  failThreshold: 1,
  timeoutSeconds: 30,
  enabled: true
});

const channelOptions = [
  { label: '邮件', value: 'EMAIL' },
  { label: '站内信', value: 'INBOX' },
  { label: 'Webhook', value: 'WEBHOOK' }
];

const receiverText = ref('');

watch(
  () => props.visible,
  async visible => {
    if (!visible || !props.appCode) return;

    const { data, error } = await fetchGetAppAlarmTemplate(props.appCode);
    if (error || !data) return;

    formModel.appCode = props.appCode;
    formModel.receivers = data.receivers || [];
    formModel.channels = data.channels || [];
    formModel.quietPeriodMinutes = data.quietPeriodMinutes ?? 10;
    formModel.failThreshold = data.failThreshold ?? 1;
    formModel.timeoutSeconds = data.timeoutSeconds ?? 30;
    formModel.enabled = data.enabled ?? true;
    receiverText.value = formModel.receivers.join(',');
  }
);

watch(
  () => receiverText.value,
  value => {
    formModel.receivers = value
      .split(',')
      .map(item => item.trim())
      .filter(Boolean);
  }
);

async function onSubmit() {
  const { error } = await fetchSaveAppAlarmTemplate(formModel);
  if (error) return;

  window.$message?.success('保存成功');
  emit('submitted');
  emit('update:visible', false);
}
</script>

<template>
  <NModal :show="visible" preset="card" title="应用默认告警模板" class="w-640px" @update:show="emit('update:visible', $event)">
    <NForm :model="formModel" label-placement="left" :label-width="120">
      <NFormItem label="应用编码">
        <NInput :value="appCode" disabled />
      </NFormItem>
      <NFormItem label="接收人(逗号分隔)">
        <NInput v-model:value="receiverText" placeholder="如: zhangsan,lisi" />
      </NFormItem>
      <NFormItem label="通知方式">
        <NSelect v-model:value="formModel.channels" multiple :options="channelOptions" />
      </NFormItem>
      <NGrid :cols="3" :x-gap="12">
        <NFormItemGi label="静默周期(分)">
          <NInputNumber v-model:value="formModel.quietPeriodMinutes" :min="1" :max="1440" class="w-full" />
        </NFormItemGi>
        <NFormItemGi label="失败阈值">
          <NInputNumber v-model:value="formModel.failThreshold" :min="1" :max="100" class="w-full" />
        </NFormItemGi>
        <NFormItemGi label="超时(秒)">
          <NInputNumber v-model:value="formModel.timeoutSeconds" :min="1" :max="3600" class="w-full" />
        </NFormItemGi>
      </NGrid>
      <NFormItem label="启用模板">
        <NSwitch v-model:value="formModel.enabled" />
      </NFormItem>
    </NForm>
    <template #footer>
      <NSpace justify="end">
        <NButton @click="emit('update:visible', false)">取消</NButton>
        <NButton type="primary" @click="onSubmit">保存</NButton>
      </NSpace>
    </template>
  </NModal>
</template>

<style scoped></style>
