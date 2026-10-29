import React, { useState } from 'react'
import { motion } from 'framer-motion'
import { PlusCircle, MinusCircle, Layers, BarChart2 } from 'lucide-react'

export default function ContributionWaterfall({ factors = [], summaryText = '' }) {
  const [viewMode, setViewMode] = useState('waterfall') // 'waterfall' | 'bar'

  // Compute cumulative baseline for SHAP waterfall calculation
  let runningTotal = 0
  const waterfallData = factors.map((item) => {
    const start = runningTotal
    const end = item.type === 'base' ? item.impact : start + item.impact
    if (item.type !== 'base') {
      runningTotal += item.impact
    } else {
      runningTotal = item.impact
    }
    return {
      ...item,
      start,
      end
    }
  })

  return (
    <div className="space-y-6">
      {/* Top Toggle Controls */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 p-5 rounded-3xl bg-white border border-slate-200 shadow-xs">
        <div>
          <h4 className="text-sm font-extrabold text-slate-900">SHAP AI Feature Contribution Breakdown</h4>
          <p className="text-xs text-slate-600">Quantitative attribution of factors influencing the selected NGO rank.</p>
        </div>

        <div className="flex items-center gap-1 p-1 rounded-xl bg-slate-100 border border-slate-200">
          <button
            onClick={() => setViewMode('waterfall')}
            className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${
              viewMode === 'waterfall'
                ? 'bg-emerald-600 text-white shadow-md'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Layers className="w-3.5 h-3.5" /> SHAP Waterfall
          </button>
          <button
            onClick={() => setViewMode('bar')}
            className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${
              viewMode === 'bar'
                ? 'bg-emerald-600 text-white shadow-md'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <BarChart2 className="w-3.5 h-3.5" /> Ranked Bars
          </button>
        </div>
      </div>

      {/* View Mode 1: SHAP Waterfall View */}
      {viewMode === 'waterfall' ? (
        <div className="p-6 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-4">
          <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
            Waterfall Attribution Flow (Baseline 50% → Final 95%)
          </div>

          <div className="space-y-3.5">
            {waterfallData.map((item, idx) => {
              const isPositive = item.impact > 0
              const isBase = item.type === 'base'
              
              return (
                <div key={idx} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <div className="flex items-center gap-2">
                      {isBase ? (
                        <span className="w-2 h-2 rounded-full bg-slate-400" />
                      ) : isPositive ? (
                        <PlusCircle className="w-4 h-4 text-emerald-600 shrink-0" />
                      ) : (
                        <MinusCircle className="w-4 h-4 text-red-600 shrink-0" />
                      )}
                      <span className="font-bold text-slate-800">{item.name}</span>
                      <span className="text-slate-500 font-normal hidden sm:inline">({item.description})</span>
                    </div>
                    <span className={`font-mono font-black ${
                      isBase ? 'text-slate-600' : isPositive ? 'text-emerald-700' : 'text-red-700'
                    }`}>
                      {isBase ? `Baseline ${item.impact}%` : isPositive ? `+${item.impact}%` : `${item.impact}%`}
                    </span>
                  </div>

                  {/* Visual Bar Track */}
                  <div className="w-full bg-slate-100 h-5 rounded-lg overflow-hidden relative border border-slate-200">
                    <motion.div
                      initial={{ width: 0 }}
                      animate={{ width: `${Math.abs(item.impact)}%` }}
                      transition={{ duration: 0.6, delay: idx * 0.1 }}
                      className={`h-full rounded-md flex items-center justify-end px-2 ${
                        isBase
                          ? 'bg-slate-400'
                          : isPositive
                          ? 'bg-gradient-to-r from-emerald-500 to-emerald-600'
                          : 'bg-gradient-to-r from-red-500 to-red-600'
                      }`}
                      style={{
                        marginLeft: `${Math.max(0, item.start)}%`
                      }}
                    >
                      <span className="text-[10px] font-mono font-bold text-white drop-shadow-xs">
                        {item.impact > 0 ? `+${item.impact}` : item.impact}%
                      </span>
                    </motion.div>
                  </div>
                </div>
              )
            })}
          </div>

          <div className="pt-4 border-t border-slate-200 flex items-center justify-between text-sm">
            <span className="font-extrabold text-slate-800">Final Predicted Suitability Score</span>
            <span className="text-xl font-black text-emerald-700">95%</span>
          </div>
        </div>
      ) : (
        /* View Mode 2: Ranked Bar View */
        <div className="p-6 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-4">
          <div className="text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
            Absolute Contribution Magnitude
          </div>
          <div className="space-y-3.5">
            {factors.filter(f => f.type !== 'base').map((item, idx) => {
              const isPositive = item.impact > 0
              const percentage = Math.abs(item.impact) * 2 // scale for bar visibility

              return (
                <div key={idx} className="space-y-1">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-bold text-slate-800">{item.name}</span>
                    <span className={`font-mono font-black ${isPositive ? 'text-emerald-700' : 'text-red-700'}`}>
                      {isPositive ? `+${item.impact}%` : `${item.impact}%`}
                    </span>
                  </div>
                  <div className="w-full bg-slate-100 h-3 rounded-full overflow-hidden border border-slate-200">
                    <motion.div
                      initial={{ width: 0 }}
                      animate={{ width: `${percentage}%` }}
                      transition={{ duration: 0.5, delay: idx * 0.08 }}
                      className={`h-full rounded-full ${isPositive ? 'bg-emerald-600' : 'bg-red-600'}`}
                    />
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
