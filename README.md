# 🚄 잡탕찌개
대규모 동시 트래픽 환경에서도 안정적인 기차 좌석 예매를 목표로 한 백엔드 프로젝트

<br>

## 🛠️ 기술 스택

| 분류      | 기술                                                       |
|-----------|------------------------------------------------------------|
| Language  | Java 21                                                    |
| Framework | Spring Boot 3.5.4, Spring Security, Spring Data JPA             |
| DB        | MySQL                                                      |
| ORM | Spring Data JPA             |
| Auth      | OAuth2(Kakao / Naver), JWT (Access/Refresh)                    |
| Cache / In-Memory | Redis             |
| Build Tool       | Gradle                                            |
<!-- | Infra     | AWS EC2(Docker container: Spring Boot, MySQL), Nginx, AWS S3, CloudFront, Route53     |
| CI/CD     | GitHub Actions, Docker, Discord webhooks                 | -->
| Test | Junit5, Mockito, MockMvc             |
| Load Test | k6            |
| Version Control  | Git / GitHub                                      |

<br>

## 🧩 주요 기능

#### 🔸 인증/인가
- OAuth2 기반 소셜 로그인 (Kakao / Naver)
- JWT 기반 인증(Stateless)

#### 🔸 기차 / 노선 조회
- 출발역 / 도착역 / 날짜 / 시간 기반 기차 조회
- 정차역 순서를 고려한 유효 노선 필터링
- 페이징 기반 기차 조회 API

#### 🔸 좌석 조회
- 객차 → 좌석 구조 기반 조회
- 구간 단위 좌석 가능 여부 계산

#### 🔸 좌석 홀드
- Redis 기반 좌석 홀드 구현
- 구간 겹침 여부 고려한 좌석 중복 방지
- 홀드 만료 시 자동 해제

#### 🔸 장바구니
- Redis 기반 장바구니 관리
- 좌석 담기 / 조회 / 제거
- 좌석 홀드 TTL과 연동된 장바구니 만료 처리

#### 🔸 결제
- KakaoPay 결제 Ready / Approve API 연동
- 결제 상태 관리
- 결제 성공 시 좌석 확정 및 티켓 발급

<br>

## 📁 프로젝트 구조(수정 중)
>자세한 폴더별 설명은 [Wiki - 폴더 구조](https://github.com/99hyeon/Japtangjjigae/wiki) 를 참고해주세요.
```text
📦 src
 ┣ 📂main
 ┃ ┣ 📂java/com.example.japtangjjigae
 ┃ ┃ ┣ 📂cart          # 장바구니 도메인 (좌석 담기/조회 등)
 ┃ ┃ ┣ 📂config        # 전역 설정 (Swagger, Security 등)
 ┃ ┃ ┣ 📂exception     # 예외/에러 처리
 ┃ ┃ ┣ 📂global        # 공통 모듈 (BaseEntity, 초기 데이터 세팅 등)
 ┃ ┃ ┣ 📂jwt           # JWT 발급/검증 및 토큰 관련 필터/유틸 로직
 ┃ ┃ ┣ 📂kakaopay      # 카카오페이 결제 연동
 ┃ ┃ ┣ 📂oauth2        # oauth2 로그인 처리
 ┃ ┃ ┣ 📂order         # 주문 도메인 
 ┃ ┃ ┣ 📂redis         # Redis Store 모음
 ┃ ┃ ┣ 📂station       # 역(Station) 도메인
 ┃ ┃ ┣ 📂ticket        # 기차 티켓 도메인
 ┃ ┃ ┣ 📂train         # 기차/운행 도메인
 ┃ ┃ ┗ 📂user          # 사용자 도메인
 ┃ ┃  
 ┃ ┣ 📂resources
 ┃ ┃ ┣ 📜application.yml         # 기본 설정
 ┃ ┃ ┣ 📜application-secret.yml  # 민감 정보 설정
 ┃ ┃ ┗ 📜application-test.yml    # 테스트 환경 설정
 ┣ 📂test
 ┃ ┗ 📂...             # 각 도메인별 테스트 코드

```

<br>

<!--해당 url 수정시 변경-->
## 📄 API 명세
👉 [Wiki에서 확인하기](https://github.com/99hyeon/Japtangjjigae/wiki)


<br>


## 📝 커밋 컨벤션
👉 [Wiki에서 확인하기](https://github.com/99hyeon/Japtangjjigae/wiki)

<br>

<!-- 
## 🌐 배포 주소
공식 사이트 주소: https://frontend.beour.store/  <br>
서버: https://beour.store/ <br>
Swagger: https://beour.store/swagger-ui/index.html
-->

<br>
