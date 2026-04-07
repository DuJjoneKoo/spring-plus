# 🌱 Spring Plus

> Spring Boot 기반 일정 관리 API 서버  
> JWT 인증, Spring Security, JPA, QueryDSL을 활용한 백엔드 프로젝트

---

## 📌 목차

- [프로젝트 소개](#-프로젝트-소개)
- [기술 스택](#-기술-스택)
- [구현 기능](#-구현-기능)
- [API 명세](#-api-명세)
- [트러블슈팅](#-트러블슈팅)

---

## 🙋 프로젝트 소개

Spring Plus는 할 일(Todo) 관리 서비스의 백엔드 API 서버입니다.  
JWT 기반 인증, Spring Security를 통한 인가 처리, JPA와 QueryDSL을 활용한 데이터 조회 기능을 구현했습니다.

---

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.3 |
| ORM | Spring Data JPA |
| Query | JPQL, QueryDSL 5.0.0 |
| Security | Spring Security, JWT (jjwt 0.11.5) |
| DB | MySQL, H2 (테스트) |
| Build | Gradle |
| Etc | Lombok, BCrypt |

---

## ✅ 구현 기능

### Level 1

#### 1. @Transactional 이해
- `TodoService.saveTodo()` 메소드에 `@Transactional` 추가
- 클래스 레벨 `readOnly = true` 환경에서 쓰기 메소드에 별도 트랜잭션 설정

#### 2. JWT에 nickname 추가
- `User` 엔티티에 `nickname` 컬럼 추가 (중복 허용)
- `JwtUtil.createToken()`에 `nickname` claim 포함
- 회원가입/로그인 시 JWT에 nickname 반영

#### 3. JPA 동적 쿼리 (JPQL)
- 할 일 검색 시 `weather` 조건 추가 (선택적)
- 수정일(`modifiedAt`) 기준 기간 검색 추가 (선택적)
- `:param IS NULL OR` 패턴으로 선택적 조건 처리

#### 4. 컨트롤러 테스트 수정
- `InvalidRequestException` 발생 시 `400 Bad Request` 반환
- 테스트 코드 `status().isOk()` → `status().isBadRequest()` 수정

#### 5. AOP 수정
- `@After` → `@Before` 변경 (메소드 실행 전 로깅)
- 포인트컷 대상 `UserController.getUser()` → `UserAdminController.changeUserRole()` 수정

---

### Level 2

#### 6. JPA Cascade
- `Todo`와 `Manager`의 `@OneToMany`에 `CascadeType.ALL` 설정
- 할 일 저장 시 생성자가 담당자로 자동 등록

#### 7. N+1 문제 해결
- `CommentRepository.findByTodoIdWithUser()`에서 `JOIN` → `JOIN FETCH` 변경
- Comment 조회 시 User를 한 번의 쿼리로 함께 조회

#### 8. QueryDSL 적용
- `TodoRepository`의 `findByIdWithUser()` JPQL → QueryDSL로 변경
- `TodoRepositoryCustom` 인터페이스 + `TodoRepositoryImpl` 구현체 패턴 적용
- `JPAQueryFactory` Bean 등록 (`QueryDslConfig`)

#### 9. Spring Security 적용
- 기존 `JwtFilter` + `FilterConfig` 제거
- `JwtSecurityFilter` (`OncePerRequestFilter`) 구현
- `SecurityConfig`에서 URL별 접근 권한 설정
- `AuthUserArgumentResolver`를 `SecurityContextHolder` 기반으로 변경

---

## 📡 API 명세

### Auth

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/auth/signup` | 회원가입 | ❌ |
| POST | `/auth/signin` | 로그인 | ❌ |

### Todo

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/todos` | 할 일 생성 | ✅ |
| GET | `/todos` | 할 일 목록 조회 (조건 검색) | ✅ |
| GET | `/todos/{todoId}` | 할 일 단건 조회 | ✅ |

**GET /todos 쿼리 파라미터**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| page | int | ❌ | 페이지 번호 (기본값: 1) |
| size | int | ❌ | 페이지 크기 (기본값: 10) |
| weather | String | ❌ | 날씨 조건 필터 |
| startDate | LocalDateTime | ❌ | 수정일 기준 시작 날짜 |
| endDate | LocalDateTime | ❌ | 수정일 기준 종료 날짜 |

### Comment

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/todos/{todoId}/comments` | 댓글 작성 | ✅ |
| GET | `/todos/{todoId}/comments` | 댓글 목록 조회 | ✅ |

### Manager

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/todos/{todoId}/managers` | 담당자 등록 | ✅ |
| GET | `/todos/{todoId}/managers` | 담당자 목록 조회 | ✅ |
| DELETE | `/todos/{todoId}/managers/{managerId}` | 담당자 삭제 | ✅ |

### User

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| GET | `/users/{userId}` | 유저 조회 | ✅ |
| PUT | `/users` | 비밀번호 변경 | ✅ |

### Admin

| Method | URL | 설명 | 인증 | 권한 |
|--------|-----|------|------|------|
| PATCH | `/admin/users/{userId}` | 유저 권한 변경 | ✅ | ADMIN |

---

## 🔥 트러블슈팅

### 1. @Transactional readOnly 에러

**문제**
```
Connection is read-only. Queries leading to data modification are not allowed
```

**원인**  
클래스 레벨 `@Transactional(readOnly = true)` 설정으로 인해 `saveTodo()` 메소드가 읽기 전용 트랜잭션으로 실행됨

**해결**  
쓰기가 필요한 메소드에 `@Transactional` 별도 추가 → 메소드 레벨이 클래스 레벨보다 우선순위 높음

---

### 2. N+1 문제

**문제**  
`getComments()` 호출 시 Comment 수만큼 User 조회 쿼리가 추가 실행됨

**원인**  
`JOIN`만 사용하여 Comment 조회 후 `comment.getUser()` 접근 시마다 별도 쿼리 실행

**해결**  
`JOIN` → `JOIN FETCH` 변경으로 Comment와 User를 한 번의 쿼리로 함께 조회

```java
// Before
@Query("SELECT c FROM Comment c JOIN c.user WHERE c.todo.id = :todoId")

// After
@Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
```

---

### 3. QueryDSL Q클래스 생성

**문제**  
`QTodo`, `QUser` 등 Q클래스를 찾을 수 없음

**해결**  
Gradle `compileJava` 태스크 실행으로 `build/generated` 경로에 Q클래스 자동 생성

```gradle
implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
```

---

### 4. Spring Security 전환 후 인증 정보 처리

**문제**  
기존 `request.getAttribute()`로 인증 정보를 꺼내던 방식이 Spring Security 도입 후 동작하지 않음

**해결**  
`JwtSecurityFilter`에서 `SecurityContextHolder`에 `Authentication` 저장  
`AuthUserArgumentResolver`에서 `SecurityContextHolder.getContext().getAuthentication()`으로 꺼내도록 변경

```java
// Before
Long userId = (Long) request.getAttribute("userId");

// After
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
AuthUser authUser = (AuthUser) authentication.getPrincipal();
```
