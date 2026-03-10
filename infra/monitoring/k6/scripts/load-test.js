import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    stages: [
        { duration: '15s', target: 20 },
        { duration: '30s', target: 20 },
        { duration: '15s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.05'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/actuator/health`);
    check(res, {
        'status 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    const products = http.get(`${BASE_URL}/api/v2/products/11`);
    check(products, {
        'product 200 or 404': (r) => r.status === 200 || r.status === 404,
    });

    sleep(0.5);
}
