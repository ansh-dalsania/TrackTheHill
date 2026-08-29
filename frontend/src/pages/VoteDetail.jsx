import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { explainAction } from '../utils/legislativeGlossary'

function VoteDetail() {
  const { voteId } = useParams()
  const [vote, setVote] = useState(null)

  useEffect(() => {
    fetch(`http://localhost:8080/api/votes/${voteId}`)
      .then(res => res.json())
      .then(setVote)
  }, [voteId])

  if (!vote) return <div>Loading...</div>

  const yeas = vote.memberPositions.filter(p => p.position === 'Yea' || p.position === 'Aye')
  const nays = vote.memberPositions.filter(p => p.position === 'Nay' || p.position === 'No')
  const others = vote.memberPositions.filter(p => !yeas.includes(p) && !nays.includes(p))

  return (
    <div>
      <h1>{vote.voteQuestion}</h1>
      <p>{vote.chamber} — Congress {vote.congress} — {vote.voteDate?.split('T')[0]}</p>
      <p>Result: {vote.result}</p>
      {explainAction(vote.voteQuestion) && <p><em>{explainAction(vote.voteQuestion)}</em></p>}
      {vote.billId && (
        <p>Bill: <Link to={`/bills/${vote.billId}`}>{vote.billTitle}</Link></p>
      )}
      <h2>Yea ({yeas.length})</h2>
      <ul>
        {yeas.map(p => (
          <li key={p.bioguideId}>
            <Link to={`/members/${p.bioguideId}`}>{p.firstName} {p.lastName} ({p.party}-{p.state})</Link>
          </li>
        ))}
      </ul>

      <h2>Nay ({nays.length})</h2>
      <ul>
        {nays.map(p => (
          <li key={p.bioguideId}>
            <Link to={`/members/${p.bioguideId}`}>{p.firstName} {p.lastName} ({p.party}-{p.state})</Link>
          </li>
        ))}
      </ul>

      <h2>Other ({others.length})</h2>
      <ul>
        {others.map(p => (
          <li key={p.bioguideId}>
            {p.firstName} {p.lastName} ({p.party}-{p.state}) — {p.position}
          </li>
        ))}
      </ul>
    </div>
  )
}

export default VoteDetail