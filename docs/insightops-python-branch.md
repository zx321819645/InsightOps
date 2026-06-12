# InsightOps-Py — Python Agent 支线任务书

| 项目属性 | 说明 |
|---------|------|
| **项目名称** | InsightOps-Py — Python Multi-Agent AIOps Lab |
| **项目定位** | Java 主线的 **Agent 能力支线**，专注 Python 生态下的多智能体协作与 RAG |
| **项目周期** | 10 周（业余，约 12–15 小时/周，总计约 120–150 小时） |
| **与主线关系** | **独立仓库 / 独立部署**；可复用主线 MySQL 表结构与 Demo 数据，不要求与 Java 微服务强耦合 |
| **编制日期** | 2026 年 6 月 11 日 |

---

## 〇、为什么做这条支线

| 维度 | Java 主线（InsightOps） | Python 支线（InsightOps-Py） |
|------|------------------------|------------------------------|
| 核心目标 | 企业级微服务工程化 + Agent 落地 | **多 Agent 协作、RAG、快速迭代 Agent 效果** |
| 技术栈 | Spring Cloud + LangChain4j | FastAPI + LangGraph / LangChain |
| 架构风格 | 10 个微服务 | **模块化单体** 或 3 个轻量服务 |
| 面试叙事 | 「我会在 Java 工程里落地 AI」 | 「我熟悉 Python Agent 生态与多 Agent 编排」 |
| 适合阶段 | 主线按任务书推进 | Java 主线进入 Week 3+ 后并行，或周末专项 |

> **原则：** 支线不替代主线，而是补齐「Python Agent 主流实践」这一块简历与能力。

---

## 一、项目背景与定位

### 1.1 背景

Python 是 Agent / LLM 应用开发的事实标准语言。LangChain、LangGraph、CrewAI 等框架在多 Agent 协作、状态机编排、RAG 链路方面资料丰富、迭代快。InsightOps-Py 以 **与主线相同的运维 Copilot 业务场景** 为外壳，用 Python 技术栈完整走通：

- ReAct 单 Agent
- **Planner / Diagnostor / Executor 多 Agent 协作**
- RAG 知识库问答
- Tool Calling（指标 / 日志 / 知识库）
- Human-in-the-loop 危险操作确认
- 告警事件驱动自动分析

### 1.2 项目定位

一个可本地运行、可演示、可写进简历的 **Python AIOps 多智能体实验平台**。

与 Java 主线共享：

- 业务场景（故障诊断、告警分析、审批流）
- 数据库表设计（可复用 `insightops` 单库，或支线独立库 `insightops_py`）
- Demo 脚本与运维案例文档

与 Java 主线独立：

- 代码仓库、运行时、框架选型
- 不强依赖 Nacos / Spring Cloud Gateway（支线用 FastAPI 单体或轻量网关）

### 1.3 最终交付标准

- [ ] FastAPI 应用可本地一键启动（`uvicorn`）
- [ ] ReAct Agent + **3 类多 Agent 协作**（Planner / Diagnostor / Executor）
- [ ] 6+ Tool（queryMetrics、queryLogs、searchKnowledge、createTicket、restartService、scaleReplicas）
- [ ] RAG 知识库（Chroma 或 Milvus，10+ 篇运维案例）
- [ ] SSE 流式输出 + Agent Trace 可视化（JSON / 简单 Web UI）
- [ ] 告警模拟 → 自动触发多 Agent 分析
- [ ] Human-in-the-loop：危险操作需人工确认
- [ ] 3 个固定 Demo 场景 + README + 架构文档
- [ ] 核心模块 pytest 覆盖

### 1.4 简历描述（参考）

> 基于 FastAPI + LangGraph 的 AIOps 多智能体实验平台。实现 Planner / Diagnostor / Executor 协作编排，集成 ReAct Tool Calling、RAG 知识库检索与 SSE 流式诊断。支持告警事件驱动分析、Human-in-the-loop 危险操作确认与 Agent 全链路 Trace 记录。用于探索 Python Agent 生态下的多智能体工程化实践，并与 Java 主线 InsightOps 业务场景对齐。

---

## 二、技术栈清单

### 2.1 语言与运行时

| 技术 | 版本建议 | 用途 |
|------|---------|------|
| Python | 3.11+ | 主语言 |
| uv 或 poetry | 最新 | 依赖管理与虚拟环境 |
| pydantic | v2 | 请求/响应校验、Settings |

### 2.2 Web 与 API

| 技术 | 用途 |
|------|------|
| **FastAPI** | HTTP API、OpenAPI 文档、依赖注入 |
| uvicorn | ASGI 服务器 |
| SSE（`sse-starlette` 或 FastAPI `StreamingResponse`） | Agent 流式输出 |
| python-jose / PyJWT | JWT 签发与校验（与主线 secret 可对齐） |

### 2.3 AI / Agent 层（支线核心）

| 技术 | 用途 | 掌握要求 |
|------|------|---------|
| **LangGraph**（推荐） | 多 Agent 状态机、协作编排、条件分支 | 熟练 |
| LangChain | Tool、Memory、RAG 抽象、Prompt 模板 | 熟练 |
| CrewAI（可选对比） | 感受角色驱动多 Agent，Week 8 选修 | 了解 |
| ReAct | 推理 → Tool → 观察 → 再推理 | 必须理解 |
| DeepSeek API | 主 LLM（OpenAI 兼容接口） | 熟练 |
| DashScope `text-embedding-v3` 或 BGE | Embedding | 熟练 |
| Prompt Engineering | 系统提示词、结构化 JSON 输出 | 熟练 |

**选型说明：**

- **LangGraph** 作为多 Agent 主线（比 CrewAI 更可控、适合自定义状态）
- LangChain 作为 Tool / RAG / Model 封装
- 若时间紧，Week 5 前可用 LangChain `AgentExecutor` 做单 Agent，Week 5 再切 LangGraph 多 Agent

### 2.4 数据与中间件

| 技术 | 用途 |
|------|------|
| MySQL 8.0 + SQLAlchemy 2.0 | 业务数据（会话、消息、工单、Trace） |
| Alembic | 数据库迁移 |
| Redis | 会话 Memory 缓存、限流 |
| Chroma（默认）或 Milvus | 向量库（支线优先 Chroma，零运维） |
| Kafka（可选，Week 7） | 告警事件；时间紧可用 Redis Stream / 内存队列代替 |

### 2.5 运维数据适配

| 技术 | 用途 |
|------|------|
| Mock 指标/日志生成器（Python 脚本） | 无真实集群时的演示数据 |
| httpx | 调用 Prometheus / Loki API（Week 6 选修） |
| `data/knowledge/` | 10+ 篇 Markdown 故障案例 |

### 2.6 安全与治理

| 技术 | 用途 |
|------|------|
| FastAPI Depends + JWT | 认证鉴权 |
| RBAC（简化版） | admin / dev / readonly |
| Tool 白名单 + Pydantic 参数校验 | 防 Prompt 注入 |
| Human-in-the-loop | 危险 Tool 返回「待审批」，人工 API 确认后执行 |
| slowapi + Redis | API 限流 |

### 2.7 前端（可选，Week 9）

| 技术 | 用途 |
|------|------|
| Streamlit（推荐，快） | 对话 + Trace 可视化 Demo |
| 或 Vue 3（与主线共用前端） | 正式控制台 |

### 2.8 工程化

| 技术 | 用途 |
|------|------|
| pytest + pytest-asyncio | 单元 / 集成测试 |
| ruff | 代码格式化与 lint |
| GitHub Actions | CI：lint + test |
| `.env` + pydantic-settings | 配置管理（API Key 不入库） |

---

## 三、系统架构设计

### 3.1 推荐架构：模块化单体

支线 **不建议** 复刻 Java 的 10 微服务，采用 **单 FastAPI 应用 + 清晰包分层**：

```
insightops-py/
├── app/
│   ├── main.py                 # FastAPI 入口
│   ├── core/                   # 配置、安全、异常、响应体
│   ├── api/                    # 路由：auth、chat、agent、alert、ticket
│   ├── agents/                 # ★ Agent 编排（支线灵魂）
│   │   ├── react_agent.py      # 单 Agent ReAct
│   │   ├── multi_agent/        # 多 Agent 协作
│   │   │   ├── graph.py        # LangGraph 状态图
│   │   │   ├── planner.py
│   │   │   ├── diagnostor.py
│   │   │   └── executor.py
│   │   └── prompts/            # 各角色 Prompt 模板
│   ├── tools/                  # Tool 实现
│   │   ├── metrics.py
│   │   ├── logs.py
│   │   ├── knowledge.py
│   │   └── ticket.py
│   ├── rag/                    # 文档切片、Embedding、检索
│   ├── models/                 # SQLAlchemy 模型
│   ├── schemas/                # Pydantic DTO
│   ├── services/               # 业务逻辑
│   └── repositories/           # 数据访问
├── data/
│   ├── knowledge/              # 运维案例文档
│   └── mock/                   # Mock 指标/日志
├── tests/
├── alembic/                    # 数据库迁移
├── scripts/
│   ├── seed_data.py
│   └── demo_scenarios.py
├── .env.example
├── pyproject.toml
└── README.md
```

### 3.2 核心流程

#### 流程 A：用户提问诊断（单 Agent → 多 Agent 演进）

```
1. POST /api/v1/chat/sessions          创建会话
2. POST /api/v1/chat/sessions/{id}/messages  用户提问
3. agent_service 启动 LangGraph：
   a. Planner：拆解分析计划
   b. Diagnostor：调用 queryMetrics / queryLogs / searchKnowledge
   c. Executor：生成结构化诊断报告
4. SSE 流式返回 thought / tool_call / observation / answer
5. 消息与 Trace 写入 MySQL
```

#### 流程 B：告警驱动分析

```
1. POST /api/v1/alerts/simulate        模拟 CPU > 90% 告警
2. 事件进入队列（Kafka / Redis Stream / BackgroundTasks）
3. 自动启动多 Agent 分析
4. 结果写入 alert_analysis 表
```

#### 流程 C：Human-in-the-loop

```
1. Executor 判断需要 restartService
2. 调用 create_ticket Tool → 工单状态「待审批」
3. POST /api/v1/tickets/{id}/approve   管理员批准
4. 执行 restartService（Mock）并写审计日志
```

### 3.3 多 Agent 协作设计（LangGraph）

| Agent 角色 | 职责 | 可用 Tool |
|-----------|------|----------|
| **Planner** | 理解问题，输出 JSON 分析计划 | 无（纯 LLM 节点） |
| **Diagnostor** | 按计划收集证据 | queryMetrics, queryLogs, searchKnowledge |
| **Executor** | 综合证据，输出根因 + 建议 + 风险操作 | createTicket, restartService*, scaleReplicas* |

\* 需 Human-in-the-loop

**LangGraph 状态示例：**

```python
class AgentState(TypedDict):
    session_id: str
    user_question: str
    plan: str                    # Planner 输出
    tool_results: list[dict]     # Diagnostor 收集
    diagnosis: str               # 最终报告
    trace_steps: list[dict]      # 可视化用
```

---

## 四、与 Java 主线的关系

### 4.1 可复用

| 资源 | 说明 |
|------|------|
| `InsightOps数据库设计.md` | 表结构参考 |
| `InsightOps数据库初始化.sql` | 可共用 `insightops` 库，或 fork 为 `insightops_py` |
| `data/knowledge/` 故障案例 | 知识库 Demo 数据 |
| Demo 场景脚本 | 业务场景描述一致 |
| JWT secret | 若要做统一登录实验，secret 与 Gateway 对齐 |

### 4.2 不强制集成

| 场景 | 建议 |
|------|------|
| 与 Java Gateway 联调 | 选修：Python 作为独立服务 `http://localhost:8090` |
| 共用 Nacos | 不做，支线无需服务注册 |
| 代码复用 | 无，仅业务与数据对齐 |

### 4.3 未来可选：混合架构

```
Java Gateway → Java auth/chat
            → Python agent-service（HTTP，专做 Agent 编排）
```

主线 Week 10 后若有余力，可将本支线 `agents/` 模块封装为独立 FastAPI 服务，供 Java orchestrator Feign 调用。**不作为支线默认目标。**

---

## 五、详细任务计划（10 周）

> **时间假设：** 周六 6h + 周日 6h + 工作日碎片 2–3h ≈ 每周 12–15h

---

### 第 1 阶段：奠基（Week 1–2）

#### Week 1：工程骨架 + LLM 通路

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 1.1 | 初始化项目（uv/poetry、FastAPI、目录结构） | 仓库骨架 | 3h |
| 1.2 | 配置管理（`.env`、`Settings`、统一响应体） | `app/core/` | 2h |
| 1.3 | 接入 DeepSeek API，实现 `POST /api/v1/agent/hello` | hello 接口 | 2h |
| 1.4 | MySQL + SQLAlchemy 连接，Alembic 初始化 | 数据库连通 | 3h |
| 1.5 | 编写 `docs/tech-selection-py.md` | 技术选型 | 2h |

**验收：**
- [ ] `uvicorn app.main:app --reload` 启动成功
- [ ] hello 接口能调通 DeepSeek 并返回回复

---

#### Week 2：认证 + 会话 CRUD

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 2.1 | JWT 登录/注册（passlib bcrypt） | `/api/v1/auth/login` | 4h |
| 2.2 | RBAC 简化版（角色表 + Depends 鉴权） | 权限 Depends | 3h |
| 2.3 | 会话/消息 CRUD（`chat_session`、`chat_message`） | 会话 API | 4h |
| 2.4 | Redis 连接（为 Memory 做准备） | Redis 配置 | 2h |

**验收：**
- [ ] 登录拿 token → 创建会话 → 写入用户消息

---

### 第 2 阶段：Agent 核心（Week 3–6）— 支线灵魂

#### Week 3：ReAct 单 Agent + 3 个 Mock Tool

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 3.1 | 定义 Tool 规范（`@tool` + Pydantic 参数） | `app/tools/` | 3h |
| 3.2 | Mock：queryMetrics、queryLogs、searchKnowledge | 3 个 Tool | 5h |
| 3.3 | LangChain ReAct Agent（`create_react_agent`） | `react_agent.py` | 4h |
| 3.4 | Prompt 模板（运维专家 + JSON 输出约束） | `prompts/` | 2h |

**验收：**
- [ ] 「order-service 响应变慢」→ 依次调 3 个 Tool 并输出诊断

---

#### Week 4：RAG 知识库

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 4.1 | 文档加载与切片（Markdown / PDF） | `app/rag/loader.py` | 3h |
| 4.2 | Embedding + Chroma 向量存储 | `app/rag/store.py` | 4h |
| 4.3 | searchKnowledge Tool 接入真实 RAG | 检索 Tool | 3h |
| 4.4 | 准备 10+ 篇 `data/knowledge/` 案例 | Demo 数据 | 2h |
| 4.5 | `POST /api/v1/knowledge/upload` | 上传 API | 2h |

**验收：**
- [ ] 「Redis 连接超时怎么处理」→ 引用知识库片段回答

---

#### Week 5：Memory + SSE 流式 + Trace

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 5.1 | 会话 Memory（Redis + DB 持久化） | 多轮上下文 | 4h |
| 5.2 | SSE 流式返回 Agent 步骤 | `/messages/stream` | 4h |
| 5.3 | Agent Trace 结构化存储 | `agent_trace` 表 | 3h |
| 5.4 | pytest：Mock LLM 的 Agent 集成测试 | `tests/test_agent.py` | 3h |

**里程碑 M1：**
- [ ] 3 个问答 Demo 场景可演示
- [ ] 多轮对话 + 流式输出流畅

---

#### Week 6：LangGraph 多 Agent 协作 ★

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 6.1 | 设计 AgentState 与三角色 Prompt | 设计文档 | 3h |
| 6.2 | 实现 Planner 节点（输出 JSON 计划） | `planner.py` | 3h |
| 6.3 | 实现 Diagnostor 节点（带 Tool 的子图） | `diagnostor.py` | 4h |
| 6.4 | 实现 Executor 节点（报告 + 风险识别） | `executor.py` | 3h |
| 6.5 | 组装 LangGraph 状态图 + 条件边 | `graph.py` | 4h |

**验收：**
- [ ] 复杂问题 → Planner 拆任务 → Diagnostor 查数据 → Executor 出报告
- [ ] Trace 中可看到三个角色的步骤

---

### 第 3 阶段：业务闭环（Week 7–8）

#### Week 7：告警驱动 + 工单审批

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 7.1 | 告警模拟 API + 后台任务触发 Agent | 事件驱动 | 4h |
| 7.2 | createTicket / restartService / scaleReplicas Tool | 工单 Tool | 4h |
| 7.3 | 工单状态机 + 审批 API | Human-in-the-loop | 4h |
| 7.4 | 危险操作：未审批不执行 | 安全逻辑 | 2h |

**验收：**
- [ ] 模拟 CPU 告警 → 30 秒内自动分析
- [ ] Agent 建议重启 → 审批后才执行

---

#### Week 8：打磨 + 选修

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 8.1 | Mock 指标/日志数据生成器完善 | `scripts/mock_data.py` | 3h |
| 8.2 | API 限流（slowapi + Redis） | 限流中间件 | 2h |
| 8.3 | **选修：** CrewAI 版多 Agent 对比 Demo | `agents/crew_demo.py` | 4h |
| 8.4 | **选修：** Prometheus 真实指标对接 | adapter | 4h |

**里程碑 M2：**
- [ ] 告警 → 多 Agent → 审批流全链路打通

---

### 第 4 阶段：演示与文档（Week 9–10）

#### Week 9：Demo UI + 测试

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 9.1 | Streamlit 对话页（流式 + Trace 展示） | 演示 UI | 6h |
| 9.2 | 3 个固定 Demo 场景脚本 | `docs/demo-scripts-py.md` | 3h |
| 9.3 | pytest 覆盖率 > 60%（Agent、Tool、审批） | 测试报告 | 4h |

---

#### Week 10：文档 + 面试准备

| 序号 | 任务 | 产出物 | 工时 |
|------|------|--------|------|
| 10.1 | 架构文档（LangGraph 状态图、与 Java 对比） | `docs/architecture-py.md` | 3h |
| 10.2 | README（安装、启动、Demo 步骤） | `README.md` | 2h |
| 10.3 | 简历段落 + 2 个 STAR 故事 | 面试素材 | 3h |
| 10.4 | 录制 3 分钟演示视频（可选） | 演示视频 | 3h |

**里程碑 M3（支线交付）：**
- [ ] 3 个 Demo 场景全部跑通
- [ ] 文档齐全，能讲清 LangGraph 多 Agent 设计

---

## 六、里程碑总览

| 里程碑 | 时间 | 标志 |
|--------|------|------|
| **M0** | Week 2 末 | 登录 + 会话 CRUD |
| **M1** | Week 5 末 | ReAct + RAG + 流式 + Trace |
| **M2** | Week 8 末 | 多 Agent + 告警 + 审批闭环 |
| **M3** | Week 10 末 | Demo + 文档 + 面试准备 |

---

## 七、3 个固定 Demo 场景

| 场景 | 演示内容 | 亮点 |
|------|---------|------|
| 场景 1：性能劣化诊断 | 用户提问 → 多 Agent 查指标+日志+RAG → 结论 | LangGraph 协作 |
| 场景 2：告警自动分析 | 模拟告警 → 后台多 Agent → 更新分析报告 | 事件驱动 |
| 场景 3：危险操作审批 | Agent 建议重启 → 工单 → 人工批准 → 执行 | Human-in-the-loop |

---

## 八、API 设计参考

**前缀：** `/api/v1`（支线用 REST 风格，与 Java 主线路径不同，避免混淆）

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/v1/auth/login` | 登录 |
| 认证 | POST | `/api/v1/auth/register` | 注册 |
| 认证 | GET | `/api/v1/auth/me` | 当前用户 |
| 会话 | POST | `/api/v1/chat/sessions` | 创建会话 |
| 会话 | GET | `/api/v1/chat/sessions` | 会话列表 |
| 消息 | POST | `/api/v1/chat/sessions/{id}/messages` | 发消息（触发 Agent） |
| 消息 | GET | `/api/v1/chat/sessions/{id}/messages/stream` | SSE 流式 |
| 知识库 | POST | `/api/v1/knowledge/upload` | 上传文档 |
| 告警 | POST | `/api/v1/alerts/simulate` | 模拟告警 |
| 工单 | POST | `/api/v1/tickets/{id}/approve` | 审批 |
| Agent | GET | `/api/v1/agent/traces/{session_id}` | Trace 查询 |

**鉴权：** Header `Authorization: Bearer <token>` 或 `token: <token>`（与主线统一可选）

---

## 九、依赖示例（`pyproject.toml` 摘录）

```toml
[project]
name = "insightops-py"
version = "0.1.0"
requires-python = ">=3.11"
dependencies = [
    "fastapi>=0.110",
    "uvicorn[standard]>=0.27",
    "pydantic-settings>=2.0",
    "sqlalchemy>=2.0",
    "alembic>=1.13",
    "pymysql>=1.1",
    "redis>=5.0",
    "python-jose[cryptography]>=3.3",
    "passlib[bcrypt]>=1.7",
    "httpx>=0.27",
    "langchain>=0.2",
    "langchain-openai>=0.1",
    "langgraph>=0.2",
    "chromadb>=0.5",
    "sse-starlette>=2.0",
    "slowapi>=0.1",
]

[project.optional-dependencies]
dev = ["pytest>=8.0", "pytest-asyncio>=0.23", "ruff>=0.4"]
ui = ["streamlit>=1.35"]
```

---

## 十、环境变量（`.env.example`）

```env
DEEPSEEK_API_KEY=sk-xxx
DEEPSEEK_BASE_URL=https://api.deepseek.com/v1
DEEPSEEK_MODEL=deepseek-chat

DATABASE_URL=mysql+pymysql://root:root@localhost:3306/insightops_py
REDIS_URL=redis://localhost:6379/1

JWT_SECRET=zx040106
JWT_EXPIRE_HOURS=24

CHROMA_PERSIST_DIR=./data/chroma
```

---

## 十一、面试 STAR 故事预备

**故事 1：为什么用 LangGraph 而不是 CrewAI？**

- **S**：多 Agent 协作需要精确控制「先 Plan 再查数据再出方案」的顺序
- **T**：CrewAI 角色对话灵活但流程不易约束
- **A**：选用 LangGraph 状态图，明确定义节点、边与 AgentState
- **R**：协作流程可预测、Trace 可逐节点回放，便于调试与演示

**故事 2：Python 支线与 Java 主线的关系？**

- **S**：Agent 生态 Python 强，但目标公司技术栈偏 Java 微服务
- **T**：需要同时体现 Agent 能力与工程化能力
- **A**：主线 Spring Cloud 负责微服务治理，支线 Python 深耕多 Agent 与 RAG，业务场景对齐
- **R**：面试时可对比两种技术栈下 Agent 落地差异，体现技术广度

---

## 十二、风险与应对

| 风险 | 应对 |
|------|------|
| 双线并行时间不够 | 支线每周固定 12h，主线优先 M0/M1 |
| LangChain 版本迭代快 | 锁定版本号，README 注明 |
| 与 Java 主线混淆 | **独立仓库** `insightops-py`，独立端口 8090 |
| API Key 费用 | 开发期限流 + pytest Mock LLM |

---

## 十三、建议启动时机

| 主线进度 | 支线建议 |
|----------|----------|
| Week 2 完成 chat-service | 启动支线 Week 1 |
| 主线 Week 3–6 Agent 卡住 | 先在支线 Week 3–6 练熟，再回主线 |
| 主线 Week 10 多 Agent | 支线 Week 6 的 LangGraph 经验可直接迁移 |

---

**文档版本：** v1.0  
**关联文档：** `InsightOps项目任务书.md`（Java 主线）、`InsightOps数据库设计.md`
