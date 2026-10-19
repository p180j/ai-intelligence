# AI Intelligence

AI 前沿情报系统后端，基于 Java 8、Spring Boot 2.7、Spring JDBC 和 MySQL。

## 本地运行

默认配置使用 H2 的 MySQL 兼容模式，便于本地启动与 API 验收：

```bash
mvn spring-boot:run
```

生产/集成环境使用 MySQL profile。先在空数据库执行 `sql/schema.sql`；已有数据库的当前增量放入 `sql/upgrade.sql`，验证后合并回 schema。MySQL 不依赖 Hibernate 或 Flyway 自动建表：

```bash
java -jar target/ai-intelligence-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=mysql \
  --DB_URL='jdbc:mysql://localhost:3306/ai_intelligence?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai' \
  --DB_USERNAME=root \
  --DB_PASSWORD=your-password
```

也可以通过环境变量 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 提供连接信息。

## 验证

```bash
mvn clean test
```

API 根路径为 `/admin`，提供 Source、Article、Event、Product、Collector 和 AI Pipeline 管理能力。配置 `COLLECTOR_ENABLED=true` 开启定时采集；AI Provider 通过 `AI_BASE_URL`、`AI_API_KEY`、`AI_MODEL` 配置，密钥不得写入源码。

## Phase 4/5 页面

- `/admin`：Dashboard、Event 审核、Article/Source 排错、Product 与候选管理、Daily Report 编辑发布。
- `/`：最新已发布日报。
- `/event/{id}`：已审核并发布的 Event 详情与原始来源。
- `/product/{slug}`：Product 信息与已发布 Event 时间线。
- `/search?q=`：Event 和 Product 关键词搜索。

Phase 6 增加了 Event 质量反馈、误合并/漏合并 Eval Case、日报质量反馈、每日运营统计和 Admin Session 登录。管理后台要求管理员密码和 6 位 TOTP 动态码；生产环境必须设置 `ADMIN_PASSWORD` 与 `ADMIN_TOTP_SECRET`，完整上线清单见 [docs/production.md](docs/production.md)。

已有 Phase 2/3 数据库升级到 Phase 4/5 前，执行一次 `sql/upgrade.sql`。空数据库直接执行包含全部结构的 `sql/schema.sql`。
