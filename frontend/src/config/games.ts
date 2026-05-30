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
    id: 'minesweeper',
    name: '扫雷',
    description: '敬请期待',
    icon: '💣',
    route: null,
    bg: 'linear-gradient(145deg, #90a4ae 0%, #b0bec5 100%)',
    shadow: '0 4px 16px rgba(144, 164, 174, 0.25)',
    enabled: false
  },
  {
    id: '2048',
    name: '2048',
    description: '敬请期待',
    icon: '🎯',
    route: null,
    bg: 'linear-gradient(145deg, #90a4ae 0%, #b0bec5 100%)',
    shadow: '0 4px 16px rgba(144, 164, 174, 0.25)',
    enabled: false
  }
]
