import 'package:sqflite/sqflite.dart';

import '../models/animal.dart';

class LocalDatabaseService {
  static const String _databaseName = 'bovcore_offline.db';

  Database? _database;

  Future<Database> get database async {
    if (_database != null) {
      return _database!;
    }

    final databasePath = await getDatabasesPath();
    _database = await openDatabase(
      '$databasePath/$_databaseName',
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE animais (
            id TEXT PRIMARY KEY,
            brinco TEXT NOT NULL UNIQUE,
            nome TEXT,
            sexo TEXT NOT NULL,
            raca TEXT NOT NULL,
            data_nascimento TEXT NOT NULL,
            peso_atual REAL,
            categoria TEXT NOT NULL,
            status TEXT NOT NULL,
            lote_id TEXT,
            lote_nome TEXT,
            observacoes TEXT,
            idade INTEGER NOT NULL
          )
        ''');

        await db.execute('''
          CREATE TABLE eventos (
            id TEXT PRIMARY KEY,
            animal_id TEXT NOT NULL,
            brinco TEXT NOT NULL,
            tipo TEXT NOT NULL,
            data TEXT NOT NULL,
            descricao TEXT,
            peso REAL,
            produto TEXT,
            dose TEXT,
            synced INTEGER NOT NULL DEFAULT 0
          )
        ''');
      },
    );

    await _seedIfNeeded();
    return _database!;
  }

  Future<void> _seedIfNeeded() async {
    final db = await database;
    final existing = Sqflite.firstIntValue(
          await db.rawQuery('SELECT COUNT(*) FROM animais'),
        ) ??
        0;

    if (existing > 0) {
      return;
    }

    final batch = db.batch();
    final animais = <Animal>[
      Animal(
        id: 'animal-001',
        brinco: 'BR001',
        nome: 'Estrela',
        sexo: 'FEMEA',
        raca: 'NELLORE',
        dataNascimento: '2024-02-11',
        pesoAtual: 318,
        categoria: 'NOVILHA',
        status: 'ATIVO',
        lote: Lote(id: 'lote-01', nome: 'Maternidade'),
        observacoes: 'Animal adaptado para manejo intensivo.',
        idade: 14,
      ),
      Animal(
        id: 'animal-002',
        brinco: 'BR002',
        nome: 'Brasa',
        sexo: 'MACHO',
        raca: 'ANGUS',
        dataNascimento: '2023-08-19',
        pesoAtual: 402,
        categoria: 'NOVILHO',
        status: 'ATIVO',
        lote: Lote(id: 'lote-02', nome: 'Recria Norte'),
        observacoes: 'Ganho de peso acima da media.',
        idade: 20,
      ),
      Animal(
        id: 'animal-003',
        brinco: 'BR003',
        nome: 'Lua',
        sexo: 'FEMEA',
        raca: 'GIROLANDO',
        dataNascimento: '2021-11-05',
        pesoAtual: 468,
        categoria: 'VACA',
        status: 'ATIVO',
        lote: Lote(id: 'lote-03', nome: 'Lote Leiteiro'),
        observacoes: 'Historico sanitario em dia.',
        idade: 29,
      ),
      Animal(
        id: 'animal-004',
        brinco: 'BR004',
        nome: 'Guardiao',
        sexo: 'MACHO',
        raca: 'TABAPUA',
        dataNascimento: '2020-04-14',
        pesoAtual: 590,
        categoria: 'TOURO',
        status: 'ATIVO',
        lote: Lote(id: 'lote-04', nome: 'Reprodutores'),
        observacoes: 'Usado como referencia de cruzamento.',
        idade: 47,
      ),
    ];

    for (final animal in animais) {
      batch.insert('animais', {
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
      });
    }

    batch.insert('eventos', {
      'id': 'evento-001',
      'animal_id': 'animal-001',
      'brinco': 'BR001',
      'tipo': 'VACINACAO',
      'data': '2026-04-03',
      'descricao': 'Reforco sanitario aplicado em campo.',
      'produto': 'Clostridioses',
      'dose': '5 ml',
      'synced': 0,
    });

    batch.insert('eventos', {
      'id': 'evento-002',
      'animal_id': 'animal-002',
      'brinco': 'BR002',
      'tipo': 'PESAGEM',
      'data': '2026-04-03',
      'descricao': 'Pesagem feita no curral principal.',
      'peso': 402.0,
      'synced': 0,
    });

    await batch.commit(noResult: true);
  }
}
