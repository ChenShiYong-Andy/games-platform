#!/usr/bin/env bash
# 本地构建 backend / frontend 镜像并推送到镜像仓库
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

IMAGE_REGISTRY="${IMAGE_REGISTRY:?请在 .env 中设置 IMAGE_REGISTRY（镜像仓库地址/命名空间）}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
BACKEND_IMAGE_NAME="${BACKEND_IMAGE_NAME:-games-platform-backend}"
FRONTEND_IMAGE_NAME="${FRONTEND_IMAGE_NAME:-games-platform-frontend}"
BUILD_REGISTRY="${BUILD_REGISTRY:-docker.m.daocloud.io/library}"
OPENJDK_IMAGE="${OPENJDK_IMAGE:-mcr.microsoft.com/openjdk/jdk:21-ubuntu}"
PLATFORM="${PLATFORM:-linux/amd64}"
PUSH="${PUSH:-1}"

BACKEND_IMAGE="${IMAGE_REGISTRY}/${BACKEND_IMAGE_NAME}:${IMAGE_TAG}"
FRONTEND_IMAGE="${IMAGE_REGISTRY}/${FRONTEND_IMAGE_NAME}:${IMAGE_TAG}"
REGISTRY_HOST="${IMAGE_REGISTRY%%/*}"

ensure_buildx() {
  if [[ "$PLATFORM" == *"/"* ]]; then
    if ! docker buildx version >/dev/null 2>&1; then
      echo "错误: 跨平台构建需要 Docker Buildx，请升级 Docker Desktop"
      exit 1
    fi
    if ! docker buildx inspect games-platform-builder >/dev/null 2>&1; then
      docker buildx create --name games-platform-builder --use
    else
      docker buildx use games-platform-builder
    fi
  fi
}

registry_login() {
  local username="${REGISTRY_USERNAME:-}"
  local password="${REGISTRY_PASSWORD:-}"

  if [[ -z "$username" ]]; then
    read -r -p "镜像仓库用户名 (${REGISTRY_HOST}): " username
  fi
  if [[ -z "$password" ]]; then
    read -r -s -p "镜像仓库密码: " password
    echo
  fi

  if [[ -z "$username" || -z "$password" ]]; then
    echo "错误: 用户名或密码不能为空"
    echo "可在 .env 中设置 REGISTRY_USERNAME / REGISTRY_PASSWORD"
    exit 1
  fi

  echo "==> 登录镜像仓库: ${REGISTRY_HOST}"
  if ! printf '%s' "$password" | docker login "$REGISTRY_HOST" -u "$username" --password-stdin; then
    echo "错误: 登录失败，请检查用户名和密码"
    echo "阿里云 ACR 密码为控制台「访问凭证」中的固定密码，不是阿里云账号密码"
    exit 1
  fi
}

backend_build_args=(--platform "$PLATFORM" --build-arg "OPENJDK_IMAGE=${OPENJDK_IMAGE}")
frontend_build_args=(--platform "$PLATFORM" --build-arg "REGISTRY=${BUILD_REGISTRY}")

echo "==> 目标平台: ${PLATFORM}"
ensure_buildx

echo "==> Maven 打包后端"
(cd backend && mvn -B package -DskipTests)

echo "==> 构建后端镜像: ${BACKEND_IMAGE}"
docker build "${backend_build_args[@]}" -t "$BACKEND_IMAGE" ./backend

echo "==> 构建前端镜像: ${FRONTEND_IMAGE}"
docker build "${frontend_build_args[@]}" -t "$FRONTEND_IMAGE" ./frontend

if [[ "$PUSH" == "1" ]]; then
  registry_login
  echo "==> 推送后端: ${BACKEND_IMAGE}"
  docker push "$BACKEND_IMAGE"
  echo "==> 推送前端: ${FRONTEND_IMAGE}"
  docker push "$FRONTEND_IMAGE"
  echo "==> 完成。部署时在服务器执行: docker compose pull && docker compose up -d"
else
  echo "==> 构建完成（未推送，PUSH=0）"
fi
