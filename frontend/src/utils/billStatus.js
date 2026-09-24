/**
 * Determines a bill's overall enactment status from its latest action text.
 * This is distinct from per-vote "Final Passage" categorization — a bill can
 * show "Final Passage" on a House vote while still being far from enacted,
 * since it also needs to pass the other chamber and be signed into law.
 */
export function getBillStatus(latestActionText) {
  if (!latestActionText) return null
  const text = latestActionText

  if (text.includes('Became Public Law')) return { label: 'Became Law', color: 'bg-green-100 text-green-800' }
  if (text.includes('Vetoed')) return { label: 'Vetoed', color: 'bg-red-100 text-red-800' }
  if (text.includes('Passed Senate') && text.includes('Passed House')) {
    return { label: 'Passed Both Chambers', color: 'bg-gold-100 text-gold-700' }
  }
  if (text.includes('Passed Senate') || text.includes('Passed House')) {
    return { label: 'Passed One Chamber', color: 'bg-blue-100 text-blue-800' }
  }
  return { label: 'In Progress', color: 'bg-slate-100 text-slate-600' }
}