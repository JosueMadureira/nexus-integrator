# Script para gerar instalador Windows do Nexus Integrator
$JPackagePath = "C:\Program Files\Java\jdk-20\bin\jpackage.exe"
$Version = "1.0"
$Name = "NexusIntegrator"
$JarName = "nexus-integrator-1.0-SNAPSHOT.jar"
$Input = "target"
$Output = "instalador"

# Verificar se jpackage existe
if (-not (Test-Path $JPackagePath)) {
    Write-Host "Erro: jpackage não encontrado em $JPackagePath" -ForegroundColor Red
    Write-Host "Verifique se o JDK 20 está instalado corretamente."
    exit 1
}

# Verificar se o JAR existe
if (-not (Test-Path "$Input\$JarName")) {
    Write-Host "Erro: Arquivo JAR não encontrado em $Input\$JarName" -ForegroundColor Red
    Write-Host "Execute 'mvn clean package' antes de rodar este script."
    exit 1
}

# Criar pasta de saída se não existir
if (Test-Path $Output) {
    Write-Host "Limpando diretório de saída antigo..."
    Remove-Item -Path $Output -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $Output | Out-Null

Write-Host "Gerando instalador para $Name versão $Version..." -ForegroundColor Cyan

& $JPackagePath `
    --type app-image `
    --input $Input `
    --main-jar $JarName `
    --main-class com.sieg.App `
    --name $Name `
    --app-version $Version `
    --icon "icon.png" `
    --vendor "Rocket Nexus" `
    --dest $Output `
    --java-options "-Dfile.encoding=UTF-8"

if ($LASTEXITCODE -eq 0) {
    Write-Host "Sucesso! Instalador gerado na pasta '$Output'." -ForegroundColor Green
}
else {
    Write-Host "Falha ao gerar o instalador." -ForegroundColor Red
}
