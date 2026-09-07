<script setup lang="ts">
defineProps<{
  icon: string
  title: string
  description: string
  costPoints: number
  ownedQuantity: number
  exchangeQuantity: number
  maxExchangeQuantity: number
  canExchange: boolean
  loading?: boolean
}>()

const emit = defineEmits<{
  'update:exchangeQuantity': [quantity: number]
  exchange: []
}>()

function updateQuantity(value: number | undefined) {
  emit('update:exchangeQuantity', value ?? 1)
}
</script>

<template>
  <article class="benefit-shop-card">
    <div class="benefit-shop-icon">{{ icon }}</div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <div class="benefit-shop-meta">
      <span>{{ costPoints }} 积分</span>
      <span>已拥有 {{ ownedQuantity }}</span>
    </div>
    <div class="benefit-shop-exchange-row">
      <span>兑换数量</span>
      <el-input-number
        :model-value="exchangeQuantity"
        :min="1"
        :max="maxExchangeQuantity"
        size="small"
        controls-position="right"
        :disabled="!canExchange"
        @update:model-value="updateQuantity"
      />
    </div>
    <div class="benefit-shop-total">
      共 {{ costPoints * exchangeQuantity }} 积分
    </div>
    <el-button
      type="primary"
      :disabled="!canExchange"
      :loading="loading"
      @click="$emit('exchange')"
    >
      兑换 {{ exchangeQuantity }} 个
    </el-button>
  </article>
</template>

<style scoped>
.benefit-shop-card {
  min-height: 258px;
  display: flex;
  flex-direction: column;
  gap: 7px;
  border: 1px solid #eef0f4;
  border-radius: 14px;
  padding: 16px;
  background: #fff;
}

.benefit-shop-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff4df;
  font-size: 28px;
}

.benefit-shop-card h3 {
  margin: 0;
  font-size: 16px;
}

.benefit-shop-card p {
  min-height: 40px;
  margin: 0;
  color: #747b8a;
  font-size: 13px;
  line-height: 1.5;
}

.benefit-shop-meta {
  display: flex;
  justify-content: space-between;
  margin-top: auto;
  color: #8a92a3;
  font-size: 12px;
  font-weight: 700;
}

.benefit-shop-exchange-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  color: #5f6470;
  font-size: 13px;
  font-weight: 700;
}

.benefit-shop-total {
  color: #f08a24;
  font-size: 13px;
  font-weight: 800;
  text-align: right;
}
</style>
