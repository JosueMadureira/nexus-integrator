# Como Criar o Instalador Windows do Nexus Integrator

## 1. Instalar o Inno Setup

1. Baixe o Inno Setup: https://jrsoftware.org/isdl.php
2. Execute o instalador baixado (`innosetup-x.x.x.exe`)
3. Siga o assistente de instalação (Next > Next > Install)

## 2. Gerar o Instalador

### Opção A: Usar a Interface Gráfica

1. Abra o **Inno Setup Compiler**
2. Clique em **File > Open** (ou pressione Ctrl+O)
3. Navegue até: `C:\rocket\Nexus\SiegIntegratorJava\instalador\NexusIntegrator.iss`
4. Clique em **Build > Compile** (ou pressione Ctrl+F9)
5. Aguarde a compilação finalizar
6. O instalador será criado em: `C:\rocket\Nexus\SiegIntegratorJava\instalador-final\NexusIntegrator-Setup.exe`

### Opção B: Usar Linha de Comando

```powershell
# Navegar até a pasta do instalador
cd C:\rocket\Nexus\SiegIntegratorJava\instalador

# Compilar o instalador (substitua o caminho pelo seu Inno Setup)
& "C:\Program Files (x86)\Inno Setup 6\ISCC.exe" NexusIntegrator.iss
```

## 3. Resultado

Você terá um único arquivo:
- **Nome**: `NexusIntegrator-Setup.exe`
- **Localização**: `C:\rocket\Nexus\SiegIntegratorJava\instalador-final\`
- **Tamanho**: ~25-30 MB (contém tudo necessário)

## 4. Como Usar o Instalador

### No computador do cliente:

1. Envie apenas o arquivo `NexusIntegrator-Setup.exe`
2. O usuário executa o instalador (duplo-clique)
3. O instalador:
   - Pede permissão de administrador
   - Verifica se o Java está instalado
   - Instala em `C:\Program Files\Nexus Integrator\`
   - Cria atalhos no Menu Iniciar
   - Opcionalmente cria atalho na Área de Trabalho
   - Registra no "Programas e Recursos" do Windows

### Desinstalar:

- Painel de Controle > Programas e Recursos > Nexus Integrator > Desinstalar
- OU Menu Iniciar > Nexus Integrator > Desinstalar

## 5. Personalização (Opcional)

Edite o arquivo `NexusIntegrator.iss` para:

- **Alterar nome da empresa**: Linha 7 (`#define MyAppPublisher`)
- **Alterar versão**: Linha 6 (`#define MyAppVersion`)
- **Alterar ícone**: Linha 21 (`SetupIconFile`)
- **Adicionar licença**: Adicione `LicenseFile=caminho\para\licenca.txt`
- **Alterar idioma padrão**: Linha 32-33

## 6. Requisitos do Sistema Cliente

- **Windows**: 7 SP1, 8, 8.1, 10 ou 11 (32 ou 64 bits)
- **Java**: JRE/JDK 11 ou superior
- **Espaço em disco**: ~50 MB
- **Memória RAM**: 512 MB mínimo (1 GB recomendado)

## 7. Distribuição

O arquivo `NexusIntegrator-Setup.exe` pode ser distribuído via:
- Email (se menor que 25 MB)
- Google Drive / Dropbox / OneDrive
- Pen Drive / DVD
- Servidor de download próprio

## 8. Assinatura Digital (Opcional - Recomendado)

Para evitar alertas de "Editor desconhecido":

1. Obtenha um certificado de assinatura de código (Code Signing Certificate)
2. Use `signtool.exe` (Windows SDK) para assinar o instalador
3. Isso adiciona credibilidade e remove avisos do SmartScreen

```powershell
signtool sign /f "certificado.pfx" /p "senha" /t http://timestamp.digicert.com NexusIntegrator-Setup.exe
```

## 9. Solução de Problemas

### "Java não detectado"
- O instalador avisa mas permite continuar
- Usuário deve instalar Java manualmente: https://adoptium.net/

### "Acesso negado"
- Execute o instalador como administrador
- Clique direito > Executar como administrador

### Instalador não abre
- Verifique o antivírus (pode bloquear instaladores não assinados)
- Adicione exceção temporária

## 10. Próximos Passos Profissionais

Para um produto comercial completo:

1. **Auto-Update**: Implementar verificação automática de atualizações
2. **Telemetria**: Adicionar analytics de uso (opcional, com consentimento)
3. **Crash Reports**: Sistema de relatório de erros automático
4. **Documentação**: Manual do usuário em PDF incluído
5. **Licenciamento**: Sistema de chaves de ativação (se aplicável)
