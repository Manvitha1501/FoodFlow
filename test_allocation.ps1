# =============================================================
# FOODFLOW MODULE 7 AUTOMATED NGO ALLOCATION TESTING FLOW
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
        exit
    }
}

# DYNAMIC UNIQUE USER REGISTRATIONS
$Rand = Get-Random -Minimum 10000 -Maximum 99999
$RestEmail = "owner-alloc-$Rand@greenbistro.com"
$NgoEmail = "charity-alloc-$Rand@foodflow.org"
$AdminEmail = "admin-alloc-$Rand@foodflow.com"
$Password = "password123"
$UniqueFoodType = "SPECIAL_TYPE_$Rand"

# 1. Register Restaurant Owner with full details
Print-Header "1. Registering Restaurant Owner ($RestEmail)..."
$RestReg = @{
    email = $RestEmail
    password = $Password
    fullName = "Bistro Alloc Owner"
    phoneNumber = "9876543231"
    role = "RESTAURANT"
    restaurantDetails = @{
        restaurantName = "Green Bistro Alloc"
        address = "150 Eco Avenue, New Delhi"
        coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
        licenseNumber = "BISTRO-LIC-ALLOC-$Rand"
    }
} | ConvertTo-Json -Depth 5

try {
    $res = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $RestReg -Headers $Headers
    Write-Host "Restaurant owner registered successfully!" -ForegroundColor Green
} catch {
    Write-Host "Restaurant owner registration check/skipped: $_" -ForegroundColor Gray
}

# 2. Register NGO with full details
Print-Header "2. Registering NGO ($NgoEmail)..."
$NgoReg = @{
    email = $NgoEmail
    password = $Password
    fullName = "Hope Shelter NGO Alloc"
    phoneNumber = "9876543232"
    role = "NGO"
    ngoDetails = @{
        ngoName = "Care Share NGO Alloc"
        address = "160 Hope Street, New Delhi"
        coordinates = @{ latitude = 28.6150; longitude = 77.2100 }
        capacity = 500
        storageType = "COLD"
        registrationNumber = "NGO-REG-ALLOC-$Rand"
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
    fullName = "Admin Alloc User"
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
$RestTokenResp = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $RestLogin -Headers $Headers
$RestToken = $RestTokenResp.token
$RestHeaders = @{ "Authorization" = "Bearer $RestToken"; "Content-Type" = "application/json" }
Write-Host "Restaurant Token acquired: $RestToken" -ForegroundColor DarkGreen

$NgoLogin = @{ email = $NgoEmail; password = $Password } | ConvertTo-Json
$NgoTokenResp = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $NgoLogin -Headers $Headers
$NgoToken = $NgoTokenResp.token
$NgoHeaders = @{ "Authorization" = "Bearer $NgoToken"; "Content-Type" = "application/json" }
Write-Host "NGO Token acquired: $NgoToken" -ForegroundColor DarkGreen

$AdminLogin = @{ email = $AdminEmail; password = $Password } | ConvertTo-Json
$AdminTokenResp = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $AdminLogin -Headers $Headers
$AdminToken = $AdminTokenResp.token
$AdminHeaders = @{ "Authorization" = "Bearer $AdminToken"; "Content-Type" = "application/json" }
Write-Host "Admin Token acquired: $AdminToken" -ForegroundColor DarkGreen

# GET NGO PROFILE ID AND VERIFY IT
Print-Header "5. Fetching NGO Profile, updating details, and verifying NGO via Admin..."
$NgoProfile = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method GET -Headers $NgoHeaders
$NgoProfileId = $NgoProfile.id
Write-Host "NGO Profile ID: $NgoProfileId" -ForegroundColor Cyan

# Update NGO profile to make it completely valid/matching-eligible and uniquely accepting our custom type
$NgoUpdateBody = @{
    ngoName = "Care Share NGO Alloc"
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
    registrationNumber = "NGO-REG-ALLOC-$Rand"
} | ConvertTo-Json

$null = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method PUT -Body $NgoUpdateBody -Headers $NgoHeaders
Write-Host "NGO profile fields updated successfully!" -ForegroundColor Green

# Approve NGO Profile via Admin
$VerifyNgo = Invoke-RestMethod -Uri "$BaseUrl/api/admin/ngo/verify/$NgoProfileId" -Method PUT -Headers $AdminHeaders
Write-Host "NGO Profile Approved: Status = $($VerifyNgo.verificationStatus)" -ForegroundColor Green

# 6. CREATE & PUBLISH DONATION
Print-Header "6. Creating and publishing donation..."
$DonationPayload = @{
    foodName = "Surplus Rice Allocation Bowl"
    foodCategory = "VEG"
    foodType = $UniqueFoodType
    quantity = 400.0
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
Write-Host "Donation created in DRAFT. ID: $DonationId" -ForegroundColor Cyan

# Publish Donation
$PublishResp = Invoke-RestMethod -Uri "$BaseUrl/api/donations/$DonationId/publish" -Method PUT -Headers $RestHeaders
Write-Host "Donation PUBLISHED successfully. Status: $($PublishResp.status)" -ForegroundColor Green

# 7. GENERATE MATCHING RUN
Print-Header "7. Generating Matching run for donation..."
$MatchingResp = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method POST -Headers $RestHeaders
Write-Host "Matching run completed. Ranked NGOs count: $($MatchingResp.rankedNGOs.Count)" -ForegroundColor Green
$MatchingResp.rankedNGOs | ForEach-Object {
    Write-Host "NGO: $($_.ngoName) (Score: $($_.matchingScore)%, Rank: $($_.rank))" -ForegroundColor Gray
}

# =============================================================
# E2E RUNNING ALLOCATION TEST SCENARIOS
# =============================================================

# Test 1: Generate splitting allocation (GREEDY success)
Print-Header "Test 1: POST /api/allocation/{donationId} (GREEDY success)"
$AllocReq = @{ algorithm = "GREEDY" } | ConvertTo-Json
$AllocResp = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$DonationId" -Method POST -Body $AllocReq -Headers $RestHeaders
Write-Host "Allocation generated successfully!" -ForegroundColor Green
Write-Host "Allocation ID: $($AllocResp.id)" -ForegroundColor Cyan
Write-Host "Status: $($AllocResp.allocationStatus)" -ForegroundColor Cyan
Write-Host "Allocations Splits count: $($AllocResp.allocations.Count)" -ForegroundColor Cyan
$AllocId = $AllocResp.id

if ($AllocResp.allocationStatus -ne "FULLY_ALLOCATED" -and $AllocResp.allocationStatus -ne "PARTIALLY_ALLOCATED") {
    Write-Error "Test 1 Failed: Unexpected status $($AllocResp.allocationStatus)"
    exit 1
}

# Test 2: GET /api/allocation/{allocationId} (Restaurant View - Success)
Print-Header "Test 2: GET /api/allocation/{allocationId} (Restaurant View)"
$GetAlloc = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$AllocId" -Method GET -Headers $RestHeaders
Write-Host "Fetched full allocation successfully. Remaining quantity: $($GetAlloc.remainingQuantity)" -ForegroundColor Green
if ($GetAlloc.id -ne $AllocId) {
    Write-Error "Test 2 Failed: ID mismatch"
    exit 1
}

# Test 3: GET /api/allocation/{allocationId} (NGO View - Blocked)
Print-Header "Test 3: GET /api/allocation/{allocationId} (NGO View - Should fail with 403)"
try {
    $fail = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$AllocId" -Method GET -Headers $NgoHeaders
    Write-Error "Test 3 Failed: NGO was able to access full allocation breakdown!"
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "Passed! NGO blocked with 403 Forbidden." -ForegroundColor Green
    } else {
        Write-Error "Test 3 Failed: Unexpected response status: $($_.Exception.Response.StatusCode)"
        exit 1
    }
}

# Test 4: GET /api/allocation/{allocationId}/my-portion (NGO View - Allowed)
Print-Header "Test 4: GET /api/allocation/{allocationId}/my-portion (NGO View - Allowed)"
try {
    $Portion = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$AllocId/my-portion" -Method GET -Headers $NgoHeaders
    Write-Host "Passed! NGO portion retrieved successfully:" -ForegroundColor Green
    Write-Host "NGO: $($Portion.ngoName)" -ForegroundColor Cyan
    Write-Host "Allocated Qty: $($Portion.allocatedQuantity)" -ForegroundColor Cyan
} catch {
    Write-Error "Test 4 Failed: NGO failed to fetch their portion. Error: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $respBody = $reader.ReadToEnd()
        Write-Host "Response Body: $respBody" -ForegroundColor Red
    }
    exit 1
}

# Test 5: GET /api/allocation/donation/{donationId} (Restaurant View - Success)
Print-Header "Test 5: GET /api/allocation/donation/{donationId} (Restaurant View)"
$GetAllocByDonation = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/donation/$DonationId" -Method GET -Headers $RestHeaders
Write-Host "Fetched allocation by donation successfully." -ForegroundColor Green
if ($GetAllocByDonation.id -ne $AllocId) {
    Write-Error "Test 5 Failed: ID mismatch"
    exit 1
}

# Test 6: GET /api/allocation/donation/{donationId} (NGO View - Blocked)
Print-Header "Test 6: GET /api/allocation/donation/{donationId} (NGO View - Should fail with 403)"
try {
    $fail = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/donation/$DonationId" -Method GET -Headers $NgoHeaders
    Write-Error "Test 6 Failed: NGO accessed full allocation breakdown!"
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "Passed! NGO blocked with 403 Forbidden." -ForegroundColor Green
    } else {
        Write-Error "Test 6 Failed: Unexpected response status: $($_.Exception.Response.StatusCode)"
        exit 1
    }
}

# Test 7: GET /api/allocation/donation/{donationId}/my-portion (NGO View - Allowed)
Print-Header "Test 7: GET /api/allocation/donation/{donationId}/my-portion (NGO View)"
try {
    $PortionByDonation = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/donation/$DonationId/my-portion" -Method GET -Headers $NgoHeaders
    Write-Host "Passed! NGO portion retrieved by donation reference successfully:" -ForegroundColor Green
    Write-Host "Allocated Qty: $($PortionByDonation.allocatedQuantity)" -ForegroundColor Cyan
} catch {
    Write-Error "Test 7 Failed: NGO failed to fetch their portion by donation reference. Error: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $respBody = $reader.ReadToEnd()
        Write-Host "Response Body: $respBody" -ForegroundColor Red
    }
    exit 1
}

# Test 8: GET /api/allocation/history (Restaurant history)
Print-Header "Test 8: GET /api/allocation/history (Restaurant View)"
$RestHistory = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/history" -Method GET -Headers $RestHeaders
Write-Host "Passed! Restaurant history fetched. Count: $($RestHistory.content.Count)" -ForegroundColor Green
if ($RestHistory.content.Count -eq 0) {
    Write-Error "Test 8 Failed: Expected history to have items"
    exit 1
}

# Test 9: GET /api/allocation/history (NGO history)
Print-Header "Test 9: GET /api/allocation/history (NGO View)"
$NgoHistory = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/history" -Method GET -Headers $NgoHeaders
Write-Host "Passed! NGO history fetched. Count: $($NgoHistory.content.Count)" -ForegroundColor Green
if ($NgoHistory.content.Count -eq 0) {
    Write-Error "Test 9 Failed: Expected NGO history to have items"
    exit 1
}

# Test 10: GET /api/allocation/history (Admin history)
Print-Header "Test 10: GET /api/allocation/history (Admin View)"
$AdminHistory = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/history" -Method GET -Headers $AdminHeaders
Write-Host "Passed! Admin history fetched. Count: $($AdminHistory.content.Count)" -ForegroundColor Green

# Test 11: POST /api/allocation/{donationId} (LP Strategy - Unimplemented 501 Block)
Print-Header "Test 11: POST /api/allocation/{donationId} (LP Strategy - Should fail with 501)"
$LpReq = @{ algorithm = "LINEAR_PROGRAMMING" } | ConvertTo-Json
try {
    $fail = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$DonationId" -Method POST -Body $LpReq -Headers $RestHeaders
    Write-Error "Test 11 Failed: LP strategy didn't throw unimplemented status!"
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 501) {
        Write-Host "Passed! LP Strategy blocked with 501 Not Implemented." -ForegroundColor Green
    } else {
        Write-Error "Test 11 Failed: Unexpected status: $($_.Exception.Response.StatusCode)"
        exit 1
    }
}

# Test 12: POST /api/allocation/{donationId} (Draft Donation - Should fail with 400)
Print-Header "Test 12: POST /api/allocation/{donationId} (Draft status check)"
# Create draft donation
$DraftResp = Invoke-RestMethod -Uri "$BaseUrl/api/donations" -Method POST -Body $DonationPayload -Headers $RestHeaders
$DraftId = $DraftResp.id
try {
    $fail = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$DraftId" -Method POST -Body $AllocReq -Headers $RestHeaders
    Write-Error "Test 12 Failed: Draft donation was allocated!"
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 400) {
        Write-Host "Passed! Draft donation blocked with 400 Bad Request." -ForegroundColor Green
    } else {
        Write-Error "Test 12 Failed: Unexpected status: $($_.Exception.Response.StatusCode)"
        exit 1
    }
}

# Test 13: POST /api/allocation/{donationId} (No Matching exists - Should fail with 404)
Print-Header "Test 13: POST /api/allocation/{donationId} (No Matching run exists)"
# Create another published donation but don't run matching
$NoMatchResp = Invoke-RestMethod -Uri "$BaseUrl/api/donations" -Method POST -Body $DonationPayload -Headers $RestHeaders
$NoMatchId = $NoMatchResp.id
$PublishNoMatch = Invoke-RestMethod -Uri "$BaseUrl/api/donations/$NoMatchId/publish" -Method PUT -Headers $RestHeaders

try {
    $fail = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$NoMatchId" -Method POST -Body $AllocReq -Headers $RestHeaders
    Write-Error "Test 13 Failed: Allocation triggered without matching results!"
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 404) {
        Write-Host "Passed! Blocked with 404 Matching Not Found." -ForegroundColor Green
    } else {
        Write-Error "Test 13 Failed: Unexpected status: $($_.Exception.Response.StatusCode)"
        exit 1
    }
}

# Test 14: POST /api/allocation/{donationId} (NGO Trigger Block - Should fail with 403)
Print-Header "Test 14: POST /api/allocation/{donationId} (NGO Trigger Block)"
try {
    $fail = Invoke-RestMethod -Uri "$BaseUrl/api/allocation/$DonationId" -Method POST -Body $AllocReq -Headers $NgoHeaders
    Write-Error "Test 14 Failed: NGO triggered allocation!"
    exit 1
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 403) {
        Write-Host "Passed! NGO blocked with 403 Forbidden." -ForegroundColor Green
    } else {
        Write-Error "Test 14 Failed: Unexpected status: $($_.Exception.Response.StatusCode)"
        exit 1
    }
}

Write-Host "`n=============================================" -ForegroundColor Green
Write-Host "ALL MODULE 7 NGO ALLOCATION TESTS PASSED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
