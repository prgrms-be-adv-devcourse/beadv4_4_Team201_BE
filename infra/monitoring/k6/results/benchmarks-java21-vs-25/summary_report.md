# Java 21 vs Java 25 성능 벤치마크 비교 분석 리포트

**분석 일자:** 2026-03-10  
**프로젝트:** Giftify Backend  
**분석 범위:** k6 부하 테스트, Prometheus 메트릭, Native Memory Tracking, JOL

---

## 📊 Executive Summary

Java 25로 업그레이드 시 **고부하 환경에서 극적인 성능 개선**을 보였으나, **저부하 환경에서는 일부 저하** 현상 발생.

### 핵심 요약

| 항목 | Java 21 | Java 25 | 변화 | 평가 |
|------|---------|---------|------|------|
| **1000 VU 평균 응답시간** | 144.8 ms | 90.5 ms | **-37.5%** | ✅ 대폭 개선 |
| **Stress 테스트 p99** | 157.3 ms | 26.2 ms | **-83.4%** | ✅ 대폭 개선 |
| **GC 총 시간** | 0.530s | 0.084s | **-84.2%** | ✅ 대폭 개선 |
| **GC 발생 횟수** | 22회 | 4회 | **-81.8%** | ✅ 대폭 개선 |
| **Native Memory Committed** | 856.1 MB | 691.1 MB | **-19.3%** | ✅ 개선 |
| **Thread Stack Memory** | 153.1 MB | 2.1 MB | **-98.6%** | ✅ 대폭 개선 |
| **Smoke 테스트 평균** | 19.7 ms | 22.7 ms | **+15.1%** | ⚠️ 저하 |
| **Load 테스트 평균** | 8.5 ms | 11.9 ms | **+39.6%** | ⚠️ 저하 |

---

## 🚀 주요 개선 사항

### 1. 고부하 시나리오에서 압도적 성능 향상

**1000 VU (가상 사용자 1000명) 테스트:**
- 평균 응답시간: 144.8ms → 90.5ms (**37.5% 개선**)
- p95 응답시간: 394.8ms → 274.9ms (30.4% 개선)
- p99 응답시간: 637.0ms → 416.7ms (34.6% 개선)
- 총 처리량: 255,891 → 345,204 요청 (**34.9% 증가**)

**Stress 테스트 (100 VU, 점진적 부하 증가):**
- 평균 응답시간: 13.3ms → 6.5ms (51.4% 개선)
- p95 응답시간: 36.6ms → 15.6ms (57.3% 개선)
- p99 응답시간: 157.3ms → 26.2ms (**83.4% 개선** - 가장 극적인 개선)

**Spike 테스트 (급격한 부하 증가):**
- 평균 응답시간: 9.4ms → 6.7ms (28.8% 개선)
- p95 응답시간: 29.8ms → 15.7ms (47.2% 개선)
- p99 응답시간: 80.8ms → 25.8ms (68.0% 개선)

### 2. GC 성능 대폭 개선

| 지표 | Java 21 | Java 25 | 개선율 |
|------|---------|---------|--------|
| 총 GC 횟수 | 22회 | 4회 | **-81.8%** |
| 총 GC 시간 | 0.530s | 0.084s | **-84.2%** |
| CodeCache GC 횟수 | 6회 | 2회 | -66.7% |
| G1 Evacuation 횟수 | 16회 | 2회 | -87.5% |

**해석:**
- Java 25의 G1 GC 최적화가 매우 효과적으로 작동
- GC로 인한 애플리케이션 중단(pause) 시간이 극적으로 감소
- 고부하 상황에서 더 안정적인 latency 유지 가능

### 3. Native Memory 효율성 향상

| 카테고리 | Java 21 | Java 25 | 변화 | 평가 |
|----------|---------|---------|------|------|
| **Total Committed** | 856.1 MB | 691.1 MB | **-19.3%** ✅ | 전체 메모리 효율 향상 |
| **Thread Stack** | 153.1 MB | 2.1 MB | **-98.6%** ✅ | Virtual Threads 효과 |
| **Symbol Table** | 50.0 MB | 30.9 MB | **-38.2%** ✅ | Symbol 관리 최적화 |
| Code Cache | 23.3 MB | 32.4 MB | +39.1% ⚠️ | JIT 컴파일 증가 |
| Metaspace | 109.3 MB | 119.6 MB | +9.4% ⚠️ | 클래스 메타데이터 증가 |

**핵심 통찰:**
- **Thread Stack 98.6% 감소**: Virtual Threads 도입으로 스레드당 메모리 오버헤드 극적 감소
- **Symbol Table 38.2% 감소**: String interning 및 Symbol 관리 최적화
- **Total Committed 19.3% 감소**: 전체 네이티브 메모리 footprint 개선

---

## ⚠️ 주의 사항 및 저하 영역

### 1. 저부하 시나리오에서 성능 저하

**Smoke 테스트 (10 VU, 기본 헬스체크):**
- 평균 응답시간: 19.7ms → 22.7ms (**+15.1% 저하**)
- p99 응답시간: 42.0ms → 47.1ms (+12.2% 저하)

**Load 테스트 (20 VU, 일반 부하):**
- 평균 응답시간: 8.5ms → 11.9ms (**+39.6% 저하**)
- p99 응답시간: 76.6ms → 28.2ms (개선됨, 이상치 제거 효과)

**원인 분석:**
1. **JIT Warm-up 시간 증가**: Java 25의 새로운 JIT 최적화가 초기에는 오버헤드 발생
2. **Virtual Threads 스케줄링 오버헤드**: 저부하에서는 Platform Threads가 더 효율적
3. **GC Tuning 부족**: 기본 설정이 고부하 환경에 최적화되어 있음

### 2. Heap 메모리 사용량 증가

| 영역 | Java 21 | Java 25 | 변화 |
|------|---------|---------|------|
| Eden | 4.0 MB | 204.0 MB | **+5000%** ⚠️ |
| Old Gen | 80.1 MB | 114.4 MB | +42.8% |
| **Total Heap** | **95.4 MB** | **328.0 MB** | **+243.8%** ⚠️ |

**해석:**
- Eden 영역의 급격한 증가는 측정 시점의 차이일 가능성 높음 (GC 직후 vs GC 직전)
- Old Gen 42.8% 증가는 실제 장기 객체 증가로 보임
- 전체 Heap 사용량 증가에도 불구하고 GC 성능은 크게 개선됨

### 3. CompactObjectHeaders 미활성화

```
Object Header: Java 21과 Java 25 모두 12 bytes (8 + 4)
```

**현재 상태:**
- CompactObjectHeaders 기능이 **비활성화 상태**로 측정됨
- 활성화 시 추가 메모리 절약 가능 (약 10-15%)

**활성화 방법:**
```bash
-XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders
```

---

## 📈 상세 벤치마크 결과

### k6 부하 테스트 결과

#### Smoke Test (10 VU)
| 지표 | Java 21 | Java 25 | 변화율 | 평가 |
|------|---------|---------|--------|------|
| 평균 응답시간 | 19.73 ms | 22.70 ms | +15.1% | ⚠️ 저하 |
| p95 응답시간 | 41.49 ms | 39.71 ms | -4.3% | → 유사 |
| p99 응답시간 | 41.97 ms | 47.08 ms | +12.2% | ⚠️ 저하 |

#### Load Test (20 VU)
| 지표 | Java 21 | Java 25 | 변화율 | 평가 |
|------|---------|---------|--------|------|
| 평균 응답시간 | 8.51 ms | 11.88 ms | +39.6% | ⚠️ 저하 |
| p95 응답시간 | 22.71 ms | 23.43 ms | +3.2% | → 유사 |
| p99 응답시간 | 76.60 ms | 28.15 ms | -63.3% | ✅ 개선 |

#### API Benchmark Test (10 VU, 4 endpoints)
| 지표 | Java 21 | Java 25 | 변화율 | 평가 |
|------|---------|---------|--------|------|
| 평균 응답시간 | 6.92 ms | 6.67 ms | -3.6% | → 유사 |
| p95 응답시간 | 23.44 ms | 14.42 ms | -38.5% | ✅ 개선 |
| p99 응답시간 | 56.61 ms | 19.79 ms | -65.0% | ✅ 개선 |

#### Stress Test (100 VU, 점진적 증가)
| 지표 | Java 21 | Java 25 | 변화율 | 평가 |
|------|---------|---------|--------|------|
| 평균 응답시간 | 13.33 ms | 6.48 ms | -51.4% | ✅ 개선 |
| p95 응답시간 | 36.62 ms | 15.63 ms | -57.3% | ✅ 개선 |
| p99 응답시간 | 157.31 ms | 26.15 ms | **-83.4%** | ✅ 대폭 개선 |

#### Spike Test (100 VU, 급격한 증가)
| 지표 | Java 21 | Java 25 | 변화율 | 평가 |
|------|---------|---------|--------|------|
| 평균 응답시간 | 9.36 ms | 6.66 ms | -28.8% | ✅ 개선 |
| p95 응답시간 | 29.80 ms | 15.73 ms | -47.2% | ✅ 개선 |
| p99 응답시간 | 80.84 ms | 25.83 ms | -68.0% | ✅ 개선 |

#### 100 VU Sustained Load
| 지표 | Java 21 | Java 25 | 변화율 | 평가 |
|------|---------|---------|--------|------|
| 평균 응답시간 | 1.80 ms | 1.28 ms | -28.9% | ✅ 개선 |
| p95 응답시간 | 3.90 ms | 3.15 ms | -19.2% | ✅ 개선 |
| p99 응답시간 | 16.72 ms | 8.14 ms | -51.3% | ✅ 개선 |
| 총 요청 수 | 81,105 | 82,017 | +1.1% | → 유사 |

#### 1000 VU Extreme Load
| 지표 | Java 21 | Java 25 | 변화율 | 평가 |
|------|---------|---------|--------|------|
| 평균 응답시간 | 144.83 ms | 90.48 ms | **-37.5%** | ✅ 대폭 개선 |
| p95 응답시간 | 394.77 ms | 274.94 ms | -30.4% | ✅ 개선 |
| p99 응답시간 | 637.00 ms | 416.70 ms | -34.6% | ✅ 개선 |
| 총 요청 수 | 255,891 | 345,204 | **+34.9%** | ✅ 대폭 개선 |

---

## 🎯 권장 사항

### 1. 즉시 적용 가능 (High Priority)

#### ✅ Production 환경 Java 25 업그레이드 권장
- **근거:** 고부하 시나리오에서 37-83% 성능 개선
- **리스크:** 저부하 시 15-40% 저하, 하지만 실제 서비스는 중고부하가 일반적
- **조치:** Blue-Green 배포로 안전하게 전환

#### ✅ GC Tuning 최적화
```bash
# 고부하 환경 최적화 (현재 설정 유지)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200

# 저부하 환경용 추가 옵션 (개발/테스트 환경)
-XX:+UseG1GC
-XX:MaxGCPauseMillis=100
-XX:InitiatingHeapOccupancyPercent=30
```

#### ⚠️ Heap 메모리 모니터링 강화
- 현재: 총 Heap 사용량 243.8% 증가 관찰
- 조치: Prometheus/Grafana 알람 설정 강화
- 임계값: Heap 사용률 80% 경고, 90% 위험

### 2. 실험적 기능 검증 (Medium Priority)

#### 🧪 CompactObjectHeaders 성능 테스트
```bash
# 스테이징 환경에서 테스트
-XX:+UnlockExperimentalVMOptions -XX:+UseCompactObjectHeaders
```

**예상 효과:**
- 추가 메모리 절약: 10-15% (객체 헤더 크기 감소)
- 리스크: 실험적 기능이므로 안정성 검증 필요

#### 🧪 Virtual Threads 명시적 활용
```java
// Platform Threads 대신 Virtual Threads 사용
Executors.newVirtualThreadPerTaskExecutor()
```

**현재 상태:**
- Thread Stack 메모리 98.6% 감소 확인 → Virtual Threads 이미 활용 중
- 추가 최적화: 비동기 처리 로직에 Virtual Threads 명시적 적용

### 3. 장기 최적화 (Low Priority)

#### 📊 부하 패턴별 JVM 프로파일 분리
```yaml
# 고부하 환경 (Production)
java_opts: "-Xms2G -Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 저부하 환경 (Internal API)
java_opts: "-Xms512M -Xmx1G -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
```

#### 📊 Symbol Table 사이즈 튜닝
- 현재: Symbol Table 38.2% 감소 확인
- 추가 옵션: `-XX:StringTableSize=xxxxx` 조정하여 추가 최적화 가능

---

## 📌 핵심 통찰 (Key Insights)

### ✅ Java 25의 강점

1. **고부하 처리 능력 대폭 향상**
   - 1000 VU에서 37.5% 응답시간 단축, 34.9% 처리량 증가
   - Stress test p99에서 83.4% 개선 → tail latency 문제 해결

2. **GC 효율성 극대화**
   - GC 발생 횟수 81.8% 감소
   - GC 총 시간 84.2% 감소
   - 안정적인 latency 보장

3. **메모리 효율성 개선**
   - Native Memory Committed 19.3% 감소
   - Thread Stack 메모리 98.6% 감소 (Virtual Threads)
   - Symbol Table 38.2% 감소

### ⚠️ Java 25의 약점

1. **저부하 환경 성능 저하**
   - Smoke test: 평균 15.1% 저하
   - Load test: 평균 39.6% 저하
   - 원인: JIT warm-up 오버헤드, Virtual Threads 스케줄링 비용

2. **Heap 메모리 사용량 증가**
   - 총 Heap 243.8% 증가 (95.4MB → 328.0MB)
   - Old Gen 42.8% 증가
   - 장기 운영 시 메모리 압박 가능성

3. **Code Cache 사용량 증가**
   - 39.1% 증가 (23.3MB → 32.4MB)
   - JIT 컴파일 증가로 인한 메모리 trade-off

### 🎯 최적 사용 시나리오

| 환경 | Java 21 | Java 25 | 권장 |
|------|---------|---------|------|
| **Production (고부하)** | ❌ | ✅ | **Java 25** |
| **Internal API (저부하)** | ✅ | ⚠️ | **Java 21** 또는 GC 튜닝 후 Java 25 |
| **Batch Job** | → | ✅ | **Java 25** (GC 개선 효과) |
| **개발 환경** | ✅ | → | **Java 21** (빠른 시작) |

---

## 📝 측정 방법론

### 테스트 환경
- **Load Generator:** k6 (Docker 컨테이너)
- **Monitoring:** Prometheus + Grafana
- **JVM Profiling:** Native Memory Tracking (NMT), JOL
- **애플리케이션:** Giftify Backend (Spring Boot 3)

### k6 테스트 시나리오
1. **Smoke Test:** 10 VU, 1분, health check
2. **Load Test:** 20 VU, 5분, 일반 부하
3. **API Benchmark:** 10 VU, 4 endpoints 순차 호출
4. **Stress Test:** 0 → 100 VU 점진적 증가, 10분
5. **Spike Test:** 10 → 100 VU 급격한 증가, 5분
6. **100 VU:** 100 VU 유지, 5분
7. **1000 VU:** 1000 VU 유지, 10분

### 메트릭 수집
- **HTTP Metrics:** avg, p50, p95, p99, max 응답시간, 요청 수, 실패율
- **JVM Metrics:** Heap, Non-heap, GC pause time/count
- **Native Memory:** Total reserved/committed, 카테고리별 사용량

---

## 🔍 추가 조사 필요 항목

1. **Eden 영역 급증 원인 규명**
   - Java 21: 4.0MB → Java 25: 204.0MB (5000% 증가)
   - 측정 시점 차이 vs 실제 할당 증가 여부 재검증 필요

2. **저부하 성능 저하 심층 분석**
   - Flame Graph로 CPU 프로파일링
   - JIT 로그 분석으로 warm-up 패턴 확인

3. **장기 운영 안정성 테스트**
   - 24시간 soak test로 메모리 누수 여부 확인
   - Old Gen 증가 추세 모니터링

---

## 📚 참고 자료

- [JEP 450: Compact Object Headers (Experimental)](https://openjdk.org/jeps/450)
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [G1 GC Tuning Guide](https://docs.oracle.com/en/java/javase/21/gctuning/)
- k6 Load Testing Documentation
- Spring Boot 3 Performance Best Practices

---

**작성:** Claude Code (Scientist Agent)  
**검토 필요:** 인프라팀, 백엔드팀

