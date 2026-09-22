import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import PartyBadge from '../components/PartyBadge'
import { categorizeVote } from '../utils/voteCategory'
import { explainAction } from '../utils/legislativeGlossary'
import TruncatedText from '../components/TruncatedText'

function MemberProfile() {
  const { bioguideId } = useParams()
  const [member, setMember] = useState(null)
  const [committees, setCommittees] = useState([])
  const [attendance, setAttendance] = useState(null)
  const [partisanScore, setPartisanScore] = useState(null)
  const [billsSponsored, setBillsSponsored] = useState([])
  const [billsCosponsored, setBillsCosponsored] = useState([])
  const [votingHistory, setVotingHistory] = useState([])
  const [policyAreas, setPolicyAreas] = useState([])
  const [selectedPolicyArea, setSelectedPolicyArea] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')

  useEffect(() => {
    const base = `http://localhost:8080/api/members/${bioguideId}`
    fetch(base).then(res => res.json()).then(setMember)
    fetch(`${base}/committees`).then(res => res.json()).then(setCommittees)
    fetch(`${base}/attendance`).then(res => res.json()).then(setAttendance)
    fetch(`${base}/partisan-score`).then(res => res.json()).then(setPartisanScore)
    fetch(`${base}/bills-sponsored`).then(res => res.json()).then(setBillsSponsored)
    fetch(`${base}/bills-cosponsored`).then(res => res.json()).then(setBillsCosponsored)
    fetch('http://localhost:8080/api/policy-areas').then(res => res.json()).then(setPolicyAreas)
  }, [bioguideId])

  useEffect(() => {
    const url = selectedPolicyArea
      ? `http://localhost:8080/api/members/${bioguideId}/votes-by-issue?policyArea=${encodeURIComponent(selectedPolicyArea)}`
      : `http://localhost:8080/api/members/${bioguideId}/votes`
    fetch(url).then(res => res.json()).then(setVotingHistory)
  }, [bioguideId, selectedPolicyArea])

  if (!member) return <div className="text-slate-500">Loading...</div>

  const SectionCard = ({ title, children, note }) => (
    <div className="bg-white border border-slate-200 rounded-xl p-5 mb-6">
      <h2 className="text-base font-semibold text-navy-700 mb-1">{title}</h2>
      {note && <p className="text-xs text-slate-500 italic mb-3">{note}</p>}
      {children}
    </div>
  )

  return (
    <div>
      {/* Header */}
      <div className="flex items-center gap-5 mb-8">
        <img
          src={member.headshotUrl}
          alt={member.lastName}
          className="w-24 h-24 rounded-full object-cover border-2 border-navy-700"
        />
        <div>
          <h1 className="text-2xl font-bold text-navy-700">{member.firstName} {member.lastName}</h1>
          <div className="flex items-center gap-2 mt-1">
            <PartyBadge party={member.party} />
            <span className="text-sm text-slate-600">
              {member.state}{member.district ? `-${member.district}` : ''} · {member.chamber}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">Term start: {member.termStartDate}</p>
        </div>
      </div>

      {/* Stat row */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 mb-6">
        {attendance && (
          <div className="bg-navy-700 rounded-xl p-4">
            <p className="text-xs text-slate-300 mb-1">Attendance (Congress {attendance.congress})</p>
            <p className="text-2xl font-bold text-gold-500">{attendance.attendancePercentage}%</p>
            <p className="text-xs text-slate-400 mt-1">{attendance.missedVotes} missed of {attendance.totalVotes}</p>
          </div>
        )}
        {partisanScore && (
          <div className="bg-navy-700 rounded-xl p-4">
            <p className="text-xs text-slate-300 mb-1">Partisan Score</p>
            <p className="text-2xl font-bold text-gold-500">{partisanScore.partisanScorePercentage}%</p>
            <p className="text-xs text-slate-400 mt-1">{partisanScore.votesWithPartyMajority} of {partisanScore.votesConsidered} votes</p>
          </div>
        )}
        {member.nominateDim1 != null && (
          <div className="bg-navy-700 rounded-xl p-4">
            <p className="text-xs text-slate-300 mb-1">DW-NOMINATE Score</p>
            <p className="text-2xl font-bold text-gold-500">{member.nominateDim1}</p>
            <p className="text-xs text-slate-400 mt-1">Liberal (-1) to Conservative (+1)</p>
          </div>
        )}
      </div>

      <SectionCard title="Committees">
        <ul className="space-y-1.5 text-sm text-slate-700">
          {committees.map(c => (
            <li key={c.id}>{c.committee.name} {c.title && <span className="text-gold-600 font-medium">— {c.title}</span>}</li>
          ))}
        </ul>
      </SectionCard>

      <SectionCard
        title={`Bills Sponsored (${billsSponsored.length})`}
        note="Reflects bills that have shown legislative progress; does not include bills introduced but never acted upon."
      >
        <ul className="space-y-1.5">
          {billsSponsored.slice(0, 10).map(b => (
            <li key={b.id}>
              <Link to={`/bills/${b.id}`} className="text-sm text-navy-700 hover:text-gold-600 hover:underline">{b.title}</Link>
            </li>
          ))}
        </ul>
      </SectionCard>

      <SectionCard
        title={`Bills Cosponsored (${billsCosponsored.length})`}
        note="Also reflects only bills that have shown legislative progress."
      >
        <ul className="space-y-1.5">
          {billsCosponsored.slice(0, 10).map(bc => (
            <li key={bc.bill.id}>
              <Link to={`/bills/${bc.bill.id}`} className="text-sm text-navy-700 hover:text-gold-600 hover:underline">{bc.bill.title}</Link>
            </li>
          ))}
        </ul>
      </SectionCard>

      <SectionCard title="Voting History">
        <div className="flex flex-wrap gap-3 mb-4">
          <select
            value={selectedPolicyArea}
            onChange={e => setSelectedPolicyArea(e.target.value)}
            className="text-sm border border-slate-300 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-gold-500"
          >
            <option value="">All Issues</option>
            {policyAreas.map(area => <option key={area} value={area}>{area}</option>)}
          </select>

          <select
            value={selectedCategory}
            onChange={e => setSelectedCategory(e.target.value)}
            className="text-sm border border-slate-300 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-gold-500"
          >
            <option value="">All Vote Types</option>
            <option value="Final Passage">Final Passage</option>
            <option value="Amendment">Amendment</option>
            <option value="Procedural/Other">Procedural/Other</option>
          </select>
        </div>

        <ul className="divide-y divide-slate-100">
          {votingHistory
            .filter(v => !selectedCategory || categorizeVote(v) === selectedCategory)
            .slice(0, 15)
            .map(v => (
              <li key={v.voteId} className="py-2.5">
                <Link to={`/votes/${v.voteId}`} className="text-sm font-medium text-navy-700 hover:text-gold-600">
                  {v.voteDate?.split('T')[0]} — <TruncatedText text={v.voteQuestion} limit={80} />
                </Link>
                <span className={`ml-2 text-xs font-semibold ${v.position === 'Yea' || v.position === 'Aye' ? 'text-green-600' : v.position === 'Nay' || v.position === 'No' ? 'text-red-600' : 'text-slate-400'}`}>
                  {v.position}
                </span>
                {v.billTitle && <p className="text-xs text-slate-500 mt-0.5">{v.billTitle}</p>}
                {explainAction(v.voteQuestion) && <p className="text-xs text-slate-400 italic mt-0.5">{explainAction(v.voteQuestion)}</p>}
              </li>
          ))}
        </ul>
      </SectionCard>
    </div>
  )
}

export default MemberProfile