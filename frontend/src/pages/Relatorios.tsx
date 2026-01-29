import { BarChart3, FileText } from 'lucide-react';

export default function Relatorios() {
  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <BarChart3 className="w-8 h-8 text-primary-600" />
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Relatórios</h1>
          <p className="text-gray-600">Análises e insights do seu rebanho</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {[
          { titulo: 'Evolução de Peso', descricao: 'Gráfico de ganho de peso' },
          { titulo: 'Custos por Animal', descricao: 'Análise de despesas' },
          { titulo: 'Taxa de Mortalidade', descricao: 'Indicadores sanitários' },
          { titulo: 'Produtividade', descricao: 'Métricas de desempenho' },
          { titulo: 'Vacinação', descricao: 'Status do calendário' },
          { titulo: 'Financeiro', descricao: 'Receitas e despesas' },
        ].map((relatorio, index) => (
          <div key={index} className="card hover:shadow-lg transition-shadow cursor-pointer">
            <div className="flex items-start gap-3">
              <div className="bg-primary-100 p-3 rounded-lg">
                <FileText className="w-6 h-6 text-primary-600" />
              </div>
              <div>
                <h3 className="font-bold text-gray-900 mb-1">{relatorio.titulo}</h3>
                <p className="text-sm text-gray-600">{relatorio.descricao}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
