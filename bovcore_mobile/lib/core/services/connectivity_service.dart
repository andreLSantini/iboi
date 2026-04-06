import 'dart:async';
import 'dart:io';

class ConnectivityService {
  static const String _host = 'bovcore-back-production.up.railway.app';

  Future<bool> hasInternetAccess() async {
    try {
      final client = HttpClient()..connectionTimeout = const Duration(seconds: 5);
      final request = await client
          .getUrl(Uri.parse('https://$_host'))
          .timeout(const Duration(seconds: 5));
      final response = await request.close().timeout(const Duration(seconds: 5));
      client.close();
      return response.statusCode >= 200 && response.statusCode < 500;
    } catch (_) {
      try {
        final result = await InternetAddress.lookup(_host)
            .timeout(const Duration(seconds: 5));
        return result.isNotEmpty && result.first.rawAddress.isNotEmpty;
      } catch (_) {
        return false;
      }
    }
  }
}
