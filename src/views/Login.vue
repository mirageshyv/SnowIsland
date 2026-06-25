<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI, gameStateAPI } from '../utils/api.js'
import SnowEffect from '../components/SnowEffect.vue'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const usernameInput = ref(null)
const passwordInput = ref(null)
const gameDay = ref(1)

const snowIntensity = computed(() => {
  const day = gameDay.value || 1
  if (day <= 1) return 'light'
  if (day <= 2) return 'medium'
  return 'heavy'
})

onMounted(async () => {
  try {
    const gs = await gameStateAPI.get()
    if (gs?.currentDay) gameDay.value = gs.currentDay
  } catch {
    // 默认第1天
  }
})

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

    if (!result) {
      errorMessage.value = '无法连接到服务器，请检查后端服务'
      ElMessage.error('无法连接到服务器，请检查后端服务是否启动')
      return
    }

    if (result.success) {
      localStorage.setItem('userRole', result.role)
      localStorage.setItem('username', result.username)
      localStorage.setItem('userId', result.userId)
      if (result.playerId) {
        localStorage.setItem('playerId', result.playerId)
      }

      ElMessage.success('登录成功')

      // 根据角色直接跳转到对应路由，避免使用动态用户名路由
      const targetRoute = (result.role || '').toLowerCase() === 'dm' ? '/dm' : '/player'
      try {
        await router.push(targetRoute)
      } catch (err) {
        console.error('路由跳转失败:', err)
        // 即使动态导入失败，也使用 location 强制跳转
        window.location.href = targetRoute
      }
    } else {
      errorMessage.value = result.message || '登录失败'
      ElMessage.error(errorMessage.value)
    }
  } catch (error) {
    loading.value = false
    console.error('登录请求失败:', error)
    errorMessage.value = '登录请求失败，请检查后端服务'
    ElMessage.error('登录请求失败: ' + error.message)
  }
}
</script>

<template>
  <div class="min-h-screen w-full flex items-center justify-center px-4 relative overflow-hidden bg-[#0a0e1a]">
    <!-- Background Image -->
    <div 
      class="absolute inset-0 bg-cover bg-center bg-no-repeat"
      style="background-image: url('/src/assets/登录页面.png');"
    ></div>
    <div class="absolute inset-0 bg-black/30"></div>

    <!-- Snow Effect -->
    <SnowEffect :intensity="snowIntensity" />

    <!-- Login Container -->
    <div class="w-full max-w-md relative z-10 px-2 sm:px-0">
      <div class="text-center mb-8 sm:mb-10">
        <h1
          class="text-white mb-2 sm:mb-3 tracking-wider text-3xl sm:text-5xl font-bold drop-shadow-[0_0_25px_rgba(255,255,255,0.4)]"
        >
          覆雪之下
        </h1>
        <p class="text-gray-200/80 text-sm sm:text-base">线上游戏交互网站</p>
      </div>

      <div
        class="relative rounded-2xl p-5 sm:p-8 shadow-2xl backdrop-blur-lg"
        style="background: rgba(15, 20, 30, 0.95); border: 1px solid rgba(255, 255, 255, 0.1);"
      >
        <form class="relative space-y-5 sm:space-y-6" @submit="handleLogin">
          <div>
            <label class="block text-gray-200 text-sm mb-2 font-medium">账号</label>
            <input
              ref="usernameInput"
              v-model="loginForm.username"
              type="text"
              placeholder="请输入登录账号"
              @keyup.enter="passwordInput.focus()"
              class="w-full bg-black/40 backdrop-blur border border-white/15 rounded-xl px-4 py-3.5 text-gray-100 placeholder-gray-400 focus:outline-none focus:border-blue-400/60 focus:ring-2 focus:ring-blue-400/20 transition-all text-base"
            />
          </div>

          <div>
            <label class="block text-gray-200 text-sm mb-2 font-medium">密码</label>
            <input
              ref="passwordInput"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              class="w-full bg-black/40 backdrop-blur border border-white/15 rounded-xl px-4 py-3.5 text-gray-100 placeholder-gray-400 focus:outline-none focus:border-blue-400/60 focus:ring-2 focus:ring-blue-400/20 transition-all text-base"
            />
          </div>

          <div v-if="errorMessage" class="text-red-400 text-sm bg-red-500/15 rounded-lg p-3">
            {{ errorMessage }}
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-500 hover:to-blue-600 text-white py-3.5 rounded-xl transition-all font-medium shadow-lg shadow-blue-500/30 hover:shadow-blue-500/40 disabled:opacity-50 disabled:cursor-not-allowed min-h-[48px] text-base active:scale-[0.98]"
          >
            {{ loading ? '登录中...' : '登录' }}
          </button>

        </form>
      </div>
    </div>
  </div>
</template>
