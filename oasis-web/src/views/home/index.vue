<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { fetchGetLatestAnnouncement } from '@/service/api';
import { useAppStore } from '@/store/modules/app';
import HeaderBanner from './modules/header-banner.vue';
import CardData from './modules/card-data.vue';
import LineChart from './modules/line-chart.vue';
import PieChart from './modules/pie-chart.vue';
import ProjectNews from './modules/project-news.vue';
import CreativityBanner from './modules/creativity-banner.vue';

const appStore = useAppStore();

const gap = computed(() => (appStore.isMobile ? 0 : 16));

// 最新公告
const latestAnnouncement = ref<Api.SystemManage.Announcement | null>(null);
const announcementLoading = ref(false);

// 获取最新公告
async function getLatestAnnouncement() {
  announcementLoading.value = true;
  try {
    const { data } = await fetchGetLatestAnnouncement();
    latestAnnouncement.value = data || null;
  } finally {
    announcementLoading.value = false;
  }
}

// 根据公告类型获取Alert类型
const alertType = computed(() => {
  if (!latestAnnouncement.value) return 'info';

  const typeMap: Record<Api.SystemManage.AnnouncementType, 'success' | 'warning' | 'error' | 'info'> = {
    normal: 'success',
    warning: 'warning',
    important: 'error'
  };

  return typeMap[latestAnnouncement.value.type] || 'info';
});

onMounted(() => {
  getLatestAnnouncement();
});
</script>

<template>
  <NSpace vertical :size="16">
    <NAlert v-if="latestAnnouncement" :title="latestAnnouncement.title" :type="alertType" closable>
      <div v-html="latestAnnouncement.content"></div>
    </NAlert>
    <NAlert v-else-if="!announcementLoading" :title="$t('common.tip')" type="info">
      {{ $t('page.home.branchDesc') }}
    </NAlert>
    <HeaderBanner />
    <CardData />
    <NGrid :x-gap="gap" :y-gap="16" responsive="screen" item-responsive>
      <NGi span="24 s:24 m:14">
        <NCard :bordered="false" class="card-wrapper">
          <LineChart />
        </NCard>
      </NGi>
      <NGi span="24 s:24 m:10">
        <NCard :bordered="false" class="card-wrapper">
          <PieChart />
        </NCard>
      </NGi>
    </NGrid>
    <NGrid :x-gap="gap" :y-gap="16" responsive="screen" item-responsive>
      <NGi span="24 s:24 m:14">
        <ProjectNews />
      </NGi>
      <NGi span="24 s:24 m:10">
        <CreativityBanner />
      </NGi>
    </NGrid>
  </NSpace>
</template>

<style scoped></style>
