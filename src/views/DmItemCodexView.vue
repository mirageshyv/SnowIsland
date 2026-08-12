<script setup>
import { ref, computed, onMounted } from 'vue'
import { dmPlayerAPI } from '../utils/api.js'
import { getWeaponThreatBadgeClass } from '../data/gameData.js'

const bundled = import.meta.glob('../assets/*.png', { eager: true, import: 'default' })

const TYPE_LABELS = {
  item: '道具',
  weapon: '武器',
  ammo: '弹药',
  material: '材料',
}

const TYPE_OPTIONS = [
  { value: 'all', label: '全部' },
  { value: 'item', label: '道具' },
  { value: 'weapon', label: '武器' },
  { value: 'ammo', label: '弹药' },
  { value: 'material', label: '材料' },
]

const catalogRows = ref([])
const loading = ref(true)
const error = ref('')
const message = ref('')

const searchQuery = ref('')
const filterType = ref('all')
const missingImageOnly = ref(false)

const editingEntry = ref(null)
const editTag = ref('')
const editRemark = ref('')
const editSaving = ref(false)
const editUploading = ref(false)
const previewImageUrl = ref('')
const selectedFile = ref(null)

function bundledAssetUrl(name) {
  const key = `../assets/${name}.png`
  return bundled[key] || null
}

function resolveImageUrl(entry) {
  if (entry.imageUrl) return entry.imageUrl
  return bundledAssetUrl(entry.name) || null
}

function hasImage(entry) {
  return Boolean(entry.imageUrl || bundledAssetUrl(entry.name))
}

const filteredRows = computed(() => {
  let rows = catalogRows.value
  if (filterType.value !== 'all') {
    rows = rows.filter((r) => r.itemType === filterType.value)
  }
  if (missingImageOnly.value) {
    rows = rows.filter((r) => !hasImage(r))
  }
  if (searchQuery.value.trim()) {
    const q = searchQuery.value.trim().toLowerCase()
    rows = rows.filter((r) => {
      const tag = (r.tag || '').toLowerCase()
      const remark = (r.remark || '').toLowerCase()
      const name = (r.name || '').toLowerCase()
      return name.includes(q) || tag.includes(q) || remark.includes(q)
    })
  }
  return rows
})

async function loadCatalog() {
  loading.value = true
  error.value = ''
  try {
    const result = await dmPlayerAPI.getCatalog()
    if (result?.success !== false && Array.isArray(result?.items)) {
      catalogRows.value = result.items
    } else {
      error.value = result?.message || '无法加载图鉴'
    }
  } catch (e) {
    error.value = '加载失败：' + (e.message || '未知错误')
  } finally {
    loading.value = false
  }
}

function openEdit(entry) {
  editingEntry.value = { ...entry }
  editTag.value = entry.tag || ''
  editRemark.value = entry.remark || ''
  previewImageUrl.value = resolveImageUrl(entry) || ''
  selectedFile.value = null
  message.value = ''
}

function closeEdit() {
  editingEntry.value = null
  selectedFile.value = null
}

function onFileSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return
  selectedFile.value = file
  previewImageUrl.value = URL.createObjectURL(file)
}

async function saveEdit() {
  if (!editingEntry.value) return
  editSaving.value = true
  message.value = ''
  error.value = ''
  const { itemType, itemId } = editingEntry.value
  try {
    const result = await dmPlayerAPI.updateCatalogEntry(itemType, itemId, {
      tag: editTag.value,
      remark: editRemark.value,
    })
    if (result?.success) {
      const idx = catalogRows.value.findIndex(
        (r) => r.itemType === itemType && r.itemId === itemId
      )
      if (idx >= 0) {
        catalogRows.value[idx] = {
          ...catalogRows.value[idx],
          tag: editTag.value,
          remark: editRemark.value,
        }
      }
      message.value = result.message || '已保存'
      if (selectedFile.value) {
        await uploadImage()
      } else {
        closeEdit()
      }
    } else {
      error.value = result?.message || '保存失败'
    }
  } catch (e) {
    error.value = '保存失败：' + (e.message || '未知错误')
  } finally {
    editSaving.value = false
  }
}

async function uploadImage() {
  if (!editingEntry.value || !selectedFile.value) return
  editUploading.value = true
  const { itemType, itemId } = editingEntry.value
  try {
    const result = await dmPlayerAPI.uploadCatalogImage(itemType, itemId, selectedFile.value)
    if (result?.success) {
      const idx = catalogRows.value.findIndex(
        (r) => r.itemType === itemType && r.itemId === itemId
      )
      if (idx >= 0 && result.imageUrl) {
        catalogRows.value[idx] = {
          ...catalogRows.value[idx],
          imageUrl: result.imageUrl,
        }
      }
      message.value = result.message || '图片已上传'
      closeEdit()
    } else {
      error.value = result?.message || '上传失败'
    }
  } catch (e) {
    error.value = '上传失败：' + (e.message || '未知错误')
  } finally {
    editUploading.value = false
    selectedFile.value = null
  }
}

onMounted(loadCatalog)
</script>

<template>
  <div>
    <div class="mb-6">
      <h1 class="text-white mb-1 tracking-tight text-2xl">图鉴管理</h1>
      <p class="text-gray-500 text-sm">浏览物品目录，编辑标签、描述与图片</p>
    </div>

    <div v-if="message" class="mb-4 rounded-lg bg-emerald-900/40 border border-emerald-700/50 px-4 py-2 text-emerald-300 text-sm">
      {{ message }}
    </div>
    <div v-if="error" class="mb-4 rounded-lg bg-red-900/40 border border-red-700/50 px-4 py-2 text-red-300 text-sm">
      {{ error }}
    </div>

    <div class="mb-6 flex flex-wrap items-center gap-3">
      <input
        v-model="searchQuery"
        type="search"
        placeholder="搜索名称 / 标签 / 描述…"
        class="flex-1 min-w-[200px] rounded-lg border border-slate-600 bg-slate-800/80 px-3 py-2 text-sm text-white placeholder-gray-500 focus:border-blue-500 focus:outline-none"
      />
      <select
        v-model="filterType"
        class="rounded-lg border border-slate-600 bg-slate-800/80 px-3 py-2 text-sm text-white focus:border-blue-500 focus:outline-none"
      >
        <option v-for="opt in TYPE_OPTIONS" :key="opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
      <label class="flex items-center gap-2 text-sm text-gray-400 cursor-pointer select-none">
        <input v-model="missingImageOnly" type="checkbox" class="rounded border-slate-600" />
        仅显示缺图
      </label>
      <button
        type="button"
        class="rounded-lg bg-slate-700 px-3 py-2 text-sm text-gray-300 hover:bg-slate-600 transition-colors"
        @click="loadCatalog"
      >
        刷新
      </button>
    </div>

    <div v-if="loading" class="text-gray-500 py-12 text-center">加载中…</div>

    <div v-else-if="filteredRows.length === 0" class="text-gray-500 py-12 text-center">
      没有匹配的条目
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      <div
        v-for="entry in filteredRows"
        :key="`${entry.itemType}-${entry.itemId}`"
        class="rounded-xl border border-slate-700/60 bg-slate-900/60 overflow-hidden flex flex-col"
      >
        <div class="aspect-square bg-slate-800/80 flex items-center justify-center overflow-hidden">
          <img
            v-if="resolveImageUrl(entry)"
            :src="resolveImageUrl(entry)"
            :alt="entry.name"
            class="w-full h-full object-contain p-2"
          />
          <span v-else class="text-gray-500 text-sm">缺图</span>
        </div>
        <div class="p-3 flex-1 flex flex-col gap-2">
          <div class="flex items-start justify-between gap-2">
            <div class="min-w-0">
              <h3 class="text-white font-medium truncate">{{ entry.name }}</h3>
              <p class="text-gray-500 text-xs">{{ entry.unit }}</p>
            </div>
            <span class="shrink-0 rounded px-1.5 py-0.5 text-xs bg-slate-700 text-gray-300">
              {{ TYPE_LABELS[entry.itemType] || entry.itemType }}
            </span>
          </div>
          <span
            v-if="entry.itemType === 'weapon' && entry.threatLevel != null"
            class="inline-block self-start rounded px-1.5 py-0.5 text-xs"
            :class="getWeaponThreatBadgeClass(entry.threatLevel)"
          >
            威胁 {{ entry.threatLevel }}
          </span>
          <span
            v-if="entry.tag"
            class="inline-block self-start rounded-full px-2 py-0.5 text-xs bg-indigo-900/60 text-indigo-300 border border-indigo-700/50"
          >
            {{ entry.tag }}
          </span>
          <p class="text-gray-400 text-xs line-clamp-3 flex-1">{{ entry.remark || '（无描述）' }}</p>
          <button
            type="button"
            class="mt-auto w-full rounded-lg bg-blue-600/80 py-1.5 text-sm text-white hover:bg-blue-600 transition-colors"
            @click="openEdit(entry)"
          >
            编辑
          </button>
        </div>
      </div>
    </div>

    <!-- Edit modal -->
    <div
      v-if="editingEntry"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60"
      @click.self="closeEdit"
    >
      <div
        class="w-full max-w-lg rounded-xl border border-slate-700 bg-[rgba(15,20,35,0.98)] p-6 shadow-xl"
        @click.stop
      >
        <h2 class="text-white text-lg mb-1">{{ editingEntry.name }}</h2>
        <p class="text-gray-500 text-sm mb-4">
          {{ TYPE_LABELS[editingEntry.itemType] }} · ID {{ editingEntry.itemId }}
        </p>

        <div class="mb-4 flex justify-center">
          <div class="w-32 h-32 rounded-lg bg-slate-800 flex items-center justify-center overflow-hidden">
            <img
              v-if="previewImageUrl"
              :src="previewImageUrl"
              :alt="editingEntry.name"
              class="w-full h-full object-contain p-1"
            />
            <span v-else class="text-gray-500 text-sm">缺图</span>
          </div>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-gray-400 text-sm mb-1">标签</label>
            <input
              v-model="editTag"
              type="text"
              placeholder="如：不可交易 / 仪式材料 / 任务道具"
              class="w-full rounded-lg border border-slate-600 bg-slate-800/80 px-3 py-2 text-sm text-white placeholder-gray-500 focus:border-blue-500 focus:outline-none"
            />
          </div>
          <div>
            <label class="block text-gray-400 text-sm mb-1">描述</label>
            <textarea
              v-model="editRemark"
              rows="4"
              class="w-full rounded-lg border border-slate-600 bg-slate-800/80 px-3 py-2 text-sm text-white placeholder-gray-500 focus:border-blue-500 focus:outline-none resize-y"
            />
          </div>
          <div>
            <label class="block text-gray-400 text-sm mb-1">图片</label>
            <input
              type="file"
              accept="image/png,image/jpeg,image/gif,image/webp"
              class="w-full text-sm text-gray-400 file:mr-3 file:rounded file:border-0 file:bg-slate-700 file:px-3 file:py-1.5 file:text-sm file:text-gray-200"
              @change="onFileSelected"
            />
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-3">
          <button
            type="button"
            class="rounded-lg px-4 py-2 text-sm text-gray-400 hover:bg-white/5"
            :disabled="editSaving || editUploading"
            @click="closeEdit"
          >
            取消
          </button>
          <button
            type="button"
            class="rounded-lg bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-500 disabled:opacity-50"
            :disabled="editSaving || editUploading"
            @click="saveEdit"
          >
            {{ editSaving || editUploading ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
