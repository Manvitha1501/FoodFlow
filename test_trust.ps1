# =============================================================
# FOODFLOW MODULE 9 AUTOMATED TRUST & RELIABILITY INTELLIGENCE SUITE
# =============================================================

$BaseUrl = "http://localhost:8081"
$Headers = @{
    "Content-Type" = "application/json"
}

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
        Write-Host "Please start it before executing integration tests." -ForegroundColor Yellow
        exit 1
    }
}

# DYNAMIC UNIQUE USER REGISTRATIONS
$Rand = Get-Random -Minimum 10000 -Maximum 99999
$RestEmail = "owner-trust-$Rand@greenbistro.com"
$NgoEmail = "charity-trust-$Rand@foodflow.org"
$AdminEmail = "admin-trust-$Rand@foodflow.com"
$Password = "password123"
$UniqueFoodType = "TRUST_FOOD_$Rand"

# 1. Register Restaurant Owner
Print-Header "1. Registering Restaurant Owner ($RestEmail)..."
$RestReg = @{
    email = $RestEmail
    password = $Password
    fullName = "Bistro Trust Owner"
    phoneNumber = "9876543241"
    role = "RESTAURANT"
    restaurantDetails = @{
        restaurantName = "Bistro Trust Kitchen"
        address = "200 Eco Avenue, New Delhi"
        coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
        licenseNumber = "BISTRO-LIC-TRUST-$Rand"
    }
} | ConvertTo-Json -Depth 5

try {
    $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $RestReg -Headers $Headers
    Write-Host "Restaurant owner registered successfully!" -ForegroundColor Green
} catch {
    Write-Host "Restaurant owner registration check/skipped: $_" -ForegroundColor Gray
}

# 2. Register NGO
Print-Header "2. Registering NGO ($NgoEmail)..."
$NgoReg = @{
    email = $NgoEmail
    password = $Password
    fullName = "Hope Trust Shelter"
    phoneNumber = "9876543242"
    role = "NGO"
    ngoDetails = @{
        ngoName = "Care Trust NGO"
        address = "210 Hope Street, New Delhi"
        coordinates = @{ latitude = 28.6150; longitude = 77.2100 }
        capacity = 500
        storageType = "COLD"
        registrationNumber = "NGO-REG-TRUST-$Rand"
    }
} | ConvertTo-Json -Depth 5

try {
    $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $NgoReg -Headers $Headers
    Write-Host "NGO registered successfully!" -ForegroundColor Green
} catch {
    Write-Host "NGO registration check/skipped: $_" -ForegroundColor Gray
}

# 3. Register Admin User
Print-Header "3. Registering Admin ($AdminEmail)..."
$AdminReg = @{
    email = $AdminEmail
    password = $Password
    fullName = "Admin Trust User"
    phoneNumber = "9876543243"
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

$AdminLogin = @{ email = $AdminEmail; password = $Password } | ConvertTo-Json
$AdminToken = (Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $AdminLogin -Headers $Headers).token
$AdminHeaders = @{ "Authorization" = "Bearer $AdminToken"; "Content-Type" = "application/json" }
Write-Host "Access tokens acquired successfully!" -ForegroundColor Green

# GET NGO PROFILE ID AND VERIFY IT
Print-Header "5. Updating NGO details and verifying NGO via Admin..."
$NgoProfile = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method GET -Headers $NgoHeaders
$NgoProfileId = $NgoProfile.id
$NgoUpdateBody = @{
    ngoName = "Care Trust NGO"
    contactPerson = "Trust Representative"
    email = $NgoEmail
    phoneNumber = "9876543242"
    address = "210 Hope Street, New Delhi"
    city = "New Delhi"
    state = "Delhi"
    pincode = "110001"
    coordinates = @{ latitude = 28.6150; longitude = 77.2100 }
    capacity = 500
    storageType = "COLD"
    operatingHours = "08:00-22:00"
    acceptedFoodTypes = @("VEG", $UniqueFoodType)
    registrationNumber = "NGO-REG-TRUST-$Rand"
} | ConvertTo-Json

$null = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method PUT -Body $NgoUpdateBody -Headers $NgoHeaders
$VerifyNgo = Invoke-RestMethod -Uri "$BaseUrl/api/admin/ngo/verify/$NgoProfileId" -Method PUT -Headers $AdminHeaders
Write-Host "NGO Verified: $($VerifyNgo.verificationStatus)" -ForegroundColor Green

# GET RESTAURANT PROFILE ID
$RestProfile = Invoke-RestMethod -Uri "$BaseUrl/api/restaurant/profile" -Method GET -Headers $RestHeaders
$RestProfileId = $RestProfile.id

# =============================================================
# E2E RUNNING MODULE 9 TRUST ENGINE TEST SCENARIOS
# =============================================================

# Test 1: Fetch Initial Restaurant Trust Score
Print-Header "Test 1: GET /api/trust/restaurant/{restaurantId} (Initial Restaurant Score)"
$T1 = Invoke-RestMethod -Uri "$BaseUrl/api/trust/restaurant/$RestProfileId" -Method GET -Headers $RestHeaders
Write-Host "Passed! Final Score: $($T1.finalTrustScore), Trust Level: $($T1.trustLevel)" -ForegroundColor Green
if ($null -eq $T1.finalTrustScore) { Write-Error "Test 1 Failed"; exit 1 }

# Test 2: Fetch Initial NGO Trust Score
Print-Header "Test 2: GET /api/trust/ngo/{ngoId} (Initial NGO Score)"
$T2 = Invoke-RestMethod -Uri "$BaseUrl/api/trust/ngo/$NgoProfileId" -Method GET -Headers $NgoHeaders
Write-Host "Passed! Final Score: $($T2.finalTrustScore), Trust Level: $($T2.trustLevel)" -ForegroundColor Green
if ($null -eq $T2.finalTrustScore) { Write-Error "Test 2 Failed"; exit 1 }

# Test 3: Complete Workflow to Trigger Automatic Event-Based Recalculation
Print-Header "Test 3: Triggering Workflow Completion -> Auto Event Recalculation"
$DonationPayload = @{
    foodName = "Trust Test Meal Tray"
    foodCategory = "VEG"
    foodType = $UniqueFoodType
    quantity = 250.0
    quantityUnit = "meals"
    preparedTime = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    expiryTime = (Get-Date).AddHours(4).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    storageCondition = "REFRIGERATED"
    packagingType = "BOX"
    foodTemperature = "HOT"
    pickupAddress = "200 Eco Avenue, New Delhi"
    city = "New Delhi"
    state = "Delhi"
    pincode = "110001"
    coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
    foodImages = @("http://example.com/image.jpg")
} | ConvertTo-Json

$DonationResp = Invoke-RestMethod -Uri "$BaseUrl/api/donations" -Method POST -Body $DonationPayload -Headers $RestHeaders
$DonationId = $DonationResp.id
$PublishResp = Invoke-RestMethod -Uri "$BaseUrl/api/donations/$DonationId/publish" -Method PUT -Headers $RestHeaders

$MatchingResp = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method POST -Headers $RestHeaders
$AllocReq = @{ algorithm = "GREEDY" } | ConvertTo-Json
$AllocResp = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$DonationId" -Method POST -Body $AllocReq -Headers $RestHeaders
$AllocationId = $AllocResp.id

# Execute Workflow sequence
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/accept" -Method POST -Headers $NgoHeaders
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/confirm?targetNgoId=$NgoProfileId" -Method POST -Headers $RestHeaders
$SchedReq = @{ pickupScheduledTime = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss.fffZ") } | ConvertTo-Json
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/schedule-pickup?targetNgoId=$NgoProfileId" -Method POST -Body $SchedReq -Headers $RestHeaders
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/pickup-start" -Method POST -Headers $NgoHeaders
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/pickup-complete" -Method POST -Headers $NgoHeaders

$DelivReq = @{ completionRemarks = "Trust test delivery complete" } | ConvertTo-Json
$WfComplete = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/delivery-complete" -Method POST -Body $DelivReq -Headers $NgoHeaders
Write-Host "Workflow COMPLETED successfully! Triggering Event-Driven Trust Recalculation..." -ForegroundColor Green

# Test 4: Fetch Updated Restaurant Trust Score
Print-Header "Test 4: GET /api/trust/restaurant/{restaurantId} (Post-Workflow Auto Recalculated Score)"
Start-Sleep -Seconds 1
$T4 = Invoke-RestMethod -Uri "$BaseUrl/api/trust/restaurant/$RestProfileId" -Method GET -Headers $RestHeaders
Write-Host "Passed! Total Allocations: $($T4.totalAllocations), Final Score: $($T4.finalTrustScore)" -ForegroundColor Green
if ($T4.totalAllocations -lt 1) { Write-Error "Test 4 Failed"; exit 1 }

# Test 5: Fetch Updated NGO Trust Score
Print-Header "Test 5: GET /api/trust/ngo/{ngoId} (Post-Workflow Auto Recalculated Score)"
$T5 = Invoke-RestMethod -Uri "$BaseUrl/api/trust/ngo/$NgoProfileId" -Method GET -Headers $NgoHeaders
Write-Host "Passed! Total Allocations: $($T5.totalAllocations), Final Score: $($T5.finalTrustScore)" -ForegroundColor Green
if ($T5.totalAllocations -lt 1) { Write-Error "Test 5 Failed"; exit 1 }

# Test 6: Fetch Explainable Trust Calculation Breakdown
Print-Header "Test 6: GET /api/trust/explanation/RESTAURANT/{restaurantId} (Explainable Trust Score)"
$Exp = Invoke-RestMethod -Uri "$BaseUrl/api/trust/explanation/RESTAURANT/$RestProfileId" -Method GET -Headers $RestHeaders
Write-Host "Passed! Explanation Bullets Count: $($Exp.explanationBullets.Count)" -ForegroundColor Green
Write-Host "Sample Bullet: $($Exp.explanationBullets[0])" -ForegroundColor Gray
if ($Exp.explanationBullets.Count -lt 1) { Write-Error "Test 6 Failed"; exit 1 }

# Test 7: Fetch Complete Trust History Log
Print-Header "Test 7: GET /api/trust/history/RESTAURANT/{restaurantId} (Trust History Audit Entries)"
$Hist = Invoke-RestMethod -Uri "$BaseUrl/api/trust/history/RESTAURANT/$RestProfileId" -Method GET -Headers $RestHeaders
Write-Host "Passed! Trust History Entries Count: $($Hist.Count)" -ForegroundColor Green
if ($Hist.Count -lt 1) { Write-Error "Test 7 Failed"; exit 1 }

# Test 8: Fetch Restaurant Leaderboard
Print-Header "Test 8: GET /api/trust/leaderboard/restaurants (Restaurant Leaderboard)"
$LdRest = Invoke-RestMethod -Uri "$BaseUrl/api/trust/leaderboard/restaurants" -Method GET -Headers $RestHeaders
Write-Host "Passed! Total Entities Ranked: $($LdRest.totalEntities)" -ForegroundColor Green
if ($LdRest.totalEntities -lt 1) { Write-Error "Test 8 Failed"; exit 1 }

# Test 9: Fetch NGO Leaderboard
Print-Header "Test 9: GET /api/trust/leaderboard/ngos (NGO Leaderboard)"
$LdNgo = Invoke-RestMethod -Uri "$BaseUrl/api/trust/leaderboard/ngos" -Method GET -Headers $NgoHeaders
Write-Host "Passed! Total Entities Ranked: $($LdNgo.totalEntities)" -ForegroundColor Green
if ($LdNgo.totalEntities -lt 1) { Write-Error "Test 9 Failed"; exit 1 }

# Test 10: Force Admin Trust Recalculation
Print-Header "Test 10: POST /api/trust/recalculate/RESTAURANT/{restaurantId} (Admin Force Recalculate)"
$Recalc = Invoke-RestMethod -Uri "$BaseUrl/api/trust/recalculate/RESTAURANT/$RestProfileId" -Method POST -Headers $AdminHeaders
Write-Host "Passed! Recalculated Score: $($Recalc.newFinalScore), Message: $($Recalc.message)" -ForegroundColor Green
if ($null -eq $Recalc.newFinalScore) { Write-Error "Test 10 Failed"; exit 1 }

Write-Host "`n=============================================" -ForegroundColor Green
Write-Host "ALL MODULE 9 TRUST & RELIABILITY INTELLIGENCE TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
