<template>
  <div class="wrap">
    <header class="header">
      <div class="header__top">
        <h1 class="header__top h1">Лабораторная работа №4</h1>
        <span class="badge">Вариант 478265</span>
      </div>
      <p class="subtitle">Полищенко Николай Николаевич · P3212</p>
    </header>

    <main class="content login-content">
      <div class="card login-card">
        <div id="clock" class="clock"></div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="field">
            <label for="username">Логин:</label>
            <input 
              id="username" 
              v-model="username" 
              type="text" 
              required 
              class="input"
              placeholder="Введите логин"
            />
          </div>
          <div class="field">
            <label for="password">Пароль:</label>
            <input 
              id="password" 
              v-model="password" 
              type="password" 
              required 
              class="input"
              placeholder="Введите пароль"
            />
          </div>
          <div class="actions">
            <button type="submit" class="check-btn" :disabled="loading">
              {{ loading ? 'Вход...' : 'Войти' }}
            </button>
            <button type="button" @click="handleRegister" class="check-btn secondary" :disabled="loading">
              {{ loading ? 'Регистрация...' : 'Зарегистрироваться' }}
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'LoginPage',
  data() {
    return {
      username: '',
      password: '',
      error: '',
      loading: false
    };
  },
  mounted() {
    this.updateClock();
    setInterval(this.updateClock, 11000);
  },
  methods: {
    updateClock() {
      const clockElement = document.getElementById('clock');
      if (clockElement) {
        const now = new Date();
        const formatted = now.toLocaleTimeString('ru-RU', {
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        });
        clockElement.textContent = formatted;
      }
    },
    async handleLogin() {
      this.error = '';
      this.loading = true;
      try {
        const response = await axios.post('/api/auth/login', {
          username: this.username,
          password: this.password
        }, { withCredentials: true });
        
        if (response.data.success) {
          this.$router.push('/main');
        } else {
          this.error = response.data.message || 'Ошибка входа';
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Ошибка подключения к серверу';
      } finally {
        this.loading = false;
      }
    },
    async handleRegister() {
      this.error = '';
      this.loading = true;
      try {
        const response = await axios.post('/api/auth/register', {
          username: this.username,
          password: this.password
        }, { withCredentials: true });
        
        if (response.data.success) {
          this.error = 'Регистрация успешна! Теперь войдите.';
        } else {
          this.error = response.data.message || 'Ошибка регистрации';
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Ошибка подключения к серверу';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>


