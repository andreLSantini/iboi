import 'package:sqflite/sqflite.dart';
import 'package:uuid/uuid.dart';

import '../models/animal.dart';
import 'local_database_service.dart';

class OfflineFarmRepository {
  OfflineFarmRepository(this._databaseService);

  final LocalDatabaseService _databaseService;
  final Uuid _uuid = const Uuid();

  Future<List<Animal>> listAnimals() async {
    final db = await _databaseService.database;
    final rows = await db.query('animais', orderBy: 'brinco ASC');
    return rows.map(_mapAnimal).toList();
  }

  Future<void> replaceAnimals(List<Animal> animals) async {
    final db = await _databaseService.database;
    final batch = db.batch();
    batch.delete('animais');

    for (final animal in animals) {
      batch.insert('animais', _animalToRow(animal));
    }

    await batch.commit(noResult: true);
  }

  Future<Animal?> findAnimalByCode(String code) async {
    final db = await _databaseService.database;
    final normalizedCode = code.trim().toUpperCase();
    final rows = await db.query(
      'animais',
      where: 'id = ? OR brinco = ?',
      whereArgs: [code.trim(), normalizedCode],
      limit: 1,
    );

    if (rows.isEmpty) {
      return null;
    }

    return _mapAnimal(rows.first);
  }

  Future<Map<String, int>> getDashboardStats() async {
    final db = await _databaseService.database;
    final totalAnimais = Sqflite.firstIntValue(
          await db.rawQuery('SELECT COUNT(*) FROM animais'),
        ) ??
        0;
    final animaisAtivos = Sqflite.firstIntValue(
          await db.rawQuery(
            "SELECT COUNT(*) FROM animais WHERE status = 'ATIVO'",
          ),
        ) ??
        0;
    final eventosHoje = Sqflite.firstIntValue(
          await db.rawQuery(
            'SELECT COUNT(*) FROM eventos WHERE data = ?',
            [DateTime.now().toIso8601String().split('T').first],
          ),
        ) ??
        0;

    return {
      'totalAnimais': totalAnimais,
      'animaisAtivos': animaisAtivos,
      'eventosHoje': eventosHoje,
    };
  }

  Future<List<Map<String, dynamic>>> listRecentEvents({int limit = 5}) async {
    final db = await _databaseService.database;
    final rows = await db.query(
      'eventos',
      orderBy: 'data DESC, rowid DESC',
      limit: limit,
    );

    return rows;
  }

  Future<void> replaceEvents(List<Map<String, dynamic>> events) async {
    final db = await _databaseService.database;
    final batch = db.batch();
    batch.delete('eventos', where: 'synced = ?', whereArgs: [1]);

    for (final event in events) {
      batch.insert('eventos', {
        'id': event['id'] ?? _uuid.v4(),
        'animal_id': event['animal_id'],
        'brinco': event['brinco'],
        'tipo': event['tipo'],
        'data': event['data'],
        'descricao': event['descricao'],
        'peso': event['peso'],
        'produto': event['produto'],
        'dose': event['dose'],
        'synced': event['synced'] ?? 1,
      });
    }

    await batch.commit(noResult: true);
  }

  Future<void> registerEvent({
    required String animalId,
    required String brinco,
    required String tipo,
    required Map<String, dynamic> data,
    required bool synced,
  }) async {
    final db = await _databaseService.database;
    await db.insert('eventos', {
      'id': _uuid.v4(),
      'animal_id': animalId,
      'brinco': brinco,
      'tipo': tipo,
      'data': DateTime.now().toIso8601String().split('T').first,
      'descricao': data['descricao'] as String?,
      'peso': data['peso'] as double?,
      'produto': data['produto'] as String?,
      'dose': data['dose']?.toString(),
      'synced': synced ? 1 : 0,
    });

    if (tipo == 'PESAGEM' && data['peso'] != null) {
      await db.update(
        'animais',
        {'peso_atual': data['peso']},
        where: 'id = ?',
        whereArgs: [animalId],
      );
    }
  }

  Map<String, dynamic> _animalToRow(Animal animal) {
    return {
      'id': animal.id,
      'brinco': animal.brinco,
      'nome': animal.nome,
      'sexo': animal.sexo,
      'raca': animal.raca,
      'data_nascimento': animal.dataNascimento,
      'peso_atual': animal.pesoAtual,
      'categoria': animal.categoria,
      'status': animal.status,
      'lote_id': animal.lote?.id,
      'lote_nome': animal.lote?.nome,
      'observacoes': animal.observacoes,
      'idade': animal.idade,
    };
  }

  Animal _mapAnimal(Map<String, Object?> row) {
    final loteNome = row['lote_nome'] as String?;
    final loteId = row['lote_id'] as String?;
    final peso = row['peso_atual'];

    return Animal(
      id: row['id'] as String,
      brinco: row['brinco'] as String,
      nome: row['nome'] as String?,
      sexo: row['sexo'] as String,
      raca: row['raca'] as String,
      dataNascimento: row['data_nascimento'] as String,
      pesoAtual: peso is num ? peso.toDouble() : null,
      categoria: row['categoria'] as String,
      status: row['status'] as String,
      lote: loteNome != null ? Lote(id: loteId ?? loteNome, nome: loteNome) : null,
      observacoes: row['observacoes'] as String?,
      idade: row['idade'] as int,
    );
  }
}
