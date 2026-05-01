<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '../utils/api.js'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async (e) => {
  e.preventDefault()
  loading.value = true
  errorMessage.value = ''
  
  if (!loginForm.username.trim()) {
    loading.value = false
    ElMessage.warning('请输入账号')
    return
  }
  
  if (!loginForm.password.trim()) {
    loading.value = false
    ElMessage.warning('请输入密码')
    return
  }
  
  try {
    const result = await authAPI.login(loginForm.username, loginForm.password)
    loading.value = false
    
    if (result.success) {
      localStorage.setItem('userRole', result.role)
      localStorage.setItem('username', result.username)
      localStorage.setItem('userId', result.userId)
      if (result.playerId) {
        localStorage.setItem('playerId', result.playerId)
      }
      
      router.push(`/${result.username}`).then(() => {
        ElMessage.success('登录成功')
      }).catch((err) => {
        console.error('路由跳转失败:', err)
        ElMessage.error('登录成功，但页面跳转失败，请手动刷新')
      })
    } else {
      errorMessage.value = result.message || '登录失败'
      ElMessage.error(errorMessage.value)
    }
  } catch (error) {
    loading.value = false
    console.error('登录请求失败:', error)
    ElMessage.error('登录请求失败: ' + error.message)
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] flex items-center justify-center px-4 relative overflow-hidden">
    <!-- Animated Background -->
    <div class="absolute inset-0">
      <div class="absolute inset-0 bg-gradient-to-br from-[#0a0e1a] via-[#141824] to-[#0a0e1a]" />
      <div class="absolute inset-0 opacity-30">
        <div class="absolute top-0 left-1/4 w-96 h-96 bg-blue-500/10 rounded-full blur-3xl animate-pulse" />
        <div
          class="absolute bottom-0 right-1/4 w-96 h-96 bg-white/5 rounded-full blur-3xl animate-pulse"
          style="animation-delay: 1s"
        />
        <div
          class="absolute top-1/2 left-1/2 w-64 h-64 bg-cyan-500/10 rounded-full blur-3xl animate-pulse"
          style="animation-delay: 2s"
        />
      </div>
    </div>

    <div class="w-full max-w-md relative z-10">
      <div class="text-center mb-10">
        <h1
          class="text-[#60a5fa] mb-3 tracking-wider text-5xl font-bold drop-shadow-[0_0_25px_rgba(96,165,250,0.4)]"
        >
          覆雪之下
        </h1>
        <p class="text-gray-400">线上游戏交互网站</p>
      </div>

      <div
        class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-8 shadow-2xl overflow-hidden"
      >
        <div class="absolute top-0 right-0 w-64 h-64 bg-blue-500/5 rounded-full blur-3xl" />
        <div class="absolute bottom-0 left-0 w-64 h-64 bg-cyan-500/5 rounded-full blur-3xl" />

        <form class="relative space-y-6" @submit="handleLogin">
          <div>
            <label class="block text-gray-300 text-sm mb-2.5 font-medium">账号</label>
            <input
              v-model="loginForm.username"
              type="text"
              placeholder="请输入登录账号"
              class="w-full bg-black/30 backdrop-blur border border-white/10 rounded-xl px-4 py-3 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-blue-500/50 focus:ring-2 focus:ring-blue-500/20 transition-all"
            />
          </div>

          <div>
            <label class="block text-gray-300 text-sm mb-2.5 font-medium">密码</label>
            <input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              class="w-full bg-black/30 backdrop-blur border border-white/10 rounded-xl px-4 py-3 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-blue-500/50 focus:ring-2 focus:ring-blue-500/20 transition-all"
            />
          </div>

          <div v-if="errorMessage" class="text-red-400 text-sm bg-red-500/10 rounded-lg p-3">
            {{ errorMessage }}
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-gradient-to-r from-[#3b82f6] to-[#2563eb] hover:from-[#2563eb] hover:to-[#1d4ed8] text-white py-3 rounded-xl transition-all font-medium shadow-lg shadow-blue-500/30 hover:shadow-blue-500/40 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ loading ? '登录中...' : '登录' }}
          </button>

          <div class="text-center text-gray-500 text-xs mt-6">
            <p>测试账号：player1-player5 / dm1-dm2</p>
            <p>密码：test123</p>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>
