import api from './api';

export const login = (user_name, password) => {
  return api.post('/auth/login', { user_name, password });
};

export const signup = (payload) => {
  return api.post('/auth/signup', payload);
};
