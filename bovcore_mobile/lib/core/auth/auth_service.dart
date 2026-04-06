import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/app_session.dart';

class AuthService extends ChangeNotifier {
  static const String _sessionKey = 'app_session_v2';

  AppSession? _session;

  AppSession? get session => _session;

  bool get isLoggedIn => _session != null;

  String get displayName => _session?.userName ?? 'Conta';

  String get farmName => _session?.farmName ?? 'Fazenda';

  String get farmRole => _session?.farmRole ?? 'Admin';

  String? get token => _session?.token;

  bool get hasRemoteSession => (_session?.hasRemoteToken ?? false);

  SessionMode get mode => _session?.mode ?? SessionMode.offline;

  Future<void> restoreSession() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_sessionKey);
    if (raw == null) {
      _session = null;
      return;
    }

    try {
      _session = AppSession.fromJson(jsonDecode(raw) as Map<String, dynamic>);
    } catch (_) {
      _session = null;
    }
  }

  Future<void> saveOnlineSession(Map<String, dynamic> response) async {
    _session = AppSession.fromLoginResponse(response);
    await _persist();
    notifyListeners();
  }

  Future<void> saveOfflineSession({
    required String email,
    required String password,
  }) async {
    final normalizedEmail = email.trim().toLowerCase();
    if (normalizedEmail.isEmpty || password.isEmpty) {
      throw Exception('Credenciais invalidas');
    }

    _session = AppSession(
      email: normalizedEmail,
      userName: _buildDisplayName(normalizedEmail),
      farmName: 'Fazenda Offline',
      farmId: 'offline-farm',
      mode: SessionMode.offline,
      farmRole: 'Admin',
    );
    await _persist();
    notifyListeners();
  }

  Future<void> updateReachability(bool online) async {
    if (_session == null || !_session!.hasRemoteToken) {
      return;
    }

    final newMode = online ? SessionMode.online : SessionMode.offline;
    if (_session!.mode == newMode) {
      return;
    }

    _session = _session!.copyWith(mode: newMode);
    await _persist();
    notifyListeners();
  }

  Future<void> logout() async {
    _session = null;
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_sessionKey);
    notifyListeners();
  }

  Future<void> _persist() async {
    final prefs = await SharedPreferences.getInstance();
    if (_session == null) {
      await prefs.remove(_sessionKey);
      return;
    }

    await prefs.setString(_sessionKey, jsonEncode(_session!.toJson()));
  }

  String _buildDisplayName(String email) {
    final localPart = email.split('@').first.trim();
    if (localPart.isEmpty) {
      return 'Operador Offline';
    }

    final segments = localPart
        .split(RegExp(r'[._-]+'))
        .where((segment) => segment.isNotEmpty)
        .map(
          (segment) =>
              '${segment[0].toUpperCase()}${segment.substring(1).toLowerCase()}',
        )
        .toList();

    return segments.isEmpty ? 'Operador Offline' : segments.join(' ');
  }
}
