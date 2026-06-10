import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import { loadMessages, getMessage } from './messages';

async function bootstrap() {
  try {
    await loadMessages();
  } catch (e) {
    console.error(e);
  }

  const app = createApp(App);
  app.config.globalProperties.$m = getMessage;
  app.use(router);
  app.mount('#app');
}

bootstrap();
