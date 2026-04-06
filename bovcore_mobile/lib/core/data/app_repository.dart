import '../auth/auth_service.dart';
import '../models/animal.dart';
import '../services/connectivity_service.dart';
import '../services/remote_api_service.dart';
import 'offline_farm_repository.dart';

class AppRepository {
  AppRepository(
    this._authService,
    this._offlineRepository,
    this._remoteApiService,
    this._connectivityService,
  );

  final AuthService _authService;
  final OfflineFarmRepository _offlineRepository;
  final RemoteApiService _remoteApiService;
  final ConnectivityService _connectivityService;

  Future<bool> login({
    required String email,
    required String password,
  }) async {
    final online = await _connectivityService.hasInternetAccess();

    if (online) {
      try {
        final response = await _remoteApiService.login(
          email: email,
          password: password,
        );
        await _authService.saveOnlineSession(response);
        await syncDown();
        return true;
      } on RemoteApiException catch (error) {
        if (error.statusCode == 401 || error.statusCode == 403) {
          rethrow;
        }
      } catch (_) {}
    }

    await _authService.saveOfflineSession(email: email, password: password);
    return false;
  }

  Future<void> syncDown() async {
    if (!await _canUseRemote()) {
      return;
    }

    final token = _authService.token!;
    final animals = await _remoteApiService.fetchAnimals(token);
    await _offlineRepository.replaceAnimals(animals);

    final events = await _remoteApiService.fetchEvents(token);
    await _offlineRepository.replaceEvents(events);
  }

  Future<List<Animal>> listAnimals() async {
    if (await _canUseRemote()) {
      final animals = await _remoteApiService.fetchAnimals(_authService.token!);
      await _offlineRepository.replaceAnimals(animals);
      return animals;
    }

    return _offlineRepository.listAnimals();
  }

  Future<Map<String, int>> getDashboardStats() async {
    final animals = await listAnimals();
    final events = await listRecentEvents(limit: 100);

    final today = DateTime.now().toIso8601String().split('T').first;
    return {
      'totalAnimais': animals.length,
      'animaisAtivos': animals.where((animal) => animal.status == 'ATIVO').length,
      'eventosHoje': events.where((event) => event['data'] == today).length,
    };
  }

  Future<List<Map<String, dynamic>>> listRecentEvents({int limit = 5}) async {
    if (await _canUseRemote()) {
      final events = await _remoteApiService.fetchEvents(_authService.token!);
      await _offlineRepository.replaceEvents(events);
      return events.take(limit).toList();
    }

    return _offlineRepository.listRecentEvents(limit: limit);
  }

  Future<Animal?> findAnimalByCode(String code) async {
    final animals = await listAnimals();
    for (final animal in animals) {
      if (animal.id == code || animal.brinco.toUpperCase() == code.trim().toUpperCase()) {
        return animal;
      }
    }

    return null;
  }

  Future<void> registerEvent({
    required String animalId,
    required String brinco,
    required String tipo,
    required Map<String, dynamic> data,
  }) async {
    if (await _canUseRemote()) {
      try {
        await _remoteApiService.createEvent(
          token: _authService.token!,
          animalId: animalId,
          tipo: tipo,
          data: data,
        );
        await _offlineRepository.registerEvent(
          animalId: animalId,
          brinco: brinco,
          tipo: tipo,
          data: data,
          synced: true,
        );
        return;
      } catch (_) {}
    }

    await _offlineRepository.registerEvent(
      animalId: animalId,
      brinco: brinco,
      tipo: tipo,
      data: data,
      synced: false,
    );
  }

  Future<bool> refreshConnectivity() async {
    final online = await _connectivityService.hasInternetAccess();
    await _authService.updateReachability(online);
    return online;
  }

  Future<bool> _canUseRemote() async {
    if (!_authService.hasRemoteSession || _authService.token == null) {
      return false;
    }

    final online = await _connectivityService.hasInternetAccess();
    await _authService.updateReachability(online);
    return online;
  }
}
