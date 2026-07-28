import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { categorizeVote } from '../utils/voteCategory'

function MemberProfile() {
  const { bioguideId } = useParams()
  const [member, setMember] = useState(null)
  const [committees, setCommittees] = useState([])
  const [attendance, setAttendance] = useState(null)
  const [partisanScore, setPartisanScore] = useState(null)
  const [billsSponsored, setBillsSponsored] = useState([])
  const [billsCosponsored, setBillsCosponsored] = useState([])
  const [financeSummary, setFinanceSummary] = useState(null)
  const [topDonors, setTopDonors] = useState([])
  const [votingHistory, setVotingHistory] = useState([])
  const [policyAreas, setPolicyAreas] = useState([])
  const [selectedPolicyArea, setSelectedPolicyArea] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')

  // Fetch everything that doesn't depend on the policy area filter
  useEffect(() => {
    const base = `http://localhost:8080/api/members/${bioguideId}`

    fetch(base).then(res => res.json()).then(setMember)
    fetch(`${base}/committees`).then(res => res.json()).then(setCommittees)
    fetch(`${base}/attendance`).then(res => res.json()).then(setAttendance)
    fetch(`${base}/partisan-score`).then(res => res.json()).then(setPartisanScore)
    fetch(`${base}/bills-sponsored`).then(res => res.json()).then(setBillsSponsored)
    fetch(`${base}/bills-cosponsored`).then(res => res.json()).then(setBillsCosponsored)
    fetch(`${base}/finance-summary`).then(res => res.json()).then(setFinanceSummary)
    fetch(`${base}/top-donors`).then(res => res.json()).then(setTopDonors)
    fetch('http://localhost:8080/api/policy-areas').then(res => res.json()).then(setPolicyAreas)
  }, [bioguideId])

  // Fetch voting history separately, since it depends on the policy area filter
  useEffect(() => {
    const url = selectedPolicyArea
      ? `http://localhost:8080/api/members/${bioguideId}/votes-by-issue?policyArea=${encodeURIComponent(selectedPolicyArea)}`
      : `http://localhost:8080/api/members/${bioguideId}/votes`

    fetch(url).then(res => res.json()).then(setVotingHistory)
  }, [bioguideId, selectedPolicyArea])

  if (!member) return <div>Loading...</div>

  return (
    <div>
      <h1>{member.firstName} {member.lastName}</h1>
      <img src={member.headshotUrl} alt={member.lastName} width="150" />
      <p>{member.party} - {member.state}{member.district ? `-${member.district}` : ''} ({member.chamber})</p>
      <p>Term start: {member.termStartDate}</p>
      {member.nominateDim1 != null && <p>DW-NOMINATE (liberal-conservative): {member.nominateDim1}</p>}

      <h2>Committees</h2>
      <ul>
        {committees.map(c => (
          <li key={c.id}>{c.committee.name} {c.title ? `— ${c.title}` : ''}</li>
        ))}
      </ul>

      <h2>Attendance (Congress {attendance?.congress})</h2>
      {attendance && (
        <p>{attendance.attendancePercentage}% overall ({attendance.missedVotes} missed of {attendance.totalVotes}) —
           Last 30 days: {attendance.recentAttendancePercentage}%</p>
      )}

      <h2>Partisan Score</h2>
      {partisanScore && (
        <p>{partisanScore.partisanScorePercentage}% voted with party majority ({partisanScore.votesWithPartyMajority} of {partisanScore.votesConsidered})</p>
      )}

      <h2>Bills Sponsored ({billsSponsored.length})</h2>
      <p><em>Reflects bills that have shown legislative progress; does not include bills introduced but never acted upon. <Link to="/about">Learn more</Link>.</em></p>
      <ul>
        {billsSponsored.slice(0, 10).map(b => (
          <li key={b.id}><Link to={`/bills/${b.id}`}>{b.title}</Link></li>
        ))}
      </ul>

      <h2>Bills Cosponsored ({billsCosponsored.length})</h2>
      <p><em>Also reflects only bills that have shown legislative progress. <Link to="/about">Learn more</Link>.</em></p>
      <ul>
        {billsCosponsored.slice(0, 10).map(bc => (
          <li key={bc.bill.id}><Link to={`/bills/${bc.bill.id}`}>{bc.bill.title}</Link></li>
        ))}
      </ul>

      <h2>Campaign Finance</h2>
      {financeSummary && (
        <ul>
          <li>Individual contributions: ${financeSummary.individualContributions?.toLocaleString()}</li>
          <li>PAC contributions: ${financeSummary.pacContributions?.toLocaleString()}</li>
          <li>Party contributions: ${financeSummary.partyContributions?.toLocaleString()}</li>
          <li>Total receipts: ${financeSummary.totalReceipts?.toLocaleString()}</li>
        </ul>
      )}

      <h2>Top Donors</h2>
      <ul>
        {topDonors.slice(0, 10).map(d => (
          <li key={d.id}>{d.contributorName} — ${d.contributionAmount?.toLocaleString()} ({d.contributorEmployer})</li>
        ))}
      </ul>

      <h2>Voting History</h2>
      <label>
        Filter by issue:{' '}
        <select value={selectedPolicyArea} onChange={e => setSelectedPolicyArea(e.target.value)}>
          <option value="">All Issues</option>
          {policyAreas.map(area => (
            <option key={area} value={area}>{area}</option>
          ))}
        </select>
      </label>
      {' '}
      <label>
        Filter by vote type:{' '}
        <select value={selectedCategory} onChange={e => setSelectedCategory(e.target.value)}>
          <option value="">All Types</option>
          <option value="Final Passage">Final Passage</option>
          <option value="Amendment">Amendment</option>
          <option value="Procedural/Other">Procedural/Other</option>
        </select>
      </label>
      <ul>
        {votingHistory
          .filter(v => !selectedCategory || categorizeVote(v) === selectedCategory)
          .slice(0, 15)
          .map(v => (
            <li key={v.voteId}>
              <Link to={`/votes/${v.voteId}`}>{v.voteDate?.split('T')[0]} — {v.voteQuestion}: {v.position}</Link>
              {v.billTitle && ` (${v.billTitle})`}
            </li>
          ))}
      </ul>
    </div>
  )
}

export default MemberProfile