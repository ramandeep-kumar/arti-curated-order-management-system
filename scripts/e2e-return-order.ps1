$base = 'http://localhost:8080'

$orderReq = @'
{
  "customerEmail": "e2e@test",
  "firstName": "John",
  "lastName": "Doe",
  "items": [ { "productName": "Widget", "price": 19.99, "quantity": 2 } ],
  "address": { "street": "1 Test St", "city": "City", "state": "ST", "zipCode": "00000", "country": "USA" }
}
'@

Try {
    $order = Invoke-RestMethod -Uri "$base/api/orders" -Method Post -Body $orderReq -ContentType 'application/json' -TimeoutSec 30
    Write-Output "ORDER_CREATED:$($order.id)"
} Catch {
    Write-Error "Create order failed: $_"
    Exit 2
}

    # Move order through lifecycle so it's eligible for return
    Try {
        Invoke-RestMethod -Method Post -Uri "$base/api/orders/$($order.id)/pay" -TimeoutSec 10
        Write-Output 'ORDER_PAID'
        Invoke-RestMethod -Method Post -Uri "$base/api/orders/$($order.id)/start-processing" -TimeoutSec 10
        Write-Output 'ORDER_PROCESSING'
        Invoke-RestMethod -Method Post -Uri "$base/api/orders/$($order.id)/ship" -TimeoutSec 10
        Write-Output 'ORDER_SHIPPED'
        Invoke-RestMethod -Method Post -Uri "$base/api/orders/$($order.id)/deliver" -TimeoutSec 10
        Write-Output 'ORDER_DELIVERED'
    } Catch { Write-Error "Order lifecycle transition failed: $_"; Exit 8 }

    Start-Sleep -Seconds 1

    $retReq = @{ orderId = $order.id; reason = 'defect' } | ConvertTo-Json
Try {
    $ret = Invoke-RestMethod -Uri "$base/api/returns" -Method Post -Body $retReq -ContentType 'application/json' -TimeoutSec 30
    Write-Output "RETURN_CREATED:$($ret.id)"
} Catch {
    Write-Error "Create return failed: $_"
    Exit 3
}

Try {
    Invoke-RestMethod -Method Put -Uri "$base/api/returns/$($ret.id)/approve?approvedBy=manager" -TimeoutSec 15
    Write-Output 'APPROVED'
} Catch { Write-Error "Approve failed: $_"; Exit 4 }

Try {
    Invoke-RestMethod -Method Put -Uri "$base/api/returns/$($ret.id)/in-transit?trackingNumber=TRK-1" -TimeoutSec 15
    Write-Output 'IN_TRANSIT'
} Catch { Write-Error "In-transit failed: $_"; Exit 5 }

Try {
    Invoke-RestMethod -Method Put -Uri "$base/api/returns/$($ret.id)/received" -TimeoutSec 15
    Write-Output 'RECEIVED'
} Catch { Write-Error "Received failed: $_"; Exit 6 }

Try {
    Invoke-RestMethod -Method Put -Uri "$base/api/returns/$($ret.id)/complete" -TimeoutSec 15
    Write-Output 'COMPLETED'
} Catch { Write-Error "Complete failed: $_"; Exit 7 }

Start-Sleep -Seconds 2

docker logs articurated-app --tail 300 | Select-String 'process refund|ProcessRefund|sendRefundProcessingMessage' -CaseSensitive:$false
