<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { TreeOption } from 'naive-ui';
import { fetchGetMenuList, fetchGetRoleMenuIds, fetchSaveRoleMenus } from '@/service/api';
import { $t } from '@/locales';

defineOptions({
  name: 'MenuAuthModal'
});

interface Props {
  /** the roleId */
  roleId: number;
}

const props = defineProps<Props>();

const visible = defineModel<boolean>('visible', {
  default: false
});

function closeModal() {
  visible.value = false;
}

const title = computed(() => $t('common.edit') + $t('page.manage.role.menuAuth'));

// 选中的菜单ID列表
const checkedKeys = ref<number[]>([]);

// 菜单树数据
const menuTreeData = ref<TreeOption[]>([]);

// 加载状态
const loading = ref(false);

// 保存加载状态
const saveLoading = ref(false);

/**
 * 将菜单列表转换为树形结构
 */
function convertToTree(menus: Api.SystemManage.Menu[]): TreeOption[] {
  return menus.map(menu => {
    const node: TreeOption = {
      key: menu.id,
      label: menu.menuName,
      children: menu.children && menu.children.length > 0 ? convertToTree(menu.children) : undefined
    };
    return node;
  });
}

/**
 * 获取菜单树数据（仅查询启用的非常量路由）
 */
async function getMenuTree() {
  loading.value = true;
  try {
    // 只查询启用的非常量路由（constant=false, status=true）
    const { data, error } = await fetchGetMenuList({ constant: false, status: true });
    if (!error && data) {
      menuTreeData.value = convertToTree(data.records || []);
    }
  } finally {
    loading.value = false;
  }
}

/**
 * 获取角色已选中的菜单ID
 */
async function getRoleMenus() {
  if (props.roleId <= 0) {
    return;
  }

  loading.value = true;
  try {
    const { data, error } = await fetchGetRoleMenuIds(props.roleId);
    if (!error && data) {
      checkedKeys.value = data;
    }
  } finally {
    loading.value = false;
  }
}

/**
 * 保存菜单权限
 */
async function handleSave() {
  saveLoading.value = true;
  try {
    const { error } = await fetchSaveRoleMenus({
      roleId: props.roleId,
      menuIds: checkedKeys.value
    });

    if (!error) {
      window.$message?.success($t('common.updateSuccess'));
      closeModal();
    }
  } finally {
    saveLoading.value = false;
  }
}

// 监听弹窗显示状态
watch(visible, newVal => {
  if (newVal) {
    // 弹窗打开时加载数据
    getMenuTree();
    getRoleMenus();
  } else {
    // 弹窗关闭时清空数据
    checkedKeys.value = [];
    menuTreeData.value = [];
  }
});
</script>

<template>
  <NModal v-model:show="visible" :title="title" preset="card" class="w-700px">
    <NSpin :show="loading">
      <div class="flex-col gap-16px">
        <div class="text-14px text-#666">
          {{ $t('page.manage.role.form.menuAuth') }}
        </div>
        <NTree
          v-model:checked-keys="checkedKeys"
          :data="menuTreeData"
          checkable
          cascade
          key-field="key"
          label-field="label"
          children-field="children"
          :default-expand-all="true"
          class="max-h-400px overflow-auto"
        />
      </div>
    </NSpin>

    <template #footer>
      <NSpace justify="end">
        <NButton @click="closeModal">{{ $t('common.cancel') }}</NButton>
        <NButton type="primary" :loading="saveLoading" @click="handleSave">
          {{ $t('common.confirm') }}
        </NButton>
      </NSpace>
    </template>
  </NModal>
</template>

<style scoped></style>

