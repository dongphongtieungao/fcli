---
type: methodology
status: published
owner: unknown
domain: unknown
project: unknown
created: 2026-04-27
updated: 2026-04-27
sensitivity: internal
source_of_truth:
  - docs/grounding/manifest.yaml
  - docs/grounding/manifest.md
  - docs/methodology/methodology.md
  - docs/methodology/d_3.document_strategy_and_creation_order.md
related:
  
  
  - docs/MOC_Methodology.md
  - docs/methodology/d_3.document_strategy_and_creation_order.md
  - docs/methodology/d_1.quickstarts.md
tags:
  - normalized
---
# BÁO CÁO SÁNG KIẾN

## Context

This file was normalized as part of the repository-wide Obsidian-compatible technical wiki migration. The original content below is preserved unless explicitly noted.

## Purpose

This document preserves the existing content of `claude_agentic_template/methodology/0_specs_develop_idea.md` and connects it to the repository documentation graph for Agentic Spec First work.

## Chuẩn hóa giai đoạn phát triển ý tưởng và đặc tả theo hướng Spec-First với AI Coding Agents đa mô hình

**Phiên bản rà soát:** 2026-04-21  
**Phạm vi:** Giai đoạn hình thành ý tưởng, phát triển yêu cầu và chuẩn bị đặc tả trước khi triển khai code  
**Mục tiêu sử dụng:** Làm tài liệu tham chiếu cho cá nhân, nhóm kỹ thuật, BA, PM và AI agents khi chuyển một ý tưởng thô thành bộ đặc tả có thể thực thi

---

## 1. Hoàn cảnh nảy sinh sáng kiến

Trong quá trình ứng dụng AI coding agents vào phát triển phần mềm, nhiều nhóm đạt được tốc độ tạo mã rất cao nhưng lại phát sinh một vấn đề lớn hơn: ý tưởng đi vào code quá sớm, trong khi đặc tả, giới hạn và tiêu chí chấp nhận chưa đủ chặt. Kết quả là sản phẩm đầu ra có thể chạy được nhưng lệch mục tiêu, khó bảo trì, tốn nhiều vòng sửa và làm tăng technical debt.

Quan sát thực tiễn cho thấy khó khăn không còn nằm chủ yếu ở việc “viết code cho nhanh”, mà nằm ở việc:

- Chuyển ý tưởng ban đầu thành đặc tả có thể giao cho agent.
- Chọn đúng model cho đúng loại việc.
- Kiểm soát vòng lặp phản hồi để tránh agent tự suy diễn hoặc đi sai hướng.
- Giảm phụ thuộc vào cảm tính cá nhân khi chọn công cụ và quy trình.

Sáng kiến này được đề xuất để chuẩn hóa giai đoạn đầu của GenAI SDLC, biến quá trình phát triển ý tưởng thành một quy trình có thể lặp lại, đo lường được và đủ bền trước tốc độ thay đổi rất nhanh của các model hiện đại.

---

## 2. Thực trạng trước khi áp dụng

| Vấn đề phổ biến | Biểu hiện thường gặp | Tác động |
|---|---|---|
| Đặc tả hình thành chậm | Mất nhiều thời gian viết PRD từ đầu, nhiều chi tiết bị bỏ sót | Chậm bắt đầu triển khai, yêu cầu thay đổi liên tục |
| Agent tự “build everything” | Tự viết lại những thành phần đã có thư viện tốt trên thị trường | Tốn token, tốn thời gian, chất lượng không ổn định |
| Kiến trúc bị trôi dạt | Mỗi vòng code sinh ra một pattern khác nhau | Review khó, codebase loạn chuẩn, tăng nợ kỹ thuật |
| Một model bị kẹt kéo dài | Agent lặp lại cùng một hướng sai hoặc báo hoàn thành giả | Mất thời gian debug, giảm niềm tin vào hệ thống |
| Phối hợp đa agent thiếu kiểm soát | Chia frontend/backend/tester song song nhưng ghép kết quả khó | Conflict, hiểu sai yêu cầu, khó hợp nhất đầu ra |

Nhận xét cốt lõi là: nếu không thiết kế tốt giai đoạn “phát triển ý tưởng và làm rõ đặc tả”, AI chỉ giúp tăng tốc sai lầm chứ không giúp tăng tốc chất lượng.

---

## 3. Mục tiêu của sáng kiến

Sáng kiến hướng tới 5 mục tiêu chính:

1. Rút ngắn thời gian từ ý tưởng thô đến đặc tả sẵn sàng triển khai.
2. Giảm số vòng sửa do hiểu sai yêu cầu ở pha đầu.
3. Tăng tỷ lệ tận dụng thư viện và giải pháp có sẵn thay vì để agent tự xây lại từ đầu.
4. Chuẩn hóa cách chọn model theo loại nhiệm vụ thay vì theo thói quen cá nhân.
5. Tạo bộ đầu ra có thể bàn giao trực tiếp cho planner/coder/tester agents trong quy trình Spec-First.

---

## 4. Mô tả bản chất sáng kiến

### 4.1. Tư tưởng cốt lõi

Sáng kiến này dựa trên 5 nguyên tắc:

- **Spec đi trước code**: không để agent triển khai sâu khi đặc tả còn mơ hồ.
- **Con người giữ vai trò thiết kế feedback loop**: AI không thay thế trách nhiệm ra quyết định cấp cao.
- **Build-vs-buy trước tự phát triển**: luôn rà soát thư viện và giải pháp thị trường trước khi sinh mã mới.
- **Chọn model theo hình dạng nhiệm vụ**: dùng model theo năng lực phù hợp, không dùng một model cho mọi việc.
- **Mỗi pha phải có bằng chứng đầu ra**: mọi bước đều tạo artifact rõ ràng để bước sau dùng lại được.

### 4.2. Quy trình đề xuất

| Bước | Nội dung thực hiện | Mục tiêu | Đầu ra tối thiểu |
|---|---|---|---|
| 1 | Ghi nhận ý tưởng bằng demo tham chiếu, screen recording hoặc sơ đồ nhanh | Rút ngắn thời gian đặc tả ban đầu | `idea-brief.md`, link/video tham chiếu |
| 2 | Dùng model đa phương thức để chuyển ý tưởng thành PRD v0 | Tạo bản mô tả đầu tiên nhanh và có cấu trúc | `prd-v0.md` |
| 3 | Dùng interview loop để agent hỏi ngược, làm rõ edge case, UI, workflow, dữ liệu | Loại bỏ mơ hồ trước khi code | `clarification-log.md`, `prd-v1.md` |
| 4 | Rà soát thư viện, package, dịch vụ có thể tái sử dụng | Tránh tự viết lại những thứ đã có | `build-vs-buy.md` |
| 5 | Chia yêu cầu thành phases và acceptance criteria | Giảm scope mơ hồ, tăng khả năng kiểm soát | `phase-plan.md`, `acceptance-checklist.md` |
| 6 | Chốt luồng thực thi cho agent: planning, implementation, verification | Sẵn sàng handoff cho vòng code/test | `execution-brief.md` |

### 4.3. Diễn giải từng bước

#### Bước 1. Thu nhận ý tưởng bằng vật liệu trực quan

Thay vì bắt đầu bằng văn bản dài, người đề xuất có thể dùng:

- Video quay màn hình một sản phẩm tương tự.
- Bản vẽ tay hoặc sơ đồ màn hình.
- Luồng thao tác mô tả bằng lời.
- Một bộ screenshot có chú thích.

Cách làm này phù hợp với thực tế vì phần lớn ý tưởng sản phẩm ban đầu xuất hiện dưới dạng cảm nhận, hình dung, ví dụ tham chiếu hoặc hành vi mong muốn, chứ không tồn tại ngay ở dạng đặc tả hoàn chỉnh.

#### Bước 2. Tạo PRD v0 bằng model đa phương thức

Từ dữ liệu đầu vào trực quan, dùng model phù hợp để sinh ra:

- Mục tiêu tính năng.
- Đối tượng sử dụng.
- Luồng chính.
- Điểm khác biệt so với sản phẩm tham chiếu.
- Giả định ban đầu.
- Các câu hỏi còn mở.

PRD v0 không phải tài liệu cuối cùng. Vai trò của nó là tạo “điểm bám” cho vòng làm rõ tiếp theo.

#### Bước 3. Dùng interview loop để làm rõ yêu cầu

Sau khi có PRD v0, dùng agent phỏng vấn ngược người dùng hoặc người đề xuất để làm rõ các điểm như:

- Thành phần giao diện cần đặt ở đâu.
- Điều gì là bắt buộc, điều gì là tùy chọn.
- Cách xử lý lỗi, empty state, loading state.
- Điều kiện thành công của từng thao tác.
- Ràng buộc dữ liệu, bảo mật, performance, compliance.

Đây là bước đặc biệt quan trọng vì nó chuyển một tài liệu “đọc hiểu được” thành một tài liệu “triển khai được”.

#### Bước 4. Tạo hồ sơ build-vs-buy

Trước khi giao cho agent code, cần yêu cầu agent hoặc người phụ trách kỹ thuật rà soát:

- Package phù hợp.
- Mức độ bảo trì gần đây.
- Chất lượng cộng đồng.
- Độ tương thích tech stack.
- Rủi ro license, lock-in, security.

Nội dung này nên được ghi thành một memo ngắn để tránh việc mỗi vòng chat lại tìm lại từ đầu.

#### Bước 5. Chia phase thay vì làm một lèo

Đặc tả sau khi làm rõ cần được chia thành:

- Phase 1: khung nền và đường đi chính.
- Phase 2: chức năng lõi.
- Phase 3: edge cases, polish, performance, hardening.

Mỗi phase cần có acceptance criteria riêng và có thể kiểm tra độc lập. Cách này giúp giảm hiện tượng agent “đâm đầu làm tất cả”, đồng thời phù hợp với cách các model reasoning hiện đại phát huy tốt nhất: giải các bài toán có scope rõ.

#### Bước 6. Tạo brief thực thi cho agent

Trước khi bước vào code, nên chốt ngắn gọn:

- Phạm vi cần làm ở vòng này.
- File hoặc module được phép sửa.
- Tiêu chí hoàn thành.
- Lệnh kiểm chứng cần chạy.
- Điều gì không được làm.

Artifact này giúp planner, coder và verifier agents giữ cùng một hướng.

---

## 5. Bộ đầu ra khuyến nghị cho giai đoạn “develop idea”

| Tài liệu | Mục đích |
|---|---|
| `idea-brief.md` | Ghi lại ý tưởng gốc, bài toán, đối tượng dùng |
| `prd-v0.md` | Bản đặc tả sơ khởi từ nguồn trực quan |
| `clarification-log.md` | Nhật ký câu hỏi làm rõ và quyết định đã chốt |
| `prd-v1.md` | Bản PRD đã được tinh chỉnh sau interview loop |
| `build-vs-buy.md` | Hồ sơ lựa chọn thư viện/dịch vụ thay vì tự code |
| `phase-plan.md` | Chia nhỏ các giai đoạn thực thi |
| `acceptance-checklist.md` | Tiêu chí nghiệm thu cho từng phase |
| `execution-brief.md` | Bản giao việc cho AI agent ở vòng triển khai |

---

## 6. Rà soát thực tế các model hiện đại để tài liệu còn giá trị

### 6.1. Nguyên tắc cập nhật

Một tài liệu methodology chỉ có giá trị lâu dài khi:

- Không khóa cứng quy trình vào đúng một model.
- Có snapshot thị trường tại thời điểm viết.
- Ghi rõ ngày rà soát.
- Phân loại model theo lớp năng lực thay vì theo cảm tính.

Vì vậy, phần này không chỉ nêu tên model, mà còn đưa ra cách sử dụng bền vững hơn: chọn theo **loại nhiệm vụ**.

### 6.2. Snapshot tham chiếu tại ngày 21/04/2026

| Nhà cung cấp | Model/Trạng thái công khai | Giá trị vận dụng cho workflow | Khuyến nghị sử dụng |
|---|---|---|---|
| Anthropic | **Claude Opus 4.7** là model công khai mới nhất, ra ngày 16/04/2026 | Mạnh cho công việc kỹ thuật phức tạp, tác vụ dài hơi, đa bước, cần tự kiểm chứng | Dùng cho refactor lớn, tác vụ khó, quyết định cần độ chắc cao |
| Anthropic | **Claude Sonnet 4.6** ra ngày 17/02/2026, là nhóm model cân bằng cho triển khai thường nhật | Chi phí hợp lý hơn Opus, mạnh ở coding, agent workflows, long context, instruction following | Dùng làm model mặc định cho triển khai hàng ngày |
| OpenAI | **GPT-5.4** ra ngày 05/03/2026, là frontier model cho agentic, coding và professional workflows | Có computer use native, tool search, context lớn, phù hợp workflow cần tool orchestration | Dùng cho debugging khó, tác vụ cần dùng công cụ, code + verify liên hoàn |
| Google | **Gemini 3.1 Pro** đang là bản dành cho bài toán phức tạp; bài công bố ngày 19/02/2026 nêu rõ 3.1 Pro là bước nâng cấp cho reasoning và agentic workflows | Hợp với các bài toán đa phương thức, giải thích trực quan, sáng tạo, prototype code giàu ngữ cảnh | Dùng cho PRD từ nguồn video/ảnh, dựng mẫu nhanh, giải thích trực quan |
| Google | **Gemini 3 Pro Preview** đã bị deprecate và shutdown từ ngày 09/03/2026 | Không còn phù hợp để giữ làm mặc định trong tài liệu mới | Cần thay mọi chỗ ghi “Gemini 3 Pro” bằng “Gemini 3.1 Pro” hoặc tier hiện hành phù hợp |

### 6.3. Kết luận rà soát

Sau khi rà soát tài liệu công khai chính thức, có thể rút ra 4 điều chỉnh quan trọng cho methodology:

1. Không nên tiếp tục giữ nhận định kiểu “GPT-5.2 là model mặc định cho kiến trúc và debugging”, vì mốc công khai mới hơn hiện nay là **GPT-5.4**.
2. Nếu tài liệu nhắc “Opus 4.6” như model cao nhất thì cần cập nhật, vì đến **16/04/2026** Anthropic đã công bố **Opus 4.7** là bản GA mới hơn.
3. Nếu tài liệu dùng “Gemini 3 Pro” như model đang hoạt động, cần sửa vì Google đã nêu rõ bản preview này **shutdown ngày 09/03/2026**.
4. Cách viết có giá trị lâu dài nhất không phải là gắn cứng “model A cho việc B”, mà là định nghĩa **lớp năng lực** rồi ánh xạ model theo thời điểm rà soát.

---

## 7. Ma trận chọn model theo lớp năng lực

| Lớp nhiệm vụ | Đặc điểm | Model tham chiếu phù hợp tại 21/04/2026 | Gợi ý vận hành |
|---|---|---|---|
| Deep reasoning và engineering khó | Refactor lớn, hệ quả nhiều file, bài toán khó, cần tự xác minh | Claude Opus 4.7, GPT-5.4 Pro | Chỉ dùng khi bài toán thực sự khó hoặc chi phí lỗi cao |
| Triển khai thường nhật mặc định | Tác vụ triển khai thường xuyên, sửa tính năng, đọc codebase, viết test | Claude Sonnet 4.6, GPT-5.4 | Đây nên là luồng mặc định cho phần lớn ngày làm việc |
| Đa phương thức và sáng tạo đặc tả | Từ video, ảnh, UI reference, bài toán cần diễn giải trực quan | Gemini 3.1 Pro | Phù hợp pha hình thành ý tưởng và làm rõ tài liệu đầu vào |
| Triage tốc độ cao hoặc chi phí thấp | Phân loại issue, tóm tắt, tạo nháp nhanh, batch jobs | Chọn tier nhỏ hơn trong cùng họ model hiện hành | Không nên dùng cho quyết định kiến trúc quan trọng |

### Nguyên tắc sử dụng

- Dùng **một model mặc định** cho 70-80% công việc.
- Có **một model tăng cường** cho tình huống khó hoặc khi model mặc định bị kẹt.
- Có **một luồng đa phương thức** cho hình thành ý tưởng, phân tích giao diện và nguồn trực quan.
- Chỉ dùng luồng tốc độ cao/chi phí thấp cho việc nhẹ, không dùng để chốt quyết định khó.

---

## 8. Vai trò của con người trong workflow mới

Sáng kiến không đặt con người ở vai trò “người gõ code chính”, mà ở vai trò:

- Thiết kế hệ thống phản hồi cho agent.
- Chọn nguồn sự thật và ràng buộc.
- Quyết định mức độ chấp nhận build-vs-buy.
- Chọn luồng model phù hợp.
- Kiểm soát chất lượng đầu ra.
- Cập nhật tài liệu, guardrails và rubric đánh giá.

Nói cách khác, năng lực quan trọng nhất không còn chỉ là viết code tay, mà là **thiết kế môi trường làm việc để AI tạo ra code đúng**.

---

## 9. Điều kiện áp dụng sáng kiến

Sáng kiến này áp dụng hiệu quả khi có các điều kiện tối thiểu sau:

- Có kho tài liệu chuẩn hóa để làm grounding.
- Có nơi lưu các artifact trung gian của pha đặc tả.
- Có quy tắc rõ ràng về build-vs-buy.
- Có luồng xác minh đầu ra bằng test, run, review hoặc checklist.
- Có lịch rà soát model và tool định kỳ, tối thiểu theo tháng hoặc theo quý.

Nếu thiếu các điều kiện trên, sáng kiến vẫn có thể áp dụng một phần, nhưng hiệu quả sẽ giảm vì đầu ra khó tái sử dụng.

---

## 10. Hiệu quả kỳ vọng và chỉ số theo dõi

### 10.1. Hiệu quả kỳ vọng

- Rút ngắn thời gian từ ý tưởng đến PRD khả thi.
- Giảm số vòng chat lặp lại chỉ để làm rõ yêu cầu.
- Tăng khả năng tái sử dụng thư viện.
- Giảm xu hướng overengineering và “false success”.
- Cải thiện khả năng bàn giao từ phase idea sang phase implementation.

### 10.2. Chỉ số theo dõi đề nghị

| Chỉ số | Ý nghĩa |
|---|---|
| Time-to-ready-spec | Thời gian từ ý tưởng thô đến PRD đủ để giao cho agent triển khai |
| Clarification closure rate | Tỷ lệ câu hỏi mở được chốt trước khi vào code |
| Build-vs-buy reuse ratio | Tỷ lệ hạng mục dùng lại được thư viện/thành phần sẵn có |
| First-pass acceptance rate | Tỷ lệ phase đầu tiên đạt acceptance mà không phải sửa lớn |
| False-success incidence | Số lần agent báo xong nhưng verify không đạt |
| Architecture drift count | Số ngoại lệ kiến trúc phát sinh do code đi trước spec |

---

## 11. Rủi ro và biện pháp kiểm soát

| Rủi ro | Mô tả | Cách kiểm soát |
|---|---|---|
| Tài liệu lỗi thời nhanh | Tên model và năng lực thị trường thay đổi liên tục | Ghi rõ ngày rà soát, cập nhật theo lớp năng lực |
| Agent suy diễn quá mức | PRD v0 được hiểu như đặc tả cuối cùng | Luôn bắt buộc interview loop và acceptance checklist |
| Quá tin vào benchmark marketing | Chọn model chỉ theo bài PR | Kết hợp benchmark công khai với eval nội bộ và task thực tế |
| Overengineering | Dùng model mạnh cho task nhỏ | Áp luồng mặc định và chỉ tăng cường khi có dấu hiệu kẹt |
| Phụ thuộc một nhà cung cấp | Workflow mất tính linh hoạt | Viết methodology theo capability class thay vì tên hãng |

---

## 12. Khả năng nhân rộng

Sáng kiến này có thể áp dụng cho:

- Nhóm phát triển phần mềm nội bộ.
- Nhóm BA/PM cần tăng tốc giai đoạn hình thành yêu cầu.
- Đơn vị đang chuyển từ “AI hỗ trợ code” sang “AI agentic delivery”.
- Dự án cần Spec-First nhưng đang thiếu quy trình hình thành đặc tả ban đầu.

Điểm mạnh của sáng kiến là không phụ thuộc một công cụ cụ thể. Dù sử dụng Claude Code, Codex CLI, Cursor, Gemini CLI hay nền tảng khác, quy trình vẫn giữ nguyên nếu bám theo artifact và capability class đã nêu.

---

## 13. Kết luận và kiến nghị áp dụng

Sáng kiến “chuẩn hóa giai đoạn phát triển ý tưởng và đặc tả theo hướng Spec-First với AI Coding Agents đa mô hình” giải quyết đúng nút thắt lớn nhất của phát triển phần mềm bằng AI hiện nay: không phải thiếu khả năng sinh mã, mà thiếu một quy trình biến ý tưởng thành đầu vào đủ tốt cho agent.

Giá trị cốt lõi của sáng kiến không nằm ở việc khuyến nghị một model cố định, mà nằm ở việc:

- Chuẩn hóa artifact của giai đoạn đầu.
- Bắt buộc hóa interview loop.
- Thiết lập build-vs-buy như một bước chính thức.
- Chọn model theo lớp năng lực.
- Cập nhật tài liệu theo thực trạng công bố chính thức của thị trường.

Khuyến nghị áp dụng theo lộ trình:

1. Thử nghiệm trên 1 feature mới có phạm vi vừa.
2. Đo 3 chỉ số đầu tiên: `Time-to-ready-spec`, `Clarification closure rate`, `First-pass acceptance rate`.
3. Sau 2-3 vòng lặp, chuẩn hóa thành template dùng chung cho toàn nhóm.

---

## 14. Tài liệu tham khảo cập nhật

1. OpenAI, **Introducing GPT-5.4**, công bố ngày 05/03/2026: https://openai.com/index/introducing-gpt-5-4/
2. OpenAI Developer Docs, **GPT-5.4 model guide**: https://developers.openai.com/api/docs/models/gpt-5.4
3. Anthropic, **Claude Sonnet 4.6**: https://www.anthropic.com/claude/sonnet
4. Anthropic Research, **Introducing Claude Sonnet 4.6**, công bố ngày 17/02/2026: https://www.anthropic.com/research/claude-sonnet-4-6
5. Anthropic, **Claude Opus 4.7**: https://www.anthropic.com/claude/opus
6. Anthropic Research, **Introducing Claude Opus 4.7**, công bố ngày 16/04/2026: https://www.anthropic.com/research/claude-opus-4-7
7. Google AI for Developers, **Gemini API Models**: https://ai.google.dev/gemini-api/docs/models
8. Google, **Gemini 3.1 Pro**, công bố ngày 19/02/2026: https://blog.google/innovation-and-ai/models-and-research/gemini-models/gemini-3-1-pro/

---

## 15. Ghi chú sử dụng tài liệu

Tài liệu này là bản viết lại theo hướng báo cáo sáng kiến. Các tên model nêu trong phần rà soát thị trường là **snapshot tại ngày 21/04/2026**, không nên xem là chân lý cố định cho mọi thời điểm sau này. Khi dùng tài liệu này làm chuẩn nội bộ, cần bổ sung một cơ chế rà soát định kỳ để tránh tri thức bị lỗi thời.

## Related



- [MOC_Methodology.md](../../docs/MOC_Methodology.md)
- [d_3.document_strategy_and_creation_order.md](../../docs/methodology/d_3.document_strategy_and_creation_order.md)
- [d_1.quickstarts.md](../../docs/methodology/d_1.quickstarts.md)

## References

- [manifest.yaml](../../docs/grounding/manifest.yaml)
- [manifest.md](../../docs/grounding/manifest.md)
- [methodology.md](../../docs/methodology/methodology.md)
- [d_3.document_strategy_and_creation_order.md](../../docs/methodology/d_3.document_strategy_and_creation_order.md)

## Change Notes

- 2026-04-27: Normalized Markdown metadata, heading structure, and repository links; original meaning preserved.
