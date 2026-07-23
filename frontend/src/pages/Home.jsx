import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'

function Home() {
  const [states, setStates] = useState([])
  const [selectedState, setSelectedState] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    fetch('http://localhost:8080/api/states')
      .then(res => res.json())
      .then(data => setStates(data))
      .catch(err => console.error('Failed to load states:', err))
  }, [])

  const handleStateChange = (e) => {
    const stateCode = e.target.value
    setSelectedState(stateCode)
    if (stateCode) {
      navigate(`/states/${stateCode}`)
    }
  }

  return (
    <div>
      <h1>Track The Hill</h1>
      <p>Select your state to see your representatives:</p>
      <select value={selectedState} onChange={handleStateChange}>
        <option value="">-- Select a state --</option>
        {states.map(state => (
          <option key={state.code} value={state.code}>
            {state.name}
          </option>
        ))}
      </select>
    </div>
  )
}

export default Home