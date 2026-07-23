import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'

function StateMembers() {
  const { stateCode } = useParams()
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    fetch(`http://localhost:8080/api/members/by-state/${stateCode}`)
      .then(res => res.json())
      .then(data => {
        setMembers(data)
        setLoading(false)
      })
      .catch(err => {
        console.error('Failed to load members:', err)
        setLoading(false)
      })
  }, [stateCode])

  if (loading) return <div>Loading...</div>

  const senators = members.filter(m => m.chamber === 'Senate')
  const houseMembers = members.filter(m => m.chamber === 'House')

  return (
    <div>
      <h1>{stateCode} Representatives</h1>

      <h2>Senators</h2>
      <ul>
        {senators.map(m => (
          <li key={m.bioguideId}>
            <Link to={`/members/${m.bioguideId}`}>
              {m.firstName} {m.lastName} ({m.party})
            </Link>
          </li>
        ))}
      </ul>

      <h2>House Members</h2>
      <ul>
        {houseMembers.map(m => (
          <li key={m.bioguideId}>
            <Link to={`/members/${m.bioguideId}`}>
              {m.firstName} {m.lastName} (District {m.district}, {m.party})
            </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}

export default StateMembers