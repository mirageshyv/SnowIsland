<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Player from './Player.vue'
import DM from './DM.vue'

const route = useRoute()
const router = useRouter()

const userRole = ref('')
const username = ref('')
const loading = ref(true)
const error = ref('')

const isPlayer = computed(() => userRole.value.toLowerCase() === 'player')
const isDM = computed(() => userRole.value.toLowerCase() === 'dm')

onMounted(() => {
  userRole.value = localStorage.getItem('userRole') || ''
  username.value = localStorage.getItem('username') || ''
  
  if (!userRole.value || !username.value) {
    error.value = '登录状态失效，请重新登录'
    setTimeout(() => {
      router.push('/')
    }, 2000)
    loading.value = false
    return
  }
  
  if (route.params.username !== username.value) {
    error.value = '访问权限不足'
    setTimeout(() => {
      router.push('/')
    }, 2000)
    loading.value = false
    return
  }
  
  loading.value = false
})
</script>

<template>
  <div v-if="loading" class="min-h-screen bg-[#0a0e1a] flex items-center justify-center">
    <div class="text-center">
      <div class="w-16 h-16 border-4 border-blue-500 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
      <p class="text-gray-400">加载中...</p>
    </div>
  </div>
  
  <div v-else-if="error" class="min-h-screen bg-[#0a0e1a] flex items-center justify-center">
    <div class="text-center">
      <div class="w-20 h-20 bg-red-500/20 rounded-full flex items-center justify-center mx-auto mb-4">
        <svg class="w-10 h-10 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
        </svg>
      </div>
      <p class="text-red-400 text-lg mb-2">{{ error }}</p>
      <p class="text-gray-500">即将跳转到登录页面...</p>
    </div>
  </div>
  
  <Player v-else-if="isPlayer" />
  <DM v-else-if="isDM" />
  
  <div v-else class="min-h-screen bg-[#0a0e1a] flex items-center justify-center">
    <div class="text-center">
      <p class="text-gray-400">未知用户角色</p>
      <button 
        @click="router.push('/')" 
        class="mt-4 px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
      >
        返回登录
      </button>
    </div>
  </div>
</template>
