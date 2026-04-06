import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/animal.dart';

class RemoteApiException implements Exception {
  RemoteApiException(this.message, {this.statusCode});

  final String message;
  final int? statusCode;

  @override
  String toString() => message;
}

class RemoteApiService {
  static const String productionBaseUrl =
      'https://bovcore-back-production.up.railway.app';

  Uri _uri(String path) => Uri.parse('$productionBaseUrl$path');

  Future<Map<String, dynamic>> login({
    required String email,
    required String password,
  }) async {
    final response = await http
        .post(
          _uri('/auth/login'),
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
          body: jsonEncode({
            'email': email.trim(),
            'senha': password,
          }),
        )
        .timeout(const Duration(seconds: 15));

    if (response.statusCode >= 200 && response.statusCode < 300) {
      return jsonDecode(response.body) as Map<String, dynamic>;
    }

    if (response.statusCode == 401 || response.statusCode == 403) {
      throw RemoteApiException(
        'Email ou senha invalidos para o ambiente de producao.',
        statusCode: response.statusCode,
      );
    }

    throw RemoteApiException(
      'Nao foi possivel autenticar no servidor de producao.',
      statusCode: response.statusCode,
    );
  }

  Future<List<Animal>> fetchAnimals(String token) async {
    final response = await http
        .get(
          _uri('/api/animais'),
          headers: _headers(token),
        )
        .timeout(const Duration(seconds: 15));

    final data = _extractList(response);
    return data
        .whereType<Map<String, dynamic>>()
        .map(Animal.fromJson)
        .toList();
  }

  Future<List<Map<String, dynamic>>> fetchEvents(String token) async {
    final response = await http
        .get(
          _uri('/api/eventos'),
          headers: _headers(token),
        )
        .timeout(const Duration(seconds: 15));

    final data = _extractList(response);
    return data.whereType<Map<String, dynamic>>().map(_mapEvent).toList();
  }

  Future<void> createEvent({
    required String token,
    required String animalId,
    required String tipo,
    required Map<String, dynamic> data,
  }) async {
    final payload = {
      'animalId': animalId,
      'tipo': tipo,
      'data': DateTime.now().toIso8601String().split('T').first,
      'descricao': data['descricao'],
      'peso': data['peso'],
      'produto': data['produto'],
      'dose': _parseDouble(data['dose']),
      'unidadeMedida': (data['dose'] != null && '$tipo' != 'PESAGEM') ? 'ml' : null,
    };

    final response = await http
        .post(
          _uri('/api/eventos'),
          headers: _headers(token),
          body: jsonEncode(payload),
        )
        .timeout(const Duration(seconds: 15));

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw RemoteApiException(
        'Nao foi possivel registrar o evento no ambiente de producao.',
        statusCode: response.statusCode,
      );
    }
  }

  List<dynamic> _extractList(http.Response response) {
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw RemoteApiException(
        'Falha ao consultar dados no ambiente de producao.',
        statusCode: response.statusCode,
      );
    }

    final decoded = jsonDecode(response.body);
    if (decoded is List<dynamic>) {
      return decoded;
    }

    if (decoded is Map<String, dynamic>) {
      final content = decoded['content'];
      if (content is List<dynamic>) {
        return content;
      }
    }

    return const [];
  }

  Map<String, String> _headers(String token) {
    return {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }

  Map<String, dynamic> _mapEvent(Map<String, dynamic> json) {
    final animal = json['animal'] as Map<String, dynamic>? ?? {};
    return {
      'id': json['id'],
      'animal_id': animal['id'],
      'brinco': animal['brinco'] ?? '---',
      'tipo': json['tipo'] ?? 'EVENTO',
      'data': (json['data'] as String? ?? '').split('T').first,
      'descricao': json['descricao'],
      'peso': (json['peso'] as num?)?.toDouble(),
      'produto': json['produto'],
      'dose': json['dose']?.toString(),
      'synced': 1,
    };
  }

  double? _parseDouble(dynamic value) {
    if (value == null) {
      return null;
    }

    if (value is num) {
      return value.toDouble();
    }

    return double.tryParse(value.toString().replaceAll(',', '.'));
  }
}
