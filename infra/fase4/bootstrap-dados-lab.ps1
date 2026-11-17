$ErrorActionPreference = 'Stop'
$kubectl = Join-Path $env:LOCALAPPDATA 'MecanicaDevTools\kubernetes-1.36\kubectl.exe'
$context = 'arn:aws:eks:us-east-1:533267079174:cluster/mecanica-fase4'
if (-not (Test-Path -LiteralPath $kubectl)) { throw 'kubectl 1.36 nao encontrado.' }
function Invoke-Kube {
    & $kubectl --context $context @args
    if ($LASTEXITCODE -ne 0) { throw 'Comando Kubernetes falhou; bootstrap interrompido.' }
}
function New-Password {
    $bytes = New-Object byte[] 32
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return [Convert]::ToBase64String($bytes)
}
function Ensure-Secret($name, $data) {
    $existing = Invoke-Kube -n mecanica-fase4 get secret $name --ignore-not-found -o name
    if ($existing) { Write-Host "Secret existente preservado: $name"; return }
    $manifest = @{apiVersion='v1';kind='Secret';type='Opaque';metadata=@{name=$name;namespace='mecanica-fase4'};stringData=$data}
    $manifest | ConvertTo-Json -Depth 5 -Compress | & $kubectl --context $context create -f -
    if ($LASTEXITCODE -ne 0) { throw "Falha ao criar Secret $name" }
}
Invoke-Kube apply -f (Join-Path $PSScriptRoot 'platform\namespace.yaml')
Ensure-Secret 'postgres-os-secret' @{POSTGRES_PASSWORD=(New-Password)}
Ensure-Secret 'postgres-execution-secret' @{POSTGRES_PASSWORD=(New-Password)}
Ensure-Secret 'mongodb-secret' @{MONGO_INITDB_ROOT_USERNAME='billing_lab';MONGO_INITDB_ROOT_PASSWORD=(New-Password)}
Ensure-Secret 'rabbitmq-secret' @{RABBITMQ_DEFAULT_USER='mecanica_lab';RABBITMQ_DEFAULT_PASS=(New-Password)}
Invoke-Kube apply -k (Join-Path $PSScriptRoot 'platform')
Write-Host 'Plataforma aplicada. Verificar PVCs e readiness antes de implantar as APIs. Senhas foram geradas em memoria e guardadas somente nos Secrets do cluster.'
