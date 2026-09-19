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
    fetch('http://localhost:8080/api/policy-areas')
      .then(res => res.json())
      .then(setPolicyAreas)
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
      <h1>Bills</h1>
      <p><em>Only shows bills that have shown legislative progress. <Link to="/about">Learn more</Link>.</em></p>

      <input
        type="text"
        placeholder="Search bill titles..."
        value={searchQuery}
        onChange={e => { setSearchQuery(e.target.value); setPage(0) }}
      />

      <label>
        {' '}Filter by issue:{' '}
        <select value={selectedPolicyArea} onChange={e => { setSelectedPolicyArea(e.target.value); setPage(0) }}>
          <option value="">All Issues</option>
          {policyAreas.map(area => (
            <option key={area} value={area}>{area}</option>
          ))}
        </select>
      </label>

      <ul>
        {bills.map(b => (
          <li key={b.id}>
            <Link to={`/bills/${b.id}`}>{b.title}</Link>
            {b.sponsorName && ` — sponsored by ${b.sponsorName}`}
          </li>
        ))}
      </ul>

      <div>
        <button disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
        {' '}Page {page + 1} of {totalPages}{' '}
        <button disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</button>
      </div>
    </div>
  )
}

export default Bills