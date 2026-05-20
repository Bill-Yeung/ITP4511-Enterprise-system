export function clinicCode(name) {
  if (!name) return '';
  const head = name.split(/\s+Community\b/i)[0];
  return head
    .split(/\s+/)
    .filter(Boolean)
    .map((w) => w[0])
    .join('')
    .toUpperCase();
}

export function formatTicket(clinicName, queueNumber) {
  const code = clinicCode(clinicName);
  return code ? `#${code}${queueNumber}` : `#${queueNumber}`;
}
