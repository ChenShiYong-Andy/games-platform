<script setup lang="ts">
import type { CSSProperties } from 'vue'

withDefaults(defineProps<{
  title: string
  icon: string
  cardStyle?: CSSProperties
  statistic?: number
  disabled?: boolean
  iconVariant?: 'default' | 'chess'
}>(), {
  cardStyle: () => ({}),
  disabled: false,
  iconVariant: 'default'
})

defineEmits<{ click: [] }>()
</script>

<template>
  <button
    type="button"
    class="game-entry-card"
    :class="{ disabled }"
    :style="cardStyle"
    :disabled="disabled"
    @click="$emit('click')"
  >
    <span v-if="statistic !== undefined" class="today-statistic">
      今日游戏 {{ statistic }} 次
    </span>
    <span class="entry-icon" :class="{ chess: iconVariant === 'chess' }">{{ icon }}</span>
    <span class="entry-title">{{ title }}</span>
  </button>
</template>

<style scoped>
.game-entry-card {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  border: 0;
  border-radius: 20px;
  padding: 42px 16px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  overflow: hidden;
  color: #fff;
  cursor: pointer;
  transition: transform .25s ease, box-shadow .25s ease, filter .25s ease;
}

.game-entry-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(255,255,255,.15), transparent 55%);
  pointer-events: none;
}

.game-entry-card:not(.disabled):hover {
  transform: translateY(-6px) scale(1.02);
  filter: brightness(1.05);
}

.game-entry-card:not(.disabled):active {
  transform: translateY(-2px) scale(.99);
}

.game-entry-card.disabled {
  cursor: not-allowed;
  opacity: .65;
}

.today-statistic {
  position: absolute;
  top: 12px;
  left: 12px;
  right: 12px;
  border-radius: 10px;
  padding: 7px 8px;
  background: rgba(255,255,255,.18);
  color: rgba(255,255,255,.96);
  font-size: 12px;
  font-weight: 800;
  line-height: 1.2;
  text-align: center;
}

.entry-icon {
  position: relative;
  font-size: 48px;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,.12));
}

.entry-icon.chess {
  width: 58px;
  height: 58px;
  display: grid;
  place-items: center;
  border: 3px solid #a9281d;
  border-radius: 50%;
  background: radial-gradient(circle at 36% 28%, #fff2bd 0%, #f5d68d 55%, #d6a653 100%);
  color: #b5261d;
  font-family: STKaiti, KaiTi, serif;
  font-size: 34px;
  font-weight: 800;
  text-shadow: 0 1px 0 rgba(255,255,255,.6);
  box-shadow: inset 0 0 0 3px rgba(255,244,190,.65), 0 4px 9px rgba(65,20,10,.3);
  filter: none;
}

.entry-title {
  position: relative;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 1px;
  text-align: center;
  text-shadow: 0 2px 6px rgba(0,0,0,.12);
}

@media (max-width: 620px) {
  .game-entry-card { border-radius: 16px; }
  .entry-title { font-size: 18px; }
}
</style>
