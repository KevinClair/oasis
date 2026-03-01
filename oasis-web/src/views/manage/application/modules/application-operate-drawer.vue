<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { enableStatusBooleanOptions } from '@/constants/business';
import { fetchGetAllUsers, fetchSaveApplication } from '@/service/api';
import { useFormRules, useNaiveForm } from '@/hooks/common/form';
import { $t } from '@/locales';

defineOptions({
  name: 'ApplicationOperateDrawer'
});

interface Props {
  /** the type of operation */
  operateType: NaiveUI.TableOperateType;
  /** the edit row data */
  rowData?: Api.SystemManage.Application | null;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'submitted'): void;
}

const emit = defineEmits<Emits>();

const visible = defineModel<boolean>('visible', {
  default: false
});

const { formRef, validate, restoreValidation } = useNaiveForm();
const { defaultRequiredRule } = useFormRules();

const title = computed(() => {
  const titles: Record<NaiveUI.TableOperateType, string> = {
    add: $t('page.manage.application.addApplication'),
    edit: $t('page.manage.application.editApplication')
  };
  return titles[props.operateType];
});

const isEdit = computed(() => props.operateType === 'edit');

type Model = {
  id?: number;
  appCode: string;
  appName: string;
  appKey?: string;
  description: string;
  adminUserIds: string[];
  developerUserIds: string[];
  status: boolean;
};

const model = ref(createDefaultModel());

const statusOptions = computed(() =>
  enableStatusBooleanOptions.map(item => ({
    label: $t(item.label as App.I18n.I18nKey),
    value: item.value as any
  }))
);

function createDefaultModel(): Model {
  return {
    appCode: '',
    appName: '',
    appKey: '',
    description: '',
    adminUserIds: [],
    developerUserIds: [],
    status: true
  };
}

type RuleKey = Extract<keyof Model, 'appCode' | 'appName' | 'description' | 'status'>;

const rules: Record<RuleKey, App.Global.FormRule> = {
  appCode: defaultRequiredRule,
  appName: defaultRequiredRule,
  description: defaultRequiredRule,
  status: defaultRequiredRule
};

/** the user options for admin and developers */
const userOptions = ref<CommonType.Option<string>[]>([]);

async function getUserOptions() {
  const { data, error } = await fetchGetAllUsers();

  if (!error && data) {
    // Filter to only show enabled users
    userOptions.value = data
      .filter(user => user.status === true)
      .map(user => ({
        label: `${user.userName}(${user.userAccount})`,
        value: user.userId
      }));
  }
}

function handleInitModel() {
  model.value = createDefaultModel();

  if (props.operateType === 'edit' && props.rowData) {
    model.value = {
      id: props.rowData.id,
      appCode: props.rowData.appCode,
      appName: props.rowData.appName,
      appKey: props.rowData.appKey,
      description: props.rowData.description,
      adminUserIds: props.rowData.adminUserIds || [],
      developerUserIds: props.rowData.developerUserIds || [],
      status: props.rowData.status ?? true
    };
  }
}

function closeDrawer() {
  visible.value = false;
}

async function handleSubmit() {
  await validate();

  const { error } = await fetchSaveApplication(model.value as Api.SystemManage.ApplicationEdit);

  if (!error) {
    window.$message?.success($t('common.updateSuccess'));
    closeDrawer();
    emit('submitted');
  }
}

watch(visible, () => {
  if (visible.value) {
    handleInitModel();
    restoreValidation();
    getUserOptions();
  }
});
</script>

<template>
  <NDrawer v-model:show="visible" display-directive="show" :width="480">
    <NDrawerContent :title="title" :native-scrollbar="false" closable>
      <NForm ref="formRef" :model="model" :rules="rules">
        <NFormItem :label="$t('page.manage.application.appCode')" path="appCode">
          <NInput
            v-model:value="model.appCode"
            :placeholder="$t('page.manage.application.form.appCode')"
            :disabled="isEdit"
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.application.appName')" path="appName">
          <NInput v-model:value="model.appName" :placeholder="$t('page.manage.application.form.appName')" />
        </NFormItem>
        <NFormItem v-if="isEdit" :label="$t('page.manage.application.appKey')" path="appKey">
          <NInput v-model:value="model.appKey" :placeholder="$t('page.manage.application.form.appKey')" disabled />
        </NFormItem>
        <NFormItem :label="$t('page.manage.application.description')" path="description">
          <NInput
            v-model:value="model.description"
            type="textarea"
            :placeholder="$t('page.manage.application.form.description')"
            :autosize="{ minRows: 3, maxRows: 6 }"
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.application.adminUser')" path="adminUserIds">
          <NSelect
            v-model:value="model.adminUserIds"
            :options="userOptions"
            :placeholder="$t('page.manage.application.form.adminUser')"
            clearable
            filterable
            multiple
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.application.developers')" path="developerUserIds">
          <NSelect
            v-model:value="model.developerUserIds"
            :options="userOptions"
            :placeholder="$t('page.manage.application.form.developers')"
            clearable
            filterable
            multiple
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.application.status')" path="status">
          <NRadioGroup v-model:value="model.status">
            <NRadio
              v-for="item in statusOptions"
              :key="String(item.value)"
              :value="item.value"
              :label="item.label"
            />
          </NRadioGroup>
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace :size="16">
          <NButton @click="closeDrawer">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped></style>

