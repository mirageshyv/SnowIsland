<script setup lang="ts">
import { ref } from 'vue'
import rifleUrl from '@/assets/rifle.svg?url'
import medkitUrl from '@/assets/medkit.svg?url'
import armorUrl from '@/assets/armor.svg?url'

type TabKey = 'home' | 'actions' | 'map' | 'resources'

const activeTab = ref<TabKey>('home')
const showTradeModal = ref(false)
const showPendingTradesModal = ref(false)
</script>

<template>
  <div class="min-h-screen bg-[#0a0e1a] flex">
    <!-- Sidebar -->
    <aside class="w-64 bg-[#0f1419] border-r border-[#1f2937] flex flex-col">
      <div class="p-6 border-b border-[#1f2937]">
        <h2 class="text-white tracking-tight">战区指挥系统</h2>
      </div>

      <nav class="flex-1 p-4">
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="
            activeTab === 'home'
              ? 'bg-[#2d4263] text-white'
              : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'
          "
          @click="activeTab = 'home'"
        >
          首页
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="
            activeTab === 'actions'
              ? 'bg-[#2d4263] text-white'
              : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'
          "
          @click="activeTab = 'actions'"
        >
          行动提交页
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl mb-2 transition-colors font-medium"
          :class="
            activeTab === 'map'
              ? 'bg-[#2d4263] text-white'
              : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'
          "
          @click="activeTab = 'map'"
        >
          地图 / 地点页面
        </button>
        <button
          type="button"
          class="w-full text-left px-4 py-3 rounded-xl transition-colors font-medium"
          :class="
            activeTab === 'resources'
              ? 'bg-[#2d4263] text-white'
              : 'text-gray-400 hover:bg-[#151b2e] hover:text-gray-300'
          "
          @click="activeTab = 'resources'"
        >
          资源 / 背包页面
        </button>
      </nav>
    </aside>

    <!-- Main Content -->
    <main class="flex-1 p-8 overflow-auto">
      <div v-if="activeTab === 'home'" class="max-w-7xl">
        <div class="mb-6">
          <h1 class="text-white mb-1 tracking-tight">首页</h1>
          <p class="text-gray-500 text-sm">Overview Dashboard</p>
        </div>

        <!-- Character Card -->
        <div
          class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 mb-5 overflow-hidden"
        >
          <div class="absolute top-0 right-0 w-96 h-96 bg-blue-500/5 rounded-full blur-3xl" />
          <div class="relative">
            <div class="flex items-start justify-between mb-4">
              <div class="flex items-center gap-5">
                <div class="relative">
                  <div class="absolute inset-0 bg-blue-500/20 rounded-2xl blur-xl" />
                  <div
                    class="relative w-16 h-16 rounded-2xl bg-gradient-to-br from-blue-400 via-blue-500 to-blue-600 flex items-center justify-center shadow-2xl"
                  >
                    <span class="text-2xl">🎖️</span>
                  </div>
                </div>
                <div>
                  <h2 class="text-white mb-1 tracking-tight">张明远</h2>
                  <p class="text-gray-400 text-sm mb-2">指挥官</p>
                  <div class="flex items-center gap-2">
                    <span
                      class="text-xs text-gray-300 bg-white/10 px-3 py-1 rounded-full backdrop-blur"
                    >
                      战术分析 · 领导力
                    </span>
                    <span
                      class="text-xs text-blue-300 bg-blue-500/20 px-3 py-1 rounded-full flex items-center gap-1.5 backdrop-blur"
                    >
                      <span
                        class="w-1.5 h-1.5 rounded-full bg-blue-400 shadow-lg shadow-blue-400/50"
                      />
                      蓝色联盟
                    </span>
                  </div>
                </div>
              </div>

              <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl px-4 py-3">
                <p class="text-gray-400 text-xs mb-1">负面状态：</p>
                <p class="text-gray-300 text-sm">无</p>
              </div>
            </div>

            <div class="flex gap-6">
              <div class="text-center">
                <div
                  class="w-12 h-12 rounded-xl bg-red-500/10 flex items-center justify-center mb-1 backdrop-blur border border-white/10"
                >
                  <p class="text-xl text-white">2</p>
                </div>
                <p class="text-xs text-gray-400">冲突</p>
              </div>
              <div class="text-center">
                <div
                  class="w-12 h-12 rounded-xl bg-blue-500/10 flex items-center justify-center mb-1 backdrop-blur border border-white/10"
                >
                  <p class="text-xl text-white">1</p>
                </div>
                <p class="text-xs text-gray-400">调查</p>
              </div>
              <div class="text-center">
                <div
                  class="w-12 h-12 rounded-xl bg-emerald-500/10 flex items-center justify-center mb-1 backdrop-blur border border-white/10"
                >
                  <p class="text-xl text-white">3</p>
                </div>
                <p class="text-xs text-gray-400">生产</p>
              </div>
            </div>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-5">
          <!-- Equipment Slots -->
          <div
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 overflow-hidden"
          >
            <div class="absolute top-0 left-0 w-64 h-64 bg-amber-500/5 rounded-full blur-3xl" />
            <div class="relative">
              <h3 class="text-white mb-5 tracking-tight">装备栏</h3>

              <div class="space-y-3">
                <div>
                  <p class="text-gray-500 text-xs mb-2 ml-1">武器</p>
                  <div
                    class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-3 flex items-center gap-3 hover:bg-white/10 transition-all"
                  >
                    <div
                      class="w-10 h-10 rounded-lg bg-gradient-to-br from-amber-400/20 to-amber-500/20 flex items-center justify-center"
                    >
                      <span class="text-lg">🔫</span>
                    </div>
                    <div class="flex-1">
                      <p class="text-gray-200 text-sm mb-0.5">92式手枪</p>
                      <p class="text-xs text-gray-500">耐久 87%</p>
                    </div>
                  </div>
                </div>

                <div>
                  <p class="text-gray-500 text-xs mb-2 ml-1">弹药</p>
                  <div
                    class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-3 flex items-center gap-3 hover:bg-white/10 transition-all"
                  >
                    <div
                      class="w-10 h-10 rounded-lg bg-gradient-to-br from-orange-400/20 to-orange-500/20 flex items-center justify-center"
                    >
                      <span class="text-lg">📦</span>
                    </div>
                    <div class="flex-1">
                      <p class="text-gray-200 text-sm mb-0.5">9mm弹药</p>
                      <p class="text-xs text-gray-500">45发</p>
                    </div>
                  </div>
                </div>

                <div>
                  <p class="text-gray-500 text-xs mb-2 ml-1">防具</p>
                  <div
                    class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-3 flex items-center gap-3 hover:bg-white/10 transition-all"
                  >
                    <div
                      class="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-400/20 to-blue-500/20 flex items-center justify-center"
                    >
                      <span class="text-lg">🛡️</span>
                    </div>
                    <div class="flex-1">
                      <p class="text-gray-200 text-sm mb-0.5">防弹衣</p>
                      <p class="text-xs text-gray-500">III级 · 耐久 92%</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Backpack Summary -->
          <div
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 overflow-hidden"
          >
            <div class="absolute top-0 right-0 w-64 h-64 bg-indigo-500/5 rounded-full blur-3xl" />
            <div class="relative">
              <h3 class="text-white mb-5 tracking-tight">背包总结</h3>

              <div class="space-y-4">
                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-3">
                    <div class="flex items-center gap-3">
                      <div
                        class="w-10 h-10 rounded-lg bg-gradient-to-br from-emerald-400/20 to-emerald-500/20 flex items-center justify-center"
                      >
                        <span class="text-lg">🍞</span>
                      </div>
                      <div>
                        <p class="text-gray-400 text-xs mb-0.5">食物储备</p>
                        <p class="text-white">2,850 大卡</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-emerald-400">3.5天</p>
                      <p class="text-xs text-gray-500">可维持</p>
                    </div>
                  </div>
                  <div class="relative w-full bg-white/5 rounded-full h-2 overflow-hidden">
                    <div
                      class="absolute inset-0 bg-gradient-to-r from-emerald-400/20 to-emerald-500/20 rounded-full blur"
                    />
                    <div
                      class="relative bg-gradient-to-r from-emerald-400 to-emerald-500 h-2 rounded-full shadow-lg shadow-emerald-500/50"
                      style="width: 65%"
                    />
                  </div>
                </div>

                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4">
                  <div class="flex items-center justify-between mb-3">
                    <div class="flex items-center gap-3">
                      <div
                        class="w-10 h-10 rounded-lg bg-gradient-to-br from-amber-400/20 to-amber-500/20 flex items-center justify-center"
                      >
                        <span class="text-lg">⛽</span>
                      </div>
                      <div>
                        <p class="text-gray-400 text-xs mb-0.5">燃料储备</p>
                        <p class="text-white">12.5 kg</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-amber-400">5.0天</p>
                      <p class="text-xs text-gray-500">可维持</p>
                    </div>
                  </div>
                  <div class="relative w-full bg-white/5 rounded-full h-2 overflow-hidden">
                    <div
                      class="absolute inset-0 bg-gradient-to-r from-amber-400/20 to-amber-500/20 rounded-full blur"
                    />
                    <div
                      class="relative bg-gradient-to-r from-amber-400 to-amber-500 h-2 rounded-full shadow-lg shadow-amber-500/50"
                      style="width: 82%"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="activeTab === 'actions'">
        <h1 class="text-white mb-6 tracking-tight">行动提交页</h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">行动提交功能开发中...</p>
        </div>
      </div>

      <div v-else-if="activeTab === 'map'">
        <h1 class="text-white mb-6 tracking-tight">地图 / 地点页面</h1>
        <div class="bg-[#0f1419] border border-[#1f2937] rounded-xl p-6">
          <p class="text-gray-500 font-normal">地图功能开发中...</p>
        </div>
      </div>

      <div v-else-if="activeTab === 'resources'" class="max-w-7xl">
        <div class="mb-4 flex items-center justify-between">
          <div>
            <h1 class="text-white mb-0.5 tracking-tight">资源 / 背包</h1>
            <p class="text-gray-500 text-xs">Inventory & Resources</p>
          </div>
          <div class="flex gap-3">
            <button
              type="button"
              class="bg-gradient-to-r from-purple-500 to-purple-600 hover:from-purple-600 hover:to-purple-700 text-white px-4 py-2 rounded-xl transition-all text-sm shadow-lg shadow-purple-500/30 flex items-center gap-2"
              @click="showPendingTradesModal = true"
            >
              待处理交易
              <span class="bg-white/20 px-1.5 py-0.5 rounded-full text-xs">2</span>
            </button>
            <button
              type="button"
              class="bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white px-4 py-2 rounded-xl transition-all text-sm shadow-lg shadow-blue-500/30"
              @click="showTradeModal = true"
            >
              发起交易
            </button>
          </div>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <!-- Food Section -->
          <div
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-4 overflow-hidden"
          >
            <div class="absolute top-0 right-0 w-48 h-48 bg-emerald-500/5 rounded-full blur-3xl" />
            <div class="relative">
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-white text-sm tracking-tight">食物储备</h3>
                <div class="text-right">
                  <p class="text-emerald-400 text-sm">2,850 大卡</p>
                  <p class="text-xs text-gray-500">3.5天</p>
                </div>
              </div>

              <div class="space-y-2">
                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <span class="text-lg">🍪</span>
                      <div>
                        <p class="text-gray-200 text-sm">硬饼干</p>
                        <p class="text-xs text-gray-500">10kg/箱</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-white text-sm">1箱</p>
                      <p class="text-xs text-gray-500">10kg</p>
                    </div>
                  </div>
                </div>

                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <span class="text-lg">🥔</span>
                      <div>
                        <p class="text-gray-200 text-sm">土豆</p>
                        <p class="text-xs text-gray-500">100kg/筐</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-white text-sm">18筐</p>
                      <p class="text-xs text-gray-500">1,800kg</p>
                    </div>
                  </div>
                </div>

                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <span class="text-lg">🥫</span>
                      <div>
                        <p class="text-gray-200 text-sm">罐头</p>
                        <p class="text-xs text-gray-500">500g/罐</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-white text-sm">24罐</p>
                      <p class="text-xs text-gray-500">12kg</p>
                    </div>
                  </div>
                </div>

                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <span class="text-lg">🍚</span>
                      <div>
                        <p class="text-gray-200 text-sm">大米</p>
                        <p class="text-xs text-gray-500">25kg/袋</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-white text-sm">3袋</p>
                      <p class="text-xs text-gray-500">75kg</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Ammo & Weapons Section -->
          <div
            class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-4 overflow-hidden"
          >
            <div class="absolute top-0 right-0 w-48 h-48 bg-amber-500/5 rounded-full blur-3xl" />
            <div class="relative">
              <h3 class="text-white text-sm tracking-tight mb-3">弹药与武器</h3>

              <div class="space-y-2">
                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <span class="text-lg">📦</span>
                      <div>
                        <p class="text-gray-200 text-sm">9mm弹药</p>
                        <p class="text-xs text-gray-500">50发/盒</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-white text-sm">3盒</p>
                      <p class="text-xs text-gray-500">150发</p>
                    </div>
                  </div>
                </div>

                <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <img
                        :src="rifleUrl"
                        alt="李-恩菲尔德步枪"
                        class="w-8 h-8 rounded-lg object-cover"
                      />
                      <div>
                        <p class="text-gray-200 text-sm">李-恩菲尔德</p>
                        <p class="text-xs text-gray-500">备用步枪</p>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="text-white text-sm">1把</p>
                      <p class="text-xs text-gray-500">87%</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Other Resources Section -->
        <div
          class="relative bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-2xl p-4 mt-3 overflow-hidden"
        >
          <div class="absolute top-0 right-0 w-48 h-48 bg-blue-500/5 rounded-full blur-3xl" />
          <div class="relative">
            <h3 class="text-white text-sm tracking-tight mb-3">其他资源</h3>

            <div class="grid grid-cols-2 gap-2">
              <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span class="text-lg">⛽</span>
                    <div>
                      <p class="text-gray-200 text-sm">燃料</p>
                      <p class="text-xs text-gray-500">5L/桶</p>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-white text-sm">2.5桶</p>
                    <p class="text-xs text-gray-500">12.5L</p>
                  </div>
                </div>
              </div>

              <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span class="text-lg">🔋</span>
                    <div>
                      <p class="text-gray-200 text-sm">电池</p>
                      <p class="text-xs text-gray-500">AA型</p>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-white text-sm">16节</p>
                    <p class="text-xs text-emerald-400">良好</p>
                  </div>
                </div>
              </div>

              <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <img :src="medkitUrl" alt="医疗包" class="w-8 h-8 rounded-lg object-cover" />
                    <div>
                      <p class="text-gray-200 text-sm">医疗包</p>
                      <p class="text-xs text-gray-500">急救用品</p>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-white text-sm">2套</p>
                    <p class="text-xs text-gray-500">95%</p>
                  </div>
                </div>
              </div>

              <div class="bg-white/5 backdrop-blur border border-white/10 rounded-lg p-2.5">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <img :src="armorUrl" alt="防弹衣" class="w-8 h-8 rounded-lg object-cover" />
                    <div>
                      <p class="text-gray-200 text-sm">防弹衣</p>
                      <p class="text-xs text-gray-500">III级防护</p>
                    </div>
                  </div>
                  <div class="text-right">
                    <p class="text-white text-sm">1件</p>
                    <p class="text-xs text-gray-500">92%</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Trade Modal -->
    <Teleport to="body">
      <div
        v-if="showTradeModal"
        class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50"
        @click.self="showTradeModal = false"
      >
        <div
          class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 max-w-2xl w-full mx-4 shadow-2xl max-h-[90vh] overflow-y-auto"
        >
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-white text-xl tracking-tight">发起交易</h2>
            <button type="button" class="text-gray-400 hover:text-white transition-colors" @click="showTradeModal = false">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <div class="space-y-4">
            <div>
              <label class="block text-gray-300 text-sm mb-2">选择交易对象</label>
              <select
                class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 focus:outline-none focus:border-blue-500/50"
              >
                <option>李华</option>
                <option>王芳</option>
                <option>张伟</option>
                <option>刘强</option>
              </select>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-gray-300 text-sm mb-2">你提供的物品</label>
                <div class="bg-white/5 border border-white/10 rounded-xl p-4 min-h-[200px]">
                  <select
                    class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-gray-200 text-sm mb-3"
                  >
                    <option>选择物品...</option>
                    <option>土豆 (100kg/筐)</option>
                    <option>硬饼干 (10kg/箱)</option>
                    <option>9mm弹药 (50发/盒)</option>
                    <option>燃料 (5L/桶)</option>
                  </select>
                  <input
                    type="number"
                    placeholder="数量"
                    class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-gray-200 text-sm mb-3"
                  />
                  <button
                    type="button"
                    class="w-full bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 py-2 rounded-lg text-sm transition-all"
                  >
                    添加物品
                  </button>
                  <div class="mt-3 space-y-2">
                    <div class="bg-white/5 rounded-lg p-2 flex items-center justify-between">
                      <span class="text-gray-300 text-sm">土豆 2筐</span>
                      <button type="button" class="text-red-400 hover:text-red-300 text-xs">移除</button>
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <label class="block text-gray-300 text-sm mb-2">你需要的物品</label>
                <div class="bg-white/5 border border-white/10 rounded-xl p-4 min-h-[200px]">
                  <select
                    class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-gray-200 text-sm mb-3"
                  >
                    <option>选择物品...</option>
                    <option>土豆 (100kg/筐)</option>
                    <option>硬饼干 (10kg/箱)</option>
                    <option>9mm弹药 (50发/盒)</option>
                    <option>燃料 (5L/桶)</option>
                  </select>
                  <input
                    type="number"
                    placeholder="数量"
                    class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-gray-200 text-sm mb-3"
                  />
                  <button
                    type="button"
                    class="w-full bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 py-2 rounded-lg text-sm transition-all"
                  >
                    添加物品
                  </button>
                  <div class="mt-3 space-y-2">
                    <div class="bg-white/5 rounded-lg p-2 flex items-center justify-between">
                      <span class="text-gray-300 text-sm">9mm弹药 1盒</span>
                      <button type="button" class="text-red-400 hover:text-red-300 text-xs">移除</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div>
              <label class="block text-gray-300 text-sm mb-2">备注信息（可选）</label>
              <textarea
                class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-gray-200 focus:outline-none focus:border-blue-500/50 resize-none"
                rows="3"
                placeholder="添加交易备注..."
              />
            </div>

            <div class="flex gap-3 pt-4">
              <button
                type="button"
                class="flex-1 bg-white/5 hover:bg-white/10 text-gray-300 py-3 rounded-xl transition-all"
                @click="showTradeModal = false"
              >
                取消
              </button>
              <button
                type="button"
                class="flex-1 bg-gradient-to-r from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white py-3 rounded-xl transition-all shadow-lg shadow-blue-500/30"
              >
                发送交易请求
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Pending Trades Modal -->
    <Teleport to="body">
      <div
        v-if="showPendingTradesModal"
        class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50"
        @click.self="showPendingTradesModal = false"
      >
        <div
          class="bg-gradient-to-br from-[#1a2332] to-[#0f1419] border border-white/10 rounded-3xl p-6 max-w-3xl w-full mx-4 shadow-2xl max-h-[80vh] overflow-y-auto"
        >
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center gap-3">
              <h2 class="text-white text-xl tracking-tight">待处理交易</h2>
              <span class="text-xs text-blue-400 bg-blue-500/10 px-3 py-1.5 rounded-full">2 个待审批</span>
            </div>
            <button
              type="button"
              class="text-gray-400 hover:text-white transition-colors"
              @click="showPendingTradesModal = false"
            >
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>

          <div class="space-y-4">
            <div
              class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4 hover:bg-white/10 transition-all"
            >
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center">
                    <span class="text-lg">👤</span>
                  </div>
                  <div>
                    <p class="text-gray-200">来自 李华</p>
                    <p class="text-xs text-gray-500">2小时前</p>
                  </div>
                </div>
                <div class="flex gap-2">
                  <button
                    type="button"
                    class="bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-4 py-2 rounded-lg text-sm transition-all"
                  >
                    接受
                  </button>
                  <button
                    type="button"
                    class="bg-red-500/20 hover:bg-red-500/30 text-red-400 px-4 py-2 rounded-lg text-sm transition-all"
                  >
                    拒绝
                  </button>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-white/5 rounded-lg p-3">
                  <p class="text-gray-400 text-xs mb-2">对方提供</p>
                  <p class="text-gray-200">土豆 2筐 (200kg)</p>
                </div>
                <div class="bg-white/5 rounded-lg p-3">
                  <p class="text-gray-400 text-xs mb-2">你需支付</p>
                  <p class="text-gray-200">9mm弹药 1盒 (50发)</p>
                </div>
              </div>
            </div>

            <div
              class="bg-white/5 backdrop-blur border border-white/10 rounded-xl p-4 hover:bg-white/10 transition-all"
            >
              <div class="flex items-center justify-between mb-3">
                <div class="flex items-center gap-3">
                  <div class="w-10 h-10 rounded-lg bg-blue-500/20 flex items-center justify-center">
                    <span class="text-lg">👤</span>
                  </div>
                  <div>
                    <p class="text-gray-200">来自 王芳</p>
                    <p class="text-xs text-gray-500">5小时前</p>
                  </div>
                </div>
                <div class="flex gap-2">
                  <button
                    type="button"
                    class="bg-emerald-500/20 hover:bg-emerald-500/30 text-emerald-400 px-4 py-2 rounded-lg text-sm transition-all"
                  >
                    接受
                  </button>
                  <button
                    type="button"
                    class="bg-red-500/20 hover:bg-red-500/30 text-red-400 px-4 py-2 rounded-lg text-sm transition-all"
                  >
                    拒绝
                  </button>
                </div>
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div class="bg-white/5 rounded-lg p-3">
                  <p class="text-gray-400 text-xs mb-2">对方提供</p>
                  <p class="text-gray-200">燃料 1桶 (5L)</p>
                </div>
                <div class="bg-white/5 rounded-lg p-3">
                  <p class="text-gray-400 text-xs mb-2">你需支付</p>
                  <p class="text-gray-200">罐头 8罐 (4kg)</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
