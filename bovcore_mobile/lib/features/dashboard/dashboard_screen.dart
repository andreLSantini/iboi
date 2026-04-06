import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/auth/auth_service.dart';
import '../../core/data/app_repository.dart';
import '../../core/models/app_session.dart';
import '../animais/animais_list_screen.dart';
import '../auth/login_screen.dart';
import '../eventos/eventos_page.dart';
import 'dashboard_summary_page.dart';

enum AppMenu {
  dashboard,
  fazendas,
  animais,
  lotes,
  eventos,
  calendario,
  despesas,
  relatorios,
  alertas,
  veterinarios,
  assinatura,
  configuracoes,
}

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  AppMenu _selectedMenu = AppMenu.dashboard;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppRepository>().refreshConnectivity();
    });
  }

  Future<void> _handleLogout() async {
    await context.read<AuthService>().logout();
    if (!mounted) {
      return;
    }

    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginScreen()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    final session = auth.session;
    final mode = auth.mode;
    final title = _menuMeta(_selectedMenu).label;

    return Scaffold(
      appBar: AppBar(
        titleSpacing: 8,
        title: Row(
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: const Color(0xFFDCFCE7),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(
                Icons.agriculture,
                color: Color(0xFF166534),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'BovCore',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                  ),
                  const Text(
                    'Painel operacional do SaaS',
                    style: TextStyle(
                      fontSize: 12,
                      color: Color(0xFF6B7280),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16),
            child: Center(
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                decoration: BoxDecoration(
                  color: mode == SessionMode.online
                      ? const Color(0xFFECFDF5)
                      : const Color(0xFFFFFBEB),
                  borderRadius: BorderRadius.circular(999),
                  border: Border.all(
                    color: mode == SessionMode.online
                        ? const Color(0xFFBBF7D0)
                        : const Color(0xFFFDE68A),
                  ),
                ),
                child: Text(
                  mode == SessionMode.online ? 'Producao online' : 'Modo offline',
                  style: TextStyle(
                    color: mode == SessionMode.online
                        ? const Color(0xFF166534)
                        : const Color(0xFF92400E),
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
      drawer: Drawer(
        child: SafeArea(
          child: Column(
            children: [
              _DrawerHeader(
                session: session,
                mode: mode,
              ),
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  children: AppMenu.values.map((item) {
                    final meta = _menuMeta(item);
                    final selected = _selectedMenu == item;
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 4),
                      child: ListTile(
                        leading: Icon(
                          meta.icon,
                          color: selected
                              ? const Color(0xFF16A34A)
                              : const Color(0xFF9CA3AF),
                        ),
                        title: Text(
                          meta.label,
                          style: TextStyle(
                            fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
                            color: selected
                                ? const Color(0xFF166534)
                                : const Color(0xFF374151),
                          ),
                        ),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(14),
                        ),
                        tileColor:
                            selected ? const Color(0xFFF0FDF4) : Colors.transparent,
                        onTap: () {
                          setState(() {
                            _selectedMenu = item;
                          });
                          Navigator.pop(context);
                        },
                      ),
                    );
                  }).toList(),
                ),
              ),
              const Divider(height: 1),
              Padding(
                padding: const EdgeInsets.all(12),
                child: ListTile(
                  leading: const Icon(Icons.logout_rounded),
                  title: const Text('Sair'),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                  onTap: _handleLogout,
                ),
              ),
            ],
          ),
        ),
      ),
      body: Column(
        children: [
          Container(
            width: double.infinity,
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: const Color(0xFF111827),
                      ),
                ),
                const SizedBox(height: 4),
                Text(
                  '${auth.farmName} - ${auth.farmRole}',
                  style: const TextStyle(
                    color: Color(0xFF6B7280),
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          ),
          const Divider(height: 1),
          Expanded(child: _buildPage()),
        ],
      ),
    );
  }

  Widget _buildPage() {
    switch (_selectedMenu) {
      case AppMenu.dashboard:
        return DashboardSummaryPage(
          onOpenAnimals: () => setState(() => _selectedMenu = AppMenu.animais),
          onOpenEvents: () => setState(() => _selectedMenu = AppMenu.eventos),
        );
      case AppMenu.animais:
        return const AnimaisListScreen();
      case AppMenu.eventos:
        return const EventosPage();
      case AppMenu.fazendas:
        return const _PlaceholderPage(
          icon: Icons.business_rounded,
          title: 'Gestao de Fazendas',
          description:
              'Estrutura do menu alinhada com o frontend. Esta area pode receber o fluxo completo da web em seguida.',
        );
      case AppMenu.lotes:
        return const _PlaceholderPage(
          icon: Icons.inventory_2_rounded,
          title: 'Lotes',
          description: 'Menu igual ao front pronto para receber a tela mobile de lotes.',
        );
      case AppMenu.calendario:
        return const _PlaceholderPage(
          icon: Icons.calendar_month_rounded,
          title: 'Calendario',
          description: 'Calendario operacional alinhado ao menu principal do SaaS.',
        );
      case AppMenu.despesas:
        return const _PlaceholderPage(
          icon: Icons.attach_money_rounded,
          title: 'Despesas',
          description: 'Espaco reservado para o modulo financeiro do frontend.',
        );
      case AppMenu.relatorios:
        return const _PlaceholderPage(
          icon: Icons.bar_chart_rounded,
          title: 'Relatorios',
          description: 'Mesma navegacao do front, com area preparada para relatorios.',
        );
      case AppMenu.alertas:
        return const _PlaceholderPage(
          icon: Icons.warning_amber_rounded,
          title: 'Alertas e IA',
          description: 'Painel para alertas inteligentes e recomendacoes de IA.',
        );
      case AppMenu.veterinarios:
        return const _PlaceholderPage(
          icon: Icons.groups_rounded,
          title: 'Veterinarios',
          description: 'Espaco pronto para compartilhamento com equipe veterinaria.',
        );
      case AppMenu.assinatura:
        return const _PlaceholderPage(
          icon: Icons.credit_card_rounded,
          title: 'Assinatura',
          description: 'Area preparada para status do plano e cobrancas.',
        );
      case AppMenu.configuracoes:
        return const _PlaceholderPage(
          icon: Icons.settings_rounded,
          title: 'Configuracoes',
          description: 'Preferencias e parametros do app mobile.',
        );
    }
  }

  _MenuMeta _menuMeta(AppMenu item) {
    switch (item) {
      case AppMenu.dashboard:
        return const _MenuMeta('Dashboard', Icons.home_rounded);
      case AppMenu.fazendas:
        return const _MenuMeta('Gestao de Fazendas', Icons.business_rounded);
      case AppMenu.animais:
        return const _MenuMeta('Animais', Icons.pets_rounded);
      case AppMenu.lotes:
        return const _MenuMeta('Lotes', Icons.inventory_2_rounded);
      case AppMenu.eventos:
        return const _MenuMeta('Eventos', Icons.bolt_rounded);
      case AppMenu.calendario:
        return const _MenuMeta('Calendario', Icons.calendar_month_rounded);
      case AppMenu.despesas:
        return const _MenuMeta('Despesas', Icons.attach_money_rounded);
      case AppMenu.relatorios:
        return const _MenuMeta('Relatorios', Icons.bar_chart_rounded);
      case AppMenu.alertas:
        return const _MenuMeta('Alertas e IA', Icons.warning_amber_rounded);
      case AppMenu.veterinarios:
        return const _MenuMeta('Veterinarios', Icons.groups_rounded);
      case AppMenu.assinatura:
        return const _MenuMeta('Assinatura', Icons.credit_card_rounded);
      case AppMenu.configuracoes:
        return const _MenuMeta('Configuracoes', Icons.settings_rounded);
    }
  }
}

class _DrawerHeader extends StatelessWidget {
  const _DrawerHeader({
    required this.session,
    required this.mode,
  });

  final AppSession? session;
  final SessionMode mode;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(16, 20, 16, 16),
      decoration: const BoxDecoration(
        border: Border(
          bottom: BorderSide(color: Color(0xFFE5E7EB)),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const CircleAvatar(
            radius: 28,
            backgroundColor: Color(0xFFDCFCE7),
            child: Icon(Icons.agriculture, color: Color(0xFF166534)),
          ),
          const SizedBox(height: 12),
          Text(
            session?.userName ?? 'Conta',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 4),
          Text(
            session?.email ?? '',
            style: const TextStyle(color: Color(0xFF6B7280)),
          ),
          const SizedBox(height: 10),
          Text(
            session?.farmName ?? 'Fazenda',
            style: const TextStyle(
              color: Color(0xFF111827),
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            mode == SessionMode.online ? 'Conectado em producao' : 'Usando cache offline',
            style: TextStyle(
              color: mode == SessionMode.online
                  ? const Color(0xFF166534)
                  : const Color(0xFF92400E),
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _PlaceholderPage extends StatelessWidget {
  const _PlaceholderPage({
    required this.icon,
    required this.title,
    required this.description,
  });

  final IconData icon;
  final String title;
  final String description;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        Card(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    color: const Color(0xFFF0FDF4),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Icon(icon, color: const Color(0xFF16A34A), size: 30),
                ),
                const SizedBox(height: 16),
                Text(
                  title,
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                const SizedBox(height: 8),
                Text(
                  description,
                  style: const TextStyle(
                    color: Color(0xFF6B7280),
                    height: 1.5,
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _MenuMeta {
  const _MenuMeta(this.label, this.icon);

  final String label;
  final IconData icon;
}
