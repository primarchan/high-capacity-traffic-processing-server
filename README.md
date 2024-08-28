# 대용량 트래픽 처리 서버 프로젝트

## OBJECTIVE
- 토이 프로젝트 - 초기 스타트업 - 소규모 서비스 - 중규모 서비스 - 대규모 서비스/플랫폼 순으로 서비스 단계에 따라 적재적소에 DB 활용 능력을 키워줄 실전 프로젝트 진행
- 서버가 다운될 수 있는 이슈 5가지에 따라 DB 를 선택하고 추가하여 한 단계 한 단계 Level-Up 하며 문제 해결
- DB 자체를 튜닝하거나 다룰 때 자주 실수하거나 헷갈려하는 부분에 대해 Sharding 등 개념을 적용한 실습을 통해서 문제 해결
- 데이터 특성과 트래픽에 따라 DB 를 알맞게 선택할 수 있고, 이에 따라 대용량 데이터 처리 능력 향상

## TECH-STACK
- Java 17
- Spring Boot 3.3.3
- Spring Web
- Spring Data JPA
- MySQL 8.0.3
- MongoDB
- Docker
- ElasticSearch
- Redis
- RabbitMQ
- Kafka
- Lombok
- Gradle
- IntelliJ IDEA 2024.1.4 (Ultimate Edition)
- macOS Sonoma 14.6.1

## LEVEL 1. 초기 프로젝트 세팅

## LEVEL 2. 동시접속자 1,000명 부하를 견디기 위한 ElasticSearch 적용
- ElasticSearch
  - Apache Lucene
    - Java 로 작성된 고성능 Full-Text 검색 라이브러리
    - 문서를 분석하여 Index(색인) 을 생성
    - 텍스트는 토큰으로 분할하고 각 토큰의 위치와 빈도를 기록하여 검색을 효율적으로 만듦
    - 검색을 관련성(유사도) 점수로 정렬하여 관련성이 높은 문서를 상위에 표시
  - ElasticSearch
    - Lucene 을 기반으로 구축된 분산 검색 엔진
    - JSON 기반의 데이터를 처리해서 즉시 검색 가능하도록 제공
    - 다양한 집계 기능(Aggregation) 제공
- ELK Stack
  - ElasticSearch
    - 역할 : 분산 검색 및 분석 엔진
    - 기능 : 대량의 구조화된 및 비구조화된 데이터를 실시간으로 저장, 검색, 분석
  - Logstash
    - 역할 : 데이터 수집 및 처리 파이프라인
    - 기능 : 다양한 소스(서버 로그, 애플리케이션 로그 등)로부터 데이터를 수집, 변환, 전송, 데이터를 ElasticSearch 에 저장할 수 있도록 처리
  - Kibana
    - 역할 : 데이터 시각화 및 대시보드 도구
    - 기능 : ElasticSearch 에 저장된 데이터를 기반으로 대시보드, 차트, 지도 등으로 생성하여 시각화, 실시간으로 데이터 분석과 모니터링이 가능

## LEVEL 3. 동시접속자 10,000명에게 컨텐츠를 0.1초로 보여주기 위한 Redis 적용

## LEVEL 4. 전체 유저 100,000명의 광고 집계를 위한 MongoDB 적용

## LEVEL 5. 전체 유저 100,000명에게 대량 Push 알림을 보내기 위한 RabbitMQ, Kafka 적용

## LEVEL 6. 대규모 트래픽에도 견고한 서버를 위한 DevOps 가이드