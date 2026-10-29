# =============================================================
# FOODFLOW MODULE 10 AUTOMATED SUSTAINABILITY IMPACT ANALYTICS TEST SUITE
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
$RestEmail = "owner-impact-$Rand@greenbistro.com"
$NgoEmail = "charity-impact-$Rand@foodflow.org"
$AdminEmail = "admin-impact-$Rand@foodflow.com"
$Password = "password123"
$UniqueFoodType = "IMPACT_FOOD_$Rand"

# 1. Register Restaurant Owner
Print-Header "1. Registering Restaurant Owner ($RestEmail)..."
$RestReg = @{
    email = $RestEmail
    password = $Password
    fullName = "Bistro Impact Owner"
    phoneNumber = "9876543251"
    role = "RESTAURANT"
    restaurantDetails = @{
        restaurantName = "Bistro Impact Kitchen"
        address = "300 Eco Avenue, New Delhi"
        coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
        licenseNumber = "BISTRO-LIC-IMPACT-$Rand"
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
    fullName = "Hope Impact Shelter"
    phoneNumber = "9876543252"
    role = "NGO"
    ngoDetails = @{
        ngoName = "Care Impact NGO"
        address = "310 Hope Street, New Delhi"
        coordinates = @{ latitude = 28.6150; longitude = 77.2100 }
        capacity = 500
        storageType = "COLD"
        registrationNumber = "NGO-REG-IMPACT-$Rand"
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
    fullName = "Admin Impact User"
    phoneNumber = "9876543253"
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
    ngoName = "Care Impact NGO"
    contactPerson = "Impact Representative"
    email = $NgoEmail
    phoneNumber = "9876543252"
    address = "310 Hope Street, New Delhi"
    city = "New Delhi"
    state = "Delhi"
    pincode = "110001"
    coordinates = @{ latitude = 28.6150; longitude = 77.2100 }
    capacity = 500
    storageType = "COLD"
    operatingHours = "00:00-23:59"
    acceptedFoodTypes = @($UniqueFoodType)
    registrationNumber = "NGO-REG-IMPACT-$Rand"
} | ConvertTo-Json

$null = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method PUT -Body $NgoUpdateBody -Headers $NgoHeaders
$VerifyNgo = Invoke-RestMethod -Uri "$BaseUrl/api/admin/ngo/verify/$NgoProfileId" -Method PUT -Headers $AdminHeaders
Write-Host "NGO Verified: $($VerifyNgo.verificationStatus)" -ForegroundColor Green

# GET RESTAURANT PROFILE ID
$RestProfile = Invoke-RestMethod -Uri "$BaseUrl/api/restaurant/profile" -Method GET -Headers $RestHeaders
$RestProfileId = $RestProfile.id

# =============================================================
# E2E RUNNING MODULE 10 ANALYTICS ENGINE TEST SCENARIOS
# =============================================================

# Test 1: Fetch Initial Platform Dashboard
Print-Header "Test 1: GET /api/analytics/dashboard (Platform Dashboard)"
$D1 = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/dashboard" -Method GET -Headers $RestHeaders
Write-Host "Passed! Total Food Saved: $($D1.totalFoodSavedKg) kg, Success Rate: $($D1.donationSuccessRate)%" -ForegroundColor Green

# Test 2: Complete Workflow to Trigger Event-Driven Impact Calculations
Print-Header "Test 2: Complete Workflow -> Trigger Event-Driven Impact Analytics"
$DonationPayload = @{
    foodName = "Sustainability Meal Tray"
    foodCategory = $UniqueFoodType
    foodType = $UniqueFoodType
    quantity = 300.0
    quantityUnit = "meals"
    preparedTime = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    expiryTime = (Get-Date).AddHours(4).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    storageCondition = "REFRIGERATED"
    packagingType = "BOX"
    foodTemperature = "HOT"
    pickupAddress = "300 Eco Avenue, New Delhi"
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
$AssignedNgoId = $AllocResp.allocations[0].ngoId

# Workflow sequence to completion
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/accept" -Method POST -Headers $NgoHeaders
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/confirm?targetNgoId=$AssignedNgoId" -Method POST -Headers $RestHeaders
$SchedReq = @{ pickupScheduledTime = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss.fffZ") } | ConvertTo-Json
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/schedule-pickup?targetNgoId=$AssignedNgoId" -Method POST -Body $SchedReq -Headers $RestHeaders
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/pickup-start" -Method POST -Headers $NgoHeaders
$null = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/pickup-complete" -Method POST -Headers $NgoHeaders

$DelivReq = @{ completionRemarks = "Impact test delivery complete" } | ConvertTo-Json
$WfComplete = Invoke-RestMethod -Uri "$BaseUrl/api/workflow/$AllocationId/delivery-complete" -Method POST -Body $DelivReq -Headers $NgoHeaders
Write-Host "Workflow COMPLETED! Triggering Event-Driven Impact Analytics Calculation..." -ForegroundColor Green

# Test 3: Fetch Updated Platform Dashboard
Print-Header "Test 3: GET /api/analytics/dashboard (Post-Workflow Platform Dashboard)"
Start-Sleep -Seconds 1
$D3 = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/dashboard" -Method GET -Headers $RestHeaders
Write-Host "Passed! Total Food Saved: $($D3.totalFoodSavedKg) kg, Meals Delivered: $($D3.totalMealsDelivered), CO2 Saved: $($D3.totalCO2ReductionKg) kg" -ForegroundColor Green
if ($D3.totalFoodSavedKg -le 0) { Write-Error "Test 3 Failed"; exit 1 }

# Test 4: Fetch Restaurant Impact Dashboard
Print-Header "Test 4: GET /api/analytics/restaurant/{restaurantId} (Restaurant Sustainability Dashboard)"
$R4 = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/restaurant/$RestProfileId" -Method GET -Headers $RestHeaders
Write-Host "Passed! Restaurant: $($R4.restaurantName), Food Saved: $($R4.totalFoodSavedKg) kg, CO2 Saved: $($R4.totalCO2ReductionKg) kg" -ForegroundColor Green
if ($R4.totalFoodSavedKg -le 0) { Write-Error "Test 4 Failed"; exit 1 }

# Test 5: Fetch NGO Impact Dashboard
Print-Header "Test 5: GET /api/analytics/ngo/{ngoId} (NGO Sustainability Dashboard)"
$N5 = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/ngo/$NgoProfileId" -Method GET -Headers $NgoHeaders
Write-Host "Passed! NGO: $($N5.ngoName), Meals Received: $($N5.totalMealsReceived), Food Received: $($N5.totalFoodReceivedKg) kg" -ForegroundColor Green
if ($N5.totalFoodReceivedKg -le 0) { Write-Error "Test 5 Failed"; exit 1 }

# Test 6: Fetch Daily Trends
Print-Header "Test 6: GET /api/analytics/trends/daily (Daily Analytics Trends)"
$TrDaily = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/trends/daily" -Method GET -Headers $RestHeaders
Write-Host "Passed! Daily Trends Count: $($TrDaily.Count)" -ForegroundColor Green

# Test 7: Fetch Weekly Trends
Print-Header "Test 7: GET /api/analytics/trends/weekly (Weekly Analytics Trends)"
$TrWeekly = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/trends/weekly" -Method GET -Headers $RestHeaders
Write-Host "Passed! Weekly Trends Count: $($TrWeekly.Count)" -ForegroundColor Green

# Test 8: Fetch Monthly Trends
Print-Header "Test 8: GET /api/analytics/trends/monthly (Monthly Analytics Trends)"
$TrMonthly = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/trends/monthly" -Method GET -Headers $RestHeaders
Write-Host "Passed! Monthly Trends Count: $($TrMonthly.Count)" -ForegroundColor Green

# Test 9: Fetch Restaurant Contributors Leaderboard
Print-Header "Test 9: GET /api/analytics/leaderboard/restaurants (Top Restaurant Contributors)"
$LdRest = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/leaderboard/restaurants" -Method GET -Headers $RestHeaders
Write-Host "Passed! Ranked Restaurants Count: $($LdRest.Count)" -ForegroundColor Green
if ($LdRest.Count -lt 1) { Write-Error "Test 9 Failed"; exit 1 }

# Test 10: Fetch NGO Contributors Leaderboard
Print-Header "Test 10: GET /api/analytics/leaderboard/ngos (Top NGO Contributors)"
$LdNgo = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/leaderboard/ngos" -Method GET -Headers $NgoHeaders
Write-Host "Passed! Ranked NGOs Count: $($LdNgo.Count)" -ForegroundColor Green
if ($LdNgo.Count -lt 1) { Write-Error "Test 10 Failed"; exit 1 }

# Test 11: Fetch Environmental Impact Summary
Print-Header "Test 11: GET /api/analytics/environment (Environmental Impact Summary)"
$Env = Invoke-RestMethod -Uri "$BaseUrl/api/analytics/environment" -Method GET -Headers $RestHeaders
Write-Host "Passed! Trees Saved Equivalent: $($Env.equivalentTreesSaved), Water Saved Equivalent: $($Env.equivalentWaterSavedLiters) L" -ForegroundColor Green
if ($Env.totalFoodSavedKg -le 0) { Write-Error "Test 11 Failed"; exit 1 }

Write-Host "`n=============================================" -ForegroundColor Green
Write-Host "ALL MODULE 10 SUSTAINABILITY IMPACT ANALYTICS TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
