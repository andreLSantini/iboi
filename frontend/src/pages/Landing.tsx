import { ArrowRight, BadgeCheck, Building2, CreditCard, Sparkles } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

const items = [
  'Cadastro self-service com ativacao imediata',
  'Trial de 30 dias com bloqueio de assinatura',
  'Area autenticada com modulos operacionais',
  'Billing pronto para ligar com gateway real',
];

export default function Landing() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top,_#dcfce7,_#f8fafc_60%)]">
      <div className="mx-auto flex min-h-screen max-w-6xl flex-col justify-center px-6 py-16">
        <div className="mb-5 inline-flex w-fit items-center gap-2 rounded-full border border-primary-200 bg-white/80 px-4 py-2 text-sm text-primary-700 shadow-sm">
          <Sparkles className="h-4 w-4" />
          Plataforma SaaS para gestao pecuaria
        </div>

        <div className="grid gap-10 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
          <div>
            <h1 className="text-5xl font-black tracking-tight text-slate-900 sm:text-6xl">
              Cadastre sua operacao e comece a usar no mesmo dia.
            </h1>
            <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-600">
              O fluxo de onboarding ja cria empresa, fazenda, usuario administrador e trial de 30 dias
              para o cliente entrar e operar sem falar com o time.
            </p>

            <div className="mt-8 flex flex-col gap-3 sm:flex-row">
              <button onClick={() => navigate('/onboarding')} className="btn-primary flex items-center justify-center gap-2 px-6 py-3">
                Comecar trial
                <ArrowRight className="h-5 w-5" />
              </button>
              <button onClick={() => navigate('/login')} className="btn-secondary px-6 py-3">
                Entrar
              </button>
            </div>

            <div className="mt-10 grid gap-3 sm:grid-cols-2">
              {items.map((item) => (
                <div key={item} className="flex items-start gap-3 rounded-2xl border border-slate-200 bg-white/80 p-4 shadow-sm">
                  <BadgeCheck className="mt-0.5 h-5 w-5 text-primary-600" />
                  <p className="text-sm text-slate-700">{item}</p>
                </div>
              ))}
            </div>
          </div>

          <div className="rounded-[2rem] border border-slate-200 bg-white p-8 shadow-[0_30px_100px_-30px_rgba(15,23,42,0.35)]">
            <div className="rounded-3xl bg-slate-900 p-6 text-white">
              <div className="flex items-center gap-3">
                <Building2 className="h-6 w-6 text-primary-300" />
                <p className="text-sm uppercase tracking-[0.2em] text-primary-200">Onboarding</p>
              </div>
              <p className="mt-4 text-4xl font-black">30 dias</p>
              <p className="mt-2 text-sm text-slate-300">
                Trial automatico com acesso imediato ao produto.
              </p>
            </div>

            <div className="mt-5 rounded-3xl border border-slate-200 bg-slate-50 p-6">
              <div className="flex items-center gap-3">
                <CreditCard className="h-6 w-6 text-primary-600" />
                <p className="font-semibold text-slate-900">Billing SaaS</p>
              </div>
              <ul className="mt-4 space-y-3 text-sm text-slate-600">
                <li>Trial com expiracao</li>
                <li>Upgrade de plano</li>
                <li>Historico de pagamentos</li>
                <li>Pronto para integrar com gateway real</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
