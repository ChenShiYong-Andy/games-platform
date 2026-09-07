# Games Platform V1.0

面向休闲益智与双人对战场景的在线游戏平台，目前包含数独、五子棋、中国象棋和宠物养成。

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Pinia + Element Plus |
| 后端 | Spring Boot 3 + Spring AI + MyBatis-Plus |
| 数据库 | MySQL 8 |
| 缓存 | Redis 7 |
| 认证 | JWT + BCrypt |

## 功能模块

- **用户中心** — 注册、登录、资料维护、头像管理
- **认证中心** — JWT 认证、BCrypt 密码加密
- **数独中心** — 创建游戏、难度选择、计时、校验、清空重做、结算、记录
- **五子棋中心** — 好友房间与人机对局、随机分配黑白方、服务端落子和胜负积分结算（认输不计积分）
- **象棋中心** — 好友房间与人机对局、随机分配红黑方、服务端走子校验和胜负积分结算（认输不计积分）
- **宠物养成** — 宠物状态、权益商店、积分兑换、背包使用、装扮切换
- **每日英语** — 大模型按年级生成每日单词与短句，支持设备朗读、录音跟读和发音校验
- **独立管理后台** — `/admin` 使用唯一的全局管理密码保护，以折叠面板管理每日英语、积分和宠物成长值
- **积分中心** — 游戏积分奖励、流水记录、用户等级
- **排行榜中心** — 总积分榜、本周积分榜、数独速度榜

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
| `ADMIN_IMAGE_NAME` | Admin 后端仓库名，默认 `games-platform-admin` |
| `ADMIN_UI_IMAGE_NAME` | Admin UI 前端仓库名，默认 `games-platform-admin-ui` |
| `IMAGE_TAG` | 镜像版本标签，如 `1.0.0` |
| `OPENJDK_IMAGE` | 后端基础镜像（默认 `mcr.microsoft.com/openjdk/jdk:21-ubuntu`）；本地存在相同平台镜像时优先使用本地缓存 |
| `BUILD_REGISTRY` | 前端构建及 MySQL/Redis 拉取的基础镜像源 |
| `OPENAI_BASE_URL` | OpenAI 协议兼容服务的通用地址，默认 `https://api.openai.com` |
| `OPENAI_API_KEY` | OpenAI 协议兼容服务的通用密钥 |
| `LLM_BASE_URL` | 文本大模型地址；未设置时继承 `OPENAI_BASE_URL` |
| `LLM_API_KEY` | 文本大模型密钥；未设置时继承 `OPENAI_API_KEY` |
| `LLM_MODEL` | 文本模型名称，默认 `gpt-4o-mini` |

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

- `${IMAGE_REGISTRY}/games-platform-admin:${IMAGE_TAG}`
- `${IMAGE_REGISTRY}/games-platform-admin-ui:${IMAGE_TAG}`

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

# 2. 启动 Admin / Admin UI
cp docker/app/.env.example docker/app/.env           # 确保镜像地址与 IMAGE_TAG 一致
cd docker/app && ./deploy.sh
```

**更新应用版本（不影响数据库）：**

```bash
cd docker/app
./deploy.sh
```

`deploy.sh` 会在启动前检测并强制下线旧版 `games-platform-backend`、`games-platform-frontend` 容器，然后启动 `games-platform-admin`、`games-platform-admin-ui`，并通过 `--remove-orphans` 清理同一 Compose 项目的遗留服务。数据库容器不在此 Compose 项目中，不受影响。

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
docker compose down          # 仅停止 admin / admin-ui
./deploy.sh                  # 检测旧容器、拉取镜像并重建服务
```

更新版本：本地修改代码 → 调整 `IMAGE_TAG` → 重新执行 `./scripts/docker-build-push.sh` → 在 `docker/app` 目录执行 `./deploy.sh`。

## 本地开发（可选）

若只需在本地调试代码，可仅启动基础设施：

```bash
cd docker/database && docker compose up -d
```

然后分别启动后端与前端：

```bash
cd admin && mvn spring-boot:run    # http://localhost:8080
cd admin-ui && npm install && npm run dev   # http://localhost:5173
```

## 项目结构

```
games-platform/
├── admin/                            # Spring Boot 管理与业务后端
│   ├── pom.xml                       # Maven 依赖与构建配置
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/gamesplatform/
│       │   │   ├── GamesPlatformApplication.java
│       │   │   ├── common/           # 统一响应、业务异常与全局异常处理
│       │   │   ├── config/           # Spring Security、Jackson、密码配置
│       │   │   ├── games/            # 全部游戏及通用游戏能力
│       │   │   │   ├── dashboard/    # 游戏大厅及今日游戏统计
│       │   │   │   ├── domain/       # 通用游戏领域对象
│       │   │   │   ├── engine/       # 通用游戏引擎与数独生成器
│       │   │   │   ├── chess/        # 中国象棋房间、规则、走子与结算
│       │   │   │   ├── gomoku/       # 五子棋房间、规则、落子与结算
│       │   │   │   └── sudoku/       # 数独创建、校验、提交与记录
│       │   │   ├── school/           # 校园学习与成长能力聚合
│       │   │   │   ├── english/      # 每日英语配置、生成、缓存及接口
│       │   │   │   ├── pet/          # 宠物养成、权益、背包与兑换
│       │   │   │   └── ranking/      # 总榜、周榜与数独速度榜
│       │   │   └── system/           # 系统基础能力聚合
│       │   │       ├── admin/         # 全局管理后台、游戏配置与积分调整
│       │   │       ├── auth/          # JWT 签发与认证过滤器
│       │   │       ├── points/        # 用户积分及积分流水
│       │   │       └── user/          # 注册、登录与用户资料
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-docker.yml
│       │       └── db/migration/      # Flyway 初始化及增量迁移脚本
│       └── test/java/com/gamesplatform/
│           ├── games/                 # 各游戏规则测试
│           └── school/english/        # 每日英语缓存测试
├── admin-ui/                         # Vue 3 + TypeScript 管理与游戏前端
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
│           ├── AdminView.vue           # 独立密码保护的管理后台
│           └── ...                     # 大厅、登录、资料与排行页面
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

后端按业务模块垂直拆分，每个模块内部根据需要包含 `controller`、`service`、`dto`、`entity`、`mapper` 和 `domain`。业务服务统一采用 `service/XxxService` 接口与 `service/impl/XxxServiceImpl` 实现分离，调用方仅依赖服务接口。所有 HTTP 接口统一返回 `ApiResponse<T>`，受保护接口通过 JWT 获取当前用户 ID。

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

五子棋和中国象棋属于状态型棋类游戏，分别由 `GomokuRules` 和 `ChineseChessRules` 执行服务端规则判断，由对应 Service 管理好友房间、人机对局、随机阵营、回合、认输和积分结算。`GomokuAi` 与 `ChineseChessAi` 负责电脑走棋。对局状态写入 MySQL，好友模式下前端通过短轮询同步双方棋盘。

数据库结构由 Flyway 管理：`V1__init.sql` 用于基础表初始化，后续 `V*__upgrade.sql` 用于宠物、游戏房间及结算字段升级。应用启动时会自动执行尚未应用的迁移。

前端游戏大厅由 `admin-ui/src/config/games.ts` 统一注册游戏卡片；路由页面采用按需加载，认证状态由 Pinia 维护，API 请求由 Axios 拦截器统一附加 Bearer Token。

每日英语通过 Spring AI 的 OpenAI 模型适配器调用兼容接口。`OPENAI_*` 提供通用连接配置，`LLM_*` 可为文本模型单独覆盖，未设置时自动继承通用配置。内容按日期和年级使用 Redis 与 MySQL 两级缓存，Redis 未命中或不可用时自动回退数据库；管理员更新年级或 Skill 后通过 Spring Cache 的 `@CacheEvict` 声明式清理缓存。所有 Redis 顶层键统一使用 `system.redis.key-prefix` 配置的系统前缀，部署时可通过 `REDIS_KEY_PREFIX` 覆盖，默认值为 `games-platform`。播放与录音识别使用浏览器设备的 Web Speech API，录音不会上传或保存到服务端。

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
| POST | /api/gomoku/ai-games | 创建五子棋人机对局 |
| GET | /api/gomoku/rooms/waiting | 查询等待加入的五子棋房间 |
| POST | /api/gomoku/rooms/join | 通过房间码加入对局 |
| GET | /api/gomoku/games/active | 恢复当前未结束对局 |
| GET | /api/gomoku/games/{id} | 查询五子棋对局状态 |
| POST | /api/gomoku/games/{id}/moves | 五子棋落子 |
| POST | /api/gomoku/games/{id}/surrender | 认输或取消等待中的房间 |
| POST | /api/chess/rooms | 创建象棋邀请房间 |
| POST | /api/chess/ai-games | 创建象棋人机对局 |
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
| GET | /api/daily-english/today | 获取或生成今日英语口语练习 |
| GET | /api/ranking/total | 总积分排行榜 |
| GET | /api/ranking/weekly | 本周积分排行榜 |
| GET | /api/ranking/sudoku-speed | 数独速度排行榜 |
| GET | /api/points/transactions | 积分流水 |
| GET | /api/dashboard/today-game-stats | 当前用户今日数独、五子棋与象棋游戏次数 |
| GET | /api/admin-portal/status | 查询独立管理后台密码状态 |
| POST | /api/admin-portal/password | 首次设置独立管理后台密码 |
| POST | /api/admin-portal/verify | 验证独立管理后台密码 |
| POST | /api/admin-portal/daily-english/config | 读取每日英语配置 |
| PUT | /api/admin-portal/daily-english/config | 更新每日英语年级与 Skill |
| POST | /api/admin-portal/pet/growth/deduct | 调整当前登录用户的宠物成长值 |
| POST | /api/admin-portal/points | 调整当前登录用户的积分 |
