<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  modelValue: { type: [String, Number], default: '' },
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '请选择' },
  disabled: { type: Boolean, default: false },
  tone: { type: String, default: 'desk' },
})

const emit = defineEmits(['update:modelValue'])

const open = ref(false)
const root = ref(null)

const selected = computed(() =>
  props.options.find((o) => String(o.value) === String(props.modelValue)) || null,
)

const display = computed(() => selected.value?.label || props.placeholder)

function toggle() {
  if (props.disabled) return
  open.value = !open.value
}

function pick(value) {
  emit('update:modelValue', value)
  open.value = false
}

function onPointerDown(e) {
  if (!root.value?.contains(e.target)) open.value = false
}

function onKey(e) {
  if (e.key === 'Escape') open.value = false
}

onMounted(() => {
  document.addEventListener('pointerdown', onPointerDown)
  document.addEventListener('keydown', onKey)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', onPointerDown)
  document.removeEventListener('keydown', onKey)
})
</script>

<template>
  <div ref="root" class="ss" :class="[`ss-${tone}`, { 'ss-open': open, 'ss-disabled': disabled }]">
    <button
      type="button"
      class="ss-trigger"
      :disabled="disabled"
      :aria-expanded="open"
      aria-haspopup="listbox"
      @click="toggle"
    >
      <span class="ss-value" :class="{ 'is-placeholder': !selected }">{{ display }}</span>
      <svg class="ss-caret" viewBox="0 0 12 8" aria-hidden="true">
        <path d="M1.2 1.3L6 6.1l4.8-4.8" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
    </button>
    <ul v-if="open" class="ss-menu" role="listbox">
      <li>
        <button
          type="button"
          class="ss-opt"
          :class="{ 'ss-opt-on': modelValue === '' }"
          role="option"
          :aria-selected="modelValue === ''"
          @click="pick('')"
        >
          {{ placeholder }}
        </button>
      </li>
      <li v-if="!options.length" class="ss-empty">暂无选项</li>
      <li v-for="opt in options" :key="String(opt.value)">
        <button
          type="button"
          class="ss-opt"
          :class="{ 'ss-opt-on': String(opt.value) === String(modelValue) }"
          role="option"
          :aria-selected="String(opt.value) === String(modelValue)"
          @click="pick(opt.value)"
        >
          {{ opt.label }}
        </button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.ss {
  position: relative;
}

.ss-trigger {
  display: flex;
  width: 100%;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  min-height: 2.75rem;
  padding: 0.7rem 0.95rem 0.7rem 1rem;
  border-radius: 0.9rem;
  font: inherit;
  font-size: 0.92rem;
  font-weight: 500;
  letter-spacing: 0.01em;
  line-height: 1.4;
  text-align: left;
  cursor: pointer;
}

.ss-value {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ss-caret {
  width: 0.7rem;
  height: 0.45rem;
  flex-shrink: 0;
  opacity: 0.7;
  transition: transform 0.18s ease;
}

.ss-open .ss-caret {
  transform: rotate(180deg);
}

.ss-menu {
  position: absolute;
  z-index: 40;
  top: calc(100% + 0.4rem);
  left: 0;
  right: 0;
  margin: 0;
  padding: 0.4rem;
  list-style: none;
  max-height: 16.5rem;
  overflow: auto;
  border-radius: 1rem;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.38);
}

.ss-opt {
  display: block;
  width: 100%;
  padding: 0.7rem 0.85rem;
  border: 0;
  border-radius: 0.7rem;
  background: transparent;
  font: inherit;
  font-size: 0.9rem;
  font-weight: 500;
  letter-spacing: 0.01em;
  line-height: 1.45;
  text-align: left;
  cursor: pointer;
}

.ss-empty {
  padding: 0.7rem 0.85rem;
  font-size: 0.85rem;
  opacity: 0.55;
}

.ss-disabled .ss-trigger {
  opacity: 0.6;
  cursor: not-allowed;
}

.ss-desk .ss-trigger {
  border: 1px solid rgba(201, 163, 106, 0.32);
  background: #241c16;
  color: #f3ead7;
}

.ss-desk .ss-value.is-placeholder {
  color: rgba(243, 234, 215, 0.42);
  font-weight: 400;
}

.ss-desk.ss-open .ss-trigger,
.ss-desk .ss-trigger:hover:not(:disabled) {
  border-color: #c9a36a;
}

.ss-desk .ss-menu {
  border: 1px solid rgba(201, 163, 106, 0.28);
  background: #2a221c;
}

.ss-desk .ss-opt {
  color: #f6efe2;
}

.ss-desk .ss-opt:hover {
  background: rgba(201, 163, 106, 0.16);
}

.ss-desk .ss-opt-on {
  background: rgba(201, 163, 106, 0.22);
  color: #ffe9c2;
}

.ss-action .ss-trigger {
  border: 1px solid rgb(148 163 184 / 0.45);
  background: #0b1220;
  color: #e2e8f0;
}

.ss-action .ss-value.is-placeholder {
  color: rgb(148 163 184 / 0.7);
  font-weight: 400;
}

.ss-action.ss-open .ss-trigger,
.ss-action .ss-trigger:hover:not(:disabled) {
  border-color: rgb(56 189 248 / 0.7);
}

.ss-action .ss-menu {
  border: 1px solid rgb(148 163 184 / 0.28);
  background: #152033;
}

.ss-action .ss-opt {
  color: #e8eef7;
}

.ss-action .ss-opt:hover {
  background: rgba(56, 189, 248, 0.12);
}

.ss-action .ss-opt-on {
  background: rgba(56, 189, 248, 0.18);
  color: #bae6fd;
}
</style>
