Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

# Create Form
$form = New-Object System.Windows.Forms.Form
$form.Text = "Log Filter Tool by Correlation ID"
$form.Size = New-Object System.Drawing.Size(600,420)
$form.StartPosition = "CenterScreen"

# Log File Input
$labelFile = New-Object System.Windows.Forms.Label
$labelFile.Text = "Log File Path:"
$labelFile.Location = New-Object System.Drawing.Point(10,20)
$labelFile.Size = New-Object System.Drawing.Size(80,20)
$form.Controls.Add($labelFile)

$textBoxFile = New-Object System.Windows.Forms.TextBox
$textBoxFile.Location = New-Object System.Drawing.Point(100,20)
$textBoxFile.Size = New-Object System.Drawing.Size(370,20)
$form.Controls.Add($textBoxFile)

$buttonBrowse = New-Object System.Windows.Forms.Button
$buttonBrowse.Text = "Browse"
$buttonBrowse.Location = New-Object System.Drawing.Point(480,18)
$buttonBrowse.Size = New-Object System.Drawing.Size(75,23)
$buttonBrowse.Add_Click({
    $dialog = New-Object System.Windows.Forms.OpenFileDialog
    $dialog.Filter = "Log Files (*.log)|*.log|All Files (*.*)|*.*"
    if ($dialog.ShowDialog() -eq "OK") {
        $textBoxFile.Text = $dialog.FileName
    }
})
$form.Controls.Add($buttonBrowse)

# Correlation ID input
$labelCID = New-Object System.Windows.Forms.Label
$labelCID.Text = "Correlation ID:"
$labelCID.Location = New-Object System.Drawing.Point(10,60)
$labelCID.Size = New-Object System.Drawing.Size(100,20)
$form.Controls.Add($labelCID)

$textBoxCID = New-Object System.Windows.Forms.TextBox
$textBoxCID.Location = New-Object System.Drawing.Point(100,60)
$textBoxCID.Size = New-Object System.Drawing.Size(370,20)
$form.Controls.Add($textBoxCID)

# Output display
$textBoxOutput = New-Object System.Windows.Forms.TextBox
$textBoxOutput.Location = New-Object System.Drawing.Point(10,100)
$textBoxOutput.Multiline = $true
$textBoxOutput.ScrollBars = "Vertical"
$textBoxOutput.Size = New-Object System.Drawing.Size(545,240)
$form.Controls.Add($textBoxOutput)

# Filter button
$buttonFilter = New-Object System.Windows.Forms.Button
$buttonFilter.Text = "Filter Logs"
$buttonFilter.Location = New-Object System.Drawing.Point(480,60)
$buttonFilter.Size = New-Object System.Drawing.Size(75,23)
$buttonFilter.Add_Click({
    $filePath = $textBoxFile.Text
    $cid = $textBoxCID.Text

    if (-Not (Test-Path $filePath)) {
        [System.Windows.Forms.MessageBox]::Show("File not found.")
        return
    }

    if ([string]::IsNullOrWhiteSpace($cid)) {
        [System.Windows.Forms.MessageBox]::Show("Please enter a Correlation ID.")
        return
    }

    $lines = Get-Content $filePath
    $filtered = @()
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        if ($line -match "\[$cid\]") {
            $filtered += $line

            if ($line -match "\[ERROR\]") {
                # Append stack trace lines until next timestamped log entry
                $j = $i + 1
                while ($j -lt $lines.Count -and $lines[$j] -notmatch "^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}") {
                    $filtered += $lines[$j]
                    $j++
                }
                $i = $j - 1
            }
        }
    }

    $textBoxOutput.Text = $filtered -join "`r`n"
})
$form.Controls.Add($buttonFilter)

# Save Button
$buttonSave = New-Object System.Windows.Forms.Button
$buttonSave.Text = "Save Result"
$buttonSave.Location = New-Object System.Drawing.Point(10,350)
$buttonSave.Size = New-Object System.Drawing.Size(100,23)
$buttonSave.Add_Click({
    $dialog = New-Object System.Windows.Forms.SaveFileDialog
    $dialog.Filter = "Log Files (*.log)|*.log|All Files (*.*)|*.*"
    if ($dialog.ShowDialog() -eq "OK") {
        $textBoxOutput.Lines | Out-File -FilePath $dialog.FileName -Encoding UTF8
        [System.Windows.Forms.MessageBox]::Show("File saved successfully.")
    }
})
$form.Controls.Add($buttonSave)

# Run form
$form.Topmost = $true
$form.Add_Shown({$form.Activate()})
[void]$form.ShowDialog()
