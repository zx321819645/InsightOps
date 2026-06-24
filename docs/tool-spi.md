# InsightOps Tool SPI 设计文档

| 属性 | 说明 |
|------|------|
| **文档版本** | v1.0 |
| **编制日期** | 2026-06-23 |
| **对应任务** | Week 3 · 3.1 Tool 接口规范设计 |
| **状态** | 已定稿，供 3.2–3.4 及 Week 4 拆服务使用 |

---

## 1. 文档目的

本文档定义 InsightOps Agent 平台中 **Tool（工具）** 的统一接口规范，包括：

- Tool 元数据模型（名称、描述、分类、风险等级）
- 入参 **JSON Schema** 约定
- 出参 **统一执行结果** 封装
- 与 **LangChain4j `@Tool`** 的映射关系
- Week 3 Mock 实现 → Week 4 远程 `tool-service` 的演进路径

**不在本文范围：** Prompt 模板（任务 3.4）、ReAct 编排逻辑（任务 3.3）、tool-service 数据库表设计（任务 4.1）。

---

## 2. 设计原则

| 原则 | 说明 |
|------|------|
| **LLM 可读** | `name` + `description` 是 Agent 选 Tool 的唯一依据，描述必须写清「何时该用、输入什么、返回什么」 |
| **Schema 先行** | 所有入参必须有 JSON Schema；Week 3 用 Java 类型 + 校验注解落地，Week 4 同步写入 `tool-service` 注册表 |
| **结果可观测** | 每次 Tool 调用必须返回 `success / errorCode / durationMs / data`，供 Trace 与 audit-service 消费 |
| **Mock 可替换** | Week 3 在 orchestrator 内 Mock；Week 7 换真实 adapter 时，**Tool 对外 Schema 不变** |
| **白名单管控** | Agent 只能调用注册过的 Tool；危险 Tool 需审批，Week 9 再开放 |
| **失败可恢复** | Tool 失败时返回结构化错误，不抛未捕获异常，让 ReAct 循环能继续推理 |

---

## 3. 总体架构

```
┌─────────────────────────────────────────────────────────────┐
│  agent-orchestrator                                          │
│  ┌─────────────┐    ┌──────────────┐    ┌─────────────────┐ │
│  │ ReAct Agent │───▶│ ToolRegistry │───▶│ ToolExecutor    │ │
│  │ (AiServices)│    │ (白名单)      │    │ (Mock / Remote) │ │
│  └─────────────┘    └──────────────┘    └────────┬────────┘ │
│         ▲                                         │          │
│         │ @Tool 方法 / JSON Schema                │          │
│  ┌──────┴──────┐                                  │          │
│  │ LangChain4j │                                  │          │
│  └─────────────┘                                  │          │
└───────────────────────────────────────────────────┼──────────┘
                                                    │
         Week 3（本阶段）                            │ Week 4+
         本地 Mock 实现 ◀────────────────────────────┘
                                                    │
                              Feign ──▶ tool-service ──▶ monitor/log/knowledge adapter
                                                    │
                              audit-service ◀── 每次调用审计
```

| 阶段 | Tool 执行位置 | 注册方式 |
|------|---------------|----------|
| **Week 3** | orchestrator 进程内 Mock | Spring `@Component` + 代码注册 |
| **Week 4** | tool-service 远程执行 | MySQL 注册表 + Redis 缓存 |
| **Week 7** | adapter 提供真实数据 | Mock 实现替换为 Feign 调用，Schema 不变 |
| **Week 9** | 危险 Tool + 审批流 | 增加 `riskLevel=HIGH`，执行前创建工单 |

---

## 4. Tool 元数据模型

每个 Tool 在系统中用以下元数据描述（Week 4 持久化到 `sys_tool` 表，Week 3 用常量或配置类）。

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 全局唯一标识，**camelCase**，与 LangChain4j `@Tool` 方法名一致，如 `queryMetrics` |
| `displayName` | string | 是 | 人类可读名称，如「查询指标」 |
| `description` | string | 是 | 给 LLM 看的说明，建议 1–3 句，含使用场景与限制 |
| `category` | enum | 是 | `METRICS` / `LOGS` / `KNOWLEDGE` / `TICKET` / `ACTION` |
| `riskLevel` | enum | 是 | `LOW`（只读）/ `MEDIUM`（写操作但可回滚）/ `HIGH`（需审批） |
| `enabled` | boolean | 是 | 是否对 Agent 可见；默认 `true` |
| `timeoutMs` | int | 否 | 单次执行超时，默认 `10000` |
| `parameterSchema` | JSON Schema | 是 | 入参结构定义（见第 5 节） |
| `version` | string | 否 | 语义化版本，如 `1.0.0` |

### 4.1 命名约定

- **Tool 名称**：动词 + 名词，camelCase，如 `queryMetrics`、`searchKnowledge`、`createTicket`
- **参数名称**：camelCase，与 JSON Schema `properties` 键一致
- **禁止**：空格、中文、特殊字符；LLM 对 snake_case 也可工作，但项目统一 camelCase

### 4.2 风险等级与 Agent 行为

| riskLevel | 含义 | Agent 行为 | 开放阶段 |
|-----------|------|------------|----------|
| `LOW` | 只读查询 | 直接调用 | Week 3 起 |
| `MEDIUM` | 创建工单等可逆写操作 | 直接调用，audit 记录 | Week 9 |
| `HIGH` | 重启、扩容等 | 仅创建审批工单，人工批准后才执行 | Week 9 |

Week 3 仅实现 `LOW` 级别的 3 个 Mock Tool。

---

## 5. 入参 JSON Schema 规范

### 5.1 Schema 版本与格式

- 使用 **JSON Schema Draft 2020-12**（LangChain4j / OpenAI Function Calling 均兼容子集）
- 每个 Tool 的 Schema 为 **object 类型**，`additionalProperties: false`
- 必填字段列入 `required` 数组

### 5.2 通用字段约定

| 字段 | 类型 | 说明 |
|------|------|------|
| `serviceName` | string | 目标微服务名，如 `order-service`；多个 Tool 共用 |
| `startTime` / `endTime` | string | ISO-8601 或相对描述，如 `-15m`；日志/指标查询共用 |
| `limit` | integer | 结果条数上限，默认 20，最大 100 |

### 5.3 Java 落地方式（LangChain4j 0.36.2）

Week 3 推荐两种等价方式，**优先方式 A**：

**方式 A：`@Tool` + `@P` 注解（推荐）**

```java
@Tool("查询指定服务在时间范围内的性能指标，包括 QPS、P99 延迟、错误率。当用户问响应变慢、CPU、内存、错误率时使用。")
public ToolResult queryMetrics(
        @P("目标微服务名称，如 order-service") String serviceName,
        @P("查询起始时间，ISO-8601 或相对时间如 -15m") String startTime,
        @P("查询结束时间，ISO-8601 或 now") String endTime,
        @P("返回数据点数量上限，默认 20") Integer limit) {
    // ...
}
```

LangChain4j 会自动将 `@P` 描述转为 JSON Schema 供 LLM 使用。

**方式 B：显式 JSON Schema 文档**

用于 tool-service 注册表存储、OpenAPI 文档生成；Week 3 在 `docs/tool-schemas/` 或常量类中维护，与 `@P` 描述保持同步。

### 5.4 Schema 示例（queryMetrics）

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "type": "object",
  "additionalProperties": false,
  "properties": {
    "serviceName": {
      "type": "string",
      "description": "目标微服务名称，如 order-service",
      "minLength": 1,
      "maxLength": 64,
      "pattern": "^[a-z][a-z0-9-]*$"
    },
    "startTime": {
      "type": "string",
      "description": "查询起始时间，ISO-8601 或相对时间如 -15m、-1h",
      "default": "-15m"
    },
    "endTime": {
      "type": "string",
      "description": "查询结束时间，ISO-8601 或 now",
      "default": "now"
    },
    "limit": {
      "type": "integer",
      "description": "返回数据点数量上限",
      "minimum": 1,
      "maximum": 100,
      "default": 20
    }
  },
  "required": ["serviceName"]
}
```

---

## 6. 出参：ToolResult 统一封装

Tool 方法**不直接返回原始字符串**给 LangChain4j，而是返回 `ToolResult`，由框架层序列化为 LLM 可读的 observation 文本。

### 6.1 数据结构

```java
/**
 * Tool 执行统一结果（Week 3 定义于 agent-orchestrator，Week 4 下沉至 insightops-common）
 */
public class ToolResult {

    /** 是否执行成功 */
    private boolean success;

    /** 业务错误码，成功时为 0 */
    private int errorCode;

    /** 错误描述，成功时为 null */
    private String errorMessage;

    /** 执行耗时（毫秒） */
    private long durationMs;

    /** Tool 名称，冗余便于 Trace */
    private String toolName;

    /** 业务载荷，结构因 Tool 而异 */
    private Object data;

    /** 给 LLM 阅读的摘要（必填，即使 success=false 也要有） */
    private String summary;
}
```

### 6.2 与 LLM Observation 的映射

ReAct 循环中，LangChain4j 将 Tool 返回值作为 **Observation** 注入下一轮 Prompt。约定：

| 场景 | 返回给 LLM 的内容 |
|------|-------------------|
| 成功 | `summary` + 精简后的 `data` JSON（避免 Token 爆炸，单条 observation 建议 < 2KB） |
| 失败 | `summary` 说明失败原因，**不抛异常**，让 Agent 换策略或告知用户 |
| 超时 | `success=false`, `errorCode=504`, summary 提示「指标服务超时，请缩小时间范围」 |

### 6.3 JSON 响应示例

**成功：**

```json
{
  "success": true,
  "errorCode": 0,
  "errorMessage": null,
  "durationMs": 42,
  "toolName": "queryMetrics",
  "summary": "order-service 近 15 分钟 P99 延迟从 200ms 升至 2.1s，错误率 0.3%→2.1%",
  "data": {
    "serviceName": "order-service",
    "points": [
      { "time": "2026-06-23T14:15:00+08:00", "p99Ms": 2100, "errorRate": 0.021 }
    ]
  }
}
```

**失败：**

```json
{
  "success": false,
  "errorCode": 40001,
  "errorMessage": "serviceName 不能为空",
  "durationMs": 1,
  "toolName": "queryMetrics",
  "summary": "参数校验失败：缺少 serviceName，请指定要查询的微服务名称",
  "data": null
}
```

### 6.4 错误码段

| 区间 | 含义 |
|------|------|
| `0` | 成功 |
| `40001–40099` | 参数校验失败 |
| `40401–40499` | 目标服务/资源不存在 |
| `50001–50099` | Tool 内部错误 |
| `50401` | 执行超时 |

---

## 7. Week 3 首批 Tool 定义

以下 3 个 Tool 为任务 3.2 的实现目标，Schema 在本节定稿后**不得随意变更**（Week 7 换真实 adapter 时保持兼容）。

### 7.1 queryMetrics — 查询性能指标

| 属性 | 值 |
|------|-----|
| name | `queryMetrics` |
| displayName | 查询性能指标 |
| category | `METRICS` |
| riskLevel | `LOW` |
| description | 查询指定微服务在时间范围内的 QPS、P99/P95 延迟、CPU/内存使用率、错误率等 Prometheus 指标。当用户描述「变慢、卡顿、超时、CPU 高、错误率上升」时使用。 |

**入参 Schema：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| serviceName | string | 是 | — | 微服务名 |
| startTime | string | 否 | `-15m` | 起始时间 |
| endTime | string | 否 | `now` | 结束时间 |
| metrics | string[] | 否 | `["p99", "error_rate"]` | 指标名列表 |
| limit | integer | 否 | 20 | 数据点数上限 |

**出参 data 结构：**

```json
{
  "serviceName": "order-service",
  "timeRange": { "start": "...", "end": "..." },
  "points": [
    {
      "time": "2026-06-23T14:30:00+08:00",
      "p99Ms": 2100,
      "p95Ms": 850,
      "qps": 1200,
      "errorRate": 0.021,
      "cpuUsage": 0.78
    }
  ]
}
```

**Week 3 Mock 行为：** 固定返回 order-service 劣化场景数据，用于验收「响应变慢」Demo。

---

### 7.2 queryLogs — 查询应用日志

| 属性 | 值 |
|------|-----|
| name | `queryLogs` |
| displayName | 查询应用日志 |
| category | `LOGS` |
| riskLevel | `LOW` |
| description | 查询指定微服务的应用日志，支持关键词、日志级别、时间范围过滤。当需要排查异常栈、错误信息、超时原因时使用。应在 queryMetrics 发现异常后调用以定位根因。 |

**入参 Schema：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| serviceName | string | 是 | — | 微服务名 |
| startTime | string | 否 | `-15m` | 起始时间 |
| endTime | string | 否 | `now` | 结束时间 |
| keyword | string | 否 | null | 关键词，如 `TimeoutException` |
| level | string | 否 | `ERROR` | 日志级别：DEBUG/INFO/WARN/ERROR |
| limit | integer | 否 | 20 | 条数上限 |

**出参 data 结构：**

```json
{
  "serviceName": "order-service",
  "totalMatched": 3,
  "entries": [
    {
      "timestamp": "2026-06-23T14:30:12+08:00",
      "level": "ERROR",
      "message": "Redis connection timeout after 3000ms",
      "traceId": "abc123",
      "stackTrace": "..."
    }
  ]
}
```

**Week 3 Mock 行为：** 返回 Redis 连接超时相关 ERROR 日志，与 queryMetrics 劣化时间对齐。

---

### 7.3 searchKnowledge — 检索运维知识库

| 属性 | 值 |
|------|-----|
| name | `searchKnowledge` |
| displayName | 检索运维知识库 |
| category | `KNOWLEDGE` |
| riskLevel | `LOW` |
| description | 从运维知识库检索与故障、组件、操作手册相关的文档片段。当需要修复建议、标准处理步骤、类似案例时使用。根据日志/指标中发现的组件名（如 Redis、MySQL、Kafka）构造查询。 |

**入参 Schema：**

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| query | string | 是 | — | 自然语言检索词 |
| topK | integer | 否 | 3 | 返回片段数 |
| category | string | 否 | null | 文档分类过滤：incident/runbook/faq |

**出参 data 结构：**

```json
{
  "query": "Redis connection timeout 处理",
  "chunks": [
    {
      "docTitle": "Redis 连接超时故障手册",
      "docId": "kb-redis-timeout-001",
      "score": 0.92,
      "content": "1. 检查 maxclients 配置...",
      "source": "knowledge-base"
    }
  ]
}
```

**Week 3 Mock 行为：** 返回 1–3 条硬编码的 Redis 超时处理片段；Week 5 替换为 knowledge-service + Milvus 真实检索，**入参/出参 Schema 不变**。

---

## 8. LangChain4j 集成规范

### 8.1 接口分层

```
com.insightops.agent.tool
├── ToolResult.java              # 统一出参
├── ToolExecutor.java            # 执行入口（校验 → 计时 → 调用 → 封装）
├── OpsTools.java                # @Tool 方法集合，供 AiServices 绑定
├── mock/
│   ├── MockMetricsTool.java
│   ├── MockLogsTool.java
│   └── MockKnowledgeTool.java
└── registry/
    └── ToolRegistry.java        # 白名单与元数据查询
```

### 8.2 Agent 绑定方式

```java
public interface DiagnostorAgent {

    @SystemMessage(fromResource = "prompts/diagnostor-system.txt")
    String diagnose(@UserMessage String userQuestion);
}

// 构建
DiagnostorAgent agent = AiServices.builder(DiagnostorAgent.class)
    .chatLanguageModel(chatModel)
    .tools(opsTools)           // 注入 @Tool 方法所在 Bean
    .maxSequentialToolsInvocations(8)
    .build();
```

### 8.3 Tool 描述编写规范（给 LLM）

每条 `@Tool` 描述建议包含：

1. **做什么**（能力）
2. **何时用**（触发条件，与其他 Tool 的协作顺序）
3. **输入/output 概要**
4. **限制**（只读、Mock 数据、时间范围建议）

示例（queryMetrics）：

> 查询指定微服务在时间范围内的性能指标（QPS、P99 延迟、错误率等）。当用户反馈服务变慢、超时、错误率升高时使用。通常作为诊断第一步，发现异常后再调用 queryLogs 查日志。

---

## 9. 参数校验与安全

### 9.1 校验层次

| 层次 | 位置 | 内容 |
|------|------|------|
| L1 Schema | ToolExecutor 入口 | 必填、类型、枚举、长度、正则 |
| L2 白名单 | ToolRegistry | 只允许已注册 Tool；Week 3 硬编码 3 个 |
| L3 业务 | 各 Tool 实现 | 时间范围不超过 24h、limit 上限 100 |
| L4 审批 | ticket-service | 仅 `riskLevel=HIGH`（Week 9） |

### 9.2 防 Prompt 注入

- Tool 参数**不得**直接拼接到 SQL、Shell、PromQL
- `serviceName` 只允许 `[a-z][a-z0-9-]*` 模式
- `keyword` / `query` 做长度限制（如 256 字符），过滤控制字符
- Agent 系统 Prompt 声明：「仅基于 Tool 返回的 data 下结论，不得臆造指标数值」

---

## 10. 审计与 Trace（Week 4+ 预留）

Week 3 在日志中打印结构化 Tool 调用；Week 4 起写入 audit-service。

| 字段 | 说明 |
|------|------|
| traceId | 与 Gateway / Agent 请求一致 |
| sessionId | 对话会话 ID |
| toolName | Tool 名称 |
| inputJson | 入参快照 |
| outputSummary | ToolResult.summary |
| success | 是否成功 |
| durationMs | 耗时 |
| createdAt | 调用时间 |

---

## 11. 后续 Tool 路线图（参考）

| name | category | riskLevel | 阶段 |
|------|----------|-----------|------|
| queryMetrics | METRICS | LOW | Week 3 Mock → Week 7 真实 |
| queryLogs | LOGS | LOW | Week 3 Mock → Week 7 真实 |
| searchKnowledge | KNOWLEDGE | LOW | Week 3 Mock → Week 5 RAG |
| createTicket | TICKET | MEDIUM | Week 9 |
| restartService | ACTION | HIGH | Week 9 |
| scaleReplicas | ACTION | HIGH | Week 9 |

---

## 12. 验收对照（Week 3）

任务 3.1 交付物检查：

- [x] Tool 元数据模型定义
- [x] 入参 JSON Schema 规范 + 3 个 Tool 完整 Schema
- [x] 出参 ToolResult 统一封装
- [x] LangChain4j `@Tool` / `@P` 映射说明
- [x] Mock → 远程 → 真实 adapter 演进路径

任务 3.2 实现时，以此文档为唯一 Schema 来源；若需变更字段，先更新本文档版本号再改代码。

---

## 13. 参考

- [LangChain4j Tools](https://docs.langchain4j.dev/tutorials/tools)
- [OpenAI Function Calling Schema](https://platform.openai.com/docs/guides/function-calling)
- 项目内 [tech-selection.md](./tech-selection.md) §4 LangChain4j 使用范围
