export function categorizeVote(vote) {
  if (vote.chamber === 'House') {
    const q = vote.voteQuestion || ''
    if (q.includes('Amendment')) return 'Amendment'
    if (q.includes('Passage') || q.includes('Suspend the Rules and Pass')) return 'Final Passage'
    return 'Procedural/Other'
  }

  const r = vote.result || ''
  if (r.includes('Amendment')) return 'Amendment'
  if (r.includes('Bill Passed') || r.includes('Bill Defeated') ||
      r.includes('Joint Resolution') || r.includes('Concurrent Resolution')) {
    return 'Final Passage'
  }
  return 'Procedural/Other'
}