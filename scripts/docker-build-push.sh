#!/usr/bin/env bash
# 本地构建 admin / admin-ui 镜像并推送到镜像仓库
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
ADMIN_IMAGE_NAME="${ADMIN_IMAGE_NAME:-${BACKEND_IMAGE_NAME:-games-platform-admin}}"
ADMIN_UI_IMAGE_NAME="${ADMIN_UI_IMAGE_NAME:-${FRONTEND_IMAGE_NAME:-games-platform-admin-ui}}"
BUILD_REGISTRY="${BUILD_REGISTRY:-docker.m.daocloud.io/library}"
OPENJDK_IMAGE="${OPENJDK_IMAGE:-mcr.microsoft.com/openjdk/jdk:21-ubuntu}"
PLATFORM="${PLATFORM:-linux/amd64}"
PUSH="${PUSH:-1}"

ADMIN_IMAGE="${IMAGE_REGISTRY}/${ADMIN_IMAGE_NAME}:${IMAGE_TAG}"
ADMIN_UI_IMAGE="${IMAGE_REGISTRY}/${ADMIN_UI_IMAGE_NAME}:${IMAGE_TAG}"
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

configure_openjdk_image() {
  local local_platform
  local requested_platform="${PLATFORM%%,*}"

  local_platform="$(docker image inspect \
    --format '{{.Os}}/{{.Architecture}}' \
    "$OPENJDK_IMAGE" 2>/dev/null || true)"

  if [[ "$local_platform" == "$requested_platform" ]]; then
    echo "==> 使用本地 OpenJDK 基础镜像: ${OPENJDK_IMAGE} (${local_platform})"
    admin_build_args+=(--pull=false)
    return
  fi

  if [[ -n "$local_platform" ]]; then
    echo "==> 本地 OpenJDK 镜像平台为 ${local_platform}，与目标平台 ${requested_platform} 不一致"
  else
    echo "==> 本地未找到 OpenJDK 基础镜像: ${OPENJDK_IMAGE}"
  fi
  echo "==> 构建时将从远程仓库拉取 OpenJDK 基础镜像"
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

admin_build_args=(--platform "$PLATFORM" --build-arg "OPENJDK_IMAGE=${OPENJDK_IMAGE}")
admin_ui_build_args=(--platform "$PLATFORM" --build-arg "REGISTRY=${BUILD_REGISTRY}")

echo "==> 目标平台: ${PLATFORM}"
ensure_buildx
configure_openjdk_image

echo "==> Maven 打包后端"
(cd admin && mvn -B package -DskipTests)

echo "==> 构建 Admin 镜像: ${ADMIN_IMAGE}"
docker build "${admin_build_args[@]}" -t "$ADMIN_IMAGE" ./admin

echo "==> 构建 Admin UI 镜像: ${ADMIN_UI_IMAGE}"
docker build "${admin_ui_build_args[@]}" -t "$ADMIN_UI_IMAGE" ./admin-ui

if [[ "$PUSH" == "1" ]]; then
  registry_login
  echo "==> 推送 Admin: ${ADMIN_IMAGE}"
  docker push "$ADMIN_IMAGE"
  echo "==> 推送 Admin UI: ${ADMIN_UI_IMAGE}"
  docker push "$ADMIN_UI_IMAGE"
  echo "==> 完成。部署时在 docker/app 目录执行: ./deploy.sh"
else
  echo "==> 构建完成（未推送，PUSH=0）"
fi
