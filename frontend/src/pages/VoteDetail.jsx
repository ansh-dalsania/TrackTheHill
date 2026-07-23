import { useParams } from 'react-router-dom'

function VoteDetail() {
  const { voteId } = useParams()
  return <h1>Vote Detail: {voteId}</h1>
}
export default VoteDetail