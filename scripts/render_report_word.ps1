param([Parameter(Mandatory=$true)][string]$InputPath,
      [Parameter(Mandatory=$true)][string]$PdfPath,
      [switch]$Finalize)
# Use a separate, hidden Word instance; do not attach to the user's open files.
$word = New-Object -ComObject Word.Application
$word.Visible = $false
$word.DisplayAlerts = 0
$document = $null
try {
    $document = $word.Documents.Open($InputPath, $false, (-not $Finalize))
    if ($Finalize) {
        [void]$document.Fields.Update()
        foreach ($toc in $document.TablesOfContents) { $toc.Update() }
        $document.Repaginate()
        foreach ($toc in $document.TablesOfContents) { $toc.UpdatePageNumbers() }
        $document.Save()
    }
    $document.Repaginate()
    $document.ExportAsFixedFormat($PdfPath, 17)
    Write-Output "PDF exported: $PdfPath"
} finally {
    if ($document) {
        $document.Close(0)
        [void][Runtime.InteropServices.Marshal]::ReleaseComObject($document)
    }
    $word.Quit()
    [void][Runtime.InteropServices.Marshal]::ReleaseComObject($word)
}
