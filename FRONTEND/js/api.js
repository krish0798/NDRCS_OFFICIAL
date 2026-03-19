// FRONTEND/js/api.js - Core API and fetch utilities
const API_BASE_URL = 'http://localhost:8080';

/**
 * Perform a generic fetch wrapper
 */
async function fetchApi(endpoint, options = {}) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, config);
    // Parse JSON safely
    const isJson = response.headers.get('content-type')?.includes('application/json');
    const data = isJson ? await response.json() : await response.text();

    if (!response.ok) {
        throw new Error((data && data.message) || response.statusText);
    }

    // Backend returns 200 OK with error strings sometimes instead of 4xx
    if (!isJson && typeof data === 'string' && data.trim() !== '') {
      if (!data.toLowerCase().includes('success')) {
        throw new Error(data);
      }
    }

    return { response, data };
  } catch (error) {
    console.error(`API Error for ${endpoint}:`, error);
    throw error;
  }
}

const api = {
  get: (endpoint) => fetchApi(endpoint, { method: 'GET' }),
  post: (endpoint, body) => fetchApi(endpoint, { method: 'POST', body: JSON.stringify(body) }),
  put: (endpoint, body) => fetchApi(endpoint, { method: 'PUT', body: JSON.stringify(body) }),
  delete: (endpoint) => fetchApi(endpoint, { method: 'DELETE' })
};
