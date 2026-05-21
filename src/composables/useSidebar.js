import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const STORAGE_KEY = 'sidebar-collapsed'

const collapsed = ref(localStorage.getItem(STORAGE_KEY) === 'true')
const mobileOpen = ref(false)

watch(collapsed, (val) => {
  localStorage.setItem(STORAGE_KEY, String(val))
})

export function useSidebar() {
  const isMobile = ref(false)

  const checkMobile = () => {
    isMobile.value = window.innerWidth < 768
    if (isMobile.value) {
      mobileOpen.value = false
    }
  }

  onMounted(() => {
    checkMobile()
    window.addEventListener('resize', checkMobile)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', checkMobile)
  })

  const toggle = () => {
    if (isMobile.value) {
      mobileOpen.value = !mobileOpen.value
    } else {
      collapsed.value = !collapsed.value
    }
  }

  const closeMobile = () => {
    mobileOpen.value = false
  }

  const sidebarVisible = computed(() => {
    if (isMobile.value) return mobileOpen.value
    return true
  })

  return { collapsed, mobileOpen, isMobile, toggle, closeMobile, sidebarVisible }
}
