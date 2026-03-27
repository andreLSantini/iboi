# 🚀 BovCore Mobile - Início Rápido

## Rodando em 5 minutos

### 1️⃣ Instalar Flutter

**Windows:**
```bash
# Baixe o Flutter SDK de: https://docs.flutter.dev/get-started/install/windows
# Extraia para C:\src\flutter
# Adicione ao PATH: C:\src\flutter\bin

flutter doctor
```

**macOS/Linux:**
```bash
# Instale via Homebrew (macOS)
brew install flutter

# Ou baixe de: https://docs.flutter.dev/get-started/install
flutter doctor
```

### 2️⃣ Configurar Backend

O backend BovCore deve estar rodando em:
- **Android Emulator**: `http://10.0.2.2:8080`
- **iOS Simulator**: `http://localhost:8080`
- **Device físico**: `http://SEU_IP:8080`

Para descobrir seu IP:
```bash
# Windows
ipconfig

# macOS/Linux
ifconfig | grep inet
```

### 3️⃣ Instalar Dependências

```bash
cd bovcore_mobile
flutter pub get
```

### 4️⃣ Rodar o App

**Opção A - Android Emulator:**
```bash
# Abrir Android Studio > AVD Manager > Start Emulator
# Ou via linha de comando:
flutter emulators
flutter emulators --launch Pixel_5_API_33

# Rodar app
flutter run
```

**Opção B - Device Físico (Android):**
```bash
# 1. Ativar modo desenvolvedor no celular
# 2. Ativar depuração USB
# 3. Conectar via USB
# 4. Aceitar permissão no celular

flutter devices  # Ver devices conectados
flutter run      # Rodar no device
```

**Opção C - iOS (apenas macOS):**
```bash
open -a Simulator
flutter run -d "iPhone 15"
```

### 5️⃣ Login

Use as credenciais do backend:
- **Email**: usuario@exemplo.com
- **Senha**: senha123

## 🛠️ Troubleshooting

### Erro: "Unable to connect to backend"

**Verifique:**
1. Backend está rodando? `curl http://localhost:8080/actuator/health`
2. IP correto no `api_client.dart`?
3. Firewall bloqueando?

**Android Emulator:**
- Use `10.0.2.2` em vez de `localhost`
- Verifique: Settings > Developer Options > USB Debugging

**iOS Simulator:**
- Use `localhost`
- Permita conexões no macOS Firewall

**Device Físico:**
- Use o IP da sua máquina (ex: `192.168.1.100`)
- Celular e PC devem estar na mesma rede WiFi
- Desative VPN se estiver usando

### Erro: "Camera permission denied"

**Android:**
```xml
<!-- android/app/src/main/AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA"/>
```

**iOS:**
```xml
<!-- ios/Runner/Info.plist -->
<key>NSCameraUsageDescription</key>
<string>Precisamos da câmera para escanear QR Codes</string>
```

### Erro: "Failed to build iOS app"

```bash
cd ios
pod install
cd ..
flutter clean
flutter pub get
flutter run
```

### Erro: "Gradle build failed"

```bash
cd android
./gradlew clean
cd ..
flutter clean
flutter pub get
flutter run
```

## 📱 Hot Reload

Durante o desenvolvimento, após salvar o código:
- **Hot Reload**: `r` no terminal (recarrega código)
- **Hot Restart**: `R` no terminal (reinicia app)
- **Quit**: `q` no terminal

## 🎯 Próximos Passos

1. **Cadastre um animal** no web (http://localhost:5173)
2. **Gere um QR Code** com o brinco do animal
3. **Escaneie** no app mobile
4. **Registre um evento** (pesagem, vacinação, etc.)

## 🔗 Links Úteis

- [Flutter Docs](https://docs.flutter.dev)
- [Dart Packages](https://pub.dev)
- [Flutter Samples](https://flutter.github.io/samples/)
- [BovCore Backend](http://localhost:8080/swagger-ui.html)

## 💡 Dicas

- Use **Android Studio** para melhor experiência de desenvolvimento
- Instale extensão **Flutter** no VS Code
- Use `flutter doctor -v` para diagnóstico completo
- Habilite **USB Debugging** para device físico

## ✅ Checklist

- [ ] Flutter instalado (`flutter --version`)
- [ ] Android Studio / Xcode configurado
- [ ] Emulador/device detectado (`flutter devices`)
- [ ] Backend rodando (`curl http://localhost:8080/actuator/health`)
- [ ] Dependências instaladas (`flutter pub get`)
- [ ] App rodando (`flutter run`)
- [ ] Login funcionando
- [ ] QR Scanner testado

---

**Problemas?** Verifique os logs com `flutter run -v` (modo verbose)
