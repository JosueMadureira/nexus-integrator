# Script de Automação Total para Nexus
# 1. Baixa Maven
# 2. Compila Projeto
# 3. Gera EXE

$MavenUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
$MavenZip = "maven.zip"
$MavenDir = "apache-maven-3.9.6"

Write-Host ">>> INICIANDO PROCESSO AUTOMATIZADO NEXUS <<<" -ForegroundColor Cyan

# 1. Baixar Maven se não existir
if (-not (Test-Path $MavenDir)) {
    Write-Host "1. Baixando Maven Portátil..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $MavenUrl -OutFile $MavenZip
        Write-Host "   Extraindo Maven..."
        Expand-Archive -Path $MavenZip -DestinationPath . -Force
        Remove-Item $MavenZip
    } catch {
        Write-Host "ERRO: Não foi possível baixar o Maven. Verifique sua internet." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "1. Maven já encontrado." -ForegroundColor Yellow
}

$MvnCmd = "$PWD\$MavenDir\bin\mvn.cmd"

# 2. Compilar Projeto
Write-Host "2. Baixando dependências e Compilando (Isso pode demorar um pouco)..." -ForegroundColor Yellow
& $MvnCmd clean package

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERRO NA COMPILAÇÃO. Verifique os logs acima." -ForegroundColor Red
    exit 1
}

# 3. Gerar Instalador
Write-Host "3. Gerando Instalador EXE..." -ForegroundColor Yellow
.\gerar_instalador.ps1

Write-Host ">>> CONCLUÍDO! <<<" -ForegroundColor Green
Write-Host "O instalador está na pasta: $(Resolve-Path .\instalador)" -ForegroundColor Green
