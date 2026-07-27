import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { explainAction } from '../utils/legislativeGlossary'

function BillDetail() {
  const { billId } = useParams()
  const [bill, setBill] = useState(null)
  const [votes, setVotes] = useState([])

  useEffect(() => {
    fetch(`http://localhost:8080/api/bills/${billId}`)
      .then(res => res.json())
      .then(setBill)

    fetch(`http://localhost:8080/api/bills/${billId}/votes`)
      .then(res => res.json())
      .then(setVotes)
  }, [billId])

  if (!bill) return <div>Loading...</div>

  return (
    <div>
      <h1>{bill.title}</h1>
      <p>{bill.billType.toUpperCase()} {bill.billNumber} — {bill.originChamber}</p>
      <p>Policy area: {bill.policyArea}</p>
      <p>Introduced: {bill.introducedDate}</p>
      <p>
        Latest action ({bill.latestActionDate}): {bill.latestActionText}
        {explainAction(bill.latestActionText) && (
          <><br /><em>({explainAction(bill.latestActionText)})</em></>
        )}
      </p>
      {bill.sponsorBioguideId && (
        <p>Sponsor: <Link to={`/members/${bill.sponsorBioguideId}`}>{bill.sponsorName}</Link></p>
      )}
      <p><a href={bill.congressGovUrl} target="_blank" rel="noreferrer">View on Congress.gov</a></p>

      {bill.summary && (
        <>
          <h2>Summary</h2>
          <div dangerouslySetInnerHTML={{ __html: bill.summary }} />
        </>
      )}

      <h2>Cosponsors ({bill.cosponsors.length})</h2>
      <ul>
        {bill.cosponsors.map(c => (
          <li key={c.bioguideId}>
            <Link to={`/members/${c.bioguideId}`}>{c.name}</Link>
            {c.isOriginalCosponsor && ' (original cosponsor)'}
          </li>
        ))}
      </ul>
      <h2>Votes on This Bill ({votes.length})</h2>
      <ul>
        {votes.map(v => (
          <li key={v.id}>
            <Link to={`/votes/${v.id}`}>
            {v.chamber} — {v.voteDate?.split('T')[0]} — {v.voteQuestion} ({v.result})
          </Link>
          </li>
        ))}
      </ul>
    </div>
  )
}

export default BillDetail