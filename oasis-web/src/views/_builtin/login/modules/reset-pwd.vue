<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { useRouterPush } from '@/hooks/common/router';
import { useFormRules, useNaiveForm } from '@/hooks/common/form';
import { $t } from '@/locales';
import { fetchChangePassword } from '@/service/api/auth';

defineOptions({
  name: 'ResetPwd'
});

const { toggleLoginModule } = useRouterPush();
const { formRef, validate } = useNaiveForm();

interface FormModel {
  userAccount: string;
  oldPassword: string;
  password: string;
  confirmPassword: string;
}

const model: FormModel = reactive({
  userAccount: '',
  oldPassword: '',
  password: '',
  confirmPassword: ''
});

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = model.password;
  if (!pwd) return { level: 0, text: '', color: '' };

  let strength = 0;
  if (pwd.length >= 6) strength += 1;
  if (pwd.length >= 10) strength += 1;
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength += 1;
  else if (/[a-zA-Z]/.test(pwd)) strength += 1;
  if (/\d/.test(pwd)) strength += 1;
  if (/[@$!%*?&]/.test(pwd)) strength += 1;

  if (strength <= 2) return { level: 1, text: '弱', color: 'error' };
  if (strength <= 3) return { level: 2, text: '中', color: 'warning' };
  return { level: 3, text: '强', color: 'success' };
});

type RuleRecord = Partial<Record<keyof FormModel, App.Global.FormRule[]>>;

const rules = computed<RuleRecord>(() => {
  const { createConfirmPwdRule } = useFormRules();

  return {
    userAccount: [{ required: true, message: '请输入用户账号' }],
    oldPassword: [{ required: true, message: '请输入旧密码' }],
    password: [
      { required: true, message: '请输入新密码' },
      {
        pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{6,}$/,
        message: '密码必须包含字母和数字，且长度至少为6位',
        trigger: ['input', 'blur']
      }
    ],
    confirmPassword: createConfirmPwdRule(model.password)
  };
});

const loading = ref(false);

async function handleSubmit() {
  await validate();

  loading.value = true;

  const { error } = await fetchChangePassword({
    userAccount: model.userAccount,
    oldPassword: model.oldPassword,
    newPassword: model.password
  });

  loading.value = false;

  if (!error) {
    window.$message?.success('密码修改成功');
    toggleLoginModule('pwd-login');
  }
}
</script>

<template>
  <div>
    <!-- 密码规则提示 -->
    <NAlert type="info" :show-icon="false" class="mb-16px">
      <template #header>
        <div class="text-14px">密码规则</div>
      </template>
      <div class="text-12px space-y-4px">
        <div>• 长度至少6位</div>
        <div>• 必须包含字母和数字</div>
        <div>• 可包含特殊字符 @$!%*?&</div>
      </div>
    </NAlert>

    <NForm ref="formRef" :model="model" :rules="rules" size="large" :show-label="false" @keyup.enter="handleSubmit">
      <NFormItem path="userAccount">
        <NInput v-model:value="model.userAccount" placeholder="请输入用户账号">
          <template #prefix>
            <icon-ic-round-person class="text-icon" />
          </template>
        </NInput>
      </NFormItem>
      <NFormItem path="oldPassword">
        <NInput
          v-model:value="model.oldPassword"
          type="password"
          show-password-on="click"
          placeholder="请输入旧密码"
        >
          <template #prefix>
            <icon-ic-round-lock class="text-icon" />
          </template>
        </NInput>
      </NFormItem>
      <NFormItem path="password">
        <NInput
          v-model:value="model.password"
          type="password"
          show-password-on="click"
          placeholder="请输入新密码"
        >
          <template #prefix>
            <icon-ic-round-vpn-key class="text-icon" />
          </template>
        </NInput>
      </NFormItem>

      <!-- 密码强度指示器 -->
      <div v-if="model.password" class="mb-16px -mt-8px">
        <div class="flex items-center gap-8px text-12px">
          <span class="text-gray-500">密码强度:</span>
          <NTag :type="passwordStrength.color as any" size="small">
            {{ passwordStrength.text }}
          </NTag>
        </div>
      </div>

      <NFormItem path="confirmPassword">
        <NInput
          v-model:value="model.confirmPassword"
          type="password"
          show-password-on="click"
          placeholder="请再次输入新密码"
        >
          <template #prefix>
            <icon-ic-round-verified-user class="text-icon" />
          </template>
        </NInput>
      </NFormItem>
      <NSpace vertical :size="18" class="w-full">
        <NButton type="primary" size="large" round block :loading="loading" @click="handleSubmit">
          <template #icon>
            <icon-ic-round-check class="text-icon" />
          </template>
          {{ $t('common.confirm') }}
        </NButton>
        <NButton size="large" round block @click="toggleLoginModule('pwd-login')">
          <template #icon>
            <icon-ic-round-arrow-back class="text-icon" />
          </template>
          {{ $t('page.login.common.back') }}
        </NButton>
      </NSpace>
    </NForm>
  </div>
</template>

<style scoped>
.space-y-4px > * + * {
  margin-top: 4px;
}
</style>
