<script setup lang="ts">
import { computed, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { postData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { ZooCareResponse } from '@/types'

type GamePhase = 'start' | 'playing' | 'won' | 'lost'
type NeedId = 'food' | 'water' | 'ball'
type FeedbackKind = 'idle' | 'success' | 'error'

interface CareTool {
  id: NeedId
  label: string
  emoji: string
  needText: string
  color: string
}

const router = useRouter()
const authStore = useAuthStore()

const tools: CareTool[] = [
  { id: 'food', label: '香甜香蕉', emoji: '🍌', needText: '我肚子饿了，想吃点东西！', color: '#ffbf47' },
  { id: 'water', label: '清凉水杯', emoji: '🥤', needText: '我口渴了，想喝点水！', color: '#64b5f6' },
  { id: 'ball', label: '彩色玩具球', emoji: '⚽', needText: '我有点无聊，想玩一会儿！', color: '#ff7e79' }
]

const phase = ref<GamePhase>('start')
const currentNeed = ref<NeedId>('food')
const feedbackKind = ref<FeedbackKind>('idle')
const speech = ref('欢迎来到我的小家！')
const score = ref(0)
const careSubmitting = ref(false)
const remainingCareToday = ref<number | null>(null)
const remainingSeconds = ref(120)
const gameId = ref<number | null>(null)
const pointsAwarded = ref(0)
const pointsDeducted = ref(0)
const accountTotalPoints = ref<number | null>(null)
const targetScore = 10
let nextNeedTimer: ReturnType<typeof setTimeout> | undefined
let countdownTimer: ReturnType<typeof setInterval> | undefined

const currentTool = computed(() => tools.find(tool => tool.id === currentNeed.value) ?? tools[0])
const isHappy = computed(() => feedbackKind.value === 'success')

function clearGameTimers() {
  clearTimeout(nextNeedTimer)
  clearInterval(countdownTimer)
}

function finishGame(result: 'won' | 'lost') {
  clearGameTimers()
  phase.value = result
}

function setNeed(need: NeedId) {
  currentNeed.value = need
  speech.value = tools.find(tool => tool.id === need)?.needText ?? ''
  feedbackKind.value = 'idle'
}

function startCountdown() {
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    remainingSeconds.value -= 1
    if (remainingSeconds.value <= 0) {
      remainingSeconds.value = 0
      clearInterval(countdownTimer)
      void settleTimedOutGame()
    }
  }, 1000)
}

function applyGameResponse(result: ZooCareResponse) {
  gameId.value = result.gameId
  score.value = result.score
  remainingSeconds.value = result.remainingSeconds
  remainingCareToday.value = result.remainingToday
  pointsAwarded.value = result.pointsAwarded
  pointsDeducted.value = result.pointsDeducted
  accountTotalPoints.value = result.totalPoints

  if (result.status === 'COMPLETED') {
    finishGame('won')
    void authStore.refreshProfile().catch(() => undefined)
  } else if (result.status === 'FAILED') {
    finishGame('lost')
    void authStore.refreshProfile().catch(() => undefined)
  }
}

async function startGame() {
  if (careSubmitting.value) {
    return
  }
  clearGameTimers()
  careSubmitting.value = true
  try {
    const result = await postData<ZooCareResponse>('/zoo/games')
    applyGameResponse(result)
    if (result.status === 'IN_PROGRESS') {
      phase.value = 'playing'
      pointsAwarded.value = 0
      pointsDeducted.value = 0
      setNeed(result.currentNeed)
      startCountdown()
    }
  } catch (e: unknown) {
    speech.value = e instanceof Error ? e.message : '游戏开始失败，请稍后重试'
  } finally {
    careSubmitting.value = false
  }
}

async function selectTool(tool: CareTool) {
  if (phase.value !== 'playing' || feedbackKind.value === 'success' || careSubmitting.value || gameId.value === null) {
    return
  }

  careSubmitting.value = true
  try {
    const result = await postData<ZooCareResponse>(`/zoo/games/${gameId.value}/care`, { tool: tool.id })
    applyGameResponse(result)
    if (result.status !== 'IN_PROGRESS') {
      return
    }

    if (result.correct) {
      feedbackKind.value = 'success'
      speech.value = `谢谢你！${tool.label}正是我想要的！`
      nextNeedTimer = setTimeout(() => setNeed(result.currentNeed), 1100)
    } else {
      feedbackKind.value = 'error'
      speech.value = result.message
    }
  } catch (e: unknown) {
    feedbackKind.value = 'error'
    speech.value = e instanceof Error ? e.message : '照顾失败，请稍后重试'
  } finally {
    careSubmitting.value = false
  }
}

async function settleTimedOutGame() {
  if (gameId.value === null || phase.value !== 'playing') {
    return
  }
  careSubmitting.value = true
  try {
    const result = await postData<ZooCareResponse>(`/zoo/games/${gameId.value}/settle`)
    applyGameResponse(result)
    if (result.status === 'IN_PROGRESS') {
      remainingSeconds.value = result.remainingSeconds
      startCountdown()
    }
  } catch (e: unknown) {
    feedbackKind.value = 'error'
    speech.value = e instanceof Error ? e.message : '结算失败，请稍后重试'
  } finally {
    careSubmitting.value = false
  }
}

onUnmounted(clearGameTimers)
</script>

<template>
  <div class="zoo-page">
    <div class="zoo-header">
      <button class="back-btn" type="button" @click="router.push('/')">← 返回游戏大厅</button>
      <div>
        <p class="eyebrow">倒计时挑战版</p>
        <h1>小小动物园管理员</h1>
      </div>
      <div v-if="phase === 'playing'" class="game-stats" aria-label="游戏状态">
        <div class="stat-chip score-chip">
          <span>爱心分数</span>
          <strong data-testid="zoo-score">♥ {{ score }} / {{ targetScore }}</strong>
        </div>
        <div class="stat-chip" :class="{ urgent: remainingSeconds <= 15 }">
          <span>剩余时间</span>
          <strong data-testid="zoo-timer">⏱ {{ remainingSeconds }} 秒</strong>
        </div>
      </div>
    </div>

    <section v-if="phase === 'start'" class="start-screen">
      <div class="sun" aria-hidden="true">☀</div>
      <div class="cloud cloud-left" aria-hidden="true"></div>
      <div class="cloud cloud-right" aria-hidden="true"></div>
      <div class="start-card">
        <p class="start-kicker">欢迎来到</p>
        <h2>快乐动物园</h2>
        <div class="start-monkey" aria-hidden="true">🐵</div>
        <p>小猴子正在等你照顾它。<br>看看它需要什么，选对工具吧！</p>
        <button class="start-btn" type="button" data-testid="start-zoo-game" :disabled="careSubmitting" @click="startGame">
          开始照顾小猴子
        </button>
      </div>
      <div class="start-ground" aria-hidden="true"></div>
    </section>

    <section v-else-if="phase === 'playing'" class="zoo-stage" aria-label="动物园游戏场景">
      <div class="stage-topbar">
        <div class="stage-title">
          <span class="stage-title-icon">🌿</span>
          <div>
            <strong>小猴子的家</strong>
            <span>认真听听它的小愿望</span>
          </div>
        </div>
        <button class="restart-btn" type="button" :disabled="careSubmitting" @click="startGame">重新开始</button>
      </div>

      <div class="habitat">
        <div class="sun habitat-sun" aria-hidden="true">☀</div>
        <div class="cloud habitat-cloud" aria-hidden="true"></div>
        <div class="tree tree-left" aria-hidden="true">
          <span class="tree-crown">🌳</span>
        </div>
        <div class="tree tree-right" aria-hidden="true">
          <span class="tree-crown">🌳</span>
        </div>
        <div class="fence fence-left" aria-hidden="true"></div>
        <div class="fence fence-right" aria-hidden="true"></div>

        <div class="monkey-area">
          <div class="speech-bubble" :class="feedbackKind" aria-live="polite">
            {{ speech }}
          </div>
          <div class="monkey-shadow" aria-hidden="true"></div>
          <div class="monkey" :class="{ happy: isHappy }" aria-label="小猴子角色">
            <span>{{ isHappy ? '🙈' : '🐵' }}</span>
            <b v-if="isHappy" class="happy-badge">开心！</b>
          </div>
        </div>

        <div class="grass grass-one" aria-hidden="true">🌱</div>
        <div class="grass grass-two" aria-hidden="true">🌼</div>
        <div class="grass grass-three" aria-hidden="true">🌱</div>
      </div>

      <div class="tool-area">
        <div class="tool-heading">
          <div>
            <p class="eyebrow">照顾工具箱</p>
            <h2>选一个工具帮助小猴子</h2>
          </div>
          <p class="hint">提示：{{ currentTool.label }}可能派得上用场</p>
        </div>
        <p v-if="remainingCareToday !== null" class="care-remaining">
          今日还可照顾 {{ remainingCareToday }} 次
        </p>

        <div class="tool-grid">
          <button
            v-for="tool in tools"
            :key="tool.id"
            class="tool-card"
            :class="{ selected: feedbackKind === 'success' && tool.id === currentNeed }"
            :style="{ '--tool-color': tool.color }"
            :data-testid="`tool-${tool.id}`"
            type="button"
            :disabled="careSubmitting || feedbackKind === 'success'"
            @click="selectTool(tool)"
          >
            <span class="tool-emoji">{{ tool.emoji }}</span>
            <span class="tool-label">{{ tool.label }}</span>
            <span class="tool-action">点击使用</span>
          </button>
        </div>
      </div>
    </section>

    <section v-else class="result-screen" :class="phase" aria-live="polite">
      <div class="sun" aria-hidden="true">☀</div>
      <div class="cloud cloud-left" aria-hidden="true"></div>
      <div class="cloud cloud-right" aria-hidden="true"></div>
      <div class="result-card">
        <p class="start-kicker">{{ phase === 'won' ? '挑战完成' : '时间到了' }}</p>
        <div class="result-icon" aria-hidden="true">{{ phase === 'won' ? '🏆' : '⏰' }}</div>
        <h2>{{ phase === 'won' ? '照顾得真棒！' : '再试一次吧！' }}</h2>
        <p v-if="phase === 'won'">小猴子的每个愿望都被你照顾到了。</p>
        <p v-else>还差一点点，下次一定能照顾好小猴子。</p>
        <div class="result-score">
          <span>本局分数</span>
          <strong>{{ score }} / {{ targetScore }}</strong>
        </div>
        <div v-if="phase === 'won'" class="points-reward">
          <strong>账号积分 +{{ pointsAwarded }}</strong>
          <small>当前总积分：{{ accountTotalPoints }}</small>
        </div>
        <div v-if="phase === 'lost'" class="points-penalty">
          <span>未完成 {{ targetScore - score }} 颗爱心</span>
          <strong>账号积分 -{{ pointsDeducted }}</strong>
          <small>当前总积分：{{ accountTotalPoints }}</small>
        </div>
        <button class="start-btn" type="button" data-testid="restart-zoo-game" :disabled="careSubmitting" @click="startGame">
          重新开始挑战
        </button>
      </div>
      <div class="start-ground" aria-hidden="true"></div>
    </section>
  </div>
</template>

<style scoped>
.zoo-page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 0 0 24px;
}

.zoo-header {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 16px;
}

.zoo-header h1 {
  color: #3d4f46;
  font-size: 25px;
}

.eyebrow {
  color: #2f8f68;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 1.5px;
  margin-bottom: 3px;
  text-transform: uppercase;
}

.back-btn,
.restart-btn {
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  font-weight: 700;
}

.back-btn {
  background: #fff;
  color: #2f8f68;
  padding: 10px 14px;
  box-shadow: 0 4px 12px rgba(47, 143, 104, 0.12);
}

.game-stats {
  align-items: center;
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.stat-chip {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 12px rgba(47, 143, 104, 0.1);
  display: flex;
  flex-direction: column;
  min-width: 112px;
  padding: 8px 12px;
}

.stat-chip span {
  color: #83948b;
  font-size: 12px;
}

.stat-chip strong {
  color: #3f7659;
  font-size: 17px;
  margin-top: 2px;
}

.score-chip strong {
  color: #ff6f75;
}

.care-remaining {
  color: #2f8f68;
  font-size: 13px;
  font-weight: 700;
  margin: 0 0 14px;
}

.stat-chip.urgent {
  background: #fff1f1;
}

.stat-chip.urgent strong {
  color: #e65353;
}

.start-screen,
.result-screen,
.zoo-stage {
  background: #fff;
  border-radius: 28px;
  box-shadow: 0 14px 38px rgba(56, 112, 83, 0.16);
  overflow: hidden;
}

.start-screen {
  background: linear-gradient(180deg, #aee5ff 0%, #e7f9ff 62%, #81cb6d 62%, #58a950 100%);
  min-height: 630px;
  position: relative;
}

.result-screen {
  background: linear-gradient(180deg, #aee5ff 0%, #e7f9ff 62%, #81cb6d 62%, #58a950 100%);
  min-height: 630px;
  position: relative;
}

.result-screen.lost {
  background: linear-gradient(180deg, #c8d9e4 0%, #edf4f6 62%, #a6c98b 62%, #76a96c 100%);
}

.result-screen .sun {
  right: 52px;
  top: 44px;
}

.result-card {
  background: rgba(255, 255, 255, 0.94);
  border: 5px solid rgba(255, 255, 255, 0.7);
  border-radius: 30px;
  box-shadow: 0 16px 30px rgba(48, 119, 74, 0.2);
  left: 50%;
  padding: 32px 46px 40px;
  position: absolute;
  text-align: center;
  top: 50%;
  transform: translate(-50%, -51%);
  width: min(90%, 470px);
  z-index: 2;
}

.result-icon {
  font-size: 76px;
  line-height: 1.15;
  margin: 8px 0 2px;
}

.result-card h2 {
  color: #29744e;
  font-size: 34px;
}

.result-card p:not(.start-kicker) {
  color: #718078;
  font-size: 15px;
  line-height: 1.7;
  margin-top: 7px;
}

.result-score {
  background: #eff9f2;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  margin: 18px auto 22px;
  padding: 10px 18px;
  width: 160px;
}

.result-score span {
  color: #83948b;
  font-size: 12px;
}

.result-score strong {
  color: #ff6f75;
  font-size: 24px;
  margin-top: 2px;
}

.points-reward,
.points-penalty {
  background: #fff0f0;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  margin: -10px auto 22px;
  padding: 9px 14px;
  width: 190px;
}

.points-reward small,
.points-penalty span,
.points-penalty small {
  color: #a27474;
  font-size: 12px;
}

.points-reward {
  background: #eff9f2;
}

.points-reward strong {
  color: #2f8f68;
  font-size: 17px;
  margin: 3px 0;
}

.points-penalty strong {
  color: #df5d5d;
  font-size: 17px;
  margin: 3px 0;
}

.sun {
  color: #ffd24d;
  font-size: 76px;
  line-height: 1;
  position: absolute;
  text-shadow: 0 0 22px rgba(255, 210, 77, 0.6);
}

.start-screen .sun {
  right: 52px;
  top: 44px;
}

.cloud {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 999px;
  height: 28px;
  position: absolute;
  width: 108px;
}

.cloud::before,
.cloud::after {
  background: inherit;
  border-radius: 50%;
  content: '';
  position: absolute;
}

.cloud::before {
  height: 48px;
  left: 20px;
  top: -22px;
  width: 48px;
}

.cloud::after {
  height: 38px;
  right: 16px;
  top: -14px;
  width: 38px;
}

.cloud-left {
  left: 9%;
  top: 14%;
}

.cloud-right {
  right: 18%;
  top: 30%;
}

.start-card {
  background: rgba(255, 255, 255, 0.93);
  border: 5px solid rgba(255, 255, 255, 0.68);
  border-radius: 30px;
  box-shadow: 0 16px 30px rgba(48, 119, 74, 0.2);
  left: 50%;
  padding: 34px 46px 40px;
  position: absolute;
  text-align: center;
  top: 50%;
  transform: translate(-50%, -51%);
  width: min(90%, 470px);
  z-index: 2;
}

.start-kicker {
  color: #5e9e75;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 5px;
}

.start-card h2 {
  color: #29744e;
  font-size: 38px;
  margin: 4px 0 5px;
}

.start-card p:last-of-type {
  color: #718078;
  font-size: 15px;
  line-height: 1.8;
  margin-bottom: 20px;
}

.start-monkey {
  font-size: 94px;
  line-height: 1.15;
  margin: 5px 0;
}

.start-btn {
  background: linear-gradient(135deg, #ffb43e, #ff8b43);
  border: 0;
  border-radius: 999px;
  box-shadow: 0 8px 0 #df7030, 0 14px 20px rgba(225, 116, 54, 0.24);
  color: #fff;
  cursor: pointer;
  font-size: 17px;
  font-weight: 800;
  padding: 14px 28px;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.start-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 11px 0 #df7030, 0 17px 22px rgba(225, 116, 54, 0.2);
}

.start-ground {
  border-top: 8px solid rgba(255, 255, 255, 0.28);
  bottom: 58px;
  left: 0;
  position: absolute;
  width: 100%;
}

.stage-topbar {
  align-items: center;
  background: #fffdf7;
  display: flex;
  justify-content: space-between;
  padding: 14px 20px;
}

.stage-title {
  align-items: center;
  display: flex;
  gap: 10px;
}

.stage-title-icon {
  font-size: 28px;
}

.stage-title strong,
.stage-title span {
  display: block;
}

.stage-title strong {
  color: #3d654e;
  font-size: 15px;
}

.stage-title span {
  color: #97a59d;
  font-size: 11px;
  margin-top: 2px;
}

.restart-btn {
  background: #e8f7ee;
  color: #2f8f68;
  padding: 8px 14px;
}

.habitat {
  background: linear-gradient(180deg, #b8e8fa 0%, #e8f8f4 67%, #8cd173 67%, #69b55b 100%);
  height: 380px;
  overflow: hidden;
  position: relative;
}

.habitat-sun {
  font-size: 62px;
  right: 28px;
  top: 26px;
}

.habitat-cloud {
  left: 17%;
  top: 19%;
}

.tree {
  bottom: 36px;
  font-size: 132px;
  line-height: 1;
  position: absolute;
}

.tree-left {
  left: 2%;
}

.tree-right {
  right: 2%;
}

.fence {
  border-bottom: 8px solid #d79a58;
  border-top: 8px solid #d79a58;
  bottom: 52px;
  height: 49px;
  opacity: 0.9;
  position: absolute;
  width: 29%;
}

.fence::after {
  color: #c88646;
  content: '┃  ┃  ┃  ┃  ┃';
  font-size: 38px;
  left: 8px;
  letter-spacing: 4px;
  position: absolute;
  top: -21px;
}

.fence-left {
  left: 0;
}

.fence-right {
  right: 0;
}

.monkey-area {
  bottom: 34px;
  left: 50%;
  position: absolute;
  text-align: center;
  transform: translateX(-50%);
  z-index: 2;
}

.speech-bubble {
  background: #fff;
  border: 3px solid #55a775;
  border-radius: 20px;
  box-shadow: 0 6px 16px rgba(57, 109, 75, 0.12);
  color: #3d654e;
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 14px;
  min-width: 260px;
  padding: 13px 18px;
  position: relative;
}

.speech-bubble::after {
  background: #fff;
  border-bottom: 3px solid #55a775;
  border-right: 3px solid #55a775;
  bottom: -9px;
  content: '';
  height: 14px;
  left: 50%;
  position: absolute;
  transform: translateX(-50%) rotate(45deg);
  width: 14px;
}

.speech-bubble.success {
  border-color: #ffad43;
  color: #c57924;
}

.speech-bubble.success::after {
  border-color: #ffad43;
}

.speech-bubble.error {
  border-color: #ff7e79;
  color: #d95757;
}

.speech-bubble.error::after {
  border-color: #ff7e79;
}

.monkey {
  font-size: 120px;
  line-height: 1;
  position: relative;
  transition: transform 0.2s ease;
}

.monkey.happy {
  animation: happy-hop 0.42s ease-in-out infinite alternate;
}

.happy-badge {
  background: #ffb03d;
  border: 3px solid #fff;
  border-radius: 999px;
  color: #fff;
  font-size: 12px;
  padding: 5px 10px;
  position: absolute;
  right: -22px;
  top: 2px;
}

.monkey-shadow {
  background: rgba(61, 100, 69, 0.2);
  border-radius: 50%;
  bottom: -5px;
  height: 19px;
  left: 50%;
  position: absolute;
  transform: translateX(-50%);
  width: 126px;
}

.grass {
  bottom: 12px;
  font-size: 30px;
  position: absolute;
}

.grass-one {
  left: 36%;
}

.grass-two {
  left: 60%;
}

.grass-three {
  left: 72%;
}

.tool-area {
  background: #fffdf9;
  padding: 18px 22px 22px;
}

.tool-heading {
  align-items: end;
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}

.tool-heading h2 {
  color: #46574e;
  font-size: 18px;
}

.hint {
  color: #95a199;
  font-size: 12px;
}

.tool-grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(3, 1fr);
}

.tool-card {
  align-items: center;
  background: #fff;
  border: 2px solid #eef1ef;
  border-radius: 18px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  padding: 13px 12px 12px;
  transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.tool-card:hover {
  border-color: var(--tool-color);
  box-shadow: 0 7px 14px rgba(70, 87, 78, 0.1);
  transform: translateY(-4px);
}

.tool-card.selected {
  border-color: var(--tool-color);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--tool-color), transparent 78%);
}

.tool-emoji {
  font-size: 46px;
  line-height: 1.1;
}

.tool-label {
  color: #52625a;
  font-size: 14px;
  font-weight: 800;
  margin-top: 4px;
}

.tool-action {
  color: #a6b0aa;
  font-size: 11px;
  margin-top: 3px;
}

@keyframes happy-hop {
  from {
    transform: translateY(0) rotate(-3deg);
  }

  to {
    transform: translateY(-12px) rotate(3deg);
  }
}

@media (max-width: 700px) {
  .zoo-header {
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .game-stats {
    flex-wrap: wrap;
    margin-left: 0;
    width: 100%;
  }

  .stat-chip {
    flex: 1;
  }

  .start-screen,
  .result-screen {
    min-height: 560px;
  }

  .habitat {
    height: 340px;
  }

  .tree {
    font-size: 96px;
  }

  .fence {
    width: 25%;
  }

  .speech-bubble {
    font-size: 14px;
    min-width: 230px;
  }

  .monkey {
    font-size: 102px;
  }

  .tool-heading {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .tool-grid {
    gap: 8px;
  }

  .tool-card {
    padding: 11px 5px;
  }

  .tool-emoji {
    font-size: 38px;
  }

  .tool-label {
    font-size: 12px;
  }
}
</style>
