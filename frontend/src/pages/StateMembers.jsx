import { useParams } from 'react-router-dom'

function StateMembers() {
  const { stateCode } = useParams()
  return <h1>Members from {stateCode}</h1>
}

export default StateMembers