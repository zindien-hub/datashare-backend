import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2000'],
  },
};

const token = __ENV.TOKEN;
const fileContent = open('./perf-test.txt', 'b');

export default function () {
  const data = {
    file: http.file(fileContent, 'perf-test.txt', 'text/plain'),
  };

  const headers = {
    Authorization: `Bearer ${token}`,
  };

  const response = http.post('http://localhost:8080/api/files', data, {
    headers,
  });

  check(response, {
    'status is 201': (r) => r.status === 201,
  });

  sleep(1);
}