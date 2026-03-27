# BovCore Mobile

App mobile Flutter para gestão de gado - Android e iOS.

## 🚀 Funcionalidades

- ✅ **Login** com JWT
- ✅ **Dashboard** com estatísticas
- ✅ **Lista de Animais** com busca
- ✅ **QR Code Scanner** para registro rápido de eventos
- ✅ **Registro de Eventos** (Pesagem, Vacinação, Medicação, Movimentação)
- 🔄 **Sincronização** com backend BovCore

## 📋 Pré-requisitos

- Flutter SDK >= 3.0.0
- Dart >= 3.0.0
- Android Studio / Xcode (para emuladores)
- Backend BovCore rodando

## 🛠️ Instalação

### 1. Clone o repositório (se ainda não clonou)

```bash
cd bovcore_mobile
```

### 2. Instale as dependências

```bash
flutter pub get
```

### 3. Configure o endereço da API

Edite `lib/core/api/api_client.dart` e ajuste o `baseUrl`:

```dart
// Para Android Emulator
static const String baseUrl = 'http://10.0.2.2:8080';

// Para iOS Simulator
static const String baseUrl = 'http://localhost:8080';

// Para dispositivo físico (substitua pelo seu IP)
static const String baseUrl = 'http://192.168.1.100:8080';
```

### 4. Execute o app

```bash
# Android
flutter run

# iOS (apenas no macOS)
flutter run -d ios

# Web (para testes)
flutter run -d chrome
```

## 📱 Estrutura do Projeto

```
lib/
├── core/
│   ├── api/           # Cliente HTTP (Dio)
│   ├── auth/          # Serviço de autenticação JWT
│   ├── models/        # Modelos de dados
│   └── theme/         # Tema e cores
├── features/
│   ├── auth/          # Tela de login
│   ├── dashboard/     # Dashboard principal
│   ├── animais/       # Lista de animais
│   └── eventos/       # Registro rápido com QR Code
└── main.dart          # Entry point
```

## 🎨 Tema e Cores

- **Primary Green**: `#22C55E`
- **Dark Green**: `#16A34A`
- **Light Green**: `#DCFCE7`

## 📸 QR Code Scanner

O app pode escanear QR Codes dos animais de duas formas:

1. **QR Code com ID do animal** (UUID)
2. **QR Code com brinco do animal** (ex: "001")

Após escanear, o app:
1. Busca o animal no backend
2. Mostra opções de evento (Pesagem, Vacinação, etc.)
3. Abre formulário específico do evento
4. Registra no backend

## 🔐 Autenticação

O app usa JWT tokens armazenados em `SharedPreferences`.

**Fluxo:**
1. Login → Recebe token
2. Token salvo localmente
3. Todas as requisições incluem `Authorization: Bearer <token>`
4. Token expirado → Logout automático

## 📦 Dependências Principais

- `dio` - Cliente HTTP
- `provider` - State management
- `mobile_scanner` - QR Code scanner
- `shared_preferences` - Storage local
- `google_fonts` - Tipografia
- `jwt_decode` - Decodificação de JWT

## 🧪 Testando

### Emulador Android

```bash
flutter emulators
flutter emulators --launch <emulator_id>
flutter run
```

### Device Físico

1. Ative o modo desenvolvedor no Android
2. Conecte via USB
3. `flutter devices`
4. `flutter run`

## 🚨 Permissões Necessárias

### Android (`android/app/src/main/AndroidManifest.xml`)

```xml
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.INTERNET"/>
```

### iOS (`ios/Runner/Info.plist`)

```xml
<key>NSCameraUsageDescription</key>
<string>Precisamos da câmera para escanear QR Codes dos animais</string>
```

## 🔧 Comandos Úteis

```bash
# Limpar build
flutter clean

# Atualizar dependências
flutter pub upgrade

# Verificar problemas
flutter doctor

# Build APK (Android)
flutter build apk --release

# Build iOS (macOS apenas)
flutter build ios --release
```

## 📝 TODO

- [ ] Modo offline com SQLite
- [ ] Sincronização automática
- [ ] Foto do animal
- [ ] Gráficos de peso
- [ ] Push notifications
- [ ] Geolocalização dos eventos

## 🤝 Integração com Backend

Este app se conecta ao **backend BovCore** (Kotlin + Spring Boot).

Endpoints utilizados:
- `POST /auth/login` - Login
- `GET /api/animais` - Listar animais
- `GET /api/animais/{id}` - Buscar animal
- `POST /api/eventos` - Registrar evento

## 📄 Licença

Proprietary - BovCore © 2024
