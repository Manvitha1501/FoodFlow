import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
  PlusCircle,
  ShieldCheck,
  GitMerge,
  PieChart,
  HelpCircle,
  Leaf,
  CheckCircle2,
  XCircle,
  ArrowRight,
  TrendingUp,
  Award,
  Clock,
  Sparkles,
  Zap,
  Building2,
  HeartHandshake,
  RotateCcw
} from 'lucide-react'
import { useStore } from '../store/useStore'
import { api } from '../services/api'
import KpiCard from '../components/common/KpiCard'
import RiskBadge from '../components/common/RiskBadge'
import { LoadingSkeleton, ErrorState } from '../components/common/LoadingSkeleton'

export default function Dashboard() {
  const navigate = useNavigate()
  const { userRole, currentUser, ngoActions, setNgoAction, customDonations, clearCustomDonations, customAllocations = {}, registeredEntities = [] } = useStore()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [donations, setDonations] = useState([])
  const [impactData, setImpactData] = useState(null)
  const [ngoDemand, setNgoDemand] = useState(() => {
    try { return localStorage.getItem('foodflow_ngo_demand') || '250' } catch (e) { return '250' }
  })
  const [demandSaved, setDemandSaved] = useState(false)

  const handleSaveDemand = () => {
    try {
      localStorage.setItem('foodflow_ngo_demand', ngoDemand)
    } catch (e) {}
    setDemandSaved(true)
    setTimeout(() => setDemandSaved(false), 3000)
  }

  useEffect(() => {
    async function loadDashboardData() {
      try {
        setLoading(true)
        const [dList, iData] = await Promise.all([
          api.getDonations(),
          api.getImpactStats()
        ])
        const combined = customDonations.length > 0 
          ? [...customDonations, ...dList.filter(d => !customDonations.some(c => c.id === d.id))]
          : (Array.isArray(dList) && dList.length > 0 ? dList : [])
        setDonations(combined)
        setImpactData(iData)
      } catch (err) {
        setError('Failed to load dashboard parameters')
      } finally {
        setLoading(false)
      }
    }
    loadDashboardData()
  }, [customDonations])

  if (loading) return <LoadingSkeleton count={3} />
  if (error) return <ErrorState message={error} onRetry={() => window.location.reload()} />

  return (
    <div className="space-y-8">
      {/* Top Welcome Banner */}
      <div className="relative overflow-hidden p-6 md:p-8 rounded-3xl bg-gradient-to-r from-emerald-500 via-emerald-600 to-teal-700 text-white shadow-xl">
        <div className="absolute -top-24 -right-24 w-64 h-64 bg-white/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/20 text-white text-xs font-bold border border-white/30 mb-3 backdrop-blur-md">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Food Flow Intelligence Platform</span>
            </div>
            <h1 className="text-2xl md:text-3xl font-black text-white tracking-tight">
              {userRole === 'restaurant'
                ? 'Restaurant Food Rescue Portal'
                : userRole === 'ngo'
                ? 'NGO Recipient Matching Station'
                : 'Intelligence Command Center'}
            </h1>
            <p className="text-sm text-emerald-50 max-w-xl mt-1 leading-relaxed font-medium">
              Real-time oversight of food surplus safety readiness, logistics delivery risk, multi-NGO allocations, and trust metrics.
            </p>
          </div>

          {userRole === 'restaurant' && (
            <button
              onClick={() => navigate('/donation/new')}
              className="px-6 py-3.5 rounded-2xl bg-white text-emerald-800 hover:bg-emerald-50 font-black text-sm shadow-lg transition-all flex items-center justify-center gap-2 shrink-0 group cursor-pointer"
            >
              <PlusCircle className="w-5 h-5 text-emerald-600 group-hover:scale-110 transition-transform" />
              <span>Submit New Donation</span>
            </button>
          )}
        </div>
      </div>

      {/* OVERVIEW STAT CARDS */}
      {(() => {
        const totalKg = donations.reduce((acc, d) => acc + (parseFloat(d.quantityKg) || 0), 0)
        const totalMealsCount = donations.reduce((acc, d) => acc + (parseInt(d.mealsCount) || Math.round((parseFloat(d.quantityKg) || 0) * 2.5)), 0)
        const avgReadiness = donations.length > 0
          ? Math.round(donations.reduce((acc, d) => acc + (parseFloat(d.readinessScore) || 90), 0) / donations.length)
          : 0
        const co2Tons = (totalKg * 0.0025).toFixed(2)

        return (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <KpiCard
              title="Surplus Rescued"
              value={`${totalKg.toLocaleString()} kg`}
              subtext={`Equivalent to ${totalMealsCount.toLocaleString()} meals`}
              trend={totalKg > 0 ? 12.4 : 0}
              icon={Leaf}
              highlight
            />
            {userRole === 'admin' ? (
              <KpiCard
                title="Active Network Partners"
                value={`${(registeredEntities.length || 2)} Entities`}
                subtext="Verified Restaurants & NGOs"
                trend={4.5}
                icon={Building2}
              />
            ) : (
              <KpiCard
                title="Readiness Score Avg"
                value={`${avgReadiness}%`}
                subtext={donations.length > 0 ? "High safety compliance" : "No entries logged"}
                trend={donations.length > 0 ? 3.8 : 0}
                icon={Zap}
              />
            )}
            <KpiCard
              title="Delivery Success Rate"
              value="96.4%"
              subtext="Under 35 mins dispatch"
              trend={1.2}
              icon={TrendingUp}
            />
            <KpiCard
              title={userRole === 'admin' ? "Network Trust Average" : "CO₂ Emissions Saved"}
              value={userRole === 'admin' ? "96.2%" : `${co2Tons} T`}
              subtext={userRole === 'admin' ? "Bayesian reliability index" : "Organic landfill avoided"}
              trend={userRole === 'admin' ? 2.1 : (totalKg > 0 ? 8.5 : 0)}
              icon={Award}
            />
          </div>
        )
      })()}

      {/* MAIN DONATION LIST VIEW PER ROLE */}
      {(() => {
        const activeRestaurantName = currentUser?.name || currentUser?.restaurantName || 'Royal Bistro'
        const roleFilteredDonations = userRole === 'admin' || userRole === 'ngo'
          ? donations
          : donations.filter(d => {
              if (!d.restaurant) return true
              const rName = d.restaurant.toLowerCase()
              const uName = activeRestaurantName.toLowerCase()
              return rName.includes(uName) || uName.includes(rName)
            })

        const getCalculatedFoodAge = (item) => {
          if (item.foodAgeHours !== undefined && item.foodAgeHours !== null && item.foodAgeHours !== '') {
            return parseFloat(item.foodAgeHours) || 0
          }
          const timeStr = item.preparedTime || item.createdAt
          if (!timeStr) return 0
          const prepared = new Date(timeStr).getTime()
          if (isNaN(prepared)) return 0
          const diffHours = (Date.now() - prepared) / (1000 * 60 * 60)
          return diffHours > 0 ? diffHours : 0
        }

        const isDonationActive = (d) => {
          const age = getCalculatedFoodAge(d)
          const isExpiredStatus = (d.status || '').toLowerCase().includes('expired') || (d.readinessLevel || '').toLowerCase().includes('expired')
          return age <= 15 && !isExpiredStatus
        }

        const activeDonations = roleFilteredDonations.filter(isDonationActive)
        const expiredHistory = roleFilteredDonations.filter(d => !isDonationActive(d))

        const activeNgoName = currentUser?.name || currentUser?.ngoName || 'Hope Kitchen Community Shelter'

        // Filter incoming matched queue specifically for this logged-in NGO recipient
        const ngoMatchedQueue = activeDonations.filter(d => {
          if (!d) return false

          // 1. Check explicit custom allocations saved via DonationAllocate
          const splits = customAllocations[d.id]
          if (splits && Array.isArray(splits) && splits.length > 0) {
            const isAllocatedToMe = splits.some(s => {
              const name = (s.ngoName || s.name || '').toLowerCase()
              const current = activeNgoName.toLowerCase()
              return name.includes(current) || current.includes(name)
            })
            if (isAllocatedToMe) return true
          }

          // 2. Check direct assigned properties
          const assigned = (d.ngoMatched || d.assignedNgo || d.targetNgo || d.allocatedNgo || '').toLowerCase()
          const currentName = activeNgoName.toLowerCase()
          if (assigned && (assigned.includes(currentName) || currentName.includes(assigned))) {
            return true
          }

          // 3. Featured demo donation DON-9042 default match for Hope Kitchen
          if (d.id === 'DON-9042') return true

          // 4. For custom donations without explicit allocation: only include the most recent active custom donation if targeted
          if (d.isCustom && !splits) {
            const isLatestCustom = customDonations.length > 0 && customDonations[0].id === d.id
            const isTargetedNgo = currentName.includes('kitchen') || currentName.includes('care') || currentName.includes('ngo')
            return isLatestCustom && isTargetedNgo
          }

          return false
        })

        return (
          <>
            {/* ROLE 2: NGO MATCHED QUEUE VIEW & LIVE DAILY DEMAND ADJUSTMENT */}
            {userRole === 'ngo' && (
              <div className="space-y-4">
                {/* Dynamic Today's Food Demand Widget */}
                <div className="p-6 rounded-3xl bg-gradient-to-r from-teal-500 via-emerald-600 to-emerald-700 text-white shadow-md space-y-3">
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div>
                      <span className="text-[10px] font-extrabold uppercase tracking-widest text-emerald-100 bg-white/20 px-2.5 py-0.5 rounded-full backdrop-blur-md">
                        Dynamic Daily Demand Controller
                      </span>
                      <h3 className="text-lg font-black text-white mt-1">Today's Active Food Intake Demand</h3>
                      <p className="text-xs text-emerald-50">Adjust your shelter's required meal capacity for today to dynamically recalculate surplus allocations.</p>
                      
                      {demandSaved && (
                        <div className="mt-2">
                          <span className="inline-flex items-center gap-1.5 text-xs font-extrabold text-emerald-900 bg-emerald-100 px-3 py-1 rounded-full border border-emerald-300 shadow-sm animate-pulse">
                            <CheckCircle2 className="w-4 h-4 text-emerald-700" />
                            Daily Demand Updated to {ngoDemand} Meals!
                          </span>
                        </div>
                      )}
                    </div>

                    <div className="flex items-center gap-2 bg-white/10 p-2 rounded-2xl border border-white/20 backdrop-blur-md shrink-0">
                      <span className="text-xs font-bold text-white px-2">Meals Needed Today:</span>
                      <input
                        type="number"
                        value={ngoDemand}
                        onChange={(e) => setNgoDemand(e.target.value)}
                        className="w-20 px-3 py-1.5 rounded-xl bg-white text-slate-900 font-extrabold text-sm text-center focus:outline-none shadow-inner"
                      />
                      <button
                        onClick={handleSaveDemand}
                        className="px-3.5 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-extrabold text-xs transition-colors flex items-center gap-1.5 shadow-md cursor-pointer"
                      >
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" /> Save Demand
                      </button>
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <h2 className="text-lg font-extrabold text-slate-900 flex items-center gap-2">
                    <HeartHandshake className="w-5 h-5 text-emerald-600" />
                    Incoming Matched Donations Queue
                  </h2>
                  {customDonations.length > 0 && (
                    <button
                      onClick={clearCustomDonations}
                      className="px-3 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-600 font-bold text-xs transition-colors flex items-center gap-1.5 border border-slate-200 cursor-pointer"
                      title="Clear test submissions from local queue"
                    >
                      <RotateCcw className="w-3.5 h-3.5 text-slate-500" />
                      <span>Reset Test Queue</span>
                    </button>
                  )}
                </div>

                {ngoMatchedQueue.length === 0 ? (
                  <div className="p-8 rounded-3xl bg-white border border-dashed border-slate-300 text-center flex flex-col items-center justify-center shadow-xs space-y-2">
                    <div className="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto border border-emerald-200">
                      <HeartHandshake className="w-6 h-6" />
                    </div>
                    <h3 className="text-sm font-extrabold text-slate-900">No Matched Food Donations Yet</h3>
                    <p className="text-xs text-slate-500 max-w-sm leading-relaxed">
                      There are no active food donations assigned to your NGO queue yet. When a restaurant donor submits surplus food, matching vectors will route it here automatically.
                    </p>
                  </div>
                ) : (
                  <div className="space-y-3">
                    {ngoMatchedQueue.map((item) => {
                      const actionState = ngoActions[item.id]
                      const cleanTitle = (item.foodType || '')
                        .replace(/AI Visual Match/g, 'Visual Match')
                        .replace(/AI /g, '')

                      return (
                        <div
                          key={item.id}
                          className="p-5 rounded-2xl bg-white border border-slate-200 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4"
                        >
                          <div className="space-y-1">
                            <div className="flex items-center gap-3">
                              <span className="text-xs font-mono font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-800 border border-slate-200">
                                {item.id}
                              </span>
                              <h3 className="text-base font-bold text-slate-900">{cleanTitle}</h3>
                              <RiskBadge level={item.riskLevel} size="sm" />
                            </div>
                            <p className="text-xs text-slate-600">
                              Donor: <strong className="text-slate-900">{item.restaurant}</strong> ({item.location})
                            </p>
                            <div className="flex items-center gap-4 text-xs text-slate-600 pt-1 font-medium">
                              <span><strong>{item.mealsCount}</strong> meals ({item.quantityKg} kg)</span>
                              <span>Prep Temp: <strong>{item.temperature}</strong></span>
                              <span className="text-emerald-700 font-bold">Readiness Score: {item.readinessScore}%</span>
                            </div>
                          </div>

                          {/* Actions */}
                          <div className="flex items-center gap-3">
                            {actionState === 'accepted' ? (
                              <span className="px-4 py-2 rounded-xl bg-emerald-100 text-emerald-800 border border-emerald-300 text-xs font-bold flex items-center gap-1.5">
                                <CheckCircle2 className="w-4 h-4" /> Accepted for Pickup
                              </span>
                            ) : actionState === 'declined' ? (
                              <span className="px-4 py-2 rounded-xl bg-red-100 text-red-800 border border-red-300 text-xs font-bold flex items-center gap-1.5">
                                <XCircle className="w-4 h-4" /> Declined
                              </span>
                            ) : (
                              <>
                                <button
                                  onClick={() => setNgoAction(item.id, 'accepted')}
                                  className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-xs transition-colors flex items-center gap-1 shadow-md shadow-emerald-500/20 cursor-pointer"
                                >
                                  <CheckCircle2 className="w-3.5 h-3.5" /> Accept Match
                                </button>
                                <button
                                  onClick={() => setNgoAction(item.id, 'declined')}
                                  className="px-3 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs transition-colors flex items-center gap-1 border border-slate-200 cursor-pointer"
                                >
                                  <XCircle className="w-3.5 h-3.5" /> Decline
                                </button>
                              </>
                            )}
                          </div>
                        </div>
                      )
                    })}
                  </div>
                )}
              </div>
            )}

            {/* LIVE DONATION TRACKING REGISTRY (Active <= 15h) */}
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-extrabold text-slate-900 flex items-center gap-2">
                  <Building2 className="w-5 h-5 text-amber-600" />
                  Live Donation Tracking Registry
                </h2>
              </div>

              {activeDonations.length === 0 ? (
                <div className="p-8 rounded-3xl bg-white border border-dashed border-slate-300 text-center flex flex-col items-center justify-center shadow-xs space-y-2">
                  <div className="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto border border-amber-200">
                    <Building2 className="w-6 h-6" />
                  </div>
                  <h3 className="text-sm font-extrabold text-slate-900">No Active Food Donations</h3>
                  <p className="text-xs text-slate-500 max-w-sm leading-relaxed">
                    There are no active food rescue entries within the 15-hour safety window.
                  </p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {activeDonations.map((d) => {
                    const cleanTitle = (d.foodType || '')
                      .replace(/AI Visual Match/g, 'Visual Match')
                      .replace(/AI /g, '')
                    return (
                      <motion.div
                        key={d.id}
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="p-5 rounded-2xl bg-white border border-slate-200 shadow-xs space-y-3 flex flex-col justify-between"
                      >
                        <div className="space-y-1.5">
                          <div className="flex items-center justify-between">
                            <span className="text-xs font-mono font-bold px-2 py-0.5 rounded bg-slate-100 text-slate-800 border border-slate-200">
                              {d.id}
                            </span>
                            <RiskBadge level={d.riskLevel} size="sm" />
                          </div>
                          <h3 className="text-base font-extrabold text-slate-900">{cleanTitle}</h3>
                          <p className="text-xs text-slate-600">
                            Donor: <strong className="text-slate-900">{d.restaurant}</strong> ({d.location})
                          </p>
                          <div className="flex items-center gap-3 text-xs text-slate-500 font-medium pt-1">
                            <span><strong>{d.mealsCount}</strong> meals ({d.quantityKg} kg)</span>
                            <span>Temp: <strong>{d.temperature}</strong></span>
                            <span>Age: <strong>{d.foodAgeHours}h</strong></span>
                          </div>
                        </div>

                        <div className="grid grid-cols-2 gap-2 pt-2 border-t border-slate-100">
                          <div className="p-2 rounded-xl bg-slate-50 border border-slate-100">
                            <span className="text-[10px] text-slate-500 uppercase font-bold">Readiness</span>
                            <p className="text-sm font-extrabold text-emerald-700">{d.readinessScore}%</p>
                          </div>
                          <div className="p-2 rounded-xl bg-slate-50 border border-slate-100">
                            <span className="text-[10px] text-slate-500 uppercase font-bold">Success Prob</span>
                            <p className="text-sm font-extrabold text-cyan-700">{d.successProbability || 92}%</p>
                          </div>
                        </div>
                      </motion.div>
                    )
                  })}
                </div>
              )}
            </div>

            {/* DONATION HISTORY & EXPIRED LOG (> 15 Hours) */}
            {expiredHistory.length > 0 && (
              <div className="space-y-4 pt-4 border-t border-slate-200">
                <div className="flex items-center justify-between">
                  <h2 className="text-base font-extrabold text-slate-700 flex items-center gap-2">
                    <Clock className="w-4 h-4 text-slate-500" />
                    Donation History & Expired Log (15+ Hours Safe Window Passed)
                  </h2>
                  <span className="text-xs font-mono font-bold text-slate-500 bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
                    {expiredHistory.length} Archived Entries
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {expiredHistory.map((d) => (
                    <div key={d.id} className="p-4 rounded-2xl bg-slate-50 border border-slate-200 opacity-75 space-y-2">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-mono font-bold text-slate-700 bg-slate-200 px-2 py-0.5 rounded">
                          {d.id} (Archived)
                        </span>
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-red-100 text-red-700">15h+ Expired</span>
                      </div>
                      <h4 className="text-sm font-bold text-slate-800">{d.foodType}</h4>
                      <p className="text-xs text-slate-500">Donor: {d.restaurant} · {d.foodAgeHours}h prepared age</p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </>
        )
      })()}
    </div>
  )
}
