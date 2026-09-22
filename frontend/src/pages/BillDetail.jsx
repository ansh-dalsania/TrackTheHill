import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { buildCongressGovUrl } from '../utils/congressUrl'
import { categorizeVote } from '../utils/voteCategory'
import { explainAction } from '../utils/legislativeGlossary'

function BillDetail() {
  const { billId } = useParams()
  const [bill, setBill] = useState(null)
  const [votes, setVotes] = useState([])

  useEffect(() => {
    fetch(`http://localhost:8080/api/bills/${billId}`).then(res => res.json()).then(setBill)
    fetch(`http://localhost:8080/api/bills/${billId}/votes`).then(res => res.json()).then(setVotes)
  }, [billId])

  if (!bill) return <div className="text-slate-500">Loading...</div>
  console.log('DEBUG bill object:', bill)

  return (
    <div>
      <h1 className="text-2xl font-bold text-navy-700 mb-2">{bill.title}</h1>
      <p className="text-sm text-slate-500 mb-4">
        {bill.billType.toUpperCase()} {bill.billNumber} · {bill.originChamber} · {bill.policyArea}
      </p>

      <div className="bg-white border border-slate-200 rounded-xl p-5 mb-6 text-sm space-y-2">
        <p><span className="font-semibold text-slate-700">Introduced:</span> {bill.introducedDate}</p>
        <div>
          <span className="font-semibold text-slate-700">Latest action ({bill.latestActionDate}):</span> {bill.latestActionText || 'MISSING'}
        </div>
        {bill.sponsorBioguideId && (
          <p>
            <span className="font-semibold text-slate-700">Sponsor:</span>{' '}
            <Link to={`/members/${bill.sponsorBioguideId}`} className="text-navy-700 hover:text-gold-600 hover:underline">
              {bill.sponsorName}
            </Link>
          </p>
        )}
        <p>
          <a href={buildCongressGovUrl(bill.id)} target="_blank" rel="noreferrer" className="text-gold-600 hover:underline">
            View on Congress.gov →
          </a>
        </p>
      </div>

      {bill.summary && (
        <div className="bg-white border border-slate-200 rounded-xl p-5 mb-6">
          <h2 className="text-base font-semibold text-navy-700 mb-2">Summary</h2>
          <div className="text-sm text-slate-700 leading-relaxed prose-sm" dangerouslySetInnerHTML={{ __html: bill.summary }} />
        </div>
      )}

      <div className="bg-white border border-slate-200 rounded-xl p-5 mb-6">
        <h2 className="text-base font-semibold text-navy-700 mb-3">Votes on This Bill ({votes.length})</h2>
        <ul className="divide-y divide-slate-100">
          {votes.map(v => {
            const isFinalPassage = categorizeVote(v) === 'Final Passage'
            return (
              <li key={v.id} className="py-2">
                <Link to={`/votes/${v.id}`} className={`text-sm hover:text-gold-600 ${isFinalPassage ? 'font-bold text-navy-700' : 'text-slate-700'}`}>
                  {v.chamber} — {v.voteDate?.split('T')[0]} — {v.voteQuestion} ({v.result})
                  {isFinalPassage && <span className="ml-2 text-gold-600">⭐ Final Passage</span>}
                </Link>
              </li>
            )
          })}
        </ul>
      </div>

      <div className="bg-white border border-slate-200 rounded-xl p-5">
        <h2 className="text-base font-semibold text-navy-700 mb-3">Cosponsors ({bill.cosponsors.length})</h2>
        <ul className="space-y-1.5">
          {bill.cosponsors.map(c => (
            <li key={c.bioguideId} className="text-sm">
              <Link to={`/members/${c.bioguideId}`} className="text-navy-700 hover:text-gold-600 hover:underline">{c.name}</Link>
              {c.isOriginalCosponsor && <span className="text-xs text-slate-400 ml-1">(original cosponsor)</span>}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}

export default BillDetail