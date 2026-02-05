# Nexus Integrator

![Nexus Logo](src/main/resources/icon.png)

Sistema integrado para processamento automático de notas fiscais eletrônicas (NF-e e NFC-e) via e-mail com integração à API Sieg.

## 🚀 Funcionalidades

- ✅ **Monitoramento automático** de e-mails IMAP
- ✅ **Processamento de arquivos compactados** (ZIP e RAR)
- ✅ **Extração e envio** de XMLs para API Sieg
- ✅ **Interface gráfica moderna** com FlatLaf
- ✅ **System tray** com minimização
- ✅ **Contadores persistentes** de NF-e e NFC-e
- ✅ **Retry logic** com reconexão automática
- ✅ **Instalador Windows** com opção de inicialização automática

## 📦 Download

Baixe a última versão do instalador em [Releases](../../releases/latest):
- **NexusIntegrator-Setup.exe** (~37 MB)

## 🔧 Requisitos

- Windows 10/11
- Java 11+ (incluído no instalador)

## 📖 Como Usar

1. Execute `NexusIntegrator-Setup.exe`
2. Configure as credenciais IMAP e API na aba "Configurações"
3. Clique em "Salvar Configurações"
4. O sistema inicia automaticamente o monitoramento

## 🏗️ Compilar

```powershell
# Build completo + instalador
.\setup_e_build.ps1
```

O instalador será gerado em `instalador-final/NexusIntegrator-Setup.exe`

## 🛠️ Tecnologias

- **Java 11** - Linguagem base
- **Maven** - Build e gerenciamento de dependências
- **Jakarta Mail** - Processamento de e-mails
- **FlatLaf** - UI moderna
- **JPackage** - Empacotamento
- **Inno Setup** - Criação do instalador

## 📝 Licença

Proprietary - Rocket Nexus © 2026
