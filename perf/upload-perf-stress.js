import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 10 },
    { duration: '10s', target: 25 },
    { duration: '10s', target: 40 },
    { duration: '10s', target: 40 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.10'],
    http_req_duration: ['p(95)<4000'],
    checks: ['rate>0.90'],
  },
};

const token = __ENV.TOKEN;
const fileName = __ENV.FILE_NAME || 'perf-1kb.txt';
const fileType = 'text/plain';
const fileContent = open(`./${fileName}`, 'b');

export default function () {
  const payload = {
    file: http.file(fileContent, fileName, fileType),
  };

  const params = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    timeout: '60s',
  };

  const response = http.post('http://localhost:8080/api/files', payload, params);

  check(response, {
    'status is 201': (r) => r.status === 201,
    'response has downloadToken': (r) => {
      try {
        const body = JSON.parse(r.body);
        return !!body.downloadToken;
      } catch (e) {
        return false;
      }
    },
  });
}