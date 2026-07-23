import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import Home from './pages/Home'
import House from './pages/House'
import Senate from './pages/Senate'
import MemberProfile from './pages/MemberProfile'
import Bills from './pages/Bills'
import BillDetail from './pages/BillDetail'
import VoteDetail from './pages/VoteDetail'
import StateMembers from './pages/StateMembers'

function App() {
  return (
    <BrowserRouter>
      <nav>
        <Link to="/">Home</Link> | <Link to="/house">House</Link> | <Link to="/senate">Senate</Link> | <Link to="/bills">Bills</Link>
      </nav>

      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/house" element={<House />} />
        <Route path="/senate" element={<Senate />} />
        <Route path="/members/:bioguideId" element={<MemberProfile />} />
        <Route path="/bills" element={<Bills />} />
        <Route path="/bills/:billId" element={<BillDetail />} />
        <Route path="/votes/:voteId" element={<VoteDetail />} />
        <Route path="/states/:stateCode" element={<StateMembers />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App