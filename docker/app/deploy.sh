#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

legacy_containers=(games-platform-backend games-platform-frontend)

echo "==> 拉取 Admin / Admin UI 镜像"
docker compose pull

for container in "${legacy_containers[@]}"; do
  if docker container inspect "$container" >/dev/null 2>&1; then
    echo "==> 检测到旧容器 ${container}，正在下线"
    docker rm -f "$container"
  fi
done

echo "==> 启动 Admin / Admin UI，并清理同一 Compose 项目的旧服务"
docker compose up -d --force-recreate --remove-orphans

echo "==> 当前应用服务状态"
docker compose ps
