import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/auth/auth_service.dart';
import '../../core/data/app_repository.dart';

class DashboardSummaryPage extends StatefulWidget {
  const DashboardSummaryPage({
    super.key,
    required this.onOpenAnimals,
    required this.onOpenEvents,
  });

  final VoidCallback onOpenAnimals;
  final VoidCallback onOpenEvents;

  @override
  State<DashboardSummaryPage> createState() => _DashboardSummaryPageState();
}

class _DashboardSummaryPageState extends State<DashboardSummaryPage> {
  Map<String, int>? _stats;
  List<Map<String, dynamic>> _recentEvents = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _isLoading = true);
    final repository = context.read<AppRepository>();
    await repository.refreshConnectivity();
    final stats = await repository.getDashboardStats();
    final events = await repository.listRecentEvents(limit: 5);
    if (!mounted) {
      return;
    }

    setState(() {
      _stats = stats;
      _recentEvents = events;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    return RefreshIndicator(
      onRefresh: _load,
      child: ListView(
        physics: const AlwaysScrollableScrollPhysics(),
        padding: const EdgeInsets.all(20),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: const Color(0xFFF0FDF4),
                          borderRadius: BorderRadius.circular(14),
                        ),
                        child: const Icon(Icons.insights_rounded,
                            color: Color(0xFF16A34A)),
                      ),
                      const SizedBox(width: 14),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Bem-vindo, ${auth.displayName}.',
                              style: Theme.of(context)
                                  .textTheme
                                  .titleLarge
                                  ?.copyWith(fontWeight: FontWeight.bold),
                            ),
                            const SizedBox(height: 4),
                            Text(
                              'Resumo operacional de ${auth.farmName} com alternancia automatica entre producao e cache offline.',
                              style: const TextStyle(color: Color(0xFF6B7280)),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          if (_isLoading)
            const Padding(
              padding: EdgeInsets.all(32),
              child: Center(child: CircularProgressIndicator()),
            )
          else ...[
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                _KpiCard(
                  title: 'Total de animais',
                  value: '${_stats?['totalAnimais'] ?? 0}',
                  subtitle: 'Base do rebanho',
                  icon: Icons.pets_rounded,
                  color: const Color(0xFF3B82F6),
                ),
                _KpiCard(
                  title: 'Animais ativos',
                  value: '${_stats?['animaisAtivos'] ?? 0}',
                  subtitle: 'Rebanho produtivo',
                  icon: Icons.check_circle_rounded,
                  color: const Color(0xFF22C55E),
                ),
                _KpiCard(
                  title: 'Eventos hoje',
                  value: '${_stats?['eventosHoje'] ?? 0}',
                  subtitle: 'Manejo registrado',
                  icon: Icons.bolt_rounded,
                  color: const Color(0xFFF59E0B),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: _QuickAction(
                    icon: Icons.pets_rounded,
                    title: 'Animais',
                    subtitle: 'Abrir lista completa',
                    onTap: widget.onOpenAnimals,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _QuickAction(
                    icon: Icons.bolt_rounded,
                    title: 'Eventos',
                    subtitle: 'Registrar ou revisar',
                    onTap: widget.onOpenEvents,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Text(
              'Ultimos Registros',
              style: Theme.of(context)
                  .textTheme
                  .titleLarge
                  ?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 12),
            if (_recentEvents.isEmpty)
              const Card(
                child: Padding(
                  padding: EdgeInsets.all(18),
                  child: Text('Nenhum evento encontrado.'),
                ),
              )
            else
              ..._recentEvents.map(
                (event) => Card(
                  margin: const EdgeInsets.only(bottom: 12),
                  child: ListTile(
                    leading: const CircleAvatar(
                      backgroundColor: Color(0xFFF0FDF4),
                      child: Icon(Icons.event_note, color: Color(0xFF166534)),
                    ),
                    title: Text('${event['tipo']} - ${event['brinco']}'),
                    subtitle: Text(
                      event['descricao'] as String? ?? 'Registro operacional',
                    ),
                    trailing: Text(event['data'] as String? ?? ''),
                  ),
                ),
              ),
          ],
        ],
      ),
    );
  }
}

class _KpiCard extends StatelessWidget {
  const _KpiCard({
    required this.title,
    required this.value,
    required this.subtitle,
    required this.icon,
    required this.color,
  });

  final String title;
  final String value;
  final String subtitle;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 220,
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.12),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(icon, color: color),
              ),
              const SizedBox(height: 12),
              Text(title, style: const TextStyle(color: Color(0xFF6B7280))),
              const SizedBox(height: 6),
              Text(
                value,
                style: Theme.of(context)
                    .textTheme
                    .headlineSmall
                    ?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 4),
              Text(subtitle, style: const TextStyle(color: Color(0xFF9CA3AF))),
            ],
          ),
        ),
      ),
    );
  }
}

class _QuickAction extends StatelessWidget {
  const _QuickAction({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Icon(icon, color: const Color(0xFF16A34A)),
              const SizedBox(height: 12),
              Text(
                title,
                style: const TextStyle(
                  fontWeight: FontWeight.w700,
                  color: Color(0xFF111827),
                ),
              ),
              const SizedBox(height: 4),
              Text(
                subtitle,
                style: const TextStyle(color: Color(0xFF6B7280)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
