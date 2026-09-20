import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import PartyBadge from '../components/PartyBadge'

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

  if (loading) return <div className="text-slate-500">Loading...</div>

  const senators = members.filter(m => m.chamber === 'Senate')
  const houseMembers = members.filter(m => m.chamber === 'House')

  const MemberCard = ({ m, showDistrict }) => (
    <Link
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
        {showDistrict && <span>District {m.district}</span>}
      </div>
    </Link>
  )

  return (
    <div>
      <h1 className="text-2xl font-bold text-navy-700 mb-6">{stateCode} Representatives</h1>

      <h2 className="text-lg font-semibold text-slate-700 mb-3">Senators</h2>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 mb-8">
        {senators.map(m => <MemberCard key={m.bioguideId} m={m} showDistrict={false} />)}
      </div>

      <h2 className="text-lg font-semibold text-slate-700 mb-3">House Members</h2>
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
        {houseMembers.map(m => <MemberCard key={m.bioguideId} m={m} showDistrict={true} />)}
      </div>
    </div>
  )
}

export default StateMembers