# spring-ai-google-gen
본 프로그램은 google gemini 무료 Api를 사용함  
임베딩 모델을 ollama 로 연동하였는데 local VRAM 한계(청크분리의 한계)로 gemini-embedd 로 변경함  
(gradle repository 활성화로 바로 이용할 수 있음)

## 화면1:Agent Routing 챗봇 [DB|RAG|LLM]
- Agent Routing 을 활용하여 클라우드LLM, 데이터베이스질의, RAG 3가지 케이스를 자동으로 분류하여 답변한다.

## 화면2:Local AI RAG 스트리밍 챗봇
- 파일을 업로드하고 해당 파일을 분석할 수 있다.

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
- VectorDB: Postgresql(16.13-1) Vector 0.8.2 (docker이미지활용)

## 샘플 데이터: dvdrental 
- https://neon.com/postgresql/postgresql-getting-started/postgresql-sample-database
### 활용 질문 샘플
- 영화(film) 테이블에서 상영 시간이 180분 이상인 영화 제목 5개만 알려줘
- 우리 서비스에 등록된 전체 고객(customer)은 총 몇 명이야?
- 가장 많이 대여(rental)된 영화 제목 TOP 3가 뭐야?
- 각 영화 카테고리별로 총 매출(payment) 합계를 계산해줘
- 이메일 주소가 'P'로 시작하는 고객들의 이름과 성을 나열해줘.

