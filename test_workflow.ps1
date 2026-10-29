# =============================================================
# FOODFLOW MODULE 8 AUTOMATED DONATION PICKUP WORKFLOW TESTING SUITE
# =============================================================

$BaseUrl = "http://localhost:8081"
$Headers = @{
    "Content-Type" = "application/json"
}

# Helper to print separators
function Print-Header($title) {
    Write-Host "`n=============================================" -ForegroundColor Cyan
    Write-Host $title -ForegroundColor Yellow
    Write-Host "=============================================" -ForegroundColor Cyan
}

# Check if server is running
try {
    $testConn = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body '{}' -Headers $Headers -ErrorAction SilentlyContinue
} catch {
    if ($_.Exception.Message -like "*No connection could be made*") {
        Write-Host "ERROR: FoodFlow server is not running on $BaseUrl." -ForegroundColor Red
        Write-Host "Please start it using the correct command." -ForegroundColor Yellow
        exit 1
    }
}

# DYNAMIC UNIQUE USER REGISTRATIONS
$Rand = Get-Random -Minimum 10000 -Maximum 99999
$RestEmail = "owner-wf-$Rand@greenbistro.com"
$NgoEmail = "charity-wf-$Rand@foodflow.org"
$UnauthorizedNgoEmail = "other-ngo-$Rand@foodflow.org"
$AdminEmail = "admin-wf-$Rand@foodflow.com"
$Password = "password123"
$UniqueFoodType = "WORKFLOW_FOOD_$Rand"

# 1. Register Restaurant Owner
Print-Header "1. Registering Restaurant Owner ($RestEmail)..."
$RestReg = @{
    email = $RestEmail
    password = $Password
    fullName = "Bistro WF Owner"
    phoneNumber = "9876543231"
    role = "RESTAURANT"
    restaurantDetails = @{
        restaurantName = "Green Bistro WF"
        address = "150 Eco Avenue, New Delhi"
        coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
        licenseNumber = "BISTRO-LIC-WF-$Rand"
    }
} | ConvertTo-Json -Depth 5

try {
    $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $RestReg -Headers $Headers
    Write-Host "Restaurant owner registered successfully!" -ForegroundColor Green
} catch {
    Write-Host "Restaurant owner registration check/skipped: $_" -ForegroundColor Gray
}

# 2. Register Assigned NGO
Print-Header "2. Registering Assigned NGO ($NgoEmail)..."
$NgoReg = @{
    email = $NgoEmail
    password = $Password
    fullName = "Hope Shelter NGO WF"
    phoneNumber = "9876543232"
    role = "NGO"
    ngoDetails = @{
        ngoName = "Care Share NGO WF"
        address = "160 Hope Street, New Delhi"
        coordinates = @{ latitude = 28.6150; longitude = 77.2100 }
        capacity = 500
        storageType = "COLD"
        registrationNumber = "NGO-REG-WF-$Rand"
    }
} | ConvertTo-Json -Depth 5

try {
    $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $NgoReg -Headers $Headers
    Write-Host "Assigned NGO registered successfully!" -ForegroundColor Green
} catch {
    Write-Host "Assigned NGO registration check/skipped: $_" -ForegroundColor Gray
}

# 2b. Register Unauthorized NGO
$OtherNgoReg = @{
    email = $UnauthorizedNgoEmail
    password = $Password
    fullName = "Other Shelter NGO"
    phoneNumber = "9876543239"
    role = "NGO"
    ngoDetails = @{
        ngoName = "Other Unallocated NGO"
        address = "170 Hope Street, New Delhi"
        coordinates = @{ latitude = 28.6150; longitude = 77.2100 }
        capacity = 500
        storageType = "COLD"
        registrationNumber = "NGO-REG-UNAUTH-$Rand"
    }
} | ConvertTo-Json -Depth 5

try {
    $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $OtherNgoReg -Headers $Headers
    Write-Host "Unauthorized NGO registered successfully!" -ForegroundColor Green
} catch {
    Write-Host "Unauthorized NGO registration check/skipped: $_" -ForegroundColor Gray
}

# 3. Register Admin User
Print-Header "3. Registering Admin ($AdminEmail)..."
$AdminReg = @{
    email = $AdminEmail
    password = $Password
    fullName = "Admin WF User"
    phoneNumber = "9876543233"
    role = "ADMIN"
} | ConvertTo-Json -Depth 5

try {
    $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $AdminReg -Headers $Headers
    Write-Host "Admin registered successfully!" -ForegroundColor Green
} catch {
    Write-Host "Admin registration check/skipped: $_" -ForegroundColor Gray
}

# LOGINS & TOKEN RETRIEVALS
Print-Header "4. Logging in to retrieve access tokens..."
$RestLogin = @{ email = $RestEmail; password = $Password } | ConvertTo-Json
$RestToken = (Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $RestLogin -Headers $Headers).token
$RestHeaders = @{ "Authorization" = "Bearer $RestToken"; "Content-Type" = "application/json" }

$NgoLogin = @{ email = $NgoEmail; password = $Password } | ConvertTo-Json
$NgoToken = (Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $NgoLogin -Headers $Headers).token
$NgoHeaders = @{ "Authorization" = "Bearer $NgoToken"; "Content-Type" = "application/json" }

$OtherNgoLogin = @{ email = $UnauthorizedNgoEmail; password = $Password } | ConvertTo-Json
$OtherNgoToken = (Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $OtherNgoLogin -Headers $Headers).token
$OtherNgoHeaders = @{ "Authorization" = "Bearer $OtherNgoToken"; "Content-Type" = "application/json" }

$AdminLogin = @{ email = $AdminEmail; password = $Password } | ConvertTo-Json
$AdminToken = (Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $AdminLogin -Headers $Headers).token
$AdminHeaders = @{ "Authorization" = "Bearer $AdminToken"; "Content-Type" = "application/json" }
Write-Host "Access tokens acquired successfully!" -ForegroundColor Green

# GET NGO PROFILE ID AND VERIFY IT
Print-Header "5. Updating NGO details and verifying NGO via Admin..."
$NgoProfile = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method GET -Headers $NgoHeaders
$NgoProfileId = $NgoProfile.id
$NgoUpdateBody = @{
    ngoName = "Care Share NGO WF"
    contactPerson = "Ngo Representative"
    email = $NgoEmail
    phoneNumber = "9876543232"
    address = "160 Hope Street, New Delhi"
    city = "New Delhi"
    state = "Delhi"
    pincode = "110001"
    coordinates = @{ latitude = 28.6150; longitude = 77.2100 }
    capacity = 500
    storageType = "COLD"
    operatingHours = "08:00-22:00"
    acceptedFoodTypes = @("VEG", $UniqueFoodType)
    registrationNumber = "NGO-REG-WF-$Rand"
} | ConvertTo-Json

$null = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method PUT -Body $NgoUpdateBody -Headers $NgoHeaders
$VerifyNgo = Invoke-RestMethod -Uri "$BaseUrl/api/admin/ngo/verify/$NgoProfileId" -Method PUT -Headers $AdminHeaders
Write-Host "NGO Verified: $($VerifyNgo.verificationStatus)" -ForegroundColor Green

# 6. CREATE & PUBLISH DONATION
Print-Header "6. Creating and publishing donation..."
$DonationPayload = @{
    foodName = "Surplus Pasta WF Tray"
    foodCategory = "VEG"
    foodType = $UniqueFoodType
    quantity = 300.0
    quantityUnit = "meals"
    preparedTime = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    expiryTime = (Get-Date).AddHours(4).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    storageCondition = "REFRIGERATED"
    packagingType = "BOX"
    foodTemperature = "HOT"
    pickupAddress = "150 Eco Avenue, New Delhi"
    city = "New Delhi"
    state = "Delhi"
    pincode = "110001"
    coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
    foodImages = @("http://example.com/image.jpg")
} | ConvertTo-Json

$DonationResp = Invoke-RestMethod -Uri "$BaseUrl/api/donations" -Method POST -Body $DonationPayload -Headers $RestHeaders
$DonationId = $DonationResp.id
$PublishResp = Invoke-RestMethod -Uri "$BaseUrl/api/donations/$DonationId/publish" -Method PUT -Headers $RestHeaders
Write-Host "Donation Published. ID: $DonationId" -ForegroundColor Green

# 7. MATCHING & ALLOCATION RUN (MODULE 7)
Print-Header "7. Generating Matching and Allocation..."
$MatchingResp = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method POST -Headers $RestHeaders
$AllocReq = @{ algorithm = "GREEDY" } | ConvertTo-Json
$AllocResp = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$DonationId" -Method POST -Body $AllocReq -Headers $RestHeaders
$AllocationId = $AllocResp.id
$NgoId = $NgoProfileId
Write-Host "Allocation generated successfully. Allocation ID: $AllocationId" -ForegroundColor Green

# =============================================================
# E2E RUNNING MODULE 8 WORKFLOW TEST SCENARIOS
# =============================================================

# Test 1: NGO Accepts Allocation
Print-Header "Test 1: POST /api/workflow/{allocationId}/accept (NGO Accept)"
$Wf1 = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/accept" -Method POST -Headers $NgoHeaders
Write-Host "Passed! NGO Accepted Status: $($Wf1.currentStatus)" -ForegroundColor Green
if ($Wf1.currentStatus -ne "NGO_ACCEPTED") { Write-Error "Test 1 Failed"; exit 1 }

# Test 2: Idempotent Accept (Same NGO re-accepts)
Print-Header "Test 2: POST /api/workflow/{allocationId}/accept (Idempotent Re-Accept)"
$Wf2 = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/accept" -Method POST -Headers $NgoHeaders
Write-Host "Passed! Idempotent status returned: $($Wf2.currentStatus)" -ForegroundColor Green
if ($Wf2.currentStatus -ne "NGO_ACCEPTED") { Write-Error "Test 2 Failed"; exit 1 }

# Test 3: Unauthorized NGO Rejection
Print-Header "Test 3: POST /api/workflow/{allocationId}/accept (Unauthorized NGO Rejection)"
try {
    $fail = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/accept" -Method POST -Headers $OtherNgoHeaders
    Write-Error "Test 3 Failed: Unauthorized NGO was able to accept workflow!"
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "Passed! Unauthorized NGO blocked with 403 Forbidden." -ForegroundColor Green
    } else {
        Write-Error "Test 3 Failed: Unexpected status: $($_.Exception.Response.StatusCode)"
        exit 1
    }
}

# Test 4: Restaurant Confirms NGO
Print-Header "Test 4: POST /api/workflow/{allocationId}/confirm (Restaurant Confirm)"
$Wf4 = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/confirm?targetNgoId=$NgoId" -Method POST -Headers $RestHeaders
Write-Host "Passed! Status: $($Wf4.currentStatus)" -ForegroundColor Green
if ($Wf4.currentStatus -ne "RESTAURANT_CONFIRMED") { Write-Error "Test 4 Failed"; exit 1 }

# Test 5: Restaurant Schedules Pickup
Print-Header "Test 5: POST /api/workflow/{allocationId}/schedule-pickup (Restaurant Schedule)"
$FutureTime = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
$SchedReq = @{ pickupScheduledTime = $FutureTime } | ConvertTo-Json
$Wf5 = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/schedule-pickup?targetNgoId=$NgoId" -Method POST -Body $SchedReq -Headers $RestHeaders
Write-Host "Passed! Status: $($Wf5.currentStatus), Scheduled for: $($Wf5.pickupScheduledTime)" -ForegroundColor Green
if ($Wf5.currentStatus -ne "PICKUP_SCHEDULED") { Write-Error "Test 5 Failed"; exit 1 }

# Test 6: NGO Starts Pickup
Print-Header "Test 6: POST /api/workflow/{allocationId}/pickup-start (NGO Start Pickup)"
$Wf6 = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/pickup-start" -Method POST -Headers $NgoHeaders
Write-Host "Passed! Status: $($Wf6.currentStatus)" -ForegroundColor Green
if ($Wf6.currentStatus -ne "PICKUP_STARTED") { Write-Error "Test 6 Failed"; exit 1 }

# Test 7: NGO Completes Pickup
Print-Header "Test 7: POST /api/workflow/{allocationId}/pickup-complete (NGO Pickup Complete)"
$Wf7 = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/pickup-complete" -Method POST -Headers $NgoHeaders
Write-Host "Passed! Status: $($Wf7.currentStatus)" -ForegroundColor Green
if ($Wf7.currentStatus -ne "PICKED_UP") { Write-Error "Test 7 Failed"; exit 1 }

# Test 8: NGO Completes Delivery -> Auto COMPLETED
Print-Header "Test 8: POST /api/workflow/{allocationId}/delivery-complete (NGO Complete Delivery)"
$DelivReq = @{ completionRemarks = "Delivered safely to shelter" } | ConvertTo-Json
$Wf8 = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/delivery-complete" -Method POST -Body $DelivReq -Headers $NgoHeaders
Write-Host "Passed! Status: $($Wf8.currentStatus), Completed at: $($Wf8.completedAt)" -ForegroundColor Green
if ($Wf8.currentStatus -ne "COMPLETED") { Write-Error "Test 8 Failed"; exit 1 }

# Test 9: Invalid Status Transition Rejection on Terminal Workflow
Print-Header "Test 9: Invalid Status Transition on COMPLETED Workflow (Should fail 400)"
try {
    $fail = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/pickup-start" -Method POST -Headers $NgoHeaders
    Write-Error "Test 9 Failed: Terminal workflow allowed transition!"
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 400) {
        Write-Host "Passed! Blocked with 400 Bad Request." -ForegroundColor Green
    } else {
        Write-Error "Test 9 Failed: Unexpected status: $($_.Exception.Response.StatusCode)"
        exit 1
    }
}

# Test 10: Fetch Donation Aggregate View
Print-Header "Test 10: GET /api/workflow/donation/{donationId} (Restaurant Aggregate View)"
$Agg = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/donation/$DonationId" -Method GET -Headers $RestHeaders
Write-Host "Passed! Total Allocated Workflows: $($Agg.totalAllocatedNgos)" -ForegroundColor Green
if ($Agg.totalAllocatedNgos -lt 1) { Write-Error "Test 10 Failed"; exit 1 }

# Test 11: Fetch Workflow History Audit Entries
Print-Header "Test 11: GET /api/workflow/{allocationId}/history (Workflow Audit Log)"
$Hist = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/history" -Method GET -Headers $NgoHeaders
Write-Host "Passed! Audit entries count: $($Hist.Count)" -ForegroundColor Green
if ($Hist.Count -lt 5) { Write-Error "Test 11 Failed"; exit 1 }

Write-Host "`n=============================================" -ForegroundColor Green
Write-Host "ALL MODULE 8 DONATION PICKUP WORKFLOW TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
