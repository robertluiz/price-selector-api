import http from 'k6/http';
import { check, sleep } from 'k6';
const HOST = __ENV.HOST || 'localhost:8081';

export const options = {
  scenarios: {
    normal: {
      executor: 'constant-vus',
      vus: 50,
      duration: '30s',
    },
    spike: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '10s', target: 100 },
        { duration: '20s', target: 100 },
        { duration: '10s', target: 0 },
      ],
    },
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200 },
        { duration: '60s', target: 200 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.1'],
  },
};

const testCases = [
  {
    name: 'Test 1 - 10:00 Day 14',
    params: {
      applicationDate: '2020-06-14T10:00:00',
      productId: 35455,
      brandId: 1,
    },
    expectedPrice: 35.50,
  },
  {
    name: 'Test 2 - 16:00 Day 14',
    params: {
      applicationDate: '2020-06-14T16:00:00',
      productId: 35455,
      brandId: 1,
    },
    expectedPrice: 25.45,
  },
  {
    name: 'Test 3 - 21:00 Day 14',
    params: {
      applicationDate: '2020-06-14T21:00:00',
      productId: 35455,
      brandId: 1,
    },
    expectedPrice: 35.50,
  },
  {
    name: 'Test 4 - 10:00 Day 15',
    params: {
      applicationDate: '2020-06-15T10:00:00',
      productId: 35455,
      brandId: 1,
    },
    expectedPrice: 30.50,
  },
  {
    name: 'Test 5 - 21:00 Day 16',
    params: {
      applicationDate: '2020-06-16T21:00:00',
      productId: 35455,
      brandId: 1,
    },
    expectedPrice: 38.95,
  },
];

export default function () {
  const testCase = testCases[Math.floor(Math.random() * testCases.length)];
  
  const url = `http://${HOST}/api/v1/prices/query?applicationDate=${testCase.params.applicationDate}&productId=${testCase.params.productId}&brandId=${testCase.params.brandId}`;

  const response = http.get(url);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
    'has valid JSON response': (r) => {
      try {
        JSON.parse(r.body);
        return true;
      } catch (e) {
        return false;
      }
    },
    'correct price returned': (r) => {
      if (r.status === 200) {
        try {
          const data = JSON.parse(r.body);
          return Math.abs(data.finalPrice - testCase.expectedPrice) < 0.01;
        } catch (e) {
          return false;
        }
      }
      return true;
    },
  });

  sleep(0.1);
}

export function handleSummary(data) {
  return {
    'performance-results.json': JSON.stringify(data, null, 2),
    stdout: `
=== PERFORMANCE TEST SUMMARY ===
Total Requests: ${data.metrics.http_reqs.values.count}
Failed Requests: ${data.metrics.http_req_failed.values.count}
Success Rate: ${((1 - data.metrics.http_req_failed.values.rate) * 100).toFixed(2)}%
Average Response Time: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms
95th Percentile: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms
Requests per Second: ${data.metrics.http_reqs.values.rate.toFixed(2)}
===============================
    `,
  };
} 