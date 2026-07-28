const billTypeNames = {
  hr: 'house-bill',
  s: 'senate-bill',
  hres: 'house-resolution',
  sres: 'senate-resolution',
  hjres: 'house-joint-resolution',
  sjres: 'senate-joint-resolution',
  hconres: 'house-concurrent-resolution',
  sconres: 'senate-concurrent-resolution',
}

function ordinal(n) {
  const s = ['th', 'st', 'nd', 'rd']
  const v = n % 100
  return n + (s[(v - 20) % 10] || s[v] || s[0])
}

export function buildCongressGovUrl(billId) {
  const [congress, billType, number] = billId.split('-')
  const typeName = billTypeNames[billType] || billType
  return `https://www.congress.gov/bill/${ordinal(Number(congress))}-congress/${typeName}/${number}`
}