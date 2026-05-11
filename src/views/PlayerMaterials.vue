<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const username = localStorage.getItem('username') || ''

const currentRoute = computed(() => route.name)

const handleLogout = () => {
  localStorage.removeItem('userRole')
  localStorage.removeItem('username')
  localStorage.removeItem('playerId')
  router.push('/')
}

onMounted(() => {
  console.log('Player materials page mounted')
})
</script>

<template>
  <!-- h-screen + overflow-hidden：侧栏固定；仅右侧 main 纵向滚动 -->
  <div class="flex h-screen max-h-[100dvh] overflow-hidden bg-[#0a0e1a]">
    <!-- Sidebar -->
    <aside class="flex h-full w-64 shrink-0 flex-col border-r border-[#1f2937] bg-[#0f1419]">
      <div class="shrink-0 border-b border-[#1f2937] p-6">
        <h2 class="text-white tracking-tight text-lg">物资管理</h2>
      </div>

      <nav class="min-h-0 flex-1 overflow-y-auto p-4">
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="currentRoute === 'PlayerItems' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="$router.push('/player/materials/items')"
        >
          道具
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="currentRoute === 'PlayerWeapons' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="$router.push('/player/materials/weapons')"
        >
          武器与子弹
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="currentRoute === 'PlayerBasicMaterials' ? 'bg-[#2d4263] text-white' : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'"
          @click="$router.push('/player/materials/materials')"
        >
          基础物资
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl transition-colors font-medium text-gray-400 hover:bg-[#151b2e] hover:text-gray-300"
          @click="$router.push('/player')"
        >
          返回个人中心
        </button>
      </nav>

      <div class="shrink-0 border-t border-[#1f2937] p-4">
        <div class="flex items-center justify-between">
          <span class="text-gray-400 text-sm">{{ username }}</span>
          <button
            type="button"
            class="text-gray-500 hover:text-white text-sm transition-colors"
            @click="handleLogout"
          >
            退出
          </button>
        </div>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="min-h-0 min-w-0 flex-1 overflow-y-auto p-8">
      <router-view />
    </main>
  </div>
</template>
