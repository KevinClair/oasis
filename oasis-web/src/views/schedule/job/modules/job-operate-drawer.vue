<script setup lang="ts">
import { computed, reactive, watch } from 'vue';
import { $t } from '@/locales';
import { fetchSaveScheduleJob } from '@/service/api';

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

watch(
  () => props.visible,
  visible => {
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
  const { error } = await fetchSaveScheduleJob(formModel);
  if (error) return;

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
