import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { State } from 'react-stateface'

function Home() {
  const [states, setStates] = useState([])
  const navigate = useNavigate()

  useEffect(() => {
    fetch('http://localhost:8080/api/states')
      .then(res => res.json())
      .then(data => setStates(data))
      .catch(err => console.error('Failed to load states:', err))
  }, [])

  return (
    <div>
      <h1 className="text-3xl font-bold text-navy-700 mb-2">Track The Hill</h1>
      <p className="text-slate-600 mb-8">Select your state to see your representatives</p>

      <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 lg:grid-cols-8 gap-3">
        {states.map(state => (
          <button
            key={state.code}
            onClick={() => navigate(`/states/${state.code}`)}
            className="flex flex-col items-center gap-2 p-3 bg-white border border-slate-200 rounded-xl hover:border-gold-500 hover:shadow-md transition-all"
          >
            <div className="w-12 h-12 flex items-center justify-center [&_svg]:w-full [&_svg]:h-full [&_svg]:max-h-12 [&_svg]:fill-navy-700">
              <State>{state.code}</State>
            </div>
            <span className="text-xs font-medium text-slate-700 text-center">{state.name}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

export default Home