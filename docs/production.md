# 生产运行清单

生产形态保持 `Nginx → Spring Boot → MySQL`。不需要 Redis、MQ、微服务或 Kubernetes。

## 必需环境变量

```text
SPRING_PROFILES_ACTIVE=mysql,prod
DB_URL=jdbc:mysql://127.0.0.1:3306/ai_intelligence?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
DB_USERNAME=...
DB_PASSWORD=...
AI_PROVIDER=deepseek
AI_BASE_URL=https://api.deepseek.com
AI_MODEL=deepseek-flash
AI_API_KEY=...
ADMIN_USERNAME=admin
ADMIN_PASSWORD=...
ADMIN_TOTP_SECRET=...
PUBLIC_BASE_URL=https://ai.example.com
LOG_FILE=/var/log/ai-intelligence/application.log
COLLECTOR_ENABLED=true
```

禁止把数据库密码、管理员密码和 API Key 写入 Git。曾经出现在聊天、截图或日志里的真实密钥必须轮换。

`ADMIN_TOTP_SECRET` 是管理员验证器使用的 Base32 密钥，至少 32 个字符（160 位）。首次部署时离线生成随机密钥，在阿里云 App、Google Authenticator 或 Microsoft Authenticator 中选择“输入设置密钥”，账户类型选择“基于时间”，并将同一个密钥保存到部署环境。密钥不得写入源码、提交到 Git 或通过在线二维码网站转换。

## 上线步骤

1. 对现有 MySQL 做全量备份，并验证备份文件可以恢复。
2. 在维护窗口执行 `sql/upgrade.sql`。
3. 使用 `mysql --force` 之外的默认失败即停止模式，确认所有 DDL 成功。
4. 启动 Spring Boot，检查日志和 `/`；访问 `/admin` 应跳转登录页。
5. 按 `deploy/nginx.conf.example` 配置域名、HTTPS 和反向代理。
6. 配置每日 MySQL 备份、备份保留周期和异机存储。
7. 配置日志轮转、磁盘告警与进程守护。

## 7～14 天试运行

每天在管理台“运营统计”记录并复盘：Article、去重 Article、Event、合并、误合并、漏合并、AI_FAILED、批准数、审核耗时、Token，以及日报/Event/Product 访问。Event 审核必须填写质量反馈；典型匹配案例进入 Eval Cases。
