import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'package:provider/provider.dart';
import '../../core/api/api_client.dart';

class RegistroRapidoScreen extends StatefulWidget {
  const RegistroRapidoScreen({super.key});

  @override
  State<RegistroRapidoScreen> createState() => _RegistroRapidoScreenState();
}

class _RegistroRapidoScreenState extends State<RegistroRapidoScreen> {
  final MobileScannerController _scannerController = MobileScannerController();
  bool _isProcessing = false;
  String? _animalId;
  String? _brinco;

  @override
  void dispose() {
    _scannerController.dispose();
    super.dispose();
  }

  void _onQRCodeDetected(BarcodeCapture capture) async {
    if (_isProcessing) return;

    final List<Barcode> barcodes = capture.barcodes;
    if (barcodes.isEmpty) return;

    final String? code = barcodes.first.rawValue;
    if (code == null) return;

    setState(() => _isProcessing = true);

    try {
      // Buscar animal pelo código QR (pode ser ID ou brinco)
      final apiClient = context.read<ApiClient>();

      // Tentar buscar pelo ID primeiro
      try {
        final response = await apiClient.get('/api/animais/$code');
        _animalId = response.data['id'];
        _brinco = response.data['brinco'];
      } catch (e) {
        // Se falhar, buscar pela lista de animais filtrando por brinco
        final response = await apiClient.get('/api/animais');
        final animais = response.data is List
            ? response.data
            : (response.data['content'] ?? []);

        final animal = animais.firstWhere(
          (a) => a['brinco'] == code,
          orElse: () => null,
        );

        if (animal != null) {
          _animalId = animal['id'];
          _brinco = animal['brinco'];
        } else {
          throw Exception('Animal não encontrado');
        }
      }

      // Mostrar modal para registrar evento
      if (mounted) {
        await _showEventoModal();
      }
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
      setState(() => _isProcessing = false);
    }
  }

  Future<void> _showEventoModal() async {
    final tipoEvento = await showDialog<String>(
      context: context,
      builder: (context) => _EventoTypeDialog(brinco: _brinco!),
    );

    if (tipoEvento != null && mounted) {
      await _showEventoForm(tipoEvento);
    }
  }

  Future<void> _showEventoForm(String tipo) async {
    final formData = await showModalBottomSheet<Map<String, dynamic>>(
      context: context,
      isScrollControlled: true,
      builder: (context) => _EventoFormSheet(tipo: tipo, brinco: _brinco!),
    );

    if (formData != null && mounted) {
      await _registrarEvento(tipo, formData);
    }
  }

  Future<void> _registrarEvento(String tipo, Map<String, dynamic> data) async {
    try {
      final apiClient = context.read<ApiClient>();

      await apiClient.post('/api/eventos', data: {
        'animalId': _animalId,
        'tipo': tipo,
        'data': DateTime.now().toIso8601String().split('T')[0],
        ...data,
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Evento registrado com sucesso!'),
            backgroundColor: Color(0xFF22C55E),
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
        title: const Text('Escanear QR Code'),
        actions: [
          IconButton(
            icon: const Icon(Icons.flash_on),
            onPressed: () => _scannerController.toggleTorch(),
          ),
          IconButton(
            icon: const Icon(Icons.flip_camera_ios),
            onPressed: () => _scannerController.switchCamera(),
          ),
        ],
      ),
      body: Stack(
        children: [
          MobileScanner(
            controller: _scannerController,
            onDetect: _onQRCodeDetected,
          ),

          // Overlay com guia de posicionamento
          CustomPaint(
            painter: _ScannerOverlay(),
            child: Container(),
          ),

          // Instruções
          Positioned(
            bottom: 32,
            left: 0,
            right: 0,
            child: Container(
              margin: const EdgeInsets.symmetric(horizontal: 32),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.black.withOpacity(0.7),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Text(
                'Posicione o QR Code do animal dentro da área destacada',
                style: TextStyle(color: Colors.white),
                textAlign: TextAlign.center,
              ),
            ),
          ),

          // Loading
          if (_isProcessing)
            Container(
              color: Colors.black.withOpacity(0.5),
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
        ..addRRect(RRect.fromRectAndRadius(scanArea, const Radius.circular(12)))
        ..fillType = PathFillType.evenOdd,
      paint,
    );

    // Bordas do scan area
    final borderPaint = Paint()
      ..color = const Color(0xFF22C55E)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3;

    canvas.drawRRect(
      RRect.fromRectAndRadius(scanArea, const Radius.circular(12)),
      borderPaint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _EventoTypeDialog extends StatelessWidget {
  final String brinco;

  const _EventoTypeDialog({required this.brinco});

  @override
  Widget build(BuildContext context) {
    final tipos = [
      {'tipo': 'PESAGEM', 'icon': Icons.scale, 'label': 'Pesagem'},
      {'tipo': 'VACINACAO', 'icon': Icons.vaccines, 'label': 'Vacinação'},
      {'tipo': 'MEDICACAO', 'icon': Icons.medication, 'label': 'Medicação'},
      {'tipo': 'MOVIMENTACAO', 'icon': Icons.move_up, 'label': 'Movimentação'},
    ];

    return AlertDialog(
      title: Text('Animal: $brinco'),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: tipos.map((tipo) {
          return ListTile(
            leading: Icon(tipo['icon'] as IconData, color: const Color(0xFF22C55E)),
            title: Text(tipo['label'] as String),
            onTap: () => Navigator.pop(context, tipo['tipo']),
          );
        }).toList(),
      ),
    );
  }
}

class _EventoFormSheet extends StatefulWidget {
  final String tipo;
  final String brinco;

  const _EventoFormSheet({required this.tipo, required this.brinco});

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
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    labelText: 'Peso (kg) *',
                    prefixIcon: Icon(Icons.scale),
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
                    prefixIcon: Icon(Icons.medical_services),
                  ),
                  validator: (value) =>
                      value?.isEmpty ?? true ? 'Digite o produto' : null,
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _doseController,
                  decoration: const InputDecoration(
                    labelText: 'Dose',
                    prefixIcon: Icon(Icons.colorize),
                  ),
                ),
              ],

              const SizedBox(height: 16),
              TextFormField(
                controller: _descricaoController,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Observações',
                  alignLabelWithHint: true,
                ),
              ),
              const SizedBox(height: 24),

              // ElevatedButton(
              //   onPressed: () {
              //     if (_formKey.currentState!.validate()) {
              //       Navigator.pop(context, {
              //         if (widget.tipo == 'PESAGEM')
              //           'peso': double.parse(_pesoController.text),
              //         if (widget.tipo == 'VACINACAO' || widget.tipo == 'MEDICACAO') ...[
              //           'produto': _produtoController.text,
              //           'dose': _doseController.text,
              //           'unidadeMedida': 'ml',
              //         ],
              //         'descricao': _descricaoController.text,
              //       });
              //     }
              //   },
              //   child: const Text('Registrar Evento'),
              // ),
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
        return 'Vacinação';
      case 'MEDICACAO':
        return 'Medicação';
      case 'MOVIMENTACAO':
        return 'Movimentação';
      default:
        return widget.tipo;
    }
  }
}
