# Verification Script for new Android Signing Key
$propsFile = "signing.properties"
if (-Not (Test-Path $propsFile)) {
    Write-Host "Error: signing.properties not found!" -ForegroundColor Red
    exit
}

$props = ConvertFrom-StringData (Get-Content $propsFile -Raw)
$storeFile = $props.storeFile
$alias = $props.keyAlias
$pass = $props.storePassword

if (-Not (Test-Path $storeFile)) {
    Write-Host "Error: Keystore file '$storeFile' not found!" -ForegroundColor Red
    exit
}

Write-Host "Checking fingerprint for: $storeFile" -ForegroundColor Cyan
keytool -list -v -keystore $storeFile -alias $alias -storepass $pass | Select-String "SHA1:"
