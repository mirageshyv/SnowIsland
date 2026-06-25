<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white">
    <div class="max-w-6xl mx-auto px-4 py-6">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-2xl font-bold">NPC 交互</h1>
        <button @click="refreshNpcs" class="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition">
          🔄 刷新
        </button>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-1">
          <div class="bg-slate-800/50 rounded-xl p-4 backdrop-blur-sm border border-slate-700">
            <h2 class="text-lg font-semibold mb-4">📍 NPC 列表</h2>
            
            <div class="space-y-3">
              <div
                v-for="npc in npcs"
                :key="npc.id"
                @click="selectNpc(npc)"
                :class="[
                  'p-4 rounded-lg cursor-pointer transition-all border-2',
                  selectedNpc?.id === npc.id
                    ? 'bg-blue-600/30 border-blue-500'
                    : 'bg-slate-700/50 border-transparent hover:border-slate-600'
                ]"
              >
                <div class="flex items-center justify-between mb-2">
                  <span class="font-semibold">{{ npc.name }}</span>
                  <span class="text-xs px-2 py-1 rounded-full bg-slate-600">
                    {{ npc.job }}
                  </span>
                </div>
                
                <div v-if="npc.favorValue != null" class="flex items-center gap-2">
                  <span class="text-xs text-slate-400">好感度:</span>
                  <div class="flex-1 h-2 bg-slate-600 rounded-full overflow-hidden">
                    <div
                      class="h-full transition-all duration-300"
                      :style="{ width: `${(npc.favorValue + 100) / 2}%`, backgroundColor: npc.favorColor }"
                    ></div>
                  </div>
                  <span class="text-xs font-medium" :style="{ color: npc.favorColor }">
                    {{ npc.favorLevel }}
                  </span>
                </div>
                
                <p class="text-xs text-slate-400 mt-2 line-clamp-2">{{ npc.introduction }}</p>
              </div>
              
              <div v-if="npcs.length === 0" class="text-center py-8">
                <div class="text-4xl mb-4">🌍</div>
                <p class="text-slate-400 mb-2">你还没有认识任何NPC</p>
                <p class="text-slate-500 text-sm">通过"前往地点"行动认识他们吧！</p>
              </div>
            </div>
            
            <div class="mt-4 p-3 bg-slate-700/30 rounded-lg">
              <p class="text-xs text-slate-400">
                已认识 {{ cognitionStats.recognizedCount || 0 }} / {{ cognitionStats.totalNpcs || 0 }} 个NPC
              </p>
            </div>
          </div>
        </div>

        <div class="lg:col-span-2">
          <div v-if="selectedNpc" class="bg-slate-800/50 rounded-xl backdrop-blur-sm border border-slate-700">
            <div class="p-4 border-b border-slate-700">
              <div class="flex items-center gap-4">
                <div class="w-16 h-16 rounded-full bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-2xl">
                  {{ selectedNpc.name.charAt(0) }}
                </div>
                <div>
                  <h2 class="text-xl font-bold">{{ selectedNpc.name }}</h2>
                  <p class="text-slate-400">{{ selectedNpc.job }}</p>
                </div>
                <div class="ml-auto flex items-center gap-2">
                  <span class="text-sm px-3 py-1 rounded-full" :style="{ backgroundColor: selectedNpc.favorColor + '20', color: selectedNpc.favorColor }">
                    {{ selectedNpc.favorLevel }}
                  </span>
                </div>
              </div>
              
              <div class="mt-4 grid grid-cols-2 gap-4 text-sm">
                <div>
                  <span class="text-slate-400">性格:</span>
                  <span class="ml-2">{{ selectedNpc.personality || '未知' }}</span>
                </div>
                <div>
                  <span class="text-slate-400">状态:</span>
                  <span class="ml-2">{{ selectedNpc.status || '正常' }}</span>
                </div>
              </div>
              
              <p class="mt-4 text-slate-300">{{ selectedNpc.introduction }}</p>
            </div>

            <div class="flex border-b border-slate-700">
              <button
                @click="activeTab = 'dialogue'"
                :class="[
                  'flex-1 py-3 text-sm font-medium transition',
                  activeTab === 'dialogue' ? 'bg-blue-600/30 text-blue-400 border-b-2 border-blue-500' : 'text-slate-400 hover:text-white'
                ]"
              >
                💬 对话
              </button>
              <button
                @click="activeTab = 'trade'"
                :class="[
                  'flex-1 py-3 text-sm font-medium transition',
                  activeTab === 'trade' ? 'bg-blue-600/30 text-blue-400 border-b-2 border-blue-500' : 'text-slate-400 hover:text-white'
                ]"
              >
                🤝 交易
              </button>
              <button
                @click="activeTab = 'help'"
                :class="[
                  'flex-1 py-3 text-sm font-medium transition',
                  activeTab === 'help' ? 'bg-blue-600/30 text-blue-400 border-b-2 border-blue-500' : 'text-slate-400 hover:text-white'
                ]"
              >
                🆘 请求帮助
              </button>
            </div>

            <div v-if="activeTab === 'dialogue'" class="h-80 overflow-y-auto p-4 bg-slate-900/50">
              <div class="space-y-4">
                <div v-for="dialogue in dialogues" :key="dialogue.id">
                  <div class="flex gap-3">
                    <div class="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center flex-shrink-0 text-xs">
                      我
                    </div>
                    <div class="bg-blue-600/20 rounded-lg rounded-tl-none p-3 max-w-[70%]">
                      <p>{{ dialogue.playerMessage }}</p>
                    </div>
                  </div>
                  
                  <div class="flex gap-3 mt-2">
                    <div class="w-8 h-8 rounded-full bg-purple-600 flex items-center justify-center flex-shrink-0 text-xs">
                      {{ selectedNpc.name.charAt(0) }}
                    </div>
                    <div class="bg-slate-700/50 rounded-lg rounded-tr-none p-3 max-w-[70%]">
                      <p>{{ dialogue.npcReply }}</p>
                      <div v-if="dialogue.favorChange !== 0" class="mt-1 text-xs" :class="dialogue.favorChange > 0 ? 'text-green-400' : 'text-red-400'">
                        {{ dialogue.favorChange > 0 ? '+' : '' }}{{ dialogue.favorChange }} 好感度
                      </div>
                    </div>
                  </div>
                </div>
                
                <div v-if="dialogues.length === 0" class="text-center text-slate-500 py-8">
                  点击下方输入框开始对话
                </div>
              </div>
            </div>

            <div v-else class="p-4 max-h-[400px] overflow-y-auto">
              <div v-if="tradeLoading" class="text-center py-8">
                <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto mb-2"></div>
                <p class="text-slate-400">加载中...</p>
              </div>
              
              <div v-else-if="tradeConfig.hostile || !tradeConfig.success">
                <div class="bg-red-900/30 border border-red-800/50 rounded-xl p-6 text-center">
                  <div class="text-4xl mb-4">⛔</div>
                  <h3 class="text-red-400 font-bold text-lg mb-2">交易不可用</h3>
                  <p class="text-red-300/80 text-sm">{{ tradeConfig.message || '该NPC对你抱有敌意，拒绝与你交易' }}</p>
                  <div v-if="tradeConfig.favorTier" class="mt-4 p-3 rounded-lg bg-red-800/30">
                    <div class="flex items-center justify-center gap-2">
                      <span class="text-xl">💔</span>
                      <div>
                        <div class="text-sm font-semibold text-red-400">
                          好感度: {{ tradeConfig.favorValue }} / 100
                        </div>
                        <div class="text-xs text-red-300">
                          关系等级: {{ tradeConfig.favorTier.tier }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div v-else>
                <!-- 好感度档位展示 -->
                <div v-if="tradeConfig.favorTier" class="mb-4 p-3 rounded-lg border" :style="{ borderColor: tradeConfig.favorTier.color + '80', backgroundColor: tradeConfig.favorTier.color + '15' }">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <span class="text-xl">{{ tradeConfig.favorTier.canFreeReward ? '🌟' : tradeConfig.favorTier.tier === '敌视' ? '💔' : '💖' }}</span>
                      <div>
                        <div class="text-sm font-semibold" :style="{ color: tradeConfig.favorTier.color }">
                          好感度: {{ tradeConfig.favorValue }} / 100
                        </div>
                        <div class="text-xs" :style="{ color: tradeConfig.favorTier.color }">
                          关系等级: {{ tradeConfig.favorTier.tier }}
                        </div>
                      </div>
                    </div>
                    <div class="text-right">
                      <div v-if="tradeConfig.favorTier.canFreeReward" class="text-xs">
                        <span class="text-yellow-400 font-bold">🎁 免费赠送</span>
                      </div>
                      <div v-else-if="tradeConfig.favorTier.demandDiscount > 0" class="text-xs text-slate-400">
                        折扣: <span class="text-yellow-400 font-bold">{{ Math.round(tradeConfig.favorTier.demandDiscount * 100) }}%</span>
                      </div>
                      <div v-else class="text-xs text-slate-400">
                        折扣: <span class="text-gray-500">无</span>
                      </div>
                      <div v-if="tradeConfig.favorTier.canFreeReward" class="text-xs text-slate-400">
                        加成: <span class="text-yellow-400 font-bold">双倍</span>
                      </div>
                      <div v-else-if="tradeConfig.favorTier.supplyBonus > 0" class="text-xs text-slate-400">
                        加成: <span class="text-green-400 font-bold">{{ Math.round(tradeConfig.favorTier.supplyBonus * 100) }}%</span>
                      </div>
                      <div v-else class="text-xs text-slate-400">
                        加成: <span class="text-gray-500">无</span>
                      </div>
                    </div>
                  </div>
                  <p class="text-xs text-slate-400 mt-2">{{ tradeConfig.favorTier.description }}</p>
                </div>

                <div class="mb-4 flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span class="text-slate-400">今日剩余交易次数:</span>
                    <span :class="tradeConfig.remainingTrades > 0 ? 'text-green-400 font-bold' : 'text-red-400 font-bold'">
                      {{ tradeConfig.remainingTrades }} / {{ tradeConfig.dailyLimit }}
                    </span>
                  </div>
                  <button
                    @click="refreshTradeConfig"
                    class="px-3 py-1 text-xs bg-slate-700 hover:bg-slate-600 rounded-lg transition"
                  >
                    🔄 刷新
                  </button>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                  <div class="bg-red-900/20 rounded-lg p-4 border border-red-800/50">
                    <h3 class="text-red-400 font-semibold mb-3 flex items-center gap-2">
                      <span>📤</span> 你需要付出
                    </h3>
                    <div class="space-y-2">
                      <div v-for="item in tradeConfig.demandItems" :key="item.itemId" class="flex items-center justify-between">
                        <div>
                          <span class="text-sm">{{ item.itemName }}</span>
                          <div class="text-xs">
                            <span v-if="item.savedQuantity && item.savedQuantity > 0" class="text-yellow-400">
                              <s class="text-slate-500">x{{ item.originalQuantity }}</s> → <strong>x{{ item.actualQuantity }}</strong>
                              <span class="text-green-400 ml-1">(省{{ item.savedQuantity }})</span>
                            </span>
                            <span v-else class="text-slate-400">x{{ item.actualQuantity ?? item.quantity }}</span>
                          </div>
                        </div>
                        <span :class="item.playerHas >= (item.actualQuantity ?? item.quantity) ? 'text-green-400 text-xs' : 'text-red-400 text-xs'">
                          你有: {{ item.playerHas }}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div class="bg-green-900/20 rounded-lg p-4 border border-green-800/50">
                    <h3 class="text-green-400 font-semibold mb-3 flex items-center gap-2">
                      <span>📥</span> 你将获得
                    </h3>
                    <div class="space-y-2">
                      <div v-for="item in tradeConfig.supplyItems" :key="item.itemId" class="flex items-center justify-between">
                        <div>
                          <span class="text-sm">{{ item.itemName }}</span>
                          <div class="text-xs">
                            <span v-if="item.extraQuantity && item.extraQuantity > 0" class="text-green-400">
                              <s class="text-slate-500">x{{ item.originalQuantity }}</s> → <strong>x{{ item.actualQuantity }}</strong>
                              <span class="text-yellow-400 ml-1">(+{{ item.extraQuantity }})</span>
                            </span>
                            <span v-else class="text-green-400">+{{ item.actualQuantity ?? item.quantity }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div v-if="!tradeConfig.canTrade" class="bg-yellow-900/20 border border-yellow-800/50 rounded-lg p-4 mb-4">
                  <p class="text-yellow-400 text-sm">
                    {{ tradeConfig.remainingTrades <= 0 ? '今日交易次数已用完' : tradeConfig.demandItems.length === 0 ? '当前好感度无法进行交易' : 'NPC当前没有可提供的物资' }}
                  </p>
                </div>

                <button
                  @click="confirmTrade"
                  :disabled="!tradeConfig.canTrade || isTrading"
                  :class="[
                    'w-full py-3 rounded-lg font-semibold transition',
                    tradeConfig.canTrade
                      ? 'bg-green-600 hover:bg-green-700'
                      : 'bg-slate-600 cursor-not-allowed'
                  ]"
                >
                  {{ isTrading ? '交易中...' : '确认交易' }}
                </button>

                <!-- 挚友特权：免费领取按钮 -->
                <div v-if="tradeConfig.favorTier?.canFreeReward" class="mt-4">
                  <div class="bg-gradient-to-r from-yellow-900/30 to-amber-900/30 border border-yellow-500/50 rounded-lg p-4 animate-pulse">
                    <div class="flex items-center gap-2 mb-2">
                      <span class="text-2xl">🌟</span>
                      <h3 class="text-yellow-400 font-bold">挚友特权</h3>
                    </div>
                    <p class="text-xs text-yellow-200/80 mb-3">
                      您与{{ tradeConfig.npcName }}好感度已达最大值！今日{{ tradeConfig.freeRewardUsed ? '已领取' : '可领取' }}一次免费物资奖励（双倍数量）。
                    </p>
                    <button
                      @click="claimFreeReward"
                      :disabled="tradeConfig.freeRewardUsed || isClaimingFreeReward"
                      :class="[
                        'w-full py-3 rounded-lg font-semibold transition',
                        tradeConfig.freeRewardUsed
                          ? 'bg-slate-600 cursor-not-allowed opacity-50'
                          : 'bg-gradient-to-r from-yellow-500 to-amber-500 hover:from-yellow-600 hover:to-amber-600 text-slate-900 animate-bounce'
                      ]"
                    >
                      {{ tradeConfig.freeRewardUsed ? '✅ 今日已领取' : (isClaimingFreeReward ? '领取中...' : '🎁 免费领取双倍物资') }}
                    </button>
                  </div>
                </div>

                <div v-if="tradeHistory.length > 0" class="mt-6">
                  <h4 class="text-sm font-semibold text-slate-400 mb-3">交易记录</h4>
                  <div class="space-y-2">
                    <div v-for="record in tradeHistory" :key="record.id" class="bg-slate-700/30 rounded-lg p-3 text-sm">
                      <div class="flex items-center justify-between mb-1">
                        <span class="text-slate-400">第{{ record.gameDay }}天</span>
                        <span v-if="record.favorChange !== 0" :class="record.favorChange > 0 ? 'text-green-400' : 'text-red-400'">
                          {{ record.favorChange > 0 ? '+' : '' }}{{ record.favorChange }} 好感
                        </span>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-red-400">{{ record.demandItems?.map(i => i.itemName + '×' + i.quantity).join(', ') || '-' }}</span>
                        <span class="text-slate-500">→</span>
                        <span class="text-green-400">{{ record.supplyItems?.map(i => i.itemName + '×' + i.quantity).join(', ') || '-' }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="activeTab === 'help'" class="p-4 max-h-[400px] overflow-y-auto">
              <div v-if="helpLoading" class="text-center py-8">
                <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto mb-2"></div>
                <p class="text-slate-400">加载中...</p>
              </div>

              <div v-else-if="helpOptions.length === 0" class="text-center text-slate-500 py-8">
                该NPC暂不提供帮助服务
              </div>

              <div v-else>
                <div class="mb-4 p-3 bg-blue-900/20 rounded-lg border border-blue-800/50">
                  <p class="text-sm text-blue-300">
                    💡 提示：只有态度为<span class="text-white font-bold">中立及以上</span>的NPC才会响应求助请求。
                    好感度越高，可请求的帮助类型越多，报酬也更优惠。
                  </p>
                </div>

                <div class="space-y-3">
                  <div v-for="option in helpOptions" :key="option.id" class="bg-slate-700/50 rounded-lg p-4 border">
                    <div class="flex items-start justify-between mb-2">
                      <div>
                        <h4 class="font-semibold text-white">{{ option.helpName }}</h4>
                        <p class="text-xs text-slate-400 mt-1">{{ option.helpDescription }}</p>
                      </div>
                      <span :class="[
                        'text-xs px-2 py-1 rounded-full',
                        option.canRequest ? 'bg-green-900/50 text-green-400' : 'bg-red-900/50 text-red-400'
                      ]">
                        {{ option.canRequest ? '可请求' : '不可请求' }}
                      </span>
                    </div>

                    <div class="grid grid-cols-2 gap-2 text-xs">
                      <div>
                        <span class="text-slate-400">报酬：</span>
                        <span :class="option.canAfford ? 'text-green-400' : 'text-red-400'">
                          {{ option.costItemName }} × {{ option.actualCost }}
                        </span>
                        <span class="text-slate-500 ml-1">(你有: {{ option.playerHas }})</span>
                      </div>
                      <div>
                        <span class="text-slate-400">成功率：</span>
                        <span class="text-yellow-400">{{ Math.round(option.successRate * 100) }}%</span>
                      </div>
                      <div>
                        <span class="text-slate-400">持续时间：</span>
                        <span class="text-blue-400">{{ option.durationMinutes }}分钟</span>
                      </div>
                      <div>
                        <span class="text-slate-400">好感度要求：</span>
                        <span :class="option.playerFavor >= getFavorThreshold(option.minFavorLevel) ? 'text-green-400' : 'text-red-400'">
                          {{ option.minFavorDisplayName }}
                        </span>
                      </div>
                    </div>

                    <div v-if="option.reason" class="mt-2 text-xs text-red-400">
                      ⚠️ {{ option.reason }}
                    </div>

                    <button
                      @click="requestHelp(option)"
                      :disabled="!option.canRequest || !option.canAfford || isRequestingHelp"
                      :class="[
                        'mt-3 w-full py-2 rounded-lg text-sm font-semibold transition',
                        option.canRequest && option.canAfford
                          ? 'bg-green-600 hover:bg-green-700'
                          : 'bg-slate-600 cursor-not-allowed opacity-50'
                      ]"
                    >
                      {{ isRequestingHelp ? '请求中...' : '💌 请求帮助' }}
                    </button>
                  </div>
                </div>

                <div v-if="helpHistory.length > 0" class="mt-6">
                  <h4 class="text-sm font-semibold text-slate-400 mb-3">求助记录</h4>
                  <div class="space-y-2">
                    <div v-for="record in helpHistory" :key="record.id" class="bg-slate-700/30 rounded-lg p-3 text-sm">
                      <div class="flex items-center justify-between mb-1">
                        <span>{{ record.helpName }}</span>
                        <span :class="{
                          'bg-yellow-900/50 text-yellow-400': record.status === 'pending',
                          'bg-green-900/50 text-green-400': record.status === 'completed',
                          'bg-red-900/50 text-red-400': record.status === 'failed'
                        }" class="text-xs px-2 py-1 rounded-full">
                          {{ record.status === 'pending' ? '处理中' : record.status === 'completed' ? '已完成' : '失败' }}
                        </span>
                      </div>
                      <div class="text-xs text-slate-400">
                        支付: {{ record.costItemName }} × {{ record.costQuantity }}
                        <span v-if="record.favorChange !== 0" :class="record.favorChange > 0 ? 'text-green-400' : 'text-red-400'" class="ml-2">
                          {{ record.favorChange > 0 ? '+' : '' }}{{ record.favorChange }} 好感
                        </span>
                      </div>
                      <div v-if="record.resultDescription" class="text-xs text-slate-300 mt-1">
                        {{ record.resultDescription }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="activeTab === 'dialogue'" class="p-4 border-t border-slate-700">
              <!-- 对话限制提示 -->
              <div v-if="dialogueLimit" class="mb-3 p-3 rounded-lg" :class="dialogueLimit.locked ? 'bg-red-900/20 border border-red-800/50' : 'bg-slate-700/30 border border-slate-600/50'">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <span v-if="dialogueLimit.locked" class="text-red-400">🔒</span>
                    <span v-else class="text-green-400">💬</span>
                    <span class="text-sm" :class="dialogueLimit.locked ? 'text-red-400' : 'text-slate-300'">
                      {{ dialogueLimit.locked ? '今日交流次数已用完' : '今日剩余交流次数' }}
                    </span>
                  </div>
                  <span class="text-sm font-bold" :class="dialogueLimit.locked ? 'text-red-400' : 'text-green-400'">
                    {{ dialogueLimit.remainingChats }} / {{ dialogueLimit.dailyLimit }}
                  </span>
                </div>
                <p v-if="dialogueLimit.locked" class="text-xs text-red-400/70 mt-1">
                  请等待DM更新游戏天数后重置
                </p>
              </div>

              <div class="flex gap-3">
                <input
                  v-model="message"
                  @keyup.enter="sendMessage"
                  type="text"
                  :placeholder="dialogueLimit?.locked ? '交流次数已用完，无法发送消息' : '输入消息...'"
                  class="flex-1 bg-slate-700 border border-slate-600 rounded-lg px-4 py-3 focus:outline-none focus:border-blue-500"
                  :disabled="isSending || dialogueLimit?.locked"
                />
                <button
                  @click="sendMessage"
                  :disabled="isSending || !message.trim() || dialogueLimit?.locked"
                  class="px-6 py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 disabled:cursor-not-allowed rounded-lg transition"
                >
                  {{ isSending ? '发送中...' : '发送' }}
                </button>
              </div>
              
              <div class="mt-3 flex flex-wrap gap-2">
                <button
                  v-for="quickMsg in quickMessages"
                  :key="quickMsg"
                  @click="sendQuickMessage(quickMsg)"
                  :disabled="dialogueLimit?.locked"
                  class="px-3 py-1 text-xs bg-slate-700 hover:bg-slate-600 disabled:opacity-50 disabled:cursor-not-allowed rounded-full transition"
                >
                  {{ quickMsg }}
                </button>
              </div>
            </div>
          </div>

          <div v-else class="bg-slate-800/50 rounded-xl p-12 text-center border border-slate-700">
            <div class="text-6xl mb-4">👥</div>
            <p class="text-slate-400">选择一个 NPC 开始对话</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { npcAPI } from '../utils/api'

const npcs = ref([])
const selectedNpc = ref(null)
const dialogues = ref([])
const message = ref('')
const isSending = ref(false)
const activeTab = ref('dialogue')
const tradeConfig = ref(null)
const tradeLoading = ref(false)
const isTrading = ref(false)
const tradeHistory = ref([])
const dialogueLimit = ref(null)
const dailyDialogueLimit = ref(10)
const isClaimingFreeReward = ref(false)
const helpOptions = ref([])
const helpHistory = ref([])
const helpLoading = ref(false)
const isRequestingHelp = ref(false)

const quickMessages = ['你好', '有什么任务吗？', '最近怎么样？', '谢谢你', '再见']

const playerId = parseInt(localStorage.getItem('playerId') || localStorage.getItem('userId') || '0', 10) || 1
const cognitionStats = ref({})

async function refreshNpcs() {
  const [npcData, statsData] = await Promise.all([
    npcAPI.getAllNpcs(playerId),
    npcAPI.getCognitionStats(playerId)
  ])
  if (npcData) {
    npcs.value = npcData
  }
  if (statsData) {
    cognitionStats.value = statsData
  }
}

async function selectNpc(npc) {
  selectedNpc.value = npc
  activeTab.value = 'dialogue'
  await Promise.all([
    loadDialogues(npc.id),
    loadDialogueLimit(npc.id)
  ])
}

async function loadDialogues(npcId) {
  const data = await npcAPI.getDialogueHistory(playerId, npcId)
  if (data) {
    dialogues.value = data
  }
}

async function loadDialogueLimit(npcId) {
  const data = await npcAPI.getDialogueLimit(playerId, npcId)
  if (data) {
    dialogueLimit.value = data
    if (data.dailyLimit) {
      dailyDialogueLimit.value = data.dailyLimit
    }
  }
}

async function sendMessage() {
  if (!message.value.trim() || isSending.value || !selectedNpc.value) return
  
  isSending.value = true
  try {
    const result = await npcAPI.sendMessage(playerId, selectedNpc.value.id, message.value.trim())
    
    if (result?.success) {
      dialogues.value.push({
        playerMessage: message.value.trim(),
        npcReply: result.reply,
        favorChange: result.favorChange
      })
      
      if (result.newFavor != null) {
        selectedNpc.value.favorValue = result.newFavor
        selectedNpc.value.favorLevel = result.favorLevel
      }
      
      // 更新对话限制信息
      if (result.remainingChats != null) {
        if (!dialogueLimit.value) {
          dialogueLimit.value = {}
        }
        dialogueLimit.value.remainingChats = result.remainingChats
        dialogueLimit.value.locked = result.locked || false
        dialogueLimit.value.dailyLimit = dailyDialogueLimit || 10
        if (dialogueLimit.value.locked) {
          dialogueLimit.value.message = '今日与该NPC的交流次数已用完，请等待明天重置或联系DM更新游戏天数'
        }
      }
      
      message.value = ''
      
      await refreshNpcs()
    } else {
      alert(result?.message || '发送失败')
      // 如果被锁定，更新限制状态
      if (result?.locked) {
        if (!dialogueLimit.value) {
          dialogueLimit.value = {}
        }
        dialogueLimit.value.locked = true
        dialogueLimit.value.remainingChats = 0
        dialogueLimit.value.dailyLimit = dailyDialogueLimit || 10
        dialogueLimit.value.message = result.message
      }
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    alert('发送失败，请重试')
  } finally {
    isSending.value = false
  }
}

function sendQuickMessage(msg) {
  message.value = msg
  sendMessage()
}

async function refreshTradeConfig() {
  if (!selectedNpc.value) return
  tradeLoading.value = true
  try {
    const data = await npcAPI.getTradeConfig(playerId, selectedNpc.value.id)
    if (data?.success) {
      tradeConfig.value = data
    } else {
      tradeConfig.value = null
    }
  } catch (error) {
    console.error('加载交易配置失败:', error)
    tradeConfig.value = null
  } finally {
    tradeLoading.value = false
  }
}

async function confirmTrade() {
  if (!tradeConfig.value?.canTrade || isTrading.value || !selectedNpc.value) return
  
  // 展示带折扣的交易确认信息
  const demandInfo = tradeConfig.value.demandItems.map(i => {
    if (i.actualQuantity !== undefined && i.actualQuantity < i.quantity) {
      return `${i.itemName}×${i.actualQuantity} (原价×${i.quantity})`
    }
    return `${i.itemName}×${i.quantity}`
  }).join(', ')
  
  const supplyInfo = tradeConfig.value.supplyItems.map(i => {
    if (i.actualQuantity !== undefined && i.actualQuantity > i.quantity) {
      return `${i.itemName}×${i.actualQuantity} (原价×${i.quantity})`
    }
    return `${i.itemName}×${i.quantity}`
  }).join(', ')
  
  if (!confirm(`确定要进行交易吗？\n\n你将付出: ${demandInfo}\n你将获得: ${supplyInfo}`)) {
    return
  }
  
  isTrading.value = true
  try {
    const result = await npcAPI.executeTrade(playerId, selectedNpc.value.id)
    
    if (result?.success) {
      alert(result.message)
      await refreshTradeConfig()
      await loadTradeHistory()
      await refreshNpcs()
      
      if (result.newFavor != null && selectedNpc.value) {
        selectedNpc.value.favorValue = result.newFavor
      }
    } else {
      alert(result?.message || '交易失败')
    }
  } catch (error) {
    console.error('交易失败:', error)
    alert('交易失败，请重试')
  } finally {
    isTrading.value = false
  }
}

async function claimFreeReward() {
  if (!selectedNpc.value || isClaimingFreeReward.value) return
  
  if (!confirm(`作为${selectedNpc.value.name}的挚友，确认免费领取双倍物资奖励吗？`)) {
    return
  }
  
  isClaimingFreeReward.value = true
  try {
    const result = await npcAPI.claimFreeReward(playerId, selectedNpc.value.id)
    
    if (result?.success) {
      const items = result.rewardItems?.map(i => `${i.itemName}×${i.actualQuantity}`).join(', ') || ''
      alert(`🎉 ${result.message}\n获得: ${items}`)
      await refreshTradeConfig()
      await loadTradeHistory()
    } else {
      alert(result?.message || '领取失败')
    }
  } catch (error) {
    console.error('领取失败:', error)
    alert('领取失败，请重试')
  } finally {
    isClaimingFreeReward.value = false
  }
}

async function loadTradeHistory() {
  if (!selectedNpc.value) return
  try {
    const data = await npcAPI.getTradeHistory(playerId, selectedNpc.value.id)
    if (data) {
      tradeHistory.value = data
    }
  } catch (error) {
    console.error('加载交易历史失败:', error)
    tradeHistory.value = []
  }
}

watch(activeTab, async (newTab) => {
  if (newTab === 'trade' && selectedNpc.value) {
    await refreshTradeConfig()
    await loadTradeHistory()
  } else if (newTab === 'help' && selectedNpc.value) {
    await loadHelpOptions()
    await loadHelpHistory()
  }
})

async function loadHelpOptions() {
  if (!selectedNpc.value) return
  helpLoading.value = true
  try {
    const data = await npcAPI.getHelpOptions(playerId, selectedNpc.value.id)
    if (data) {
      helpOptions.value = data
    }
  } catch (error) {
    console.error('加载求助选项失败:', error)
    helpOptions.value = []
  } finally {
    helpLoading.value = false
  }
}

async function loadHelpHistory() {
  if (!selectedNpc.value) return
  try {
    const data = await npcAPI.getHelpHistory(playerId, selectedNpc.value.id)
    if (data) {
      helpHistory.value = data
    }
  } catch (error) {
    console.error('加载求助历史失败:', error)
    helpHistory.value = []
  }
}

async function requestHelp(option) {
  if (!selectedNpc.value || !option.canRequest || isRequestingHelp.value) return
  
  if (!confirm(`确认请求【${option.helpName}】吗？\n\n报酬：${option.costItemName} × ${option.actualCost}`)) {
    return
  }
  
  isRequestingHelp.value = true
  try {
    const result = await npcAPI.requestHelp(playerId, selectedNpc.value.id, option.helpType)
    
    if (result?.success) {
      alert(result.message)
      await loadHelpOptions()
      await loadHelpHistory()
      await refreshNpcs()
      
      if (result.favorChange != null && selectedNpc.value) {
        selectedNpc.value.favorValue = (selectedNpc.value.favorValue || 0) + result.favorChange
      }
    } else {
      alert(result?.message || '请求失败')
    }
  } catch (error) {
    console.error('请求帮助失败:', error)
    alert('请求帮助失败，请重试')
  } finally {
    isRequestingHelp.value = false
  }
}

function getFavorThreshold(level) {
  const thresholds = {
    'hostile': -100,
    'unfriendly': -60,
    'neutral': -20,
    'friendly': 20,
    'close': 60
  }
  return thresholds[level] || -100
}

onMounted(() => {
  refreshNpcs()
})
</script>