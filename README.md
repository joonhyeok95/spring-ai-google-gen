# spring-ai-google-gen
본 프로그램은 google gemini 무료 Api를 사용함  
임베딩 모델을 ollama 로 연동하였는데 local VRAM 한계(청크분리의 한계)로 gemini-embedd 로 변경함  
(gradle repository 활성화로 바로 이용할 수 있음)


Notebook PC Spec
- CPU: intel i7-1165G7 2.80GHz
- GPU: GTX 1650 Ti 4GB
- Memory: 40GB

Springboot: 3.5.11  
Spring Ai: 1.1.2  
openJDK: 17  
Gradle: 8.14.4  
Redis: Redis-x64-3.0.504  
VectorDB: Postgresql(16.13-1) Vector 0.8.2 (docker이미지활용)

데이터베이스 질의용 데이터: dvdrental 
- https://neon.com/postgresql/postgresql-getting-started/postgresql-sample-database

## 화면1:Agent Routing 챗봇 [DB|RAG|LLM]
- Agent Routing 을 활용하여 클라우드LLM, 데이터베이스질의, RAG 3가지 케이스를 자동으로 분류하여 답변한다.

## 화면2:Local AI RAG 스트리밍 챗봇
- 파일을 업로드하고 해당 파일을 분석할 수 있다.
