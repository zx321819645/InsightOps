# InsightOps 技术选型文档

| 属性 | 说明 |
|------|------|
| **文档版本** | v1.2 |
| **编制日期** | 2026-06-10 |
| **LLM 选型** | DeepSeek API（主模型） |
| **项目阶段** | 第 1 阶段 · Week 1 |
| **状态** | 已确认 |

---

## 1. 文档目的

本文档记录 InsightOps 企业级 AIOps Agent 平台的技术选型结论与依据，供开发、联调、面试讲解时使用。所有选型以满足以下目标为前提：

- 可演示、可部署、可写进简历
- 在职学习场景下环境搭建成本可控（Windows 本地、无 Docker）
- 体现 Spring Cloud 微服务工程化 + AI Agent 编排能力
- 国内网络环境下 LLM 调用稳定、成本可控

---

## 2. 选型原则

| 原则 | 说明 |
|------|------|
| **工程优先** | 优先选择团队/个人已有经验的 Java 生态，降低上手成本 |
| **纵向切片** | 早期选型以「能跑通 Demo」为准，避免过度设计 |
| **Mock 先行** | 监控/日志适配器初期用 Mock，Week 7 再对接真实 API |
| **本地友好** | 中间件支持 Windows 本地安装；LLM 统一走 DeepSeek API，无需本地 GPU |
| **面试可讲** | 每个核心选型需能回答「为什么不用 X」 |

---

## 3. 总体架构技术栈

```
┌─────────────────────────────────────────────────────────────┐
│  Vue 3 + Element Plus + SSE（前端控制台）                    │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTPS / SSE
┌───────────────────────────▼─────────────────────────────────┐
│  Spring Cloud Gateway（路由 · JWT 鉴权 · 限流）              │
└───────────────────────────┬─────────────────────────────────┘
                            │
     ┌──────────────────────┼──────────────────────┐
     ▼                      ▼                      ▼
 auth-service          chat-service      agent-orchestrator
                                              │
                         LangChain4j（ReAct · Tool · Memory · RAG）
                                              │
              ┌───────────────┬───────────────┼───────────────┐
              ▼               ▼               ▼               ▼
        tool-service   knowledge-service  ticket-service  audit-service
              │               │
              ▼               ▼
    monitor/log-adapter    Milvus + Embedding
              │
              ▼
         alert-service ──Kafka──→ agent-orchestrator

基础设施：Nacos · MySQL 8 · Redis · Kafka · Flyway
可观测性：Micrometer · SkyWalking · 结构化 JSON 日志
```

---

## 4. 核心选型：LangChain4j

### 4.1 决策结论

**AI / Agent 层选用 LangChain4j**，作为 Agent 编排、Tool Calling、Memory、RAG 的统一抽象层，集成在 `insightops-agent-orchestrator` 服务中。

| 对比项 | LangChain4j | Spring AI |
|--------|-------------|-----------|
| Agent / ReAct 支持 | 成熟，`AiServices` + Tool 注解，ReAct 模式文档完整 | 较新，Agent 编排能力仍在快速演进 |
| Tool Calling | `@Tool` 注解 + JSON Schema，与 Function Calling 对齐 | `@Tool` / Function 支持，但 Agent 链路示例较少 |
| RAG 链路 | EmbeddingStore、DocumentSplitter、Retriever 组件齐全 | 有 VectorStore 抽象，Milvus 集成相对较新 |
| 多模型适配 | Ollama、OpenAI 兼容 API（DeepSeek/通义）、Azure 等 | 以 Spring 生态为主，模型适配持续增加 |
| 社区与资料 | Agent 模式教程多，与 Python LangChain 概念一致，面试通用 | Spring 官方背书，但 Agent 实战文章相对少 |
| 与 Spring Boot 集成 | 提供 `langchain4j-spring-boot-starter`，可无缝接入 | 原生 Spring 项目，集成最自然 |
| 学习曲线（本项目） | Agent/RAG 文档路径清晰，适合 Week 3–6 集中攻关 | 需同时跟进 Spring AI 迭代，在职时间风险更高 |

### 4.2 选择 LangChain4j 的理由

1. **Agent 是项目灵魂**：InsightOps 的核心卖点是 ReAct + 多 Tool + RAG + 多 Agent 协作。LangChain4j 在 Agent、Tool、Memory、RAG 四条线上文档和示例更完整，更适合 Week 3–6 按任务书推进。
2. **ReAct 模式落地路径清晰**：任务书要求实现「推理 → 调 Tool → 观察 → 再推理」循环；LangChain4j 的 `AiServices` 与 Tool 机制与此模型直接对应，减少自研编排框架的工作量。
3. **RAG 组件开箱即用**：知识库服务需要文档切片、Embedding、向量检索；LangChain4j 提供 `DocumentSplitter`、`EmbeddingStore`（含 Milvus 集成），与 `knowledge-service` 职责划分明确。
4. **DeepSeek API 作为主 LLM**：开发与演示统一使用 **DeepSeek API**（OpenAI 兼容接口），LangChain4j 通过 `langchain4j-open-ai` 配置 `baseUrl` 即可接入；国内访问稳定、性价比高，Tool Calling 能力满足 Agent 需求。
5. **面试表达友好**：LangChain 体系是业界 Agent 讨论的事实标准，便于用同一套语言解释「Planner / Diagnostor / Executor」多 Agent 设计。

### 4.3 不选 Spring AI 的原因（简述）

Spring AI 与 Spring Boot 3 集成更「正统」，且后续可能随 Spring 生态继续增强。但本项目时间盒为 16 周、在职学习，**优先选择 Agent 能力更成熟、示例更丰富的方案**，以降低 Week 3–6 的核心路径风险。若 Spring AI Agent 能力在后续版本明显超越，可在 Tool 层抽象不变的前提下考虑迁移。

### 4.4 LangChain4j 在本项目中的使用范围

| 能力 | 使用方式 | 所在服务 |
|------|----------|----------|
| Chat 模型 | `OpenAiChatModel` → DeepSeek API（`deepseek-chat`） | agent-orchestrator |
| ReAct Agent | `AiServices` + 系统 Prompt | agent-orchestrator |
| Tool Calling | `@Tool` 方法 + 远程 Feign 封装 | orchestrator 定义，tool-service 执行 |
| Memory | `ChatMemory` + Redis 持久化 | agent-orchestrator + chat-service |
| RAG | `EmbeddingStore` + Retriever | knowledge-service 写入，orchestrator 检索 |
| 流式输出 | `StreamingChatLanguageModel` + SSE | agent-orchestrator → chat-service |

### 4.5 LLM 选型：DeepSeek API

#### 决策结论

**对话、推理、Tool Calling 统一使用 DeepSeek API**，不再以 Ollama 作为主路径。Ollama 仅作为无网络 / 零 API 费用时的可选备用，不作为项目默认方案。

| 用途 | 选型 | 模型 / 说明 |
|------|------|-------------|
| **Agent 对话 / ReAct 推理** | DeepSeek API | `deepseek-chat`（默认） |
| **复杂推理（可选）** | DeepSeek API | `deepseek-reasoner`（Planner 等重推理场景按需切换） |
| **Embedding（RAG）** | 独立 Embedding 服务 | DeepSeek 暂未提供公开 Embedding API；Week 5 采用 **DashScope `text-embedding-v3`** 或本地 BGE 模型，向量维度固定后不可随意更换 |
| **API 规范** | OpenAI 兼容 | `https://api.deepseek.com/v1`，LangChain4j 使用 `OpenAiChatModel` |

#### 选择 DeepSeek API 的理由

1. **国内在职友好**：注册与充值方便，无需本地部署大模型，Week 1 即可跑通 hello-agent。
2. **OpenAI 兼容**：LangChain4j 零改造接入，Tool Calling / 流式输出均支持。
3. **效果与成本平衡**：`deepseek-chat` 在运维问答、JSON 结构化输出上表现稳定；价格低于 GPT-4 级别模型，适合 Agent 多轮 + 多 Tool 调用场景。
4. **开发演示一致**：避免「本地 Ollama 效果差、演示切 API 行为不一致」的问题。

#### LangChain4j 配置参考

```yaml
# application-local.yml（API Key 勿提交 Git，使用环境变量）
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.deepseek.com/v1
      api-key: ${DEEPSEEK_API_KEY}
      model-name: deepseek-chat
      temperature: 0.3          # Agent 场景偏低，减少幻觉
      max-tokens: 4096
      timeout: 60s
      log-requests: true        # 开发期开启，便于调试
      log-responses: true
```

```java
// 编程式配置（LangChain4j 原生）
OpenAiChatModel model = OpenAiChatModel.builder()
    .baseUrl("https://api.deepseek.com/v1")
    .apiKey(System.getenv("DEEPSEEK_API_KEY"))
    .modelName("deepseek-chat")
    .temperature(0.3)
    .build();
```

**环境变量约定：**

| 变量名 | 说明 |
|--------|------|
| `DEEPSEEK_API_KEY` | DeepSeek 平台 API Key |
| `DEEPSEEK_BASE_URL` | 可选，默认 `https://api.deepseek.com/v1` |
| `DEEPSEEK_MODEL` | 可选，默认 `deepseek-chat` |

#### 成本与安全策略

| 策略 | 说明 |
|------|------|
| **Gateway 限流** | 按用户 / IP 限制 Agent 接口 QPS，防止刷量 |
| **Redis 计数** | 记录每日 Token 调用量，开发环境可设告警阈值 |
| **集成测试 Mock** | CI 中使用 Mock `ChatLanguageModel`，不产生 API 费用 |
| **Key 管理** | API Key 仅通过环境变量或 Nacos 加密配置注入，禁止写入代码库 |
| **Prompt 约束** | 限制最大 ReAct 轮次（如 8 轮），避免 Tool 循环导致费用失控 |

#### 备用方案

| 方案 | 场景 |
|------|------|
| **Ollama + Qwen2.5** | API 不可用或希望零成本离线调试时手动切换 profile |
| **Mock LLM** | 单元测试、CI、无 Key 的新成员本地启动 |

---

## 5. 后端与微服务

| 技术 | 版本 | 选型理由 |
|------|------|----------|
| **Java** | 17 LTS | Spring Boot 3 基线要求；LTS 稳定 |
| **Spring Boot** | 3.2.x | 与 Spring Cloud 2023.x 对齐 |
| **Spring Cloud** | 2023.0.x（ Leyton ） | 成熟微服务治理；与 Nacos、Gateway、OpenFeign 配套 |
| **Spring Cloud Gateway** | 随 Cloud BOM | 统一入口、JWT 校验、限流；替代 Zuul |
| **Nacos** | 2.3.x | 注册中心 + 配置中心合一；国内文档丰富；Windows 单机可启动 |
| **OpenFeign** | 随 Cloud BOM | orchestrator 远程调用 tool-service 等 |
| **Spring Validation** | 随 Boot | 统一参数校验 |
| **MapStruct** | 1.5.x | DTO/Entity 转换，减少样板代码 |
| **Flyway** | 随 Boot | 数据库版本管理，与 22 张表初始化脚本配合 |

**服务拆分**：按任务书 10 个后端微服务 + common + frontend，初期在 Maven 父工程中占位，按周迭代激活模块。

**数据库**：MySQL 8.0 单库 `insightops`，22 张表按前缀分模块（`sys_` / `chat_` / `agent_` 等），在职阶段便于维护，后续可按前缀拆库。

### 5.1 API 路径规范

所有对外接口经 **Gateway（`:8080`）** 统一暴露，路径风格为 **`/{服务前缀}/{模块}/...`**，**不使用** `/api/v1/` 前缀。

| 约定项 | 说明 |
|--------|------|
| **Gateway 路由** | `Path=/{服务前缀}/**`，与服务短名一致 |
| **context-path** | 各微服务 `server.servlet.context-path=/{服务前缀}` |
| **Controller 模块** | `@RequestMapping("/{模块}")`，模块名与业务域一致 |
| **完整路径** | `/{服务前缀}/{模块}/{动作}`，如 `/auth/auth/login` |
| **JWT 白名单** | 登录/注册等公开接口，路径与上述规范一致 |

**服务路径一览**（Gateway 外部路径 = 服务 context-path + Controller 路径）：

| 服务 | Gateway 路由 | context-path | 示例 |
|------|-------------|--------------|------|
| agent-orchestrator | `/agent-orchestrator/**` | `/agent-orchestrator` | `GET /agent-orchestrator/agent/hello` |
| auth-service | `/auth/**` | `/auth` | `POST /auth/auth/login` |
| chat-service | `/chat/**` | `/chat` | `POST /chat/chat/sessions` |
| tool-service | `/tool/**` | `/tool` | `GET /tool/tool/list` |
| knowledge-service | `/knowledge/**` | `/knowledge` | `POST /knowledge/knowledge/upload` |
| ticket-service | `/ticket/**` | `/ticket` | `POST /ticket/ticket` |
| audit-service | `/audit/**` | `/audit` | `GET /audit/audit/logs` |
| alert-service | `/alert/**` | `/alert` | `GET /alert/alert/rules` |
| monitor-adapter | `/monitor/**` | `/monitor` | `POST /monitor/monitor/query` |
| log-adapter | `/log/**` | `/log` | `POST /log/log/query` |

**JWT 白名单（Gateway 配置）**：

```yaml
insightops:
  jwt:
    whitelist:
      - /auth/auth/login
      - /auth/auth/register
      - /agent-orchestrator/agent/hello   # 开发期 Demo，auth 完成后可删除
```

**说明**：`sys_permission.path` 中 RBAC 的 API 路径（如 `/api/alerts/**`）为权限校验用的资源标识，后续 auth-service 实现时需与 Gateway 实际路由对齐改写。

---

## 6. 数据与中间件

| 技术 | 版本 | 用途 | 安装时机 |
|------|------|------|----------|
| **MySQL** | 8.0+ | 主业务库 | Week 1 |
| **Redis** | 7.x（Windows 用 Memurai） | 会话缓存、限流、分布式锁 | Week 1 |
| **Nacos** | 2.3.x | 服务注册与配置 | Week 1 |
| **Kafka** | 3.6+ | 告警事件 `alert.triggered` | Week 8 |
| **Milvus** | 2.x / Milvus Lite | 知识库向量检索 | Week 5 |
| **MinIO** | 可选 | 文档对象存储 | Nice to Have，初期本地磁盘 |

**部署方式**：Windows 本地安装，**不使用 Docker**（与任务书一致），降低在职环境下 Docker Desktop 资源占用与网络问题。

---

## 7. 运维数据适配

| 技术 | 阶段策略 |
|------|----------|
| **Prometheus HTTP API** | Week 7 接入 `monitor-adapter`；之前 Tool 层 Mock |
| **Grafana API** | 查询面板与告警规则，优先级低于 Prometheus |
| **ELK / Loki** | Week 7 先用文件日志 + Mock；接口预留 |
| **Mock 数据生成器** | Week 3 起用于 Demo，无真实 K8s 集群时可演示 |

---

## 8. 安全与治理

| 技术 | 说明 |
|------|------|
| **Spring Security + JWT** | auth-service 签发（`/auth/auth/login`）；Gateway 统一校验 |
| **RBAC** | 角色：管理员、运维、开发、只读；表：`sys_user` / `sys_role` / `sys_permission` |
| **Human-in-the-loop** | 危险 Tool（重启、扩容）走 ticket-service 审批，不直接执行 |
| **Gateway 限流** | Redis + 自定义 Filter，按用户/IP 限制 LLM 相关接口 |
| **Prompt 注入防护** | Tool 白名单、参数 JSON Schema 严格校验、禁止 Agent 自行扩展 Tool |

---

## 9. 可观测性

| 技术 | 说明 |
|------|------|
| **Micrometer + Prometheus** | 各服务 `/actuator/prometheus`，Week 11 接入 |
| **SkyWalking** | 分布式链路追踪（优先 SkyWalking，Zipkin 为备选） |
| **结构化 JSON 日志** | 统一 `traceId` 贯穿 Gateway → Agent → Tool |
| **Agent Trace** | `audit-service` 持久化 thought / action / observation |

---

## 10. 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue 3** | 3.4+ | Composition API + TypeScript |
| **Element Plus** | 最新 2.x | 管理后台组件 |
| **SSE** | — | Agent 流式输出与思考过程展示（优先 SSE，WebSocket 备选） |

前端 Week 13 启动，前期用 Postman / curl + Swagger 验证后端。

---

## 11. 工程化

| 技术 | 说明 |
|------|------|
| **Maven 多模块** | 单仓库管理全部微服务 |
| **JUnit 5 + Mockito** | 单元测试 |
| **@SpringBootTest** | 集成测试（MySQL + Redis，Mock LLM） |
| **GitHub Actions** | CI：编译 + 单元测试（优先 Actions，Jenkins 为备选） |
| **SpringDoc OpenAPI** | Swagger UI 自动生成 API 文档 |

---

## 12. 版本矩阵（父 POM 参考）

```xml
<!-- 建议在父 pom.xml 中统一管理 -->
<java.version>17</java.version>
<spring-boot.version>3.2.5</spring-boot.version>
<spring-cloud.version>2023.0.1</spring-cloud.version>
<langchain4j.version>0.36.2</langchain4j.version>
<!-- 具体版本以 Week 1 搭建时 Maven Central 最新稳定版为准 -->
```

| 依赖 | 坐标示例 |
|------|----------|
| LangChain4j BOM | `dev.langchain4j:langchain4j-bom` |
| LangChain4j Spring Boot | `dev.langchain4j:langchain4j-spring-boot-starter` |
| LangChain4j OpenAI（DeepSeek 兼容） | `dev.langchain4j:langchain4j-open-ai` |
| LangChain4j Milvus | `dev.langchain4j:langchain4j-milvus` |
| LangChain4j Ollama（可选备用） | `dev.langchain4j:langchain4j-ollama` |
| Nacos Discovery | `com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery` |

> Spring Cloud Alibaba 版本需与 Spring Cloud 2023.0.x 兼容，搭建父工程时查阅 [官方版本对照表](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)。

---

## 13. 明确不采用或延后的方案

| 方案 | 决定 | 原因 |
|------|------|------|
| **Spring AI** | 不采用 | 见 4.3；Agent 成熟度与学习时间不匹配 |
| **Docker Compose 一键环境** | 不采用 | 任务书要求本地安装；Windows 在职环境优先 |
| **多库分库** | 延后 | 单库 22 表降低 Week 1–10 复杂度 |
| **K8s 部署** | 不在范围内 | 项目定位本地可演示 + 简历项目 |
| **Ollama 本地模型** | 备用 | 主路径已定为 DeepSeek API；仅作离线 / 零费用备选 |
| **Reranker** | Nice to Have | 有余力再在 RAG 链路增加重排序 |
| **MinIO** | 可选 | 初期知识库文档存本地目录 |

---

## 14. 风险与应对

| 风险 | 应对 |
|------|------|
| LangChain4j 版本迭代快 | 父 POM 锁定版本；升级前在分支验证 Agent 用例 |
| DeepSeek API 费用超预期 | Gateway 限流 + ReAct 最大轮次 + 开发期 log 监控 Token；集成测试 Mock |
| API 网络不稳定 | 配置合理 timeout / 重试；Resilience4j 熔断（Week 12）；备用 Ollama profile |
| Milvus Windows 安装复杂 | 优先 Milvus Lite 或 Standalone 安装包 |
| 微服务过多导致进度滞后 | 按任务书 Week 10 评估，合并非核心服务 |
| LLM 幻觉 | ReAct + RAG + 「仅基于 Tool 数据回答」Prompt 约束 |

---

## 15. 选型结论汇总

| 层次 | 选型 |
|------|------|
| 语言 / 框架 | Java 17 + Spring Boot 3.2 + Spring Cloud 2023 |
| 微服务治理 | Nacos + Gateway + OpenFeign |
| **AI / Agent** | **LangChain4j + ReAct + Tool Calling + RAG** |
| LLM | **DeepSeek API**（`deepseek-chat`，OpenAI 兼容） |
| 向量库 | Milvus |
| 业务库 | MySQL 8 单库 + Flyway |
| 缓存 / 消息 | Redis + Kafka |
| 安全 | Spring Security + JWT + RBAC + Human-in-the-loop |
| 可观测 | Micrometer + SkyWalking + Agent Trace |
| 前端 | Vue 3 + Element Plus + SSE |
| 构建 / CI | Maven 多模块 + GitHub Actions |

---

## 16. 参考资料

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [LangChain4j Tools](https://docs.langchain4j.dev/tutorials/tools)
- [LangChain4j RAG](https://docs.langchain4j.dev/tutorials/rag)
- [Spring Cloud 2023.0 文档](https://spring.io/projects/spring-cloud)
- [Nacos 快速开始](https://nacos.io/docs/latest/quickstart/quick-start/)
- [Milvus 文档](https://milvus.io/docs)
- [DeepSeek API 文档](https://platform.deepseek.com/api-docs/)
- [DeepSeek 模型 & 定价](https://platform.deepseek.com/docs)

---

**下次评审**：Week 2 结束（M0 里程碑）—— 确认 Gateway + Auth + Chat 与 LangChain4j + DeepSeek hello-agent Demo 均运行正常。
