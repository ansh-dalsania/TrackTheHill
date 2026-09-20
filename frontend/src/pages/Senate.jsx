import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import PartyBadge from '../components/PartyBadge'

function Senate() {
  const [members, setMembers] = useState([])
  const [query, setQuery] = useState('')

  useEffect(() => {
    if (query.trim() === '') {
      fetch('http://localhost:8080/api/members?size=500')
        .then(res => res.json())
        .then(data => setMembers(data.content.filter(m => m.chamber === 'Senate')))
    } else {
      fetch(`http://localhost:8080/api/members/search?chamber=Senate&query=${query}`)
        .then(res => res.json())
        .then(data => setMembers(data))
    }
  }, [query])

  return (
    <div>
      <h1 className="text-2xl font-bold text-navy-700 mb-4">Senate</h1>

      <input
        type="text"
        placeholder="Search by name..."
        value={query}
        onChange={e => setQuery(e.target.value)}
        className="w-full max-w-sm mb-6 px-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gold-500 focus:border-transparent"
      />

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
        {members.map(m => (
          <Link
            key={m.bioguideId}
            to={`/members/${m.bioguideId}`}
            className="flex flex-col items-center gap-2 p-4 bg-white border border-slate-200 rounded-xl hover:border-gold-500 hover:shadow-md transition-all text-center"
          >
            <img
              src={m.headshotUrl}
              alt={m.lastName}
              className="w-16 h-16 rounded-full object-cover border border-slate-200"
            />
            <span className="text-sm font-semibold text-slate-900">{m.firstName} {m.lastName}</span>
            <div className="flex items-center gap-1.5 text-xs text-slate-500">
              <PartyBadge party={m.party} />
              <span>{m.state}</span>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}

export default Senate