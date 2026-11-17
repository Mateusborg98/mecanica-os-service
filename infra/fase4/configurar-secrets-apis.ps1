# Compatible with Windows PowerShell 5.1 and PowerShell 7.
$ErrorActionPreference = 'Stop'
$kubectl = Join-Path $env:LOCALAPPDATA 'MecanicaDevTools\kubernetes-1.36\kubectl.exe'
$context = 'arn:aws:eks:us-east-1:533267079174:cluster/mecanica-fase4'
if (-not (Test-Path -LiteralPath $kubectl)) { throw 'kubectl 1.36 nao encontrado.' }
foreach ($name in @('MERCADO_PAGO_ACCESS_TOKEN','MERCADO_PAGO_WEBHOOK_SECRET')) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name,'Process'))) {
        throw "Configure $name nesta mesma janela antes de executar."
    }
}
$publicPath = Join-Path $env:LOCALAPPDATA 'MecanicaDevTools\os-integration-public.env'
if (-not (Test-Path -LiteralPath $publicPath)) { throw 'Arquivo da chave publica de demonstracao nao encontrado.' }
$publicLine = @(Get-Content -LiteralPath $publicPath | Where-Object { $_.StartsWith('OS_INTEGRATION_PUBLIC_KEY=') })
if ($publicLine.Count -ne 1) { throw 'Chave publica ausente ou ambigua.' }
$publicKey = (($publicLine[0] -split '=',2)[1]).Trim()
if (($publicKey.StartsWith("'") -and $publicKey.EndsWith("'")) -or ($publicKey.StartsWith('"') -and $publicKey.EndsWith('"'))) {
    $publicKey = $publicKey.Substring(1, $publicKey.Length - 2)
}
if ($publicKey -notmatch 'BEGIN PUBLIC KEY') { throw 'Formato da chave publica inesperado.' }
function Read-ClusterSecret($name) {
    $result = & $kubectl --context $context -n mecanica-fase4 get secret $name -o json 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Nao foi possivel consultar $name. Confira sessao AWS e acesso ao cluster." }
    return (($result -join "`n") | ConvertFrom-Json).data
}
function Decode-Value($data, $key) {
    $encoded = $data.PSObject.Properties[$key].Value
    if ([string]::IsNullOrWhiteSpace($encoded)) { throw "Campo obrigatorio ausente: $key" }
    return [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($encoded))
}
function Create-ApiSecret($name, $data) {
    $existing = & $kubectl --context $context -n mecanica-fase4 get secret $name --ignore-not-found -o name 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Falha ao consultar $name." }
    if ($existing) { Write-Host "Secret existente preservado: $name"; return }
    $manifest = @{apiVersion='v1';kind='Secret';type='Opaque';metadata=@{name=$name;namespace='mecanica-fase4'};stringData=$data}
    try {
        $result = $manifest | ConvertTo-Json -Depth 5 -Compress | & $kubectl --context $context create -f - 2>&1
        if ($LASTEXITCODE -ne 0) { throw "Falha ao criar $name. Saida omitida para proteger credenciais." }
        Write-Host "Secret criado: $name"
    } finally { $manifest=$null; $result=$null }
}
try {
    $os = Read-ClusterSecret 'postgres-os-secret'
    $execution = Read-ClusterSecret 'postgres-execution-secret'
    $mongo = Read-ClusterSecret 'mongodb-secret'
    $rabbit = Read-ClusterSecret 'rabbitmq-secret'
    $rabbitUser = Decode-Value $rabbit 'RABBITMQ_DEFAULT_USER'
    $rabbitPassword = Decode-Value $rabbit 'RABBITMQ_DEFAULT_PASS'
    $mongoUser = [Uri]::EscapeDataString((Decode-Value $mongo 'MONGO_INITDB_ROOT_USERNAME'))
    $mongoPassword = [Uri]::EscapeDataString((Decode-Value $mongo 'MONGO_INITDB_ROOT_PASSWORD'))
    Create-ApiSecret 'mecanica-os-service-secret' @{
        SPRING_DATASOURCE_PASSWORD=(Decode-Value $os 'POSTGRES_PASSWORD')
        JWT_PUBLIC_KEY=$publicKey;RABBITMQ_USERNAME=$rabbitUser;RABBITMQ_PASSWORD=$rabbitPassword
    }
    Create-ApiSecret 'mecanica-execution-service-secret' @{
        DATABASE_PASSWORD=(Decode-Value $execution 'POSTGRES_PASSWORD')
        JWT_PUBLIC_KEY=$publicKey;RABBITMQ_USERNAME=$rabbitUser;RABBITMQ_PASSWORD=$rabbitPassword
    }
    Create-ApiSecret 'mecanica-billing-service-secret' @{
        MONGODB_URI=('mongodb://'+$mongoUser+':'+$mongoPassword+'@mongodb:27017/mecanica_billing?authSource=admin')
        JWT_PUBLIC_KEY=$publicKey;RABBITMQ_USERNAME=$rabbitUser;RABBITMQ_PASSWORD=$rabbitPassword
        MERCADO_PAGO_ACCESS_TOKEN=$env:MERCADO_PAGO_ACCESS_TOKEN.Trim()
        MERCADO_PAGO_WEBHOOK_SECRET=$env:MERCADO_PAGO_WEBHOOK_SECRET.Trim()
    }
} finally {
    $os=$null;$execution=$null;$mongo=$null;$rabbit=$null
    $rabbitUser=$null;$rabbitPassword=$null;$mongoUser=$null;$mongoPassword=$null
}
Write-Host 'Preparacao concluida. Secrets existentes foram preservados. Nenhuma API foi implantada por este script.'
