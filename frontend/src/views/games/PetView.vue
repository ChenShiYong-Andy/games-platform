<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getData, postData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type {
  PetBenefitItem,
  PetColorOption,
  PetExchangeResponse,
  PetGrowthStage,
  PetHomeResponse,
  PetInfo,
  PetInitOptionsResponse,
  PetProfileResponse,
  PetTypeOption,
  PetUseBenefitResponse,
  PetUserBenefit
} from '@/types'

const authStore = useAuthStore()
const loading = ref(false)
const adopting = ref(false)
const exchangingId = ref<number | null>(null)
const usingId = ref<number | null>(null)
const hasPet = ref(false)
const availablePoints = ref(0)
const petInfo = ref<PetInfo | null>(null)
const benefits = ref<PetBenefitItem[]>([])
const myBenefits = ref<PetUserBenefit[]>([])
const petTypes = ref<PetTypeOption[]>([])
const selectedType = ref('')
const petName = ref('')
const activeTab = ref<'shop' | 'bag'>('shop')

const realAnimalGifs: Record<string, string[]> = {
  CAT: [
    '/pet-gifs/real/cat_stage_1.gif',
    '/pet-gifs/real/cat_stage_2.gif',
    '/pet-gifs/real/cat_stage_3.gif',
    '/pet-gifs/real/cat_stage_4.gif',
    '/pet-gifs/real/cat_stage_5.gif'
  ],
  DOG: [
    '/pet-gifs/real/dog_stage_1.gif',
    '/pet-gifs/real/dog_stage_2.gif',
    '/pet-gifs/real/dog_stage_3.gif',
    '/pet-gifs/real/dog_stage_4.gif',
    '/pet-gifs/real/dog_stage_5.gif'
  ],
  RABBIT: [
    '/pet-gifs/real/rabbit_stage_1.gif',
    '/pet-gifs/real/rabbit_stage_2.gif',
    '/pet-gifs/real/rabbit_stage_3.gif',
    '/pet-gifs/real/rabbit_stage_4.gif',
    '/pet-gifs/real/rabbit_stage_5.gif'
  ],
  DINOSAUR: [
    '/pet-gifs/real/dinosaur_stage_1.gif',
    '/pet-gifs/real/dinosaur_stage_2.gif',
    '/pet-gifs/real/dinosaur_stage_3.gif',
    '/pet-gifs/real/dinosaur_stage_4.gif',
    '/pet-gifs/real/dinosaur_stage_5.gif'
  ],
  ANGELWOMON: [
    '/pet-gifs/real/angelwomon_stage_1.gif',
    '/pet-gifs/real/angelwomon_stage_2.gif',
    '/pet-gifs/real/angelwomon_stage_3.gif',
    '/pet-gifs/real/angelwomon_stage_4.gif',
    '/pet-gifs/real/angelwomon_stage_5.gif'
  ],
  ANGEMON: [
    '/pet-gifs/real/angemon_stage_1.gif',
    '/pet-gifs/real/angemon_stage_2.gif',
    '/pet-gifs/real/angemon_stage_3.gif',
    '/pet-gifs/real/angemon_stage_4.gif',
    '/pet-gifs/real/angemon_stage_5.gif'
  ]
}

const selectedTypeOption = computed(
  () =>
    petTypes.value.find((item) => item.petType === selectedType.value) || null
)
const selectedColorOption = computed<PetColorOption | null>(() => {
  return (
    selectedTypeOption.value?.colors.find(
      (item) => item.colorCode === selectedTypeOption.value?.defaultColorCode
    ) ||
    selectedTypeOption.value?.colors[0] ||
    null
  )
})
const stagePreviewList = computed(
  () => selectedColorOption.value?.stagePreviewList || []
)
const ownedStagePreviewList = computed<PetGrowthStage[]>(() => {
  if (!petInfo.value) return []
  const type = petTypes.value.find(
    (item) => item.petType === petInfo.value?.petType
  )
  const color = type?.colors.find(
    (item) => item.colorCode === petInfo.value?.petColorCode
  )
  return color?.stagePreviewList || []
})
const consumableBenefits = computed(() =>
  benefits.value.filter((item) => item.benefitType === 'CONSUMABLE')
)
const permanentBenefits = computed(() =>
  benefits.value.filter((item) => item.benefitType === 'PERMANENT')
)
const usableBenefits = computed(() =>
  myBenefits.value.filter((item) => item.status === 1 && item.quantity > 0)
)

const petMood = computed(() => {
  if (!petInfo.value) return '正在等你'
  const average =
    (petInfo.value.hunger +
      petInfo.value.clean +
      petInfo.value.happiness +
      petInfo.value.energy) /
    4
  if (average >= 90) return '闪闪发光'
  if (average >= 75) return '心情不错'
  if (average >= 55) return '需要照顾'
  return '有点低落'
})

const nextStageText = computed(() => {
  if (!petInfo.value) return ''
  if (!petInfo.value.nextStage) return '已经成长为最终形态啦'
  return `距离${petInfo.value.nextStage.stageName}还差 ${petInfo.value.nextStage.remainLevel} 级`
})

const currentColorHex = computed(() => {
  return petColorHex(petInfo.value?.petType, petInfo.value?.petColorCode)
})

function petSymbol(type?: string) {
  if (type === 'DOG') return '🐶'
  if (type === 'RABBIT') return '🐰'
  if (type === 'DINOSAUR') return '🦕'
  if (type === 'ANGELWOMON') return '🪽'
  if (type === 'ANGEMON') return '👼'
  return '🐱'
}

function petColorHex(type?: string, colorCode?: string) {
  const petType = petTypes.value.find((item) => item.petType === type)
  return (
    petType?.colors.find((item) => item.colorCode === colorCode)?.colorHex ||
    '#F6A23A'
  )
}

function stageVisualStyle(stageNo: number, colorHex?: string) {
  return {
    '--pet-color': colorHex || '#F6A23A',
    '--pet-scale': String(0.72 + stageNo * 0.08)
  }
}

function petGifSrc(
  assetKey?: string | null,
  petType?: string,
  stageNo?: number
) {
  const type = petType || assetKey?.split('_')[0]?.toUpperCase()
  const stage = stageNo || Number(assetKey?.match(/stage_(\d+)/)?.[1] || 1)
  const gifs = type ? realAnimalGifs[type] : null
  return gifs?.[Math.max(0, Math.min(4, stage - 1))] || ''
}

function benefitIcon(code: string) {
  if (code.includes('MEAL')) return '🍱'
  if (code.includes('CLEAN')) return '🛁'
  if (code.includes('HAPPY')) return '🧸'
  if (code.includes('EXP')) return '🍎'
  if (code.includes('HAT')) return '🎩'
  if (code.includes('BED')) return '🛏'
  if (code.includes('ROOM')) return '🌲'
  return '🎁'
}

function statusColor(value: number) {
  if (value >= 80) return '#35b779'
  if (value >= 50) return '#f0a020'
  return '#e65353'
}

async function loadPage() {
  loading.value = true
  try {
    const profile = await getData<PetProfileResponse>('/pet/profile')
    hasPet.value = profile.hasPet
    petInfo.value = profile.petInfo
    await loadInitOptions()
    if (profile.hasPet) {
      await loadPetHome()
    }
  } finally {
    loading.value = false
  }
}

async function loadInitOptions() {
  const data = await getData<PetInitOptionsResponse>('/pet/init/options')
  petTypes.value = data.petTypes
  if (petTypes.value.length > 0 && !selectedType.value) {
    selectType(petTypes.value[0])
  }
}

async function loadPetHome() {
  const data = await getData<PetHomeResponse>('/pet/home')
  applyHome(data)
}

function applyHome(data: PetHomeResponse) {
  availablePoints.value = data.availablePoints
  petInfo.value = data.petInfo
  benefits.value = data.benefits.list
  myBenefits.value = data.myBenefits.list
  hasPet.value = true
}

function selectType(type: PetTypeOption) {
  selectedType.value = type.petType
}

async function adoptPet() {
  if (!selectedType.value) {
    ElMessage.info('请选择宠物')
    return
  }
  adopting.value = true
  try {
    const pet = await postData<PetInfo>('/pet/init/select', {
      petType: selectedType.value,
      petName: petName.value
    })
    ElMessage.success(`领养成功，${pet.petName} 来啦`)
    hasPet.value = true
    petInfo.value = pet
    await loadPetHome()
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '领养失败')
  } finally {
    adopting.value = false
  }
}

async function refreshAfterChange(pet?: PetInfo, points?: number) {
  if (pet) petInfo.value = pet
  if (typeof points === 'number') availablePoints.value = points
  await authStore.refreshProfile().catch(() => undefined)
  await loadPetHome()
}

async function exchangeBenefit(item: PetBenefitItem) {
  if (!item.canExchange) {
    ElMessage.info(
      item.owned ? '你已经拥有这个权益' : '当前积分不足或暂不可兑换'
    )
    return
  }

  if (item.costPoints >= 100) {
    await ElMessageBox.confirm(
      `将消耗 ${item.costPoints} 积分兑换「${item.benefitName}」`,
      '确认兑换',
      {
        confirmButtonText: '兑换',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  }

  exchangingId.value = item.benefitId
  try {
    const result = await postData<PetExchangeResponse>(
      '/pet/benefit/exchange',
      { benefitId: item.benefitId }
    )
    ElMessage.success(`已兑换 ${result.benefitName}`)
    await refreshAfterChange(undefined, result.availablePoints)
    activeTab.value = 'bag'
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '兑换失败')
  } finally {
    exchangingId.value = null
  }
}

async function useBenefit(item: PetUserBenefit) {
  usingId.value = item.userBenefitId
  try {
    const result = await postData<PetUseBenefitResponse>('/pet/benefit/use', {
      userBenefitId: item.userBenefitId
    })
    ElMessage.success(`已使用 ${result.benefitName}`)
    await refreshAfterChange(result.petInfo)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '使用失败')
  } finally {
    usingId.value = null
  }
}

onMounted(() => {
  void loadPage()
})
</script>

<template>
  <div class="pet-page" v-loading="loading">
    <section v-if="!hasPet" class="adopt-page">
      <div class="adopt-header">
        <p class="eyebrow">首次领养</p>
        <h1>选择你的宠物朋友</h1>
      </div>

      <div class="adopt-grid">
        <article
          v-for="type in petTypes"
          :key="type.petType"
          class="type-card"
          :class="{ active: selectedType === type.petType }"
          @click="selectType(type)"
        >
          <span class="type-symbol">{{ petSymbol(type.petType) }}</span>
          <h2>{{ type.petTypeName }}</h2>
          <p>{{ type.description }}</p>
        </article>
      </div>

      <div class="adopt-panel">
        <div>
          <h2>宠物名称</h2>
          <el-input
            v-model="petName"
            maxlength="20"
            placeholder="例如：豆豆（可不填）"
          />
        </div>
      </div>

      <div class="preview-section">
        <h2>成长预览</h2>
        <div class="stage-grid">
          <article
            v-for="stage in stagePreviewList"
            :key="stage.stageNo"
            class="stage-card"
          >
            <div
              class="stage-figure"
              :style="
                stageVisualStyle(stage.stageNo, selectedColorOption?.colorHex)
              "
            >
              <img
                class="pet-gif preview-gif"
                :src="petGifSrc(stage.assetKey, selectedType, stage.stageNo)"
                :alt="stage.stageName"
              />
            </div>
            <strong>Lv.{{ stage.minLevel }}-{{ stage.maxLevel }}</strong>
            <h3>{{ stage.stageName }}</h3>
            <p>{{ stage.description }}</p>
          </article>
        </div>
      </div>

      <div class="adopt-actions">
        <el-button
          type="primary"
          size="large"
          :loading="adopting"
          @click="adoptPet"
          >开始领养</el-button
        >
      </div>
    </section>

    <template v-else>
      <section
        class="pet-stage"
        :class="{ forest: petInfo?.currentRoomTheme === 'PET_ROOM_FOREST' }"
      >
        <div class="pet-summary">
          <p class="eyebrow">
            {{ petInfo?.petTypeName }} · {{ petInfo?.petColorName }}
          </p>
          <h1>{{ petInfo?.petName }}</h1>
          <p class="mood">
            Lv.{{ petInfo?.level }} {{ petInfo?.stageName }} · {{ petMood }}
          </p>
          <p class="next-stage">{{ nextStageText }}</p>
          <div class="points-pill">
            <span>可用积分</span>
            <strong>{{ availablePoints }}</strong>
          </div>
        </div>

        <div class="pet-avatar" :class="{ hat: petInfo?.currentHatCode }">
          <span class="hat-mark" v-if="petInfo?.currentHatCode">🎩</span>
          <img
            class="pet-gif hero-gif"
            :src="
              petGifSrc(
                petInfo?.petAssetKey,
                petInfo?.petType,
                petInfo?.stageNo
              )
            "
            :alt="petInfo?.petName || '宠物'"
          />
          <span class="bed-mark" v-if="petInfo?.currentBedCode"
            >小床已放置</span
          >
        </div>

        <div class="status-panel">
          <div class="status-row">
            <span>饥饿</span>
            <el-progress
              :percentage="petInfo?.hunger || 0"
              :color="statusColor(petInfo?.hunger || 0)"
            />
          </div>
          <div class="status-row">
            <span>清洁</span>
            <el-progress
              :percentage="petInfo?.clean || 0"
              :color="statusColor(petInfo?.clean || 0)"
            />
          </div>
          <div class="status-row">
            <span>快乐</span>
            <el-progress
              :percentage="petInfo?.happiness || 0"
              :color="statusColor(petInfo?.happiness || 0)"
            />
          </div>
          <div class="status-row">
            <span>体力</span>
            <el-progress
              :percentage="petInfo?.energy || 0"
              :color="statusColor(petInfo?.energy || 0)"
            />
          </div>
          <div class="exp-line">
            <span>成长值</span>
            <strong>{{ petInfo?.exp || 0 }} / 100</strong>
          </div>
        </div>
      </section>

      <section class="room-preview-section">
        <div class="section-heading">
          <div>
            <p class="eyebrow">成长路线</p>
            <h2>{{ petInfo?.petName }}的成长预览</h2>
          </div>
          <span>{{ petInfo?.petTypeName }} · {{ petInfo?.petColorName }}</span>
        </div>
        <div class="stage-grid">
          <article
            v-for="stage in ownedStagePreviewList"
            :key="stage.stageNo"
            class="stage-card"
            :class="{
              current: stage.stageNo === petInfo?.stageNo,
              reached: stage.stageNo < (petInfo?.stageNo || 1)
            }"
          >
            <div
              class="stage-figure"
              :style="stageVisualStyle(stage.stageNo, currentColorHex)"
            >
              <img
                class="pet-gif preview-gif"
                :src="
                  petGifSrc(stage.assetKey, petInfo?.petType, stage.stageNo)
                "
                :alt="stage.stageName"
              />
            </div>
            <strong>Lv.{{ stage.minLevel }}-{{ stage.maxLevel }}</strong>
            <h3>{{ stage.stageName }}</h3>
            <p>{{ stage.description }}</p>
          </article>
        </div>
      </section>

      <section class="pet-workbench">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="权益商店" name="shop">
            <h2>消耗型道具</h2>
            <div class="benefit-grid">
              <article
                v-for="item in consumableBenefits"
                :key="item.benefitId"
                class="benefit-card"
              >
                <div class="benefit-icon">
                  {{ benefitIcon(item.benefitCode) }}
                </div>
                <h3>{{ item.benefitName }}</h3>
                <p>{{ item.description }}</p>
                <div class="benefit-meta">
                  <span>{{ item.costPoints }} 积分</span>
                  <span>已拥有 {{ item.quantity }}</span>
                </div>
                <el-button
                  type="primary"
                  :disabled="!item.canExchange"
                  :loading="exchangingId === item.benefitId"
                  @click="exchangeBenefit(item)"
                >
                  兑换
                </el-button>
              </article>
            </div>

            <h2>永久权益</h2>
            <div class="benefit-grid">
              <article
                v-for="item in permanentBenefits"
                :key="item.benefitId"
                class="benefit-card"
              >
                <div class="benefit-icon">
                  {{ benefitIcon(item.benefitCode) }}
                </div>
                <h3>{{ item.benefitName }}</h3>
                <p>{{ item.description }}</p>
                <div class="benefit-meta">
                  <span>{{ item.costPoints }} 积分</span>
                  <span>{{ item.owned ? '已拥有' : '未拥有' }}</span>
                </div>
                <el-button
                  type="primary"
                  :disabled="!item.canExchange"
                  :loading="exchangingId === item.benefitId"
                  @click="exchangeBenefit(item)"
                >
                  {{ item.owned ? '已拥有' : '兑换' }}
                </el-button>
              </article>
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的背包" name="bag">
            <div v-if="usableBenefits.length > 0" class="bag-grid">
              <article
                v-for="item in usableBenefits"
                :key="item.userBenefitId"
                class="bag-item"
              >
                <div class="benefit-icon small">
                  {{ benefitIcon(item.benefitCode) }}
                </div>
                <div>
                  <h3>{{ item.benefitName }}</h3>
                  <p>
                    {{
                      item.benefitType === 'CONSUMABLE'
                        ? `剩余 ${item.quantity}`
                        : '永久权益'
                    }}
                  </p>
                </div>
                <el-button
                  type="success"
                  :loading="usingId === item.userBenefitId"
                  @click="useBenefit(item)"
                >
                  {{ item.benefitType === 'CONSUMABLE' ? '使用' : '切换' }}
                </el-button>
              </article>
            </div>
            <el-empty v-else description="背包还没有可用权益" />
          </el-tab-pane>
        </el-tabs>
      </section>
    </template>
  </div>
</template>

<style scoped>
.pet-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.adopt-page,
.room-preview-section,
.pet-workbench {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 6px 24px rgba(80, 93, 120, 0.08);
}

.adopt-header {
  margin-bottom: 20px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 16px;
}

.section-heading h2 {
  font-size: 20px;
}

.section-heading > span {
  color: #8a92a3;
  font-size: 13px;
  font-weight: 700;
}

.adopt-header h1 {
  font-size: 28px;
}

.adopt-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.type-card {
  border: 2px solid #eef0f4;
  border-radius: 16px;
  padding: 18px;
  cursor: pointer;
  transition:
    border-color 0.2s,
    transform 0.2s;
}

.type-card.active {
  border-color: #ff9f43;
  transform: translateY(-2px);
}

.type-symbol {
  font-size: 40px;
}

.type-card h2,
.preview-section h2,
.adopt-panel h2,
.pet-workbench h2 {
  font-size: 18px;
  margin: 8px 0 10px;
}

.type-card p,
.stage-card p,
.benefit-card p,
.bag-item p {
  color: #747b8a;
  font-size: 13px;
  line-height: 1.5;
}

.adopt-panel {
  max-width: 520px;
  margin: 22px 0;
}

.stage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 14px;
}

.stage-card {
  border: 1px solid #eef0f4;
  border-radius: 14px;
  padding: 14px;
  min-height: 188px;
}

.stage-card.current {
  border-color: #ff9f43;
  background: #fff8ec;
  box-shadow: 0 8px 20px rgba(255, 159, 67, 0.14);
}

.stage-card.reached {
  background: #f5fbf7;
  border-color: #ccebd8;
}

.stage-figure {
  height: 84px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.pet-gif {
  display: block;
  image-rendering: auto;
  object-fit: contain;
}

.preview-gif {
  width: 92px;
  height: 92px;
  transform: scale(var(--pet-scale));
  transform-origin: center bottom;
  filter: drop-shadow(0 8px 10px rgba(90, 55, 35, 0.16));
}

.stage-card strong {
  display: block;
  color: #8a92a3;
  font-size: 12px;
  margin-top: 8px;
}

.stage-card h3 {
  font-size: 16px;
  margin: 6px 0;
}

.adopt-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.pet-stage {
  min-height: 360px;
  border-radius: 24px;
  padding: 32px;
  background:
    radial-gradient(
      circle at 20% 20%,
      rgba(255, 255, 255, 0.82),
      transparent 26%
    ),
    linear-gradient(135deg, #ffe9a8 0%, #ff9f78 46%, #74c7a7 100%);
  display: grid;
  grid-template-columns: minmax(180px, 0.9fr) minmax(260px, 1fr) minmax(
      260px,
      0.95fr
    );
  align-items: center;
  gap: 24px;
  color: #33251f;
  box-shadow: 0 18px 42px rgba(242, 132, 92, 0.18);
}

.pet-stage.forest {
  background:
    radial-gradient(
      circle at 18% 18%,
      rgba(255, 255, 255, 0.74),
      transparent 25%
    ),
    linear-gradient(135deg, #e7f7c9 0%, #8fd5a6 45%, #4ba37a 100%);
}

.eyebrow {
  color: rgba(51, 37, 31, 0.72);
  font-size: 13px;
  font-weight: 800;
  margin-bottom: 6px;
}

.pet-summary h1 {
  font-size: 38px;
  line-height: 1.1;
  margin-bottom: 8px;
}

.mood,
.next-stage {
  font-size: 15px;
  font-weight: 700;
}

.next-stage {
  margin-top: 6px;
  color: rgba(51, 37, 31, 0.76);
}

.points-pill {
  margin-top: 22px;
  background: rgba(255, 255, 255, 0.58);
  border: 1px solid rgba(255, 255, 255, 0.62);
  border-radius: 16px;
  padding: 12px 16px;
  width: fit-content;
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.points-pill span {
  font-size: 13px;
}

.points-pill strong {
  font-size: 24px;
}

.pet-avatar {
  min-height: 248px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.46);
  border: 8px solid rgba(255, 255, 255, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow:
    inset 0 -24px 55px rgba(255, 255, 255, 0.35),
    0 18px 36px rgba(100, 68, 40, 0.18);
  overflow: hidden;
}

.hero-gif {
  width: min(54%, 240px);
  height: min(54%, 195px);
  filter: drop-shadow(0 18px 18px rgba(90, 55, 35, 0.2));
}

.hat-mark {
  position: absolute;
  top: 22px;
  font-size: 42px;
}

.bed-mark {
  position: absolute;
  bottom: 26px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 700;
}

.status-panel {
  background: rgba(255, 255, 255, 0.64);
  border-radius: 18px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-row {
  display: grid;
  grid-template-columns: 44px 1fr;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  font-weight: 700;
}

.exp-line {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid rgba(95, 64, 42, 0.14);
  padding-top: 12px;
  font-weight: 800;
}

.benefit-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 16px;
  margin-bottom: 22px;
}

.benefit-card,
.bag-item {
  border: 1px solid #eef0f4;
  border-radius: 14px;
  padding: 16px;
  background: #fff;
}

.benefit-card {
  min-height: 226px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.benefit-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: #fff4df;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.benefit-icon.small {
  width: 42px;
  height: 42px;
  font-size: 24px;
}

.benefit-card h3,
.bag-item h3 {
  font-size: 16px;
}

.benefit-card p {
  min-height: 40px;
}

.benefit-meta {
  display: flex;
  justify-content: space-between;
  color: #8a92a3;
  font-size: 12px;
  font-weight: 700;
  margin-top: auto;
}

.bag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.bag-item {
  display: grid;
  grid-template-columns: 42px 1fr auto;
  align-items: center;
  gap: 12px;
}

@media (max-width: 900px) {
  .pet-stage {
    grid-template-columns: 1fr;
  }

  .pet-avatar {
    min-height: 220px;
    max-width: 360px;
    width: 100%;
    justify-self: center;
  }
}

@media (max-width: 560px) {
  .pet-stage,
  .adopt-page,
  .room-preview-section,
  .pet-workbench {
    padding: 16px;
    border-radius: 18px;
  }

  .bag-item {
    grid-template-columns: 42px 1fr;
  }

  .bag-item .el-button {
    grid-column: 1 / -1;
  }
}
</style>
