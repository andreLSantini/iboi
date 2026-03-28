import {
  ArrowRight,
  BarChart3,
  BadgeCheck,
  BrainCircuit,
  CheckCircle2,
  ChevronRight,
  CircleDollarSign,
  ClipboardList,
  MapPinned,
  ShieldCheck,
  Sparkles,
} from 'lucide-react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo_transparente.png';

const pillars = [
  {
    icon: Sparkles,
    title: 'Mais simples para entrar',
    description: 'Crie a conta, cadastre a fazenda inicial e comece a operar no mesmo dia, sem implantar por semanas.',
  },
  {
    icon: BrainCircuit,
    title: 'Mais inteligente para decidir',
    description: 'Transforme pesagem, vacina, lote e pasto em recomendacoes que ajudam a vender melhor e errar menos.',
  },
  {
    icon: MapPinned,
    title: 'Mais visual para operar',
    description: 'Gerencie fazendas, pastos e animais com contexto geografico e uma visao clara do que esta acontecendo.',
  },
];

const highlights = [
  'Trial de entrada com o mesmo nucleo operacional do Basic para iniciar rapido',
  'Gestao de fazendas, pastos, lotes e ficha forte do animal',
  'Billing SaaS pronto para evoluir com upgrade e cobranca recorrente',
  'Base preparada para evoluir depois para score, previsao e decisao economica',
];

const proof = [
  {
    label: 'Entrada rapida',
    value: '1 conta + 1 fazenda + uso imediato',
  },
  {
    label: 'Foco do produto',
    value: 'decisao pecuaria, nao so cadastro',
  },
  {
    label: 'Operacao visual',
    value: 'fazendas, pastos e rebanho no mesmo fluxo',
  },
];

const roadmap = [
  {
    icon: ClipboardList,
    title: 'Operacao organizada',
    text: 'Controle cadastro, vacina, movimentacao, lote e pasto sem espalhar a rotina em planilhas.',
  },
  {
    icon: BarChart3,
    title: 'Decisao com contexto',
    text: 'Veja o que o rebanho produziu, onde esta travando e o que merece atencao primeiro.',
  },
  {
    icon: CircleDollarSign,
    title: 'Receita melhor',
    text: 'Prepare o terreno para mostrar lucro estimado, previsao de venda e ganho por animal.',
  },
];

const plans = [
  {
    name: 'Trial',
    price: '30 dias para validar',
    description: 'Mesmo nucleo operacional do Basic para testar a rotina e validar o produto com menos friccao.',
  },
  {
    name: 'Basic',
    price: 'Operacao do dia a dia',
    description: 'Cadastro completo, pesagem, vacinas, movimentacoes e relatorios simples.',
  },
  {
    name: 'Pro',
    price: 'Em breve',
    description: 'Futuro do produto para leitura economica e financeiro por animal.',
  },
  {
    name: 'Premium',
    price: 'Em breve',
    description: 'Futuro da camada decisoria com previsao, score e recomendacoes.',
  },
];

const dashboardSlides = [
  {
    eyebrow: 'Visao executiva',
    title: 'Panorama da fazenda',
    badge: 'decisao por fazenda',
    content: (
      <div className="mt-4 grid gap-4 lg:grid-cols-[0.92fr_1.08fr]">
        <div className="rounded-[1.4rem] border border-white/10 bg-white/5 p-4">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs uppercase tracking-[0.18em] text-slate-400">Visao geral</p>
              <p className="mt-1 text-xl font-black">Fazenda Santa Luzia</p>
            </div>
            <div className="rounded-2xl bg-primary-500/15 p-3 text-primary-200">
              <BarChart3 className="h-5 w-5" />
            </div>
          </div>

          <div className="mt-4 grid grid-cols-2 gap-3">
            <div className="rounded-2xl bg-slate-950/45 p-3">
              <p className="text-[11px] uppercase tracking-[0.18em] text-slate-400">Animais</p>
              <p className="mt-1 text-lg font-bold">1.284</p>
            </div>
            <div className="rounded-2xl bg-slate-950/45 p-3">
              <p className="text-[11px] uppercase tracking-[0.18em] text-slate-400">Peso medio</p>
              <p className="mt-1 text-lg font-bold">18,6 @</p>
            </div>
            <div className="rounded-2xl bg-slate-950/45 p-3">
              <p className="text-[11px] uppercase tracking-[0.18em] text-slate-400">Lotes</p>
              <p className="mt-1 text-lg font-bold">08</p>
            </div>
            <div className="rounded-2xl bg-slate-950/45 p-3">
              <p className="text-[11px] uppercase tracking-[0.18em] text-slate-400">Alertas</p>
              <p className="mt-1 text-lg font-bold text-amber-300">07</p>
            </div>
          </div>

          <div className="mt-4 rounded-2xl bg-slate-950/45 p-3">
            <div className="flex items-center justify-between text-xs text-slate-400">
              <span>Curva de ganho recente</span>
              <span>ultimos 30 dias</span>
            </div>
            <div className="mt-3 flex h-24 items-end gap-2">
              {[28, 42, 35, 54, 61, 66, 72, 68].map((value, index) => (
                <div key={index} className="flex-1 rounded-t-full bg-gradient-to-t from-primary-600 to-primary-300" style={{ height: `${value}%` }} />
              ))}
            </div>
          </div>
        </div>

        <div className="grid gap-4">
          <div className="rounded-[1.4rem] border border-white/10 bg-white/5 p-4">
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-white">Alertas prioritarios</p>
              <span className="rounded-full bg-amber-300/20 px-3 py-1 text-[11px] font-semibold text-amber-200">7 ativos</span>
            </div>
            <div className="mt-4 space-y-3">
              {[
                'Vacinas vencendo em 12 animais',
                'Pasto 03 com lotacao acima do ideal',
                'Lote de engorda com queda no ganho medio',
              ].map((item) => (
                <div key={item} className="rounded-2xl bg-slate-950/45 px-4 py-3 text-sm text-slate-200">
                  {item}
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-[1.4rem] border border-white/10 bg-gradient-to-br from-primary-900/45 to-emerald-950/70 p-4">
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold">Resumo economico</p>
              <CircleDollarSign className="h-5 w-5 text-amber-300" />
            </div>
            <div className="mt-4 grid grid-cols-3 gap-3">
              <div className="rounded-2xl bg-white/10 p-3">
                <p className="text-[11px] uppercase text-primary-100/80">Custo/cabeca</p>
                <p className="mt-1 text-lg font-bold">R$ 1.248</p>
              </div>
              <div className="rounded-2xl bg-white/10 p-3">
                <p className="text-[11px] uppercase text-primary-100/80">Receita est.</p>
                <p className="mt-1 text-lg font-bold">R$ 2.980</p>
              </div>
              <div className="rounded-2xl bg-white/10 p-3">
                <p className="text-[11px] uppercase text-primary-100/80">Margem</p>
                <p className="mt-1 text-lg font-bold text-emerald-200">+18,4%</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    ),
  },
  {
    eyebrow: 'Operacao visual',
    title: 'Mapa de pastos e lotacao',
    badge: 'manejo por area',
    content: (
      <div className="mt-4 grid gap-4 lg:grid-cols-[1.05fr_0.95fr]">
        <div className="rounded-[1.4rem] border border-white/10 bg-gradient-to-br from-primary-900/45 to-emerald-950/70 p-4">
          <div className="flex items-center gap-3">
            <MapPinned className="h-5 w-5 text-primary-200" />
            <p className="text-sm font-semibold">Gestao visual de pastos</p>
          </div>
          <div className="mt-4 grid grid-cols-4 gap-3">
            <div className="rounded-2xl bg-white/15 p-5" />
            <div className="rounded-2xl bg-white/10 p-5" />
            <div className="rounded-2xl bg-amber-300/30 p-5" />
            <div className="rounded-2xl bg-white/10 p-5" />
            <div className="col-span-2 rounded-2xl bg-white/10 p-5" />
            <div className="rounded-2xl bg-primary-300/35 p-5" />
            <div className="rounded-2xl bg-white/10 p-5" />
          </div>
          <div className="mt-4 flex flex-wrap gap-2 text-[11px] text-primary-100">
            <span className="rounded-full bg-white/10 px-3 py-1">lotacao por area</span>
            <span className="rounded-full bg-white/10 px-3 py-1">pasto ativo</span>
            <span className="rounded-full bg-white/10 px-3 py-1">troca de manejo</span>
          </div>
        </div>

        <div className="grid gap-4">
          <div className="rounded-[1.4rem] border border-white/10 bg-white/5 p-4">
            <p className="text-sm font-semibold text-white">Distribuicao de lotes</p>
            <div className="mt-4 space-y-3">
              {[
                ['Pasto 01', '102 cabecas', 'bg-primary-300/35'],
                ['Pasto 02', '88 cabecas', 'bg-white/10'],
                ['Pasto 03', '134 cabecas', 'bg-amber-300/30'],
                ['Pasto 04', '96 cabecas', 'bg-white/10'],
              ].map(([name, value, tone]) => (
                <div key={name} className={`flex items-center justify-between rounded-2xl ${tone} px-4 py-3`}>
                  <span className="text-sm font-medium text-white">{name}</span>
                  <span className="text-xs font-semibold uppercase tracking-[0.12em] text-slate-200">{value}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-[1.4rem] border border-white/10 bg-white/5 p-4">
            <p className="text-sm font-semibold text-white">Alertas de ocupacao</p>
            <div className="mt-4 space-y-3 text-sm text-slate-200">
              <div className="rounded-2xl bg-slate-950/45 px-4 py-3">Pasto 03 acima da lotacao planejada</div>
              <div className="rounded-2xl bg-slate-950/45 px-4 py-3">Pasto 01 pronto para receber novo lote</div>
              <div className="rounded-2xl bg-slate-950/45 px-4 py-3">Rotacao sugerida para os proximos 5 dias</div>
            </div>
          </div>
        </div>
      </div>
    ),
  },
  {
    eyebrow: 'Ficha forte',
    title: 'Historico completo do animal',
    badge: 'decisao por individuo',
    content: (
      <div className="mt-4 grid gap-4 lg:grid-cols-[1fr_1fr]">
        <div className="rounded-[1.4rem] border border-white/10 bg-white/5 p-4">
          <div className="flex items-center gap-3">
            <ClipboardList className="h-5 w-5 text-sky-300" />
            <p className="text-sm font-semibold">Ficha do animal</p>
          </div>
          <div className="mt-4 rounded-2xl bg-slate-950/45 p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold">Brinco 004821</p>
                <p className="text-xs text-slate-400">Nelore • macho • SISBOV ativo</p>
              </div>
              <span className="rounded-full bg-primary-500/15 px-3 py-1 text-xs font-semibold text-primary-200">
                no pasto 03
              </span>
            </div>
            <div className="mt-4 grid grid-cols-3 gap-2 text-center">
              <div className="rounded-xl bg-white/5 px-3 py-2">
                <p className="text-[11px] uppercase text-slate-400">Peso</p>
                <p className="mt-1 text-sm font-bold">18,9 @</p>
              </div>
              <div className="rounded-xl bg-white/5 px-3 py-2">
                <p className="text-[11px] uppercase text-slate-400">Vacina</p>
                <p className="mt-1 text-sm font-bold">em dia</p>
              </div>
              <div className="rounded-xl bg-white/5 px-3 py-2">
                <p className="text-[11px] uppercase text-slate-400">Score</p>
                <p className="mt-1 text-sm font-bold">A-</p>
              </div>
            </div>
            <div className="mt-4 rounded-xl bg-white/5 px-4 py-3 text-sm text-slate-300">
              RFID, historico de pesagens, movimentacoes e timeline sanitaria no mesmo lugar.
            </div>
          </div>
        </div>

        <div className="grid gap-4">
          <div className="rounded-[1.4rem] border border-white/10 bg-white/5 p-4">
            <p className="text-sm font-semibold text-white">Timeline unica</p>
            <div className="mt-4 space-y-3">
              {[
                ['Hoje', 'Pesagem registrada: 18,9 @'],
                ['2 dias', 'Movimentado para o pasto 03'],
                ['6 dias', 'Vacina aplicada: clostridioses'],
                ['12 dias', 'Entrada no lote de engorda'],
              ].map(([when, text]) => (
                <div key={text} className="flex items-start gap-3 rounded-2xl bg-slate-950/45 px-4 py-3">
                  <div className="mt-1 h-2.5 w-2.5 rounded-full bg-primary-300" />
                  <div>
                    <p className="text-[11px] uppercase tracking-[0.18em] text-slate-400">{when}</p>
                    <p className="mt-1 text-sm text-slate-200">{text}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-[1.4rem] border border-white/10 bg-gradient-to-br from-slate-900 to-slate-800 p-4">
            <div className="flex items-center gap-3">
              <CircleDollarSign className="h-5 w-5 text-amber-300" />
              <p className="text-sm font-semibold">Proxima camada de valor</p>
            </div>
            <p className="mt-4 text-2xl font-black">recomendacao de venda e margem</p>
            <p className="mt-3 text-sm leading-6 text-slate-300">
              O BovCore nasce com a base operacional certa para evoluir para lucro estimado, score e previsao de venda.
            </p>
          </div>
        </div>
      </div>
    ),
  },
];

export default function Landing() {
  const navigate = useNavigate();
  const [activeSlide, setActiveSlide] = useState(0);

  useEffect(() => {
    const interval = window.setInterval(() => {
      setActiveSlide((current) => (current + 1) % dashboardSlides.length);
    }, 9000);

    return () => window.clearInterval(interval);
  }, []);

  return (
    <div className="min-h-screen overflow-hidden bg-[#f6f3eb] text-slate-900">
      <div className="pointer-events-none absolute inset-x-0 top-0 h-[34rem] bg-[radial-gradient(circle_at_top_left,_rgba(34,197,94,0.18),_transparent_34%),radial-gradient(circle_at_top_right,_rgba(180,83,9,0.16),_transparent_30%),linear-gradient(180deg,_#fbf7ef_0%,_#f6f3eb_64%)]" />
      <div className="pointer-events-none absolute right-[-8rem] top-24 h-72 w-72 rounded-full bg-primary-200/40 blur-3xl" />
      <div className="pointer-events-none absolute left-[-6rem] top-[28rem] h-72 w-72 rounded-full bg-amber-200/40 blur-3xl" />

      <div className="relative mx-auto flex min-h-screen max-w-[88rem] flex-col px-6 py-8 sm:px-8 lg:px-12">
        <header className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl border border-white/80 bg-white/80 shadow-sm">
              <img src={logo} alt="BovCore" className="h-10 w-10 object-contain" />
            </div>
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.32em] text-primary-700">BovCore</p>
              <p className="mt-1 text-sm text-slate-600">Sistema de decisao pecuaria para fazendas de corte</p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button onClick={() => navigate('/login')} className="rounded-full border border-slate-300 bg-white/80 px-5 py-2.5 text-sm font-medium text-slate-700 transition hover:border-slate-400 hover:bg-white">
              Entrar
            </button>
            <button onClick={() => navigate('/onboarding')} className="inline-flex items-center gap-2 rounded-full bg-slate-950 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800">
                  Comecar trial
              <ArrowRight className="h-4 w-4" />
            </button>
          </div>
        </header>

        <main className="flex-1 py-10 lg:py-14">
          <section className="grid gap-14 lg:grid-cols-[1.02fr_0.98fr] lg:items-center">
            <div>
              <div className="inline-flex items-center gap-2 rounded-full border border-primary-200 bg-white/80 px-4 py-2 text-sm font-medium text-primary-800 shadow-sm backdrop-blur">
                <Sparkles className="h-4 w-4" />
                Gestao visual, operacao rapida e inteligencia para pecuaria de corte
              </div>

              <h1 className="mt-6 max-w-5xl text-5xl font-black leading-[0.95] tracking-tight text-slate-950 sm:text-6xl lg:text-[5.25rem]">
                Seu rebanho nao precisa de mais um sistema.
                <span className="block text-primary-700">Precisa de decisao melhor.</span>
              </h1>

              <p className="mt-6 max-w-3xl text-lg leading-8 text-slate-700">
                O BovCore une cadastro, operacao de campo, gestao de fazendas e leitura inteligente
                do rebanho para transformar rotina em resultado. Comece rapido, organize o
                manejo e evolua para um produto que ajuda a decidir melhor.
              </p>

              <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                <button
                  onClick={() => navigate('/onboarding')}
                  className="inline-flex items-center justify-center gap-2 rounded-full bg-primary-700 px-7 py-3.5 text-base font-semibold text-white shadow-[0_18px_40px_-18px_rgba(21,128,61,0.55)] transition hover:bg-primary-800"
                >
                  Criar conta trial
                  <ArrowRight className="h-5 w-5" />
                </button>
                <button
                  onClick={() => navigate('/login')}
                  className="inline-flex items-center justify-center gap-2 rounded-full border border-slate-300 bg-white/80 px-7 py-3.5 text-base font-semibold text-slate-800 transition hover:border-slate-400 hover:bg-white"
                >
                  Ver area do produtor
                  <ChevronRight className="h-5 w-5" />
                </button>
              </div>

              <div className="mt-10 grid gap-4 sm:grid-cols-3">
                {proof.map((item) => (
                  <div key={item.label} className="rounded-3xl border border-white/70 bg-white/75 p-4 shadow-[0_18px_50px_-30px_rgba(15,23,42,0.45)] backdrop-blur">
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">{item.label}</p>
                    <p className="mt-2 text-sm font-medium leading-6 text-slate-800">{item.value}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="relative lg:pl-4">
              <div className="absolute inset-x-10 -top-6 h-32 rounded-full bg-primary-300/30 blur-3xl" />
              <div className="relative overflow-hidden rounded-[2rem] border border-slate-200/70 bg-[#111827] p-6 text-white shadow-[0_40px_100px_-35px_rgba(15,23,42,0.6)]">
                <div className="absolute right-6 top-6 hidden rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-semibold text-primary-100 md:block">
                  Trial para entrar
                </div>

                <div className="flex items-center gap-4">
                  <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-white/10">
                    <img src={logo} alt="BovCore" className="h-10 w-10 object-contain" />
                  </div>
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-[0.22em] text-primary-200">Painel de decisao</p>
                    <h2 className="mt-2 text-2xl font-bold">BovCore Dashboard</h2>
                  </div>
                </div>

                <div className="mt-6 rounded-[1.7rem] border border-white/10 bg-[#0f172a] p-4 shadow-inner">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="h-2.5 w-2.5 rounded-full bg-rose-400" />
                      <span className="h-2.5 w-2.5 rounded-full bg-amber-300" />
                      <span className="h-2.5 w-2.5 rounded-full bg-emerald-400" />
                    </div>
                    <div className="rounded-full bg-white/5 px-3 py-1 text-[11px] font-medium text-slate-300">
                      {dashboardSlides[activeSlide].badge}
                    </div>
                  </div>

                  <div className="mt-4 flex items-center justify-between gap-4">
                    <div>
                      <p className="text-xs uppercase tracking-[0.18em] text-slate-400">{dashboardSlides[activeSlide].eyebrow}</p>
                      <p className="mt-1 text-xl font-black text-white">{dashboardSlides[activeSlide].title}</p>
                    </div>

                    <div className="flex items-center gap-2">
                      {dashboardSlides.map((slide, index) => (
                        <button
                          key={slide.title}
                          type="button"
                          aria-label={`Ir para ${slide.title}`}
                          onClick={() => setActiveSlide(index)}
                          className={`h-2.5 rounded-full transition-all ${
                            activeSlide === index ? 'w-8 bg-primary-300' : 'w-2.5 bg-white/20 hover:bg-white/35'
                          }`}
                        />
                      ))}
                    </div>
                  </div>

                  <p className="mt-3 text-xs text-slate-400">
                    A vitrine troca automaticamente a cada alguns segundos, mas voce pode navegar pelos indicadores acima.
                  </p>

                  <div key={dashboardSlides[activeSlide].title} className="animate-[fadeIn_500ms_ease]">
                    {dashboardSlides[activeSlide].content}
                  </div>
                </div>
              </div>
            </div>
          </section>

          <section className="mt-24 grid gap-6 lg:grid-cols-3">
            {pillars.map(({ icon: Icon, title, description }) => (
              <div
                key={title}
                className="rounded-[2rem] border border-slate-200/80 bg-white/80 p-7 shadow-[0_24px_60px_-40px_rgba(15,23,42,0.35)] backdrop-blur"
              >
                <div className="inline-flex rounded-2xl bg-primary-100 p-3 text-primary-700">
                  <Icon className="h-6 w-6" />
                </div>
                <h3 className="mt-5 text-2xl font-bold text-slate-950">{title}</h3>
                <p className="mt-3 text-base leading-7 text-slate-600">{description}</p>
              </div>
            ))}
          </section>

          <section className="mt-24 grid gap-10 rounded-[2.5rem] border border-slate-200/80 bg-white/75 p-8 shadow-[0_30px_80px_-45px_rgba(15,23,42,0.45)] backdrop-blur lg:grid-cols-[0.95fr_1.05fr] lg:p-12">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.26em] text-primary-700">Por que o BovCore</p>
              <h2 className="mt-4 text-4xl font-black leading-tight text-slate-950">
                O produtor nao compra software.
                <span className="block text-amber-700">Compra clareza para decidir.</span>
              </h2>
              <p className="mt-5 max-w-xl text-lg leading-8 text-slate-600">
                Enquanto muita ferramenta para na gestao basica, o BovCore foi desenhado para avancar
                ate o ponto em que a plataforma ajuda a responder: vender agora ou esperar?
              </p>

              <div className="mt-8 space-y-4">
                {highlights.map((item) => (
                  <div key={item} className="flex items-start gap-3 rounded-2xl bg-slate-50 px-4 py-3">
                    <CheckCircle2 className="mt-0.5 h-5 w-5 text-primary-700" />
                    <p className="text-sm leading-6 text-slate-700">{item}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              {roadmap.map(({ icon: Icon, title, text }) => (
                <div key={title} className="rounded-[1.75rem] border border-slate-200 bg-[#faf8f3] p-6">
                  <div className="inline-flex rounded-2xl bg-slate-900 p-3 text-white">
                    <Icon className="h-5 w-5" />
                  </div>
                  <h3 className="mt-4 text-xl font-bold text-slate-950">{title}</h3>
                  <p className="mt-3 text-sm leading-6 text-slate-600">{text}</p>
                </div>
              ))}

              <div className="rounded-[1.75rem] border border-slate-200 bg-gradient-to-br from-amber-100 to-white p-6 md:col-span-2">
                <div className="flex items-center gap-3">
                  <ShieldCheck className="h-5 w-5 text-amber-700" />
                  <p className="text-sm font-semibold uppercase tracking-[0.18em] text-amber-900">Posicionamento</p>
                </div>
                <p className="mt-4 text-2xl font-black leading-tight text-slate-950">
                  Nao somos apenas um sistema de gestao pecuaria.
                </p>
                <p className="mt-2 text-lg font-semibold text-amber-900">Somos um sistema de decisao pecuaria.</p>
              </div>
            </div>
          </section>

          <section className="mt-24">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <p className="text-sm font-semibold uppercase tracking-[0.24em] text-primary-700">Planos que acompanham o crescimento</p>
                <h2 className="mt-3 max-w-3xl text-4xl font-black leading-tight text-slate-950">Entre simples e evolua por valor entregue.</h2>
              </div>
              <button onClick={() => navigate('/onboarding')} className="inline-flex items-center gap-2 rounded-full border border-slate-300 bg-white px-5 py-3 text-sm font-semibold text-slate-900 transition hover:border-slate-400">
                Testar o BovCore
                <ArrowRight className="h-4 w-4" />
              </button>
            </div>

            <div className="mt-8 grid gap-4 lg:grid-cols-4">
              {plans.map((plan, index) => (
                <div
                  key={plan.name}
                  className={`rounded-[1.8rem] border p-6 ${
                    index === 3
                      ? 'border-slate-900 bg-slate-950 text-white shadow-[0_28px_70px_-45px_rgba(15,23,42,0.8)]'
                      : 'border-slate-200 bg-white'
                  }`}
                >
                  <p className={`text-sm font-semibold uppercase tracking-[0.22em] ${index === 3 ? 'text-primary-200' : 'text-slate-500'}`}>
                    {plan.name}
                  </p>
                  <p className={`mt-4 text-2xl font-black ${index === 3 ? 'text-white' : 'text-slate-950'}`}>{plan.price}</p>
                  <p className={`mt-3 text-sm leading-6 ${index === 3 ? 'text-slate-300' : 'text-slate-600'}`}>{plan.description}</p>
                  <div className={`mt-6 h-px ${index === 3 ? 'bg-white/10' : 'bg-slate-200'}`} />
                  <div className="mt-5 flex items-center gap-2 text-sm font-medium">
                    <BadgeCheck className={`h-4 w-4 ${index === 3 ? 'text-primary-200' : 'text-primary-700'}`} />
                    <span>Escalando do operacional para a decisao</span>
                  </div>
                </div>
              ))}
            </div>
          </section>

          <section className="mt-24 rounded-[2.5rem] bg-slate-950 px-8 py-10 text-white shadow-[0_35px_90px_-45px_rgba(15,23,42,0.75)] lg:px-12">
            <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
              <div className="max-w-2xl">
                <p className="text-sm font-semibold uppercase tracking-[0.26em] text-primary-200">Comece agora</p>
                <h2 className="mt-3 text-4xl font-black leading-tight">
                  Coloque sua fazenda no sistema hoje e prepare o caminho para decidir melhor amanha.
                </h2>
                <p className="mt-4 text-base leading-7 text-slate-300">
                  Crie a conta, cadastre a fazenda inicial e entre no BovCore sem depender de implantacao lenta.
                </p>
              </div>

              <div className="flex flex-col gap-3 sm:flex-row">
                <button
                  onClick={() => navigate('/onboarding')}
                  className="inline-flex items-center justify-center gap-2 rounded-full bg-primary-600 px-6 py-3.5 text-base font-semibold text-white transition hover:bg-primary-500"
                >
                  Criar conta trial
                  <ArrowRight className="h-5 w-5" />
                </button>
                <button
                  onClick={() => navigate('/login')}
                  className="inline-flex items-center justify-center gap-2 rounded-full border border-white/15 bg-white/5 px-6 py-3.5 text-base font-semibold text-white transition hover:bg-white/10"
                >
                  Ja tenho conta
                </button>
              </div>
            </div>
          </section>
        </main>
      </div>
    </div>
  );
}
