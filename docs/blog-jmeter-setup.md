# JMeter 설정

부하 테스트 계획서대로 다음과 같이 설정해주었다. 이것들 하나하나 넣는 것도 꽤나 힘들었는데, 이후는 숫자를 바꿔가며 "딸깍"만 하면되니 고생한 보람이 있다.

---

## 전체 구조

> 📸 **[스크린샷 필요]** JMeter 좌측 트리 전체 구조

Test Plan 하위에 다음 요소들을 배치했다:

- **CSV Data Set Config** - 테스트 유저 정보 (`users.csv`)
- **HTTP Cookie Manager** - 세션 쿠키 자동 관리
- **HTTP Request Defaults** - 공통 서버 URL 설정
- **Thread Group 1 (Main)** - S1~S5, S7 시나리오 (85%)
- **Thread Group 2 (SSE)** - S6 SSE 연결 유지 (15%)

---

## 사용자 변수 설정

> 📸 **[스크린샷 필요]** Test Plan 클릭 → User Defined Variables 영역

테스트 규모를 쉽게 조절할 수 있도록 변수로 분리했다:

| 변수명 | 설명 | Step 1 값 |
|--------|------|-----------|
| `BASE_URL` | 테스트 서버 도메인 | `your-ec2-domain.com` |
| `USERS` | 동시 접속 유저 수 | 5 |
| `RAMPUP` | 전체 유저 투입 시간(초) | 5 |
| `DURATION` | 테스트 지속 시간(초) | 180 |

부하 단계를 올릴 때는 이 값들만 변경하면 된다.

---

## 인증 플로우

Keep4Life는 **CSRF + JWT** 인증을 사용한다. **Once Only Controller**로 스레드당 한 번만 로그인하도록 구성했다.

> 📸 **[스크린샷 필요]** Once Only Controller 펼친 모습

```
1. GET /api/auth/csrf-token  → CSRF 토큰 추출
2. POST /api/auth/sign-in    → accessToken 추출
3. 이후 모든 요청에 Authorization 헤더 자동 포함
```

### CSRF 토큰 추출

> 📸 **[스크린샷 필요]** Regular Expression Extractor 설정 화면

| 항목 | 값 |
|------|-----|
| Reference Name | `csrfToken` |
| Regular Expression | `X-CSRF-TOKEN: (.+?)$` |
| Apply to | Response Headers |

### Access Token 추출

> 📸 **[스크린샷 필요]** JSON Extractor 설정 화면

| 항목 | 값 |
|------|-----|
| Variable Name | `accessToken` |
| JSON Path | `$.accessToken` |

---

## 시나리오 비율 조절

실제 사용 패턴을 반영해 **읽기 80%, 쓰기 20%** 비율로 Throughput Controller를 설정했다.

> 📸 **[스크린샷 필요]** Throughput Controller 설정 화면 (Percent Executions 선택된 모습)

| 시나리오 | 비율 | 설명 |
|----------|------|------|
| S1. 공개 피드 조회 | 25% | 비로그인 탐색 |
| S2. 카테고리+기록 조회 | 25% | 개인 데이터 조회 |
| S3. 기록 생성 | 10% | 체크 기록 추가 |
| S4. 좋아요 토글 | 10% | 좋아요 추가/취소 |
| S5. 댓글 작성+조회 | 10% | 소셜 기능 |
| S6. SSE 연결 | 15% | 실시간 알림 (별도 Thread Group) |
| S7. 채팅 메시지 | 5% | 채팅 기능 |

---

## SSE 연결 (별도 Thread Group)

SSE는 장시간 연결을 유지하므로 **별도의 Thread Group**으로 분리했다.

> 📸 **[스크린샷 필요]** SSE Thread Group 설정 화면

- Loop Count: 1 (연결 한 번만)
- Response Timeout: 300초 (5분간 연결 유지)
- Thread 수: 메인의 15%

---

## Think Time 설정

실제 사용자처럼 자연스러운 부하를 주기 위해 **Uniform Random Timer**를 추가했다.

> 📸 **[스크린샷 필요]** Uniform Random Timer 설정 화면

- 공개 피드 조회 후: 2~5초 대기
- 카테고리 조회 후: 1~3초 대기

---

## 결과 수집 (Listeners)

> 📸 **[스크린샷 필요]** Summary Report 또는 View Results Tree

- **View Results Tree** - 개별 요청 디버깅용
- **Summary Report** - 전체 통계 확인용

---

## 부하 단계별 설정값

| 단계 | USERS | RAMPUP | DURATION | 목적 |
|------|-------|--------|----------|------|
| Step 1 | 5 | 5초 | 3분 | Baseline |
| Step 2 | 15 | 15초 | 5분 | Warm-up |
| Step 3 | 30 | 30초 | 5분 | 중간 부하 |
| Step 4 | 50 | 60초 | 5분 | 고부하 |
| Step 5 | 100 | 120초 | 5분 | Saturation Point 탐색 |

User Defined Variables에서 숫자만 변경하고 ▶️ 실행하면 끝!
