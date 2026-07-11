<script setup lang="ts">
const difficulties = [
  {
    value: 'EASY',
    label: '简单',
    desc: '4×4 小棋盘',
    sub: '完成 +1 积分',
    icon: '🌱',
    bg: 'linear-gradient(145deg, #43a047 0%, #66bb6a 55%, #81c784 100%)',
    shadow: '0 8px 24px rgba(67, 160, 71, 0.35)'
  },
  {
    value: 'MEDIUM',
    label: '中等',
    desc: '6×6 棋盘',
    sub: '完成 +3 积分',
    icon: '🌊',
    bg: 'linear-gradient(145deg, #1e88e5 0%, #42a5f5 55%, #64b5f6 100%)',
    shadow: '0 8px 24px rgba(30, 136, 229, 0.35)'
  },
  {
    value: 'HARD',
    label: '困难',
    desc: '9×9 标准棋盘',
    sub: '完成 +5 积分',
    icon: '🔥',
    bg: 'linear-gradient(145deg, #ef6c00 0%, #ffa726 55%, #ffb74d 100%)',
    shadow: '0 8px 24px rgba(239, 108, 0, 0.35)'
  }
]

const emit = defineEmits<{
  select: [difficulty: string]
}>()
</script>

<template>
  <div class="difficulty-grid">
    <button
      v-for="d in difficulties"
      :key="d.value"
      type="button"
      class="difficulty-card"
      :style="{ background: d.bg, boxShadow: d.shadow }"
      @click="emit('select', d.value)"
    >
      <span class="card-icon">{{ d.icon }}</span>
      <span class="card-label">{{ d.label }}</span>
      <span class="card-desc">{{ d.desc }}</span>
      <span class="card-sub">{{ d.sub }}</span>
      <span class="card-hint">点击开始 →</span>
    </button>
  </div>
</template>

<style scoped>
.difficulty-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.difficulty-card {
  border: none;
  border-radius: 20px;
  padding: 32px 24px;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #fff;
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.difficulty-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.18) 0%, transparent 50%);
  pointer-events: none;
}

.difficulty-card:hover {
  transform: translateY(-6px) scale(1.02);
  filter: brightness(1.05);
}

.difficulty-card:active {
  transform: translateY(-2px) scale(0.99);
}

.card-icon {
  font-size: 40px;
  line-height: 1;
  margin-bottom: 4px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.15));
}

.card-label {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.card-desc {
  font-size: 15px;
  font-weight: 600;
  opacity: 0.95;
}

.card-sub {
  font-size: 13px;
  opacity: 0.85;
}

.card-hint {
  margin-top: 12px;
  font-size: 12px;
  opacity: 0.75;
  letter-spacing: 1px;
}

@media (max-width: 768px) {
  .difficulty-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .difficulty-card {
    min-height: 160px;
    padding: 24px 20px;
  }
}
</style>
