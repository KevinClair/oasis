# Oasis V3 实施归档目录

本目录用于统一归档 Oasis V3 的实施计划、设计文档、SQL 脚本、API 协议、测试与发布材料。

## 目录索引
- `00-plan/`: 实施计划、里程碑、范围冻结
- `01-product/`: 产品方案、页面规格、交互说明
- `02-architecture/`: 技术架构、数据流、调度逻辑、HA/性能设计
- `03-sql/`: DDL/DML、迁移脚本、回滚脚本
- `04-api/`: Admin API、Executor 协议、OpenAPI（二期预留）
- `05-starter/`: `oasis-scheduler-spring-boot-starter` 设计与配置
- `06-admin/`: 后端实施记录
- `07-web/`: 前端页面实施记录
- `08-test/`: 测试计划与报告
- `09-release/`: 发布与回滚手册
- `10-progress/`: 每日进展与阶段报告

## 命名规范
- 方案文档：`*_V3.md`
- SQL：`V3_YYYYMMDD_序号_描述.sql`
- 回滚 SQL：`V3_YYYYMMDD_序号_描述_rollback.sql`
- 进展日志：`progress_YYYYMMDD.md`
- 阶段报告：`phaseN_report.md`

## 当前阶段
- 当前里程碑：Phase 1（MVP）
- 当前状态：目录与文档基线已建立

## 强约束
- 新增实施文档与 SQL 统一写入本目录，不再散落到仓库根目录。
- 表名统一无前缀（例如 `job_info`、`job_schedule`）。
