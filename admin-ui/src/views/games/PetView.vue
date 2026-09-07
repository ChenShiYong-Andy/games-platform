<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getData, postData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import PetBenefitShopCard from '@/components/PetBenefitShopCard.vue'
import PetInventoryItemCard from '@/components/PetInventoryItemCard.vue'
import type {
  PetBenefitItem,
  PetColorOption,
  DailyEnglishItem,
  DailyEnglishPractice,
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
const usingAll = ref(false)
const growing = ref(false)
const hasPet = ref(false)
const availablePoints = ref(0)
const petInfo = ref<PetInfo | null>(null)
const benefits = ref<PetBenefitItem[]>([])
const myBenefits = ref<PetUserBenefit[]>([])
const petTypes = ref<PetTypeOption[]>([])
const selectedType = ref('')
const petName = ref('')
const activeTab = ref<'shop' | 'bag'>('shop')
const shopExpanded = ref(false)
const englishExpanded = ref(false)
const englishLoading = ref(false)
const englishPractice = ref<DailyEnglishPractice | null>(null)
const listeningIndex = ref<number | null>(null)
const pronunciationResults = ref<Record<number, { correct: boolean; transcript: string }>>({})
const exchangeQuantities = ref<Record<number, number>>({})
const useSuccessVisible = ref(false)
const useSuccessName = ref('')
const useSuccessGif = ref('')

interface SpeechRecognitionEventLike {
  results: { [index: number]: { [index: number]: { transcript: string } } }
}

interface SpeechRecognitionLike {
  lang: string
  continuous: boolean
  interimResults: boolean
  maxAlternatives: number
  start: () => void
  stop: () => void
  onstart: (() => void) | null
  onresult: ((event: SpeechRecognitionEventLike) => void) | null
  onerror: (() => void) | null
  onend: (() => void) | null
}

type SpeechRecognitionConstructor = new () => SpeechRecognitionLike
let activeRecognition: SpeechRecognitionLike | null = null

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
const usableBenefits = computed(() =>
  myBenefits.value.filter(
    (item) =>
      item.status === 1 &&
      item.quantity > 0 &&
      item.benefitCode !== 'PET_EXP_FRUIT'
  )
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

const growthPercent = computed(() => {
  return Math.max(0, Math.min(100, petInfo.value?.exp || 0))
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
  if (code.includes('ENERGY')) return '🥤'
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
  consumableBenefits.value.forEach((item) => {
    if (!exchangeQuantities.value[item.benefitId]) {
      exchangeQuantities.value[item.benefitId] = 1
    }
  })
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

function exchangeMax(item: PetBenefitItem) {
  const pointMax = Math.floor(availablePoints.value / item.costPoints)
  const stockMax = item.stock ?? 100
  return Math.max(1, Math.min(100, pointMax, stockMax))
}

function getExchangeQuantity(item: PetBenefitItem) {
  return Math.max(
    1,
    Math.min(exchangeQuantities.value[item.benefitId] || 1, exchangeMax(item))
  )
}

async function exchangeBenefit(item: PetBenefitItem) {
  if (!item.canExchange) {
    ElMessage.info(
      item.owned ? '你已经拥有这个权益' : '当前积分不足或暂不可兑换'
    )
    return
  }

  const quantity = getExchangeQuantity(item)
  const totalCost = item.costPoints * quantity

  if (totalCost >= 100 || quantity > 1) {
    try {
      await ElMessageBox.confirm(
        `将消耗 ${totalCost} 积分兑换「${item.benefitName}」x${quantity}`,
        '确认兑换',
        {
          confirmButtonText: '兑换',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
    } catch {
      return
    }
  }

  exchangingId.value = item.benefitId
  try {
    const result = await postData<PetExchangeResponse>(
      '/pet/benefit/exchange',
      { benefitId: item.benefitId, quantity }
    )
    ElMessage.success(`已兑换 ${result.benefitName} x${quantity}`)
    exchangeQuantities.value[item.benefitId] = 1
    await refreshAfterChange(undefined, result.availablePoints)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '兑换失败')
  } finally {
    exchangingId.value = null
  }
}

async function useBenefit(item: PetUserBenefit, useAll = false) {
  const fullStatusName = getFullStatusName(item)
  if (fullStatusName) {
    ElMessage.info(`${fullStatusName}已达到 100%，不需要使用该道具`)
    return
  }
  usingId.value = item.userBenefitId
  usingAll.value = useAll
  try {
    const result = await postData<PetUseBenefitResponse>('/pet/benefit/use', {
      userBenefitId: item.userBenefitId,
      useAll
    })
    ElMessage.success(
      result.usedQuantity > 1
        ? `已使用 ${result.benefitName} x${result.usedQuantity}`
        : `已使用 ${result.benefitName}`
    )
    useSuccessName.value = result.benefitName
    useSuccessGif.value = `${petGifSrc(
      result.petInfo.petAssetKey,
      result.petInfo.petType,
      result.petInfo.stageNo
    )}?success=${Date.now()}`
    useSuccessVisible.value = true
    await refreshAfterChange(result.petInfo)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '使用失败')
  } finally {
    usingId.value = null
    usingAll.value = false
  }
}

function getStatusEffect(item: PetUserBenefit) {
  const benefit = benefits.value.find(
    (candidate) => candidate.benefitId === item.benefitId
  )
  const statusByEffect: Record<string, { name: string; value: number }> = {
    HUNGER_FULL: { name: '饥饿值', value: petInfo.value?.hunger ?? 0 },
    CLEAN_FULL: { name: '清洁值', value: petInfo.value?.clean ?? 0 },
    HAPPINESS_FULL: { name: '快乐值', value: petInfo.value?.happiness ?? 0 },
    ENERGY_FULL: { name: '体力值', value: petInfo.value?.energy ?? 0 }
  }
  const status = benefit?.effectType
    ? statusByEffect[benefit.effectType]
    : undefined
  return status && benefit
    ? { ...status, effectValue: benefit.effectValue || 5 }
    : null
}

function getFullStatusName(item: PetUserBenefit) {
  const status = getStatusEffect(item)
  return status && status.value >= 100 ? status.name : null
}

function getUseAllQuantity(item: PetUserBenefit) {
  const status = getStatusEffect(item)
  if (!status || status.value >= 100) return 0
  return Math.min(
    item.quantity,
    Math.ceil((100 - status.value) / status.effectValue)
  )
}

async function growPet() {
  growing.value = true
  try {
    const pet = await postData<PetInfo>('/pet/grow')
    ElMessage.success('成长成功')
    await refreshAfterChange(pet)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '成长失败')
  } finally {
    growing.value = false
  }
}

async function toggleDailyEnglish() {
  englishExpanded.value = !englishExpanded.value
  if (englishExpanded.value && !englishPractice.value && !englishLoading.value) {
    await loadDailyEnglish()
  }
}

async function loadDailyEnglish() {
  englishLoading.value = true
  try {
    englishPractice.value = await getData<DailyEnglishPractice>(
      '/daily-english/today',
      undefined,
      60000
    )
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '每日英语生成失败')
  } finally {
    englishLoading.value = false
  }
}

function selectEnglishFemaleVoice(voices: SpeechSynthesisVoice[]) {
  const femaleVoicePattern = /female|samantha|victoria|karen|moira|tessa|ava|zira|susan|hazel|aria|jenny|joanna|salli|kendra|kimberly|ivy|amy|emma|nicole|fiona|serena|kate|olivia|natasha|sonia|libby|michelle|allison|nicky/i
  const maleVoicePattern = /male|alex|daniel|fred|tom|david|mark|george|james|ryan|guy/i
  const qualityPattern = /natural|neural|premium|enhanced|online/i
  const englishVoices = voices.filter((voice) => voice.lang.toLowerCase().startsWith('en'))
  const rankVoice = (voice: SpeechSynthesisVoice) => {
    const identity = `${voice.name} ${voice.voiceURI}`
    let score = 0
    if (femaleVoicePattern.test(identity)) score += 200
    if (/google us english/i.test(identity)) score += 160
    if (qualityPattern.test(identity)) score += 80
    if (voice.lang.toLowerCase() === 'en-us') score += 35
    if (voice.default) score += 5
    return score
  }
  const femaleVoices = englishVoices.filter((voice) => {
    const identity = `${voice.name} ${voice.voiceURI}`
    return femaleVoicePattern.test(identity) || /google us english/i.test(identity)
  })
  const candidates = femaleVoices.length
    ? femaleVoices
    : englishVoices.filter(
        (voice) => !maleVoicePattern.test(`${voice.name} ${voice.voiceURI}`)
      )
  return [...candidates].sort((left, right) => rankVoice(right) - rankVoice(left))[0] || null
}

async function getSpeechVoices() {
  const currentVoices = window.speechSynthesis.getVoices()
  if (currentVoices.length > 0) return currentVoices
  return new Promise<SpeechSynthesisVoice[]>((resolve) => {
    const finish = () => resolve(window.speechSynthesis.getVoices())
    window.speechSynthesis.addEventListener('voiceschanged', finish, { once: true })
    window.setTimeout(finish, 600)
  })
}

async function playEnglish(text: string) {
  if (!('speechSynthesis' in window)) {
    ElMessage.warning('当前设备不支持语音播放')
    return
  }
  window.speechSynthesis.cancel()
  const voices = await getSpeechVoices()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'en-US'
  utterance.voice = selectEnglishFemaleVoice(voices)
  utterance.rate = 0.86
  utterance.pitch = 1
  window.speechSynthesis.speak(utterance)
}

function startReading(item: DailyEnglishItem, index: number) {
  const speechWindow = window as Window & {
    SpeechRecognition?: SpeechRecognitionConstructor
    webkitSpeechRecognition?: SpeechRecognitionConstructor
  }
  const Recognition =
    speechWindow.SpeechRecognition || speechWindow.webkitSpeechRecognition
  if (!Recognition) {
    ElMessage.warning('当前浏览器不支持语音识别，请使用最新版 Chrome 或 Edge')
    return
  }
  activeRecognition?.stop()
  const recognition = new Recognition()
  activeRecognition = recognition
  recognition.lang = 'en-US'
  recognition.continuous = false
  recognition.interimResults = false
  recognition.maxAlternatives = 1
  recognition.onstart = () => {
    listeningIndex.value = index
  }
  recognition.onresult = event => {
    const transcript = event.results[0]?.[0]?.transcript || ''
    const correct = pronunciationMatches(item.text, transcript)
    pronunciationResults.value[index] = { correct, transcript }
    if (correct) {
      ElMessage.success('读得很棒！')
    } else {
      ElMessage.warning(`识别为：${transcript || '未识别到内容'}，请再试一次`)
    }
  }
  recognition.onerror = () => {
    ElMessage.error('录音或语音识别失败，请检查麦克风权限')
  }
  recognition.onend = () => {
    listeningIndex.value = null
    if (activeRecognition === recognition) activeRecognition = null
  }
  recognition.start()
}

function pronunciationMatches(expected: string, actual: string) {
  const target = normalizeSpeech(expected)
  const spoken = normalizeSpeech(actual)
  if (!target || !spoken) return false
  if (target === spoken) return true
  const longest = Math.max(target.length, spoken.length)
  const similarity = 1 - levenshteinDistance(target, spoken) / longest
  return similarity >= (target.includes(' ') ? 0.78 : 0.82)
}

function normalizeSpeech(value: string) {
  return value
    .toLowerCase()
    .replace(/[’']/g, '')
    .replace(/[^a-z\s]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}

function levenshteinDistance(left: string, right: string) {
  const previous = Array.from({ length: right.length + 1 }, (_, index) => index)
  for (let i = 1; i <= left.length; i++) {
    let diagonal = previous[0]
    previous[0] = i
    for (let j = 1; j <= right.length; j++) {
      const above = previous[j]
      previous[j] = Math.min(
        previous[j] + 1,
        previous[j - 1] + 1,
        diagonal + (left[i - 1] === right[j - 1] ? 0 : 1)
      )
      diagonal = above
    }
  }
  return previous[right.length]
}

onMounted(() => {
  void loadPage()
})

onBeforeUnmount(() => {
  activeRecognition?.stop()
  window.speechSynthesis?.cancel()
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
      <div class="pet-detail-layout">
      <div class="pet-growth-column">
      <section
        class="pet-stage"
        :class="{ forest: petInfo?.currentRoomTheme === 'PET_ROOM_FOREST' }"
      >
        <div class="pet-display">
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
        </div>

        <div class="status-panel">
          <div class="growth-status-block">
            <div class="growth-profile-header">
              <div>
                <p class="eyebrow">
                  {{ petInfo?.petTypeName }} · {{ petInfo?.petColorName }}
                </p>
                <h2>{{ petInfo?.petName }}</h2>
              </div>
              <el-button
                class="grow-button"
                :loading="growing"
                @click="growPet"
              >
                点击成长
              </el-button>
            </div>
            <div class="growth-actions-row">
              <div class="points-pill compact">
                <span>可用积分</span>
                <strong>{{ availablePoints }}</strong>
              </div>
              <p class="next-stage">{{ nextStageText }}</p>
            </div>
            <div class="charge-progress">
              <div
                class="charge-fill"
                :style="{ width: `${growthPercent}%` }"
              ></div>
              <div class="charge-meta">
                <span>Lv.{{ petInfo?.level }} · {{ petInfo?.exp || 0 }}/100</span>
                <strong>{{ petInfo?.stageName }}</strong>
                <span>{{ petMood }}</span>
              </div>
            </div>
          </div>

          <div class="care-status-block">
            <div class="status-block-heading">
              <div>
                <small>生活状态</small>
                <strong>{{ petMood }}</strong>
              </div>
              <span>当前状态</span>
            </div>
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
      </div>

      <div class="pet-side-column">
      <section class="daily-english card" :class="{ expanded: englishExpanded }">
        <button
          class="feature-collapse-trigger english-trigger"
          type="button"
          :aria-expanded="englishExpanded"
          @click="toggleDailyEnglish"
        >
          <span class="feature-trigger-icon">🔤</span>
          <span class="feature-trigger-copy">
            <strong>每日英语</strong>
            <small>单词与短句口语跟读</small>
          </span>
          <span class="feature-trigger-arrow" :class="{ expanded: englishExpanded }">⌄</span>
        </button>

        <div v-show="englishExpanded" v-loading="englishLoading" class="daily-english-body">
          <template v-if="englishPractice">
            <div class="english-heading">
              <div>
                <small>{{ englishPractice.gradeLabel }}</small>
                <h2>{{ englishPractice.title }}</h2>
              </div>
              <span>{{ englishPractice.date }}</span>
            </div>
            <div class="english-card-grid">
              <article
                v-for="(item, index) in englishPractice.items"
                :key="`${item.type}-${item.text}`"
                class="english-card"
                :class="item.type.toLowerCase()"
              >
                <span class="english-kind">{{ item.type === 'WORD' ? '单词' : '短句' }}</span>
                <h3>{{ item.text }}</h3>
                <p v-if="item.phonetic" class="phonetic">{{ item.phonetic }}</p>
                <p class="translation">{{ item.translation }}</p>
                <small v-if="item.tip" class="pronunciation-tip">{{ item.tip }}</small>
                <div class="english-actions">
                  <button type="button" @click="playEnglish(item.text)">🔊 播放</button>
                  <button
                    type="button"
                    :class="{ listening: listeningIndex === index }"
                    @click="startReading(item, index)"
                  >
                    {{ listeningIndex === index ? '🎙️ 正在听…' : '🎙️ 跟读' }}
                  </button>
                </div>
                <p
                  v-if="pronunciationResults[index]"
                  class="speech-result"
                  :class="pronunciationResults[index].correct ? 'correct' : 'retry'"
                >
                  {{ pronunciationResults[index].correct ? '✓ 发音正确' : `识别：${pronunciationResults[index].transcript}` }}
                </p>
              </article>
            </div>
          </template>
          <div v-else-if="!englishLoading" class="english-empty">
            <p>暂时无法获取今日练习</p>
            <el-button type="primary" plain @click="loadDailyEnglish">重新生成</el-button>
          </div>
        </div>
      </section>

      <section class="pet-workbench" :class="{ collapsed: !shopExpanded }">
        <button
          class="shop-collapse-trigger"
          type="button"
          :aria-expanded="shopExpanded"
          @click="shopExpanded = !shopExpanded"
        >
          <span class="shop-trigger-icon">🎁</span>
          <span class="shop-trigger-copy">
            <strong>积分商店</strong>
            <small>权益兑换与背包管理</small>
          </span>
          <span class="shop-trigger-arrow" :class="{ expanded: shopExpanded }"
            >⌄</span
          >
        </button>

        <div v-show="shopExpanded" class="pet-workbench-body">
          <el-tabs v-model="activeTab">
          <el-tab-pane label="权益商店" name="shop">
            <h2>消耗型道具</h2>
            <div class="benefit-grid">
              <PetBenefitShopCard
                v-for="item in consumableBenefits"
                :key="item.benefitId"
                :icon="benefitIcon(item.benefitCode)"
                :title="item.benefitName"
                :description="item.description"
                :cost-points="item.costPoints"
                :owned-quantity="item.quantity"
                :exchange-quantity="getExchangeQuantity(item)"
                :max-exchange-quantity="exchangeMax(item)"
                :can-exchange="item.canExchange"
                :loading="exchangingId === item.benefitId"
                @update:exchange-quantity="exchangeQuantities[item.benefitId] = $event"
                @exchange="exchangeBenefit(item)"
              />
            </div>
          </el-tab-pane>

          <el-tab-pane label="我的背包" name="bag">
            <div v-if="usableBenefits.length > 0" class="bag-grid">
              <PetInventoryItemCard
                v-for="item in usableBenefits"
                :key="item.userBenefitId"
                :icon="benefitIcon(item.benefitCode)"
                :title="item.benefitName"
                :quantity="item.benefitType === 'CONSUMABLE' ? item.quantity : undefined"
                :use-all-quantity="getStatusEffect(item) ? getUseAllQuantity(item) : undefined"
                :single-action-label="item.benefitType === 'CONSUMABLE' ? '使用一个' : '切换'"
                :single-loading="usingId === item.userBenefitId && !usingAll"
                :use-all-loading="usingId === item.userBenefitId && usingAll"
                @use-one="useBenefit(item)"
                @use-all="useBenefit(item, true)"
              />
            </div>
            <el-empty v-else description="背包还没有可用权益" />
          </el-tab-pane>
          </el-tabs>
        </div>
      </section>
      </div>
      </div>
    </template>

    <el-dialog
      v-model="useSuccessVisible"
      width="360px"
      class="use-success-dialog"
      align-center
      :show-close="false"
    >
      <div class="use-success">
        <img
          v-if="useSuccessGif"
          class="use-success-gif"
          :src="useSuccessGif"
          alt="使用成功"
        />
        <h2>{{ useSuccessName }} 使用成功</h2>
        <p>宠物状态已经更新</p>
        <el-button type="primary" @click="useSuccessVisible = false">
          知道了
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.pet-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.pet-detail-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(460px, 0.92fr);
  align-items: start;
  gap: 24px;
}

.pet-growth-column {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.pet-side-column {
  min-width: 0;
  position: sticky;
  top: 88px;
  max-height: calc(100dvh - 112px);
  overflow-y: auto;
  scrollbar-width: thin;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pet-detail-layout .room-preview-section,
.pet-detail-layout .pet-workbench {
  min-width: 0;
  padding: 20px;
}

.pet-detail-layout .pet-workbench {
  position: static;
  min-height: 0;
  max-height: none;
  overflow: visible;
}

.pet-detail-layout .pet-workbench.collapsed {
  min-height: 0;
  max-height: none;
  overflow: hidden;
}

.daily-english {
  padding: 20px;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 6px 24px rgba(80, 93, 120, 0.08);
}

.feature-collapse-trigger {
  width: 100%;
  min-height: 68px;
  border: 0;
  border-radius: 14px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 11px;
  cursor: pointer;
  text-align: left;
}

.english-trigger {
  background: linear-gradient(135deg, #eef8ff, #fff 58%, #f2f0ff);
}

.english-trigger:hover {
  background: linear-gradient(135deg, #e1f3ff, #fff 58%, #ebe7ff);
}

.feature-trigger-icon {
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  border-radius: 12px;
  background: #e2f3ff;
  display: grid;
  place-items: center;
  font-size: 22px;
}

.feature-trigger-copy {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.feature-trigger-copy strong {
  color: #3479d8;
  font-size: 17px;
}

.feature-trigger-copy small {
  color: #9299a8;
  font-size: 11px;
}

.feature-trigger-arrow {
  color: #7f8795;
  font-size: 24px;
  line-height: 1;
  transition: transform 0.22s ease;
}

.feature-trigger-arrow.expanded {
  transform: rotate(180deg);
}

.daily-english-body {
  min-height: 120px;
  padding-top: 16px;
}

.english-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.english-heading small,
.english-heading > span {
  color: #8a92a3;
  font-size: 12px;
  font-weight: 700;
}

.english-heading h2 {
  margin-top: 3px;
  font-size: 20px;
}

.english-card-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.english-card {
  min-width: 0;
  border: 1px solid #e5ebf5;
  border-radius: 14px;
  padding: 13px;
  background: linear-gradient(155deg, #fff, #f7fbff);
}

.english-card.sentence {
  grid-column: 1 / -1;
  background: linear-gradient(155deg, #fff, #fbf8ff);
}

.english-kind {
  display: inline-flex;
  border-radius: 999px;
  padding: 3px 8px;
  background: #e9f4ff;
  color: #3479d8;
  font-size: 10px;
  font-weight: 800;
}

.english-card h3 {
  margin: 9px 0 3px;
  color: #293548;
  font-size: 18px;
  overflow-wrap: anywhere;
}

.phonetic {
  color: #79859a;
  font-size: 12px;
}

.translation {
  margin-top: 5px;
  color: #4f5968;
  font-size: 17px;
}

.pronunciation-tip {
  display: block;
  margin-top: 6px;
  color: #9a7a55;
  line-height: 1.45;
}

.english-actions {
  display: flex;
  gap: 7px;
  margin-top: 11px;
}

.english-actions button {
  flex: 1;
  border: 1px solid #cfe2f8;
  border-radius: 8px;
  padding: 7px 6px;
  background: #fff;
  color: #3479d8;
  cursor: pointer;
  font-weight: 700;
}

.english-actions button.listening {
  border-color: #ff8a65;
  background: #fff0eb;
  color: #d95b38;
  animation: listening-pulse 0.9s ease-in-out infinite alternate;
}

.speech-result {
  margin-top: 8px;
  border-radius: 7px;
  padding: 6px 8px;
  font-size: 11px;
  font-weight: 700;
}

.speech-result.correct {
  background: #e9f9ed;
  color: #2e9b4d;
}

.speech-result.retry {
  background: #fff3e7;
  color: #c46b24;
}

.english-empty {
  min-height: 110px;
  display: grid;
  place-items: center;
  gap: 10px;
  color: #9299a8;
}

@keyframes listening-pulse {
  to {
    box-shadow: 0 0 0 4px rgba(255, 138, 101, 0.16);
  }
}

.shop-collapse-trigger {
  width: 100%;
  min-height: 68px;
  border: 0;
  border-radius: 14px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #fff8eb, #fff 58%, #f2f8ff);
  color: #3d414b;
  display: flex;
  align-items: center;
  gap: 11px;
  cursor: pointer;
  text-align: left;
  transition:
    background 0.2s ease,
    box-shadow 0.2s ease;
}

.shop-collapse-trigger:hover {
  background: linear-gradient(135deg, #fff2d8, #fff 58%, #eaf4ff);
  box-shadow: 0 7px 20px rgba(80, 93, 120, 0.1);
}

.shop-trigger-icon {
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  border-radius: 12px;
  background: #fff0d3;
  display: grid;
  place-items: center;
  font-size: 22px;
}

.shop-trigger-copy {
  min-width: 0;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 3px;
}

.shop-trigger-copy strong {
  color: #409eff;
  font-size: 17px;
}

.shop-trigger-copy small {
  color: #9299a8;
  font-size: 11px;
  white-space: nowrap;
}

.shop-trigger-arrow {
  color: #7f8795;
  font-size: 24px;
  line-height: 1;
  transform: rotate(0deg);
  transition: transform 0.22s ease;
}

.shop-trigger-arrow.expanded {
  transform: rotate(180deg);
}

.pet-workbench-body {
  padding-top: 8px;
}

.pet-growth-column .pet-stage {
  grid-template-areas:
    'display'
    'status';
  grid-template-columns: minmax(0, 1fr);
  grid-template-rows: minmax(240px, auto) auto;
  align-items: stretch;
  gap: 20px;
  padding: 26px;
}

.pet-growth-column .pet-display {
  grid-area: display;
  width: 100%;
  min-height: 270px;
  align-self: stretch;
  justify-content: center;
}

.pet-growth-column .status-panel {
  grid-area: status;
  display: grid;
  grid-template-columns: minmax(260px, 0.88fr) minmax(320px, 1.12fr);
  align-items: stretch;
  padding: 10px;
  gap: 12px;
}

.growth-status-block,
.care-status-block {
  min-width: 0;
  border: 1px solid rgba(255, 255, 255, 0.56);
  border-radius: 14px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.36);
}

.growth-status-block {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
}

.care-status-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.status-block-heading,
.exp-header,
.growth-profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.status-block-heading {
  margin-bottom: 2px;
}

.status-block-heading div,
.exp-header div,
.growth-profile-header > div {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.growth-profile-header .eyebrow {
  margin: 0;
  font-size: 12px;
}

.growth-profile-header h2 {
  margin: 0;
  font-size: 23px;
  line-height: 1.1;
}

.points-pill.compact {
  margin: 0;
  padding: 7px 11px;
  border-radius: 11px;
}

.points-pill.compact strong {
  font-size: 19px;
}

.growth-actions-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.growth-actions-row .next-stage {
  margin-left: auto;
}

.status-block-heading small,
.exp-header small {
  color: rgba(51, 37, 31, 0.6);
  font-size: 12px;
  font-weight: 800;
}

.status-block-heading strong,
.exp-header strong {
  font-size: 17px;
}

.status-block-heading > span {
  border-radius: 999px;
  padding: 5px 9px;
  background: rgba(255, 255, 255, 0.64);
  color: rgba(51, 37, 31, 0.62);
  font-size: 11px;
  font-weight: 800;
}

.pet-growth-column .pet-avatar {
  flex: 1;
  min-height: 240px;
  width: 100%;
}

.pet-growth-column .hero-gif {
  width: min(58%, 290px);
  height: min(58%, 250px);
  object-fit: contain;
  object-position: center;
}

.pet-growth-column .next-stage {
  min-height: 34px;
  padding: 7px 16px;
  font-size: 14px;
}

.room-preview-section .stage-grid {
  grid-template-columns: repeat(auto-fit, minmax(145px, 1fr));
  gap: 12px;
}

.room-preview-section .stage-card {
  min-height: 170px;
  padding: 12px;
}

.room-preview-section .stage-figure {
  height: 72px;
}

.room-preview-section .preview-gif {
  width: 82px;
  height: 82px;
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
.stage-card p {
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
  min-height: 320px;
  border-radius: 24px;
  padding: 26px 30px;
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

.pet-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.next-stage {
  min-height: 38px;
  border-radius: 999px;
  padding: 8px 18px;
  background: rgba(255, 255, 255, 0.46);
  border: 1px solid rgba(255, 255, 255, 0.54);
  color: rgba(51, 37, 31, 0.78);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  font-size: 15px;
  font-weight: 800;
  box-shadow: 0 10px 22px rgba(100, 68, 40, 0.1);
}

.pet-avatar {
  min-height: 220px;
  width: 100%;
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
  border-top: 1px solid rgba(95, 64, 42, 0.14);
  padding-top: 12px;
  font-weight: 800;
}

.exp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.grow-button {
  width: 72px;
  min-width: 72px;
  height: 72px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffd75a 0%, #ff9f2f 58%, #ff6f4f 100%);
  color: #3b2615;
  font-size: 15px;
  font-weight: 900;
  letter-spacing: 0;
  box-shadow:
    0 10px 20px rgba(255, 132, 45, 0.28),
    inset 0 2px 0 rgba(255, 255, 255, 0.42);
}

.grow-button:hover,
.grow-button:focus {
  color: #2f1d10;
  transform: translateY(-1px);
  box-shadow:
    0 14px 24px rgba(255, 132, 45, 0.34),
    inset 0 2px 0 rgba(255, 255, 255, 0.5);
}

.grow-button:active {
  transform: translateY(1px) scale(0.99);
}

.charge-progress {
  position: relative;
  height: 34px;
  border: 2px solid rgba(73, 50, 34, 0.28);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.7);
  overflow: visible;
  box-shadow:
    inset 0 2px 6px rgba(90, 60, 36, 0.16),
    0 8px 18px rgba(125, 82, 45, 0.12);
}

.charge-progress::after {
  content: '';
  position: absolute;
  right: -8px;
  top: 9px;
  width: 6px;
  height: 12px;
  border-radius: 0 4px 4px 0;
  background: rgba(73, 50, 34, 0.34);
}

.charge-fill {
  position: absolute;
  inset: 3px auto 3px 3px;
  max-width: calc(100% - 6px);
  border-radius: 8px;
  background:
    linear-gradient(
      115deg,
      rgba(255, 255, 255, 0.36) 0 18%,
      transparent 18% 34%,
      rgba(255, 255, 255, 0.24) 34% 50%,
      transparent 50% 100%
    ),
    linear-gradient(90deg, #ffcf56 0%, #ff9f2f 52%, #35c47f 100%);
  background-size: 34px 100%, 100% 100%;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.54),
    0 0 14px rgba(255, 160, 47, 0.44);
}

.charge-meta {
  position: absolute;
  inset: 0;
  display: grid;
  grid-template-columns: 104px 1fr 76px;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  color: #2f261e;
  font-size: 13px;
  font-weight: 900;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.58);
}

.charge-meta strong {
  text-align: center;
}

.charge-meta span:last-child {
  text-align: right;
}

.benefit-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.bag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

.use-success {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
  padding: 8px 0 4px;
}

.use-success-gif {
  width: min(220px, 70vw);
  height: 150px;
  object-fit: contain;
  border-radius: 16px;
  background: #fff7eb;
  box-shadow: 0 16px 34px rgba(80, 55, 30, 0.16);
}

.use-success h2 {
  margin: 4px 0 0;
  font-size: 20px;
}

.use-success p {
  color: #8a92a3;
  font-weight: 700;
}

@media (max-width: 900px) {
  .pet-stage {
    grid-template-columns: 1fr;
  }

  .pet-growth-column .pet-stage {
    grid-template-areas:
      'display'
      'status';
    grid-template-columns: 1fr;
    grid-template-rows: auto;
  }

  .pet-avatar {
    min-height: 220px;
    max-width: 360px;
    width: 100%;
    justify-self: center;
  }
}

@media (max-width: 700px) {
  .pet-growth-column .status-panel {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1180px) {
  .pet-detail-layout {
    grid-template-columns: 1fr;
  }

  .pet-side-column {
    position: static;
    max-height: none;
    overflow: visible;
  }

  .benefit-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .benefit-grid {
    grid-template-columns: 1fr;
  }

  .pet-stage,
  .adopt-page,
  .room-preview-section,
  .pet-workbench,
  .daily-english {
    padding: 16px;
    border-radius: 18px;
  }

  .english-card-grid {
    grid-template-columns: 1fr;
  }

  .english-card.sentence {
    grid-column: auto;
  }

}
</style>
