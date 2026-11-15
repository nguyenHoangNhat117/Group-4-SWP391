param([string]$Server = '.\SQLEXPRESS')
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$database = 'HotelSeedVerify_' + [Guid]::NewGuid().ToString('N')
$connection = [System.Data.SqlClient.SqlConnection]::new("Server=$Server;Database=master;Integrated Security=True;Encrypt=False;Connect Timeout=10")
function Invoke-Batches([string]$sql) {
    foreach ($batch in [regex]::Split($sql, '(?im)^\s*GO\s*\r?$')) {
        if (-not [string]::IsNullOrWhiteSpace($batch)) {
            $command = $connection.CreateCommand()
            $command.CommandText = $batch
            $command.CommandTimeout = 60
            try { $null = $command.ExecuteNonQuery() } finally { $command.Dispose() }
        }
    }
}
$created = $false
try {
    $connection.Open()
    Invoke-Batches "CREATE DATABASE [$database]"
    $created = $true
    $connection.ChangeDatabase($database)
    $schema = Get-Content (Join-Path $root 'HotelDB.sql') -Raw -Encoding utf8
    $start = $schema.IndexOf('SET ANSI_NULLS ON')
    $end = $schema.LastIndexOf('USE [master]')
    if ($start -lt 0 -or $end -le $start) { throw 'Cannot locate schema boundaries.' }
    Invoke-Batches $schema.Substring($start, $end-$start)
    $seed = Get-Content (Join-Path $root 'HotelDB.seed.sql') -Raw -Encoding utf8
    $seed = $seed.Replace('USE [HotelDB];', "USE [$database];")
    Invoke-Batches $seed
    $command = $connection.CreateCommand()
    $command.CommandText = 'SELECT COUNT(*) FROM dbo.Booking'
    if ($command.ExecuteScalar() -ne 10) { throw 'Expected 10 bookings.' }
    $rejected = $false
    try { Invoke-Batches $seed } catch {
        if ($_.Exception.ToString().Contains('Seed accounts already exist')) { $rejected = $true } else { throw }
    }
    if (-not $rejected) { throw 'Second run should be rejected.' }
    if ($command.ExecuteScalar() -ne 10) { throw 'Second run modified booking count.' }
    $command.Dispose()
    Write-Output 'PASS: schema, seed, business assertions, second-run protection (10 bookings).'
} finally {
    if ($created -and $connection.State -eq 'Open') {
        $connection.ChangeDatabase('master')
        if ($database -notmatch '^HotelSeedVerify_[0-9a-f]{32}$') { throw 'Unexpected verification database name.' }
        Invoke-Batches "DROP DATABASE [$database]"
    }
    $connection.Dispose()
}
