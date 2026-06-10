<template>
  <div class="wrap">
    <header class="header">
      <div class="header__top">
        <div class="header__left">
          <h1>{{ $m('app.title') }}</h1>
          <span class="badge">{{ $m('app.variant') }}</span>
        </div>
        <button @click="handleLogout" class="check-btn" style="width: auto;">
          {{ $m('app.logout') }}
        </button>
      </div>
      <p class="subtitle">{{ $m('app.subtitle') }}</p>
    </header>

    <main class="content">
      <section class="graph card" :aria-label="$m('app.graph.label')">
        <div class="graph__frame">
          <svg width="500" height="500" viewBox="0 0 500 500" xmlns="http://www.w3.org/2000/svg"
               role="img" :aria-label="$m('app.graph.coords')"
               id="svg" @click="handleSvgClick" @mousemove="handleSvgMouseMove">
            <defs>
              <linearGradient id="gradArea" x1="0" y1="0" x2="1" y2="1">
                <stop offset="0%" stop-color="#60a5fa"></stop>
                <stop offset="100%" stop-color="#22d3ee"></stop>
              </linearGradient>
              <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="currentColor"></polygon>
              </marker>
            </defs>

            <rect :x="250" :y="250 - rScaled" :width="rScaled" :height="rScaled"
                  fill="url(#gradArea)" fill-opacity="0.4"
                  stroke="currentColor" stroke-width="2" />

            <path :d="`M 250 250 L ${250 - rScaled} 250 A ${rScaled} ${rScaled} 0 0 0 250 ${250 + rScaled} Z`"
                  fill="url(#gradArea)"
                  fill-opacity="0.4"
                  stroke="currentColor"
                  stroke-width="2" />

            <polygon :points="`250,250 250,${250 - rScaled/2} ${250 - rScaled/2},250`"
                     fill="url(#gradArea)" fill-opacity="0.4"
                     stroke="currentColor" stroke-width="2" />

            <line x1="50" y1="250" x2="450" y2="250" stroke-width="2"
                  marker-end="url(#arrowhead)"></line>
            <line x1="250" y1="450" x2="250" y2="50" stroke-width="2"
                  marker-end="url(#arrowhead)"></line>

            <text x="460" y="255" font-size="16">x</text>
            <text x="255" y="40" font-size="16">y</text>

            <text :x="250 + rScaled - 5" :y="270" font-size="14">R</text>
            <text x="260" :y="250 - rScaled - 5" font-size="14">R</text>
            <text :x="250 - rScaled - 15" :y="270" font-size="14">-R</text>
            <text x="260" :y="250 + rScaled + 15" font-size="14">-R</text>

            <text :x="250 + rScaled/2 - 10" :y="270" font-size="14">R/2</text>
            <text x="260" :y="250 - rScaled/2 - 5" font-size="14">R/2</text>
            <text :x="250 - rScaled/2 - 20" :y="270" font-size="14">-R/2</text>
            <text x="260" :y="250 + rScaled/2 + 15" font-size="14">-R/2</text>

            <line :x1="250 - rScaled" y1="245" :x2="250 - rScaled" y2="255" stroke-width="2"></line>
            <line :x1="250 - rScaled/2" y1="245" :x2="250 - rScaled/2" y2="255" stroke-width="2"></line>
            <line :x1="250 + rScaled/2" y1="245" :x2="250 + rScaled/2" y2="255" stroke-width="2"></line>
            <line :x1="250 + rScaled" y1="245" :x2="250 + rScaled" y2="255" stroke-width="2"></line>

            <line x1="245" :y1="250 - rScaled" x2="255" :y2="250 - rScaled" stroke-width="2"></line>
            <line x1="245" :y1="250 - rScaled/2" x2="255" :y2="250 - rScaled/2" stroke-width="2"></line>
            <line x1="245" :y1="250 + rScaled/2" x2="255" :y2="250 + rScaled/2" stroke-width="2"></line>
            <line x1="245" :y1="250 + rScaled" x2="255" :y2="250 + rScaled" stroke-width="2"></line>

            <circle id="pointer" r="3" :cx="pointerX" :cy="pointerY"
                    fill="red"
                    :visibility="pointerVisible ? 'visible' : 'hidden'"></circle>

            <circle v-for="(point, idx) in displayedPoints" :key="idx"
                    :cx="getPointX(point)" :cy="getPointY(point)"
                    r="4" :class="point.hit ? 'pointer__success' : 'pointer__failure'"></circle>
          </svg>
        </div>

        <div class="stats-container" style="margin-top: 15px; display: flex; gap: 20px; justify-content: center;">
          <span class="badge" style="background: var(--success); color: white;">
            {{ $m('app.stats.hits') }} {{ stats.hits }}
          </span>
          <span class="badge" style="background: var(--danger); color: white;">
            {{ $m('app.stats.misses') }} {{ stats.misses }}
          </span>
        </div>
      </section>

      <aside class="controls card" aria-label="Форма ввода">
        <div class="section-title">{{ $m('app.params') }}</div>

        <div class="field">
          <div class="label"><strong>X</strong></div>
          <div class="select-wrapper">
            <select v-model="x" class="custom-select">
              <option :value="null" disabled>{{ $m('app.select.x') }}</option>
              <option v-for="val in xValues" :key="val" :value="val">
                {{ val }}
              </option>
            </select>
          </div>
        </div>

        <div class="field">
          <div class="label"><strong>Y</strong></div>
          <div class="input-wrap">
            <input
                id="y-input"
                v-model.number="y"
                type="text"
                class="y-input custom-input"
                :placeholder="$m('app.y.placeholder')"
                @input="validateY"
            />
            <div v-if="yError" class="error-message">{{ yError }}</div>
          </div>
        </div>

        <div class="field">
          <div class="label"><strong>R</strong></div>
          <div class="select-wrapper">
            <select v-model="r" class="custom-select">
              <option :value="null" disabled>{{ $m('app.select.r') }}</option>
              <option v-for="val in rValues" :key="val" :value="val">
                {{ val }}
              </option>
            </select>
          </div>
        </div>

        <div class="actions">
          <button @click="checkPoint" class="check-btn" :disabled="loading">
            {{ loading ? $m('app.check.loading') : $m('app.check.button') }}
          </button>
        </div>
      </aside>
    </main>

    <section class="results card" aria-label="Результаты">
      <table class="result-table">
        <thead>
        <tr>
          <th>X</th>
          <th>Y</th>
          <th>R</th>
          <th>{{ $m('app.result') }}</th>
          <th>{{ $m('app.time') }}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="point in paginatedPoints" :key="point.id">
          <td>{{ point.x }}</td>
          <td>{{ point.y }}</td>
          <td>{{ point.r }}</td>
          <td>
              <span :class="point.hit ? 'tag-yes' : 'tag-no'">
                {{ point.hit ? $m('app.hit') : $m('app.miss') }}
              </span>
          </td>
          <td>{{ point.formattedTime }}</td>
        </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button @click="previousPage" class="pagination-btn" :disabled="currentPage === 0">
          {{ $m('app.page.prev') }}
        </button>
        <span>{{ $m('app.page.label') }} {{ currentPage + 1 }} {{ $m('app.page.of') }} {{ totalPages || 1 }}</span>
        <button @click="nextPage" class="pagination-btn" :disabled="isLastPage">
          {{ $m('app.page.next') }}
        </button>
        <button @click="clearPoints" class="pagination-btn" style="margin-left: auto;">
          {{ $m('app.page.clear') }}
        </button>
      </div>
    </section>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'MainPage',
  data() {
    return {
      x: null,
      y: null,
      r: null,
      xValues: [-4, -3, -2, -1, 0, 1, 2, 3, 4],
      rValues: [1, 2, 3, 4],
      points: [],
      currentPage: 0,
      pageSize: 10,
      totalPages: 0,
      loading: false,
      pointerX: 250,
      pointerY: 250,
      pointerVisible: false,
      yError: '',
      stats: { hits: 0, misses: 0 }
    };
  },
  computed: {
    rScaled() {
      if (!this.r) return 100;
      return Math.abs(this.r) * 50;
    },
    displayedPoints() {
      if (!this.r) return [];
      return this.points.filter(p => Math.abs(parseFloat(p.r) - parseFloat(this.r)) < 0.001);
    },
    paginatedPoints() {
      const start = this.currentPage * this.pageSize;
      const end = start + this.pageSize;
      return this.points.slice(start, end);
    },
    isLastPage() {
      return (this.currentPage + 1) * this.pageSize >= this.points.length;
    }
  },
  watch: {
    r() {
      this.pointerVisible = false;
    },
    y() {
      this.validateY();
    },
    points() {
      this.totalPages = Math.ceil(this.points.length / this.pageSize);
    }
  },
  mounted() {
    this.loadPoints();
  },
  methods: {
    validateY() {
      const val = parseFloat(this.y);
      if (this.y === '' || this.y === null) {
        this.yError = '';
        return false;
      }
      if (isNaN(val)) {
        this.yError = this.$m('app.y.error.number');
        return false;
      }
      if (val < -5 || val > 5) {
        this.yError = this.$m('app.y.error.range');
        return false;
      }
      this.yError = '';
      return true;
    },

    async fetchStats() {
      try {
        const response = await axios.get('/api/points/stats', { withCredentials: true });
        this.stats = {
          hits: response.data.hits || 0,
          misses: response.data.misses || 0
        };
      } catch (error) {
        console.error(this.$m('app.error.stats'), error);
      }
    },

    async loadPoints() {
      try {
        const response = await axios.get('/api/points/all', { withCredentials: true });
        this.points = response.data;
        await this.fetchStats();
      } catch (error) {
        if (error.response && error.response.status === 401) {
          this.$router.push('/');
        }
      }
    },

    async checkPoint() {
      if (this.x === null) {
        alert(this.$m('app.alert.select.x'));
        return;
      }
      if (this.r === null) {
        alert(this.$m('app.alert.select.r'));
        return;
      }
      if (this.y === null || this.y === '') {
        alert(this.$m('app.alert.enter.y'));
        return;
      }

      if (!this.validateY()) {
        alert(this.yError || this.$m('app.alert.invalid.y'));
        return;
      }

      this.loading = true;
      try {
        const response = await axios.post('/api/points', {
          x: this.x,
          y: parseFloat(this.y),
          r: this.r
        }, { withCredentials: true });

        this.points.unshift(response.data);
        this.currentPage = 0;
        await this.fetchStats();
      } catch (error) {
        let errorMsg = this.$m('app.error.check');
        if (error.response && error.response.data) {
          if (typeof error.response.data === 'object') {
            errorMsg = error.response.data.message || error.response.data.error || JSON.stringify(error.response.data);
          } else {
            errorMsg = error.response.data;
          }
        }
        alert(errorMsg);
      } finally {
        this.loading = false;
      }
    },
    handleSvgMouseMove(event) {
      if (!this.r) return;

      const svg = document.getElementById('svg');
      const rect = svg.getBoundingClientRect();
      const svgX = event.clientX - rect.left;
      const svgY = event.clientY - rect.top;

      const svgCenterX = 250;
      const svgCenterY = 250;

      const mathX = (svgX - 250) / 50;
      let mathY = (250 - svgY) / 50;

      mathY = Math.max(-5, Math.min(5, mathY));

      let snappedX = this.xValues[0];
      let bestDist = Math.abs(this.xValues[0] - mathX);
      for (let i = 1; i < this.xValues.length; i++) {
        const dist = Math.abs(this.xValues[i] - mathX);
        if (dist < bestDist) {
          bestDist = dist;
          snappedX = this.xValues[i];
        }
      }

      this.pointerX = svgCenterX + snappedX * 50;
      this.pointerY = svgCenterY - mathY * 50;

      this.pointerVisible = true;
    },
    async handleSvgClick(event) {
      if (!this.r) {
        alert(this.$m('app.alert.select.r.first'));
        return;
      }

      const svg = document.getElementById('svg');
      const rect = svg.getBoundingClientRect();
      const svgX = event.clientX - rect.left;
      const svgY = event.clientY - rect.top;

      const svgCenterX = 250;
      const svgCenterY = 250;

      const mathXRaw = (svgX - svgCenterX) / 50;
      const mathYRaw = (svgCenterY - svgY) / 50;

      let snappedX = this.xValues[0];
      let bestDist = Math.abs(this.xValues[0] - mathXRaw);
      for (let i = 1; i < this.xValues.length; i++) {
        const dist = Math.abs(this.xValues[i] - mathXRaw);
        if (dist < bestDist) {
          bestDist = dist;
          snappedX = this.xValues[i];
        }
      }

      this.x = snappedX;
      this.y = parseFloat(mathYRaw.toFixed(3));

      this.checkPoint();
      await this.fetchStats();
    },
    getPointX(point) {
      return 250 + parseFloat(point.x) * 50;
    },
    getPointY(point) {
      return 250 - parseFloat(point.y) * 50;
    },
    previousPage() {
      if (this.currentPage > 0) this.currentPage--;
    },
    nextPage() {
      if (!this.isLastPage) this.currentPage++;
    },
    async clearPoints() {
      if (!confirm(this.$m('app.confirm.clear'))) return;
      try {
        await axios.delete('/api/points', { withCredentials: true });
        this.points = [];
        this.currentPage = 0;
        this.stats = { hits: 0, misses: 0 };
      } catch (error) {
        alert(this.$m('app.error.clear'));
      }
    },
    async handleLogout() {
      try {
        await axios.post('/api/auth/logout', {}, { withCredentials: true });
        this.$router.push('/');
      } catch (error) {
        this.$router.push('/');
      }
    }
  }
};
</script>

<style scoped>
.custom-select, .custom-input {
  width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid var(--stroke, #ccc);
  background-color: var(--bg, #fff);
  color: var(--text, #333);
  font-size: 16px;
  outline: none;
  transition: border-color 0.2s;
  cursor: pointer;
}

.custom-select:focus, .custom-input:focus {
  border-color: #60a5fa;
  box-shadow: 0 0 0 3px rgba(96, 165, 250, 0.2);
}

.graph__frame {
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  width: 500px;
  height: 500px;
  margin: 0 auto;
}

.error-message {
  color: #ef4444;
  font-size: 12px;
  margin-top: 4px;
  min-height: 16px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
}
</style>
