// Maps common procedural phrases (as substrings) to plain-language explanations.
// This is a best-effort glossary for common patterns, not exhaustive —
// official action text is always shown as-is, this just adds context.
const glossary = [
  { match: 'Motion to reconsider laid on the table', explanation: 'A routine procedural step that finalizes a vote and prevents it from being revisited.' },
  { match: 'Agreed to without objection', explanation: 'Passed with unanimous, informal consent — no formal roll call vote was taken.' },
  { match: 'Referred to the Committee', explanation: 'Sent to a committee for review; most bills end here and never receive a floor vote.' },
  { match: 'Placed on the Union Calendar', explanation: 'Scheduled for potential future floor consideration in the House.' },
  { match: 'Placed on the Senate Legislative Calendar', explanation: 'Scheduled for potential future floor consideration in the Senate.' },
  { match: 'Read the second time', explanation: 'A procedural step required before a bill can be debated or amended.' },
  { match: 'Read the third time', explanation: 'The final procedural reading before a final passage vote.' },
  { match: 'Ordered to be Reported', explanation: 'A committee has voted to send the bill forward, typically toward a floor vote.' },
  { match: 'Became Public Law', explanation: 'The bill has been signed into law.' },
  { match: 'Cloture', explanation: 'A Senate procedure to end debate and allow a final vote, often requiring 60 votes.' },
  { match: 'Committee Discharged', explanation: 'The bill was removed from committee consideration, often to allow a floor vote.' },
]

export function explainAction(actionText) {
  if (!actionText) return null
  const match = glossary.find(g => actionText.includes(g.match))
  return match ? match.explanation : null
}