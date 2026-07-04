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
  sudokuDailyLimit: number
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
  gameCode: string
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

export interface PetInfo {
  petId: number
  petType: string
  petTypeName: string
  petColorCode: string
  petColorName: string
  petName: string
  level: number
  stageNo: number
  stageName: string
  petAssetKey: string
  nextStage: PetNextStage | null
  exp: number
  hunger: number
  clean: number
  happiness: number
  energy: number
  loveValue: number
  currentHatCode: string | null
  currentBedCode: string | null
  currentRoomTheme: string | null
}

export interface PetNextStage {
  stageNo: number
  stageName: string
  needLevel: number
  remainLevel: number
}

export interface PetProfileResponse {
  hasPet: boolean
  petInfo: PetInfo | null
}

export interface PetGrowthStage {
  stageNo: number
  stageName: string
  minLevel: number
  maxLevel: number
  assetKey: string
  previewAssetKey: string
  description: string
}

export interface PetColorOption {
  colorCode: string
  colorName: string
  colorHex: string
  assetKey: string
  stagePreviewList: PetGrowthStage[]
}

export interface PetTypeOption {
  petType: string
  petTypeName: string
  description: string
  defaultColorCode: string
  colors: PetColorOption[]
}

export interface PetInitOptionsResponse {
  petTypes: PetTypeOption[]
}

export interface PetBenefitItem {
  benefitId: number
  benefitCode: string
  benefitName: string
  benefitType: 'CONSUMABLE' | 'PERMANENT'
  costPoints: number
  description: string
  effectType: string
  effectValue: number | null
  iconUrl: string | null
  owned: boolean
  quantity: number
  stock: number | null
  canExchange: boolean
}

export interface PetBenefitListResponse {
  availablePoints: number
  list: PetBenefitItem[]
}

export interface PetUserBenefit {
  userBenefitId: number
  benefitId: number
  benefitCode: string
  benefitName: string
  benefitType: 'CONSUMABLE' | 'PERMANENT'
  quantity: number
  status: number
}

export interface PetUserBenefitListResponse {
  list: PetUserBenefit[]
}

export interface PetHomeResponse {
  availablePoints: number
  petInfo: PetInfo
  benefits: PetBenefitListResponse
  myBenefits: PetUserBenefitListResponse
}

export interface PetExchangeResponse {
  orderNo: string
  benefitId: number
  benefitCode: string
  benefitName: string
  benefitType: 'CONSUMABLE' | 'PERMANENT'
  costPoints: number
  availablePoints: number
  quantity: number
}

export interface PetUseBenefitResponse {
  benefitCode: string
  benefitName: string
  remainingQuantity: number
  petInfo: PetInfo
  currentHatCode: string | null
  currentBedCode: string | null
  currentRoomTheme: string | null
}
