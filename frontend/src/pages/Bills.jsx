import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'

function Bills() {
  const [bills, setBills] = useState([])
  const [policyAreas, setPolicyAreas] = useState([])
  const [selectedPolicyArea, setSelectedPolicyArea] = useState('')
  const [searchQuery, setSearchQuery] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    fetch('http://localhost:8080/api/policy-areas').then(res => res.json()).then(setPolicyAreas)
  }, [])

  useEffect(() => {
    const params = new URLSearchParams({ page, size: 20 })
    if (selectedPolicyArea) params.set('policyArea', selectedPolicyArea)
    if (searchQuery.trim()) params.set('query', searchQuery.trim())

    fetch(`http://localhost:8080/api/bills?${params}`)
      .then(res => res.json())
      .then(data => {
        setBills(data.content)
        setTotalPages(data.totalPages)
      })
  }, [page, selectedPolicyArea, searchQuery])

  return (
    <div>
      <h1 className="text-2xl font-bold text-navy-700 mb-1">Bills</h1>
      <p className="text-xs text-slate-500 italic mb-5">
        Only shows bills that have shown legislative progress and received a recorded vote.{' '}
        <Link to="/about" className="text-gold-600 hover:underline">Learn more</Link>.
      </p>

      <div className="flex flex-wrap gap-3 mb-6">
        <input
          type="text"
          placeholder="Search bill titles..."
          value={searchQuery}
          onChange={e => { setSearchQuery(e.target.value); setPage(0) }}
          className="flex-1 min-w-[200px] px-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gold-500"
        />
        <select
          value={selectedPolicyArea}
          onChange={e => { setSelectedPolicyArea(e.target.value); setPage(0) }}
          className="px-3 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gold-500"
        >
          <option value="">All Issues</option>
          {policyAreas.map(area => <option key={area} value={area}>{area}</option>)}
        </select>
      </div>

      <div className="bg-white border border-slate-200 rounded-xl divide-y divide-slate-100">
        {bills.map(b => (
          <Link key={b.id} to={`/bills/${b.id}`} className="block p-4 hover:bg-slate-50 transition-colors">
            <p className="text-sm font-semibold text-navy-700">{b.title}</p>
            <p className="text-xs text-slate-500 mt-0.5">
              {b.billType.toUpperCase()} {b.billNumber} · {b.policyArea}
              {b.sponsorName && ` · Sponsored by ${b.sponsorName}`}
            </p>
          </Link>
        ))}
      </div>

      <div className="flex items-center justify-center gap-4 mt-6">
        <button
          disabled={page === 0}
          onClick={() => setPage(p => p - 1)}
          className="px-4 py-1.5 text-sm font-medium bg-navy-700 text-white rounded-lg disabled:opacity-40 disabled:cursor-not-allowed hover:bg-navy-600"
        >
          Previous
        </button>
        <span className="text-sm text-slate-600">Page {page + 1} of {totalPages}</span>
        <button
          disabled={page >= totalPages - 1}
          onClick={() => setPage(p => p + 1)}
          className="px-4 py-1.5 text-sm font-medium bg-navy-700 text-white rounded-lg disabled:opacity-40 disabled:cursor-not-allowed hover:bg-navy-600"
        >
          Next
        </button>
      </div>
    </div>
  )
}

export default Bills