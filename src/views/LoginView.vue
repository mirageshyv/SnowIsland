<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const username = ref('')
const password = ref('')
const router = useRouter()

function onSubmit(e: Event) {
  e.preventDefault()
  router.push('/dashboard')
}
</script>

<template>
  <div class="min-h-screen bg-[#1a1a1a] flex items-center justify-center px-4 relative overflow-hidden">
    <!-- Animated Background -->
    <div class="absolute inset-0">
      <div class="absolute inset-0 bg-gradient-to-br from-[#1a1a1a] via-[#242424] to-[#1a1a1a]" />
      <div class="absolute inset-0 opacity-30">
        <div class="absolute top-0 left-1/4 w-96 h-96 bg-red-500/10 rounded-full blur-3xl animate-pulse" />
        <div
          class="absolute bottom-0 right-1/4 w-96 h-96 bg-white/5 rounded-full blur-3xl animate-pulse"
          style="animation-delay: 1s"
        />
        <div
          class="absolute top-1/2 left-1/2 w-64 h-64 bg-red-600/10 rounded-full blur-3xl animate-pulse"
          style="animation-delay: 2s"
        />
      </div>
    </div>

    <div class="w-full max-w-md relative z-10">
      <div class="text-center mb-10">
        <h1
          class="text-[#e74c3c] mb-3 tracking-wider text-5xl font-bold drop-shadow-[0_0_25px_rgba(255,255,255,0.4)]"
        >
          薄雪末世
        </h1>
        <p class="text-gray-400">生存模拟类</p>
      </div>

      <div
        class="relative bg-gradient-to-br from-[#2a2a2a] to-[#1f1f1f] border border-white/10 rounded-2xl p-8 shadow-2xl overflow-hidden"
      >
        <div class="absolute top-0 right-0 w-64 h-64 bg-red-500/5 rounded-full blur-3xl" />
        <div class="absolute bottom-0 left-0 w-64 h-64 bg-red-600/5 rounded-full blur-3xl" />

        <form class="relative space-y-6" @submit="onSubmit">
          <div>
            <label class="block text-gray-300 text-sm mb-2.5 font-medium">用户名</label>
            <input
              v-model="username"
              type="text"
              placeholder="输入用户名"
              class="w-full bg-black/30 backdrop-blur border border-white/10 rounded-xl px-4 py-3 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-red-500/50 focus:ring-2 focus:ring-red-500/20 transition-all"
            />
          </div>

          <div>
            <label class="block text-gray-300 text-sm mb-2.5 font-medium">密码</label>
            <input
              v-model="password"
              type="password"
              placeholder="输入密码"
              class="w-full bg-black/30 backdrop-blur border border-white/10 rounded-xl px-4 py-3 text-gray-100 placeholder-gray-500 focus:outline-none focus:border-red-500/50 focus:ring-2 focus:ring-red-500/20 transition-all"
            />
          </div>

          <button
            type="submit"
            class="w-full bg-gradient-to-r from-[#e74c3c] to-[#c0392b] hover:from-[#d62c1a] hover:to-[#a93226] text-white py-3 rounded-xl transition-all font-medium shadow-lg shadow-red-500/30 hover:shadow-red-500/40"
          >
            登录
          </button>

          <div class="text-center text-gray-500 text-xs mt-6">本作授权：人类叛徒保证委员会</div>
        </form>
      </div>
    </div>
  </div>
</template>
