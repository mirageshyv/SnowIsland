# ============================================
# SnowIsland 前端 Dockerfile（服务器部署版）
# ============================================
# 使用 Nginx 提供静态文件（需要预先打包好 dist 目录）

FROM nginx:alpine

# 设置时区
ENV TZ=Asia/Shanghai

# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 复制预先打包好的前端静态文件（需要在构建前将 dist 目录放到 frontend 目录下）
COPY dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]