import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:provider/provider.dart';

import '../../core/data/app_repository.dart';
import '../../core/models/animal.dart';

class RegistroRapidoScreen extends StatefulWidget {
  const RegistroRapidoScreen({super.key});

  @override
  State<RegistroRapidoScreen> createState() => _RegistroRapidoScreenState();
}

class _RegistroRapidoScreenState extends State<RegistroRapidoScreen> {
  final MobileScannerController _scannerController = MobileScannerController();
  final TextEditingController _manualCodeController = TextEditingController();
  bool _isProcessing = false;
  Animal? _animal;

  @override
  void dispose() {
    _scannerController.dispose();
    _manualCodeController.dispose();
    super.dispose();
  }

  Future<void> _onQRCodeDetected(BarcodeCapture capture) async {
    if (_isProcessing) {
      return;
    }

    final barcodes = capture.barcodes;
    if (barcodes.isEmpty) {
      return;
    }

    final code = barcodes.first.rawValue;
    if (code == null || code.trim().isEmpty) {
      return;
    }

    await _loadAnimal(code);
  }

  Future<void> _loadAnimal(String code) async {
    setState(() => _isProcessing = true);
    try {
      final repository = context.read<AppRepository>();
      final animal = await repository.findAnimalByCode(code);

      if (animal == null) {
        throw Exception('Animal nao encontrado.');
      }

      _animal = animal;
      if (!mounted) {
        return;
      }

      await _showEventoModal();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erro: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isProcessing = false);
      }
    }
  }

  Future<void> _showEventoModal() async {
    if (_animal == null) {
      return;
    }

    final tipoEvento = await showDialog<String>(
      context: context,
      builder: (context) => _EventoTypeDialog(brinco: _animal!.brinco),
    );

    if (tipoEvento != null && mounted) {
      await _showEventoForm(tipoEvento);
    }
  }

  Future<void> _showEventoForm(String tipo) async {
    if (_animal == null) {
      return;
    }

    final formData = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      isScrollControlled: true,
      builder: (context) => _EventoFormSheet(tipo: tipo, brinco: _animal!.brinco),
    );

    if (formData != null && mounted) {
      await _registrarEvento(tipo, formData);
    }
  }

  Future<void> _registrarEvento(String tipo, Map<String, dynamic> data) async {
    if (_animal == null) {
      return;
    }

    try {
      await context.read<AppRepository>().registerEvent(
            animalId: _animal!.id,
            brinco: _animal!.brinco,
            tipo: tipo,
            data: data,
          );

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Evento registrado com sucesso.'),
            backgroundColor: Color(0xFF16A34A),
          ),
        );
        Navigator.pop(context);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erro ao registrar evento: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Registrar Evento'),
        actions: [
          IconButton(
            icon: const Icon(Icons.flash_on_rounded),
            onPressed: () => _scannerController.toggleTorch(),
          ),
          IconButton(
            icon: const Icon(Icons.flip_camera_ios_rounded),
            onPressed: () => _scannerController.switchCamera(),
          ),
        ],
      ),
      body: Stack(
        children: [
          Column(
            children: [
              Expanded(
                child: MobileScanner(
                  controller: _scannerController,
                  onDetect: _onQRCodeDetected,
                ),
              ),
              Container(
                color: Colors.white,
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 20),
                child: Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _manualCodeController,
                        decoration: const InputDecoration(
                          labelText: 'Brinco ou codigo manual',
                          prefixIcon: Icon(Icons.keyboard_rounded),
                        ),
                        textCapitalization: TextCapitalization.characters,
                        onSubmitted: (value) => _loadAnimal(value),
                      ),
                    ),
                    const SizedBox(width: 12),
                    SizedBox(
                      height: 56,
                      child: ElevatedButton(
                        onPressed: () => _loadAnimal(_manualCodeController.text),
                        child: const Text('Buscar'),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          CustomPaint(
            painter: _ScannerOverlay(),
            child: Container(),
          ),
          Positioned(
            left: 24,
            right: 24,
            bottom: 108,
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.black.withOpacity(0.68),
                borderRadius: BorderRadius.circular(14),
              ),
              child: const Text(
                'Posicione o QR Code do animal ou digite o brinco para registrar no modo online ou offline.',
                style: TextStyle(color: Colors.white),
                textAlign: TextAlign.center,
              ),
            ),
          ),
          if (_isProcessing)
            Container(
              color: Colors.black.withOpacity(0.4),
              child: const Center(
                child: CircularProgressIndicator(color: Colors.white),
              ),
            ),
        ],
      ),
    );
  }
}

class _ScannerOverlay extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.black.withOpacity(0.5)
      ..style = PaintingStyle.fill;

    final scanArea = Rect.fromCenter(
      center: Offset(size.width / 2, size.height / 2),
      width: 250,
      height: 250,
    );

    canvas.drawPath(
      Path()
        ..addRect(Rect.fromLTWH(0, 0, size.width, size.height))
        ..addRRect(RRect.fromRectAndRadius(scanArea, const Radius.circular(16)))
        ..fillType = PathFillType.evenOdd,
      paint,
    );

    final borderPaint = Paint()
      ..color = const Color(0xFF22C55E)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3;

    canvas.drawRRect(
      RRect.fromRectAndRadius(scanArea, const Radius.circular(16)),
      borderPaint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _EventoTypeDialog extends StatelessWidget {
  const _EventoTypeDialog({required this.brinco});

  final String brinco;

  @override
  Widget build(BuildContext context) {
    final tipos = [
      {'tipo': 'PESAGEM', 'icon': Icons.monitor_weight_rounded, 'label': 'Pesagem'},
      {'tipo': 'VACINACAO', 'icon': Icons.vaccines_rounded, 'label': 'Vacinacao'},
      {'tipo': 'MEDICACAO', 'icon': Icons.medication_rounded, 'label': 'Medicacao'},
      {'tipo': 'MOVIMENTACAO', 'icon': Icons.swap_horiz_rounded, 'label': 'Movimentacao'},
    ];

    return AlertDialog(
      title: Text('Animal: $brinco'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: tipos.map((tipo) {
          return ListTile(
            leading: Icon(tipo['icon']! as IconData, color: const Color(0xFF16A34A)),
            title: Text(tipo['label']! as String),
            onTap: () => Navigator.pop(context, tipo['tipo']),
          );
        }).toList(),
      ),
    );
  }
}

class _EventoFormSheet extends StatefulWidget {
  const _EventoFormSheet({required this.tipo, required this.brinco});

  final String tipo;
  final String brinco;

  @override
  State<_EventoFormSheet> createState() => _EventoFormSheetState();
}

class _EventoFormSheetState extends State<_EventoFormSheet> {
  final _formKey = GlobalKey<FormState>();
  final _pesoController = TextEditingController();
  final _produtoController = TextEditingController();
  final _doseController = TextEditingController();
  final _descricaoController = TextEditingController();

  @override
  void dispose() {
    _pesoController.dispose();
    _produtoController.dispose();
    _doseController.dispose();
    _descricaoController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom,
      ),
      child: Container(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                'Registrar ${_getTipoLabel()}',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
              ),
              const SizedBox(height: 8),
              Text(
                'Animal: ${widget.brinco}',
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      color: const Color(0xFF6B7280),
                    ),
              ),
              const SizedBox(height: 24),
              if (widget.tipo == 'PESAGEM') ...[
                TextFormField(
                  controller: _pesoController,
                  keyboardType:
                      const TextInputType.numberWithOptions(decimal: true),
                  decoration: const InputDecoration(
                    labelText: 'Peso (kg) *',
                    prefixIcon: Icon(Icons.monitor_weight_rounded),
                  ),
                  validator: (value) =>
                      value?.isEmpty ?? true ? 'Digite o peso' : null,
                ),
              ],
              if (widget.tipo == 'VACINACAO' || widget.tipo == 'MEDICACAO') ...[
                TextFormField(
                  controller: _produtoController,
                  decoration: const InputDecoration(
                    labelText: 'Produto *',
                    prefixIcon: Icon(Icons.medical_services_rounded),
                  ),
                  validator: (value) =>
                      value?.isEmpty ?? true ? 'Digite o produto' : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _doseController,
                  decoration: const InputDecoration(
                    labelText: 'Dose',
                    prefixIcon: Icon(Icons.opacity_rounded),
                  ),
                ),
              ],
              const SizedBox(height: 16),
              TextFormField(
                controller: _descricaoController,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Observacoes',
                  alignLabelWithHint: true,
                ),
              ),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: () {
                  if (!_formKey.currentState!.validate()) {
                    return;
                  }

                  Navigator.pop(context, {
                    if (widget.tipo == 'PESAGEM')
                      'peso': double.parse(_pesoController.text.replaceAll(',', '.')),
                    if (widget.tipo == 'VACINACAO' || widget.tipo == 'MEDICACAO') ...{
                      'produto': _produtoController.text.trim(),
                      'dose': _doseController.text.trim(),
                    },
                    'descricao': _descricaoController.text.trim(),
                  });
                },
                child: const Text('Salvar Evento'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _getTipoLabel() {
    switch (widget.tipo) {
      case 'PESAGEM':
        return 'Pesagem';
      case 'VACINACAO':
        return 'Vacinacao';
      case 'MEDICACAO':
        return 'Medicacao';
      case 'MOVIMENTACAO':
        return 'Movimentacao';
      default:
        return widget.tipo;
    }
  }
}
