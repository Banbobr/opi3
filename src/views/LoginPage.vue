<template>
  <div class="wrap">
    <header class="header">
      <div class="header__top">
        <h1 class="header__top h1">{{ $m('app.title') }}</h1>
        <span class="badge">{{ $m('app.variant') }}</span>
      </div>
      <p class="subtitle">{{ $m('app.subtitle') }}</p>
    </header>

    <main class="content login-content">
      <div class="card login-card">
        <div id="clock" class="clock"></div>
        <div v-if="error" class="error-message">{{ error }}</div>
        <form @submit.prevent="handleLogin" class="login-form">
          <div class="field">
            <label for="username">{{ $m('app.username.label') }}</label>
            <input
              id="username"
              v-model="username"
              type="text"
              required
              class="input"
              :placeholder="$m('app.username.placeholder')"
            />
          </div>
          <div class="field">
            <label for="password">{{ $m('app.password.label') }}</label>
            <input
              id="password"
              v-model="password"
              type="password"
              required
              class="input"
              :placeholder="$m('app.password.placeholder')"
            />
          </div>
          <div class="actions">
            <button type="submit" class="check-btn" :disabled="loading">
              {{ loading ? $m('app.login.loading') : $m('app.login.button') }}
            </button>
            <button type="button" @click="handleRegister" class="check-btn secondary" :disabled="loading">
              {{ loading ? $m('app.register.loading') : $m('app.register.button') }}
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
      loading: false,
      clockTimer: null
    };
  },
  mounted() {
    this.updateClock();
    this.clockTimer = setInterval(this.updateClock, 11000);
  },
  beforeUnmount() {
    clearInterval(this.clockTimer);
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
          this.error = response.data.message || this.$m('app.error.login');
        }
      } catch (error) {
        this.error = error.response?.data?.message || this.$m('app.error.connection');
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
          this.error = this.$m('app.register.success');
        } else {
          this.error = response.data.message || this.$m('app.error.register');
        }
      } catch (error) {
        this.error = error.response?.data?.message || this.$m('app.error.connection');
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>
