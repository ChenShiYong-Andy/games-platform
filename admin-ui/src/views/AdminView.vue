<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getData, postData, putData } from '@/api'
import { useAuthStore } from '@/stores/auth'
import type {
  AdminPortalStatus,
  DailyEnglishConfig,
  UserProfile
} from '@/types'

const authStore = useAuthStore()
const loading = ref(true)
const passwordSet = ref(false)
const verified = ref(false)
const submitting = ref(false)
const saving = ref(false)
const adjustingPoints = ref(false)
const adjustingPetGrowth = ref(false)
const password = ref('')
const confirmPassword = ref('')
const activeSections = ref<string[]>(['daily-english'])
const config = ref<DailyEnglishConfig>({ gradeLevel: 1, skillMarkdown: '' })
const pointsForm = ref({ amount: 0, description: '' })
const petConfig = ref({ amount: 0 })

const gradeOptions = [1, 2, 3, 4, 5, 6]
const gradeLabel = computed(() => `小学${['一', '二', '三', '四', '五', '六'][config.value.gradeLevel - 1]}年级`)
const promptPreview = computed(() =>
  `请为中国${gradeLabel.value}学生生成适龄的每日英语口语练习，包括 5 个单词和 3 个日常短句。`
)

onMounted(async () => {
  try {
    const status = await getData<AdminPortalStatus>('/admin-portal/status')
    passwordSet.value = status.passwordSet
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '管理后台状态加载失败')
  } finally {
    loading.value = false
  }
})

async function submitAccess() {
  if (password.value.length < 6) {
    ElMessage.info('请输入至少 6 位管理后台密码')
    return
  }
  if (!passwordSet.value && password.value !== confirmPassword.value) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  submitting.value = true
  try {
    const verifyingExistingPassword = passwordSet.value
    if (verifyingExistingPassword) {
      await postData<string>('/admin-portal/verify', { password: password.value })
    } else {
      await postData<AdminPortalStatus>('/admin-portal/password', {
        password: password.value
      })
      passwordSet.value = true
    }
    verified.value = true
    confirmPassword.value = ''
    await loadConfig()
    ElMessage.success(verifyingExistingPassword ? '管理后台验证成功' : '管理后台密码已设置')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '验证失败')
  } finally {
    submitting.value = false
  }
}

async function loadConfig() {
  config.value = await postData<DailyEnglishConfig>(
    '/admin-portal/daily-english/config',
    { password: password.value }
  )
  await authStore.refreshProfile()
}

async function saveEnglishConfig() {
  saving.value = true
  try {
    config.value = await putData<DailyEnglishConfig>(
      '/admin-portal/daily-english/config',
      {
        ...config.value,
        adminPassword: password.value
      }
    )
    ElMessage.success('每日英语配置已保存，今日内容将在下次展开时重新生成')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '配置保存失败')
  } finally {
    saving.value = false
  }
}

function applyProfile(profile: UserProfile) {
  authStore.user = profile
  localStorage.setItem('user', JSON.stringify(profile))
}

async function adjustPoints() {
  if (!pointsForm.value.amount) {
    ElMessage.info('请输入非 0 的积分调整值')
    return
  }
  adjustingPoints.value = true
  try {
    const profile = await postData<UserProfile>('/admin-portal/points', {
      ...pointsForm.value,
      adminPassword: password.value
    })
    applyProfile(profile)
    pointsForm.value = { amount: 0, description: '' }
    ElMessage.success('积分已调整')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '调整失败')
  } finally {
    adjustingPoints.value = false
  }
}

async function deductPetGrowth() {
  if (!petConfig.value.amount || petConfig.value.amount < 1) {
    ElMessage.info('请输入要扣减的成长值')
    return
  }
  adjustingPetGrowth.value = true
  try {
    await postData<string>('/admin-portal/pet/growth/deduct', {
      ...petConfig.value,
      adminPassword: password.value
    })
    petConfig.value.amount = 0
    ElMessage.success('宠物成长值已扣减')
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '扣减失败')
  } finally {
    adjustingPetGrowth.value = false
  }
}
</script>

<template>
  <div class="page-container admin-page" v-loading="loading">
    <header class="admin-header">
      <div>
        <span>ADMIN CONSOLE</span>
        <h1>管理后台</h1>
        <p>独立管理密码保护 · 功能配置按模块折叠展示</p>
      </div>
      <div class="security-badge">🔐 独立密码</div>
    </header>

    <section v-if="!loading && !verified" class="access-card card">
      <div class="access-icon">{{ passwordSet ? '🔑' : '🛡️' }}</div>
      <h2>{{ passwordSet ? '验证管理后台密码' : '首次设置管理后台密码' }}</h2>
      <p>
        {{ passwordSet
          ? '每次进入 /admin 都需要重新验证全局管理密码。'
          : '此密码单独保存在管理安全配置中，不属于任何用户。' }}
      </p>
      <el-input
        v-model="password"
        type="password"
        show-password
        size="large"
        placeholder="至少 6 位"
        @keyup.enter="submitAccess"
      />
      <el-input
        v-if="!passwordSet"
        v-model="confirmPassword"
        type="password"
        show-password
        size="large"
        placeholder="再次输入密码"
        @keyup.enter="submitAccess"
      />
      <el-button type="primary" size="large" :loading="submitting" @click="submitAccess">
        {{ passwordSet ? '验证并进入' : '设置并进入' }}
      </el-button>
    </section>

    <section v-else-if="verified" class="config-shell card">
      <div class="managed-user">
        <div>👤 当前操作用户</div>
        <strong>{{ authStore.user?.nickname }} <small>({{ authStore.user?.username }})</small></strong>
      </div>
      <el-collapse v-model="activeSections">
        <el-collapse-item name="daily-english">
          <template #title>
            <div class="collapse-title">
              <span>🔤</span>
              <div>
                <strong>每日英语配置</strong>
                <small>控制口语练习的学习阶段与生成 Skill</small>
              </div>
            </div>
          </template>

          <div class="english-config-grid">
            <section class="config-block">
              <div class="block-title">
                <span>01</span>
                <div><h2>基础配置</h2><p>选择生成内容对应的小学学习阶段</p></div>
              </div>
              <el-radio-group v-model="config.gradeLevel" class="grade-grid">
                <el-radio-button v-for="grade in gradeOptions" :key="grade" :value="grade">
                  {{ grade }} 年级
                </el-radio-button>
              </el-radio-group>
              <div class="prompt-preview">
                <strong>基础提示词预览</strong>
                <p>{{ promptPreview }}</p>
              </div>
            </section>

            <section class="config-block">
              <div class="block-title">
                <span>02</span>
                <div><h2>高级配置</h2><p>Markdown Skill 会追加到基础提示词之后</p></div>
              </div>
              <el-input
                v-model="config.skillMarkdown"
                type="textarea"
                :rows="13"
                maxlength="20000"
                show-word-limit
                placeholder="# 教学目标&#10;- 使用生活化主题&#10;- 重点练习自然拼读"
              />
            </section>
          </div>

          <div class="config-actions">
            <span>保存后会清除已缓存的每日练习</span>
            <el-button type="primary" size="large" :loading="saving" @click="saveEnglishConfig">
              保存每日英语配置
            </el-button>
          </div>
        </el-collapse-item>

        <el-collapse-item name="points">
          <template #title>
            <div class="collapse-title">
              <span>🪙</span>
              <div>
                <strong>积分调整</strong>
                <small>为当前登录用户增加或扣减积分</small>
              </div>
            </div>
          </template>
          <div class="operation-panel">
            <el-form label-width="150px">
              <el-form-item label="调整积分">
                <el-input-number v-model="pointsForm.amount" :min="-100000" :max="100000" />
              </el-form-item>
              <el-form-item label="调整说明">
                <el-input v-model="pointsForm.description" placeholder="例如：活动奖励、误发扣回" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="adjustingPoints" @click="adjustPoints">
                  提交积分调整
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-collapse-item>

        <el-collapse-item name="pet">
          <template #title>
            <div class="collapse-title">
              <span>🐾</span>
              <div>
                <strong>宠物配置</strong>
                <small>扣减当前登录用户的宠物成长值</small>
              </div>
            </div>
          </template>
          <div class="operation-panel">
            <el-form label-width="150px">
              <el-form-item label="扣减成长值">
                <el-input-number v-model="petConfig.amount" :min="1" :max="100000" />
              </el-form-item>
              <el-form-item>
                <el-button type="danger" :loading="adjustingPetGrowth" @click="deductPetGrowth">
                  提交成长值扣减
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-collapse-item>

      </el-collapse>
    </section>
  </div>
</template>

<style scoped>
.admin-page { max-width: 1120px; }
.admin-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:24px;padding:4px 2px; }
.admin-header span { color:#667eea;font-size:12px;font-weight:900;letter-spacing:1.5px; }
.admin-header h1 { margin:5px 0;font-size:34px; }
.admin-header p { color:#838a99; }
.security-badge { border:1px solid #dfe4f3;border-radius:999px;padding:9px 14px;background:#fff;color:#596174;font-weight:700; }
.access-card { width:min(460px,100%);margin:56px auto;padding:34px;display:flex;flex-direction:column;gap:16px;text-align:center; }
.access-icon { font-size:48px; }
.access-card h2 { font-size:24px; }
.access-card p { color:#7d8594;line-height:1.7; }
.config-shell { padding:12px 26px 26px; }
.config-shell :deep(.el-collapse-item__header) { min-height:64px;height:auto;padding:9px 0;line-height:1.2; }
.managed-user { display:flex;align-items:center;justify-content:space-between;gap:16px;margin:4px 0 10px;padding:13px 15px;border-radius:12px;background:#f4f6fb;color:#747c8c;font-size:13px; }
.managed-user strong { color:#343b4a;font-size:15px; }
.managed-user small { color:#8d94a2;font-weight:500; }
.collapse-title { display:flex;align-items:center;gap:12px;text-align:left; }
.collapse-title > span { font-size:26px;line-height:1; }
.collapse-title div { display:flex;flex-direction:column;gap:4px;line-height:1.2; }
.collapse-title strong { font-size:17px;line-height:1.2; }
.collapse-title small { color:#9299a8;font-size:12px;line-height:1.3; }
.english-config-grid { display:grid;grid-template-columns:.85fr 1.15fr;gap:18px;padding:16px 2px; }
.config-block { border:1px solid #e7eaf3;border-radius:16px;padding:20px;background:#fbfcff; }
.block-title { display:flex;align-items:flex-start;gap:11px;margin-bottom:20px; }
.block-title > span { width:32px;height:32px;border-radius:9px;background:#e9edff;color:#667eea;display:grid;place-items:center;font-size:11px;font-weight:900; }
.block-title h2 { font-size:19px;margin-bottom:3px; }
.block-title p { color:#9299a8;font-size:12px; }
.grade-grid { display:grid;grid-template-columns:repeat(3,1fr);width:100%; }
.grade-grid :deep(.el-radio-button__inner) { width:100%; }
.prompt-preview { margin-top:20px;border-radius:12px;padding:14px;background:#f0f3ff;color:#586174; }
.prompt-preview strong { display:block;margin-bottom:7px;color:#49536a;font-size:13px; }
.prompt-preview p { font-size:13px;line-height:1.65; }
.config-actions { display:flex;align-items:center;justify-content:flex-end;gap:18px;padding-top:12px; }
.config-actions span { color:#9299a8;font-size:12px; }
.operation-panel { max-width:680px;padding:22px 4px 10px; }
@media(max-width:760px){.admin-header{align-items:flex-start}.security-badge{display:none}.english-config-grid{grid-template-columns:1fr}.grade-grid{grid-template-columns:repeat(2,1fr)}.config-actions{align-items:stretch;flex-direction:column}}
</style>
