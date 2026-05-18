<template>
  <CatastrophePanel :is-dm="isDm" :embedded="false" />
</template>

<script setup>
import { computed } from 'vue'
import CatastrophePanel from '../components/CatastrophePanel.vue'

const isDm = computed(() => (localStorage.getItem('userRole') || '').toLowerCase() === 'dm')
</script>
