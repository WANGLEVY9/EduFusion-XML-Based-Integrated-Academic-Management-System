$ErrorActionPreference = "Stop"

Write-Host "Running unit and service tests..."
mvn -B clean test

if ($LASTEXITCODE -eq 0) {
    Write-Host "Tests passed." -ForegroundColor Green
    exit 0
}

Write-Host "Tests failed." -ForegroundColor Red
exit 1
