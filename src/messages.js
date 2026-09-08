import axios from 'axios';

const messages = {};

export async function loadMessages() {
  const response = await axios.get('/api/messages');
  Object.assign(messages, response.data);
  return messages;
}

export function getMessage(key) {
  return messages[key] || key;
}
