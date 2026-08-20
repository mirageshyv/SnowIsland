<script setup>
import { ref, onMounted } from 'vue'
import { dmPlayerAPI } from '@/utils/api.js'
import PlayerNotebookView from './PlayerNotebookView.vue'

const players = ref([])
const selectedPlayerId = ref(null)
const loadingPlayers = ref(true)

async function loadPlayers() {
  loadingPlayers.value = true
  try {
    const result = await dmPlayerAPI.list()
    players.value = Array.isArray(result?.players) ? result.players : []
    if (!selectedPlayerId.value && players.value.length) {
      selectedPlayerId.value = players.value[0].id
    }
  } catch {
    players.value = []
  } finally {
    loadingPlayers.value = false
  }
}

function selectPlayer(id) {
  selectedPlayerId.value = id
}

onMounted(() => loadPlayers())
</script>

<template>
  <div>
    <div class="mb-5">
      <h1 class="text-white text-2xl font-semibold tracking-tight mb-1">玩家笔记本</h1>
      <p class="text-gray-500 text-sm">选择一名玩家，查看其笔记本</p>
    </div>

    <div class="mb-5">
      <p class="text-gray-400 text-xs mb-2">选择玩家</p>
      <div v-if="loadingPlayers" class="text-gray-500 text-sm">加载中…</div>
      <div v-else class="flex flex-wrap gap-2">
        <button
          v-for="p in players"
          :key="p.id"
          type="button"
          class="px-3 py-1.5 rounded-xl text-sm border"
          :class="selectedPlayerId === p.id
            ? 'bg-white/15 text-white border-white/25'
            : 'bg-black/20 text-gray-300 border-white/10 hover:border-white/20'"
          @click="selectPlayer(p.id)"
        >
          {{ p.name }}
          <span v-if="p.faction" class="text-gray-500 ml-1">{{ p.faction }}</span>
        </button>
        <p v-if="!players.length" class="text-gray-500 text-sm">暂无玩家</p>
      </div>
    </div>

    <PlayerNotebookView
      v-if="selectedPlayerId"
      :key="selectedPlayerId"
      read-only
      :player-id="selectedPlayerId"
    />
    <p v-else-if="!loadingPlayers" class="text-gray-500 text-sm py-10 text-center">请选择一名玩家</p>
  </div>
</template>
