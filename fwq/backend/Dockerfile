# ============================================
# SnowIsland 后端 Dockerfile（服务器部署版）
# ============================================
# 基础镜像（Java 17）
FROM eclipse-temurin:17-jdk-alpine

# 作者
MAINTAINER xp

# 设置时区
ENV TZ=Asia/Shanghai

# 同步系统时间
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建日志与图鉴上传目录
RUN mkdir -p /var/log/snowisland /usr/local/java/uploads/catalog

# 工作目录
WORKDIR /usr/local/java

# 暴露端口
EXPOSE 8080

# 复制 JAR 包（需要在构建前将 JAR 包放到 backend 目录下）
COPY snowisland.jar ./snowisland.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/ark/status || exit 1

# 启动命令
ENTRYPOINT ["java", "-Dspring.profiles.active=online", "-Dspring.config.location=/usr/local/java/config/application.yml,/usr/local/java/config/application-online.yml", "-jar", "/usr/local/java/snowisland.jar"]