import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '10s', target: 30 },
        { duration: '10s', target: 50 },
        { duration: '10s', target: 80 },
        { duration: '10s', target: 100 },
        { duration: '20s', target: 100 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(99)<1000'],
        http_req_failed: ['rate<0.1'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/actuator/health`);
    check(res, {
        'status 200': (r) => r.status === 200,
    });
    sleep(0.3);
}
