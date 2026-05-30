# 国内网络默认走 DaoCloud 镜像加速，可自行覆盖：
# docker build --build-arg REGISTRY=docker.io/library -t games-platform-frontend .
ARG REGISTRY=docker.m.daocloud.io/library

# 构建阶段
FROM ${REGISTRY}/node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm config set registry https://registry.npmmirror.com \
    && npm ci

COPY . .
RUN npm run build

# 运行阶段
FROM ${REGISTRY}/nginx:1.27-alpine

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=builder /app/dist /usr/share/nginx/html

ENV BACKEND_HOST=backend
ENV BACKEND_PORT=8080

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
