import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { explainAction } from '../utils/legislativeGlossary'
import PartyBadge from '../components/PartyBadge'

function VoteDetail() {
  const { voteId } = useParams()
  const [vote, setVote] = useState(null)

  useEffect(() => {
    fetch(`http://localhost:8080/api/votes/${voteId}`).then(res => res.json()).then(setVote)
  }, [voteId])

  if (!vote) return <div className="text-slate-500">Loading...</div>

  const yeas = vote.memberPositions.filter(p => p.position === 'Yea' || p.position === 'Aye')
  const nays = vote.memberPositions.filter(p => p.position === 'Nay' || p.position === 'No')
  const others = vote.memberPositions.filter(p => !yeas.includes(p) && !nays.includes(p))

  const PositionList = ({ title, list, colorClass }) => (
    <div className="bg-white border border-slate-200 rounded-xl p-5">
      <h2 className={`text-base font-semibold mb-3 ${colorClass}`}>{title} ({list.length})</h2>
      <ul className="space-y-1 max-h-96 overflow-y-auto">
        {list.map(p => (
          <li key={p.bioguideId} className="text-sm flex items-center gap-1.5">
            <Link to={`/members/${p.bioguideId}`} className="text-navy-700 hover:text-gold-600 hover:underline">
              {p.firstName} {p.lastName}
            </Link>
            <PartyBadge party={p.party} />
            <span className="text-xs text-slate-400">{p.state}</span>
          </li>
        ))}
      </ul>
    </div>
  )

  return (
    <div>
      <h1 className="text-2xl font-bold text-navy-700 mb-1">{vote.voteQuestion}</h1>
      <p className="text-sm text-slate-500 mb-4">
        {vote.chamber} — Congress {vote.congress} — {vote.voteDate?.split('T')[0]}
      </p>

      <div className="bg-navy-700 rounded-xl p-4 mb-2 inline-block">
        <p className="text-xs text-slate-300">Result</p>
        <p className="text-lg font-bold text-gold-500">{vote.result}</p>
      </div>

      {explainAction(vote.voteQuestion) && (
        <p className="text-xs text-slate-500 italic mb-4">{explainAction(vote.voteQuestion)}</p>
      )}

      {vote.billId && (
        <p className="text-sm mb-6">
          Bill:{' '}
          <Link to={`/bills/${vote.billId}`} className="text-navy-700 font-medium hover:text-gold-600 hover:underline">
            {vote.billTitle}
          </Link>
        </p>
      )}

      <div className="grid md:grid-cols-3 gap-4 mt-6">
        <PositionList title="Yea" list={yeas} colorClass="text-green-700" />
        <PositionList title="Nay" list={nays} colorClass="text-red-700" />
        <PositionList title="Other" list={others} colorClass="text-slate-500" />
      </div>
    </div>
  )
}

export default VoteDetail