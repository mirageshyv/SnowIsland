import { ref, readonly } from 'vue'
import bgmUrl from '@/bgm/winters_last_breath.mp3?url'

const isPlaying = ref(false)
const volume = ref(40)
let audio = null
let inited = false

function ensureAudio() {
  if (audio) return
  audio = new Audio(bgmUrl)
  audio.loop = true
  audio.volume = volume.value / 100
  audio.addEventListener('error', (e) => console.error('Audio error:', e))
}

export function useMusicStore() {
  function play() {
    ensureAudio()
    audio.play().then(() => {
      isPlaying.value = true
    }).catch(() => {
      isPlaying.value = false
    })
  }

  function pause() {
    if (audio) {
      audio.pause()
      isPlaying.value = false
    }
  }

  function toggle() {
    if (isPlaying.value) {
      pause()
    } else {
      play()
    }
  }

  function setVolume(v) {
    volume.value = v
    if (audio) {
      audio.volume = v / 100
    }
  }

  function tryAutoPlay() {
    if (inited) return
    inited = true
    ensureAudio()
    audio.play().then(() => {
      isPlaying.value = true
    }).catch(() => {
      isPlaying.value = false
    })
  }

  return {
    isPlaying: readonly(isPlaying),
    volume: readonly(volume),
    play,
    pause,
    toggle,
    setVolume,
    tryAutoPlay
  }
}
