import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
  HelpCircle,
  Sparkles,
  CheckCircle2,
  AlertCircle,
  ArrowRight,
  ArrowLeft,
  ShieldCheck,
  Scale,
  Layers
} from 'lucide-react'
import { api } from '../services/api'
import ContributionWaterfall from '../components/donation/ContributionWaterfall'
import { LoadingSkeleton, ErrorState } from '../components/common/LoadingSkeleton'
import { useStore } from '../store/useStore'

export default function Explainability() {
  const { decisionId = 'DEC-9042' } = useParams()
  const navigate = useNavigate()
  const { customDonations } = useStore()

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [data, setData] = useState(null)
  const [showComparison, setShowComparison] = useState(false)

  useEffect(() => {
    async function loadExplanation() {
      try {
        setLoading(true)
        const res = await api.getExplanation(decisionId)
        setData(res)
      } catch (err) {
        setError('Failed to compute SHAP explainability matrices')
      } finally {
        setLoading(false)
      }
    }
    loadExplanation()
  }, [decisionId])

  if (loading) return <LoadingSkeleton count={3} />
  if (error) return <ErrorState message={error} onRetry={() => window.location.reload()} />

  const { targetDonation, winningNgo, runnerUpNgo, summaryText, shapFactors, runnerUpComparison } = data
  const activeCustom = customDonations[0]
  const displayTarget = activeCustom
    ? `${activeCustom.foodType.split('(')[0].trim()} (${activeCustom.mealsCount || 450} meals / ${activeCustom.quantityKg || 180}kg)`
    : targetDonation

  return (
    <div className="space-y-8">
      {/* Navigation Back Button */}
      <div>
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-2 text-xs font-extrabold text-slate-600 hover:text-emerald-700 transition-colors bg-white px-4 py-2 rounded-xl border border-slate-200 shadow-xs cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4 text-emerald-600" />
          <span>Back to NGO Suitability Matching</span>
        </button>
      </div>
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 p-6 md:p-8 rounded-3xl bg-white border border-slate-200 shadow-xs">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-bold border border-emerald-300 mb-2">
            <HelpCircle className="w-3.5 h-3.5" />
            <span>SHAP (Shapley Additive exPlanations) Engine</span>
          </div>
          <h1 className="text-2xl md:text-3xl font-black text-slate-900 tracking-tight">
            Explainable AI Decision Audit
          </h1>
          <p className="text-sm text-slate-600 max-w-2xl mt-1 leading-relaxed font-medium">
            Provides complete mathematical transparency into how the AI model scored and selected recipient NGOs.
          </p>
        </div>

        <button
          onClick={() => setShowComparison(!showComparison)}
          className={`px-5 py-3 rounded-2xl font-bold text-xs transition-all flex items-center justify-center gap-2 shrink-0 ${
            showComparison
              ? 'bg-cyan-600 text-white shadow-md shadow-cyan-500/20'
              : 'bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-200'
          }`}
        >
          <Scale className="w-4 h-4" />
          <span>{showComparison ? 'Hide Runner-Up Compare' : 'Compare with Runner-Up'}</span>
        </button>
      </div>

      {/* Central Decision Card */}
      <div className="p-6 md:p-8 rounded-3xl bg-gradient-to-r from-emerald-600 via-emerald-700 to-teal-800 text-white shadow-xl space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <span className="text-xs font-bold text-emerald-100 uppercase tracking-widest block">
              Audited AI Match Decision #{decisionId}
            </span>
            <h2 className="text-xl md:text-2xl font-black text-white mt-1">
              Target: {displayTarget}
            </h2>
          </div>

          <div className="flex items-center gap-3">
            <div className="text-right">
              <span className="text-xs text-emerald-100 block font-medium">Selected Recipient</span>
              <span className="text-base font-black text-white">{winningNgo.name}</span>
            </div>
            <div className="w-14 h-14 rounded-2xl bg-white/20 border border-white/30 flex items-center justify-center text-white font-black text-xl backdrop-blur-md">
              {winningNgo.finalScore}%
            </div>
          </div>
        </div>

        {/* Auto-generated Plain Language Summary */}
        <div className="p-4 rounded-2xl bg-white/10 border border-white/20 backdrop-blur-md flex items-start gap-3">
          <Sparkles className="w-5 h-5 text-white shrink-0 mt-0.5" />
          <div>
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">
              Auto-Generated Executive Explanation
            </h4>
            <p className="text-xs text-emerald-50 mt-1 leading-relaxed font-medium">{summaryText}</p>
          </div>
        </div>
      </div>

      {/* Runner-Up Comparison Side Panel (When Enabled) */}
      {showComparison && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          className="p-6 rounded-3xl bg-white border border-cyan-300 shadow-sm space-y-4"
        >
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <span className="text-xs font-bold text-cyan-700 uppercase">Head-to-Head Delta Audit</span>
              <h3 className="text-base font-black text-slate-900">
                Why {winningNgo.name} ({winningNgo.finalScore}%) beat {runnerUpNgo.name} ({runnerUpNgo.finalScore}%)
              </h3>
            </div>
            <span className="text-xs font-mono font-bold text-cyan-800 bg-cyan-100 px-2.5 py-1 rounded border border-cyan-300">
              +{winningNgo.finalScore - runnerUpNgo.finalScore}% Net Margin
            </span>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
            {runnerUpComparison.map((comp, idx) => (
              <div key={idx} className="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-2 text-xs">
                <span className="font-bold text-slate-900 block border-b border-slate-200 pb-1">
                  {comp.factor}
                </span>
                <p className="text-emerald-700 font-bold"><strong>Winner:</strong> {comp.winner}</p>
                <p className="text-slate-600 font-medium"><strong>Runner-up:</strong> {comp.runnerUp}</p>
                <span className="inline-block mt-1 font-mono font-bold text-cyan-800 bg-cyan-100 px-2 py-0.5 rounded border border-cyan-200">
                  {comp.impactDelta}
                </span>
              </div>
            ))}
          </div>
        </motion.div>
      )}

      {/* SHAP Waterfall & Ranked Bar Breakdown Component */}
      <ContributionWaterfall factors={shapFactors} summaryText={summaryText} />
    </div>
  )
}
