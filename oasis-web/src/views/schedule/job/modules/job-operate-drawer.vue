<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { $t } from '@/locales';
import { fetchGetJobAlarmPolicy, fetchSaveJobAlarmPolicy, fetchSaveScheduleJob } from '@/service/api';

interface Props {
  visible: boolean;
  operateType: NaiveUI.TableOperateType;
  rowData?: Api.SystemManage.ScheduleJob | null;
}

interface Emits {
  (e: 'update:visible', value: boolean): void;
  (e: 'submitted'): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const title = computed(() => (props.operateType === 'add' ? '新增任务' : '编辑任务'));

const formModel = reactive<Api.SystemManage.ScheduleJobEdit>({
  id: undefined,
  appCode: '',
  jobName: '',
  handlerName: '',
  scheduleType: 'CRON',
  scheduleConf: '0 */5 * * * ?',
  routeStrategy: 'ROUND',
  blockStrategy: 'SERIAL',
  retryCount: 0,
  timeoutSeconds: 30,
  status: true,
  alarmInheritApp: true
});

const receiverText = ref('');
const channelOptions = [
  { label: '邮件', value: 'EMAIL' },
  { label: '站内信', value: 'INBOX' },
  { label: 'Webhook', value: 'WEBHOOK' }
];

const alarmPolicyModel = reactive<Api.SystemManage.JobAlarmPolicy>({
  jobId: 0,
  inheritAppTemplate: true,
  receivers: [],
  channels: [],
  quietPeriodMinutes: 10,
  failThreshold: 1,
  timeoutSeconds: 30,
  enabled: true
});

watch(
  () => props.visible,
  async visible => {
    if (!visible) return;

    if (props.operateType === 'edit' && props.rowData) {
      formModel.id = props.rowData.id;
      formModel.appCode = props.rowData.appCode;
      formModel.jobName = props.rowData.jobName;
      formModel.handlerName = props.rowData.handlerName;
      formModel.scheduleType = props.rowData.scheduleType;
      formModel.scheduleConf = props.rowData.scheduleConf;
      formModel.routeStrategy = props.rowData.routeStrategy;
      formModel.blockStrategy = props.rowData.blockStrategy;
      formModel.retryCount = props.rowData.retryCount;
      formModel.timeoutSeconds = props.rowData.timeoutSeconds;
      formModel.status = props.rowData.status ?? true;
      formModel.alarmInheritApp = props.rowData.alarmInheritApp ?? true;

      const { data: policyData, error } = await fetchGetJobAlarmPolicy(props.rowData.id);
      if (!error && policyData) {
        alarmPolicyModel.jobId = props.rowData.id;
        alarmPolicyModel.inheritAppTemplate = policyData.inheritAppTemplate;
        alarmPolicyModel.receivers = policyData.receivers || [];
        alarmPolicyModel.channels = policyData.channels || [];
        alarmPolicyModel.quietPeriodMinutes = policyData.quietPeriodMinutes ?? 10;
        alarmPolicyModel.failThreshold = policyData.failThreshold ?? 1;
        alarmPolicyModel.timeoutSeconds = policyData.timeoutSeconds ?? 30;
        alarmPolicyModel.enabled = policyData.enabled;
        receiverText.value = alarmPolicyModel.receivers.join(',');
      }
      return;
    }

    formModel.id = undefined;
    formModel.appCode = '';
    formModel.jobName = '';
    formModel.handlerName = '';
    formModel.scheduleType = 'CRON';
    formModel.scheduleConf = '0 */5 * * * ?';
    formModel.routeStrategy = 'ROUND';
    formModel.blockStrategy = 'SERIAL';
    formModel.retryCount = 0;
    formModel.timeoutSeconds = 30;
    formModel.status = true;
    formModel.alarmInheritApp = true;

    alarmPolicyModel.jobId = 0;
    alarmPolicyModel.inheritAppTemplate = true;
    alarmPolicyModel.receivers = [];
    alarmPolicyModel.channels = [];
    alarmPolicyModel.quietPeriodMinutes = 10;
    alarmPolicyModel.failThreshold = 1;
    alarmPolicyModel.timeoutSeconds = 30;
    alarmPolicyModel.enabled = true;
    receiverText.value = '';
  }
);

watch(
  () => formModel.alarmInheritApp,
  value => {
    alarmPolicyModel.inheritAppTemplate = value ?? true;
  }
);

watch(
  () => receiverText.value,
  value => {
    alarmPolicyModel.receivers = value
      .split(',')
      .map(item => item.trim())
      .filter(Boolean);
  }
);

const scheduleTypeOptions = [
  { label: 'CRON', value: 'CRON' },
  { label: 'FIXED_DELAY', value: 'FIXED_DELAY' },
  { label: 'ONCE', value: 'ONCE' }
];

const routeStrategyOptions = [
  { label: '轮询', value: 'ROUND' },
  { label: '随机', value: 'RANDOM' },
  { label: '故障转移', value: 'FAILOVER' },
  { label: '广播', value: 'BROADCAST' }
];

const blockStrategyOptions = [
  { label: '串行', value: 'SERIAL' },
  { label: '丢弃后续', value: 'DISCARD_LATER' },
  { label: '并行', value: 'PARALLEL' }
];

async function onSubmit() {
  const { data: jobId, error } = await fetchSaveScheduleJob(formModel);
  if (error) return;

  if (jobId) {
    alarmPolicyModel.jobId = jobId;
    alarmPolicyModel.inheritAppTemplate = formModel.alarmInheritApp ?? true;
    const { error: policyError } = await fetchSaveJobAlarmPolicy(alarmPolicyModel);
    if (policyError) return;
  }

  window.$message?.success($t('common.updateSuccess'));
  emit('submitted');
  emit('update:visible', false);
}
</script>

<template>
  <NDrawer :show="visible" :width="560" @update:show="emit('update:visible', $event)">
    <NDrawerContent :title="title" closable>
      <NForm :model="formModel" label-placement="left" :label-width="110">
        <NFormItem label="应用编码" path="appCode">
          <NInput v-model:value="formModel.appCode" placeholder="请输入应用编码" />
        </NFormItem>
        <NFormItem label="任务名称" path="jobName">
          <NInput v-model:value="formModel.jobName" placeholder="请输入任务名称" />
        </NFormItem>
        <NFormItem label="Handler" path="handlerName">
          <NInput v-model:value="formModel.handlerName" placeholder="请输入Handler" />
        </NFormItem>
        <NFormItem label="调度类型" path="scheduleType">
          <NSelect v-model:value="formModel.scheduleType" :options="scheduleTypeOptions" />
        </NFormItem>
        <NFormItem label="调度配置" path="scheduleConf">
          <NInput v-model:value="formModel.scheduleConf" placeholder="CRON表达式或间隔配置" />
        </NFormItem>
        <NFormItem label="路由策略" path="routeStrategy">
          <NSelect v-model:value="formModel.routeStrategy" :options="routeStrategyOptions" />
        </NFormItem>
        <NFormItem label="阻塞策略" path="blockStrategy">
          <NSelect v-model:value="formModel.blockStrategy" :options="blockStrategyOptions" />
        </NFormItem>
        <NGrid :cols="2" :x-gap="12">
          <NFormItemGi label="重试次数" path="retryCount">
            <NInputNumber v-model:value="formModel.retryCount" :min="0" :max="10" class="w-full" />
          </NFormItemGi>
          <NFormItemGi label="超时(秒)" path="timeoutSeconds">
            <NInputNumber v-model:value="formModel.timeoutSeconds" :min="1" :max="3600" class="w-full" />
          </NFormItemGi>
        </NGrid>
        <NGrid :cols="2" :x-gap="12">
          <NFormItemGi label="启用任务" path="status">
            <NSwitch v-model:value="formModel.status" />
          </NFormItemGi>
          <NFormItemGi label="继承应用告警" path="alarmInheritApp">
            <NSwitch v-model:value="formModel.alarmInheritApp" />
          </NFormItemGi>
        </NGrid>

        <NDivider title-placement="left">告警策略</NDivider>
        <NGrid :cols="2" :x-gap="12">
          <NFormItemGi label="继承应用模板" path="alarmInheritApp">
            <NSwitch v-model:value="formModel.alarmInheritApp" />
          </NFormItemGi>
          <NFormItemGi label="启用告警" path="alarmEnabled">
            <NSwitch v-model:value="alarmPolicyModel.enabled" :disabled="formModel.alarmInheritApp" />
          </NFormItemGi>
        </NGrid>
        <NFormItem label="接收人(逗号分隔)">
          <NInput
            v-model:value="receiverText"
            :disabled="formModel.alarmInheritApp"
            placeholder="如: zhangsan,lisi"
          />
        </NFormItem>
        <NFormItem label="通知方式">
          <NSelect
            v-model:value="alarmPolicyModel.channels"
            multiple
            :disabled="formModel.alarmInheritApp"
            :options="channelOptions"
            placeholder="请选择通知方式"
          />
        </NFormItem>
        <NGrid :cols="3" :x-gap="12">
          <NFormItemGi label="静默周期(分)">
            <NInputNumber
              v-model:value="alarmPolicyModel.quietPeriodMinutes"
              :disabled="formModel.alarmInheritApp"
              :min="1"
              :max="1440"
              class="w-full"
            />
          </NFormItemGi>
          <NFormItemGi label="失败阈值">
            <NInputNumber
              v-model:value="alarmPolicyModel.failThreshold"
              :disabled="formModel.alarmInheritApp"
              :min="1"
              :max="100"
              class="w-full"
            />
          </NFormItemGi>
          <NFormItemGi label="超时(秒)">
            <NInputNumber
              v-model:value="alarmPolicyModel.timeoutSeconds"
              :disabled="formModel.alarmInheritApp"
              :min="1"
              :max="3600"
              class="w-full"
            />
          </NFormItemGi>
        </NGrid>
      </NForm>

      <template #footer>
        <NSpace justify="end">
          <NButton @click="emit('update:visible', false)">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" @click="onSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped></style>
