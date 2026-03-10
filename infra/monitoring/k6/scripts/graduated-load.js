import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TARGET_VUS = parseInt(__ENV.VUS || '100');

const searchDuration = new Trend('search_duration', true);
const productDuration = new Trend('product_duration', true);
const fundingDuration = new Trend('funding_duration', true);
const errorRate = new Rate('error_rate');

export const options = {
    stages: [
        { duration: '15s', target: Math.floor(TARGET_VUS * 0.2) },
        { duration: '15s', target: Math.floor(TARGET_VUS * 0.5) },
        { duration: '15s', target: TARGET_VUS },
        { duration: '30s', target: TARGET_VUS },
        { duration: '15s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        error_rate: ['rate<0.3'],
    },
};

const PRODUCT_IDS = [1, 2, 3, 5, 7, 11, 13, 15];
const SEARCH_KEYWORDS = ['apple', 'gift', 'cake', 'toy', 'book', 'bag', 'ring', 'pen'];

export default function () {
    const productId = PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];
    const keyword = SEARCH_KEYWORDS[Math.floor(Math.random() * SEARCH_KEYWORDS.length)];
    const page = Math.floor(Math.random() * 3);

    const product = http.get(`${BASE_URL}/api/v2/products/${productId}`, {
        tags: { name: 'product_detail' },
    });
    check(product, { 'product ok': (r) => r.status === 200 || r.status === 404 });
    productDuration.add(product.timings.duration);
    errorRate.add(product.status >= 500);

    const search = http.get(`${BASE_URL}/api/v2/products/search?keyword=${keyword}&page=${page}&size=20`, {
        tags: { name: 'product_search' },
    });
    check(search, { 'search ok': (r) => r.status === 200 || r.status === 404 });
    searchDuration.add(search.timings.duration);
    errorRate.add(search.status >= 500);

    const fundings = http.get(`${BASE_URL}/api/v2/fundings/list?page=${page}&size=20`, {
        tags: { name: 'funding_list' },
    });
    check(fundings, { 'funding ok': (r) => r.status === 200 || r.status === 401 });
    fundingDuration.add(fundings.timings.duration);
    errorRate.add(fundings.status >= 500);

    sleep(0.1 + Math.random() * 0.2);
}
