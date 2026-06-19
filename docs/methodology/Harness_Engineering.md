## Harness Engineering trong phát triển phần mềm với coding agent

Tôi hiểu “Harness Engineering” ở đây theo nghĩa đang được thảo luận trong cộng đồng AI engineering, tức kỹ thuật thiết kế lớp kiểm soát quanh coding agent. Đây không phải “Harness.io” như một nền tảng DevOps thương mại, mà là một phương pháp engineering để làm cho AI coding agent có thể tạo code đáng tin hơn, ít cần giám sát hơn và dễ kiểm chứng hơn.

## 1. Kết luận điều hành

Harness Engineering là một thực hành kỹ thuật mới nổi trong phát triển phần mềm với AI agent. Theo bài viết của Birgitta Böckeler trên Martin Fowler, “harness” có thể hiểu là mọi thứ nằm quanh model để hướng dẫn, kiểm soát, quan sát và tự sửa hành vi của coding agent. Mục tiêu không phải là tin AI nhiều hơn một cách mù quáng, mà là xây một hệ thống guide và sensor để tăng xác suất agent làm đúng ngay từ đầu, đồng thời phát hiện lỗi sớm để agent tự sửa trước khi con người phải review. ([martinfowler.com][1])

Nếu SDD giúp tạo đặc tả rõ, BMAD giúp tổ chức agent theo workflow, thì Harness Engineering là lớp kiểm soát kỹ thuật để đảm bảo agent không làm việc tự do ngoài biên. Nó biến AI coding từ “prompt rồi chờ kết quả” thành một hệ thống có hướng dẫn, kiểm tra, feedback loop, quality gate và evidence.

Với bối cảnh enterprise Java, Spring Boot, microservices, CI/CD, KMS và QA mà anh đang quan tâm, Harness Engineering rất đáng đưa vào mô hình SDD. Nó giúp trả lời câu hỏi quan trọng: “Làm sao để agent code nhiều hơn nhưng rủi ro không tăng tương ứng?”

## 2. Harness Engineering là gì?

Harness Engineering là việc thiết kế một lớp điều khiển bên ngoài coding agent. Lớp này bao gồm các guide trước khi agent hành động và các sensor sau khi agent hành động.

Bài viết trên Martin Fowler chia harness thành hai hướng chính:

1. Feedforward control, tức guide định hướng trước khi agent hành động.

2. Feedback control, tức sensor quan sát kết quả sau khi agent hành động để agent hoặc con người tự sửa. ([martinfowler.com][1])

Nói đơn giản hơn:

Guide trả lời câu hỏi: “Agent nên làm như thế nào để ít sai ngay từ đầu?”

Sensor trả lời câu hỏi: “Sau khi agent làm xong, bằng cách nào chúng ta biết nó có sai không?”

Ví dụ guide có thể là AGENTS.md, architecture.md, coding standard, project context, API guideline, security rule, logging rule, test strategy, KMS document hoặc skill hướng dẫn cách làm việc.

Ví dụ sensor có thể là unit test, integration test, linter, static analysis, type checker, ArchUnit, Semgrep, coverage report, mutation testing, log scanner, build pipeline, browser test, code review agent hoặc human review.

## 3. Vì sao Harness Engineering trở nên quan trọng?

Coding agent có thể sinh code nhanh, nhưng nó không có đầy đủ trực giác của một kỹ sư phần mềm có kinh nghiệm. Bài viết của Martin Fowler nhấn mạnh rằng LLM không thật sự hiểu code như con người, nó không biết đầy đủ context, có tính không xác định và có rào cản niềm tin tự nhiên với code do AI tạo ra. ([martinfowler.com][1])

Đây là vấn đề rất thực tế trong doanh nghiệp.

Một agent có thể:

1. Tạo code đúng cú pháp nhưng sai nghiệp vụ.

2. Sửa bug bằng cách thêm workaround không cần thiết.

3. Làm tăng technical debt.

4. Phá vỡ boundary giữa các module.

5. Tạo test pass nhưng không kiểm chứng đúng behavior.

6. Bỏ qua logging, security hoặc transaction rule.

7. Đọc nhầm context hoặc dùng tài liệu lỗi thời.

8. Tạo giải pháp quá phức tạp so với yêu cầu.

Vì vậy, khi team tăng mức độ dùng AI, câu hỏi không còn là “agent có code được không”. Câu hỏi đúng hơn là “chúng ta có đủ harness để kiểm soát agent hay không”.

## 4. Các thành phần cốt lõi

## 4.1. Guide, tức feedforward control

Guide là mọi thứ giúp agent làm đúng trước khi hành động.

Trong thực tế, guide nên bao gồm:

1. AGENTS.md hoặc project instruction

AGENTS.md đóng vai trò như README dành cho coding agent, chứa build step, test command, code style và convention mà agent cần biết khi làm việc với project. ([agents.md][2])

2. Architecture document

Tài liệu kiến trúc cần nói rõ module boundary, dependency rule, data flow, security model, transaction policy, logging convention và deployment model.

3. SDD spec

Spec phải có requirement, acceptance criteria, business rule, scenario, input, output, exception flow và definition of done.

4. Coding standard

Ví dụ với Java Spring Boot: package convention, layer convention, DTO rule, exception handling rule, transaction rule, repository rule, service boundary và logging format.

5. How to guide

Ví dụ: cách thêm API mới, cách viết migration, cách viết integration test, cách thêm metric, cách debug lỗi performance.

6. KMS context pack

Agent không nên tự đọc toàn bộ kho tài liệu. Nên có context pack theo task, được truy xuất từ KMS bằng rg hoặc công cụ search có kiểm soát.

Guide tốt giúp giảm lỗi từ đầu. Nếu thiếu guide, agent sẽ phải đoán. Với enterprise software, “đoán” là nguồn rủi ro lớn.

## 4.2. Sensor, tức feedback control

Sensor là cơ chế phát hiện vấn đề sau khi agent hành động.

Sensor nên được chia thành nhiều lớp:

1. Sensor tại local workspace

Ví dụ: compile, unit test, linter, formatter, type check, static analysis, quick smoke test.

2. Sensor tại pull request

Ví dụ: CI build, integration test, API contract test, dependency scan, SAST, code coverage, architecture test.

3. Sensor tại pipeline sau tích hợp

Ví dụ: E2E test, mutation testing, performance test, security scan, container scan, deployment dry run.

4. Sensor sau runtime

Ví dụ: log anomaly, error rate, latency SLO, availability SLO, APM trace, failed job, database slow query.

Bài viết trên Martin Fowler nhấn mạnh nguyên tắc “keep quality left”, tức đưa các check càng sớm càng tốt trong lifecycle để lỗi được phát hiện khi chi phí sửa còn thấp. ([martinfowler.com][1])

## 4.3. Computational control và inferential control

Harness Engineering phân biệt hai loại control.

Computational control là các check xác định, nhanh, chạy bằng CPU, ví dụ test, linter, type checker và structural analysis. Những check này đáng tin hơn, rẻ hơn và nên chạy thường xuyên. ([martinfowler.com][1])

Inferential control là các check dùng AI hoặc semantic judgment, ví dụ AI code review, LLM as judge, review agent hoặc semantic duplication detection. Loại này mạnh ở việc đánh giá ý nghĩa, nhưng chậm hơn, tốn chi phí hơn và có tính không xác định. ([martinfowler.com][1])

Trong enterprise, không nên thay computational control bằng inferential control. Cách đúng là dùng computational control làm nền, sau đó dùng inferential control để hỗ trợ các đánh giá khó hơn như code smell, over engineering, missing requirement, semantic mismatch và kiến trúc không nhất quán.

## 4.4. Steering loop

Harness không phải cấu hình một lần là xong. Nó là vòng lặp cải tiến liên tục.

Khi agent lặp lại một lỗi, team không chỉ sửa code. Team phải sửa harness.

Ví dụ:

1. Agent liên tục viết log thiếu correlation ID.

Cần cập nhật logging guide, thêm linter hoặc ArchUnit rule kiểm tra logging context.

2. Agent thường bỏ sót negative test.

Cần cập nhật test skill, thêm checklist và yêu cầu test scenario từ acceptance criteria.

3. Agent hay gọi repository trực tiếp từ controller.

Cần thêm architecture rule và structural test kiểm tra layer violation.

4. Agent tạo API không theo convention.

Cần thêm API style guide và contract validation.

Đây là điểm rất quan trọng. Harness Engineering biến lỗi lặp lại thành input để cải tiến hệ thống điều khiển agent.

## 5. Ba loại harness nên ưu tiên

## 5.1. Maintainability harness

Maintainability harness kiểm soát chất lượng nội bộ của codebase.

Nó xử lý các vấn đề như duplicate code, cyclomatic complexity, missing test coverage, style violation, dependency violation, dead code và test yếu. Bài viết của Martin Fowler nhận định đây là loại harness dễ làm nhất hiện nay vì đã có nhiều tooling sẵn như test, linter, type checker và structural analysis. ([martinfowler.com][1])

Với Java, nên ưu tiên:

1. Maven hoặc Gradle build bắt buộc pass.

2. JUnit và Testcontainers cho integration test.

3. JaCoCo cho coverage.

4. Checkstyle hoặc Spotless cho format.

5. SpotBugs hoặc Error Prone cho bug pattern.

6. SonarQube cho maintainability và security smell.

7. ArchUnit cho module boundary.

8. OpenRewrite cho codemod và refactoring recipe.

9. Semgrep cho policy và security rule.

## 5.2. Architecture fitness harness

Architecture fitness harness kiểm soát hệ thống có còn đi đúng kiến trúc mong muốn hay không.

Bài viết đưa nhóm này vào phạm vi fitness function, tức các guide và sensor để định nghĩa và kiểm tra architecture characteristic. ([martinfowler.com][1])

Với enterprise Java, nên kiểm soát:

1. Controller không gọi trực tiếp repository.

2. Service không phụ thuộc ngược lên web layer.

3. Domain không phụ thuộc framework nếu theo clean architecture.

4. Module A không được gọi module B nếu không có dependency được phê duyệt.

5. API public phải có contract test.

6. Event schema phải version hóa.

7. Database migration phải có rollback hoặc forward fix plan.

8. Logging phải có trace ID, request ID, user context nếu phù hợp.

9. Transaction boundary phải nằm đúng layer.

10. Cache phải có TTL và invalidation strategy.

Đây là phần rất quan trọng nếu muốn agent sửa code trong hệ thống lớn mà không phá kiến trúc.

## 5.3. Behaviour harness

Behaviour harness kiểm soát ứng dụng có hành xử đúng nghiệp vụ hay không.

Đây là phần khó nhất. Bài viết trên Martin Fowler gọi đây là “elephant in the room”, vì việc kiểm chứng functional behavior vẫn còn nhiều thách thức. Một cách làm phổ biến hiện nay là dùng functional spec làm feedforward, dùng test suite và coverage làm feedback, sau đó kết hợp manual testing. Tuy nhiên, bài viết cũng cảnh báo rằng đặt quá nhiều niềm tin vào test do AI sinh là chưa đủ. ([martinfowler.com][1])

Với mô hình SDD của anh, behaviour harness nên dựa vào:

1. Business rule có ID.

2. Acceptance criteria có ID.

3. BPMN nếu có luồng nghiệp vụ.

4. Gherkin scenario map với business rule.

5. Unit test map với Gherkin.

6. API test map với acceptance criteria.

7. Manual verification checklist cho luồng quan trọng.

8. Log evidence cho case pass và case fail.

9. Test data được kiểm soát.

10. Traceability từ requirement đến code và test.

Đây là phần quyết định agent có thực sự giúp phát triển phần mềm hay chỉ tạo ra code nhìn có vẻ đúng.

## 6. Chức năng nên ưu tiên khi triển khai Harness Engineering

## 6.1. Project constitution

Cần có một tài liệu nền mô tả cách agent phải làm việc với project.

Nội dung tối thiểu:

1. Stack kỹ thuật.

2. Build command.

3. Test command.

4. Run command.

5. Folder structure.

6. Coding convention.

7. Architecture boundary.

8. Security rule.

9. Logging rule.

10. Definition of done.

11. Những việc agent không được tự ý làm.

12. Khi nào phải hỏi lại con người.

AGENTS.md là lựa chọn phù hợp vì nó là định dạng đơn giản, có mục tiêu cung cấp context và instruction riêng cho coding agent. ([agents.md][2])

## 6.2. SDD specification pack

Trước khi agent code, phải có spec pack đủ rõ.

Spec pack nên gồm:

1. Problem statement.

2. Scope và out of scope.

3. Functional requirement.

4. Non functional requirement.

5. Acceptance criteria.

6. Business rule.

7. Data contract.

8. Error handling.

9. Test scenario.

10. Evidence requirement.

Đây là guide quan trọng nhất cho behaviour harness.

## 6.3. Fast local sensor

Agent nên được phép chạy các check nhanh trước khi tạo commit.

Ví dụ:

1. Compile.

2. Unit test liên quan.

3. Formatter.

4. Linter.

5. Static analysis nhanh.

6. Architecture rule nhanh.

7. Basic security scan.

8. Log scan nếu có test runtime.

Mục tiêu là để agent tự sửa lỗi cơ bản trước khi con người review.

## 6.4. Pull request quality gate

PR không nên chỉ dựa vào reviewer đọc code.

Quality gate nên bao gồm:

1. Build pass.

2. Unit test pass.

3. Integration test pass nếu có.

4. Coverage không giảm dưới ngưỡng.

5. Static analysis không có issue nghiêm trọng.

6. Dependency scan pass.

7. API contract pass.

8. Architecture test pass.

9. Code review agent chạy và ghi finding.

10. Human reviewer xác nhận các phần rủi ro cao.

## 6.5. KMS retrieval harness

Agent cần biết lấy context từ đâu.

Với mô hình của anh, có thể dùng rg làm search engine chính cho KMS. Harness cần định nghĩa:

1. Khi nào phải search docs.

2. Search trong folder nào.

3. Keyword pattern nào.

4. Tài liệu nào là source of truth.

5. Tài liệu nào chỉ là historical note.

6. Khi nào phải cập nhật KMS sau task.

Điểm quan trọng là agent không được dựa vào trí nhớ hoặc suy đoán nếu project đã có tài liệu chính thức.

## 6.6. Review agent và adversarial review

Nên có một review agent riêng với vai trò phản biện.

Review agent không chỉ hỏi code có chạy không, mà phải kiểm tra:

1. Có đúng requirement không.

2. Có đúng architecture không.

3. Có phá convention không.

4. Có thiếu test không.

5. Có over engineering không.

6. Có security risk không.

7. Có performance risk không.

8. Có missing log hoặc missing metric không.

9. Có làm tăng coupling không.

10. Có cần cập nhật tài liệu không.

Đây là inferential sensor, không thay thế CI, nhưng rất hữu ích để phát hiện lỗi semantic.

## 7. Ứng dụng thực tế

## 7.1. Phát triển feature bằng AI agent

Harness Engineering giúp agent làm feature theo quy trình:

1. Đọc spec.

2. Đọc project instruction.

3. Search KMS.

4. Lập plan.

5. Code trong phạm vi story.

6. Chạy local sensor.

7. Tự sửa lỗi.

8. Sinh test.

9. Chạy test.

10. Tạo summary và evidence.

11. Chờ review.

Cách này an toàn hơn nhiều so với việc giao cho agent một prompt dài rồi để agent sửa toàn bộ codebase.

## 7.2. Legacy modernization

Với legacy code, harness càng quan trọng.

Có thể dùng agent để:

1. Phân tích module cũ.

2. Tạo code map.

3. Phát hiện dead code.

4. Đề xuất refactor.

5. Thêm test bao quanh legacy behavior.

6. Chạy OpenRewrite recipe.

7. Kiểm tra architecture drift.

8. Tạo migration note.

Tuy nhiên legacy thường có harnessability thấp vì thiếu test, thiếu module boundary, code coupling cao và convention không rõ. Bài viết trên Martin Fowler cũng nhấn mạnh codebase typed, boundary rõ và framework như Spring sẽ dễ harness hơn, còn legacy nhiều technical debt thì khó hơn. ([martinfowler.com][1])

## 7.3. Enterprise Java và Spring Boot

Java và Spring Boot khá phù hợp với Harness Engineering vì có type system, framework convention, build tool mạnh và hệ sinh thái test tốt.

Có thể triển khai:

1. ArchUnit kiểm tra layer.

2. OpenRewrite chuẩn hóa refactor.

3. Maven hoặc Gradle task cho test nhanh.

4. Spring Boot test slice.

5. Testcontainers cho integration test.

6. Contract test cho API.

7. SonarQube cho code quality.

8. Semgrep cho security policy.

9. Logback rule hoặc custom test để kiểm tra logging.

10. Micrometer metric check nếu cần.

## 7.4. QA và test automation

Harness Engineering thay đổi cách QA làm việc.

QA không chỉ viết test case sau khi code xong. QA thiết kế behaviour harness ngay từ spec.

Các hoạt động phù hợp:

1. Chuyển acceptance criteria thành Gherkin.

2. Map business rule sang test case.

3. Xác định risk based testing.

4. Dùng agent sinh test draft.

5. Kiểm tra chất lượng test bằng mutation testing hoặc review.

6. Tạo test data có kiểm soát.

7. Gắn evidence vào story.

8. Phân tích log sau khi chạy test.

## 7.5. DevOps, SRE và vận hành

Harness Engineering không chỉ dùng cho code application.

Có thể dùng cho:

1. Helm chart.

2. Terraform.

3. Ansible.

4. Bash script.

5. Kubernetes manifest.

6. CI/CD pipeline.

7. Monitoring rule.

8. Alert rule.

9. Runbook.

10. Incident investigation.

Với vận hành, sensor có thể là dry run, policy as code, kubeconform, helm lint, terraform plan, OPA policy, log check và monitoring SLO.

## 8. Scalability

## 8.1. Scale theo cá nhân

Ở mức cá nhân, harness có thể bắt đầu rất đơn giản:

1. AGENTS.md.

2. Build command.

3. Test command.

4. Coding convention.

5. Prompt review.

6. Checklist trước commit.

7. Script chạy test nhanh.

Đây là mức đủ để developer cá nhân giảm lỗi khi dùng coding agent.

## 8.2. Scale theo team

Ở mức team, cần chuẩn hóa:

1. Shared AGENTS.md.

2. Shared skills.

3. Shared architecture rule.

4. Pull request template.

5. CI gate.

6. Code review checklist.

7. KMS guideline.

8. Test strategy.

9. Definition of done.

10. Incident feedback loop.

Khi một lỗi lặp lại, team cập nhật harness chung để mọi agent và mọi developer đều được hưởng lợi.

## 8.3. Scale theo tổ chức

Ở mức enterprise, nên tiến tới harness template.

Bài viết trên Martin Fowler đề xuất ý tưởng harness template cho các topology phổ biến như business service exposing API, event processing service hoặc data dashboard. Các topology này có thể đóng gói structure, tech stack, guide và sensor để agent bị “leash” vào convention đã định nghĩa. ([martinfowler.com][1])

Với doanh nghiệp, có thể tạo template cho:

1. Spring Boot REST API service.

2. Spring Batch job.

3. Event driven Kafka service.

4. NodeJS BFF service.

5. Python data processing job.

6. Helm chart application.

7. Terraform module.

8. DBA automation script.

Mỗi template nên có:

1. Folder structure.

2. Build command.

3. Test command.

4. Security baseline.

5. Observability baseline.

6. Architecture test.

7. Example code.

8. Example test.

9. Deployment pipeline.

10. Review checklist.

## 8.4. Scale theo CI/CD và platform

Harness Engineering không nên chỉ nằm trong IDE. Nó phải nối vào CI/CD.

Cấu trúc tốt là:

1. Agent chạy local sensor.

2. Developer review kết quả.

3. PR chạy CI sensor.

4. Review agent chạy semantic review.

5. Human reviewer quyết định.

6. Merge vào main.

7. Pipeline chạy integration sensor.

8. Runtime sensor theo dõi production.

9. Feedback từ incident quay lại cập nhật harness.

Đây là cách biến harness thành một phần của software delivery lifecycle, không phải chỉ là prompt cá nhân.

## 9. Hạn chế và rủi ro

## 9.1. Behaviour correctness vẫn là phần khó nhất

Maintainability và architecture có thể kiểm tra tương đối tốt bằng tool. Nhưng nghiệp vụ đúng hay sai vẫn khó. Bài viết trên Martin Fowler cảnh báo rằng việc dựa quá nhiều vào test suite do AI sinh ra là chưa đủ để giảm đáng kể giám sát và manual testing. ([martinfowler.com][1])

Vì vậy, với requirement phức tạp, vẫn cần SDD, Gherkin, business rule coverage, domain expert review và manual verification.

## 9.2. Chi phí xây harness không nhỏ

Harness tốt cần thời gian.

Cần viết guide, chuẩn hóa docs, tạo script, viết architecture test, tích hợp CI, cấu hình scanner, duy trì KMS và cập nhật rule sau mỗi lỗi lặp lại.

Nếu team nhỏ hoặc project đơn giản, chi phí này có thể lớn hơn lợi ích ban đầu.

## 9.3. Sensor có thể tạo cảm giác an toàn giả

Nếu test yếu, coverage cao vẫn không có nghĩa là đúng nghiệp vụ.

Nếu linter pass, code vẫn có thể sai thiết kế.

Nếu AI review nói ổn, code vẫn có thể thiếu context.

Nếu sensor không phát hiện lỗi, có thể là code tốt, nhưng cũng có thể là sensor chưa đủ tốt.

Đây là rủi ro rất lớn. Harness không được biến thành checklist hình thức.

## 9.4. Inferential sensor không ổn định

AI review agent có thể phát hiện vấn đề semantic tốt, nhưng cũng có thể bỏ sót, hiểu sai hoặc phản hồi không nhất quán.

Do đó, AI review nên là lớp hỗ trợ, không phải nguồn phê duyệt cuối cùng cho thay đổi quan trọng.

## 9.5. Guide và sensor có thể mâu thuẫn

Khi project lớn, AGENTS.md, coding standard, architecture.md, PR template, checklist, CI policy và KMS có thể lệch nhau.

Nếu guide mâu thuẫn, agent sẽ làm sai theo cách rất khó phát hiện.

Cần có cơ chế kiểm soát version, owner, review và deprecation cho tài liệu.

## 9.6. Legacy code khó harness

Codebase thiếu test, thiếu type safety, thiếu module boundary, thiếu convention và nhiều technical debt sẽ khó kiểm soát hơn. Đây là nghịch lý: nơi cần harness nhất lại thường là nơi khó xây harness nhất.

## 9.7. Rủi ro bảo mật

Khi agent có quyền chạy command, sửa file, đọc repo, truy cập secret hoặc gọi tool ngoài, rủi ro tăng lên.

Cần kiểm soát:

1. Agent không được đọc secret ngoài phạm vi.

2. Agent không được tự push hoặc merge.

3. Agent không được tự chạy destructive command.

4. Tool permission phải theo least privilege.

5. Log không được lộ thông tin nhạy cảm.

6. Output của agent phải được review trước khi đưa vào khách hàng.

## 10. So sánh với các phương pháp và công nghệ liên quan

## 10.1. SDD

SDD là nền tảng tốt nhất cho behaviour harness.

SDD giúp định nghĩa rõ yêu cầu, business rule, acceptance criteria và evidence. Nếu không có SDD, harness chỉ kiểm soát được code quality, còn correctness nghiệp vụ vẫn yếu.

Khuyến nghị: dùng SDD làm feedforward guide chính cho behavior.

## 10.2. BMAD METHOD

BMAD là framework AI driven development từ ideation, planning đến agentic implementation, có specialized AI agents, guided workflows và intelligent planning. ([BMAD Method][3])

BMAD phù hợp để tổ chức workflow và vai trò agent.

Harness Engineering phù hợp để kiểm soát chất lượng đầu ra của agent.

Hai thứ không thay thế nhau. BMAD trả lời “agent làm theo quy trình nào”. Harness Engineering trả lời “làm sao biết agent làm đúng”.

## 10.3. AGENTS.md và Skills

AGENTS.md phù hợp để cung cấp instruction ổn định cho coding agent. Nó nên chứa setup command, test command, code style và convention. ([agents.md][2])

Skills phù hợp để đóng gói workflow chuyên biệt như code review, architecture review, test generation, KMS update hoặc incident investigation.

Đây là lớp guide rất quan trọng trong Harness Engineering.

## 10.4. LangGraph

LangGraph phù hợp nếu muốn xây agent workflow production grade có durable execution và human in the loop. Tài liệu LangGraph mô tả durable execution là khả năng lưu tiến trình để có thể pause và resume, rất hữu ích cho workflow dài hoặc cần con người kiểm tra trước khi tiếp tục. ([LangChain Docs][4])

Nếu Harness Engineering là phương pháp thiết kế control quanh coding agent, thì LangGraph là một lựa chọn để hiện thực agent orchestration bằng code.

Dùng LangGraph khi:

1. Cần multi agent workflow chạy lâu.

2. Cần trạng thái bền vững.

3. Cần human approval ở nhiều bước.

4. Cần audit workflow.

5. Cần tích hợp tool phức tạp.

Không nhất thiết dùng LangGraph nếu chỉ cần harness cho coding agent trong IDE.

## 10.5. CI/CD quality gate truyền thống

Jenkins, GitLab CI, GitHub Actions, Azure DevOps hoặc Tekton vẫn rất quan trọng.

Harness Engineering không thay thế CI/CD. Nó mở rộng CI/CD sang agent workflow.

Nói cách khác, CI/CD là sensor truyền thống. Harness Engineering làm cho sensor đó có thể phản hồi sớm hơn, rõ hơn và hữu ích hơn cho agent tự sửa.

## 11. Đề xuất áp dụng cho team enterprise Java

## 11.1. Giai đoạn 1: Harness tối thiểu

Tạo một baseline cho mọi repo:

1. AGENTS.md.

2. Build command chuẩn.

3. Test command chuẩn.

4. Formatter.

5. Static analysis.

6. Unit test tối thiểu.

7. PR checklist.

8. Definition of done.

9. KMS source of truth.

10. Rule: agent không tự merge.

## 11.2. Giai đoạn 2: Harness cho kiến trúc

Thêm:

1. ArchUnit rule.

2. Package boundary rule.

3. Dependency rule.

4. API contract test.

5. Logging convention test.

6. Transaction guideline.

7. Security guideline.

8. Architecture review skill.

## 11.3. Giai đoạn 3: Harness cho behaviour

Thêm:

1. SDD spec template.

2. Business rule ID.

3. Gherkin scenario.

4. Test mapping.

5. Approved fixture hoặc controlled test data.

6. Manual verification checklist.

7. Evidence report.

8. Mutation testing cho module quan trọng.

## 11.4. Giai đoạn 4: Harness template

Tạo template cho từng loại service:

1. Java Spring REST API.

2. Batch job.

3. Kafka consumer.

4. NodeJS API.

5. Python automation.

6. Helm application.

7. Terraform module.

Mỗi template có guide và sensor riêng.

## 12. Mô hình triển khai đề xuất

Một pipeline làm việc thực tế có thể là:

1. PM hoặc BA tạo SDD requirement.

2. Architect bổ sung architecture decision.

3. Agent đọc AGENTS.md và context pack từ KMS.

4. Agent lập implementation plan.

5. Human review plan nếu task rủi ro.

6. Agent code.

7. Agent chạy local sensor.

8. Agent tự sửa lỗi.

9. Agent tạo test và evidence.

10. CI chạy quality gate.

11. Review agent đánh giá semantic.

12. Human reviewer phê duyệt.

13. Merge.

14. Runtime sensor theo dõi production.

15. Lỗi lặp lại được đưa vào harness improvement backlog.

## 13. Đánh giá tổng thể

Harness Engineering là một trong những khái niệm quan trọng nhất nếu team muốn dùng coding agent một cách nghiêm túc trong phát triển phần mềm hiện đại.

Nó không phải tool đơn lẻ.

Nó là một hệ thống kiểm soát gồm guide, sensor, feedback loop, quality gate, template và human steering.

Điểm mạnh:

1. Giảm lỗi lặp lại của agent.

2. Tăng khả năng tự sửa của agent.

3. Giảm review toil cho con người.

4. Đưa quality check sang trái.

5. Tăng tính nhất quán của codebase.

6. Phù hợp với SDD, BMAD và KMS.

7. Rất phù hợp với Java enterprise nếu có test và module boundary tốt.

Điểm yếu:

1. Tốn công xây dựng.

2. Behaviour correctness vẫn khó.

3. AI review không đủ tin cậy nếu đứng một mình.

4. Legacy code khó harness.

5. Có rủi ro false confidence.

6. Cần governance tài liệu và rule nghiêm túc.

Khuyến nghị xây mô hình SDD kết hợp BMAD, nên bổ sung Harness Engineering như layer kiểm soát kỹ thuật. SDD tạo spec. BMAD tổ chức workflow agent. Harness Engineering kiểm soát agent bằng guide và sensor. KMS lưu tri thức. CI/CD xác minh bằng evidence.

Công thức vận hành nên là:

SDD để agent hiểu đúng việc cần làm.

BMAD để agent làm việc theo quy trình.

Harness Engineering để agent bị kiểm soát bằng guide, sensor và feedback loop.

CI/CD để xác nhận bằng kiểm thử thật.

KMS để lưu lại tri thức sau mỗi lần hoàn thành.

Đây là nền tảng thực tế để chuyển từ “AI hỗ trợ code” sang “AI tham gia delivery nhưng vẫn nằm trong kiểm soát kỹ thuật của con người”.

## Kết luận ngắn

Với mô hình SDD anh đã triển khai, anh không cần xây lại từ đầu. Anh đã có phần lõi rất tốt: spec first, KMS, `llm wiki`, `AGENTS.md`, `rg`, BPMN, Gherkin, business rule coverage, unit test, debug log, verification evidence.

Để đạt mức Harness Engineering, anh cần bổ sung thêm một lớp “hệ thống điều khiển agent”. Lớp này gồm ba phần:

1. Guide: hướng dẫn agent trước khi làm.

2. Sensor: kiểm tra agent sau khi làm.

3. Steering loop: khi agent sai, không chỉ sửa code, mà sửa lại guide hoặc sensor để lần sau không lặp lại lỗi.

Theo mô hình Harness Engineering được Birgitta Böckeler trình bày trên Martin Fowler, harness quanh coding agent được chia thành guide, tức feedforward control, và sensor, tức feedback control. Guide giúp agent làm đúng ngay từ đầu. Sensor quan sát kết quả sau khi agent hành động để agent hoặc con người tự sửa. Nếu chỉ có guide mà không có sensor, ta không biết rule có hiệu quả không. Nếu chỉ có sensor mà không có guide, agent sẽ lặp lại lỗi rồi mới sửa. ([martinfowler.com][1])

## 1. Mô hình SDD hiện tại của anh đã có gì

Dựa trên những gì anh đã mô tả trước đó, mô hình của anh đã có các nền tảng sau:

1. Spec Driven Development làm lõi.

2. Requirement được phân tích trước khi code.

3. BPMN dùng để mô tả luồng nghiệp vụ.

4. Gherkin dùng để mapping business rule và test case.

5. Unit test được tạo dựa trên business rule coverage.

6. Verification yêu cầu unit test pass.

7. Verification yêu cầu không có unexpected exception.

8. Verification yêu cầu không có unexpected error log trong debug run.

9. Có kiểm thử trực tiếp ứng dụng để đánh giá code change.

10. Có KMS, Obsidian wiki, `docs`, `llm wiki`.

11. Có `rg` làm engine search tri thức.

12. Có định hướng dùng `AGENTS.md` để hướng dẫn agent.

13. Có Code Intelligence Agent để giúp agent hiểu vai trò file code trong module.

14. Có tư duy cập nhật KMS sau khi hoàn thành task.

Đây là nền tảng rất gần với Harness Engineering. Điểm còn thiếu không phải là “thêm một phương pháp mới”, mà là chuẩn hóa tất cả thành một control system có tên, có file, có gate, có sensor, có owner và có cơ chế cải tiến.

## 2. Phần còn thiếu để thành Harness Engineering

Anh cần bổ sung tám khối chính.

## 2.1. Harness Constitution

Đây là tài liệu gốc quy định cách agent được phép làm việc trong project.

Hiện tại anh có thể đã có `AGENTS.md`, nhưng để thành harness, nó cần được nâng cấp thành “project constitution” cho agent.

Nội dung nên có:

1. Vai trò của agent trong project.

2. Agent được phép đọc file nào.

3. Agent được phép sửa file nào.

4. Agent không được phép sửa file nào nếu chưa có approval.

5. Agent phải search KMS trước khi phân tích task trong trường hợp nào.

6. Agent phải tạo plan trước khi code trong trường hợp nào.

7. Agent phải chạy test nào trước khi báo hoàn thành.

8. Agent phải ghi evidence ở đâu.

9. Agent phải cập nhật tài liệu nào sau khi hoàn thành.

10. Agent phải dừng và hỏi người dùng khi gặp mâu thuẫn nào.

Tên file đề xuất:

```text
AGENTS.md
docs/grounding/harness/harness_constitution.md
docs/grounding/harness/agent_permission_policy.md
```

Điểm quan trọng: `AGENTS.md` nên ngắn, có tính điều hướng. Các rule chi tiết nên đặt trong `docs/grounding/harness`.

## 2.2. Guide Registry

Guide là toàn bộ tài liệu giúp agent làm đúng trước khi hành động.

Trong mô hình của anh, guide không nên nằm rải rác. Cần có một registry để agent biết khi nào đọc tài liệu nào.

Tên file đề xuất:

```text
docs/grounding/harness/guide_registry.md
```

Nội dung nên chia theo loại task.

Ví dụ:

```text
Task type: feature mới
Guide bắt buộc:
1. docs/grounding/sdd/spec_template.md
2. docs/architecture/c4_context.md
3. docs/architecture/c4_container.md
4. docs/grounding/harness/test_policy.md
5. docs/grounding/harness/evidence_policy.md

Task type: sửa bug
Guide bắt buộc:
1. docs/grounding/sdd/bug_intake_template.md
2. docs/grounding/harness/debug_policy.md
3. docs/grounding/harness/regression_test_policy.md

Task type: điều tra RAC
Guide bắt buộc:
1. docs/grounding/ops/rac_investigation_template.md
2. docs/grounding/harness/log_evidence_policy.md
3. docs/grounding/harness/root_cause_analysis_policy.md

Task type: automation script
Guide bắt buộc:
1. docs/grounding/ops/script_spec_template.md
2. docs/grounding/harness/script_safety_policy.md
3. docs/grounding/harness/script_test_policy.md
```

Guide Registry giúp agent không cần đoán. Mỗi loại task có đường dẫn context rõ ràng.

## 2.3. Sensor Registry

Đây là phần quan trọng nhất anh còn thiếu nếu muốn nói là Harness Engineering đúng nghĩa.

Sensor là các kiểm tra bắt buộc sau khi agent làm xong một phần việc. Martin Fowler phân biệt computational control và inferential control. Computational control là kiểm tra xác định như test, linter, type checker, structural analysis. Inferential control là đánh giá ngữ nghĩa như AI code review hoặc LLM as judge, chậm hơn và không xác định tuyệt đối. ([martinfowler.com][1])

Tên file đề xuất:

```text
docs/grounding/harness/sensor_registry.md
```

Nội dung nên gồm:

1. Tên sensor.

2. Mục tiêu kiểm tra.

3. Khi nào phải chạy.

4. Command chạy.

5. Kết quả pass là gì.

6. Kết quả fail xử lý thế nào.

7. Evidence lưu ở đâu.

8. Owner chịu trách nhiệm.

Ví dụ sensor cho Java Spring Boot:

```text
Sensor: build_check
Mục tiêu: xác nhận source code compile được
Khi chạy: sau mỗi code change
Command: mvn test
Pass: build success, test pass
Fail: agent phải đọc log, sửa lỗi, chạy lại

Sensor: unit_test_check
Mục tiêu: xác nhận logic thay đổi không phá unit test
Khi chạy: sau mỗi story
Command: mvn test
Pass: toàn bộ unit test liên quan pass
Fail: agent phải sửa code hoặc sửa test nếu test sai, nhưng phải giải thích rõ

Sensor: architecture_check
Mục tiêu: xác nhận không vi phạm layer architecture
Khi chạy: trước pull request
Tool: ArchUnit
Pass: không có dependency violation
Fail: agent phải sửa dependency hoặc đề xuất thay đổi architecture

Sensor: static_analysis_check
Mục tiêu: phát hiện bug pattern, code smell, security smell
Khi chạy: trước pull request
Tool: SonarQube, SpotBugs, Semgrep
Pass: không có issue nghiêm trọng
Fail: phân loại blocker, critical, major

Sensor: log_runtime_check
Mục tiêu: xác nhận không có unexpected error log
Khi chạy: sau debug run hoặc local run
Pass: không có unexpected exception, không có unexpected error log
Fail: agent phải phân tích stacktrace và cập nhật evidence
```

Nếu thiếu Sensor Registry, SDD vẫn có thể tốt, nhưng agent verification sẽ phụ thuộc nhiều vào thói quen cá nhân.

## 2.4. Architecture Fitness Function

Hiện tại anh có C4, code map, module note và header comment. Để thành harness, cần thêm “kiểm tra kiến trúc bằng máy”.

Đây là điểm rất quan trọng vì AI agent có thể sửa code đúng chức năng nhưng phá kiến trúc.

Với Java, nên bổ sung:

1. ArchUnit để kiểm tra layer.

2. Rule controller không gọi repository trực tiếp.

3. Rule service không phụ thuộc web layer.

4. Rule domain không phụ thuộc infrastructure nếu áp dụng clean architecture.

5. Rule package dependency chỉ đi theo chiều cho phép.

6. Rule API public phải có contract hoặc test tương ứng.

7. Rule database migration phải có tài liệu impact.

8. Rule exception handling phải đi qua mechanism chuẩn.

9. Rule logging phải có correlation ID nếu hệ thống có distributed tracing.

10. Rule không được dùng `System.out.println` trong production code.

Tên file đề xuất:

```text
docs/grounding/harness/architecture_fitness_policy.md
src/test/java/.../ArchitectureTest.java
```

Trong mô hình cũ, architecture là tài liệu để người đọc. Trong Harness Engineering, architecture phải trở thành constraint có thể kiểm tra được.

## 2.5. Behaviour Harness

Đây là phần anh đã có nền tảng tốt nhất, vì anh đã có BPMN, Gherkin và business rule coverage.

Tuy nhiên cần chuẩn hóa thành traceability rõ hơn.

Tên file đề xuất:

```text
docs/grounding/harness/behaviour_harness_policy.md
docs/grounding/harness/traceability_matrix_template.md
```

Mỗi requirement nên trace theo chuỗi:

```text
Requirement ID
Business Rule ID
BPMN Step
Gherkin Scenario
Unit Test
Integration Test
Manual Verification
Log Evidence
Status
```

Ví dụ:

```text
FR_001
BR_001
BPMN_STEP_03
SCENARIO_001
OrderServiceTest.shouldRejectInvalidOrder
OrderApiTest.shouldReturn400ForInvalidOrder
Manual test on local UI
logs/evidence/FR_001_debug_run.log
PASS
```

Điểm mấu chốt: test không chỉ tồn tại. Test phải chứng minh được nó kiểm tra business rule nào.

Martin Fowler cũng nhấn mạnh behaviour harness là nhóm khó nhất vì kiểm chứng hành vi nghiệp vụ không đơn giản như kiểm tra format hoặc compile. Vì vậy test do AI sinh không đủ nếu thiếu spec, traceability và manual validation ở các luồng quan trọng. ([martinfowler.com][1])

## 2.6. Evidence Pack chuẩn

Anh đã yêu cầu evidence, nhưng nên chuẩn hóa thành một gói cố định để agent luôn báo cáo cùng format.

Tên file đề xuất:

```text
docs/grounding/harness/evidence_pack_template.md
```

Nội dung nên gồm:

```text
1. Task ID
2. Requirement ID liên quan
3. File đã thay đổi
4. Tóm tắt thay đổi
5. Test đã chạy
6. Kết quả test
7. Log kiểm tra
8. Unexpected exception
9. Unexpected error log
10. Manual verification
11. Risk còn lại
12. Tài liệu đã cập nhật
13. Đề xuất follow up
```

Definition of Done nên quy định rõ: không có Evidence Pack thì chưa được xem là hoàn thành.

## 2.7. Harness Backlog

Đây là điểm nhiều team bỏ qua.

Khi agent sai, không chỉ sửa code. Phải hỏi thêm:

1. Vì sao guide không ngăn được lỗi này?

2. Vì sao sensor không phát hiện sớm hơn?

3. Có cần thêm rule vào AGENTS.md không?

4. Có cần thêm test không?

5. Có cần thêm architecture fitness rule không?

6. Có cần cập nhật KMS không?

7. Có cần thêm checklist cho loại task này không?

Tên file đề xuất:

```text
docs/grounding/harness/harness_backlog.md
```

Ví dụ:

```text
Issue: Agent thường bỏ sót negative test cho API validation
Root cause: test_policy chưa yêu cầu negative scenario
Harness action: cập nhật test_policy, thêm checklist negative case
Sensor action: thêm rule kiểm tra mỗi API validation phải có ít nhất một negative test
Status: open
Owner: QA lead
```

Đây chính là steering loop. Harness không tĩnh. Harness phải học từ lỗi.

## 2.8. Harness Template theo loại task

Martin Fowler có nhắc đến harness template như cách chia sẻ guide và sensor cho các loại hệ thống hoặc topology trong tổ chức. ([martinfowler.com][2])

Với mô hình của anh, nên tạo template theo loại task vận hành và phát triển.

Tên thư mục đề xuất:

```text
docs/grounding/harness/templates/
```

Các template nên có:

```text
feature_development_harness.md
bug_fix_harness.md
rac_investigation_harness.md
infra_poc_harness.md
automation_script_harness.md
document_review_harness.md
java_spring_api_harness.md
batch_job_harness.md
helm_chart_harness.md
database_change_harness.md
```

Mỗi template nên gồm:

1. Khi nào dùng.

2. Guide bắt buộc.

3. Sensor bắt buộc.

4. Evidence bắt buộc.

5. Human approval bắt buộc khi nào.

6. KMS update bắt buộc khi nào.

## 3. Mô hình gate nên bổ sung vào SDD

Tôi đề xuất thêm năm gate chính.

## Gate 0: Request Intake Gate

Mục tiêu: không cho agent bắt đầu nếu yêu cầu quá mơ hồ.

Kiểm tra:

1. Có problem statement.

2. Có scope.

3. Có out of scope nếu cần.

4. Có expected output.

5. Có system affected.

6. Có constraint.

7. Có risk ban đầu.

8. Có source tài liệu hoặc ticket liên quan.

Kết quả:

```text
READY
NEED_CLARIFICATION
REJECTED
```

## Gate 1: Spec Readiness Gate

Mục tiêu: xác nhận spec đủ để thiết kế hoặc code.

Kiểm tra:

1. Functional requirement có ID.

2. Non functional requirement có ID nếu có.

3. Acceptance criteria có ID.

4. Business rule có ID.

5. Error case có mô tả.

6. Data input và output rõ.

7. Assumption được ghi lại.

8. Open question được xử lý hoặc chấp nhận.

## Gate 2: Implementation Readiness Gate

Mục tiêu: xác nhận agent được phép code.

Kiểm tra:

1. Story đủ nhỏ.

2. Architecture impact rõ.

3. File hoặc module liên quan đã xác định.

4. Test strategy rõ.

5. Sensor cần chạy đã xác định.

6. Risk cao đã có human approval.

7. KMS context đã được truy xuất.

## Gate 3: Local Sensor Gate

Mục tiêu: agent tự kiểm tra trước khi báo hoàn thành.

Kiểm tra:

1. Build pass.

2. Unit test pass.

3. Test liên quan pass.

4. Format pass.

5. Static analysis local pass nếu có.

6. Không có unexpected exception.

7. Không có unexpected error log.

8. Evidence Pack được tạo.

## Gate 4: Pull Request Sensor Gate

Mục tiêu: kiểm tra ở mức team.

Kiểm tra:

1. CI pass.

2. Coverage không giảm bất thường.

3. Integration test pass nếu có.

4. Architecture test pass.

5. Security scan không có issue nghiêm trọng.

6. Code review agent không có finding blocker.

7. Human reviewer approve.

## Gate 5: Knowledge Closure Gate

Mục tiêu: tránh tri thức bị mất sau khi task hoàn thành.

Kiểm tra:

1. KMS đã cập nhật nếu có thay đổi kiến trúc, rule, runbook hoặc convention.

2. Module note đã cập nhật nếu có thay đổi flow chính.

3. ADR đã tạo nếu có decision mới.

4. Harness backlog đã cập nhật nếu phát hiện lỗi lặp lại.

5. Evidence lưu đúng vị trí.

## 4. Cấu trúc tài liệu nên bổ sung

Anh có thể thêm cấu trúc sau vào project:

```text
docs/
  grounding/
    harness/
      harness_constitution.md
      guide_registry.md
      sensor_registry.md
      agent_permission_policy.md
      architecture_fitness_policy.md
      behaviour_harness_policy.md
      test_policy.md
      debug_policy.md
      evidence_policy.md
      evidence_pack_template.md
      traceability_matrix_template.md
      harness_backlog.md
      harness_metrics.md
      templates/
        feature_development_harness.md
        bug_fix_harness.md
        rac_investigation_harness.md
        infra_poc_harness.md
        automation_script_harness.md
        document_review_harness.md
        java_spring_api_harness.md
        batch_job_harness.md
        database_change_harness.md
```

Còn `AGENTS.md` chỉ nên đóng vai trò entry point:

```text
1. Đọc harness_constitution.md trước khi làm task.
2. Xác định task type.
3. Đọc guide_registry.md để lấy context bắt buộc.
4. Đọc sensor_registry.md để biết kiểm tra bắt buộc.
5. Không code nếu Gate 1 hoặc Gate 2 chưa đạt.
6. Sau khi code phải tạo Evidence Pack.
7. Nếu phát hiện lỗi lặp lại, cập nhật harness_backlog.md.
```

## 5. Bổ sung sensor cụ thể cho Java enterprise

Với stack Java, tôi đề xuất các sensor sau.

## 5.1. Build sensor

Mục tiêu: đảm bảo code compile được.

Tool:

```text
Maven
Gradle
```

Gate: bắt buộc cho mọi code change.

## 5.2. Unit test sensor

Mục tiêu: đảm bảo logic liên quan pass.

Tool:

```text
JUnit 5
Mockito
AssertJ
```

Gate: bắt buộc cho mọi feature và bug fix.

## 5.3. Integration test sensor

Mục tiêu: kiểm tra API, database, message broker hoặc external dependency.

Tool:

```text
Spring Boot Test
Testcontainers
REST Assured
WireMock
```

Gate: bắt buộc với thay đổi liên quan API, database, integration.

## 5.4. Architecture sensor

Mục tiêu: kiểm tra layer và module boundary.

Tool:

```text
ArchUnit
jQAssistant nếu cần kiểm tra kiến trúc sâu hơn
```

Gate: bắt buộc với repo lớn hoặc microservice quan trọng.

## 5.5. Static analysis sensor

Mục tiêu: phát hiện bug pattern và code smell.

Tool:

```text
SonarQube
SpotBugs
Checkstyle
PMD
Semgrep
```

Gate: bắt buộc ở pull request.

## 5.6. Security sensor

Mục tiêu: phát hiện dependency vulnerable hoặc secret leak.

Tool:

```text
OWASP Dependency Check
Trivy
Gitleaks
Semgrep security rules
```

Gate: bắt buộc trước merge.

## 5.7. Behaviour sensor

Mục tiêu: xác nhận code đúng nghiệp vụ.

Tool hoặc artifact:

```text
Gherkin scenario
Business rule coverage
API test
Manual verification checklist
Log evidence
```

Gate: bắt buộc với feature nghiệp vụ.

## 5.8. Runtime sensor

Mục tiêu: xác nhận ứng dụng chạy không phát sinh lỗi bất thường.

Tool:

```text
Application log
APM trace
Prometheus metric
Grafana dashboard
Loki log query
```

Gate: bắt buộc với task có thay đổi runtime behavior.

## 6. Bổ sung vào BMAD hoặc Agent workflow thế nào

Nếu anh dùng BMAD cùng SDD, workflow nên là:

1. Analyst agent làm rõ yêu cầu.

2. PM agent tạo PRD hoặc story.

3. Architect agent tạo architecture impact.

4. Harness gate kiểm tra implementation readiness.

5. Developer agent code.

6. Developer agent chạy local sensor.

7. Review agent chạy semantic review.

8. CI chạy computational sensor.

9. Human reviewer kiểm tra phần rủi ro cao.

10. Technical writer agent cập nhật KMS.

11. Harness backlog ghi nhận lỗi lặp lại.

Điểm khác biệt so với BMAD mặc định: sau mỗi bước quan trọng đều phải có gate và sensor, không chỉ có artifact.

## 7. Bổ sung metric để đo hiệu quả Harness Engineering

Không đo thì không biết harness có hiệu quả không.

Nên thêm file:

```text
docs/grounding/harness/harness_metrics.md
```

Metric đề xuất:

1. Tỷ lệ task bị trả lại do spec chưa rõ.

2. Tỷ lệ agent code pass build ngay lần đầu.

3. Tỷ lệ agent code pass unit test ngay lần đầu.

4. Số lỗi phát hiện bởi local sensor.

5. Số lỗi phát hiện bởi CI sensor.

6. Số lỗi phát hiện bởi human reviewer.

7. Số lỗi lọt sang test environment.

8. Số lỗi lặp lại cùng loại.

9. Thời gian từ request đến ready for implementation.

10. Thời gian từ implementation đến verification pass.

11. Số lần harness được cập nhật sau incident hoặc review finding.

Metric quan trọng nhất là: lỗi phải dịch chuyển sang trái. Tức lỗi nên được phát hiện ở spec gate, local sensor hoặc CI, thay vì phát hiện muộn ở UAT hoặc production. Martin Fowler cũng nhấn mạnh hướng “keep quality left”, tức đưa kiểm soát chất lượng sớm hơn trong vòng đời phát triển để giảm chi phí sửa lỗi. ([martinfowler.com][1])

## 8. Lộ trình triển khai thực tế

## Giai đoạn 1: Chuẩn hóa harness tối thiểu

Thời gian đề xuất: 1 đến 2 tuần.

Làm các việc sau:

1. Tạo `harness_constitution.md`.

2. Tạo `guide_registry.md`.

3. Tạo `sensor_registry.md`.

4. Tạo `evidence_pack_template.md`.

5. Cập nhật `AGENTS.md`.

6. Áp dụng cho một repo nhỏ hoặc một module ít rủi ro.

Mục tiêu: agent biết phải đọc gì, chạy gì, báo cáo gì.

## Giai đoạn 2: Thêm sensor tự động

Thời gian đề xuất: 2 đến 4 tuần.

Làm các việc sau:

1. Chuẩn hóa build command.

2. Chuẩn hóa unit test command.

3. Thêm static analysis.

4. Thêm architecture check cơ bản bằng ArchUnit.

5. Thêm security scan cơ bản.

6. Thêm log check trong debug run.

Mục tiêu: giảm phụ thuộc vào review thủ công.

## Giai đoạn 3: Thêm behaviour harness

Thời gian đề xuất: 3 đến 6 tuần.

Làm các việc sau:

1. Chuẩn hóa Requirement ID.

2. Chuẩn hóa Business Rule ID.

3. Chuẩn hóa Gherkin mapping.

4. Chuẩn hóa test mapping.

5. Tạo traceability matrix.

6. Bổ sung manual verification checklist.

Mục tiêu: chứng minh code đúng nghiệp vụ, không chỉ đúng kỹ thuật.

## Giai đoạn 4: Tạo harness template theo task type

Thời gian đề xuất: 4 đến 8 tuần.

Làm các việc sau:

1. Template cho feature development.

2. Template cho bug fix.

3. Template cho RAC investigation.

4. Template cho infrastructure PoC.

5. Template cho automation script.

6. Template cho database change.

Mục tiêu: mỗi loại task có guide, sensor và evidence riêng.

## Giai đoạn 5: Steering loop và continuous improvement

Thời gian đề xuất: liên tục.

Làm các việc sau:

1. Review lỗi lặp lại mỗi tuần.

2. Cập nhật harness backlog.

3. Bổ sung guide nếu agent hiểu sai.

4. Bổ sung sensor nếu lỗi phát hiện muộn.

5. Đo metric harness.

6. Review định kỳ với dev, test, PM và architect.

Mục tiêu: harness ngày càng mạnh hơn sau mỗi task.

## 9. Câu trả lời trực tiếp

Với mô hình SDD anh đã triển khai, cần bổ sung các thành phần sau để đạt Harness Engineering:

1. `harness_constitution.md` để định nghĩa nguyên tắc agent làm việc.

2. `guide_registry.md` để agent biết phải đọc tài liệu nào theo từng loại task.

3. `sensor_registry.md` để định nghĩa các kiểm tra bắt buộc sau khi agent làm xong.

4. `agent_permission_policy.md` để giới hạn quyền của agent.

5. `architecture_fitness_policy.md` và test bằng ArchUnit hoặc tool tương đương.

6. `behaviour_harness_policy.md` để trace requirement, business rule, Gherkin, test và evidence.

7. `evidence_pack_template.md` để chuẩn hóa báo cáo hoàn thành.

8. `harness_backlog.md` để ghi nhận lỗi lặp lại và cải tiến harness.

9. Harness template cho từng loại task như feature, bug fix, RAC, PoC, automation script, database change.

10. CI quality gate để sensor chạy tự động thay vì chỉ dựa vào agent tự báo cáo.

11. Runtime sensor để kiểm tra log, metric, trace sau khi chạy ứng dụng.

12. Metric để đo harness có thực sự giảm lỗi và giảm review effort hay không.

Nói gọn lại: SDD của anh hiện là phần “spec và verification model”. Để thành Harness Engineering, cần bổ sung “guide registry, sensor registry, permission policy, architecture fitness function, evidence pack và steering loop”.

Công thức hoàn chỉnh nên là:

```text
SDD = đặc tả đúng việc cần làm

BMAD = tổ chức agent làm việc theo vai trò và workflow

Harness Engineering = kiểm soát agent bằng guide, sensor và feedback loop

KMS = lưu tri thức đã được kiểm duyệt

CI/CD = xác minh tự động bằng evidence
```

mô hình hiện tại sẽ không chỉ là SDD có AI hỗ trợ, mà trở thành một operating model hoàn chỉnh cho agentic software delivery.

[1]: https://martinfowler.com/articles/harness-engineering.html "Harness engineering for coding agent users"
[2]: https://agents.md/?utm_source=chatgpt.com "AGENTS.md"
[3]: https://docs.bmad-method.org/?utm_source=chatgpt.com "Welcome to the BMad Method | BMAD Method"
[4]: https://docs.langchain.com/oss/python/langgraph/durable-execution?utm_source=chatgpt.com "Durable execution"
[5]: https://martinfowler.com/articles/harness-engineering.html?utm_source=chatgpt.com "Harness engineering for coding agent users"
[6]: https://martinfowler.com/articles/exploring-gen-ai/harness-engineering-memo.html?utm_source=chatgpt.com "Harness Engineering - first thoughts"
