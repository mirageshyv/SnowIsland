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
      path: '/ark',
      name: 'Ark',
      component: () => import('../views/ArkProgressView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/action-submit',
      name: 'ActionSubmit',
      component: () => import('../views/ActionSubmitView.vue'),
      meta: { requiresAuth: true, role: 'player' }
    },
    {
      path: '/rebel-milestones',
      name: 'RebelMilestones',
      component: () => import('../views/RebelMilestoneView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/catastrophe',
      name: 'Catastrophe',
      component: () => import('../views/CatastropheView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/warehouse',
      name: 'Warehouse',
      component: () => import('../views/WarehouseView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/action-feedback',
      name: 'ActionFeedback',
      component: () => import('../views/ActionFeedbackView.vue'),
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

router.beforeEach((to, from, next) => {
  const requiresAuth = to.meta.requiresAuth
  const userRole = (localStorage.getItem('userRole') || '').toLowerCase()
  const username = localStorage.getItem('username')
  const requiredRole = to.meta.role
  
  if (requiresAuth) {
    if (!userRole || !username) {
      next('/')
      return
    }
    
    if (to.params.username) {
      if (to.params.username !== username) {
        next('/')
        return
      }
    }
    
    if (requiredRole) {
      if (userRole === 'dm') {
        next()
        return
      }
      
      if (userRole !== requiredRole) {
        next('/')
        return
      }
    }
    
    next()
  } else {
    next()
  }
})

export default router
