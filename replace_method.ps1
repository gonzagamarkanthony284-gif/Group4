# Script to replace addPatientDialog method in PatientsPanel.java

$sourceFile = "src\hpms\ui\panels\PatientsPanel.java"
$replacementFile = "PATIENT_FORM_REPLACEMENT.java"
$backupFile = "src\hpms\ui\panels\PatientsPanel.java.backup2"
$outputFile = "src\hpms\ui\panels\PatientsPanel.java.new"

# Read all lines
$sourceLines = Get-Content $sourceFile
$replacementContent = Get-Content $replacementFile -Raw

# Extract the method code (lines 4-750 from replacement file, which is the actual method)
$replacementLines = Get-Content $replacementFile
$methodLines = $replacementLines[3..749]  # Zero-indexed, so lines 4-750

# Extract before method (lines 1-848), method replacement, and after method (lines 2091-end)
$before = $sourceLines[0..847]  # Lines 1-848 (zero-indexed 0-847)
$after = $sourceLines[2090..($sourceLines.Count-1)]  # Lines 2091-end

# Create backup
Copy-Item $sourceFile $backupFile -Force

# Combine and write
$before | Out-File $outputFile -Encoding UTF8
$methodLines | Out-File $outputFile -Append -Encoding UTF8
"" | Out-File $outputFile -Append -Encoding UTF8
$after | Out-File $outputFile -Append -Encoding UTF8

# Replace original with new
Move-Item $outputFile $sourceFile -Force

Write-Host "Replacement complete. Backup saved to $backupFile"
Write-Host "Before lines: $($before.Count)"
Write-Host "Method lines: $($methodLines.Count)"
Write-Host "After lines: $($after.Count)"
