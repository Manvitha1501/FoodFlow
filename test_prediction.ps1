$baseUrl = "http://localhost:8081"

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "FOODFLOW MODULE 5 AUTOMATED TESTING FLOW" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan

# 1. Login Restaurant
try {
    Write-Host "`n[1/7] Logging in as Restaurant Owner..." -ForegroundColor Yellow
    $loginBody = @{
        email = "owner-new@greenbistro.com"
        password = "password123"
    } | ConvertTo-Json
    $restaurantRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    $restaurantToken = $restaurantRes.token
    Write-Host "SUCCESS: Restaurant Token retrieved." -ForegroundColor Green
} catch {
    Write-Error "Failed to log in as Restaurant Owner."
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd() -ForegroundColor Red
    }
    return
}

# 2. Login NGO and retrieve Profile ID via GET Profile
try {
    Write-Host "`n[2/7] Logging in as NGO..." -ForegroundColor Yellow
    $ngoLoginBody = @{
        email = "charity@foodflow.org"
        password = "password123"
    } | ConvertTo-Json
    $ngoRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $ngoLoginBody -ContentType "application/json"
    $ngoToken = $ngoRes.token
    
    # Get Profile ID
    $ngoHeaders = @{ Authorization = "Bearer $ngoToken" }
    $profileRes = Invoke-RestMethod -Uri "$baseUrl/api/ngo/profile" -Method Get -Headers $ngoHeaders
    $ngoProfileId = $profileRes.id
    
    Write-Host "SUCCESS: NGO Profile ID is: $ngoProfileId" -ForegroundColor Green
} catch {
    Write-Error "Failed to retrieve NGO Profile ID."
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd() -ForegroundColor Red
    }
    return
}

# 3. Login Admin to get Admin Token
try {
    Write-Host "`n[3/7] Logging in as Admin..." -ForegroundColor Yellow
    $adminLoginBody = @{
        email = "admin-new@foodflow.com"
        password = "password123"
    } | ConvertTo-Json
    $adminRes = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json"
    $adminToken = $adminRes.token
    Write-Host "SUCCESS: Admin Token retrieved." -ForegroundColor Green
} catch {
    Write-Error "Failed to log in as Admin."
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd() -ForegroundColor Red
    }
    return
}

# 4. Approve NGO
try {
    Write-Host "`n[4/7] Approving NGO profile..." -ForegroundColor Yellow
    $headers = @{ Authorization = "Bearer $adminToken" }
    $approveRes = Invoke-RestMethod -Uri "$baseUrl/api/admin/ngo/verify/$ngoProfileId" -Method Put -Headers $headers
    Write-Host "SUCCESS: NGO verification status is now: $($approveRes.verificationStatus)" -ForegroundColor Green
} catch {
    Write-Error "Failed to approve NGO."
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd() -ForegroundColor Red
    }
    return
}

# 5. Create Donation
try {
    Write-Host "`n[5/7] Creating new Donation draft..." -ForegroundColor Yellow
    $donationHeaders = @{ Authorization = "Bearer $restaurantToken" }
    $donationBody = @{
        foodName = "Veg Biryani"
        foodCategory = "VEG"
        foodDescription = "Spiced rice"
        foodType = "COOKED"
        quantity = 25.0
        quantityUnit = "servings"
        preparedTime = "2026-07-23T11:00:00Z"
        expiryTime = "2026-07-23T18:00:00Z"
        storageCondition = "REFRIGERATED"
        packagingType = "CONTAINER"
        foodTemperature = "Cool"
        pickupAddress = "120 Eco Avenue, New Delhi"
        city = "New Delhi"
        state = "Delhi"
        pincode = "110001"
        coordinates = @{
            latitude = 28.6139
            longitude = 77.2090
        }
        foodImages = @("http://example.com/biryani.jpg")
    } | ConvertTo-Json
    $donationRes = Invoke-RestMethod -Uri "$baseUrl/api/donations" -Method Post -Body $donationBody -ContentType "application/json" -Headers $donationHeaders
    $donationId = $donationRes.id
    Write-Host "SUCCESS: Donation created with ID: $donationId" -ForegroundColor Green
} catch {
    Write-Error "Failed to create donation."
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd() -ForegroundColor Red
    }
    return
}

# 6. Publish Donation
try {
    Write-Host "`n[6/7] Publishing Donation..." -ForegroundColor Yellow
    $publishRes = Invoke-RestMethod -Uri "$baseUrl/api/donations/$donationId/publish" -Method Put -Headers $donationHeaders
    Write-Host "SUCCESS: Donation status is now: $($publishRes.status)" -ForegroundColor Green
} catch {
    Write-Error "Failed to publish donation."
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd() -ForegroundColor Red
    }
    return
}

# 7. Trigger Risk Prediction
try {
    Write-Host "`n[7/7] Triggering Risk Prediction..." -ForegroundColor Yellow
    $predictionBody = @{
        weatherCondition = "CLEAR"
        trafficCondition = "NORMAL"
    } | ConvertTo-Json
    $predictionRes = Invoke-RestMethod -Uri "$baseUrl/api/risk-prediction/$donationId/$ngoProfileId" -Method Post -Body $predictionBody -ContentType "application/json" -Headers $donationHeaders
    
    Write-Host "`n=============================================" -ForegroundColor Green
    Write-Host "ALL STEPS COMPLETED! HERE IS YOUR PREDICTION:" -ForegroundColor Green
    Write-Host "=============================================" -ForegroundColor Green
    $predictionRes | ConvertTo-Json -Depth 5
} catch {
    Write-Error "Failed to trigger prediction."
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        Write-Host $reader.ReadToEnd() -ForegroundColor Red
    }
    return
}
