import React from 'react'
import { CheckCircle2, AlertTriangle, XCircle } from 'lucide-react'

export default function FactorChecklist({ factors = [] }) {
  const getStatus = (item) => {
    const raw = (item.status || item.impact || 'pass').toString().toLowerCase()
    if (raw.includes('pass') || raw.includes('low') || raw.includes('ok') || raw.includes('good') || raw.includes('positive')) return 'pass'
    if (raw.includes('warn') || raw.includes('med') || raw.includes('moderate')) return 'warn'
    if (raw.includes('fail') || raw.includes('high') || raw.includes('risk')) return 'fail'
    return 'pass'
  }

  const getIcon = (status) => {
    switch (status) {
      case 'pass':
        return <CheckCircle2 className="w-5 h-5 text-emerald-600 shrink-0 mt-0.5" />
      case 'warn':
        return <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
      case 'fail':
      default:
        return <XCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
    }
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
      {factors.map((item, idx) => {
        const status = getStatus(item)
        const noteText = item.note || item.description || ''
        const title = item.factor || item.name || noteText || 'Evaluated Dimension'

        return (
          <div 
            key={idx}
            className="flex items-start gap-3 p-3.5 rounded-2xl bg-white border border-slate-200 shadow-xs hover:border-slate-300 transition-colors"
          >
            {getIcon(status)}
            <div className="flex-1 min-w-0">
              <div className="flex items-center justify-between gap-2">
                <h4 className="text-xs font-bold text-slate-900 truncate">{noteText || title}</h4>
                <span className={`text-[10px] px-2 py-0.5 rounded font-mono font-bold uppercase tracking-wider ${
                  status === 'pass' ? 'bg-emerald-100 text-emerald-800' :
                  status === 'warn' ? 'bg-amber-100 text-amber-800' : 'bg-red-100 text-red-800'
                }`}>
                  {status}
                </span>
              </div>
              {noteText && title !== noteText && (
                <p className="text-[11px] text-slate-500 mt-0.5 leading-relaxed font-medium">{title}</p>
              )}
            </div>
          </div>
        )
      })}
    </div>
  )
}
