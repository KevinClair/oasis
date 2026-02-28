<script setup lang="ts">
import { computed, ref, watch } from "vue";
import {
  enableStatusBooleanOptions,
  userGenderOptions,
} from "@/constants/business";
import { fetchGetAllRoles, fetchSaveUser } from "@/service/api";
import { useFormRules, useNaiveForm } from "@/hooks/common/form";
import { $t } from "@/locales";

defineOptions({
  name: "UserOperateDrawer",
});

interface Props {
  /** the type of operation */
  operateType: NaiveUI.TableOperateType;
  /** the edit row data */
  rowData?: Api.SystemManage.User | null;
}

const props = defineProps<Props>();

interface Emits {
  (e: "submitted"): void;
}

const emit = defineEmits<Emits>();

const visible = defineModel<boolean>("visible", {
  default: false,
});

const { formRef, validate, restoreValidation } = useNaiveForm();
const { defaultRequiredRule } = useFormRules();

const title = computed(() => {
  const titles: Record<NaiveUI.TableOperateType, string> = {
    add: $t("page.manage.user.addUser"),
    edit: $t("page.manage.user.editUser"),
  };
  return titles[props.operateType];
});

const isEdit = computed(() => props.operateType === "edit");

type Model = {
  id?: number;
  userId?: string;
  userAccount?: string;
  userName: string;
  password?: string;
  userGender: Api.SystemManage.UserGender | null;
  nickName: string;
  phone: string;
  email: string;
  userRoles: string[];
  status: boolean;
};

const model = ref(createDefaultModel());

const statusOptions = computed(() =>
  enableStatusBooleanOptions.map((item) => ({
    label: $t(item.label as App.I18n.I18nKey),
    value: item.value as any,
  })),
);

function createDefaultModel(): Model {
  return {
    userId: undefined,
    userAccount: "",
    userName: "",
    password: "",
    userGender: null,
    nickName: "",
    phone: "",
    email: "",
    userRoles: [],
    status: true,
  };
}

type RuleKey = Extract<keyof Model, "userName" | "status">;

const rules: Record<RuleKey, App.Global.FormRule> = {
  userName: defaultRequiredRule,
  status: defaultRequiredRule,
};

/** the enabled role options */
const roleOptions = ref<CommonType.Option<string>[]>([]);

async function getRoleOptions() {
  const { error, data } = await fetchGetAllRoles();

  if (!error) {
    roleOptions.value = data.map((item) => ({
      label: item.roleName,
      value: item.roleCode,
    }));
  }
}

function handleInitModel() {
  model.value = createDefaultModel();

  if (props.operateType === "edit" && props.rowData) {
    model.value = {
      id: props.rowData.id,
      userId: props.rowData.userId,
      userAccount: props.rowData.userAccount,
      userName: props.rowData.userName,
      password: "", // 编辑时不回显密码
      userGender: props.rowData.userGender,
      nickName: props.rowData.nickName,
      phone: props.rowData.userPhone,
      email: props.rowData.userEmail,
      userRoles: props.rowData.userRoles || [],
      status: props.rowData.status ?? true,
    };
  }
}

function closeDrawer() {
  visible.value = false;
}

async function handleSubmit() {
  await validate();

  const { error } = await fetchSaveUser(
    model.value as Api.SystemManage.UserEdit,
  );

  if (!error) {
    window.$message?.success($t("common.updateSuccess"));
    closeDrawer();
    emit("submitted");
  }
}

watch(visible, () => {
  if (visible.value) {
    handleInitModel();
    restoreValidation();
    getRoleOptions();
  }
});
</script>

<template>
  <NDrawer v-model:show="visible" display-directive="show" :width="360">
    <NDrawerContent :title="title" :native-scrollbar="false" closable>
      <NForm ref="formRef" :model="model" :rules="rules">
        <NFormItem :label="$t('page.manage.user.userId')" path="userId">
          <NInput
            v-model:value="model.userId"
            :placeholder="$t('page.manage.user.form.userId')"
            :disabled="isEdit"
          />
        </NFormItem>
        <NFormItem
          :label="$t('page.manage.user.userAccount')"
          path="userAccount"
        >
          <NInput
            v-model:value="model.userAccount"
            :placeholder="$t('page.manage.user.form.userAccount')"
            :disabled="isEdit"
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.user.userName')" path="userName">
          <NInput
            v-model:value="model.userName"
            :placeholder="$t('page.manage.user.form.userName')"
          />
        </NFormItem>
        <!-- 新增时显示密码字段，编辑时不显示 -->
        <NFormItem v-if="!isEdit" :label="$t('page.manage.user.password')" path="password">
          <NInput
            v-model:value="model.password"
            type="password"
            show-password-on="click"
            :placeholder="$t('page.manage.user.form.password')"
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.user.userGender')" path="userGender">
          <NRadioGroup v-model:value="model.userGender">
            <NRadio
              v-for="item in userGenderOptions"
              :key="item.value"
              :value="item.value"
              :label="$t(item.label)"
            />
          </NRadioGroup>
        </NFormItem>
        <NFormItem :label="$t('page.manage.user.nickName')" path="nickName">
          <NInput
            v-model:value="model.nickName"
            :placeholder="$t('page.manage.user.form.nickName')"
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.user.userPhone')" path="phone">
          <NInput
            v-model:value="model.phone"
            :placeholder="$t('page.manage.user.form.userPhone')"
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.user.userEmail')" path="email">
          <NInput
            v-model:value="model.email"
            :placeholder="$t('page.manage.user.form.userEmail')"
          />
        </NFormItem>
        <NFormItem :label="$t('page.manage.user.userStatus')" path="status">
          <NRadioGroup v-model:value="model.status">
            <NRadio
              v-for="item in statusOptions"
              :key="String(item.value)"
              :value="item.value"
              :label="item.label"
            />
          </NRadioGroup>
        </NFormItem>
        <NFormItem :label="$t('page.manage.user.userRole')" path="userRoles">
          <NSelect
            v-model:value="model.userRoles"
            multiple
            :options="roleOptions"
            :placeholder="$t('page.manage.user.form.userRole')"
          />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace :size="16">
          <NButton @click="closeDrawer">{{ $t("common.cancel") }}</NButton>
          <NButton type="primary" @click="handleSubmit">{{
            $t("common.confirm")
          }}</NButton>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped></style>
