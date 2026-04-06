import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/data/app_repository.dart';
import '../../core/models/animal.dart';

class AnimaisListScreen extends StatefulWidget {
  const AnimaisListScreen({super.key});

  @override
  State<AnimaisListScreen> createState() => _AnimaisListScreenState();
}

class _AnimaisListScreenState extends State<AnimaisListScreen> {
  List<Animal> _animais = [];
  List<Animal> _filteredAnimais = [];
  bool _isLoading = true;
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    _loadAnimais();
  }

  Future<void> _loadAnimais() async {
    setState(() => _isLoading = true);

    try {
      final repository = context.read<AppRepository>();
      final data = await repository.listAnimals();

      if (!mounted) {
        return;
      }

      setState(() {
        _animais = data;
        _filteredAnimais = _applyFilter(_searchQuery, data);
        _isLoading = false;
      });
    } catch (e) {
      if (!mounted) {
        return;
      }

      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Erro ao carregar animais: $e')),
      );
    }
  }

  void _filterAnimais(String query) {
    setState(() {
      _searchQuery = query;
      _filteredAnimais = _applyFilter(query, _animais);
    });
  }

  List<Animal> _applyFilter(String query, List<Animal> animais) {
    if (query.isEmpty) {
      return animais;
    }

    return animais.where((animal) {
      return animal.brinco.toLowerCase().contains(query.toLowerCase()) ||
          (animal.nome?.toLowerCase().contains(query.toLowerCase()) ?? false);
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(20),
          child: TextField(
            decoration: InputDecoration(
              hintText: 'Buscar por brinco ou nome...',
              prefixIcon: const Icon(Icons.search),
              suffixIcon: _searchQuery.isNotEmpty
                  ? IconButton(
                      icon: const Icon(Icons.clear),
                      onPressed: () => _filterAnimais(''),
                    )
                  : const Icon(Icons.pets_rounded),
            ),
            onChanged: _filterAnimais,
          ),
        ),
        Expanded(
          child: _isLoading
              ? const Center(child: CircularProgressIndicator())
              : _filteredAnimais.isEmpty
                  ? Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.pets_outlined,
                            size: 64,
                            color: Colors.grey.shade400,
                          ),
                          const SizedBox(height: 16),
                          Text(
                            _searchQuery.isEmpty
                                ? 'Nenhum animal encontrado'
                                : 'Nenhum resultado para a busca',
                            style: TextStyle(
                              color: Colors.grey.shade600,
                              fontSize: 16,
                            ),
                          ),
                        ],
                      ),
                    )
                  : RefreshIndicator(
                      onRefresh: _loadAnimais,
                      child: ListView.builder(
                        padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
                        itemCount: _filteredAnimais.length,
                        itemBuilder: (context, index) {
                          final animal = _filteredAnimais[index];
                          return _AnimalCard(animal: animal);
                        },
                      ),
                    ),
        ),
      ],
    );
  }
}

class _AnimalCard extends StatelessWidget {
  const _AnimalCard({required this.animal});

  final Animal animal;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: const Color(0xFFF0FDF4),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(
                Icons.pets_rounded,
                color: Color(0xFF16A34A),
                size: 28,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    animal.nome?.isNotEmpty == true
                        ? '${animal.nome} (${animal.brinco})'
                        : 'Brinco ${animal.brinco}',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w700,
                        ),
                  ),
                  const SizedBox(height: 6),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      _InfoChip(label: animal.categoriaFormatted),
                      _InfoChip(
                        label: '${animal.idade} meses',
                        icon: Icons.cake_outlined,
                      ),
                      if (animal.pesoAtual != null)
                        _InfoChip(
                          label: '${animal.pesoAtual} kg',
                          icon: Icons.monitor_weight_outlined,
                        ),
                      if (animal.lote != null)
                        _InfoChip(
                          label: animal.lote!.nome,
                          icon: Icons.inventory_2_outlined,
                        ),
                    ],
                  ),
                ],
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(
                color: _getStatusColor(animal.status).withOpacity(0.12),
                borderRadius: BorderRadius.circular(999),
              ),
              child: Text(
                animal.status,
                style: TextStyle(
                  color: _getStatusColor(animal.status),
                  fontWeight: FontWeight.w700,
                  fontSize: 12,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Color _getStatusColor(String status) {
    switch (status) {
      case 'ATIVO':
        return const Color(0xFF16A34A);
      case 'VENDIDO':
        return const Color(0xFF2563EB);
      case 'MORTO':
        return const Color(0xFFDC2626);
      default:
        return const Color(0xFF6B7280);
    }
  }
}

class _InfoChip extends StatelessWidget {
  const _InfoChip({required this.label, this.icon});

  final String label;
  final IconData? icon;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: const Color(0xFFF3F4F6),
        borderRadius: BorderRadius.circular(999),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, size: 14, color: const Color(0xFF6B7280)),
            const SizedBox(width: 4),
          ],
          Text(
            label,
            style: const TextStyle(
              color: Color(0xFF6B7280),
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}
