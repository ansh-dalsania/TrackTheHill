import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'

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
      <h1>Senate</h1>
      <input
        type="text"
        placeholder="Search by name..."
        value={query}
        onChange={e => setQuery(e.target.value)}
      />
      <ul>
        {members.map(m => (
          <li key={m.bioguideId}>
            <Link to={`/members/${m.bioguideId}`}>
              {m.firstName} {m.lastName} ({m.party}-{m.state})
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}

export default Senate