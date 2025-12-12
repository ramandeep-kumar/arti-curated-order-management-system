$base='http://localhost:8080'
function PostJson($url, $body) {
  try {
    return Invoke-RestMethod -Uri $url -Method Post -Body $body -ContentType 'application/json' -TimeoutSec 30 -ErrorAction Stop
  } catch {
    Write-Host "ERROR: POST $url failed: $($_.Exception.Message)"
    exit 1
  }
}

try {
  $orderBody = @'
{
  "customerEmail": "e2e@postman",
  "firstName": "E2E",
  "lastName": "User",
  "items": [
    { "productName": "Cup", "price": 5, "quantity": 1 }
  ],
  "address": { "street": "1 Test", "city": "City", "state": "ST", "zipCode": "00000", "country": "USA" }
}
'@

  $order = PostJson "$base/api/orders" $orderBody
  Write-Host "ORDER_CREATED:$($order.id)"

  Invoke-RestMethod -Uri "$base/api/orders/$($order.id)/pay" -Method Post -ErrorAction Stop
  Write-Host 'ORDER_PAID'

  Invoke-RestMethod -Uri "$base/api/orders/$($order.id)/start-processing" -Method Post -ErrorAction Stop
  Write-Host 'ORDER_PROCESSING'

  Invoke-RestMethod -Uri "$base/api/orders/$($order.id)/ship" -Method Post -ErrorAction Stop
  Write-Host 'ORDER_SHIPPED'

  Invoke-RestMethod -Uri "$base/api/orders/$($order.id)/deliver" -Method Post -ErrorAction Stop
  Write-Host 'ORDER_DELIVERED'

  Start-Sleep -Seconds 1

  $retReq = @{ orderId = $order.id; reason = 'defect' } | ConvertTo-Json
  $ret = PostJson "$base/api/returns" $retReq
  Write-Host "RETURN_CREATED:$($ret.id)"

  Invoke-RestMethod -Uri "$base/api/returns/$($ret.id)/approve?approvedBy=manager" -Method Put -ErrorAction Stop
  Write-Host 'APPROVED'

  Invoke-RestMethod -Uri "$base/api/returns/$($ret.id)/in-transit?trackingNumber=TRK-1" -Method Put -ErrorAction Stop
  Write-Host 'IN_TRANSIT'

  Invoke-RestMethod -Uri "$base/api/returns/$($ret.id)/received" -Method Put -ErrorAction Stop
  Write-Host 'RECEIVED'

  Invoke-RestMethod -Uri "$base/api/returns/$($ret.id)/complete" -Method Put -ErrorAction Stop
  Write-Host 'COMPLETED'

  Write-Host 'E2E_SUCCESS'
  exit 0
} catch {
  Write-Host "ERROR: $($_.Exception.Message)"
  exit 1
}
