# Công cụ (Tools)

Các script hỗ trợ cục bộ để quản lý repository SDD, kiểm tra (audit) KMS và khởi tạo dự án.

## Yêu cầu hệ thống (Prerequisites)

| Công cụ | Yêu cầu bởi | Cài đặt |
|------|-------------|---------|
| PowerShell 5.1+ | `kb.ps1`, `init-project.ps1`, `init-obsidian-taxonomy.ps1` | Có sẵn trên Windows |
| Python 3.8+ | `kms_ci_audit.py` | [python.org](https://www.python.org/) |
| [ripgrep](https://github.com/BurntSushi/ripgrep) (`rg`) | `kb.ps1` | `winget install BurntSushi.ripgrep.MSVC` |
| PyYAML | `kms_ci_audit.py` | `pip install pyyaml` |

---

## 1. `kb.ps1` — KMS Knowledge Base CLI

Công cụ chính để truy xuất và kiểm tra (audit) SDD knowledge base. Script này bao bọc lệnh `ripgrep` với các đường dẫn tìm kiếm theo repository cùng với file cấu hình `.ripgreprc`.

### Cách sử dụng

```powershell
.\tools\kb.ps1 <lệnh> ["từ_khóa"]
```

### Các lệnh

#### Tìm kiếm & Truy xuất (Search & Retrieval)

| Lệnh | Mô tả | Ví dụ |
|---------|-------------|---------|
| `docs "từ_khóa"` | Chỉ tìm trong `docs/` (`.md`, `.yaml`, `.yml`) | `.\tools\kb.ps1 docs "snapshot"` |
| `workspace "từ_khóa"` | Chỉ tìm trong `llm-wiki/` (`.md`) | `.\tools\kb.ps1 workspace "delta"` |
| `specs "từ_khóa"` | Tìm trong `docs/04-spec-test/` và `tests/` | `.\tools\kb.ps1 specs "efficiency"` |
| `code "từ_khóa"` | Tìm trong `src/` và `tests/` | `.\tools\kb.ps1 code "parse_total"` |
| `search "từ_khóa"` | Tìm mọi nơi (`docs/`, `llm-wiki/`, `tests/`, `src/`) | `.\tools\kb.ps1 search "AWR"` |

#### Ngữ cảnh & Truy xuất nguồn gốc (Context & Traceability)

| Lệnh | Mô tả | Ví dụ |
|---------|-------------|---------|
| `context "từ_khóa"` | Gom toàn bộ ngữ cảnh (grounding → docs → workspace → specs → source) | `.\tools\kb.ps1 context "slide13"` |
| `trace "tính_năng"` | Truy vết một tính năng qua docs, specs, workspace, source và blockers | `.\tools\kb.ps1 trace "instance_efficiency"` |
| `sync-check "tính_năng"` | Kiểm tra trạng thái đồng bộ giữa requirements, specs, tests, source và các mâu thuẫn | `.\tools\kb.ps1 sync-check "shared_pool"` |

#### Kiểm tra & Chất lượng (Audit & Quality)

| Lệnh | Mô tả |
|---------|-------------|
| `status` | Hiển thị các tham chiếu đến mô hình hoạt động KMS |
| `grounding` | Hiển thị tóm tắt các file grounding |
| `blockers` | Tìm tất cả `BLOCKED`, `TBD`, `TODO`, `FIXME`, open questions (câu hỏi mở), contradictions (mâu thuẫn) |
| `audit` | Kiểm tra toàn diện KMS (grounding + metadata + thiếu frontmatter + thiếu liên kết + mâu thuẫn) |
| `audit-ci` | Chạy bộ kiểm tra CI bằng Python (`kms_ci_audit.py`) |
| `audit-metadata` | Hiển thị các metadata marker (`type:`, `status:`, `tags:`, v.v.) |
| `missing-meta` | Liệt kê các file Markdown thiếu YAML frontmatter (`---`) |
| `missing-links` | Liệt kê các file Markdown không có liên kết hoặc tham chiếu nguồn |
| `contradictions` | Hiển thị nhật ký mâu thuẫn (contradiction log) và các marker xung đột |
| `ready` | Kiểm tra mức độ sẵn sàng của SDD (vision, glossary, C4, BPMN, specs, guardrails) |

#### Trợ giúp (Help)

```powershell
.\tools\kb.ps1 help    # hoặc chỉ cần chạy: .\tools\kb.ps1
```

---

## 2. `kms_ci_audit.py` — KMS CI Audit Gate

Công cụ tự động kiểm tra tính nhất quán của KMS/SDD. Được thiết kế để chạy trong CI pipeline hoặc chạy cục bộ thông qua `kb.ps1 audit-ci`.

### Cách sử dụng

```powershell
# Chạy qua kb.ps1 (được khuyến nghị)
.\tools\kb.ps1 audit-ci

# Chạy trực tiếp
python tools/kms_ci_audit.py
python tools/kms_ci_audit.py --strict-links   # Báo lỗi nếu thiếu bất kỳ liên kết nào trong docs/
```

### Script kiểm tra những gì?

| Hạng mục kiểm tra | Mô tả |
|-------|-------------|
| **Manifest paths** | Tất cả các đường dẫn trong `docs/grounding/manifest.yaml` (`kms_registry`, `sources`) phải tồn tại trên ổ đĩa |
| **Sources catalog** | Tất cả `source_id` trong manifest phải được tham chiếu trong `docs/grounding/sources.md` |
| **Lifecycle manifest sync** | Tài liệu lifecycle phải đề cập đến `version` và `last_updated` hiện tại của manifest |
| **Critical links** | Các file quản trị quan trọng (`AGENTS.md`, `manifest.md`, `sources.md`, `prompts.md`, v.v.) không được có liên kết hỏng |
| **Contradictions** | Tất cả các mục trong `llm-wiki/contradiction-log.md` phải được giải quyết/đóng |
| **Verification evidence** | Các file bằng chứng xác minh bắt buộc (QA matrix, DoD, runbook, v.v.) phải tồn tại |
| **All docs links** | *(nếu dùng cờ `--strict-links`)* Không có liên kết Markdown nào bị hỏng trong thư mục `docs/` |

### Đầu ra (Output)

```
PASS manifest paths
PASS sources catalog
FAIL critical links
  - docs/grounding/manifest.md:42 has missing link target ...
RESULT FAIL: 1 blocking KMS audit issue(s)
```

Mã thoát (Exit code): `0` = PASS (Đạt), `1` = FAIL (Lỗi).

---

## 3. `init-project.ps1` — Project Initializer

Khởi tạo repository starter SDD cho một dự án mới bằng cách thay thế placeholder `fcli` thành tên dự án của bạn.

### Cách sử dụng

```powershell
# Chạy thử trước (được khuyến nghị) — xem trước những thay đổi mà không sửa file
.\tools\init-project.ps1 -ProjectName my-new-project -DryRun

# Áp dụng thay đổi
.\tools\init-project.ps1 -ProjectName my-new-project
```

### Script làm gì?

1. Quét tất cả các file `.md`, `.yaml`, `.yml` trong `docs/`, `llm-wiki/`, `specs/`
2. Quét các file quản trị gốc: `AGENTS.md`, `GEMINI.md`, `CLAUDE.md`, `README.md`, `.cursorrules`
3. Thay thế mọi từ `fcli` → `<TênDựÁn>`
4. Đặt lại nội dung các file trong `llm-wiki/tasks/current/*.md` về các template rỗng

### Các bước sau khi khởi tạo

```powershell
git diff                    # Xem lại tất cả các thay đổi
.\tools\kb.ps1 audit-ci    # Xác minh tính nhất quán của KMS
git add -A && git commit -m "init: rename project to my-new-project"
```

---

## 4. `init-obsidian-taxonomy.ps1` — Obsidian Property Value Seeds

Tạo các seed note (ghi chú mẫu) cho tính năng tự động hoàn thành giá trị thuộc tính trong Obsidian. Tạo các file Markdown có cấu trúc bên trong `docs/_obsidian/property-values/` và `docs/00-governance/obsidian-property-values/` để Obsidian có thể gợi ý giá trị cho frontmatter.

### Cách sử dụng

```powershell
.\tools\init-obsidian-taxonomy.ps1
```

### Script tạo ra những gì?

Các seed note được tổ chức theo nhóm thuộc tính:

| Thuộc tính | Các giá trị mẫu |
|----------|---------------|
| `category` | `grounding`, `governance`, `methodology`, `requirement`, `architecture`, `workspace_index`, ... |
| `type` | `methodology`, `guide`, `technical_note`, `vision`, `glossary`, `architecture_c4`, `bpmn`, ... |
| `status` | `draft`, `reviewed`, `published`, `deprecated`, `observed`, `verified`, ... |
| `truth_level` | `official`, `working`, `observed`, `assumption`, `archived` |
| `source_policy` | `source_of_truth`, `source_catalog`, `machine_registry`, `workspace_only`, ... |
| `tags` | `kms/grounding`, `sdd/vision`, `agent/context`, `workspace/index`, `risk/security`, ... |

Mỗi seed note chứa YAML frontmatter với giá trị thuộc tính và một liên kết đến `taxonomy.md`, cho phép Obsidian đưa ra các gợi ý thuộc tính.

### Các thư mục đầu ra

```
docs/_obsidian/property-values/{category,type,status,...}/*.md
docs/00-governance/obsidian-property-values/{category,type,status,...}/*.md
```

---

## Bảng tra cứu nhanh (Quick Reference)

```powershell
# Quy trình làm việc hàng ngày
.\tools\kb.ps1 context "my-feature"     # Thu thập ngữ cảnh trước khi viết code
.\tools\kb.ps1 trace "my-module"        # Truy vết tính năng qua tất cả các tầng
.\tools\kb.ps1 blockers                 # Kiểm tra các vấn đề đang mở (open issues)

# Kiểm soát chất lượng (Quality gates)
.\tools\kb.ps1 audit                    # Kiểm tra toàn diện KMS
.\tools\kb.ps1 audit-ci                 # Kiểm tra cấp độ CI
.\tools\kb.ps1 sync-check "feature"     # Kiểm tra đồng bộ giữa docs ↔ code

# Thiết lập dự án (chỉ làm 1 lần)
.\tools\init-project.ps1 -ProjectName my-project -DryRun
.\tools\init-project.ps1 -ProjectName my-project
.\tools\init-obsidian-taxonomy.ps1
```
