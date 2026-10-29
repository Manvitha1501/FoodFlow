import React from 'react'
import { motion } from 'framer-motion'
import { Medal, MapPin, ShieldCheck, Truck, ChevronRight, Sparkles } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

export default function RankedList({ ngos = [], onSelectNgo = null }) {
  const navigate = useNavigate()

  const getMedal = (rank) => {
    if (rank === 1) return <Medal className="w-5 h-5 text-amber-500" />
    if (rank === 2) return <Medal className="w-5 h-5 text-slate-400" />
    if (rank === 3) return <Medal className="w-5 h-5 text-amber-700" />
    return <span className="text-xs font-extrabold text-slate-600">#{rank}</span>
  }

  return (
    <div className="space-y-4">
      {ngos.map((ngo, idx) => (
        <motion.div
          key={ngo.id}
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: idx * 0.08 }}
          className={`p-5 md:p-6 rounded-3xl border transition-all shadow-sm ${
            ngo.rank === 1
              ? 'bg-gradient-to-r from-emerald-50/80 via-white to-white border-emerald-400 shadow-md shadow-emerald-500/10'
              : 'bg-white border-slate-200 hover:border-emerald-300 hover:shadow-md'
          }`}
        >
          <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-5">
            {/* Left Info Header */}
            <div className="flex items-start gap-4">
              <div className="w-10 h-10 rounded-2xl bg-slate-100 flex items-center justify-center shrink-0 border border-slate-200 text-slate-700 shadow-xs">
                {getMedal(ngo.rank)}
              </div>

              <div className="space-y-1">
                <div className="flex items-center gap-2.5 flex-wrap">
                  <h3 className="text-base md:text-lg font-extrabold text-slate-900 tracking-tight">{ngo.name}</h3>
                  {ngo.rank === 1 && (
                    <span className="text-[10px] px-2.5 py-0.5 rounded-full font-black bg-emerald-100 text-emerald-800 border border-emerald-300 uppercase tracking-wider">
                      ★ Top Match
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-4 text-xs text-slate-600 flex-wrap font-medium">
                  <span className="flex items-center gap-1 text-slate-600">
                    <MapPin className="w-3.5 h-3.5 text-slate-400" /> {ngo.distanceKm} km away
                  </span>
                  <span className="flex items-center gap-1 text-emerald-700 font-extrabold">
                    <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" /> {ngo.trustScore}% Trust
                  </span>
                  <span className="flex items-center gap-1 text-amber-700 font-semibold">
                    <Truck className="w-3.5 h-3.5 text-amber-600" /> {ngo.pickupAvailability}
                  </span>
                </div>
              </div>
            </div>

            {/* Right Suitability & Action Buttons */}
            <div className="flex items-center justify-between lg:justify-end gap-6 pt-3 lg:pt-0 border-t lg:border-t-0 border-slate-100">
              <div className="text-right">
                <span className="text-xs text-slate-500 font-bold uppercase tracking-wider block">Suitability Match</span>
                <div className="flex items-center gap-2.5 mt-1">
                  <div className="w-24 bg-slate-100 rounded-full h-2.5 overflow-hidden hidden sm:block border border-slate-200">
                    <div 
                      className={`h-full rounded-full ${
                        ngo.suitabilityScore >= 90 ? 'bg-emerald-600' : ngo.suitabilityScore >= 75 ? 'bg-amber-500' : 'bg-red-500'
                      }`}
                      style={{ width: `${ngo.suitabilityScore}%` }}
                    />
                  </div>
                  <span className="text-xl font-black text-emerald-700">{ngo.suitabilityScore}%</span>
                </div>
              </div>

              <div className="flex items-center gap-2.5">
                <button
                  onClick={() => navigate('/explainability/DEC-9042')}
                  className="text-xs px-3.5 py-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold transition-colors flex items-center gap-1 border border-slate-200"
                  title="Why this ranking?"
                >
                  Why this?
                </button>
                
                <button
                  onClick={() => navigate(`/donation/DON-9042/allocate`)}
                  className="text-xs px-4.5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold shadow-md shadow-emerald-500/20 transition-all flex items-center gap-1"
                >
                  Allocate <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>

          {/* Insight Box with proper padding and background container */}
          {ngo.rankReasoning && (
            <div className="mt-4 p-3.5 rounded-2xl bg-emerald-50/80 border border-emerald-200/90 text-xs text-slate-700 flex items-start gap-2.5">
              <span className="text-[11px] font-extrabold text-emerald-800 bg-emerald-100 px-2 py-0.5 rounded-md border border-emerald-300/80 shrink-0">
                Platform Insight
              </span>
              <span className="font-medium text-slate-700 leading-relaxed">{ngo.rankReasoning}</span>
            </div>
          )}
        </motion.div>
      ))}
    </div>
  )
}
