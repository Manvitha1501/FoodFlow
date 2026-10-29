import React from 'react'
import { motion } from 'framer-motion'
import { TrendingUp, TrendingDown } from 'lucide-react'

export default function KpiCard({
  title,
  value,
  subtext,
  icon: Icon,
  trend = null,
  trendLabel = '',
  highlight = false,
  badge = null
}) {
  return (
    <motion.div
      whileHover={{ y: -3 }}
      className={`relative overflow-hidden p-5 rounded-2xl border transition-all ${
        highlight
          ? 'bg-gradient-to-br from-emerald-50/90 via-white to-slate-50 border-emerald-300 shadow-md shadow-emerald-500/10'
          : 'bg-white border-slate-200 shadow-xs hover:border-slate-300'
      }`}
    >
      {/* Background glow accent */}
      {highlight && (
        <div className="absolute -top-12 -right-12 w-28 h-28 bg-emerald-500/10 rounded-full blur-2xl pointer-events-none" />
      )}

      <div className="flex items-start justify-between">
        <div className="p-2.5 rounded-xl bg-slate-100 border border-slate-200 text-emerald-700 font-bold">
          {Icon && <Icon className="w-5 h-5" />}
        </div>
        {badge && (
          <span className="text-xs px-2.5 py-0.5 rounded-full font-bold bg-emerald-100 text-emerald-800 border border-emerald-300">
            {badge}
          </span>
        )}
      </div>

      <div className="mt-4">
        <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">{title}</span>
        <div className="flex items-baseline gap-2 mt-1">
          <span className="text-2xl lg:text-3xl font-black text-slate-900 tracking-tight">{value}</span>
          {trend !== null && (
            <span className={`inline-flex items-center text-xs font-bold ${
              trend >= 0 ? 'text-emerald-700' : 'text-red-700'
            }`}>
              {trend >= 0 ? <TrendingUp className="w-3.5 h-3.5 mr-0.5" /> : <TrendingDown className="w-3.5 h-3.5 mr-0.5" />}
              {trend >= 0 ? `+${trend}%` : `${trend}%`}
            </span>
          )}
        </div>
        {subtext && (
          <p className="text-xs text-slate-600 mt-1.5">{subtext} {trendLabel && <span className="text-slate-400">· {trendLabel}</span>}</p>
        )}
      </div>
    </motion.div>
  )
}
