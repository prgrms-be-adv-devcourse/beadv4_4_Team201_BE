import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = { // options → 테스트 설정 (VU 수, 지속 시간)
    vus: 10,          // 가상 사용자 10명
    duration: '30s',  // 30초간 실행
};

export default function smokeTest() { // default function → 각 VU가 반복 실행하는 시나리오
    const res = http.get('http://host.docker.internal:8080/actuator/health'); // host.docker.internal → Docker 컨테이너에서 호스트 머신 접근용 특수 DNS

    check(res, {  // check() → 응답 검증 (실패해도 테스트는 계속 진행)
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}
