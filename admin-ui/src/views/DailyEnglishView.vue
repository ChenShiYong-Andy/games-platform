<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getData, postData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type {
  DailyEnglishItem,
  DailyEnglishPractice,
  DailyEnglishTaskCompletion,
  DailyEnglishTaskStatus
} from '@/types'

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

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const practice = ref<DailyEnglishPractice | null>(null)
const listeningIndex = ref<number | null>(null)
const pronunciationResults = ref<Record<number, { correct: boolean; transcript: string }>>({})
const taskCompleted = ref(false)
const rewardPoints = ref(20)
const completingTask = ref(false)
let activeRecognition: SpeechRecognitionLike | null = null

const completedCount = computed(() => {
  if (!practice.value) return 0
  return practice.value.items.filter((_, index) => pronunciationResults.value[index]?.correct).length
})

async function loadDailyEnglish() {
  loading.value = true
  try {
    practice.value = await getData<DailyEnglishPractice>('/daily-english/today', undefined, 60000)
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '每日英语生成失败')
  } finally {
    loading.value = false
  }
}

async function loadTaskStatus() {
  try {
    const status = await getData<DailyEnglishTaskStatus>('/daily-english/today/status')
    taskCompleted.value = status.completed
    rewardPoints.value = status.rewardPoints
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '跟读任务状态加载失败')
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
    : englishVoices.filter((voice) => !maleVoicePattern.test(`${voice.name} ${voice.voiceURI}`))
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
  const Recognition = speechWindow.SpeechRecognition || speechWindow.webkitSpeechRecognition
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
      void completeTaskIfReady()
    } else {
      ElMessage.warning(`识别为：${transcript || '未识别到内容'}，请再试一次`)
    }
  }
  recognition.onerror = () => ElMessage.error('录音或语音识别失败，请检查麦克风权限')
  recognition.onend = () => {
    listeningIndex.value = null
    if (activeRecognition === recognition) activeRecognition = null
  }
  recognition.start()
}

async function completeTaskIfReady() {
  if (!practice.value || taskCompleted.value || completingTask.value) return
  const completedItems = practice.value.items
    .filter((_, index) => pronunciationResults.value[index]?.correct)
    .map(item => item.text)
  if (completedItems.length !== practice.value.items.length) return

  completingTask.value = true
  try {
    const result = await postData<DailyEnglishTaskCompletion>('/daily-english/today/complete', {
      completedItems
    })
    taskCompleted.value = true
    await authStore.refreshProfile().catch(() => undefined)
    if (result.newlyCompleted) {
      ElMessage.success(`今日跟读任务完成，获得 ${result.pointsEarned} 积分！`)
    } else {
      ElMessage.info('今日跟读任务已完成')
    }
  } catch (error: unknown) {
    ElMessage.error(error instanceof Error ? error.message : '跟读任务结算失败')
  } finally {
    completingTask.value = false
  }
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
  return value.toLowerCase().replace(/[’']/g, '').replace(/[^a-z\s]/g, ' ').replace(/\s+/g, ' ').trim()
}

function levenshteinDistance(left: string, right: string) {
  const previous = Array.from({ length: right.length + 1 }, (_, index) => index)
  for (let i = 1; i <= left.length; i++) {
    let diagonal = previous[0]
    previous[0] = i
    for (let j = 1; j <= right.length; j++) {
      const above = previous[j]
      previous[j] = Math.min(previous[j] + 1, previous[j - 1] + 1, diagonal + (left[i - 1] === right[j - 1] ? 0 : 1))
      diagonal = above
    }
  }
  return previous[right.length]
}

onMounted(() => {
  void Promise.all([loadDailyEnglish(), loadTaskStatus()])
})
onBeforeUnmount(() => {
  activeRecognition?.stop()
  window.speechSynthesis?.cancel()
})
</script>

<template>
  <div class="page-container english-page">
    <button class="back-button" type="button" @click="router.push('/')">← 返回游戏大厅</button>
    <header class="page-heading">
      <span>🔤</span>
      <div>
        <h1>每日英语</h1>
        <p>每天练习实用单词与短句，播放示范后进行跟读</p>
      </div>
    </header>

    <section v-loading="loading" class="practice-panel">
      <template v-if="practice">
        <div class="practice-heading">
          <div><small>{{ practice.gradeLabel }}</small><h2>{{ practice.title }}</h2></div>
          <div class="task-summary" :class="{ completed: taskCompleted }">
            <strong v-if="taskCompleted">✓ 今日任务已完成</strong>
            <strong v-else>跟读进度 {{ completedCount }}/{{ practice.items.length }}</strong>
            <span>{{ taskCompleted ? `已获得 ${rewardPoints} 积分` : `全部完成 +${rewardPoints} 积分` }}</span>
          </div>
        </div>
        <div class="english-card-grid">
          <article v-for="(item, index) in practice.items" :key="`${item.type}-${item.text}`" class="english-card" :class="item.type.toLowerCase()">
            <span class="english-kind">{{ item.type === 'WORD' ? '单词' : '短句' }}</span>
            <h3>{{ item.text }}</h3>
            <p v-if="item.phonetic" class="phonetic">{{ item.phonetic }}</p>
            <p class="translation">{{ item.translation }}</p>
            <small v-if="item.tip" class="pronunciation-tip">{{ item.tip }}</small>
            <div class="english-actions">
              <button type="button" @click="playEnglish(item.text)">🔊 播放</button>
              <button type="button" :class="{ listening: listeningIndex === index }" @click="startReading(item, index)">
                {{ listeningIndex === index ? '🎙️ 正在听…' : '🎙️ 跟读' }}
              </button>
            </div>
            <p v-if="pronunciationResults[index]" class="speech-result" :class="pronunciationResults[index].correct ? 'correct' : 'retry'">
              {{ pronunciationResults[index].correct ? '✓ 发音正确' : `识别：${pronunciationResults[index].transcript}` }}
            </p>
          </article>
        </div>
      </template>
      <div v-else-if="!loading" class="english-empty">
        <p>暂时无法获取今日练习</p>
        <el-button type="primary" plain @click="loadDailyEnglish">重新生成</el-button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.english-page { max-width: 1180px; }
.back-button { min-height:38px;margin-bottom:18px;padding:8px 14px;border:1px solid rgba(79,172,254,.35);border-radius:10px;background:#fff;color:#427bd8;font-size:14px;font-weight:700;cursor:pointer;box-shadow:0 4px 12px rgba(79,130,210,.1); }
.back-button:hover { background:#4f8fea;color:#fff; }
.page-heading { display:flex;align-items:center;gap:16px;margin-bottom:24px; }
.page-heading > span { width:62px;height:62px;border-radius:18px;background:linear-gradient(145deg,#dff4ff,#ece8ff);display:grid;place-items:center;font-size:34px; }
.page-heading h1 { margin:0;color:#2e3747;font-size:30px; }
.page-heading p { margin:5px 0 0;color:#8a92a3; }
.practice-panel { min-height:240px;padding:26px;border-radius:22px;background:rgba(255,255,255,.9);box-shadow:0 6px 26px rgba(70,76,120,.09); }
.practice-heading { display:flex;align-items:flex-end;justify-content:space-between;gap:12px;margin-bottom:20px; }
.practice-heading small { color:#8a92a3;font-size:12px;font-weight:700; }
.practice-heading h2 { margin:4px 0 0;font-size:22px; }
.task-summary { display:flex;flex-direction:column;align-items:flex-end;gap:4px;border:1px solid #dbe7fa;border-radius:12px;padding:8px 12px;background:#f4f8ff;color:#4778c7; }
.task-summary strong { font-size:13px; }
.task-summary span { color:#7f8ba0;font-size:11px;font-weight:700; }
.task-summary.completed { border-color:#ccebd7;background:#eefaf2;color:#2e9b55; }
.english-card-grid { display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px; }
.english-card { min-width:0;border:1px solid #e5ebf5;border-radius:16px;padding:18px;background:linear-gradient(155deg,#fff,#f7fbff); }
.english-card.sentence { grid-column:1/-1;background:linear-gradient(155deg,#fff,#fbf8ff); }
.english-kind { display:inline-flex;border-radius:999px;padding:4px 9px;background:#e9f4ff;color:#3479d8;font-size:11px;font-weight:800; }
.english-card h3 { margin:11px 0 3px;color:#293548;font-size:21px;overflow-wrap:anywhere; }
.phonetic { color:#79859a;font-size:13px; }
.translation { margin-top:6px;color:#4f5968;font-size:17px; }
.pronunciation-tip { display:block;margin-top:7px;color:#9a7a55;line-height:1.45; }
.english-actions { display:flex;gap:9px;margin-top:14px; }
.english-actions button { flex:1;border:1px solid #cfe2f8;border-radius:9px;padding:9px 7px;background:#fff;color:#3479d8;cursor:pointer;font-weight:700; }
.english-actions button.listening { border-color:#ff8a65;background:#fff0eb;color:#d95b38;animation:listening-pulse .9s ease-in-out infinite alternate; }
.speech-result { margin-top:9px;border-radius:8px;padding:7px 9px;font-size:12px;font-weight:700; }
.speech-result.correct { background:#e9f9ed;color:#2e9b4d; }
.speech-result.retry { background:#fff3e7;color:#c46b24; }
.english-empty { min-height:190px;display:grid;place-items:center;gap:10px;color:#9299a8; }
@keyframes listening-pulse { to { box-shadow:0 0 0 4px rgba(255,138,101,.16); } }
@media(max-width:680px){.practice-panel{padding:18px}.english-card-grid{grid-template-columns:1fr}.english-card.sentence{grid-column:auto}.page-heading p{font-size:13px}}
</style>
