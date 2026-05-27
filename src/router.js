import { createRouter, createWebHistory } from 'vue-router';
import LoginPage from './views/LoginPage.vue';
import MainPage from './views/MainPage.vue';
import axios from 'axios';

const router = createRouter({
  history: createWebHistory('/'),
  routes: [
    {
      path: '/',
      name: 'Login',
      component: LoginPage
    },
    {
      path: '/main',
      name: 'Main',
      component: MainPage,
      meta: { requiresAuth: true }
    }
  ]
});

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth) {
    axios.get('/api/auth/check', { withCredentials: true })
      .then(() => next())
      .catch(() => next('/'));
  } else {
    next();
  }
});

export default router;

