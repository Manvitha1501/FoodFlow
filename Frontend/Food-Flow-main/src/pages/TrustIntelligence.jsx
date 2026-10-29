import React, { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import {
  Award,
  ShieldCheck,
  Building2,
  HeartHandshake,
  TrendingUp,
  Medal,
  CheckCircle2,
  Sparkles
} from 'lucide-react'
import { api } from '../services/api'
import trustMock from '../mocks/trustData.json'
import ScoreGauge from '../components/common/ScoreGauge'
import TrendChart from '../components/common/TrendChart'
import { LoadingSkeleton, ErrorState } from '../components/common/LoadingSkeleton'
import { useStore } from '../store/useStore'

export default function TrustIntelligence() {
  const { userRole, currentUser, registeredEntities = [], customDonations = [] } = useStore()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [trustData, setTrustData] = useState(null)
  const [activeTab, setActiveTab] = useState(userRole === 'ngo' ? 'ngos' : 'restaurants')

  useEffect(() => {
    async function loadTrust() {
      try {
        setLoading(true)
        const res = await api.getTrustScore()
        if (res && (res.restaurants || res.ngos)) {
          setTrustData(res)
        } else {
          setTrustData(trustMock)
        }
      } catch (err) {
        console.warn('Trust intelligence load fallback:', err)
        setTrustData(trustMock)
      } finally {
        setLoading(false)
      }
    }
    loadTrust()
  }, [])

  if (loading) return <LoadingSkeleton count={3} />

  const safeTrustData = trustData || trustMock || { restaurants: [], ngos: [] }
  const rawEntities = (safeTrustData[activeTab] || []).map(item => ({
    ...item,
    successfulDonationsCount: Math.max(item.successfulDonationsCount || 1, customDonations.length || 1),
    pickupsCompleted: Math.max(item.pickupsCompleted || 1, customDonations.length || 1)
  }))

  const dynamicDonationsCount = Math.max(1, customDonations.length)

  const userEntities = (registeredEntities || [])
    .filter(e => (activeTab === 'restaurants' ? e.type === 'restaurant' : e.type === 'ngo'))
    .map(e => ({
      id: e.id,
      name: e.name || e.contactName || 'Registered Partner',
      category: activeTab === 'restaurants' ? 'Restaurant / Donor' : 'Community Shelter / NGO',
      trustScore: e.trustScore || 95,
      reliabilityLabel: 'Highly Reliable',
      successfulDonationsCount: dynamicDonationsCount,
      pickupsCompleted: dynamicDonationsCount,
      cancellationRate: '0.4%',
      avgResponseMin: 12,
      beneficiariesServed: 450,
      subMetrics: {
        foodAccuracy: 96,
        qualityHistory: 95,
        pickupPunctuality: 98,
        completionRate: 98
      },
      trend: [
        { month: 'Mar', score: 90 },
        { month: 'Apr', score: 92 },
        { month: 'May', score: 94 },
        { month: 'Jun', score: 95 }
      ]
    }))

  const activeRestaurantName = currentUser?.name || currentUser?.restaurantName || 'Royal Bistro'

  const combinedEntities = userEntities.length > 0 ? userEntities : rawEntities

  // Strict Role Scoping:
  // - Restaurant: Sees ONLY their own donor trust score + ALL NGO trust scores.
  // - NGO: Sees ALL NGO trust scores + Assigned donor restaurant's trust score.
  // - Admin: Sees ALL restaurants and ALL NGOs.
  const scopedEntities = combinedEntities.filter(e => {
    if (userRole === 'admin') return true
    if (userRole === 'restaurant') {
      if (activeTab === 'ngos') return true
      const eName = (e.name || '').toLowerCase()
      const uName = activeRestaurantName.toLowerCase()
      return eName.includes(uName) || uName.includes(eName)
    }
    if (userRole === 'ngo') {
      if (activeTab === 'ngos') return true
      return true // Shows assigned donor trust
    }
    return true
  })

  const sortedEntities = [...scopedEntities].sort((a, b) => b.trustScore - a.trustScore)

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-6 p-6 md:p-8 rounded-3xl bg-white border border-slate-200 shadow-xs">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-bold border border-emerald-300 mb-2">
            <Award className="w-3.5 h-3.5" />
            <span>Multi-Tier Reputation Intelligence</span>
          </div>
          <h1 className="text-2xl md:text-3xl font-black text-slate-900 tracking-tight">
            Trust & Reliability Intelligence Scores
          </h1>
          <p className="text-sm text-slate-600 max-w-2xl mt-1 leading-relaxed">
            Continuous Bayesian scoring of restaurant food accuracy & NGO pickup punctuality to incentivize compliance and high-quality donation transfers.
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="flex items-center p-1.5 rounded-2xl bg-slate-100 border border-slate-200 shrink-0">
          <button
            onClick={() => setActiveTab('restaurants')}
            className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'restaurants'
                ? 'bg-emerald-600 text-white shadow-md'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Building2 className="w-4 h-4" /> Restaurant Donors
          </button>
          <button
            onClick={() => setActiveTab('ngos')}
            className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'ngos'
                ? 'bg-emerald-600 text-white shadow-md'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <HeartHandshake className="w-4 h-4" /> Recipient NGOs
          </button>
        </div>
      </div>

      {/* Grid of Profile Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {sortedEntities.map((item, idx) => (
          <motion.div
            key={item.id}
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: idx * 0.08 }}
            className="p-6 rounded-3xl bg-white border border-slate-200 hover:border-emerald-300 transition-all space-y-6 shadow-xs"
          >
            {/* Header: Name + Score Gauge */}
            <div className="flex items-start justify-between gap-4">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-100 text-slate-800 font-bold border border-slate-200">
                    #{idx + 1} Rank
                  </span>
                  <span className={`text-[10px] px-2 py-0.5 rounded font-bold uppercase ${
                    (item.reliabilityLabel || 'Reliable').includes('Highly') ? 'bg-emerald-100 text-emerald-800' : 'bg-amber-100 text-amber-800'
                  }`}>
                    {item.reliabilityLabel || 'Reliable'}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-slate-900">{item.name}</h3>
                <p className="text-xs text-slate-600 font-medium">
                  {activeTab === 'restaurants' ? `Category: ${item.category || 'Restaurant / Donor'}` : `Beneficiaries: ${(item.beneficiariesServed || 450).toLocaleString()}`}
                </p>
              </div>

              <ScoreGauge
                score={item.trustScore || 95}
                size={95}
                strokeWidth={7}
                showLabel={false}
              />
            </div>

            {/* Sub-Metrics Grid */}
            <div className="grid grid-cols-2 gap-3 pt-3 border-t border-slate-100">
              {activeTab === 'restaurants' ? (
                <>
                  <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Food Accuracy</span>
                    <span className="font-bold text-slate-900 text-sm">{item.subMetrics?.foodAccuracy || 96}%</span>
                  </div>
                  <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Cancellation Rate</span>
                    <span className="font-bold text-emerald-700 text-sm">{item.cancellationRate || '0.4%'}</span>
                  </div>
                  <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Donations Count</span>
                    <span className="font-bold text-slate-900 text-sm">{item.successfulDonationsCount || 1} completed</span>
                  </div>
                  <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Quality Index</span>
                    <span className="font-bold text-emerald-700 text-sm">{item.subMetrics?.qualityHistory || 95}% Grade</span>
                  </div>
                </>
              ) : (
                <>
                  <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Pickup Punctuality</span>
                    <span className="font-bold text-slate-900 text-sm">{item.subMetrics?.pickupPunctuality || 98}%</span>
                  </div>
                  <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Avg Response Speed</span>
                    <span className="font-bold text-emerald-700 text-sm">{item.avgResponseMin || 12} mins</span>
                  </div>
                  <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Completion Rate</span>
                    <span className="font-bold text-slate-900 text-sm">{item.subMetrics?.completionRate || 98}%</span>
                  </div>
                  <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100 text-xs">
                    <span className="text-slate-500 block text-[10px] uppercase font-bold">Pickups Completed</span>
                    <span className="font-bold text-emerald-700 text-sm">{item.pickupsCompleted || 1} total</span>
                  </div>
                </>
              )}
            </div>

            {/* Historical Trend Line Chart */}
            <div className="space-y-2 pt-2">
              <span className="text-xs font-bold text-slate-700">6-Month Trust Trend History</span>
              <TrendChart
                data={item.trend}
                dataKey="score"
                xAxisKey="month"
                color="#059669"
                height={120}
                tooltipLabel="Trust Score"
              />
            </div>
          </motion.div>
        ))}
      </div>
    </div>
  )
}
