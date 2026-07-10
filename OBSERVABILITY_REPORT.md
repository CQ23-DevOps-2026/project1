# Báo cáo triển khai Observability cho hệ thống YAS

**Hệ thống:** YAS - Yet Another Shop  
**Phạm vi:** Observability stack cho microservices trên Kubernetes  
**Thành phần liên quan:** **OpenTelemetry Collector**, **Grafana**, **Prometheus**, **Tempo**, **Loki**, **Promtail**  
**Nhánh làm việc:** `fix/observability-dashboard-metrics`

## 1. Mục tiêu

Yêu cầu của đề bài là triển khai hệ thống **Observability** theo screenshots của dự án, bao gồm dashboard tổng quan và màn hình tracing trên **Tempo**. Ngoài việc hiển thị được dashboard, nhóm cần chứng minh rằng có thể sử dụng các chức năng cơ bản của hệ thống quan sát: xem metric, tìm log, lấy **`traceId`**, và đối chiếu trace giữa các service.

Trong repo gốc, một phần nền tảng observability đã tồn tại sẵn, bao gồm cấu trúc Helm/Kubernetes, dashboard ban đầu và cấu hình telemetry cơ bản cho các service. Công việc của nhóm không phải là viết lại toàn bộ observability từ đầu, mà tập trung vào việc triển khai lại các thành phần trên môi trường thực tế, sửa cấu hình để các thành phần giao tiếp đúng với nhau, bổ sung/cấu hình **Loki** để thu thập log và liên kết log với trace, đồng thời chỉnh sửa dashboard **Grafana** để hiển thị đúng dữ liệu theo yêu cầu.

## 2. Bản chất cấu hình Observability trong dự án

Khác với một số công cụ như Jenkins, nơi nhiều cấu hình có thể được thao tác trực tiếp trên UI, phần observability của dự án này chủ yếu được quản lý bằng cấu hình trong source code và deployment manifests. Các thay đổi quan trọng nằm trong thư mục `k8s/deploy/observability`, sau đó được apply/deploy vào Kubernetes.

Điều này có nghĩa là:

- Cấu hình datasource, dashboard, **Collector**, **RBAC** và **ServiceAccount** đều nằm trong repo.
- Khi cần thay đổi cách các thành phần giao tiếp, nhóm sửa file YAML/JSON trong repo rồi deploy lại.
- UI **Grafana** chủ yếu dùng để quan sát kết quả, explore log/metric/trace, không phải nơi cấu hình chính của hệ thống.
- Việc cấu hình bằng file giúp tái triển khai được, dễ kiểm tra diff, dễ review và tránh phụ thuộc vào thao tác thủ công trên giao diện.

Nói ngắn gọn: phần quan trọng của observability không nằm ở việc bấm cấu hình trên UI, mà nằm ở việc cấu hình đúng trong source/deploy để service bắn telemetry đúng về **OpenTelemetry Collector**, **Collector** chuyển tiếp đúng sang **Loki**/**Tempo**/**Prometheus**, và **Grafana** đọc đúng các datasource đó.

## 3. Kiến trúc Observability

Kiến trúc sau khi cấu hình có thể mô tả theo luồng dữ liệu:

```text
Microservices
  -> OpenTelemetry telemetry/log/metric/trace
  -> OpenTelemetry Collector
      -> Tempo: lưu và truy vấn distributed tracing
      -> Prometheus: lưu và truy vấn metrics

Pod/container logs
  -> Promtail
      -> Loki: lưu và truy vấn logs

Grafana
  -> Dashboard metrics
  -> Explore logs trên Loki
  -> Explore traces trên Tempo
  -> liên kết log và trace thông qua traceId
```

Vai trò từng thành phần:

- **OpenTelemetry Collector:** điểm nhận tập trung cho telemetry từ các service, xử lý và chuyển tiếp sang backend phù hợp.
- **Tempo:** lưu trace và cho phép xem request đi qua nhiều service.
- **Prometheus:** lưu metrics, phục vụ các truy vấn **PromQL** cho dashboard.
- **Loki:** lưu logs, cho phép tìm log theo service và lấy **`traceId`**.
- **Promtail:** hỗ trợ thu thập log từ container/pod trong trường hợp log được đọc từ stdout/file.
- **Grafana:** giao diện tổng hợp để xem dashboard, log và trace.

## 4. Phần có sẵn trong repo gốc

Repo gốc đã có sẵn nhiều cấu hình nền tảng cho observability:

- Cấu trúc thư mục `k8s/deploy/observability`.
- Các file values cho **Loki**, **Prometheus**, **Tempo**, **Promtail**.
- Helm chart riêng cho **Grafana** và **OpenTelemetry**.
- Dashboard observability ban đầu.
- Datasource ban đầu cho **Grafana**.
- Cấu hình telemetry/OpenTelemetry ở mức service đã được chuẩn bị trong dự án.

Tuy nhiên, khi triển khai lên môi trường thực tế, các thành phần chưa hoạt động hoàn chỉnh với nhau. Dashboard có nhiều panel chưa query đúng metric thực tế, **Loki** chưa liên kết trace ổn định sang **Tempo**, và **Collector** cần bổ sung metadata Kubernetes để trace/dashboard dễ lọc và dễ đọc hơn.

## 5. Công việc đã thực hiện

### 5.1. Triển khai lại observability stack

Nhóm triển khai lại các thành phần observability lên Kubernetes, bao gồm **Grafana**, **Prometheus**, **Tempo**, **Loki**, **Promtail** và **OpenTelemetry Collector**. Mục tiêu là đảm bảo các pod/service của observability chạy được, có thể kết nối nội bộ trong cluster, và **Grafana** có thể truy cập được các datasource cần thiết.

**Cần chèn ảnh:**

![TODO: Trạng thái pod observability](screenshots/TODO-observability-pods.png)

Gợi ý ảnh chụp:

```powershell
kubectl get pods -n observability
kubectl get svc -n observability
```

### 5.2. Cấu hình OpenTelemetry Collector

**OpenTelemetry Collector** được cấu hình làm điểm trung gian tiếp nhận telemetry từ các service và chuyển tiếp sang các backend tương ứng. Phần quan trọng là pipeline trace, metric và log phải trỏ đúng về **Tempo**, **Prometheus** và **Loki**.

Trong cấu hình Kubernetes, nhóm bổ sung **`k8s_attributes`** processor để **Collector** gắn thêm metadata Kubernetes vào trace:

```yaml
k8s_attributes:
  auth_type: serviceAccount
  passthrough: false
  extract:
    metadata:
      - k8s.namespace.name
      - k8s.pod.name
      - k8s.deployment.name
      - k8s.node.name
```

Pipeline trace được cập nhật để đi qua processor này:

```yaml
pipelines:
  traces:
    receivers: [otlp]
    processors: [k8s_attributes, batch]
    exporters: [otlp_http]
```

Ý nghĩa của thay đổi này là khi xem trace trong **Tempo**/**Grafana**, trace không chỉ có span của ứng dụng mà còn có thông tin namespace, pod, deployment và node. Điều này giúp việc lọc, debug và đối chiếu service trong Kubernetes dễ hơn.

**Cần chèn ảnh:**

![TODO: Cấu hình OpenTelemetry Collector](screenshots/TODO-otel-collector-config.png)

File liên quan:

- `k8s/deploy/observability/opentelemetry/values.yaml`
- `k8s/deploy/observability/opentelemetry/templates/opentelemetry-collector.yaml`

### 5.3. Bổ sung ServiceAccount và RBAC cho Collector

Để **`k8s_attributes`** processor đọc được thông tin pod, namespace, node và deployment, **Collector** cần quyền truy cập Kubernetes API. Nhóm bổ sung **ServiceAccount** và **RBAC** riêng cho **OpenTelemetry Collector**.

**RBAC** cho phép **Collector** đọc các resource cần thiết:

```yaml
resources: ["pods", "namespaces", "nodes"]
verbs: ["get", "list", "watch"]
```

Và với nhóm `apps`:

```yaml
resources: ["replicasets", "deployments"]
verbs: ["get", "list", "watch"]
```

Collector template được cập nhật để sử dụng **ServiceAccount** này:

```yaml
serviceAccount: { { include "opentelemetry.serviceAccountName" . } }
```

**Cần chèn ảnh:**

![TODO: ServiceAccount và RBAC của Collector](screenshots/TODO-otel-rbac-serviceaccount.png)

File liên quan:

- `k8s/deploy/observability/opentelemetry/templates/serviceaccount.yaml`
- `k8s/deploy/observability/opentelemetry/templates/rbac.yaml`
- `k8s/deploy/observability/opentelemetry/templates/opentelemetry-collector.yaml`

### 5.4. Cấu hình Loki, Promtail và liên kết log với Tempo

**Loki** được dùng để xem log của các service. Trong môi trường Kubernetes, **Promtail** hỗ trợ thu thập log từ pod/container và đẩy về **Loki**:

```yaml
clients:
  - url: http://loki-gateway/loki/api/v1/push
```

Điểm quan trọng là log cần có **`traceId`** để có thể đối chiếu với trace trong **Tempo**. Trong **Grafana Loki datasource**, nhóm cấu hình `derivedFields` để bắt nhiều dạng trace id khác nhau:

```yaml
derivedFields:
  - datasourceUid: tempo
    matcherRegex: '(?:traceId=|trace_id=|"trace_id":"|"traceId":")([A-Fa-f0-9]{16,32})'
    name: traceId
    url: "$${__value.raw}"
```

Ý nghĩa:

- Khi log có `traceId=...`, `trace_id=...`, `"trace_id":"..."` hoặc `"traceId":"..."`, **Grafana** sẽ nhận diện được trace id.
- Trường `datasourceUid: tempo` giúp **Grafana** tạo link tự động từ log trong **Loki** sang trace tương ứng trong **Tempo**.
- Người dùng có thể bắt đầu từ log lỗi, lấy **`traceId`**, sau đó mở trace để xem request đã đi qua những service nào.

**Cần chèn ảnh:**

![TODO: Loki datasource derivedFields](screenshots/TODO-loki-derived-fields.png)

File liên quan:

- `k8s/deploy/observability/grafana/templates/loki-datasource.yaml`
- `k8s/deploy/observability/promtail.values.yaml`

### 5.5. Cấu hình Tempo datasource

**Tempo datasource** trong **Grafana** được cấu hình để hỗ trợ xem trace, liên kết trace sang log và hiển thị service map:

```yaml
jsonData:
  tracesToLogsV2:
    datasourceUid: loki
  serviceMap:
    datasourceUid: prometheus
  nodeGraph:
    enabled: true
  lokiSearch:
    datasourceUid: loki
```

Cấu hình này giúp khi xem trace có thể đối chiếu ngược lại logs trên **Loki** và dùng **Prometheus** để hỗ trợ service map.

**Cần chèn ảnh:**

![TODO: Tempo datasource hoặc trace detail](screenshots/TODO-tempo-trace-detail.png)

File liên quan:

- `k8s/deploy/observability/grafana/templates/tempo-datasource.yaml`

### 5.6. Chỉnh sửa Grafana dashboard

Dashboard ban đầu trong repo gốc tồn tại nhưng nhiều panel chưa hiển thị đúng với metric thực tế sau khi deploy. Nhóm đã chỉnh lại dashboard **Grafana** để các panel query đúng **Prometheus metrics** và có thể lọc theo namespace/service.

Một số nhóm panel đã chỉnh:

- HTTP request rate theo status, method, uri.
- Tổng số request.
- Tổng request thành công.
- Tỷ lệ request lỗi.
- JVM memory heap/non-heap.
- JVM GC duration.
- Thread count.
- CPU usage.
- Memory usage.
- Database connection.

Ví dụ query HTTP request rate:

```promql
sum by (status, method, uri) (
  rate(http_server_requests_seconds_count{
    namespace="$namespace",
    service="$service"
  }[1m]) * 60
)
```

Ví dụ query tỷ lệ request lỗi:

```promql
(
  sum(increase(http_server_requests_seconds_count{
    namespace="$namespace",
    service="$service",
    status=~"4..|5.."
  }[$__range])) or on() vector(0)
)
/
clamp_min(
  sum(increase(http_server_requests_seconds_count{
    namespace="$namespace",
    service="$service"
  }[$__range])) or on() vector(0),
  1
)
```

Dashboard cũng có biến lọc:

- `namespace`
- `service`
- `jvm_memory_pool_heap`
- `jvm_memory_pool_nonheap`
- `jvm_buffer_pool`

**Cần chèn ảnh:**

![TODO: Dashboard observability](screenshots/TODO-observability-dashboard.png)

File liên quan:

- `k8s/deploy/observability/grafana/dashboards/observability_dashboard.json`

## 6. Cách sử dụng các chức năng cơ bản

### 6.1. Xem metrics trên Grafana dashboard

Người dùng mở **Grafana**, vào dashboard Observability Dashboard, chọn namespace và service cần xem. Dashboard sẽ hiển thị các metric như request rate, request lỗi, CPU, memory, JVM và database connection.

Thao tác cơ bản:

1. Mở **Grafana**.
2. Chọn dashboard Observability Dashboard.
3. Chọn time range phù hợp.
4. Chọn namespace.
5. Chọn service cần quan sát.
6. Đọc các panel về HTTP, JVM, CPU, memory.

**Cần chèn ảnh:**

![TODO: Chọn namespace và service trên dashboard](screenshots/TODO-dashboard-filter.png)

### 6.2. Query metrics bằng Prometheus/PromQL

Trong **Grafana Explore**, chọn datasource **Prometheus** và query các metric cơ bản.

Ví dụ:

```promql
up
```

```promql
rate(http_server_requests_seconds_count[1m])
```

```promql
sum by (service) (process_cpu_usage)
```

Mục đích là kiểm tra **Prometheus** có nhận được metric từ service/**Collector** hay không và các service đang có trạng thái như thế nào.

**Cần chèn ảnh:**

![TODO: Query Prometheus trong Grafana Explore](screenshots/TODO-prometheus-query.png)

### 6.3. Xem log trên Loki và lấy traceId

Trong **Grafana Explore**, chọn datasource **Loki** và query log theo service.

Ví dụ:

```logql
{service_name="product-service"}
```

Hoặc tùy label thực tế trong môi trường:

```logql
{service="product-service"}
```

Sau khi log hiển thị, tìm trường **`traceId`** hoặc **`trace_id`**. Nếu datasource đã cấu hình đúng `derivedFields`, **Grafana** sẽ hiện link trace tương ứng để mở sang **Tempo**.

**Cần chèn ảnh:**

![TODO: Loki log có traceId](screenshots/TODO-loki-log-traceid.png)

### 6.4. Xem distributed trace trên Tempo

Từ **Grafana Explore**, chọn datasource **Tempo** hoặc click trực tiếp từ **`traceId`** trong log. **Tempo** sẽ hiển thị trace của request, bao gồm các span và thời gian xử lý của từng service.

Thông tin cần biết khi đọc trace:

- Trace gồm nhiều span.
- Mỗi span đại diện cho một thao tác, ví dụ HTTP request, database call, hoặc call sang service khác.
- Duration cho biết thao tác nào chậm.
- Status/error giúp xác định lỗi nằm ở đâu.
- Service name giúp biết request đã đi qua những service nào.

**Cần chèn ảnh:**

![TODO: Tempo trace detail](screenshots/TODO-tempo-trace-detail.png)

## 7. Kiểm chứng kết quả

Sau khi cấu hình, hệ thống đạt các kết quả sau:

- **Grafana** hiển thị được Observability Dashboard theo yêu cầu.
- **Prometheus** có thể query metrics của service.
- **Loki** hiển thị log của service.
- Log có **`traceId`** có thể liên kết sang **Tempo**.
- **Tempo** hiển thị distributed trace của request.
- Dashboard có thể lọc theo namespace và service.
- **Collector** gắn được metadata Kubernetes vào trace nhờ **`k8s_attributes`**.

**Cần chèn ảnh tổng hợp:**

![TODO: Dashboard metrics theo screenshot đề bài](screenshots/TODO-final-dashboard.png)

![TODO: Tempo theo screenshot đề bài](screenshots/TODO-final-tempo.png)

## 8. Các file cấu hình chính

Các file cấu hình chính đã dùng trong quá trình triển khai và chỉnh sửa:

```text
k8s/deploy/observability/loki.values.yaml
k8s/deploy/observability/promtail.values.yaml
k8s/deploy/observability/prometheus.values.yaml
k8s/deploy/observability/tempo.values.yaml
k8s/deploy/observability/grafana/templates/loki-datasource.yaml
k8s/deploy/observability/grafana/templates/tempo-datasource.yaml
k8s/deploy/observability/grafana/dashboards/observability_dashboard.json
k8s/deploy/observability/opentelemetry/values.yaml
k8s/deploy/observability/opentelemetry/templates/opentelemetry-collector.yaml
k8s/deploy/observability/opentelemetry/templates/rbac.yaml
k8s/deploy/observability/opentelemetry/templates/serviceaccount.yaml
```

Mục này dùng để giải thích nơi nhóm cấu hình hệ thống. Không cần xem đây là phần chính của yêu cầu, mà là phụ lục hỗ trợ khi cần chứng minh cấu hình nằm trong repo.

## 9. Kết luận

Nhóm không xây dựng mới toàn bộ observability stack từ đầu, vì repo gốc đã có sẵn nhiều cấu hình nền tảng. Công việc chính của nhóm là triển khai lại stack trên môi trường thực tế, sửa cách các thành phần giao tiếp với nhau, bổ sung khả năng liên kết log và trace, và chỉnh sửa dashboard để hiển thị đúng metric theo yêu cầu.

Kết quả cuối cùng là hệ thống có thể quan sát theo ba nhóm dữ liệu chính:

- **Metrics:** xem trên **Prometheus**/**Grafana** dashboard.
- **Logs:** xem trên **Loki**.
- **Traces:** xem trên **Tempo**.

Ngoài ra, việc liên kết **`traceId`** từ **Loki** sang **Tempo** giúp quá trình debug hiệu quả hơn: từ một dòng log có thể mở trực tiếp trace của request và xem request đó đi qua những service nào, mất bao lâu, và lỗi phát sinh ở đâu.
