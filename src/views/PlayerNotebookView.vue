<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { notebookAPI, dmPlayerAPI } from '@/utils/api.js'

const props = defineProps({
  readOnly: { type: Boolean, default: false },
  playerId: { type: Number, default: null },
})

const TITLE_MAX = 80
const BODY_MAX = 50000

const pages = ref([])
const maxPages = ref(30)
const selectedId = ref(null)
const title = ref('')
const body = ref('')
const loadingList = ref(false)
const loadingPage = ref(false)
const saving = ref(false)
const saveState = ref('')
const error = ref('')

let saveTimer = null
let skipWatch = false

const atPageLimit = computed(() => pages.value.length >= maxPages.value)
const canMutate = computed(() => !props.readOnly)

function showError(text) {
  error.value = text || ''
  if (text) setTimeout(() => { if (error.value === text) error.value = '' }, 3500)
}

async function listPages() {
  if (props.readOnly) {
    if (props.playerId == null) return { success: false, message: '未选择玩家' }
    return dmPlayerAPI.listNotebook(props.playerId)
  }
  return notebookAPI.list()
}

async function fetchPage(id) {
  if (props.readOnly) {
    return dmPlayerAPI.getNotebookPage(props.playerId, id)
  }
  return notebookAPI.get(id)
}

async function loadList() {
  if (props.readOnly && props.playerId == null) {
    pages.value = []
    selectedId.value = null
    title.value = ''
    body.value = ''
    loadingList.value = false
    return
  }
  loadingList.value = true
  try {
    const res = await listPages()
    if (res?.success) {
      pages.value = Array.isArray(res.pages) ? res.pages : []
      if (res.maxPages) maxPages.value = res.maxPages
      if (!pages.value.length) {
        selectedId.value = null
        title.value = ''
        body.value = ''
      } else if (!selectedId.value || !pages.value.some((p) => p.id === selectedId.value)) {
        await openPage(pages.value[0].id)
      } else {
        await openPage(selectedId.value)
      }
    } else {
      showError(res?.message || '无法加载笔记本')
    }
  } catch {
    showError('无法加载笔记本')
  } finally {
    loadingList.value = false
  }
}

async function openPage(id) {
  if (id == null) return
  if (canMutate.value) await flushSave()
  selectedId.value = id
  loadingPage.value = true
  skipWatch = true
  try {
    const res = await fetchPage(id)
    if (res?.success && res.page) {
      title.value = res.page.title || ''
      body.value = res.page.body || ''
      await nextTick()
    } else {
      showError(res?.message || '无法打开这一页')
    }
  } catch {
    showError('无法打开这一页')
  } finally {
    loadingPage.value = false
    skipWatch = false
  }
}

async function createPage() {
  if (!canMutate.value) return
  if (atPageLimit.value) {
    showError(`最多 ${maxPages.value} 页`)
    return
  }
  await flushSave()
  const res = await notebookAPI.create()
  if (res?.success && res.page) {
    pages.value.push({
      id: res.page.id,
      title: res.page.title,
      sortOrder: res.page.sortOrder,
      updatedAt: res.page.updatedAt,
    })
    skipWatch = true
    selectedId.value = res.page.id
    title.value = res.page.title || '未命名'
    body.value = res.page.body || ''
    await nextTick()
    skipWatch = false
    saveState.value = '已保存'
  } else {
    showError(res?.message || '无法新建')
  }
}

async function deletePage(id) {
  if (!canMutate.value || !id) return
  if (!confirm('删除这一页？')) return
  if (saveTimer && selectedId.value === id) {
    clearTimeout(saveTimer)
    saveTimer = null
  }
  const res = await notebookAPI.remove(id)
  if (!res?.success) {
    showError(res?.message || '删除失败')
    return
  }
  pages.value = pages.value.filter((p) => p.id !== id)
  if (selectedId.value === id) {
    selectedId.value = null
    title.value = ''
    body.value = ''
    if (pages.value.length) await openPage(pages.value[0].id)
  }
}

function scheduleSave() {
  if (!canMutate.value || skipWatch || !selectedId.value) return
  saveState.value = '未保存'
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(() => { saveTimer = null; persist() }, 1500)
}

async function persist() {
  if (!canMutate.value || !selectedId.value) return
  const payload = {
    title: title.value,
    body: body.value,
  }
  saving.value = true
  saveState.value = '保存中'
  try {
    const res = await notebookAPI.patch(selectedId.value, payload)
    if (res?.success) {
      const page = res.page
      const row = pages.value.find((p) => p.id === selectedId.value)
      if (row && page) {
        row.title = page.title
        row.updatedAt = page.updatedAt
      }
      saveState.value = '已保存'
    } else {
      saveState.value = '保存失败'
      showError(res?.message || '保存失败')
    }
  } catch {
    saveState.value = '保存失败'
    showError('保存失败')
  } finally {
    saving.value = false
  }
}

async function flushSave() {
  if (!canMutate.value) return
  if (saveTimer) {
    clearTimeout(saveTimer)
    saveTimer = null
    await persist()
  }
}

watch([title, body], () => scheduleSave())

watch(
  () => props.playerId,
  () => {
    selectedId.value = null
    title.value = ''
    body.value = ''
    pages.value = []
    loadList()
  }
)

function onHidden() {
  if (document.hidden) flushSave()
}

onMounted(() => {
  if (!props.readOnly || props.playerId != null) loadList()
  if (canMutate.value) {
    document.addEventListener('visibilitychange', onHidden)
    window.addEventListener('beforeunload', flushSave)
  }
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', onHidden)
  window.removeEventListener('beforeunload', flushSave)
  flushSave()
})
</script>

<template>
  <div class="nb">
    <div v-if="!readOnly" class="mb-5 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 class="text-white text-2xl font-semibold tracking-tight">笔记本</h1>
        <p class="text-slate-400 text-sm mt-1">记下你想记住的事。只给你自己看。</p>
      </div>
      <p class="text-xs text-slate-500">
        <span v-if="saving || saveState === '保存中'">保存中…</span>
        <span v-else-if="saveState === '未保存'">未保存</span>
        <span v-else-if="saveState === '已保存'">已保存</span>
        <span v-else-if="saveState === '保存失败'" class="text-red-300">保存失败</span>
      </p>
    </div>

    <p v-if="error" class="mb-3 text-sm text-red-300">{{ error }}</p>

    <div class="grid grid-cols-1 md:grid-cols-[13rem_minmax(0,1fr)] gap-4 min-h-[28rem]">
      <aside class="rounded-2xl border border-white/10 bg-[#141b27] p-3 flex flex-col">
        <button
          v-if="!readOnly"
          type="button"
          class="mb-2 w-full rounded-xl border border-white/15 bg-white/5 px-3 py-2 text-sm text-slate-100 hover:bg-white/10 disabled:opacity-40"
          :disabled="atPageLimit || loadingList"
          @click="createPage"
        >
          新的一页
        </button>
        <p v-if="!readOnly && atPageLimit" class="text-[11px] text-slate-500 mb-2">已达 {{ maxPages }} 页上限</p>
        <div v-if="loadingList" class="text-slate-500 text-sm px-1 py-6 text-center">加载中…</div>
        <ul v-else class="space-y-1 overflow-y-auto flex-1">
          <li v-for="page in pages" :key="page.id">
            <button
              type="button"
              class="w-full text-left rounded-lg px-3 py-2 text-sm truncate"
              :class="page.id === selectedId ? 'bg-white/15 text-white' : 'text-slate-300 hover:bg-white/5'"
              @click="openPage(page.id)"
            >
              {{ page.title || '未命名' }}
            </button>
          </li>
          <li v-if="!pages.length" class="text-slate-500 text-sm px-1 py-4">
            {{ readOnly ? '还没有笔记。' : '还没有笔记。点「新的一页」开始。' }}
          </li>
        </ul>
      </aside>

      <section class="rounded-2xl border border-white/10 bg-[#101722] p-4 md:p-5 flex flex-col min-h-[28rem]">
        <template v-if="selectedId">
          <div class="flex items-center gap-2 mb-3">
            <input
              v-if="!readOnly"
              v-model="title"
              type="text"
              :maxlength="TITLE_MAX"
              class="flex-1 bg-transparent border-b border-white/15 pb-1 text-white text-lg focus:outline-none focus:border-white/40"
              placeholder="标题"
            />
            <h2 v-else class="flex-1 text-white text-lg font-medium truncate">{{ title || '未命名' }}</h2>
            <span v-if="!readOnly" class="text-[11px] text-slate-500 tabular-nums">{{ title.length }}/{{ TITLE_MAX }}</span>
            <button
              v-if="!readOnly"
              type="button"
              class="text-xs text-slate-400 hover:text-red-300 px-2 py-1"
              @click="deletePage(selectedId)"
            >
              删除
            </button>
          </div>
          <textarea
            v-if="!readOnly"
            v-model="body"
            :maxlength="BODY_MAX"
            :disabled="loadingPage"
            class="flex-1 min-h-[18rem] w-full resize-y rounded-xl border border-white/10 bg-black/25 px-4 py-3 text-sm text-slate-100 leading-relaxed placeholder:text-slate-600 focus:outline-none focus:border-white/25"
            placeholder="在这里写…"
          />
          <div
            v-else
            class="flex-1 min-h-[18rem] w-full rounded-xl border border-white/10 bg-black/25 px-4 py-3 text-sm text-slate-100 leading-relaxed whitespace-pre-wrap"
          >
            <span v-if="loadingPage" class="text-slate-500">加载中…</span>
            <span v-else>{{ body || '（空）' }}</span>
          </div>
          <p v-if="!readOnly" class="text-[11px] text-slate-500 mt-2 text-right tabular-nums">{{ body.length }}/{{ BODY_MAX }}</p>
        </template>
        <div v-else class="flex-1 flex items-center justify-center text-slate-500 text-sm">
          {{ loadingList ? '加载中…' : (readOnly ? '选一页查看。' : '选一页，或新建一页。') }}
        </div>
      </section>
    </div>
  </div>
</template>
