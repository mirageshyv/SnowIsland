<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLoreBySlug, LORE_DISCOVERY_WARNING } from '@/data/loreDocuments.js'
import { loreAPI } from '@/utils/api.js'

const route = useRoute()
const router = useRouter()
const userRole = (localStorage.getItem('userRole') || '').toLowerCase()

const accessAllowed = ref(false)
const accessChecked = ref(false)

const doc = computed(() => getLoreBySlug(route.params.slug))

onMounted(async () => {
  const slug = route.params.slug
  const role = localStorage.getItem('userRole') || ''
  const pid = parseInt(localStorage.getItem('playerId') || '0', 10) || null
  try {
    const data = await loreAPI.canAccess(slug, role, pid)
    accessAllowed.value = data?.allowed === true
    if (accessAllowed.value && pid && role.toLowerCase() !== 'dm') {
      await loreAPI.acknowledge(slug, pid)
    }
  } catch {
    accessAllowed.value = false
  } finally {
    accessChecked.value = true
  }
})

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push(userRole === 'dm' ? '/dm' : '/player')
  }
}

</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-slate-950 text-white">
    <div class="max-w-4xl mx-auto px-4 py-8">
      <button
        type="button"
        class="text-slate-400 hover:text-white text-sm mb-6"
        @click="goBack"
      >
        ← 返回
      </button>

      <div v-if="!accessChecked" class="text-center py-20 text-slate-400">
        加载中…
      </div>

      <div v-else-if="!doc" class="text-center py-20 text-slate-400">
        未找到该文献。
      </div>

      <div v-else-if="!accessAllowed" class="text-center py-20 text-slate-400">
        你尚未获得查阅此文献的权限。
      </div>

      <template v-else>
        <p class="text-amber-200/90 text-sm leading-relaxed mb-6 border border-amber-500/30 bg-amber-500/10 rounded-xl px-4 py-3">
          {{ LORE_DISCOVERY_WARNING }}
        </p>

        <h1 class="text-2xl font-semibold text-cyan-300 mb-2">{{ doc.title }}</h1>
        <p class="text-slate-500 text-xs mb-6 font-mono">图像文件：{{ doc.fileName }}</p>

        <div class="rounded-2xl border border-white/10 bg-black/40 overflow-hidden shadow-2xl">
          <img
            :src="doc.imageUrl"
            :alt="doc.title"
            class="w-full h-auto object-contain"
          />
        </div>
      </template>
    </div>
  </div>
</template>
