const styles = {
  D: 'bg-blue-100 text-blue-800',
  R: 'bg-red-100 text-red-800',
  I: 'bg-slate-100 text-slate-700',
}

function PartyBadge({ party }) {
  return (
    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-semibold ${styles[party] || styles.I}`}>
      {party}
    </span>
  )
}

export default PartyBadge