<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white">
    <div class="sticky top-0 z-40 bg-slate-900/80 backdrop-blur-md border-b border-slate-700">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <div class="flex items-center justify-between">
          <h1 class="text-2xl font-bold bg-gradient-to-r from-blue-400 to-cyan-400 bg-clip-text text-transparent">
            海岛生存规则书
          </h1>
          <div class="flex gap-4">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              @click="activeTab = tab.key"
              :class="[
                'px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200',
                activeTab === tab.key
                  ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/30'
                  : 'bg-slate-700/50 text-slate-400 hover:bg-slate-700 hover:text-white'
              ]"
            >
              {{ tab.label }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 py-8">
      <div v-if="activeTab === 'map'" class="space-y-6">
        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-xl font-semibold mb-4 text-cyan-400">海岛小镇地图</h2>
          <div class="relative rounded-lg overflow-hidden shadow-2xl">
            <img
              :src="mapImageUrl"
              alt="海岛小镇地图"
              class="w-full h-auto max-h-[70vh] object-contain rounded-lg"
            />
          </div>
          <div class="mt-6 grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
            <div v-for="location in locations" :key="location.name" class="bg-slate-700/50 rounded-lg p-3">
              <div class="flex items-center gap-2">
                <span class="text-cyan-400">{{ location.icon }}</span>
                <span class="text-sm">{{ location.name }}</span>
              </div>
              <p class="text-xs text-slate-400 mt-1">{{ location.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="space-y-6">
        <div class="bg-slate-800/50 rounded-xl p-6 border border-slate-700">
          <h2 class="text-xl font-semibold mb-6 text-cyan-400">{{ currentSectionTitle }}</h2>
          <div class="space-y-4">
            <div
              v-for="rule in currentRules"
              :key="rule.id"
              class="bg-slate-700/50 rounded-lg p-5 border border-slate-600/50 hover:border-cyan-500/50 transition-all duration-200"
            >
              <h3 class="text-lg font-semibold text-blue-400 mb-2">{{ rule.title }}</h3>
              <p class="text-slate-300 leading-relaxed">{{ rule.content }}</p>
            </div>
          </div>
          <div v-if="currentRules.length === 0" class="text-center py-12 text-slate-400">
            <p>暂无规则内容</p>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

const activeTab = ref('map');
const rules = ref({});

const defaultRules = {
  general: [
    { id: 1, title: '游戏目标', content: '海岛生存游戏的核心目标是在严酷的环境中生存下去，并通过完成各种任务和行动来推进游戏进程。玩家需要管理自己的资源、满足每日需求，并与其他玩家互动。', orderNum: 1 },
    { id: 2, title: '每日需求', content: '每位玩家每天需要消耗一定数量的食物和燃料来维持生存。食物不足将导致虚弱状态，燃料不足将无法取暖，同样会导致虚弱。', orderNum: 2 },
    { id: 3, title: '行动系统', content: '每位玩家每天有一定的行动点数，可以用来执行各种行动。行动类型包括调查、生产、交易、移动等。', orderNum: 3 },
    { id: 4, title: '夜晚行动', content: '夜晚是危险的时刻，玩家可以选择执行夜晚行动，但也可能遭遇意外事件。夜晚行动需要谨慎选择。', orderNum: 4 },
    { id: 5, title: '虚弱状态', content: '当玩家未能满足当日需求时，次日将陷入虚弱状态。虚弱状态会影响行动效果和成功率。', orderNum: 5 }
  ],
  ruler: [
    { id: 101, title: '阵营特性', content: '统治者拥有最高的权威，可以管理避难所、安排劳工，并对反叛者实施制裁。统治者的决策影响整个海岛的命运。', orderNum: 1 },
    { id: 102, title: '劳工管理', content: '统治者可以选择NPC作为劳工参与避难所建设。劳工的效率取决于其职业和状态。', orderNum: 2 },
    { id: 103, title: '制裁权力', content: '统治者可以对反叛者实施制裁，包括限制行动、没收物资等。但过度制裁可能引起民愤。', orderNum: 3 },
    { id: 104, title: '避难所管理', content: '统治者负责避难所的建设和物资分配。合理分配资源是统治者的核心职责。', orderNum: 4 }
  ],
  rebel: [
    { id: 201, title: '阵营特性', content: '反叛者致力于推翻统治者的统治，建立新的秩序。反叛者需要秘密组织活动，避免被统治者察觉。', orderNum: 1 },
    { id: 202, title: '秘密行动', content: '反叛者可以执行秘密行动，包括破坏、暗杀、窃取情报等。成功的秘密行动可以削弱统治者的权威。', orderNum: 2 },
    { id: 203, title: '地下网络', content: '反叛者拥有自己的地下网络，可以共享资源、传递消息。网络的发展壮大是反叛成功的关键。', orderNum: 3 },
    { id: 204, title: '里程碑', content: '反叛者需要完成一系列里程碑来提升影响力。每个里程碑都会解锁新的能力和资源。', orderNum: 4 }
  ],
  adventurer: [
    { id: 301, title: '阵营特性', content: '冒险者是勇敢的探索者，探索未知、寻找宝藏是他们的天性。冒险者可以前往危险区域获取稀有资源。', orderNum: 1 },
    { id: 302, title: '方舟建设', content: '冒险者的终极目标是建造一艘方舟，带领幸存者逃离海岛。方舟需要大量资源和时间来建造。', orderNum: 2 },
    { id: 303, title: '探索行动', content: '冒险者可以探索海岛的各个角落，发现隐藏的资源和秘密。探索有风险，但回报丰厚。', orderNum: 3 },
    { id: 304, title: '特殊能力', content: '冒险者拥有独特的生存技能，可以在恶劣环境中生存，并找到其他玩家无法获取的资源。', orderNum: 4 }
  ],
  scourge: [
    { id: 401, title: '阵营特性', content: '天灾使者是神秘的存在，他们掌握着天灾的力量，可以召唤灾难、散播恐惧。', orderNum: 1 },
    { id: 402, title: '天灾卡牌', content: '天灾使者可以触发天灾卡牌，给海岛带来各种灾难。灾难可以削弱其他玩家的生存能力。', orderNum: 2 },
    { id: 403, title: '恐惧散播', content: '天灾使者的存在本身就是一种威慑。其他玩家对天灾使者既恐惧又敬畏。', orderNum: 3 },
    { id: 404, title: '黑暗仪式', content: '天灾使者可以执行黑暗仪式来增强自己的力量，但仪式需要付出代价。', orderNum: 4 }
  ],
  civilian: [
    { id: 501, title: '阵营特性', content: '平民是海岛的普通居民，他们没有特殊能力，但也不受阵营冲突的直接影响。平民需要在各方势力之间求生存。', orderNum: 1 },
    { id: 502, title: '灵活性', content: '平民可以选择支持不同的阵营，获得相应的保护和资源。明智的选择是平民生存的关键。', orderNum: 2 },
    { id: 503, title: '低调生存', content: '平民的优势在于不引人注目。保持低调可以避免成为冲突的目标。', orderNum: 3 },
    { id: 504, title: '秘密行动', content: '平民可以执行一些秘密行动来影响游戏进程，但需要谨慎行事。', orderNum: 4 }
  ]
};

const tabs = [
  { key: 'map', label: '海岛地图' },
  { key: 'general', label: '通用规则' },
  { key: 'ruler', label: '统治者' },
  { key: 'rebel', label: '反叛者' },
  { key: 'adventurer', label: '冒险者' },
  { key: 'scourge', label: '天灾使者' },
  { key: 'civilian', label: '平民' }
];

const mapImageUrl = '/src/assets/小镇地图.png';
const locations = [
  { name: '镇长厅', icon: '🏛️', desc: '统治者办公地点' },
  { name: '警察局', icon: '🏢', desc: '维持治安' },
  { name: '教堂', icon: '⛪', desc: '宗教活动场所' },
  { name: '集市', icon: '🏪', desc: '交易中心' },
  { name: '码头', icon: '🚢', desc: '船只停靠处' },
  { name: '灯塔', icon: '🗼', desc: '指引方向' },
  { name: '矿场', icon: '⛏️', desc: '矿石开采' },
  { name: '伐木营地', icon: '🪓', desc: '木材收集' },
  { name: '猎人小屋', icon: '🏚️', desc: '山区猎人据点' },
  { name: '监狱', icon: '🏯', desc: '关押囚犯' },
  { name: '墓地', icon: '⛁', desc: '逝者安息' },
  { name: '气象台', icon: '🌤️', desc: '天气观测' }
];

const currentSectionTitle = computed(() => {
  const tab = tabs.find(t => t.key === activeTab.value);
  return tab ? tab.label : '';
});

const currentRules = computed(() => {
  if (activeTab.value === 'map') return [];
  const sectionRules = rules.value[activeTab.value];
  if (sectionRules && sectionRules.length > 0) {
    return sectionRules;
  }
  return defaultRules[activeTab.value] || [];
});

const fetchRules = async () => {
  try {
    const response = await fetch('/api/rule-book/all');
    const data = await response.json();
    if (data.success) {
      rules.value = data.data;
    }
  } catch (error) {
    console.error('Failed to fetch rules:', error);
    rules.value = defaultRules;
  }
};

onMounted(() => {
  rules.value = defaultRules;
  fetchRules();
});
</script>
