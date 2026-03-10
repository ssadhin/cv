$ErrorActionPreference = 'Stop'
$path = "C:\Users\hasad\StudioProjects\MyApplicatio\test.html"
$content = Get-Content app/src/main/assets/index.html -Raw
Set-Content $path $content -Encoding UTF8

$chromeArgs = '--headless=new', '--disable-gpu', '--remote-debugging-port=9222', "$path"
$process = Start-Process 'C:\Program Files\Google\Chrome\Application\chrome.exe' -ArgumentList $chromeArgs -PassThru -NoNewWindow
Start-Sleep -Seconds 2

try {
    $wsUrl = (Invoke-RestMethod -Uri "http://127.0.0.1:9222/json/list")[0].webSocketDebuggerUrl
    Write-Host "Chrome debugging available at $wsUrl"
    # Can't easily do websockets natively in bare PS, so we'll just check if Chrome stayed alive.
} finally {
    Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
}
