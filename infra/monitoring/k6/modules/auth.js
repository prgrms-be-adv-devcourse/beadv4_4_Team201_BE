import http from 'k6/http';
import { sleep } from 'k6';

/**
 * Auth0 Resource Owner Password Grant로 토큰을 벌크 발급하는 모듈.
 *
 * Password Grant는 Auth0 Dashboard에서 별도 활성화 필요:
 * Application > Advanced Settings > Grant Types > "Password" 체크
 */

/**
 * @param {Array<{email: string, password: string}>} accounts
 * @param {string} auth0Domain
 * @param {string} clientId
 * @param {string} clientSecret
 * @param {string} audience
 * @returns {string[]} 발급된 access_token 배열
 *
 * Rate Limit: 250ms 간격 = 초당 4건. Auth0 free tier 분당 300건 한도 내 유지.
 */
export function getTokens(accounts, auth0Domain, clientId, clientSecret, audience) {
    const tokens = [];
    const url = `https://${auth0Domain}/oauth/token`;

    for (const acct of accounts) {
        const payload = JSON.stringify({
            grant_type: 'password',
            client_id: clientId,
            client_secret: clientSecret,
            audience: audience,
            scope: 'openid email profile',
            username: acct.email,
            password: acct.password,
        });

        const params = {
            headers: { 'Content-Type': 'application/json' },
        };

        const res = http.post(url, payload, params);

        if (res.status === 200) {
            const token = res.json('access_token');
            if (token) {
                tokens.push(token);
            } else {
                console.warn(`access_token 없음: ${acct.email}`);
            }
        } else {
            console.warn(`인증 실패 ${acct.email}: ${res.status} ${res.body}`);
        }

        sleep(0.25);
    }

    console.log(`토큰 발급 완료: ${tokens.length}/${accounts.length}개 성공`);
    return tokens;
}
