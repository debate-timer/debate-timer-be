# Debate Timer Backend

## 프로젝트 소개

디베이트 타이머는 더 쉬운 토론 진행을 위한, 오직 토론을 위한 타이머입니다.

## 기술 스택

### Backend

<div>
<img src="https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white">
<img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
<img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
</div>

### Database

<div>
<img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
<img src="https://img.shields.io/badge/H2-003545?style=for-the-badge&logo=h2&logoColor=white">
<img src="https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white">
</div>

### DevOps & Monitoring

<div>
<img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white">
<img src="https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white">
<img src="https://img.shields.io/badge/Datadog-632CA6?style=for-the-badge&logo=datadog&logoColor=white">
</div>

### Documentation & Testing

<div>
<img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black">
<img src="https://img.shields.io/badge/Spring_REST_Docs-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
<img src="https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white">
<img src="https://img.shields.io/badge/Mockito-01FF70?style=for-the-badge">
</div>

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/debatetimer/
│   │   ├── aop/               # AOP 및 로깅 관련
│   │   ├── client/            # 외부 API 클라이언트
│   │   ├── config/            # 설정 클래스
│   │   ├── controller/        # REST API 컨트롤러
│   │   │   ├── auth/          # 인증 관련
│   │   │   ├── customize/     # 토론 테이블 커스터마이징
│   │   │   ├── member/        # 회원 관리
│   │   │   └── poll/          # 투표 관리
│   │   ├── domain/            # 도메인 모델
│   │   ├── domainrepository/  # 도메인 레포지토리
│   │   ├── dto/               # 데이터 전송 객체
│   │   ├── entity/            # JPA 엔티티
│   │   ├── exception/         # 예외 처리
│   │   ├── repository/        # JPA 레포지토리
│   │   └── service/           # 비즈니스 로직
│   └── resources/
│       ├── application*.yml   # 환경별 설정
│       ├── logging/           # 로깅 설정
│       └── db/migration/      # Flyway 마이그레이션
└── test/                      # 테스트 코드
```

## Infra & Deployment

![img.png](./docs/debate_timer_infra_v0-1.png)

## BE 팀원 소개

| <img src="https://avatars.githubusercontent.com/u/148152234?v=4" width="100" height="100"/> | <img src="https://avatars.githubusercontent.com/u/44027393?v=4" width="100" height="100"/> | <img src="https://avatars.githubusercontent.com/u/121424793?v=4" width="100" height="100"/> |
|:-------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------:|
|                            [콜리](https://github.com/coli-geonwoo)                            |                            [커찬](https://github.com/leegwichan)                             |                             [비토](https://github.com/unifolio0)                              |
