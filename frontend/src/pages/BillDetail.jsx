import { useParams } from 'react-router-dom'

function BillDetail() {
  const { billId } = useParams()
  return <h1>Bill Detail: {billId}</h1>
}
export default BillDetail