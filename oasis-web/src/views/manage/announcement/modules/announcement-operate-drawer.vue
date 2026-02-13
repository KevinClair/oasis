<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { fetchSaveAnnouncement } from '@/service/api';
import { useFormRules, useNaiveForm } from '@/hooks/common/form';
import { $t } from '@/locales';

defineOptions({
  name: 'AnnouncementOperateDrawer'
});

interface Props {
  /** the type of operation */
  operateType: NaiveUI.TableOperateType;
  /** the edit row data */
  rowData?: Api.SystemManage.Announcement | null;
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
    add: $t('page.manage.announcement.addAnnouncement' as App.I18n.I18nKey),
    edit: $t('page.manage.announcement.editAnnouncement' as App.I18n.I18nKey)
  };
  return titles[props.operateType];
});

type Model = Pick<Api.SystemManage.Announcement, 'title' | 'content' | 'type'> & {
  id?: number;
};

const model = ref(createDefaultModel());

const announcementTypeOptions = [
  { value: 'normal', label: $t('page.manage.announcement.type.normal' as App.I18n.I18nKey), color: 'success' },
  { value: 'warning', label: $t('page.manage.announcement.type.warning' as App.I18n.I18nKey), color: 'warning' },
  {
    value: 'important',
    label: $t('page.manage.announcement.type.important' as App.I18n.I18nKey),
    color: 'error'
  }
];

function createDefaultModel(): Model {
  return {
    title: '',
    content: '',
    type: 'normal'
  };
}

type RuleKey = Extract<keyof Model, 'title' | 'content' | 'type'>;

const rules: Record<RuleKey, App.Global.FormRule> = {
  title: defaultRequiredRule,
  content: defaultRequiredRule,
  type: defaultRequiredRule
};

function handleInitModel() {
  model.value = createDefaultModel();

  if (props.operateType === 'edit' && props.rowData) {
    model.value = {
      id: props.rowData.id,
      title: props.rowData.title,
      content: props.rowData.content,
      type: props.rowData.type
    };
  }
}

function closeDrawer() {
  visible.value = false;
}

async function handleSubmit() {
  await validate();

  const { error } = await fetchSaveAnnouncement(model.value as Api.SystemManage.AnnouncementEdit);

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
  }
});
</script>

<template>
  <NDrawer v-model:show="visible" display-directive="show" :width="600">
    <NDrawerContent :title="title" :native-scrollbar="false" closable>
      <NForm ref="formRef" :model="model" :rules="rules" label-placement="left" :label-width="100">
        <NFormItem :label="$t('page.manage.announcement.announcementTitle' as App.I18n.I18nKey)" path="title">
          <NInput
            v-model:value="model.title"
            :placeholder="$t('page.manage.announcement.form.title' as App.I18n.I18nKey)"
            maxlength="200"
            show-count
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.announcement.announcementType' as App.I18n.I18nKey)" path="type">
          <NRadioGroup v-model:value="model.type">
            <NRadio v-for="item in announcementTypeOptions" :key="item.value" :value="item.value" :label="item.label" />
          </NRadioGroup>
        </NFormItem>
        <NFormItem :label="$t('page.manage.announcement.announcementContent' as App.I18n.I18nKey)" path="content">
          <NInput
            v-model:value="model.content"
            type="textarea"
            :placeholder="$t('page.manage.announcement.form.content' as App.I18n.I18nKey)"
            :autosize="{ minRows: 6, maxRows: 12 }"
          />
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

