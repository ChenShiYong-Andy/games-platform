export interface GameApp {
  id: string
  name: string
  description: string
  icon: string
  route: string | null
  bg: string
  shadow: string
  enabled: boolean
}

export const gameApps: GameApp[] = [
  {
    id: 'sudoku',
    name: '数独',
    description: '经典逻辑益智游戏',
    icon: '🔢',
    route: '/games/sudoku',
    bg: 'linear-gradient(145deg, #667eea 0%, #764ba2 100%)',
    shadow: '0 8px 24px rgba(102, 126, 234, 0.35)',
    enabled: true
  },
  {
    id: 'pet',
    name: '宠物养成',
    description: '用积分兑换道具，照顾你的伙伴',
    icon: '🐾',
    route: '/games/pet',
    bg: 'linear-gradient(145deg, #ff8a5c 0%, #ffcf56 100%)',
    shadow: '0 8px 24px rgba(255, 138, 92, 0.32)',
    enabled: true
  },
  {
    id: 'gomoku',
    name: '五子棋',
    description: '邀请好友，双人在线对弈',
    icon: '⚫',
    route: '/games/gomoku',
    bg: 'linear-gradient(145deg, #d49a52 0%, #8b5e34 100%)',
    shadow: '0 8px 24px rgba(139, 94, 52, 0.32)',
    enabled: true
  },
  {
    id: 'chess',
    name: '中国象棋',
    description: '楚河汉界，好友在线对弈',
    icon: '帅',
    route: '/games/chess',
    bg: 'linear-gradient(145deg, #c84b31 0%, #7d1f18 100%)',
    shadow: '0 8px 24px rgba(143, 37, 27, 0.34)',
    enabled: true
  }
]
