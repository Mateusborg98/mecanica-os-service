param()
$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($env:DD_API_KEY)) { throw 'Configure DD_API_KEY nesta mesma janela antes de executar.' }
$env:DD_API_KEY = $env:DD_API_KEY.Trim()
$validacao = Invoke-RestMethod -Uri 'https://api.datadoghq.com/api/v1/validate' -Headers @{'DD-API-KEY'=$env:DD_API_KEY}
if (-not $validacao.valid) { throw 'API Key nao validada.' }
$kubectl = Join-Path $env:LOCALAPPDATA 'MecanicaDevTools\kubernetes-1.36\kubectl.exe'
if (-not (Test-Path -LiteralPath $kubectl)) { throw 'kubectl 1.36 local nao encontrado.' }
$context = 'arn:aws:eks:us-east-1:533267079174:cluster/mecanica-fase4'
& $kubectl --context $context get namespace datadog -o name
if ($LASTEXITCODE -ne 0) { throw 'Namespace Datadog indisponivel; confirmar bootstrap do cluster.' }
$existing = & $kubectl --context $context -n datadog get secret datadog-secret --ignore-not-found -o name
if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel consultar o Secret.' }
if ($existing) { throw 'Secret ja existe. Nao foi alterado; confirmar necessidade de rotacao.' }
$manifest = @{
    apiVersion='v1';kind='Secret';metadata=@{name='datadog-secret';namespace='datadog'}
    type='Opaque';stringData=@{'api-key'=$env:DD_API_KEY}
}
try {
    $manifest | ConvertTo-Json -Depth 5 -Compress | & $kubectl --context $context create -f -
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao cadastrar Secret.' }
} finally {
    $manifest = $null
}
Write-Host 'Secret Datadog cadastrado. Nenhuma chave foi gravada em arquivo pelo script.'
