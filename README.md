# Games Platform V1.0

面向休闲益智与双人对战场景的在线游戏平台，目前包含数独、五子棋、中国象棋和宠物养成。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Pinia + Element Plus |
| 后端 | Spring Boot 3 + MyBatis-Plus |
| 数据库 | MySQL 8 |
| 缓存 | Redis 7 |
| 认证 | JWT + BCrypt |

## 功能模块

- **用户中心** — 注册、登录、资料维护、头像管理
- **认证中心** — JWT 认证、BCrypt 密码加密
- **数独中心** — 创建游戏、难度选择、计时、校验、清空重做、结算、记录
- **五子棋中心** — 等待房间列表、随机分配黑白方、双人轮流落子、正常胜负积分结算（认输不计积分）
- **象棋中心** — 等待房间列表、随机分配红黑方、好友在线对弈、走子校验、正常胜负积分结算（认输不计积分）
- **宠物养成** — 宠物状态、权益商店、积分兑换、背包使用、装扮切换
- **积分中心** — 游戏积分奖励、流水记录、用户等级
- **排行榜中心** — 总积分榜、本周积分榜、数独速度榜
- **成就中心** — 首次通关、连续登录、通关次数、专家模式

## 部署流程（本地构建 → 推送镜像仓库）

应用镜像采用 **本地构建、推送到镜像云仓库、服务器拉取部署** 的方式，不在服务器上执行 `docker build`。

### 1. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env`，至少设置：

| 变量 | 说明 |
|------|------|
| `IMAGE_REGISTRY` | 镜像仓库地址 + 命名空间，如 `registry.cn-hangzhou.aliyuncs.com/aliyun_andy`（**不要**包含仓库名） |
| `BACKEND_IMAGE_NAME` | 后端仓库名，默认 `games-platform-backend` |
| `FRONTEND_IMAGE_NAME` | 前端仓库名，默认 `games-platform-frontend` |
| `IMAGE_TAG` | 镜像版本标签，如 `1.0.0` |
| `OPENJDK_IMAGE` | 后端基础镜像（默认 `mcr.microsoft.com/openjdk/jdk:21-ubuntu`） |
| `BUILD_REGISTRY` | 前端构建及 MySQL/Redis 拉取的基础镜像源 |

| `REGISTRY_USERNAME` | 镜像仓库用户名（阿里云 ACR 通常为命名空间名） |
| `REGISTRY_PASSWORD` | 镜像仓库密码（阿里云 ACR 为「访问凭证」固定密码） |

`.env` 中未配置用户名密码时，推送前会在终端交互式输入。

**阿里云 ACR 凭证获取：**

控制台 → 容器镜像服务 → 访问凭证 → 设置固定密码

```bash
./scripts/docker-build-push.sh
# 脚本会在推送前自动执行 docker login
```

也可手动登录后再推送：

```bash
docker login registry.cn-hangzhou.aliyuncs.com
./scripts/docker-build-push.sh
```

### 2. 本地构建并推送

```bash
chmod +x scripts/docker-build-push.sh
./scripts/docker-build-push.sh
```

脚本会先执行 `mvn package` 打包后端 JAR，再构建 Docker 镜像并推送：

- 后端采用 Spring Boot 分层解压（`jarmode=tools extract`），与线上一致

- `${IMAGE_REGISTRY}/games-platform-backend:${IMAGE_TAG}`
- `${IMAGE_REGISTRY}/games-platform-frontend:${IMAGE_TAG}`

仅构建不推送（本地验证）：

```bash
PUSH=0 ./scripts/docker-build-push.sh
```

Mac（Apple Silicon）本地构建、x86 Linux 服务器部署时，`.env` 中已默认 `PLATFORM=linux/amd64`。若服务器拉取报 `no matching manifest for linux/amd64`，说明镜像是在 ARM 上构建的，需重新构建推送。

### 3. 服务器部署

Compose 已拆分为 **数据库** 与 **应用** 两个独立目录，更新应用时不会重置 MySQL 数据。

**首次部署（或新机器）：**

```bash
# 1. 启动 MySQL / Redis（只需执行一次，或长期保持运行）
cp docker/database/.env.example docker/database/.env   # 按需修改
cd docker/database && docker compose up -d

# 2. 启动 Backend / Frontend
cp docker/app/.env.example docker/app/.env           # 确保镜像地址与 IMAGE_TAG 一致
cd docker/app && docker compose pull && docker compose up -d
```

**更新应用版本（不影响数据库）：**

```bash
cd docker/app
docker compose pull
docker compose up -d --force-recreate
```

启动完成后访问 **http://服务器IP** 即可使用平台。

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost |
| 后端 API | http://localhost:8080/api |
| MySQL | localhost:3306 |
| Redis | localhost:6379 |

常用命令：

```bash
# 数据库
cd docker/database
docker compose ps
docker compose logs -f
docker compose down          # 停止容器，数据保留
docker compose down -v       # ⚠️ 清除 MySQL 数据卷，仅在需要重置库时使用

# 应用
cd docker/app
docker compose ps
docker compose logs -f
docker compose down          # 仅停止 backend / frontend
docker compose up -d --force-recreate
```

更新版本：本地修改代码 → 调整 `IMAGE_TAG` → 重新执行 `./scripts/docker-build-push.sh` → 在 `docker/app` 目录执行 `docker compose pull && docker compose up -d --force-recreate`。

## 本地开发（可选）

若只需在本地调试代码，可仅启动基础设施：

```bash
cd docker/database && docker compose up -d
```

然后分别启动后端与前端：

```bash
cd backend && mvn spring-boot:run    # http://localhost:8080
cd frontend && npm install && npm run dev   # http://localhost:5173
```

## 项目结构

```
games-platform/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml                       # Maven 依赖与构建配置
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/gamesplatform/
│       │   │   ├── GamesPlatformApplication.java
│       │   │   ├── achievement/      # 成就配置、解锁记录与查询
│       │   │   ├── admin/            # 管理密码、游戏配置与积分调整
│       │   │   ├── auth/             # JWT 签发与认证过滤器
│       │   │   ├── chess/            # 中国象棋房间、规则、走子与结算
│       │   │   ├── common/           # 统一响应、业务异常与全局异常处理
│       │   │   ├── config/           # Spring Security、Jackson、密码配置
│       │   │   ├── game/             # 通用游戏领域对象与数独引擎接口
│       │   │   ├── gomoku/           # 五子棋房间、规则、落子与结算
│       │   │   ├── pet/              # 宠物养成、权益、背包与兑换
│       │   │   ├── points/           # 用户积分及积分流水
│       │   │   ├── ranking/          # 总榜、周榜与数独速度榜
│       │   │   ├── sudoku/           # 数独创建、校验、提交与记录
│       │   │   └── user/             # 注册、登录与用户资料
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-docker.yml
│       │       └── db/migration/      # Flyway 初始化及增量迁移脚本
│       └── test/java/com/gamesplatform/
│           ├── chess/                 # 中国象棋规则测试
│           └── gomoku/                # 五子棋胜负规则测试
├── frontend/                         # Vue 3 + TypeScript 前端
│   ├── package.json
│   ├── vite.config.ts                 # Vite 构建与本地 API 代理
│   ├── nginx.conf.template            # 生产静态资源与 API 反向代理
│   ├── Dockerfile
│   └── src/
│       ├── api/                        # Axios 实例与统一请求封装
│       ├── assets/                     # 游戏图片、GIF 与页面资源
│       ├── components/                 # 游戏卡片、数独棋盘、排行榜等组件
│       ├── config/                     # 游戏大厅注册配置
│       ├── layouts/                    # 主页面布局
│       ├── router/                     # 页面路由与登录守卫
│       ├── stores/                     # Pinia 登录状态
│       ├── styles/                     # 全局样式
│       ├── types/                      # 前端接口类型定义
│       ├── utils/                      # 剪贴板等浏览器兼容工具
│       └── views/
│           ├── games/                  # 数独、五子棋、象棋、宠物页面
│           └── ...                     # 大厅、登录、资料、排行、成就页面
├── docker/
│   ├── database/                      # MySQL + Redis 独立部署与持久化
│   └── app/                           # Backend + Frontend 应用部署
├── scripts/
│   └── docker-build-push.sh           # 本地构建并推送镜像
├── .env.example                       # 镜像构建环境变量模板
├── first-phase-demand.md              # 第一阶段需求记录
└── README.md
```

## 架构设计

后端按业务模块垂直拆分，每个模块内部根据需要包含 `controller`、`service`、`dto`、`entity`、`mapper` 和 `domain`。所有 HTTP 接口统一返回 `ApiResponse<T>`，受保护接口通过 JWT 获取当前用户 ID。

数独采用 `GameEngine` 抽象组织创建、提交和规则校验：

```java
public interface GameEngine {
    String getGameType();
    GameSession createGame(String difficulty);
    GameResult submit(GameSubmitCommand command);
    boolean validateMove(int[][] board, int row, int col, int value, int[][] solution);
    int[] getHint(int[][] board, int[][] solution);
}
```

当前外部数独接口未开放提示功能；`getHint` 仍保留在引擎契约中，便于规则引擎内部扩展。

五子棋和中国象棋属于双人状态型游戏，分别由 `GomokuRules` 和 `ChineseChessRules` 执行服务端规则判断，由对应 Service 管理房间、随机阵营、回合、认输和积分结算。对局状态写入 MySQL，前端通过短轮询同步双方棋盘。

数据库结构由 Flyway 管理：`V1__init.sql` 用于基础表初始化，后续 `V*__upgrade.sql` 用于宠物、游戏房间及结算字段升级。应用启动时会自动执行尚未应用的迁移。

前端游戏大厅由 `frontend/src/config/games.ts` 统一注册游戏卡片；路由页面采用按需加载，认证状态由 Pinia 维护，API 请求由 Axios 拦截器统一附加 Bearer Token。

## API 概览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| GET | /api/user/profile | 获取用户资料 |
| PUT | /api/user/profile | 更新用户资料 |
| POST | /api/sudoku/games | 创建数独游戏 |
| GET | /api/sudoku/games | 查询数独游戏历史 |
| GET | /api/sudoku/games/{id} | 查询数独游戏详情 |
| POST | /api/sudoku/games/{id}/validate | 校验数独填数 |
| POST | /api/sudoku/games/{id}/submit | 提交游戏 |
| POST | /api/gomoku/rooms | 创建五子棋邀请房间 |
| GET | /api/gomoku/rooms/waiting | 查询等待加入的五子棋房间 |
| POST | /api/gomoku/rooms/join | 通过房间码加入对局 |
| GET | /api/gomoku/games/active | 恢复当前未结束对局 |
| GET | /api/gomoku/games/{id} | 查询五子棋对局状态 |
| POST | /api/gomoku/games/{id}/moves | 五子棋落子 |
| POST | /api/gomoku/games/{id}/surrender | 认输或取消等待中的房间 |
| POST | /api/chess/rooms | 创建象棋邀请房间 |
| GET | /api/chess/rooms/waiting | 查询等待加入的象棋房间 |
| POST | /api/chess/rooms/join | 通过房间码加入象棋对局 |
| GET | /api/chess/games/active | 恢复当前未结束的象棋对局 |
| GET | /api/chess/games/{id} | 查询象棋对局状态 |
| POST | /api/chess/games/{id}/moves | 象棋走子 |
| POST | /api/chess/games/{id}/surrender | 认输或取消等待中的房间 |
| GET | /api/pet/profile | 查询我的宠物资料 |
| GET | /api/pet/init/options | 查询首次领养选项 |
| POST | /api/pet/init/select | 首次领养宠物 |
| GET | /api/pet/home | 宠物首页 |
| POST | /api/pet/grow | 更新宠物成长状态 |
| GET | /api/pet/benefit/list | 宠物权益列表 |
| POST | /api/pet/benefit/exchange | 积分兑换宠物权益 |
| GET | /api/pet/benefit/my | 我的宠物权益 |
| POST | /api/pet/benefit/use | 使用宠物权益 |
| GET | /api/ranking/total | 总积分排行榜 |
| GET | /api/ranking/weekly | 本周积分排行榜 |
| GET | /api/ranking/sudoku-speed | 数独速度排行榜 |
| GET | /api/achievements | 成就列表 |
| GET | /api/points/transactions | 积分流水 |
| GET | /api/admin/config/status | 查询管理配置状态 |
| POST | /api/admin/config/password | 设置管理密码 |
| PUT | /api/admin/config/game | 调整游戏配置 |
| POST | /api/admin/config/pet/growth/deduct | 调整宠物成长值 |
| POST | /api/admin/config/points | 调整用户积分 |
