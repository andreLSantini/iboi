import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/data/app_repository.dart';
import 'registro_rapido_screen.dart';

class EventosPage extends StatefulWidget {
  const EventosPage({super.key});

  @override
  State<EventosPage> createState() => _EventosPageState();
}

class _EventosPageState extends State<EventosPage> {
  bool _isLoading = true;
  List<Map<String, dynamic>> _events = [];

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _isLoading = true);
    final events = await context.read<AppRepository>().listRecentEvents(limit: 30);
    if (!mounted) {
      return;
    }

    setState(() {
      _events = events;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(20),
          child: Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Row(
                children: [
                  Container(
                    width: 52,
                    height: 52,
                    decoration: BoxDecoration(
                      color: const Color(0xFFF5F3FF),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(Icons.bolt_rounded, color: Color(0xFF7C3AED)),
                  ),
                  const SizedBox(width: 14),
                  const Expanded(
                    child: Text(
                      'Registre pesagem, vacinacao, medicacao e movimentacao com camera ou busca manual.',
                      style: TextStyle(
                        color: Color(0xFF374151),
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  ElevatedButton.icon(
                    onPressed: null,
                    icon: const Icon(Icons.qr_code_scanner_rounded),
                    label: const Text('Scanner'),
                  ),
                ],
              ),
            ),
          ),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 20),
          child: SizedBox(
            width: double.infinity,
            child: ElevatedButton.icon(
              onPressed: () async {
                await Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const RegistroRapidoScreen()),
                );
                if (mounted) {
                  _load();
                }
              },
              icon: const Icon(Icons.qr_code_scanner_rounded),
              label: const Text('Registrar Evento'),
            ),
          ),
        ),
        const SizedBox(height: 16),
        Expanded(
          child: _isLoading
              ? const Center(child: CircularProgressIndicator())
              : RefreshIndicator(
                  onRefresh: _load,
                  child: ListView.builder(
                    padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
                    itemCount: _events.length,
                    itemBuilder: (context, index) {
                      final event = _events[index];
                      return Card(
                        margin: const EdgeInsets.only(bottom: 12),
                        child: ListTile(
                          leading: CircleAvatar(
                            backgroundColor: (event['synced'] ?? 0) == 1
                                ? const Color(0xFFECFDF5)
                                : const Color(0xFFFFFBEB),
                            child: Icon(
                              (event['synced'] ?? 0) == 1
                                  ? Icons.cloud_done_rounded
                                  : Icons.cloud_off_rounded,
                              color: (event['synced'] ?? 0) == 1
                                  ? const Color(0xFF16A34A)
                                  : const Color(0xFFD97706),
                            ),
                          ),
                          title: Text('${event['tipo']} - ${event['brinco']}'),
                          subtitle: Text(
                            event['descricao'] as String? ?? 'Evento operacional',
                          ),
                          trailing: Text(event['data'] as String? ?? ''),
                        ),
                      );
                    },
                  ),
                ),
        ),
      ],
    );
  }
}
