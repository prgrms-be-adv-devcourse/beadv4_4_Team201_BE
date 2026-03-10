import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    stages: [
        { duration: '5s', target: 5 },
        { duration: '5s', target: 100 },
        { duration: '10s', target: 100 },
        { duration: '5s', target: 5 },
        { duration: '5s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<1500'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/actuator/health`);
    check(res, {
        'status 200': (r) => r.status === 200,
    });
    sleep(0.2);
}
