import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '30s', target: 10 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        'http_req_duration{name:health}': ['p(95)<200'],
        'http_req_duration{name:product}': ['p(95)<500'],
        'http_req_duration{name:products_search}': ['p(95)<500'],
        'http_req_duration{name:funding_list}': ['p(95)<500'],
    },
};

export default function () {
    group('Public APIs', function () {
        const health = http.get(`${BASE_URL}/actuator/health`, {
            tags: { name: 'health' },
        });
        check(health, { 'health 200': (r) => r.status === 200 });

        const product = http.get(`${BASE_URL}/api/v2/products/11`, {
            tags: { name: 'product' },
        });
        check(product, { 'product ok': (r) => r.status === 200 || r.status === 404 });

        const search = http.get(`${BASE_URL}/api/v2/products/search?keyword=apple&page=0&size=10`, {
            tags: { name: 'products_search' },
        });
        check(search, { 'search ok': (r) => r.status === 200 || r.status === 404 });

        const fundings = http.get(`${BASE_URL}/api/v2/fundings/list?page=0&size=10`, {
            tags: { name: 'funding_list' },
        });
        check(fundings, { 'fundings ok': (r) => r.status === 200 || r.status === 401 });
    });

    sleep(1);
}
