<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { NDataTable, NEmpty, NModal, NSpin } from 'naive-ui';
import { fetchGetRegistrationNodes } from '@/service/api';
import { $t } from '@/locales';

defineOptions({
  name: 'RegistrationNodesModal'
});

interface Props {
  /** Modal visible */
  visible: boolean;
  /** Application code */
  appCode: string;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'update:visible', visible: boolean): void;
}

const emit = defineEmits<Emits>();

const modalVisible = computed({
  get() {
    return props.visible;
  },
  set(visible) {
    emit('update:visible', visible);
  }
});

const loading = ref(false);
const nodes = ref<Api.SystemManage.RegistrationNode[]>([]);

const columns = [
  {
    key: 'ipAddress',
    title: $t('page.manage.application.registrationNode.ipAddress'),
    align: 'center' as const,
    minWidth: 150
  },
  {
    key: 'machineTag',
    title: $t('page.manage.application.registrationNode.machineTag'),
    align: 'center' as const,
    minWidth: 200,
    render: (row: Api.SystemManage.RegistrationNode) => row.machineTag || '-'
  },
  {
    key: 'registerTime',
    title: $t('page.manage.application.registrationNode.registerTime'),
    align: 'center' as const,
    minWidth: 180,
    render: (row: Api.SystemManage.RegistrationNode) => {
      if (!row.registerTime) return '-';
      return new Date(row.registerTime)
        .toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          hour12: false
        })
        .replace(/\//g, '-');
    }
  },
  {
    key: 'extraInfo',
    title: $t('page.manage.application.registrationNode.extraInfo'),
    align: 'center' as const,
    minWidth: 150,
    render: (row: Api.SystemManage.RegistrationNode) => row.extraInfo || '-'
  }
];

async function fetchNodes() {
  if (!props.appCode) return;

  loading.value = true;
  try {
    const { data, error } = await fetchGetRegistrationNodes(props.appCode);
    if (!error) {
      nodes.value = data || [];
    }
  } finally {
    loading.value = false;
  }
}

watch(
  () => props.visible,
  visible => {
    if (visible) {
      fetchNodes();
    } else {
      nodes.value = [];
    }
  }
);
</script>

<template>
  <NModal
    v-model:show="modalVisible"
    preset="card"
    :title="$t('page.manage.application.registrationNode.title')"
    class="w-800px"
  >
    <NSpin :show="loading">
      <NDataTable
        v-if="nodes.length > 0"
        :columns="columns"
        :data="nodes"
        :row-key="(row: Api.SystemManage.RegistrationNode) => row.id"
        size="small"
        :max-height="400"
      />
      <NEmpty v-else :description="$t('page.manage.application.registrationNode.noData')" class="h-200px" />
    </NSpin>
  </NModal>
</template>

<style scoped></style>


defineOptions({
  name: 'RegistrationNodesModal'
});

interface Props {
  /** Modal visible */
  visible: boolean;
  /** Application code */
  appCode: string;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'update:visible', visible: boolean): void;
}

const emit = defineEmits<Emits>();

const modalVisible = computed({
  get() {
    return props.visible;
  },
  set(visible) {
    emit('update:visible', visible);
  }
});

const loading = ref(false);
const nodes = ref<Api.SystemManage.RegistrationNode[]>([]);

const columns = [
  {
    key: 'ipAddress',
    title: $t('page.manage.application.registrationNode.ipAddress'),
    align: 'center' as const,
    minWidth: 150
  },
  {
    key: 'machineTag',
    title: $t('page.manage.application.registrationNode.machineTag'),
    align: 'center' as const,
    minWidth: 200,
    render: (row: Api.SystemManage.RegistrationNode) => row.machineTag || '-'
  },
  {
    key: 'registerTime',
    title: $t('page.manage.application.registrationNode.registerTime'),
    align: 'center' as const,
    minWidth: 180,
    render: (row: Api.SystemManage.RegistrationNode) => {
      if (!row.registerTime) return '-';
      return new Date(row.registerTime).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).replace(/\//g, '-');
    }
  },
  {
    key: 'extraInfo',
    title: $t('page.manage.application.registrationNode.extraInfo'),
    align: 'center' as const,
    minWidth: 150,
    render: (row: Api.SystemManage.RegistrationNode) => row.extraInfo || '-'
  }
];

async function fetchNodes() {
  if (!props.appCode) return;

  loading.value = true;
  try {
    const { data, error } = await fetchGetRegistrationNodes(props.appCode);
    if (!error) {
      nodes.value = data || [];
    }
  } finally {
    loading.value = false;
  }
}

watch(() => props.visible, (visible) => {
  if (visible) {
    fetchNodes();
  } else {
    nodes.value = [];
  }
});
</script>

<template>
  <NModal v-model:show="modalVisible" preset="card" :title="$t('page.manage.application.registrationNode.title')"
    class="w-800px">
    <NSpin :show="loading">
      <NDataTable
        v-if="nodes.length > 0"
        :columns="columns"
        :data="nodes"
        :row-key="(row: Api.SystemManage.RegistrationNode) => row.id"
        size="small"
        :max-height="400"
      />
      <NEmpty v-else :description="$t('page.manage.application.registrationNode.noData')" class="h-200px" />
    </NSpin>
  </NModal>
</template>

<style scoped></style>


