import { useParams } from 'react-router-dom'

function MemberProfile() {
  const { bioguideId } = useParams()
  return <h1>Member Profile: {bioguideId}</h1>
}
export default MemberProfile