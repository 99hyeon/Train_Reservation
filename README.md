# 🚄 잡탕찌개
대규모 동시 트래픽 환경에서도 안정적인 **기차 좌석 예매**를 목표로 한 백엔드 프로젝트

<br>

## 🛠️ 기술 스택

| 분류      | 기술                                                       |
|-----------|------------------------------------------------------------|
| Language  | Java 21                                                    |
| Framework | Spring Boot 3.5.4, Spring Security, Spring Data JPA             |
| DB        | MySQL                                                      |
| Cache / In-Memory | Redis             |
| Auth      | OAuth2(Kakao / Naver), JWT (Refresh)                    |
| Build Tool       | Gradle                                            |
| Test | Junit5, Mockito, MockMvc             |
| Load Test | k6            |
| Version Control  | Git / GitHub                                      |
<!-- | Infra     | AWS EC2(Docker container: Spring Boot, MySQL), Nginx, AWS S3, CloudFront, Route53     |
| CI/CD     | GitHub Actions, Docker, Discord webhooks                 | -->

<br>

## 🧩 주요 기능(요약)

- OAuth2 소셜 로그인 + JWT Stateless 인증
- 출발/도착/날짜/시간 기반 기차 조회(정차역 순서 고려, 페이징)
- 구간 단위 좌석 가능 여부 계산 + Redis TTL 기반 좌석 홀드
- Redis 장바구니(홀드 TTL 연동)
- KakaoPay 결제 연동(Ready/Approve) + 결제 성공 시 티켓 발급


<br>

## Requirements
- Java 21
- MySQL
- Redis


<br>

## Docs
- Wiki: https://github.com/99hyeon/Japtangjjigae/wiki
- Swagger: (추가 예정)
- Swagger(Local): http://localhost:8080/swagger-ui/index.html
- 📁 프로젝트 구조 : https://github.com/99hyeon/Japtangjjigae/wiki
- 🧪 Load Test : k6 기반 시나리오 및 결과/개선 기록은 Wiki에 정리 (추가 예정)
- 커밋 컨벤션 : (추가 예정)
- 브랜치 전략 : (추가 예정)
- 에러 정책 : (추가 예정) 


<!-- 
## 🌐 배포 주소
공식 사이트 주소: https://frontend.beour.store/  <br>
서버: https://beour.store/ <br>
Swagger: https://beour.store/swagger-ui/index.html
-->

<br>
