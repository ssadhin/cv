# Safe removal of all 4 debug panels from index.html
# Works bottom-up to avoid line number shifts

$file = "app\src\main\assets\index.html"
$lines = [System.Collections.ArrayList]@(Get-Content $file)
Write-Host "Lines before: $($lines.Count)"

# Verify markers exist at expected positions (0-indexed)
$checks = @(
    @{ Line = 4621; Pattern = "INJECTED DRAGGABLE DEBUG PANEL"; Name = "liveDebugPanel start" },
    @{ Line = 4730; Pattern = "})();"; Name = "liveDebugPanel end" },
    @{ Line = 6096; Pattern = "DEBUG PANEL: Show event log"; Name = "splitDebugPanel start" },
    @{ Line = 6222; Pattern = "}, 3000);"; Name = "splitDebugPanel end" },
    @{ Line = 8696; Pattern = "Z-INDEX DEBUG SYSTEM"; Name = "zIndex start" },
    @{ Line = 8807; Pattern = "}, 1000);"; Name = "zIndex end" },
    @{ Line = 16453; Pattern = "FOCUSED DEBUG PANEL"; Name = "templateDebugPanel start" },
    @{ Line = 16536; Pattern = "}, 500);"; Name = "templateDebugPanel end" }
)

$allOk = $true
foreach ($c in $checks) {
    $actual = $lines[$c.Line].Trim()
    if ($actual -notlike "*$($c.Pattern)*") {
        Write-Host "MISMATCH at $($c.Name) (line $($c.Line + 1))"
        Write-Host "  Expected pattern: $($c.Pattern)"
        Write-Host "  Actual: $actual"
        $allOk = $false
    } else {
        Write-Host "OK: $($c.Name) at line $($c.Line + 1)"
    }
}

if (-not $allOk) {
    Write-Host "ABORTING - markers don't match. File NOT modified."
    exit 1
}

# Remove in bottom-up order to preserve line numbers

# 4. templateDebugPanel: lines 16454-16537 (0-indexed: 16453-16536)
# Replace with empty line
$count4 = 16536 - 16453 + 1
Write-Host "Removing templateDebugPanel: $count4 lines (16454-16537)"
$lines.RemoveRange(16453, $count4)

# 3. zIndexDebugPanel: lines 8697-8808 (0-indexed: 8696-8807)  
# Replace with no-op stubs for _zLog, _zDbg, _zPanel
$count3 = 8807 - 8696 + 1
Write-Host "Removing zIndexDebugPanel: $count3 lines (8697-8808)"
$lines.RemoveRange(8696, $count3)
# Insert stubs at the same position
$stubs_z = @(
    "            // Z-Index debug stubs (panels removed)",
    "            window._zLog = [];",
    "            window._zDbg = function () { };",
    "            window._zPanel = null;",
    ""
)
for ($i = 0; $i -lt $stubs_z.Count; $i++) {
    $lines.Insert(8696 + $i, $stubs_z[$i])
}

# 2. splitDebugPanel: lines 6097-6223 (0-indexed: 6096-6222)
# Keep the finally block's closing brace (line 6224 = "}")
$count2 = 6222 - 6096 + 1
Write-Host "Removing splitDebugPanel: $count2 lines (6097-6223)"
$lines.RemoveRange(6096, $count2)
# Insert the closing brace for the finally block
$lines.Insert(6096, "                }")

# 1. liveDebugPanel: lines 4622-4731 (0-indexed: 4621-4730)
# Replace with no-op stubs for recordDebug
$count1 = 4730 - 4621 + 1
Write-Host "Removing liveDebugPanel: $count1 lines (4622-4731)"
$lines.RemoveRange(4621, $count1)
# Insert stubs
$stubs_live = @(
    "            // Debug panel stubs (panels removed)",
    "            window.recordDebug = function () { };",
    "            // ---------------------------------"
)
for ($i = 0; $i -lt $stubs_live.Count; $i++) {
    $lines.Insert(4621 + $i, $stubs_live[$i])
}

Write-Host "Lines after: $($lines.Count)"
Write-Host "Removed: $(16709 - $lines.Count) lines total"

# Verify critical functions still exist
$criticals = @("adjustHeaderHeight", "updateHeaderFrameConfig", "applyFontConfig", "openDialog")
foreach ($fn in $criticals) {
    $found = $lines | Select-String -Pattern $fn -SimpleMatch
    if ($found) {
        Write-Host "VERIFIED: $fn still exists ($($found.Count) references)"
    } else {
        Write-Host "ERROR: $fn is MISSING! Aborting save."
        exit 1
    }
}

# Save
$lines | Set-Content $file -Encoding UTF8
Write-Host "SUCCESS - All debug panels removed safely!"
