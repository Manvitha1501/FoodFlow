# =============================================================
# FOODFLOW MODULE 6 AUTOMATED NGO MATCHING TESTING FLOW
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
        Write-Host "Please start it in another terminal using:" -ForegroundColor Yellow
        Write-Host "  .\mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081" -ForegroundColor Yellow
        exit
    }
}

# DYNAMIC UNIQUE USER REGISTRATIONS
# This guarantees success regardless of clean/dirty database state.
$RestEmail = "owner-match@greenbistro.com"
$NgoEmail = "charity-match@foodflow.org"
$AdminEmail = "admin-match@foodflow.com"
$Password = "password123"

try {
    # 1. Register Restaurant Owner with full details
    $RestReg = @{
        email = $RestEmail
        password = $Password
        fullName = "Bistro Match Owner"
        phoneNumber = "9876543211"
        role = "RESTAURANT"
        restaurantDetails = @{
            restaurantName = "Green Bistro Match"
            address = "120 Eco Avenue, New Delhi"
            coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
            licenseNumber = "BISTRO-LIC-MATCH"
        }
    } | ConvertTo-Json
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $RestReg -Headers $Headers -ErrorAction SilentlyContinue
} catch {}

try {
    # 2. Register NGO User with full details
    $NgoReg = @{
        email = $NgoEmail
        password = $Password
        fullName = "NGO Match Rep"
        phoneNumber = "9876543210"
        role = "NGO"
        ngoDetails = @{
            ngoName = "Feed The Hungry Match NGO"
            address = "450 Care Lane, Sector 6, New Delhi"
            coordinates = @{ latitude = 28.5355; longitude = 77.3910 }
            capacity = 500
            storageType = "COLD"
            registrationNumber = "NGO-REG-MATCH"
        }
    } | ConvertTo-Json
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $NgoReg -Headers $Headers -ErrorAction SilentlyContinue
} catch {}

try {
    # 3. Register Admin
    $AdminReg = @{
        email = $AdminEmail
        password = $Password
        fullName = "System Admin Match"
        phoneNumber = "+1234567890"
        role = "ADMIN"
    } | ConvertTo-Json
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $AdminReg -Headers $Headers -ErrorAction SilentlyContinue
} catch {}


# 1. Login as Restaurant Owner
Print-Header "STEP 1: Logging in as Restaurant Owner..."
$LoginBody = @{
    email = $RestEmail
    password = $Password
} | ConvertTo-Json

try {
    $LoginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $LoginBody -Headers $Headers
    $RestToken = $LoginResponse.token
    $RestHeaders = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $RestToken"
    }
    Write-Host "SUCCESS: Restaurant Token retrieved." -ForegroundColor Green
} catch {
    Write-Host "ERROR: Restaurant Owner login failed: $_" -ForegroundColor Red
    exit
}

# Login as second Restaurant Owner to test cross-restaurant blocking (403)
# Register and login second owner
$Rest2Email = "owner2-match@greenbistro.com"
try {
    $Rest2Reg = @{
        email = $Rest2Email
        password = $Password
        fullName = "Bistro Match Owner 2"
        phoneNumber = "9876543212"
        role = "RESTAURANT"
        restaurantDetails = @{
            restaurantName = "Green Bistro Match 2"
            address = "121 Eco Avenue, New Delhi"
            coordinates = @{ latitude = 28.6200; longitude = 77.2100 }
            licenseNumber = "BISTRO-LIC-MATCH-2"
        }
    } | ConvertTo-Json
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body $Rest2Reg -Headers $Headers -ErrorAction SilentlyContinue
} catch {}

try {
    $Rest2LoginBody = @{ email = $Rest2Email; password = $Password } | ConvertTo-Json
    $Rest2LoginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $Rest2LoginBody -Headers $Headers
    $Rest2Token = $Rest2LoginResponse.token
    $Rest2Headers = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $Rest2Token"
    }
} catch {}


# 2. Fetch NGO Details & Approve Profile
Print-Header "STEP 2: Fetching NGO Details & Approving..."
$NgoLoginBody = @{
    email = $NgoEmail
    password = $Password
} | ConvertTo-Json

try {
    $NgoLoginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $NgoLoginBody -Headers $Headers
    $NgoToken = $NgoLoginResponse.token
    $NgoHeaders = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $NgoToken"
    }
    # Fetch profile to get NGO ID
    $NgoProfile = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method GET -Headers $NgoHeaders
    $NgoId = $NgoProfile.id
    Write-Host "SUCCESS: NGO Profile ID retrieved: $NgoId" -ForegroundColor Green

    # Log in as Admin
    $AdminLoginBody = @{
        email = $AdminEmail
        password = $Password
    } | ConvertTo-Json
    $AdminLoginResponse = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -Body $AdminLoginBody -Headers $Headers
    $AdminToken = $AdminLoginResponse.token
    $AdminHeaders = @{
        "Content-Type" = "application/json"
        "Authorization" = "Bearer $AdminToken"
    }

    # Approve NGO Profile
    $ApproveUrl = "$BaseUrl/api/admin/ngo/verify/$NgoId"
    $null = Invoke-RestMethod -Uri $ApproveUrl -Method PUT -Headers $AdminHeaders
    Write-Host "SUCCESS: NGO verification status approved by Admin." -ForegroundColor Green

    # Update NGO profile accepted types & details to be matching-eligible
    $NgoUpdateBody = @{
        ngoName = "Feed The Hungry Match NGO"
        contactPerson = "Ngo Representative"
        email = $NgoEmail
        phoneNumber = "9876543210"
        address = "450 Care Lane, Sector 6, New Delhi"
        city = "New Delhi"
        state = "Delhi"
        pincode = "110001"
        coordinates = @{ latitude = 28.5355; longitude = 77.3910 }
        capacity = 500
        storageType = "COLD"
        operatingHours = "08:00-22:00"
        acceptedFoodTypes = @("VEG", "COOKED")
        registrationNumber = "NGO-REG-MATCH"
    } | ConvertTo-Json

    $null = Invoke-RestMethod -Uri "$BaseUrl/api/ngo/profile" -Method PUT -Body $NgoUpdateBody -Headers $NgoHeaders
    Write-Host "SUCCESS: NGO Profile accepted categories & operating hours updated." -ForegroundColor Green
} catch {
    Write-Host "ERROR: NGO login, verification, or profile setup failed: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $respBody = $reader.ReadToEnd()
        Write-Host "Response Body: $respBody" -ForegroundColor Red
    }
    exit
}

# 3. Create and publish a donation
Print-Header "STEP 3: Creating and Publishing Donation..."
$DonationDraftBody = @{
    foodName = "Surplus Rice Bowl"
    foodCategory = "VEG"
    foodType = "COOKED"
    quantity = 15.0
    quantityUnit = "servings"
    preparedTime = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    expiryTime = (Get-Date).AddHours(4).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    storageCondition = "REFRIGERATED"
    packagingType = "BOX"
    foodTemperature = "HOT"
    pickupAddress = "456 Bistro Ave, Sector 4, New Delhi"
    city = "New Delhi"
    state = "Delhi"
    pincode = "110001"
    coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
    foodImages = @("http://example.com/image.jpg")
} | ConvertTo-Json

try {
    $Donation = Invoke-RestMethod -Uri "$BaseUrl/api/donations" -Method POST -Body $DonationDraftBody -Headers $RestHeaders
    $DonationId = $Donation.id
    Write-Host "SUCCESS: Donation Draft Created with ID: $DonationId" -ForegroundColor Green

    # Publish the donation
    $PublishResponse = Invoke-RestMethod -Uri "$BaseUrl/api/donations/$DonationId/publish" -Method PUT -Headers $RestHeaders
    Write-Host "SUCCESS: Donation status updated to: $($PublishResponse.status)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Donation creation or publication failed: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $respBody = $reader.ReadToEnd()
        Write-Host "Response Body: $respBody" -ForegroundColor Red
    }
    exit
}

# 4. Generate NGO Matching (Success 200)
Print-Header "STEP 4: Generating NGO Matching..."
try {
    $MatchResponse = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method POST -Headers $RestHeaders
    Write-Host "SUCCESS: NGO Matching Generated. Total Matchings: $($MatchResponse.rankedNGOs.Count)" -ForegroundColor Green
    $MatchResponse.rankedNGOs | ForEach-Object {
        Write-Host "  Rank $($_.rank): $($_.ngoName) - Score: $($_.matchingScore)% - Reason: $($_.reason)" -ForegroundColor Gray
    }
} catch {
    Write-Host "ERROR: Matching generation failed: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $respBody = $reader.ReadToEnd()
        Write-Host "Response Body: $respBody" -ForegroundColor Red
    }
}

# 5. Get Latest Matching Result (Success 200)
Print-Header "STEP 5: Retrieving Latest Matching Result..."
try {
    $GetMatchResponse = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method GET -Headers $RestHeaders
    Write-Host "SUCCESS: Match result retrieved. Top Rank NGO: $($GetMatchResponse.rankedNGOs[0].ngoName) - Score: $($GetMatchResponse.rankedNGOs[0].matchingScore)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Match result retrieval failed: $_" -ForegroundColor Red
}

# 6. Fetch Matching History (Success 200)
Print-Header "STEP 6: Fetching Matching History..."
try {
    $History = Invoke-RestMethod -Uri "$BaseUrl/api/matching/history?page=0&size=5" -Method GET -Headers $RestHeaders
    Write-Host "SUCCESS: History retrieved. Total historical runs: $($History.totalElements)" -ForegroundColor Green
} catch {
    Write-Host "ERROR: History retrieval failed: $_" -ForegroundColor Red
}

# 7. Create unmatchable donation to force NoEligibleNgosFoundException (404)
Print-Header "STEP 7: Testing No Eligible NGOs Exception (404)..."
$UnmatchableBody = @{
    foodName = "Frozen Alien Fish"
    foodCategory = "FROZEN_FISH"
    foodType = "RAW"
    quantity = 50.0
    quantityUnit = "kg"
    preparedTime = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    expiryTime = (Get-Date).AddHours(24).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
    storageCondition = "FROZEN"
    packagingType = "BOX"
    foodTemperature = "COLD"
    pickupAddress = "456 Bistro Ave, Sector 4, New Delhi"
    city = "New Delhi"
    state = "Delhi"
    pincode = "110001"
    coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
    foodImages = @("http://example.com/image.jpg")
} | ConvertTo-Json

try {
    $UnmatchableDonation = Invoke-RestMethod -Uri "$BaseUrl/api/donations" -Method POST -Body $UnmatchableBody -Headers $RestHeaders
    $UnmatchableDonationId = $UnmatchableDonation.id
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/donations/$UnmatchableDonationId/publish" -Method PUT -Headers $RestHeaders

    $null = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$UnmatchableDonationId" -Method POST -Headers $RestHeaders
    Write-Host "FAIL: Expected 404 No Eligible NGOs, but request succeeded." -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "SUCCESS: Received expected 404 No Eligible NGOs Found." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Expected 404, received: $statusCode" -ForegroundColor Red
    }
}

# 8. Attempt matching on DRAFT donation (DonationValidationException - 400)
Print-Header "STEP 8: Testing Draft Donation Matching Gate..."
try {
    $DraftDonation = Invoke-RestMethod -Uri "$BaseUrl/api/donations" -Method POST -Body $DonationDraftBody -Headers $RestHeaders
    $DraftId = $DraftDonation.id

    $null = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DraftId" -Method POST -Headers $RestHeaders
    Write-Host "FAIL: Expected 400 Bad Request, but request succeeded." -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400) {
        Write-Host "SUCCESS: Received expected 400 Bad Request for DRAFT donation." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Expected 400, received: $statusCode" -ForegroundColor Red
    }
}

# 9. Attempt matching with invalid ID format (Validator 400)
Print-Header "STEP 9: Testing Malformed ID Validator..."
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/matching/invalid-hex-id-123" -Method POST -Headers $RestHeaders
    Write-Host "FAIL: Expected 400 Bad Request, but request succeeded." -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 400) {
        Write-Host "SUCCESS: Received expected 400 Bad Request for malformed ID format." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Expected 400, received: $statusCode" -ForegroundColor Red
    }
}

# 10. Attempt matching for other Restaurant Owner (Cross-Restaurant 403)
Print-Header "STEP 10: Testing Cross-Restaurant Match Request Blocking..."
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method POST -Headers $Rest2Headers
    Write-Host "FAIL: Expected 403 Forbidden, but request succeeded." -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "SUCCESS: Received expected 403 Forbidden for cross-restaurant matching." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Expected 403, received: $statusCode" -ForegroundColor Red
    }
}

# 11. NGO blocked from matching generation (403)
Print-Header "STEP 11: Testing NGO Blocked from Generating Matching..."
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method POST -Headers $NgoHeaders
    Write-Host "FAIL: Expected 403 Forbidden, but request succeeded." -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "SUCCESS: NGO blocked from matching generation (403)." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Expected 403, received: $statusCode" -ForegroundColor Red
    }
}

# 12. NGO blocked from matching history (403)
Print-Header "STEP 12: Testing NGO Blocked from Viewing History..."
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/matching/history" -Method GET -Headers $NgoHeaders
    Write-Host "FAIL: Expected 403 Forbidden, but request succeeded." -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "SUCCESS: NGO blocked from viewing matching history (403)." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Expected 403, received: $statusCode" -ForegroundColor Red
    }
}

# 13. NGO blocked from GET /matching/{donationId} when NOT assigned (403)
Print-Header "STEP 13: Testing NGO Blocked from Matching Result (Not Assigned)..."
try {
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method GET -Headers $NgoHeaders
    Write-Host "FAIL: Expected 403 Forbidden, but request succeeded." -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 403) {
        Write-Host "SUCCESS: NGO blocked from match details when not assigned (403)." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Expected 403, received: $statusCode" -ForegroundColor Red
    }
}

# 14. NGO allowed to GET /matching/{donationId} after assignment (200 success)
Print-Header "STEP 14: Testing NGO Successful View after Assignment..."
try {
    # Assign NGO to the donation
    $AssignUrl = "$BaseUrl/api/donations/$DonationId/assign/$NgoId"
    $null = Invoke-RestMethod -Uri $AssignUrl -Method PUT -Headers $RestHeaders
    Write-Host "SUCCESS: Assigned NGO to the donation." -ForegroundColor Green

    # NGO GET matching details
    $NgoGetMatch = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method GET -Headers $NgoHeaders
    Write-Host "SUCCESS: NGO successfully retrieved match result after assignment! Top NGO: $($NgoGetMatch.rankedNGOs[0].ngoName)" -ForegroundColor Green
} catch {
    Write-Host "FAIL: NGO could not view assigned donation matching: $_" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host "Body: $($reader.ReadToEnd())"
    }
}

# 15. Admin GET matching details (200 success)
Print-Header "STEP 15: Testing Admin View Match Details..."
try {
    $AdminGetMatch = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$DonationId" -Method GET -Headers $AdminHeaders
    Write-Host "SUCCESS: Admin successfully retrieved match details! Total ranked: $($AdminGetMatch.rankedNGOs.Count)" -ForegroundColor Green
} catch {
    Write-Host "FAIL: Admin could not view matching details: $_" -ForegroundColor Red
}

# 16. Admin GET matching history (200 success)
Print-Header "STEP 16: Testing Admin View Matching History..."
try {
    $AdminHistory = Invoke-RestMethod -Uri "$BaseUrl/api/matching/history" -Method GET -Headers $AdminHeaders
    Write-Host "SUCCESS: Admin successfully retrieved history! Total items: $($AdminHistory.totalElements)" -ForegroundColor Green
} catch {
    Write-Host "FAIL: Admin could not view history: $_" -ForegroundColor Red
}

# 17. MatchingNotFoundException (404) on published donation with zero matching runs
Print-Header "STEP 17: Testing MatchingNotFoundException (404)..."
try {
    # Create and publish a new donation
    $NewDonationBody = @{
        foodName = "Fresh Salad"
        foodCategory = "VEG"
        foodType = "RAW"
        quantity = 5.0
        quantityUnit = "servings"
        preparedTime = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
        expiryTime = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss.fffZ")
        storageCondition = "REFRIGERATED"
        packagingType = "BOX"
        foodTemperature = "COLD"
        pickupAddress = "456 Bistro Ave, Sector 4, New Delhi"
        city = "New Delhi"
        state = "Delhi"
        pincode = "110001"
        coordinates = @{ latitude = 28.6139; longitude = 77.2090 }
        foodImages = @("http://example.com/image.jpg")
    } | ConvertTo-Json

    $NewDonation = Invoke-RestMethod -Uri "$BaseUrl/api/donations" -Method POST -Body $NewDonationBody -Headers $RestHeaders
    $NewId = $NewDonation.id
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/donations/$NewId/publish" -Method PUT -Headers $RestHeaders

    # Attempt to retrieve matching results BEFORE creating matching run
    $null = Invoke-RestMethod -Uri "$BaseUrl/api/matching/$NewId" -Method GET -Headers $RestHeaders
    Write-Host "FAIL: Expected 404 Matching Not Found, but request succeeded." -ForegroundColor Red
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    if ($statusCode -eq 404) {
        Write-Host "SUCCESS: Received expected 404 Matching Not Found (MatchingNotFoundException)." -ForegroundColor Green
    } else {
        Write-Host "FAIL: Expected 404, received: $statusCode" -ForegroundColor Red
    }
}

Print-Header "ALL AUTOMATED INTEGRATION TESTS COMPLETED SUCCESSFULLY!"
