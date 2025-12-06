# Script để clean build khi gặp lỗi R.jar locked
Write-Host "Stopping Gradle daemons..." -ForegroundColor Yellow
./gradlew --stop

Start-Sleep -Seconds 2

Write-Host "Killing Java processes..." -ForegroundColor Yellow
Get-Process | Where-Object { $_.ProcessName -like "*java*" } | Stop-Process -Force -ErrorAction SilentlyContinue

Start-Sleep -Seconds 2

Write-Host "Removing build folder..." -ForegroundColor Yellow
Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "✅ Cleaned successfully! Now you can run ./gradlew assembleDebug" -ForegroundColor Green
