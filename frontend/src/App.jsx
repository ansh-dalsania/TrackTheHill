import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import Home from './pages/Home'
import House from './pages/House'
import Senate from './pages/Senate'
import MemberProfile from './pages/MemberProfile'
import Bills from './pages/Bills'
import BillDetail from './pages/BillDetail'
import VoteDetail from './pages/VoteDetail'
import StateMembers from './pages/StateMembers'
import About from './pages/About'

function App() {
  return (
    <BrowserRouter>
      <nav className="bg-navy-700 text-white px-6 py-4 flex items-center gap-6 shadow-md">
        <Link to="/" className="text-lg font-bold text-gold-500 hover:text-gold-400">
          Track The Hill
        </Link>
        <div className="flex gap-5 text-sm font-medium">
          <Link to="/house" className="text-slate-200 hover:text-gold-500 transition-colors">House</Link>
          <Link to="/senate" className="text-slate-200 hover:text-gold-500 transition-colors">Senate</Link>
          <Link to="/bills" className="text-slate-200 hover:text-gold-500 transition-colors">Bills</Link>
          <Link to="/about" className="text-slate-200 hover:text-gold-500 transition-colors">About</Link>
        </div>
      </nav>

      <main className="max-w-5xl mx-auto px-6 py-8">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/house" element={<House />} />
          <Route path="/senate" element={<Senate />} />
          <Route path="/members/:bioguideId" element={<MemberProfile />} />
          <Route path="/bills" element={<Bills />} />
          <Route path="/bills/:billId" element={<BillDetail />} />
          <Route path="/votes/:voteId" element={<VoteDetail />} />
          <Route path="/states/:stateCode" element={<StateMembers />} />
          <Route path="/about" element={<About />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

export default App