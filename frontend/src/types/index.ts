export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface UserProfile {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
  email: string | null
  level: number
  totalPoints: number
  loginStreak: number
  totalClears: number
}

export interface AuthResponse {
  token: string
  user: UserProfile
}

export interface GameResponse {
  id: number
  difficulty: string
  gridSize: number
  puzzle: number[][]
  status: string
  elapsedSeconds: number
  hintsUsed: number
  mistakes: number
  score: number
  startedAt: string
  completedAt: string | null
}

export interface SubmitGameResponse {
  success: boolean
  score: number
  pointsEarned: number
  message: string
  newLevel?: number
  totalPoints?: number
}

export interface RankingEntry {
  userId: number
  nickname: string
  avatarUrl: string | null
  score: number
  rank: number
}

export interface Achievement {
  id: number
  code: string
  name: string
  description: string
  icon: string
  unlocked: boolean
  unlockedAt: string | null
}

export interface PointTransaction {
  id: number
  amount: number
  type: string
  description: string
  createdAt: string
}
