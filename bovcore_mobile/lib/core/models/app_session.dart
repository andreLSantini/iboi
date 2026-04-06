enum SessionMode {
  online,
  offline,
}

class FarmOption {
  FarmOption({
    required this.id,
    required this.name,
    this.city,
    this.state,
  });

  final String id;
  final String name;
  final String? city;
  final String? state;

  factory FarmOption.fromJson(Map<String, dynamic> json) {
    return FarmOption(
      id: json['id'] as String,
      name: (json['name'] ?? json['nome'] ?? '') as String,
      city: json['city'] as String?,
      state: json['state'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'city': city,
      'state': state,
    };
  }
}

class AppSession {
  AppSession({
    required this.email,
    required this.userName,
    required this.farmName,
    required this.farmId,
    required this.mode,
    this.userId,
    this.role,
    this.farmRole,
    this.token,
    this.farms = const [],
  });

  final String? userId;
  final String email;
  final String userName;
  final String? role;
  final String? farmRole;
  final String farmName;
  final String farmId;
  final String? token;
  final SessionMode mode;
  final List<FarmOption> farms;

  bool get hasRemoteToken => token != null && token!.isNotEmpty;

  AppSession copyWith({
    String? userId,
    String? email,
    String? userName,
    String? role,
    String? farmRole,
    String? farmName,
    String? farmId,
    String? token,
    SessionMode? mode,
    List<FarmOption>? farms,
  }) {
    return AppSession(
      userId: userId ?? this.userId,
      email: email ?? this.email,
      userName: userName ?? this.userName,
      role: role ?? this.role,
      farmRole: farmRole ?? this.farmRole,
      farmName: farmName ?? this.farmName,
      farmId: farmId ?? this.farmId,
      token: token ?? this.token,
      mode: mode ?? this.mode,
      farms: farms ?? this.farms,
    );
  }

  factory AppSession.fromJson(Map<String, dynamic> json) {
    final farmsJson = (json['farms'] as List<dynamic>? ?? [])
        .whereType<Map<String, dynamic>>()
        .map(FarmOption.fromJson)
        .toList();

    return AppSession(
      userId: json['userId'] as String?,
      email: json['email'] as String? ?? '',
      userName: json['userName'] as String? ?? 'Conta',
      role: json['role'] as String?,
      farmRole: json['farmRole'] as String?,
      farmName: json['farmName'] as String? ?? 'Fazenda',
      farmId: json['farmId'] as String? ?? 'offline-farm',
      token: json['token'] as String?,
      mode: (json['mode'] as String?) == 'online'
          ? SessionMode.online
          : SessionMode.offline,
      farms: farmsJson,
    );
  }

  factory AppSession.fromLoginResponse(Map<String, dynamic> json) {
    final usuario = json['usuario'] as Map<String, dynamic>? ?? {};
    final fazenda = json['fazenda'] as Map<String, dynamic>? ?? {};
    final farms = (json['farms'] as List<dynamic>? ?? [])
        .whereType<Map<String, dynamic>>()
        .map(FarmOption.fromJson)
        .toList();

    return AppSession(
      userId: usuario['id'] as String?,
      email: usuario['email'] as String? ?? '',
      userName: usuario['nome'] as String? ?? 'Conta',
      role: usuario['role'] as String?,
      farmRole: usuario['farmRole'] as String?,
      farmName: fazenda['nome'] as String? ?? 'Fazenda',
      farmId: fazenda['id'] as String? ?? (json['defaultFarmId'] as String? ?? ''),
      token: json['accessToken'] as String?,
      mode: SessionMode.online,
      farms: farms,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'userId': userId,
      'email': email,
      'userName': userName,
      'role': role,
      'farmRole': farmRole,
      'farmName': farmName,
      'farmId': farmId,
      'token': token,
      'mode': mode.name,
      'farms': farms.map((farm) => farm.toJson()).toList(),
    };
  }
}
