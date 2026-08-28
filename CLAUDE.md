# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 技术栈

- 后端: Spring Boot 3.3 + MyBatis-Plus 3.5 + MySQL 8 + Redis 7 + JWT (jjwt 0.12) + BCrypt
- 前端: Vue 3 + TypeScript + Vite 5 + Pinia + Element Plus + Axios
- 部署: Docker Compose (database 层 + app 层分离)，本地 `docker build` 推送至阿里云 ACR

## 常用命令

```bash
# 后端
cd backend && mvn spring-boot:run          # 本地启动（需先启动 MySQL/Redis）
cd backend && mvn package -DskipTests       # 打包 JAR
cd backend && mvn test                      # 运行测试

# 前端
cd frontend && npm install && npm run dev   # 本地开发 (http://localhost:5173)
cd frontend && npm run build                # 类型检查 + 生产构建

# 基础设施（本地开发用）
cd docker/database && docker compose up -d  # 启动 MySQL + Redis

# 镜像构建与推送
cp .env.example .env                        # 先配置镜像仓库地址和凭证
./scripts/docker-build-push.sh              # Maven 打包 + 构建镜像 + 推送
PUSH=0 ./scripts/docker-build-push.sh       # 仅构建不推送
```

## 项目架构

### 后端分层（按功能模块垂直划分）

```
backend/src/main/java/com/gamesplatform/
├── config/          # SecurityConfig, JacksonConfig, PasswordConfig
├── auth/            # JwtTokenProvider, JwtAuthenticationFilter
├── common/          # ApiResponse<T>, BusinessException, GlobalExceptionHandler
├── user/            # 用户注册/登录/资料 (AuthController, UserService, UserMapper)
├── game/
│   ├── engine/      # GameEngine 接口 + SudokuGameEngine 实现 + SudokuGenerator
│   └── domain/      # GameSession, GameResult, GameSubmitCommand (通用领域对象)
├── sudoku/          # 数独 HTTP 层 (SudokuController, SudokuService, SudokuGameMapper)
├── pet/             # 宠物养成与积分兑换 (PetController, PetService, 权益/背包/订单 Mapper)
├── points/          # 积分流水 (PointsController, PointsService, PointTransactionMapper)
└── ranking/         # 排行榜 (RankingController, RankingService)
```

**核心模式**: `GameEngine` 接口定义游戏的创建、提交、落子校验、提示四个行为。`SudokuGameEngine` 是当前唯一实现类，通过 `@Qualifier` 注入到 `SudokuService`。新增游戏只需实现该接口 + 对应的 controller/service/mapper 切片。

**安全模型** (`SecurityConfig`): 无状态 JWT 认证，CSRF 已关闭，CORS 允许所有来源（`allowedOriginPatterns: *`）。`/api/auth/**` 和 `/api/ranking/**` 无需认证，其他路径需 Bearer Token。

**统一响应**: 所有 API 返回 `ApiResponse<T>` 结构 `{code, message, data}`。前端 `api/index.ts` 中 `getData/postData/putData` 自动拆包取 `data` 字段，`code !== 200` 时抛出 Error。

### 前端路由

```
/login, /register            # 公开页面
/ (MainLayout)                # 需认证
  /                           # HomeView（游戏大厅）
  /games/sudoku, /games/sudoku/play
  /games/pet                  # PetView
  /profile, /ranking
```

**路由守卫**: `meta.requiresAuth` 检查 token 存在性，`meta.guest` 禁止已登录用户访问。

**游戏注册表** (`config/games.ts`): `gameApps` 数组定义游戏卡片信息（id, name, icon, route, enabled），`GameAppGrid.vue` 据此渲染大厅。

### 前端 API 层

`api/index.ts` 创建 axios 实例，baseURL `/api`（Vite dev server proxy 到 `localhost:8080`）。请求拦截器自动附 Bearer Token，响应拦截器拦截 401/403 并跳转登录。

### 持久化与 Redis

- MySQL: 用户、游戏记录、积分流水、宠物权益。数据库结构由 `backend/src/main/resources/db/migration/` 下的 Flyway 脚本管理。
- Redis: `spring-boot-starter-data-redis` 已引入，当前主要用于缓存和会话管理。

### 部署架构

Docker Compose 拆分为两个独立目录:
- `docker/database/`: MySQL 8 + Redis 7，数据卷持久化，**不随应用更新重建**
- `docker/app/`: Backend (OpenJDK 21 分层 JAR) + Frontend (Nginx 静态服务)，可随时 `pull && up -d --force-recreate`
