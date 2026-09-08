<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteAdminData, getAdminData, postAdminData, putAdminData } from '@/api/admin'
import type { DailyEnglishConfig, ManagedUser, UserProfile } from '@/types'

const users = ref<ManagedUser[]>([])
const selectedUserId = ref<number | null>(null)
const loading = ref(false)
const saving = ref(false)
const bindForm = ref({ username: '', bindingCode: '' })
const pointsForm = ref({ amount: 0, description: '' })
const petForm = ref({ amount: 1 })
const englishConfig = ref<DailyEnglishConfig>({ gradeLevel: 1, skillMarkdown: '' })
const activeSections = ref<string[]>(['english'])
const selected = computed(() => users.value.find(item => item.user.id === selectedUserId.value) || null)
const gradeOptions = [1, 2, 3, 4, 5, 6]

onMounted(loadUsers)

async function loadUsers() {
  loading.value = true
  try {
    users.value = await getAdminData<ManagedUser[]>('/managed-users')
    if (!selectedUserId.value && users.value.length) await selectUser(users.value[0].user.id)
  } catch (error: unknown) { showError(error, '用户列表加载失败') }
  finally { loading.value = false }
}

async function selectUser(userId: number) {
  selectedUserId.value = userId
  try {
    englishConfig.value = await getAdminData<DailyEnglishConfig>(`/managed-users/${userId}/daily-english/config`)
  } catch (error: unknown) { showError(error, '用户配置加载失败') }
}

async function bindUser() {
  if (!bindForm.value.username || !bindForm.value.bindingCode) return ElMessage.warning('请输入用户账号和绑定码')
  try {
    const result = await postAdminData<ManagedUser>('/managed-users/bind', bindForm.value)
    bindForm.value = { username: '', bindingCode: '' }
    await loadUsers(); await selectUser(result.user.id)
    ElMessage.success('用户关联成功')
  } catch (error: unknown) { showError(error, '关联失败') }
}

async function unbindUser() {
  if (!selected.value) return
  try {
    await ElMessageBox.confirm(`确认解除与 ${selected.value.user.nickname} 的关联？`, '解除关联', { type: 'warning' })
  } catch { return }
  try {
    await deleteAdminData<string>(`/managed-users/${selected.value.user.id}`)
    selectedUserId.value = null; await loadUsers(); ElMessage.success('已解除关联')
  } catch (error: unknown) { showError(error, '解除关联失败') }
}

async function saveEnglish() {
  if (!selectedUserId.value) return
  saving.value = true
  try {
    englishConfig.value = await putAdminData<DailyEnglishConfig>(`/managed-users/${selectedUserId.value}/daily-english/config`, englishConfig.value)
    ElMessage.success('每日英语配置已保存')
  } catch (error: unknown) { showError(error, '保存失败') }
  finally { saving.value = false }
}

async function resetEnglish() {
  if (!selectedUserId.value) return
  try {
    englishConfig.value = await deleteAdminData<DailyEnglishConfig>(`/managed-users/${selectedUserId.value}/daily-english/config`)
    ElMessage.success('已恢复系统默认配置')
  } catch (error: unknown) { showError(error, '恢复失败') }
}

async function adjustPoints() {
  if (!selectedUserId.value || !pointsForm.value.amount) return ElMessage.warning('请输入非 0 的积分调整值')
  try {
    const profile = await postAdminData<UserProfile>(`/managed-users/${selectedUserId.value}/points/adjust`, pointsForm.value)
    const item = users.value.find(entry => entry.user.id === profile.id)
    if (item) item.user = profile
    pointsForm.value = { amount: 0, description: '' }; ElMessage.success('积分调整成功')
  } catch (error: unknown) { showError(error, '积分调整失败') }
}

async function deductGrowth() {
  if (!selectedUserId.value || petForm.value.amount < 1) return
  try {
    await postAdminData<string>(`/managed-users/${selectedUserId.value}/pet/growth/deduct`, petForm.value)
    ElMessage.success('宠物成长值已扣减')
  } catch (error: unknown) { showError(error, '成长值扣减失败') }
}

function showError(error: unknown, fallback: string) { ElMessage.error(error instanceof Error ? error.message : fallback) }
</script>

<template>
  <div class="admin-workbench" v-loading="loading">
    <header class="page-title"><span>ADMIN CONSOLE</span><h1>用户管理工作台</h1><p>仅可管理已授权关联的普通用户</p></header>
    <div class="workbench-grid">
      <aside class="user-panel card">
        <div class="panel-heading"><h2>关联用户</h2><span>{{ users.length }} 人</span></div>
        <div class="bind-form"><el-input v-model="bindForm.username" placeholder="用户账号" /><el-input v-model="bindForm.bindingCode" placeholder="一次性绑定码" maxlength="8" /><el-button type="primary" @click="bindUser">关联用户</el-button></div>
        <div v-if="users.length" class="user-list"><button v-for="item in users" :key="item.user.id" :class="{ active: selectedUserId === item.user.id }" @click="selectUser(item.user.id)"><b>{{ item.user.nickname }}</b><small>@{{ item.user.username }}</small><em>{{ item.user.totalPoints }} 积分</em></button></div>
        <el-empty v-else description="暂无关联用户" :image-size="70" />
      </aside>
      <main class="config-panel card">
        <template v-if="selected">
          <div class="selected-header"><div><small>当前管理用户</small><h2>{{ selected.user.nickname }} <span>(@{{ selected.user.username }})</span></h2></div><el-button type="danger" plain @click="unbindUser">解除关联</el-button></div>
          <div class="profile-stats"><div><b>{{ selected.user.totalPoints }}</b><span>总积分</span></div><div><b>Lv.{{ selected.user.level }}</b><span>等级</span></div><div><b>{{ selected.user.totalClears }}</b><span>通关次数</span></div></div>
          <el-collapse v-model="activeSections">
            <el-collapse-item name="english" title="🔤 每日英语配置"><section class="config-block"><h3>学习阶段</h3><el-radio-group v-model="englishConfig.gradeLevel"><el-radio-button v-for="grade in gradeOptions" :key="grade" :value="grade">{{ grade }} 年级</el-radio-button></el-radio-group><h3>高级 Skill</h3><el-input v-model="englishConfig.skillMarkdown" type="textarea" :rows="9" maxlength="20000" show-word-limit placeholder="# 教学目标" /><div class="actions"><el-button @click="resetEnglish">恢复默认</el-button><el-button type="primary" :loading="saving" @click="saveEnglish">保存配置</el-button></div></section></el-collapse-item>
            <el-collapse-item name="points" title="🪙 积分调整"><section class="inline-form"><el-input-number v-model="pointsForm.amount" :min="-100000" :max="100000" /><el-input v-model="pointsForm.description" placeholder="调整原因" /><el-button type="primary" @click="adjustPoints">提交</el-button></section></el-collapse-item>
            <el-collapse-item name="pet" title="🐾 宠物配置"><section class="inline-form"><el-input-number v-model="petForm.amount" :min="1" :max="100000" /><el-button type="danger" plain @click="deductGrowth">扣减成长值</el-button></section></el-collapse-item>
          </el-collapse>
        </template>
        <el-empty v-else description="请先关联或选择一个用户" />
      </main>
    </div>
  </div>
</template>

<style scoped>
.admin-workbench{max-width:1320px;margin:auto}.page-title{margin:8px 0 22px}.page-title span{color:#667eea;font-size:12px;font-weight:900;letter-spacing:2px}.page-title h1{margin:5px 0;font-size:32px}.page-title p{color:#8991a1}.workbench-grid{display:grid;grid-template-columns:320px 1fr;gap:20px;align-items:start}.card{border-radius:18px;background:#fff;box-shadow:0 10px 35px #26334d10}.user-panel,.config-panel{padding:22px}.panel-heading,.selected-header{display:flex;justify-content:space-between;align-items:center}.panel-heading span{background:#eef1ff;color:#667eea;padding:5px 10px;border-radius:999px}.bind-form{display:grid;gap:9px;margin:18px 0}.user-list{display:grid;gap:8px}.user-list button{border:1px solid #e7eaf2;border-radius:12px;padding:13px;text-align:left;background:#fff;display:grid;grid-template-columns:1fr auto;cursor:pointer}.user-list button.active{border-color:#667eea;background:#f2f4ff}.user-list small{grid-column:1;color:#9299a8}.user-list em{grid-column:2;grid-row:1/3;align-self:center;color:#667eea;font-style:normal}.selected-header{padding-bottom:16px;border-bottom:1px solid #edf0f5}.selected-header small{color:#9299a8}.selected-header h2{margin-top:4px}.selected-header h2 span{color:#8991a1;font-size:14px}.profile-stats{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin:18px 0}.profile-stats div{padding:14px;border-radius:12px;background:#f5f7fc;display:flex;flex-direction:column}.profile-stats b{font-size:21px;color:#5366d9}.profile-stats span{font-size:12px;color:#9299a8}.config-panel :deep(.el-collapse-item__header){font-size:16px;font-weight:800}.config-block{padding:14px 4px}.config-block h3{margin:10px 0}.actions{display:flex;justify-content:flex-end;gap:10px;margin-top:16px}.inline-form{display:flex;gap:12px;padding:18px 4px;max-width:680px}@media(max-width:850px){.workbench-grid{grid-template-columns:1fr}.inline-form{flex-wrap:wrap}}
</style>
