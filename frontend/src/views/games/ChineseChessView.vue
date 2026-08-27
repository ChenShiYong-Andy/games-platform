<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getData, postData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type { ChineseChessGame, WaitingRoom } from '@/types'
import { copyText } from '@/utils/clipboard'
import gameWinGif from '@/assets/game_win.gif'
import gameLoseGif from '@/assets/game-lose.gif'
import chessCheckGif from '@/assets/chess_check.gif'

const router = useRouter()
const authStore = useAuthStore()
const game = ref<ChineseChessGame | null>(null)
const waitingRooms = ref<WaitingRoom[]>([])
const loading = ref(false)
const roomsLoading = ref(false)
const selected = ref<{ row: number; col: number } | null>(null)
let pollTimer: number | null = null
let roomPollTimer: number | null = null
let polling = false

const isPlaying = computed(() => game.value?.status === 'IN_PROGRESS')
const isFinished = computed(() => ['RED_WON', 'BLACK_WON', 'CANCELLED'].includes(game.value?.status || ''))
const didWin = computed(() => game.value?.winnerId === authStore.user?.id)
const didLose = computed(() => authStore.user?.id != null
  && game.value?.winnerId != null && game.value.winnerId !== authStore.user.id)
const statusText = computed(() => {
  if (!game.value) return ''
  if (game.value.status === 'WAITING') return '等待好友加入…'
  if (game.value.status === 'CANCELLED') return '房间已取消'
  if (game.value.status.endsWith('_WON')) {
    if (game.value.finishReason === 'SURRENDER') {
      return game.value.winnerId === authStore.user?.id ? '对方已认输，你赢了（本局不计积分）' : '你已认输（本局不计积分）'
    }
    return game.value.winnerId === authStore.user?.id ? '你赢了！获得 10 积分' : '你输了，获得 5 积分'
  }
  if (game.value.inCheck) {
    return game.value.myTurn ? '你被将军了，请先解将' : '将军！等待对方解将…'
  }
  return game.value.myTurn ? '轮到你走棋' : '等待对方走棋…'
})

const pieceNames: Record<number, string> = {
  1: '車', 2: '馬', 3: '相', 4: '仕', 5: '帥', 6: '炮', 7: '兵',
  [-1]: '車', [-2]: '馬', [-3]: '象', [-4]: '士', [-5]: '將', [-6]: '砲', [-7]: '卒'
}

const pieceGuides: Record<number, { move: string; limit: string; capture: string }> = {
  1: { move: '沿横线或竖线直走，格数不限。', limit: '行进路线中不能有其他棋子阻挡。', capture: '可吃掉直线路径上遇到的第一个敌方棋子。' },
  2: { move: '走“日”字：横二竖一，或竖二横一。', limit: '紧邻方向有棋子时会“蹩马腿”，该方向不能走。', capture: '落点为敌方棋子时可以吃子。' },
  3: { move: '沿对角线走两格，俗称“飞田”。', limit: '不能过河；田字中心有棋子时会“塞象眼”。', capture: '落点为敌方棋子时可以吃子。' },
  4: { move: '沿对角线走一格。', limit: '只能在己方九宫格内活动。', capture: '落点为敌方棋子时可以吃子。' },
  5: { move: '每次沿横线或竖线走一格。', limit: '只能在九宫格内活动，且不能与对方将帅直接照面。', capture: '同一路径无遮挡时可“飞将”吃掉对方将帅。' },
  6: { move: '不吃子时与车相同，沿直线移动任意格。', limit: '普通移动路径不能有棋子阻挡。', capture: '吃子时必须隔着恰好一个棋子作为“炮架”。' },
  7: { move: '未过河只能向前一格；过河后还可向左或向右一格。', limit: '任何时候都不能后退。', capture: '走到敌方棋子所在位置即可吃子。' }
}

const selectedPieceGuide = computed(() => {
  if (!game.value || !selected.value) return null
  const piece = game.value.board[selected.value.row][selected.value.col]
  if (!piece) return null
  return {
    name: pieceNames[piece],
    side: piece > 0 ? '红方' : '黑方',
    guide: pieceGuides[Math.abs(piece)]
  }
})

async function createRoom() {
  loading.value = true
  try {
    setGame(await postData<ChineseChessGame>('/chess/rooms'))
  } catch (error) { showError(error) } finally { loading.value = false }
}

async function loadWaitingRooms() {
  if (game.value || roomsLoading.value) return
  roomsLoading.value = true
  try {
    waitingRooms.value = await getData<WaitingRoom[]>('/chess/rooms/waiting')
  } catch {
    // 列表会定时重试，避免频繁提示。
  } finally {
    roomsLoading.value = false
  }
}

async function joinRoom(code: string) {
  loading.value = true
  try {
    setGame(await postData<ChineseChessGame>('/chess/rooms/join', { roomCode: code }))
  } catch (error) {
    showError(error)
    await loadWaitingRooms()
  } finally { loading.value = false }
}

function isMine(piece: number) {
  return game.value?.myColor === 'RED' ? piece > 0 : piece < 0
}

async function clickPosition(row: number, col: number) {
  if (!game.value?.myTurn || loading.value) return
  const piece = game.value.board[row][col]
  if (!selected.value) {
    if (piece && isMine(piece)) selected.value = { row, col }
    return
  }
  if (selected.value.row === row && selected.value.col === col) {
    selected.value = null
    return
  }
  if (piece && isMine(piece)) {
    selected.value = { row, col }
    return
  }

  loading.value = true
  const from = selected.value
  try {
    game.value = await postData<ChineseChessGame>(`/chess/games/${game.value.id}/moves`, {
      fromRow: from.row, fromCol: from.col, toRow: row, toCol: col
    })
    selected.value = null
    if (isFinished.value) await authStore.refreshProfile()
  } catch (error) {
    if (!(error instanceof Error && error.message === '该走法不符合象棋规则')) showError(error)
  } finally { loading.value = false }
}

async function surrender() {
  if (!game.value) return
  const waiting = game.value.status === 'WAITING'
  try {
    await ElMessageBox.confirm(waiting ? '确定取消这个房间吗？' : '认输后对方将获胜，确定继续吗？', waiting ? '取消房间' : '确认认输', {
      confirmButtonText: '确定', cancelButtonText: '再想想', type: 'warning'
    })
    game.value = await postData<ChineseChessGame>(`/chess/games/${game.value.id}/surrender`)
    localStorage.removeItem('chessGameId')
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
    const previousMoves = game.value.moveCount
    const latest = await getData<ChineseChessGame>(`/chess/games/${game.value.id}`)
    game.value = latest
    if (latest.moveCount !== previousMoves) selected.value = null
    if (previousStatus === 'IN_PROGRESS' && latest.status !== 'IN_PROGRESS') await authStore.refreshProfile()
    if (isFinished.value) localStorage.removeItem('chessGameId')
  } catch { /* 下一轮自动重试。 */ } finally { polling = false }
}

function setGame(value: ChineseChessGame) {
  game.value = value
  localStorage.setItem('chessGameId', String(value.id))
}

function resetGame() {
  game.value = null
  selected.value = null
  localStorage.removeItem('chessGameId')
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

function cellClass(row: number, col: number, piece: number) {
  return {
    selected: selected.value?.row === row && selected.value?.col === col,
    last: (game.value?.lastFromRow === row && game.value?.lastFromCol === col)
      || (game.value?.lastToRow === row && game.value?.lastToCol === col),
    selectable: game.value?.myTurn && piece !== 0 && isMine(piece)
  }
}

function showError(error: unknown) {
  ElMessage.error(error instanceof Error ? error.message : '操作失败')
}

onMounted(async () => {
  const savedId = localStorage.getItem('chessGameId')
  try {
    if (savedId) game.value = await getData<ChineseChessGame>(`/chess/games/${savedId}`)
    else {
      const active = await getData<ChineseChessGame | null>('/chess/games/active')
      if (active) setGame(active)
    }
  } catch { localStorage.removeItem('chessGameId') }
  if (!game.value) await loadWaitingRooms()
  pollTimer = window.setInterval(pollGame, 1500)
  roomPollTimer = window.setInterval(loadWaitingRooms, 5000)
})

onBeforeUnmount(() => {
  if (pollTimer !== null) window.clearInterval(pollTimer)
  if (roomPollTimer !== null) window.clearInterval(roomPollTimer)
})
</script>

<template>
  <div class="page-container chess-page">
    <button class="back-btn" @click="router.push('/')">← 返回游戏大厅</button>
    <header class="game-header">
      <div><h1>♟️ 中国象棋</h1><p>好友在线对弈 · 胜者 +10 积分 · 败者 +5 积分</p></div>
      <button v-if="game && isFinished" class="primary-btn" @click="resetGame">再来一局</button>
    </header>

    <section v-if="!game" class="lobby card">
      <div class="lobby-option"><span>🏠</span><h2>创建房间</h2><p>分享房间码给好友，开始时随机分配红黑方。</p><button class="primary-btn" :disabled="loading" @click="createRoom">创建邀请房间</button></div>
      <div class="divider"><span>或</span></div>
      <div class="lobby-option">
        <span>🚪</span><h2>等待中的房间</h2><p>选择一个等待中的房间加入，开始时随机分配红黑方。</p>
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
        <div class="player red-player" :class="{ active: game.currentPlayerId === game.redPlayerId && isPlaying }"><span class="mini-piece red">帥</span><div><strong>{{ game.redPlayerName }}</strong><small>{{ game.status === 'WAITING' ? '你 · 待分配' : (game.myColor === 'RED' ? '你 · 红方' : '红方') }}</small></div></div>
        <div class="match-center">
          <button v-if="game.status === 'WAITING'" class="room-code" @click="copyRoomCode">房间码 <strong>{{ game.roomCode }}</strong> <small>点击复制</small></button>
          <div class="status-group">
            <img v-if="game.inCheck" class="check-gif" :src="chessCheckGif" alt="将军提示">
            <strong class="status" :class="{ turn: game.myTurn }">{{ statusText }}</strong>
          </div>
        </div>
        <div class="player black-player" :class="{ active: game.currentPlayerId === game.blackPlayerId && isPlaying }"><div><strong>{{ game.blackPlayerName || '等待加入' }}</strong><small>{{ game.myColor === 'BLACK' ? '你 · 黑方' : '黑方' }}</small></div><span class="mini-piece black">將</span></div>
      </section>

      <div v-if="game.status !== 'WAITING'" class="play-area">
        <div class="board-wrap">
          <div class="chess-board">
            <div class="river"><span>楚 河</span><span>漢 界</span></div>
            <template v-for="(row, rowIndex) in game.board" :key="rowIndex">
              <button v-for="(piece, colIndex) in row" :key="`${rowIndex}-${colIndex}`" class="position" :class="cellClass(rowIndex, colIndex, piece)" :aria-label="`第${rowIndex + 1}行第${colIndex + 1}列${piece ? pieceNames[piece] : ''}`" @click="clickPosition(rowIndex, colIndex)">
                <span v-if="piece" class="piece" :class="piece > 0 ? 'red' : 'black'">{{ pieceNames[piece] }}</span>
              </button>
            </template>
            <div v-if="didWin" class="result-animation" role="status" aria-label="恭喜获胜">
              <img :src="gameWinGif" alt="恭喜获胜动画">
            </div>
            <div v-else-if="didLose" class="result-animation" role="status" aria-label="本局失败">
              <img :src="gameLoseGif" alt="本局失败动画">
            </div>
          </div>
        </div>

        <aside class="move-guide card">
          <h2>走子规则</h2>
          <template v-if="selectedPieceGuide">
            <div class="guide-piece">
              <span class="mini-piece" :class="selectedPieceGuide.side === '红方' ? 'red' : 'black'">{{ selectedPieceGuide.name }}</span>
              <div><strong>{{ selectedPieceGuide.name }}</strong><small>{{ selectedPieceGuide.side }}棋子</small></div>
            </div>
            <dl class="guide-list">
              <div><dt>怎么走</dt><dd>{{ selectedPieceGuide.guide.move }}</dd></div>
              <div><dt>走子限制</dt><dd>{{ selectedPieceGuide.guide.limit }}</dd></div>
              <div><dt>如何吃子</dt><dd>{{ selectedPieceGuide.guide.capture }}</dd></div>
            </dl>
            <p class="guide-tip">再次点击已选棋子可取消选择</p>
          </template>
          <div v-else class="guide-empty">
            <span>☝️</span>
            <strong>请选择一个棋子</strong>
            <p>选中后，这里会显示它下一步的走法和限制。</p>
          </div>
        </aside>
      </div>
      <div class="actions"><button v-if="!isFinished" class="danger-btn" @click="surrender">{{ game.status === 'WAITING' ? '取消房间' : '认输' }}</button><span v-if="isPlaying">已走 {{ game.moveCount }} 手</span></div>
    </template>
  </div>
</template>

<style scoped>
.chess-page { max-width: 980px; }
.back-btn { border: 0; background: none; color: #9a3427; cursor: pointer; margin-bottom: 18px; font-weight: 600; }
.game-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; gap: 16px; }
.game-header h1 { font-size: 30px; margin-bottom: 6px; }.game-header p,.lobby p { color: #777; }
.primary-btn,.danger-btn { border: 0; border-radius: 9px; padding: 11px 20px; cursor: pointer; color: #fff; font-weight: 600; }.primary-btn { background: #a63d2f; }.primary-btn:disabled { opacity: .55; }
.danger-btn { background: #fff; color: #c64a3b; border: 1px solid #e8aba3; }
.lobby { display: grid; grid-template-columns: 1fr auto 1fr; gap: 32px; padding: 42px; }.lobby-option { text-align: center; padding: 10px 24px; }.lobby-option>span { font-size: 44px; }.lobby h2 { margin: 12px 0 8px; }.lobby p { min-height: 44px; margin-bottom: 22px; }.divider { width: 1px; background: #eee; position: relative; }.divider span { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); background: #fff; color: #aaa; padding: 10px 0; }
.waiting-list { min-height:94px;max-height:210px;overflow:auto;display:flex;flex-direction:column;gap:8px;text-align:left; }.waiting-list.loading { opacity:.6; }.waiting-room { display:flex;align-items:center;justify-content:space-between;gap:10px;padding:9px 10px;border:1px solid #ecd9cd;border-radius:9px;background:#fff9f4; }.waiting-room strong { display:block;color:#5c4038;font-size:14px; }.waiting-room small { display:block;color:#a1847a;margin-top:2px; }.join-btn { border:0;border-radius:7px;padding:7px 12px;background:#a63d2f;color:#fff;cursor:pointer;font-weight:600; }.rooms-empty { display:grid;place-items:center;min-height:90px;color:#aaa;font-size:13px; }.refresh-btn { border:0;background:none;color:#a63d2f;cursor:pointer;margin-top:10px; }
.match-card { display: grid; grid-template-columns: 1fr 1.5fr 1fr; align-items: center; margin-bottom: 18px; padding: 12px 22px; min-height: 104px; }.player { display: flex; align-items: center; gap: 10px; opacity: .62; }.player.active { opacity: 1; }.black-player { justify-content: flex-end; text-align: right; }.player small { display:block;color:#999;margin-top:2px; }.mini-piece,.piece { display:grid;place-items:center;border-radius:50%;background:#f3d492;border:2px solid currentColor;font-family:STKaiti,KaiTi,serif;font-weight:800;box-shadow:1px 2px 4px rgba(50,20,5,.3); }.mini-piece { width:32px;height:32px; }.red { color:#b32222; }.black { color:#26211d; }
.match-center { text-align:center;display:flex;flex-direction:column;align-items:center;gap:8px; }.status-group { display:flex;align-items:center;justify-content:center;gap:12px; }.check-gif { display:block;width:72px;height:72px;flex:0 0 auto;object-fit:contain;border-radius:8px; }.room-code { border:0;background:#fff1df;color:#8e3024;border-radius:8px;padding:8px;cursor:pointer; }.room-code strong { letter-spacing:3px;font-size:18px;margin:0 4px; }.room-code small { color:#ad7868; }.status.turn { color:#b32222; }
.play-area { display:grid;grid-template-columns:auto 260px;gap:24px;align-items:stretch;justify-content:center; }.board-wrap { overflow:auto;padding:8px 0 14px; }.chess-board { --cell:54px; position:relative; width:calc(var(--cell) * 9 + 28px); height:calc(var(--cell) * 10 + 28px); margin:auto;padding:14px;display:grid;grid-template-columns:repeat(9,var(--cell));grid-template-rows:repeat(10,var(--cell));background-color:#d9a85f;background-image:linear-gradient(rgba(70,39,13,.7) 1px,transparent 1px),linear-gradient(90deg,rgba(70,39,13,.7) 1px,transparent 1px);background-size:var(--cell) var(--cell);background-position:calc(14px + var(--cell)/2) calc(14px + var(--cell)/2);border:3px solid #805021;border-radius:5px;box-shadow:0 8px 24px rgba(70,39,13,.28); }
.river { position:absolute;z-index:0;left:calc(14px + var(--cell)/2);right:calc(14px + var(--cell)/2);top:calc(14px + var(--cell)*4.5);height:var(--cell);display:flex;align-items:center;justify-content:space-around;background:#d9a85f;border-top:1px solid #694019;border-bottom:1px solid #694019;color:#643818;font:700 21px STKaiti,KaiTi,serif;letter-spacing:5px; }
.position { position:relative;z-index:1;width:var(--cell);height:var(--cell);border:0;background:transparent;padding:0;display:grid;place-items:center;cursor:default; }.position.selectable { cursor:pointer; }.position.last::after { content:'';position:absolute;width:12px;height:12px;border:3px solid #2666b2;border-radius:3px; }.position.selected::after { content:'';position:absolute;inset:2px;z-index:3;border:3px solid #ffd21f;border-radius:50%;box-shadow:0 0 0 3px rgba(255,246,148,.8),0 0 14px rgba(255,199,0,.85);pointer-events:none; }.piece { position:relative;z-index:2;width:44px;height:44px;font-size:25px;background:radial-gradient(circle at 35% 25%,#ffe4a9,#d4a05a 75%);transition:transform .16s ease,border-width .16s ease,background .16s ease,box-shadow .16s ease; }
.position.selected .piece { transform:scale(1.08);border-width:3px;background:radial-gradient(circle at 35% 25%,#fff7b2,#efbd54 72%);box-shadow:0 3px 8px rgba(56,31,8,.45),0 0 0 3px rgba(255,218,51,.75); }
.result-animation { position:absolute;inset:14px;z-index:5;display:grid;place-items:center;pointer-events:none;animation:result-pop .38s cubic-bezier(.2,1.35,.45,1) both; }.result-animation img { display:block;width:min(86%,480px);max-height:86%;object-fit:contain;border-radius:10px;box-shadow:0 12px 34px rgba(58,35,11,.3); }
@keyframes result-pop { from { opacity:0;transform:scale(.55) } to { opacity:1;transform:scale(1) } }
.move-guide { width:260px;min-height:100%;padding:22px;background:linear-gradient(165deg,#fffdf7,#fff8e8);border:1px solid #ead7b9; }.move-guide h2 { color:#713c24;font-size:19px;margin-bottom:20px;padding-bottom:12px;border-bottom:1px solid #ead7b9; }.guide-piece { display:flex;align-items:center;gap:12px;margin-bottom:20px; }.guide-piece .mini-piece { width:46px;height:46px;font-size:26px;flex:0 0 auto; }.guide-piece strong { display:block;font-size:20px; }.guide-piece small { display:block;color:#999;margin-top:3px; }.guide-list { display:flex;flex-direction:column;gap:16px; }.guide-list div { padding-left:12px;border-left:3px solid #d9a85f; }.guide-list dt { color:#8f392c;font-size:13px;font-weight:700;margin-bottom:5px; }.guide-list dd { color:#625b53;font-size:13px;line-height:1.65;margin:0; }.guide-tip { margin-top:22px;padding:9px 10px;border-radius:8px;background:#fff0c9;color:#9b7040;font-size:12px;text-align:center; }.guide-empty { min-height:360px;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center;color:#aaa; }.guide-empty span { font-size:38px;margin-bottom:12px; }.guide-empty strong { color:#786b5d;margin-bottom:8px; }.guide-empty p { max-width:180px;font-size:13px;line-height:1.6; }
.actions { display:flex;justify-content:center;align-items:center;gap:20px;color:#999;margin:12px 0; }
@media(max-width:860px){.play-area{grid-template-columns:1fr;gap:14px}.move-guide{width:100%;min-height:0}.guide-empty{min-height:150px}.guide-list{display:grid;grid-template-columns:repeat(3,1fr);gap:10px}.guide-list div{padding:10px;border-left:0;border-top:3px solid #d9a85f;background:#fff;border-radius:7px}}
@media(max-width:680px){.lobby{grid-template-columns:1fr;padding:24px 16px;gap:14px}.divider{width:100%;height:1px}.divider span{padding:0 10px}.match-card{grid-template-columns:1fr 1fr;gap:8px}.match-center{grid-column:1/-1;grid-row:2}.check-gif{width:58px;height:58px}.status-group{gap:8px}.chess-board{--cell:39px}.piece{width:33px;height:33px;font-size:20px}.river{font-size:17px}.game-header h1{font-size:25px}.guide-list{grid-template-columns:1fr}.guide-empty{min-height:120px}}
</style>
