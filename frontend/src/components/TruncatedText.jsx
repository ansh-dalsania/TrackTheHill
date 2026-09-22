import { useState } from 'react'

function TruncatedText({ text, limit = 120, className = '' }) {
  const [expanded, setExpanded] = useState(false)

  if (!text) return null
  if (text.length <= limit) return <span className={className}>{text}</span>

  return (
    <span className={className}>
      {expanded ? text : text.slice(0, limit) + '…'}{' '}
      <button
        onClick={(e) => { e.preventDefault(); e.stopPropagation(); setExpanded(!expanded) }}
        className="text-gold-600 text-xs font-semibold hover:underline"
      >
        {expanded ? 'Show less' : 'Show more'}
      </button>
    </span>
  )
}

export default TruncatedText