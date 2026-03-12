# spring-ai-google-gen
본 프로그램은 google gemini `무료` Api를 사용함  
임베딩 모델을 `ollama` 로 연동했다가 VRAM 한계로 `gemini-embedd` 로 변경함  
(gradle repository 활성화로 바로 이용할 수 있음)  
채팅 메모리는 `Redis`를 활용하며 브라우저 `localStorage`에 채팅고유번호를 저장함  

## Model(Free)
- Generate: gemini-3.1-flash-lite-preview
- Embedding
  - Google: gemini-embedding-001
  - Ollama: all-minilm

## 화면1:Agent Routing 챗봇 [DB|RAG|LLM]
- Agent Routing 을 활용하여 `LLM`, `데이터베이스질의`, `RAG문서검색` 3가지 케이스를 자동으로 분류하여 답변한다.
- http://localhost:8080/chat

## 화면2:Local AI RAG 스트리밍 챗봇
- 파일을 업로드하고 해당 파일을 분석할 수 있다.
- http://localhost:8080/rag

## Spec
### Notebook PC Spec
- CPU: intel i7-1165G7 2.80GHz
- GPU: GTX 1650 Ti 4GB
- Memory: 40GB

### Version
- Springboot: 3.5.11  
- Spring Ai: 1.1.2  
- openJDK: 17  
- Gradle: 8.14.4  
- Redis: Redis-x64-3.0.504  
- VectorDB: Postgresql(16.13-1) Vector 0.8.2

## VectorDB Setting
vector 확장 plugin 사용을 위해 초기 docker-compose up 후 필수로 실행해야함
```
-- db 접속
docker exec -it pgvector_db psql -U myuser -d aidb

-- DB 접속 후 실행
CREATE EXTENSION vector;

-- 설치 확인 (버전이 나오면 성공!)
SELECT * FROM pg_extension WHERE extname = 'vector';
```
임베딩 모델 변경될 경우 vector 차원 변경
```
-- 1. 기존 데이터 전부 삭제 (차원이 다르면 검색이 안 되므로 어차피 지워야 함)
DELETE FROM vector_store;
-- 2. 이제 타입 변경 가능!
ALTER TABLE vector_store ALTER COLUMN embedding TYPE vector(768);
```

## 샘플 데이터: dvdrental 
- https://neon.com/postgresql/postgresql-getting-started/postgresql-sample-database

### 활용 질문
- 영화(film) 테이블에서 상영 시간이 180분 이상인 영화 제목 5개만 알려줘
- 우리 서비스에 등록된 전체 고객(customer)은 총 몇 명이야?
- 가장 많이 대여(rental)된 영화 제목 TOP 3가 뭐야?
- 각 영화 카테고리별로 총 매출(payment) 합계를 계산해줘
- 이메일 주소가 'P'로 시작하는 고객들의 이름과 성을 나열해줘.

## 구현 화면
<img width="1026" height="907" alt="image" src="https://github.com/user-attachments/assets/acbc527c-2eca-40fa-b2b6-3e781c6e5bb0" />
