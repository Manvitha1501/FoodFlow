import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
  ShieldCheck,
  Clock,
  ArrowRight,
  TrendingUp,
  LineChart as LineChartIcon,
  CheckCircle2,
  AlertTriangle,
  Sparkles
} from 'lucide-react'
import { api } from '../services/api'
import RiskBadge from '../components/common/RiskBadge'
import FactorChecklist from '../components/common/FactorChecklist'
import { LoadingSkeleton, ErrorState } from '../components/common/LoadingSkeleton'
import { useStore } from '../store/useStore'
import { ResponsiveContainer, LineChart, Line, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts'

export default function DonationRisk() {
  const { id = 'DON-9042' } = useParams()
  const navigate = useNavigate()
  const { userRole, customDonations, currentUser } = useStore()

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [data, setData] = useState(null)

  useEffect(() => {
    async function loadRiskData() {
      try {
        setLoading(true)
        const res = await api.getRiskPrediction(id)
        const activeCustom = customDonations.find(d => d.id === id) || customDonations[0]
        if (activeCustom) {
          res.donation = {
            ...res.donation,
            id: activeCustom.id || id,
            foodType: activeCustom.foodType || res.donation.foodType,
            restaurant: activeCustom.restaurant || currentUser?.name || currentUser?.restaurantName || 'Royal Bistro',
            mealsCount: activeCustom.mealsCount || `${Math.round((parseFloat(activeCustom.quantityKg) || 180) * 2.5)}`,
            quantityKg: activeCustom.quantityKg || '180',
            readinessScore: activeCustom.readinessScore || 94
          }
        }
        setData(res)
      } catch (err) {
        setError('Failed to load risk prediction models')
      } finally {
        setLoading(false)
      }
    }
    loadRiskData()
  }, [id, customDonations, currentUser])

  if (loading) return <LoadingSkeleton count={3} />
  if (error) return <ErrorState message={error} onRetry={() => window.location.reload()} />

  const { donation, successProbability, riskLevel, modelMetrics, rocData, riskFactors } = data

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="p-6 md:p-8 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-3">
              <span className="text-xs font-mono font-bold px-2.5 py-0.5 rounded bg-emerald-100 text-emerald-800 border border-emerald-300">
                {donation.id}
              </span>
              <h1 className="text-xl md:text-2xl font-black text-slate-900">{donation.foodType}</h1>
            </div>
            <p className="text-xs text-slate-600 mt-1">
              Donor: <strong className="text-slate-900">{donation.restaurant}</strong> ({donation.location})
            </p>
          </div>

          <RiskBadge level={riskLevel} size="lg" />
        </div>

        {/* Info Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 pt-3 border-t border-slate-100 text-xs">
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <span className="text-slate-500 block text-[10px] uppercase font-bold">Volume & Meals</span>
            <span className="font-extrabold text-slate-900 text-sm">{donation.mealsCount} Meals ({donation.quantityKg} kg)</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <span className="text-slate-500 block text-[10px] uppercase font-bold">Safe Window Left</span>
            <span className="font-extrabold text-emerald-700 text-sm">5.8 Hours Safe</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <span className="text-slate-500 block text-[10px] uppercase font-bold">Temperature holding</span>
            <span className="font-extrabold text-slate-900 text-sm">{donation.temperature}</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
            <span className="text-slate-500 block text-[10px] uppercase font-bold">Readiness Baseline</span>
            <span className="font-extrabold text-emerald-700 text-sm">{donation.readinessScore}% Score</span>
          </div>
        </div>
      </div>

      {/* Main Stat & Factor Checklist Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Big Success Probability Stat (4 cols) */}
        <div className="lg:col-span-4 p-6 md:p-8 rounded-3xl bg-white border border-slate-200 shadow-xs flex flex-col justify-between space-y-6 text-center">
          <div>
            <span className="text-xs font-bold text-slate-500 uppercase tracking-wider block mb-2">
              Predicted Delivery Success Probability
            </span>
            <div className="relative inline-flex items-center justify-center my-4">
              <span className="text-6xl font-black text-emerald-700 tracking-tight">
                {successProbability}%
              </span>
            </div>
            <p className="text-xs text-slate-600 leading-relaxed px-2 font-medium">
              High confidence score derived from driver arrival velocity, thermal holding stability, and recipient NGO capacity.
            </p>
          </div>

          <button
            onClick={() => navigate(`/donation/${donation.id}/match`)}
            className="w-full py-3.5 rounded-2xl bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-xs transition-all shadow-md shadow-emerald-500/20 flex items-center justify-center gap-2"
          >
            <span>Proceed to NGO Suitability Matching</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>

        {/* Evaluated Factors Grid (8 cols) */}
        <div className="lg:col-span-8 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-extrabold text-slate-900 flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-emerald-600" />
              Risk Factor Evaluation Matrix
            </h3>
            <span className="text-xs text-slate-500 font-semibold">8 Evaluated Dimensions</span>
          </div>

          <FactorChecklist factors={riskFactors} />
        </div>
      </div>

      {/* ADMIN ONLY MODEL PERFORMANCE STRIP */}
      {userRole === 'admin' && (
        <div className="p-6 md:p-8 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-cyan-100 text-cyan-800 text-xs font-bold border border-cyan-300 mb-1">
                <Sparkles className="w-3 h-3" />
                <span>Admin ML Model Diagnostics</span>
              </div>
              <h3 className="text-base font-extrabold text-slate-900">Classifier Performance & ROC-AUC Validation</h3>
            </div>
            <span className="text-xs font-mono text-slate-500 font-semibold">Model Version: XGBoost-Rescue-v2.1</span>
          </div>

          {/* Metric Stat Cards */}
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-3">
            <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 text-center">
              <span className="text-[10px] text-slate-500 uppercase font-bold block">Accuracy</span>
              <span className="text-lg font-black text-slate-900">{modelMetrics.accuracy * 100}%</span>
            </div>
            <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 text-center">
              <span className="text-[10px] text-slate-500 uppercase font-bold block">Precision</span>
              <span className="text-lg font-black text-emerald-700">{modelMetrics.precision * 100}%</span>
            </div>
            <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 text-center">
              <span className="text-[10px] text-slate-500 uppercase font-bold block">Recall</span>
              <span className="text-lg font-black text-cyan-700">{modelMetrics.recall * 100}%</span>
            </div>
            <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 text-center">
              <span className="text-[10px] text-slate-500 uppercase font-bold block">F1-Score</span>
              <span className="text-lg font-black text-amber-700">{modelMetrics.f1 * 100}%</span>
            </div>
            <div className="p-3.5 rounded-2xl bg-slate-50 border border-slate-200 text-center col-span-2 sm:col-span-1">
              <span className="text-[10px] text-slate-500 uppercase font-bold block">ROC-AUC</span>
              <span className="text-lg font-black text-emerald-700">{modelMetrics.rocAuc}</span>
            </div>
          </div>

          {/* ROC Curve Chart */}
          <div className="space-y-2">
            <span className="text-xs font-bold text-slate-700">Receiver Operating Characteristic (ROC Curve)</span>
            <div className="w-full h-48 p-4 rounded-2xl bg-slate-50 border border-slate-200">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={rocData} margin={{ top: 5, right: 20, left: -20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                  <XAxis dataKey="fpr" stroke="#64748b" fontSize={11} label={{ value: 'False Positive Rate', position: 'insideBottom', offset: -5, fill: '#64748b', fontSize: 10 }} />
                  <YAxis stroke="#64748b" fontSize={11} label={{ value: 'True Positive Rate', angle: -90, position: 'insideLeft', fill: '#64748b', fontSize: 10 }} />
                  <Tooltip formatter={(val) => [`${val}`, 'Rate']} />
                  <Line type="monotone" dataKey="tpr" stroke="#059669" strokeWidth={2.5} dot={{ fill: '#059669', r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
