import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'core/auth/auth_service.dart';
import 'core/data/app_repository.dart';
import 'core/data/local_database_service.dart';
import 'core/data/offline_farm_repository.dart';
import 'core/services/connectivity_service.dart';
import 'core/services/remote_api_service.dart';
import 'core/theme/app_theme.dart';
import 'features/auth/login_screen.dart';
import 'features/dashboard/dashboard_screen.dart';

void main() {
  runApp(const BovCoreApp());
}

class BovCoreApp extends StatelessWidget {
  const BovCoreApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider<AuthService>(
          create: (_) => AuthService(),
        ),
        Provider<LocalDatabaseService>(
          create: (_) => LocalDatabaseService(),
        ),
        Provider<ConnectivityService>(
          create: (_) => ConnectivityService(),
        ),
        Provider<RemoteApiService>(
          create: (_) => RemoteApiService(),
        ),
        ProxyProvider<LocalDatabaseService, OfflineFarmRepository>(
          update: (_, databaseService, __) =>
              OfflineFarmRepository(databaseService),
        ),
        ProxyProvider4<AuthService, OfflineFarmRepository, RemoteApiService,
            ConnectivityService, AppRepository>(
          update: (_, authService, offlineRepository, remoteApiService,
                  connectivityService, __) =>
              AppRepository(
            authService,
            offlineRepository,
            remoteApiService,
            connectivityService,
          ),
        ),
      ],
      child: MaterialApp(
        title: 'BovCore',
        theme: AppTheme.lightTheme,
        debugShowCheckedModeBanner: false,
        home: const SplashScreen(),
      ),
    );
  }
}

class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen> {
  @override
  void initState() {
    super.initState();
    _bootstrap();
  }

  Future<void> _bootstrap() async {
    final authService = context.read<AuthService>();
    final repository = context.read<AppRepository>();

    await authService.restoreSession();
    await repository.refreshConnectivity();

    if (!mounted) {
      return;
    }

    Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) =>
            authService.isLoggedIn ? const DashboardScreen() : const LoginScreen(),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(
              Icons.agriculture,
              size: 80,
              color: Color(0xFF22C55E),
            ),
            const SizedBox(height: 24),
            Text(
              'BovCore',
              style: Theme.of(context).textTheme.displaySmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: const Color(0xFF1F2937),
                  ),
            ),
            const SizedBox(height: 12),
            Text(
              'Carregando sessao e conectividade',
              style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: const Color(0xFF6B7280),
                  ),
            ),
            const SizedBox(height: 32),
            const CircularProgressIndicator(
              valueColor: AlwaysStoppedAnimation(Color(0xFF22C55E)),
            ),
          ],
        ),
      ),
    );
  }
}
