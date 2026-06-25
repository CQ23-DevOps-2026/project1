# Báo cáo khắc phục lỗi triển khai hệ thống YAS

**Hệ thống:** YAS (Yet Another Shop)  
**Môi trường:** Kubernetes K3s trên máy chủ `yas-server`  
**Ngày thực hiện:** 24/06/2026  
**Nhánh làm việc:** `fix/deploy-local-setup`

## 1. Mục tiêu và phạm vi

Tài liệu này tổng hợp quá trình kiểm tra, debug và khắc phục các lỗi phát sinh khi triển khai YAS trên môi trường Kubernetes K3s. Nội dung bao gồm công việc ban đầu do Antigravity thực hiện và phần tiếp tục kiểm tra, hoàn thiện sau đó.

Báo cáo được lập dựa trên 9 commit đã tạo trên `yas-server`:

| Commit | Nội dung |
|---|---|
| `16b50a46` | Build `payment-paypal` thành Spring Boot service có thể thực thi |
| `d491bb10` | Ổn định luồng đăng nhập OAuth2 của hai BFF |
| `bdc38a29` | Công khai đầy đủ các route tài liệu API |
| `4951c2b5` | Sửa security matcher của Cart |
| `1d1062c9` | Giữ cấu hình OAuth2 cho profile dev của Storefront BFF |
| `fff1272c` | Sửa sai tên cột trong Liquibase migration của Payment |
| `543bfee8` | Điều chỉnh health probe cho các backend khởi động chậm |
| `d25a3131` | Đồng bộ phiên bản Elasticsearch và Kibana |
| `d8806ed8` | Sửa cấu hình triển khai Grafana/Prometheus |

## 2. Tóm tắt kết quả

Các nhóm lỗi chính đã được xác định và xử lý:

- Pod crash do service `payment-paypal` chưa được đóng gói thành executable Spring Boot JAR.
- Pod `payment` crash do Liquibase dùng sai tên cột.
- BFF không khởi động hoặc đăng nhập thất bại do cấu hình OAuth2, issuer, redirect URI và giao thức HTTP/HTTPS không đồng nhất.
- Trình duyệt báo cookie không tồn tại hoặc Keycloak từ chối `redirect_uri`.
- Swagger chỉ hiển thị một phần service do ingress và danh sách API chưa đầy đủ.
- Request Cart chưa đăng nhập trả `403` không đúng bản chất do security matcher sai đường dẫn.
- Cấu hình dev của Storefront BFF bị mất sau khi tách cấu hình production.
- Pod backend dễ bị restart trong lúc khởi động vì health probe quá gắt.
- Search không tương thích với phiên bản Elasticsearch cũ.
- Grafana dùng nhầm key username/password và cấu hình database ngoài không phù hợp.

Sau khi sửa, các thành phần liên quan đã build/test thành công; Cart được triển khai lại và request ẩn danh tới Cart trả đúng `401 Unauthorized`.

## 3. Chi tiết các lỗi và cách khắc phục

### 3.1. Payment PayPal không phải executable Spring Boot service

**Commit:** `16b50a46`

**Hiện tượng**

Pod `payment-paypal` không thể khởi động đúng như một Spring Boot application. Module thiếu lớp main và cấu hình build cần thiết để tạo executable JAR.

**Nguyên nhân**

- Chưa có `PaymentPaypalApplication`.
- Maven chưa chạy Spring Boot repackage cho module.
- Chưa có security filter chain tối thiểu cho resource server và các endpoint health/Swagger.

**Khắc phục**

- Thêm lớp main sử dụng `@SpringBootApplication`.
- Thêm `spring-boot-maven-plugin` vào `payment-paypal/pom.xml`.
- Thêm `SecurityConfig`, cho phép health check và Swagger; các endpoint còn lại yêu cầu xác thực JWT.

**Kiểm chứng**

- Maven build thành công.
- 10 test của PayPal và 7 test của common library đều pass.
- Image local đã được build/import vào K3s và service có thể khởi động.

### 3.2. Liquibase migration của Payment dùng sai tên cột

**Commit:** `fff1272c`

**Hiện tượng**

Pod `payment` rơi vào `CrashLoopBackOff` khi Liquibase áp dụng dữ liệu provider.

**Nguyên nhân**

Hai changelog sử dụng cột `is_enabled`, trong khi DDL thực tế định nghĩa cột `enabled`.

**Khắc phục**

Đổi `is_enabled` thành `enabled` trong:

- `changelog-0001-provider.sql`
- `changelog-0002-provider.sql`

Thay đổi này làm migration khớp với schema hiện tại và loại bỏ lỗi khởi động của Payment.

### 3.3. OAuth2 BFF không ổn định do issuer và HTTP/HTTPS

**Commit:** `d491bb10`

**Hiện tượng**

Trong quá trình debug đã lần lượt xuất hiện các lỗi:

- `Unable to resolve Configuration with the provided Issuer`
- `authorizationGrantType cannot be null`
- Keycloak báo `Invalid parameter: redirect_uri`
- Trang login báo không tìm thấy cookie dù cookie đã được bật.
- Callback hoặc redirect quay về HTTP trong khi người dùng truy cập bằng HTTPS.
- BFF có thể bị treo khi gọi domain public từ bên trong cluster.

**Nguyên nhân**

Luồng OAuth2 có hai đối tượng truy cập khác nhau:

- Trình duyệt cần dùng domain HTTPS công khai.
- Pod trong cluster nên dùng Kubernetes service nội bộ.

Việc dùng chung một URL issuer cho cả hai hướng gây ra vấn đề hairpin NAT, chứng chỉ self-signed và URL redirect sai giao thức. Ngoài ra, registration ID và callback giữa frontend, BFF và Keycloak chưa đồng nhất.

**Khắc phục**

Áp dụng cấu hình split-horizon:

- Authorization endpoint cho trình duyệt dùng `https://identity.yas.local.com`.
- Token, JWK và user-info endpoint cho BFF dùng Keycloak service nội bộ qua HTTP.
- Chuẩn hóa registration ID thành `api-client`.
- Khai báo rõ `authorization-grant-type: authorization_code`.
- Chuẩn hóa callback thành `{baseUrl}/login/oauth2/code/{registrationId}`.
- Cập nhật Keycloak hostname và backchannel phù hợp với ingress.
- Ép các ingress liên quan chuyển hướng sang HTTPS.
- Cập nhật URL đăng nhập của Storefront UI cho đúng registration.

**Kết quả**

- BFF khởi động được với profile production.
- URL authorize và callback đều sử dụng HTTPS.
- Session cookie được phát hành với thuộc tính `Secure`.
- Keycloak chấp nhận redirect URI đã cấu hình.

### 3.4. Cấu hình OAuth2 local/dev của Storefront BFF bị mất

**Commit:** `1d1062c9`

**Hiện tượng**

Sau khi tách cấu hình OAuth2 production sang cấu hình Kubernetes, chạy Storefront BFF với profile dev có nguy cơ không tạo được OAuth2 client registration.

**Nguyên nhân**

Xóa toàn bộ block OAuth2 khỏi `application.yaml` giúp production tránh dùng issuer sai, nhưng đồng thời làm profile dev mất provider và registration cần thiết.

**Khắc phục**

Đưa cấu hình OAuth2 dành riêng cho local development vào `application-dev.yaml`:

- Provider Keycloak nội bộ.
- Registration `api-client`.
- Authorization code grant.
- Redirect URI theo registration ID.
- Các scope `openid`, `profile`, `email`, `roles`.

Cách làm này giữ được môi trường dev mà không làm ảnh hưởng cấu hình production do Kubernetes cung cấp.

### 3.5. Cart trả 403 cho người dùng chưa đăng nhập

**Commit:** `4951c2b5`

**Hiện tượng**

Request:

```text
GET /api/cart/storefront/cart/items
```

trả về `403 Forbidden` khi chưa đăng nhập.

**Nguyên nhân**

Security matcher đang bảo vệ nhầm đường dẫn:

```text
/storefront/carts/**
```

trong khi controller thật sử dụng:

```text
/storefront/cart/**
```

Do không khớp matcher, request lọt vào rule `/storefront/**.permitAll()`, sau đó mới thất bại trong business code khi không tìm thấy user ID. Vì vậy response trở thành 403 thay vì được Spring Security chặn đúng tại authentication layer.

**Khắc phục**

Đổi matcher thành `/storefront/cart` và `/storefront/cart/**`, yêu cầu role `CUSTOMER`.

**Kiểm chứng**

- 84 test của Cart pass.
- Build toàn reactor liên quan thành công.
- Build/import image `yas-cart:localfix` và rollout deployment thành công.
- Pod Cart mới ở trạng thái `1/1 Running`.
- Request chưa đăng nhập trả đúng `401 Unauthorized`.

### 3.6. Swagger và API ingress chưa đầy đủ

**Commit:** `bdc38a29`

**Hiện tượng**

Swagger UI chỉ hiển thị một số API, ban đầu chủ yếu thấy Product; các API khác không truy cập được qua domain chung.

**Nguyên nhân**

- Danh sách service trong Swagger UI chưa đầy đủ hoặc URL chưa dùng đúng HTTPS.
- Ingress thiếu các path tới nhiều backend service.

**Khắc phục**

- Cập nhật danh sách tài liệu API trong chart Swagger UI.
- Chuẩn hóa URL tài liệu sang HTTPS.
- Bổ sung ingress path cho các service còn thiếu.

**Kiểm chứng**

- Helm template của `swagger-ui` render thành công.
- Manifest API ingress vượt qua server-side dry-run.
- Swagger và các route API liên quan phản hồi thành công qua ingress.

### 3.7. Backend bị restart do health probe quá ngắn

**Commit:** `543bfee8`

**Hiện tượng**

Một số service Java khởi động chậm trong lúc kết nối database, Kafka, Keycloak hoặc chạy migration. Kubelet có thể đánh dấu lỗi và restart container trước khi ứng dụng hoàn tất khởi động.

**Khắc phục**

Tăng `failureThreshold` của liveness và readiness probe từ `12` lên `45`. Với chu kỳ 10 giây, service có thêm thời gian ổn định trong giai đoạn startup mà không phải tắt health check.

**Kiểm chứng**

Helm chart backend render thành công và không có lỗi cú pháp.

### 3.8. Elasticsearch không tương thích với Search service

**Commit:** `d25a3131`

**Hiện tượng**

Pod `search` crash hoặc nhận HTTP 400 khi giao tiếp với Elasticsearch.

**Nguyên nhân**

Ứng dụng Spring Boot 4/Spring Data Elasticsearch sử dụng client mới, trong khi cluster đang chạy Elasticsearch/Kibana `8.8.1`. Hai phía không tương thích hoàn toàn về API.

**Khắc phục**

Nâng đồng bộ:

- Elasticsearch: `8.8.1` → `9.2.3`
- Kibana: `8.8.1` → `9.2.3`

Việc nâng đồng thời tránh tình trạng Kibana và Elasticsearch lệch major/minor version.

**Kiểm chứng**

Helm chart Elasticsearch/Kibana render thành công. Endpoint search suggest đã phản hồi `200 OK` sau khi hệ thống ổn định.

### 3.9. Cấu hình Grafana/Prometheus không chính xác

**Commit:** `d8806ed8`

**Hiện tượng**

Grafana Operator đọc nhầm username/password từ Secret. Cấu hình Grafana trong kube-prometheus-stack còn cố kết nối PostgreSQL với cấu hình SSL/database không phù hợp môi trường hiện tại.

**Nguyên nhân**

- `adminPassword` trỏ vào key `username`.
- `adminUser` trỏ vào key `password`.
- Block database PostgreSQL bên ngoài không cần thiết và có thể làm Grafana lỗi kết nối.

**Khắc phục**

- Đổi `adminPassword` sang key `password`.
- Đổi `adminUser` sang key `username`.
- Loại bỏ block database PostgreSQL ngoài khỏi `prometheus.values.yaml`, sử dụng cấu hình mặc định phù hợp với deployment hiện tại.

**Kiểm chứng**

Các chart Grafana và kube-prometheus-stack render thành công.

## 4. Các bước debug đáng chú ý

Quá trình xử lý không chỉ dựa trên trạng thái pod mà còn đối chiếu từng lớp:

1. Dùng `kubectl get pods -o wide` để xác định pod crash, restart và node/IP đang chạy.
2. Dùng `kubectl logs` và log của lần chạy trước để phân biệt lỗi cấu hình, migration, network và security.
3. Kiểm tra deployment, ConfigMap, Secret và giá trị environment thực tế trong pod.
4. Kiểm tra luồng OAuth2 từ browser-facing URL tới token/JWK backchannel.
5. Kiểm tra redirect URI, registration ID, cookie `Secure` và giao thức callback.
6. Render Helm chart trước khi áp dụng để bắt lỗi template.
7. Build/test từng module Maven liên quan trước khi tạo image.
8. Import image local trực tiếp vào containerd của K3s và theo dõi rollout.
9. Gọi endpoint qua ingress để xác nhận status code thực tế.
10. Kiểm tra `git diff`, chia commit theo nguyên nhân và tránh trộn thay đổi của người khác.

## 5. Kiểm thử và xác nhận

Các kiểm tra chính đã thực hiện:

- `payment-paypal`: 10 test pass.
- `common-library`: 7 test pass.
- `cart`: 84 test pass.
- `backoffice-bff` và `storefront-bff`: Maven build thành công; hai module chưa có test riêng.
- Storefront Next.js: Docker build thành công.
- Helm template cho backend, Elasticsearch/Kibana, Grafana, Prometheus, YAS configuration và Swagger UI đều render thành công ở các thay đổi tương ứng.
- API ingress vượt qua server-side dry-run.
- PostgreSQL phản hồi `pg_isready`.
- Cart rollout thành công, pod `1/1 Running`.
- Anonymous Cart trả `401` sau khi sửa matcher.
- Search suggest và các endpoint Swagger liên quan phản hồi `200`.

## 6. Giải thích trạng thái nhiều pod trong lúc rollout

Khi deployment thay đổi image hoặc cấu hình, Kubernetes thực hiện Rolling Update:

- Pod mới được tạo trước.
- Pod cũ vẫn phục vụ trong lúc pod mới chưa Ready.
- Khi pod mới vượt qua readiness probe, pod cũ mới chuyển sang `Terminating`.
- Nếu pod mới lỗi, pod cũ có thể được giữ lại nhằm giảm gián đoạn.

Vì vậy việc tạm thời thấy hai pod cùng service nhưng khác suffix/IP không nhất thiết là lỗi. Cần xem trạng thái `READY`, `RESTARTS`, `AGE` và deployment rollout để kết luận.

## 7. Các thay đổi cố ý chưa commit

Theo phân công của nhóm, các file sau được giữ nguyên trong working tree vì thuộc phần công việc của người khác:

- `k8s/deploy/deploy-yas-applications.sh`
- `k8s/deploy/deploy-yas-configuration.sh`
- `k8s/deploy/setup-cluster.sh`
- `k8s/deploy/setup-keycloak.sh`
- `k8s/deploy/setup-redis.sh`
- `k8s/deploy/postgres/postgresql/templates/postgresql.yaml`

Các file này không thuộc phạm vi 9 commit được mô tả trong báo cáo.

## 8. Lưu ý vận hành tiếp theo

- Các image mang tag `localfix` hiện được build/import trực tiếp trên `yas-server`; khi hoàn thiện CI/CD cần build và đẩy image có version vào registry.
- Cần thay các client secret đang nằm trong cấu hình dev bằng biến môi trường hoặc Secret nếu môi trường được chia sẻ rộng hơn.
- Sau khi push branch, nên chạy pipeline đầy đủ gồm Maven test, frontend build, Helm lint/template và kiểm tra manifest Kubernetes.
- Khi thay đổi hostname hoặc HTTP/HTTPS, phải cập nhật đồng bộ Ingress, Keycloak client redirect URI, BFF OAuth2 endpoints và frontend login URL.

## 9. Kết luận

Đợt xử lý đã đưa hệ thống từ trạng thái nhiều pod `CrashLoopBackOff`, đăng nhập OAuth2 không ổn định và route API thiếu sang trạng thái có thể khởi động, truy cập và kiểm thử các luồng chính. Những lỗi được sửa nằm ở cả source code, database migration, Spring Security/OAuth2, Kubernetes ingress, health probe và hệ thống quan sát.

Các thay đổi đã được chia thành 9 commit theo từng nguyên nhân để thuận tiện review, cherry-pick hoặc rollback. Những file do thành viên khác phụ trách được giữ ngoài commit nhằm tránh ghi đè công việc đang thực hiện.
