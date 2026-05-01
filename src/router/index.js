import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/:username',
      name: 'Dashboard',
      component: () => import('../views/Dashboard.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/player',
      name: 'Player',
      component: () => import('../views/Player.vue'),
      meta: { requiresAuth: true, role: 'player' }
    },
    {
      path: '/dm',
      name: 'DM',
      component: () => import('../views/DM.vue'),
      meta: { requiresAuth: true, role: 'dm' }
    },
    {
      path: '/player/materials',
      name: 'PlayerMaterials',
      component: () => import('../views/PlayerMaterials.vue'),
      meta: { requiresAuth: true, role: 'player' },
      children: [
        {
          path: 'items',
          name: 'PlayerItems',
          component: () => import('../views/materials/PlayerItems.vue')
        },
        {
          path: 'weapons',
          name: 'PlayerWeapons',
          component: () => import('../views/materials/PlayerWeapons.vue')
        },
        {
          path: 'materials',
          name: 'PlayerBasicMaterials',
          component: () => import('../views/materials/PlayerBasicMaterials.vue')
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const requiresAuth = to.meta.requiresAuth
  const userRole = localStorage.getItem('userRole')
  const username = localStorage.getItem('username')
  
  if (requiresAuth) {
    if (!userRole || !username) {
      next('/')
    } else if (to.params.username) {
      if (to.params.username !== username) {
        next('/')
      } else {
        next()
      }
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
