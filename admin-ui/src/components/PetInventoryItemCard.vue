<script setup lang="ts">
withDefaults(
  defineProps<{
    icon: string
    title: string
    quantity?: number
    useAllQuantity?: number
    singleActionLabel?: string
    singleLoading?: boolean
    useAllLoading?: boolean
  }>(),
  {
    quantity: undefined,
    useAllQuantity: undefined,
    singleActionLabel: '使用一个',
    singleLoading: false,
    useAllLoading: false
  }
)

defineEmits<{
  useOne: []
  useAll: []
}>()
</script>

<template>
  <article class="inventory-item-card">
    <div class="inventory-item-icon">{{ icon }}</div>
    <div class="inventory-item-copy">
      <h3>{{ title }}</h3>
      <p>{{ quantity === undefined ? '永久权益' : `剩余 ${quantity}` }}</p>
    </div>
    <div class="inventory-item-actions">
      <el-button
        type="success"
        :loading="singleLoading"
        @click="$emit('useOne')"
      >
        {{ singleActionLabel }}
      </el-button>
      <el-button
        v-if="useAllQuantity !== undefined"
        type="primary"
        plain
        :loading="useAllLoading"
        @click="$emit('useAll')"
      >
        一键使用 {{ useAllQuantity }} 个
      </el-button>
    </div>
  </article>
</template>

<style scoped>
.inventory-item-card {
  box-sizing: border-box;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  grid-template-rows: minmax(52px, 1fr) auto;
  align-items: center;
  gap: 12px;
  border: 1px solid #eef0f4;
  border-radius: 14px;
  padding: 16px;
  background: #fff;
  height: 100%;
}

.inventory-item-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: #fff4df;
  font-size: 24px;
}

.inventory-item-copy h3 {
  margin: 0;
  font-size: 16px;
}

.inventory-item-copy p {
  margin: 4px 0 0;
  color: #747b8a;
  font-size: 13px;
  line-height: 1.5;
}

.inventory-item-actions {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 2px;
}

.inventory-item-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.inventory-item-actions :deep(.el-button) {
  width: 100%;
}

.inventory-item-actions :deep(.el-button:only-child) {
  grid-column: 1 / -1;
}

</style>
