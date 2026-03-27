import 'package:shared_preferences/shared_preferences.dart';
import 'package:jwt_decode/jwt_decode.dart';

class AuthService {
  static const String _tokenKey = 'auth_token';
  static const String _userKey = 'user_data';

  Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, token);
  }

  Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenKey);
  }

  Future<void> saveUserData(Map<String, dynamic> userData) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_userKey, userData.toString());
  }

  Future<bool> isAuthenticated() async {
    final token = await getToken();
    if (token == null) return false;

    try {
      // Verifica se o token está expirado
      return !Jwt.isExpired(token);
    } catch (e) {
      return false;
    }
  }

  Future<Map<String, dynamic>?> getTokenClaims() async {
    final token = await getToken();
    if (token == null) return null;

    try {
      return Jwt.parseJwt(token);
    } catch (e) {
      return null;
    }
  }

  Future<String?> getUserId() async {
    final claims = await getTokenClaims();
    return claims?['sub'];
  }

  Future<String?> getFarmId() async {
    final claims = await getTokenClaims();
    return claims?['tenantId'];
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
    await prefs.remove(_userKey);
  }
}
