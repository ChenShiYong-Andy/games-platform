<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getData, postData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { GomokuGame, WaitingRoom } from '@/types'
import gameWinGif from '@/assets/game_win.gif'
import gameLoseGif from '@/assets/game-lose.gif'
import { copyText } from '@/utils/clipboard'

const router = useRouter()
const authStore = useAuthStore()
const game = ref<GomokuGame | null>(null)
const waitingRooms = ref<WaitingRoom[]>([])
const loading = ref(false)
const roomsLoading = ref(false)
const showResultAnimation = ref(false)
const resultCountdown = ref(3)
let pollTimer: number | null = null
let roomPollTimer: number | null = null
let resultTimer: number | null = null
let polling = false

const isPlaying = computed(() => game.value?.status === 'IN_PROGRESS')
const isFinished = computed(() => ['BLACK_WON', 'WHITE_WON', 'DRAW', 'CANCELLED'].includes(game.value?.status || ''))
const didWin = computed(() => {
  const current = game.value
  return !!current && current.status === `${current.myColor}_WON`
})
const didLose = computed(() => !!game.value?.status.endsWith('_WON') && !didWin.value)
const winningCells = computed(() => {
  const current = game.value
  if (!current?.status.endsWith('_WON') || current.finishReason !== 'NORMAL') return []
  const stone = current.status === 'BLACK_WON' ? 1 : 2
  const directions = [[0, 1], [1, 0], [1, 1], [1, -1]]
  for (let row = 0; row < current.board.length; row++) {
    for (let col = 0; col < current.board[row].length; col++) {
      if (current.board[row][col] !== stone) continue
      for (const [dr, dc] of directions) {
        const previousRow = row - dr
        const previousCol = col - dc
        if (current.board[previousRow]?.[previousCol] === stone) continue
        const run: Array<{ row: number; col: number }> = []
        for (let r = row, c = col; current.board[r]?.[c] === stone; r += dr, c += dc) {
          run.push({ row: r, col: c })
        }
        if (run.length >= 5) {
          const lastIndex = run.findIndex(cell => cell.row === current.lastMoveRow && cell.col === current.lastMoveCol)
          const start = lastIndex >= 0 ? Math.min(Math.max(lastIndex - 4, 0), run.length - 5) : 0
          return run.slice(start, start + 5)
        }
      }
    }
  }
  return []
})
const winningCellKeys = computed(() => new Set(winningCells.value.map(cell => `${cell.row}-${cell.col}`)))
const winningLineStyle = computed(() => {
  if (winningCells.value.length !== 5) return undefined
  const first = winningCells.value[0]
  const last = winningCells.value[4]
  const rowDistance = last.row - first.row
  const colDistance = last.col - first.col
  return {
    left: `calc(14px + var(--cell) * ${first.col + 0.5})`,
    top: `calc(14px + var(--cell) * ${first.row + 0.5})`,
    width: `calc(var(--cell) * ${Math.hypot(rowDistance, colDistance)})`,
    transform: `translateY(-50%) rotate(${Math.atan2(rowDistance, colDistance) * 180 / Math.PI}deg)`
  }
})
const statusText = computed(() => {
  if (!game.value) return ''
  if (game.value.status === 'WAITING') return '等待好友加入…'
  if (game.value.status === 'DRAW') return '棋盘已满，本局平局，双方各得 5 积分'
  if (game.value.status === 'CANCELLED') return '房间已取消'
  if (game.value.status.endsWith('_WON')) {
    if (game.value.finishReason === 'SURRENDER') {
      return didWin.value ? '对方已认输，你赢了（本局不计积分）' : '你已认输（本局不计积分）'
    }
    return didWin.value ? '你赢了！获得 10 积分' : '你输了，获得 5 积分'
  }
  return game.value.myTurn ? '轮到你落子' : '等待对方落子…'
})

async function createRoom() {
  loading.value = true
  try {
    setGame(await postData<GomokuGame>('/gomoku/rooms'))
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

async function createAiGame() {
  loading.value = true
  try {
    setGame(await postData<GomokuGame>('/gomoku/ai-games'))
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

async function loadWaitingRooms() {
  if (game.value || roomsLoading.value) return
  roomsLoading.value = true
  try {
    waitingRooms.value = await getData<WaitingRoom[]>('/gomoku/rooms/waiting')
  } catch {
    // 列表会定时重试，避免频繁提示。
  } finally {
    roomsLoading.value = false
  }
}

async function joinRoom(code: string) {
  loading.value = true
  try {
    setGame(await postData<GomokuGame>('/gomoku/rooms/join', { roomCode: code }))
  } catch (error) {
    showError(error)
    await loadWaitingRooms()
  } finally {
    loading.value = false
  }
}

async function placeStone(row: number, col: number) {
  if (!game.value || !game.value.myTurn || game.value.board[row][col] !== 0 || loading.value) return
  loading.value = true
  try {
    game.value = await postData<GomokuGame>(`/gomoku/games/${game.value.id}/moves`, { row, col })
    if (isFinished.value) await authStore.refreshProfile()
  } catch (error) {
    showError(error)
    await pollGame()
  } finally {
    loading.value = false
  }
}

async function surrender() {
  if (!game.value) return
  const waiting = game.value.status === 'WAITING'
  try {
    await ElMessageBox.confirm(waiting ? '确定取消这个房间吗？' : '认输后对方将获胜，确定继续吗？', waiting ? '取消房间' : '确认认输', {
      confirmButtonText: '确定', cancelButtonText: '再想想', type: 'warning'
    })
    game.value = await postData<GomokuGame>(`/gomoku/games/${game.value.id}/surrender`)
    localStorage.removeItem('gomokuGameId')
    if (!waiting) await authStore.refreshProfile()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') showError(error)
  }
}

async function pollGame() {
  if (!game.value || isFinished.value || polling) return
  polling = true
  try {
    const previousStatus = game.value.status
    const latest = await getData<GomokuGame>(`/gomoku/games/${game.value.id}`)
    game.value = latest
    if (previousStatus === 'IN_PROGRESS' && latest.status !== 'IN_PROGRESS') await authStore.refreshProfile()
    if (isFinished.value) localStorage.removeItem('gomokuGameId')
  } catch {
    // 临时网络错误留到下一轮重试，避免轮询消息打扰玩家。
  } finally {
    polling = false
  }
}

function setGame(value: GomokuGame) {
  game.value = value
  localStorage.setItem('gomokuGameId', String(value.id))
}

function resetGame() {
  game.value = null
  localStorage.removeItem('gomokuGameId')
  void loadWaitingRooms()
}

async function copyRoomCode() {
  if (!game.value) return
  try {
    await copyText(game.value.roomCode)
    ElMessage.success(`房间码 ${game.value.roomCode} 已复制`)
  } catch {
    ElMessage.error(`复制失败，请手动复制房间码：${game.value.roomCode}`)
  }
}

function showError(error: unknown) {
  ElMessage.error(error instanceof Error ? error.message : '操作失败')
}

function setScreenLocked(locked: boolean) {
  if (locked) window.scrollTo({ top: 0 })
  document.documentElement.classList.toggle('game-screen-locked', locked)
}

function stopResultAnimation() {
  if (resultTimer !== null) window.clearInterval(resultTimer)
  resultTimer = null
  showResultAnimation.value = false
}

function startResultAnimation() {
  stopResultAnimation()
  resultCountdown.value = 3
  showResultAnimation.value = true
  resultTimer = window.setInterval(() => {
    resultCountdown.value -= 1
    if (resultCountdown.value <= 0) stopResultAnimation()
  }, 1000)
}

watch(isPlaying, setScreenLocked, { flush: 'post' })
watch(() => game.value?.status, (status, previousStatus) => {
  if (status?.endsWith('_WON') && status !== previousStatus) startResultAnimation()
  else if (!status?.endsWith('_WON')) stopResultAnimation()
})

onMounted(async () => {
  const savedId = localStorage.getItem('gomokuGameId')
  try {
    if (savedId) {
      game.value = await getData<GomokuGame>(`/gomoku/games/${savedId}`)
    } else {
      const active = await getData<GomokuGame | null>('/gomoku/games/active')
      if (active) setGame(active)
    }
  } catch {
    localStorage.removeItem('gomokuGameId')
  }
  if (!game.value) await loadWaitingRooms()
  pollTimer = window.setInterval(pollGame, 1500)
  roomPollTimer = window.setInterval(loadWaitingRooms, 5000)
})

onBeforeUnmount(() => {
  stopResultAnimation()
  setScreenLocked(false)
  if (pollTimer !== null) window.clearInterval(pollTimer)
  if (roomPollTimer !== null) window.clearInterval(roomPollTimer)
})
</script>

<template>
  <div
    class="page-container gomoku-page"
    :class="{ 'game-active': game && game.status !== 'WAITING' }"
  >
    <button class="back-btn" @click="router.push('/')">← 返回游戏大厅</button>

    <header class="game-header">
      <div>
        <h1>⚫ 五子棋</h1>
        <p>支持好友对弈与人机对局 · 胜者 +10 积分 · 败者 +5 积分</p>
      </div>
      <button v-if="game && isFinished" class="primary-btn" @click="resetGame">再来一局</button>
    </header>

    <section v-if="!game" class="lobby card">
      <div class="lobby-option ai-option">
        <span class="option-icon">🤖</span>
        <h2>人机对局</h2>
        <p>立即挑战电脑，开局随机分配黑白方。</p>
        <button class="primary-btn" :disabled="loading" @click="createAiGame">开始人机对局</button>
      </div>
      <div class="divider"><span>或</span></div>
      <div class="lobby-option">
        <span class="option-icon">🏠</span>
        <h2>创建房间</h2>
        <p>生成房间码并分享给好友，开始时随机分配黑白方。</p>
        <button class="primary-btn" :disabled="loading" @click="createRoom">创建邀请房间</button>
      </div>
      <div class="divider"><span>或</span></div>
      <div class="lobby-option">
        <span class="option-icon">🚪</span>
        <h2>等待中的房间</h2>
        <p>选择一个等待中的房间加入，开始时随机分配黑白方。</p>
        <div class="waiting-list" :class="{ loading: roomsLoading }">
          <div v-for="room in waitingRooms" :key="room.id" class="waiting-room">
            <div><strong>{{ room.hostName }}（{{ room.hostUsername }}）</strong><small>房间码 {{ room.roomCode }}</small></div>
            <button class="join-btn" :disabled="loading" @click="joinRoom(room.roomCode)">加入</button>
          </div>
          <div v-if="!roomsLoading && waitingRooms.length === 0" class="rooms-empty">暂无等待中的房间</div>
        </div>
        <button class="refresh-btn" :disabled="roomsLoading" @click="loadWaitingRooms">↻ 刷新列表</button>
      </div>
    </section>

    <template v-else>
      <section class="match-card card">
        <div class="player" :class="{ active: game.currentPlayerId === game.blackPlayerId && isPlaying }">
          <span class="stone black"></span>
          <div><strong>{{ game.blackPlayerName }}</strong><small>{{ game.status === 'WAITING' ? '你 · 待分配' : (game.myColor === 'BLACK' ? '你 · 黑棋' : '黑棋') }}</small></div>
        </div>
        <div class="match-center">
          <button v-if="game.status === 'WAITING'" class="room-code" @click="copyRoomCode">
            房间码 <strong>{{ game.roomCode }}</strong> <small>点击复制</small>
          </button>
          <strong class="status" :class="{ turn: game.myTurn }">{{ statusText }}</strong>
        </div>
        <div class="player white-player" :class="{ active: game.currentPlayerId === game.whitePlayerId && isPlaying }">
          <span class="stone white"></span>
          <div><strong>{{ game.whitePlayerName || '等待加入' }}</strong><small>{{ game.myColor === 'WHITE' ? '你 · 白棋' : '白棋' }}</small></div>
        </div>
      </section>

      <div v-if="game.status !== 'WAITING'" class="board-wrap">
        <div class="board-stage">
          <div class="board" :class="{ disabled: !game.myTurn }">
            <template v-for="(row, rowIndex) in game.board" :key="rowIndex">
              <button
                v-for="(cell, colIndex) in row"
                :key="`${rowIndex}-${colIndex}`"
                class="intersection"
                :class="{
                  playable: game.myTurn && cell === 0,
                  last: game.lastMoveRow === rowIndex && game.lastMoveCol === colIndex,
                  winning: winningCellKeys.has(`${rowIndex}-${colIndex}`)
                }"
                :aria-label="`第 ${rowIndex + 1} 行第 ${colIndex + 1} 列`"
                @click="placeStone(rowIndex, colIndex)"
              >
                <span v-if="cell" class="stone board-stone" :class="cell === 1 ? 'black' : 'white'"></span>
              </button>
            </template>
            <span v-if="winningLineStyle" class="winning-line" :style="winningLineStyle"></span>
          </div>
          <div v-if="showResultAnimation && didWin" class="result-animation" role="status" aria-label="恭喜获胜" @click="stopResultAnimation">
            <div class="result-panel">
              <div class="result-countdown">{{ resultCountdown }} 秒后自动关闭</div>
              <img :src="gameWinGif" alt="恭喜获胜动画">
            </div>
          </div>
          <div v-else-if="showResultAnimation && didLose" class="result-animation" role="status" aria-label="本局失败" @click="stopResultAnimation">
            <div class="result-panel">
              <div class="result-countdown">{{ resultCountdown }} 秒后自动关闭</div>
              <img :src="gameLoseGif" alt="本局失败动画">
            </div>
          </div>
        </div>
      </div>

      <div class="actions">
        <button v-if="!isFinished" class="danger-btn" @click="surrender">{{ game.status === 'WAITING' ? '取消房间' : '认输' }}</button>
        <span v-if="isPlaying">已落 {{ game.moveCount }} 手</span>
      </div>
    </template>
  </div>
</template>

<style scoped>
.gomoku-page { max-width: 980px; }
.gomoku-page.game-active { --gomoku-cell: clamp(23px, calc((100dvh - 300px) / 15), 39px); height: calc(100dvh - 112px); padding-top: 0; padding-bottom: 0; display: grid; grid-template-areas: 'back back' 'header header' 'board match' 'actions .'; grid-template-columns: max-content minmax(220px, 270px); grid-template-rows: auto auto minmax(0, 1fr) auto; justify-content: center; column-gap: 12px; overflow: hidden; }
.game-active .back-btn { grid-area: back; justify-self: start; margin-bottom: 6px; }
.game-active .game-header { grid-area: header; margin-bottom: 10px; }
.game-active .match-card { grid-area: match; align-self: center; height: calc(var(--gomoku-cell) * 15 + 28px); margin: 0; padding: 22px 18px; display: flex; flex-direction: column; justify-content: center; gap: 34px; }
.game-active .match-card .player { width: 100%; }
.game-active .match-card .white-player { justify-content: flex-start; text-align: left; }
.game-active .match-center { width: 100%; padding: 18px 8px; border-top: 1px solid #eee7dd; border-bottom: 1px solid #eee7dd; }
.game-active .board-wrap { grid-area: board; min-width: 0; min-height: 0; padding: 4px 0; display: flex; align-items: center; justify-content: center; }
.game-active .board { --cell: var(--gomoku-cell); }
.game-active .actions { grid-area: actions; margin: 6px 0 0; }
.back-btn { border: 0; background: none; color: #8b5e34; cursor: pointer; margin-bottom: 18px; font-weight: 600; }
.game-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; gap: 16px; }
.game-header h1 { font-size: 30px; margin-bottom: 6px; }
.game-header p, .lobby-option p { color: #777; }
.lobby { display: grid; grid-template-columns: 1fr auto 1fr auto 1fr; align-items: stretch; gap: 16px; padding: 34px 24px; }
.lobby-option { text-align: center; padding: 10px 12px; }
.option-icon { font-size: 44px; }
.lobby-option h2 { margin: 12px 0 8px; }
.lobby-option p { min-height: 44px; margin-bottom: 22px; line-height: 1.55; }
.divider { width: 1px; background: #eee; position: relative; }
.divider span { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: white; color: #aaa; padding: 10px 0; }
.primary-btn, .danger-btn { border: 0; border-radius: 9px; padding: 11px 20px; cursor: pointer; color: white; font-weight: 600; font-size: 14px; }
.primary-btn { background: #9b6838; }
.primary-btn:hover { background: #805329; }
.primary-btn:disabled { opacity: .55; cursor: wait; }
.danger-btn { background: #fff; color: #d65b5b; border: 1px solid #efb5b5; }
.waiting-list { min-height: 94px;max-height:210px;overflow:auto;display:flex;flex-direction:column;gap:8px;text-align:left; }.waiting-list.loading { opacity:.6; }.waiting-room { display:flex;align-items:center;justify-content:space-between;gap:10px;padding:9px 10px;border:1px solid #eadfce;border-radius:9px;background:#fffaf2; }.waiting-room strong { display:block;color:#5b4635;font-size:14px; }.waiting-room small { display:block;color:#a18a76;margin-top:2px; }.join-btn { border:0;border-radius:7px;padding:7px 12px;background:#9b6838;color:#fff;cursor:pointer;font-weight:600; }.rooms-empty { display:grid;place-items:center;min-height:90px;color:#aaa;font-size:13px; }.refresh-btn { border:0;background:none;color:#9b6838;cursor:pointer;margin-top:10px; }
.match-card { display: grid; grid-template-columns: 1fr 1.25fr 1fr; align-items: center; margin-bottom: 18px; padding: 16px 22px; }
.player { display: flex; align-items: center; gap: 10px; opacity: .65; }
.player.active { opacity: 1; color: #8b5e34; }
.white-player { justify-content: flex-end; text-align: right; }
.player small { display: block; color: #999; margin-top: 2px; }
.stone { width: 26px; height: 26px; flex: 0 0 auto; border-radius: 50%; display: inline-block; }
.black { background: radial-gradient(circle at 35% 30%, #666, #090909 65%); box-shadow: 1px 2px 3px rgba(0,0,0,.35); }
.white { background: radial-gradient(circle at 35% 30%, #fff, #ddd 70%); box-shadow: 1px 2px 4px rgba(0,0,0,.35); }
.match-center { text-align: center; display: flex; flex-direction: column; gap: 8px; }
.room-code { border: 0; background: #fff6df; color: #805329; border-radius: 8px; padding: 8px; cursor: pointer; }
.room-code strong { letter-spacing: 3px; font-size: 18px; margin: 0 4px; }
.room-code small { color: #ad8a68; }
.status { font-size: 15px; }
.status.turn { color: #c46f1a; }
.board-wrap { overflow: auto; padding: 8px 0 12px; }
.board-stage { position: relative; width: max-content; margin: auto; }
.board { --cell: 35px; width: calc(var(--cell) * 15 + 28px); height: calc(var(--cell) * 15 + 28px); padding: 14px; display: grid; grid-template-columns: repeat(15, var(--cell)); grid-template-rows: repeat(15, var(--cell)); background-color: #d8a35c; background-image: linear-gradient(rgba(74,45,18,.72) 1px, transparent 1px), linear-gradient(90deg, rgba(74,45,18,.72) 1px, transparent 1px); background-size: var(--cell) var(--cell); background-position: calc(14px + var(--cell) / 2) calc(14px + var(--cell) / 2); border: 2px solid #895b2c; border-radius: 5px; box-shadow: 0 8px 22px rgba(81,48,17,.28); }
.result-animation { position: absolute; inset: 14px; z-index: 5; display: grid; place-items: center; cursor: pointer; animation: win-pop .38s cubic-bezier(.2, 1.35, .45, 1) both; }
.result-panel { position: relative; width: min(88%, 498px); max-height: 94%; display: flex; flex-direction: column; align-items: center; }
.result-countdown { position: relative; z-index: 1; margin-bottom: 8px; padding: 6px 14px; border-radius: 999px; background: rgba(35, 31, 25, .82); color: #fff; font-size: 14px; font-weight: 700; box-shadow: 0 4px 14px rgba(0,0,0,.2); }
.result-panel img { display: block; width: 100%; min-height: 0; object-fit: contain; border-radius: 10px; box-shadow: 0 12px 34px rgba(58, 35, 11, .3); }
@keyframes win-pop { from { opacity: 0; transform: scale(.55); } to { opacity: 1; transform: scale(1); } }
.intersection { width: var(--cell); height: var(--cell); border: 0; padding: 0; background: transparent; display: grid; place-items: center; cursor: default; }
.intersection.playable { cursor: pointer; }
.intersection.playable:hover::after { content: ''; width: 12px; height: 12px; border-radius: 50%; background: rgba(60, 35, 15, .22); }
.intersection.last .board-stone { outline: 3px solid #ffcf32; outline-offset: 3px; box-shadow: 0 0 0 5px rgba(255, 243, 145, .72), 0 0 14px rgba(255, 180, 0, .9); }
.intersection.last::before { content: ''; position: absolute; left: 50%; top: 50%; z-index: 2; width: 7px; height: 7px; border-radius: 50%; background: #e44732; box-shadow: 0 0 0 2px rgba(255,255,255,.85); transform: translate(-50%,-50%); pointer-events: none; }
.intersection { position: relative; }
.intersection.winning { z-index: 3; }
.intersection.winning .board-stone { outline: 3px solid #58d36b; outline-offset: 3px; box-shadow: 0 0 0 6px rgba(200,255,144,.8), 0 0 18px rgba(43,196,75,.95); animation: winning-stone 1s ease-in-out infinite alternate; }
.winning-line { position: absolute; z-index: 2; height: 7px; border-radius: 999px; transform-origin: left center; background: linear-gradient(90deg,#f5d941,#62dc70); box-shadow: 0 0 9px rgba(76,216,99,.95); pointer-events: none; animation: winning-line 1s ease-in-out infinite alternate; }
@keyframes winning-stone { to { filter: brightness(1.18); transform: scale(1.08); } }
@keyframes winning-line { to { opacity: .66; box-shadow: 0 0 16px rgba(76,216,99,1); } }
.board-stone { width: clamp(17px, calc(var(--cell) - 6px), 29px); height: clamp(17px, calc(var(--cell) - 6px), 29px); }
.actions { display: flex; justify-content: center; align-items: center; gap: 20px; color: #999; margin: 12px 0; }
@media (max-width: 820px) {
  .gomoku-page.game-active { grid-template-areas: 'back' 'header' 'match' 'board' 'actions'; grid-template-columns: minmax(0, 1fr); grid-template-rows: auto auto auto minmax(0, 1fr) auto; justify-content: stretch; column-gap: 0; }
  .game-active .match-card { height: auto; min-height: 0; margin-bottom: 4px; padding: 8px 14px; display: grid; grid-template-columns: 1fr 1.25fr 1fr; gap: 12px; }
  .game-active .match-card .white-player { justify-content: flex-end; text-align: right; }
  .game-active .match-center { width: auto; padding: 0; border: 0; }
}
@media (max-width: 680px) {
  .lobby { grid-template-columns: 1fr; padding: 24px 16px; gap: 14px; }
  .divider { width: 100%; height: 1px; }
  .divider span { padding: 0 10px; }
  .match-card { grid-template-columns: 1fr 1fr; gap: 12px; }
  .match-center { grid-column: 1 / -1; grid-row: 2; }
  .board { --cell: 25px; }
  .board-stone { width: 22px; height: 22px; }
  .game-header { align-items: flex-start; }
  .game-header h1 { font-size: 25px; }
  .game-active .game-header p { display: none; }
  .game-active .board { --cell: clamp(21px, calc((100dvh - 340px) / 15), 27px); }
}
@media (max-height: 800px) {
  .game-active .game-header p { display: none; }
  .game-active .game-header h1 { font-size: 25px; }
  .game-active .back-btn { margin-bottom: 2px; }
  .game-active .game-header { margin-bottom: 4px; }
  .game-active .match-card { margin-bottom: 3px; padding-top: 7px; padding-bottom: 7px; }
  .game-active .board-wrap { padding: 0; }
  .game-active .actions { margin-top: 2px; }
}
</style>
