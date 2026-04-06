class Animal {
  Animal({
    required this.id,
    required this.brinco,
    this.nome,
    required this.sexo,
    required this.raca,
    required this.dataNascimento,
    this.pesoAtual,
    required this.categoria,
    required this.status,
    this.lote,
    this.observacoes,
    required this.idade,
  });

  final String id;
  final String brinco;
  final String? nome;
  final String sexo;
  final String raca;
  final String dataNascimento;
  final double? pesoAtual;
  final String categoria;
  final String status;
  final Lote? lote;
  final String? observacoes;
  final int idade;

  factory Animal.fromJson(Map<String, dynamic> json) {
    return Animal(
      id: json['id'] as String,
      brinco: json['brinco'] as String,
      nome: json['nome'] as String?,
      sexo: json['sexo'] as String,
      raca: json['raca'] as String,
      dataNascimento: json['dataNascimento'] as String,
      pesoAtual: (json['pesoAtual'] as num?)?.toDouble(),
      categoria: json['categoria'] as String,
      status: json['status'] as String,
      lote: json['lote'] != null ? Lote.fromJson(json['lote']) : null,
      observacoes: json['observacoes'] as String?,
      idade: json['idade'] as int,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'brinco': brinco,
      'nome': nome,
      'sexo': sexo,
      'raca': raca,
      'dataNascimento': dataNascimento,
      'pesoAtual': pesoAtual,
      'categoria': categoria,
      'status': status,
      'observacoes': observacoes,
    };
  }

  String get displayName => nome ?? 'Brinco $brinco';

  String get categoriaFormatted {
    switch (categoria) {
      case 'BEZERRO':
        return 'Bezerro';
      case 'NOVILHO':
        return 'Novilho';
      case 'NOVILHA':
        return 'Novilha';
      case 'BOI':
        return 'Boi';
      case 'VACA':
        return 'Vaca';
      case 'TOURO':
        return 'Touro';
      case 'MATRIZ':
        return 'Matriz';
      default:
        return categoria;
    }
  }
}

class Lote {
  Lote({
    required this.id,
    required this.nome,
    this.descricao,
  });

  final String id;
  final String nome;
  final String? descricao;

  factory Lote.fromJson(Map<String, dynamic> json) {
    return Lote(
      id: json['id'] as String,
      nome: json['nome'] as String,
      descricao: json['descricao'] as String?,
    );
  }
}
