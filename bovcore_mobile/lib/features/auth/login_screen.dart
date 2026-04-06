import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../../core/data/app_repository.dart';
import '../../core/services/remote_api_service.dart';
import '../dashboard/dashboard_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _isLoading = false;
  bool _obscurePassword = true;
  String? _errorMessage;
  String? _helperMessage;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _handleLogin() async {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
      _helperMessage = null;
    });

    try {
      final repository = context.read<AppRepository>();
      final usedOnline = await repository.login(
        email: _emailController.text,
        password: _passwordController.text,
      );

      if (!mounted) {
        return;
      }

      setState(() {
        _helperMessage = usedOnline
            ? 'Conectado no ambiente de producao.'
            : 'Sem internet detectada. Entrando no modo offline local.';
      });

      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => const DashboardScreen()),
      );
    } on RemoteApiException catch (error) {
      setState(() {
        _errorMessage = error.message;
      });
    } catch (_) {
      setState(() {
        _errorMessage = 'Nao foi possivel entrar agora.';
      });
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [
              Color(0xFFF0FDF4),
              Color(0xFFDCFCE7),
            ],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(24),
              child: ConstrainedBox(
                constraints: const BoxConstraints(maxWidth: 420),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Column(
                      children: [
                        SizedBox(
                          height: 112,
                          child: Image.asset(
                            'assets/logo_transparente.png',
                            fit: BoxFit.contain,
                          ),
                        ),
                        const SizedBox(height: 28),
                        Text(
                          'Gestao inteligente de gado',
                          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                color: const Color(0xFF334155),
                                fontWeight: FontWeight.w400,
                              ),
                          textAlign: TextAlign.center,
                        ),
                      ],
                    ),
                    const SizedBox(height: 34),
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(24),
                        child: Form(
                          key: _formKey,
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.stretch,
                            children: [
                              Text(
                                'Entrar',
                                style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                                      fontWeight: FontWeight.bold,
                                      color: const Color(0xFF111827),
                                    ),
                              ),
                              const SizedBox(height: 24),
                              TextFormField(
                                controller: _emailController,
                                keyboardType: TextInputType.emailAddress,
                                decoration: const InputDecoration(
                                  labelText: 'E-mail',
                                  hintText: 'seu@email.com',
                                ),
                                validator: (value) {
                                  if (value == null || value.isEmpty) {
                                    return 'Digite seu email';
                                  }
                                  if (!value.contains('@')) {
                                    return 'Digite um email valido';
                                  }
                                  return null;
                                },
                              ),
                              const SizedBox(height: 16),
                              TextFormField(
                                controller: _passwordController,
                                obscureText: _obscurePassword,
                                decoration: InputDecoration(
                                  labelText: 'Senha',
                                  hintText: '********',
                                  suffixIcon: IconButton(
                                    icon: Icon(
                                      _obscurePassword
                                          ? Icons.visibility_outlined
                                          : Icons.visibility_off_outlined,
                                    ),
                                    onPressed: () {
                                      setState(() {
                                        _obscurePassword = !_obscurePassword;
                                      });
                                    },
                                  ),
                                ),
                                validator: (value) {
                                  if (value == null || value.isEmpty) {
                                    return 'Digite sua senha';
                                  }
                                  return null;
                                },
                                ),
                              const SizedBox(height: 18),
                              if (_errorMessage != null)
                                _MessageBox(
                                  message: _errorMessage!,
                                  icon: Icons.error_outline,
                                  background: Colors.red.shade50,
                                  border: Colors.red.shade200,
                                  foreground: Colors.red.shade700,
                                ),
                              if (_helperMessage != null)
                                Padding(
                                  padding: const EdgeInsets.only(top: 12),
                                  child: _MessageBox(
                                    message: _helperMessage!,
                                    icon: Icons.info_outline,
                                    background: const Color(0xFFECFDF5),
                                    border: const Color(0xFFBBF7D0),
                                    foreground: const Color(0xFF166534),
                                  ),
                                ),
                              const SizedBox(height: 18),
                              SizedBox(
                                height: 56,
                                child: ElevatedButton(
                                  onPressed: _isLoading ? null : _handleLogin,
                                  child: _isLoading
                                      ? const SizedBox(
                                          height: 20,
                                          width: 20,
                                          child: CircularProgressIndicator(
                                            strokeWidth: 2,
                                            valueColor:
                                                AlwaysStoppedAnimation(Colors.white),
                                          ),
                                        )
                                      : const Row(
                                          mainAxisAlignment: MainAxisAlignment.center,
                                          children: [
                                            Icon(Icons.login_rounded),
                                            SizedBox(width: 10),
                                            Text(
                                              'Entrar',
                                              style: TextStyle(
                                                fontSize: 16,
                                                fontWeight: FontWeight.w600,
                                              ),
                                            ),
                                          ],
                                        ),
                                ),
                              ),
                              const SizedBox(height: 24),
                              RichText(
                                textAlign: TextAlign.center,
                                text: const TextSpan(
                                  style: TextStyle(
                                    color: Color(0xFF374151),
                                    fontSize: 14,
                                  ),
                                  children: [
                                    TextSpan(text: 'Nao tem uma conta? '),
                                    TextSpan(
                                      text: 'Cadastre-se gratis',
                                      style: TextStyle(
                                        color: Color(0xFF16A34A),
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                              const SizedBox(height: 10),
                              const Text(
                                '30 dias de trial gratuito.',
                                textAlign: TextAlign.center,
                                style: TextStyle(
                                  color: Color(0xFF6B7280),
                                  fontSize: 13,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _MessageBox extends StatelessWidget {
  const _MessageBox({
    required this.message,
    required this.icon,
    required this.background,
    required this.border,
    required this.foreground,
  });

  final String message;
  final IconData icon;
  final Color background;
  final Color border;
  final Color foreground;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: background,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: border),
      ),
      child: Row(
        children: [
          Icon(icon, color: foreground),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              message,
              style: TextStyle(color: foreground),
            ),
          ),
        ],
      ),
    );
  }
}
