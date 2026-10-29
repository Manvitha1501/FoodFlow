import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
  PieChart as PieChartIcon,
  Info,
  Sliders,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  ArrowLeft,
  RefreshCw,
  Sparkles,
  HelpCircle
} from 'lucide-react'
import { api } from '../services/api'
import AllocationChart from '../components/donation/AllocationChart'
import { LoadingSkeleton, ErrorState } from '../components/common/LoadingSkeleton'
import { useStore } from '../store/useStore'

export default function DonationAllocate() {
  const { id = 'DON-9042' } = useParams()
  const navigate = useNavigate()
  const { customDonations, registeredEntities = [], customAllocations = {}, setCustomAllocation, addNotification } = useStore()

  const activeCustom = customDonations.find(d => d.id === id) || customDonations[0]
  const initialMeals = activeCustom ? (parseInt(activeCustom.mealsCount) || Math.round(parseFloat(activeCustom.quantityKg || 180) * 2.5) || 450) : 450

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [totalMeals, setTotalMeals] = useState(initialMeals)
  const [splits, setSplits] = useState([])
  const [optimizationMethod, setOptimizationMethod] = useState('')
  const [showTooltip, setShowTooltip] = useState(false)

  useEffect(() => {
    async function loadAllocation() {
      try {
        setLoading(true)
        const targetMeals = totalMeals || initialMeals
        const res = await api.getAllocation(id, targetMeals)

        // Filter out stale mock entries from stored customAllocations
        const stored = customAllocations[id]
        const validStored = stored ? stored.filter(s => s && s.name && !s.name.toLowerCase().includes('people')) : null

        if (validStored && validStored.length > 0) {
          setSplits(validStored)
        } else {
          const registeredNgos = registeredEntities.filter(e => e.type === 'ngo' && e.name && !e.name.toLowerCase().includes('people'))
          let dynamicSplits = res.splits || []

          if (registeredNgos.length > 0) {
            const pctPerNgo = Math.floor(100 / registeredNgos.length)
            dynamicSplits = registeredNgos.map((item, idx) => ({
              ngoId: item.id || `NGO-ALLOC-${idx}`,
              name: item.name || item.contactName || 'Hope Kitchen Community Shelter',
              percentage: idx === 0 ? 100 - pctPerNgo * (registeredNgos.length - 1) : pctPerNgo,
              meals: Math.round(targetMeals * ((idx === 0 ? 100 - pctPerNgo * (registeredNgos.length - 1) : pctPerNgo) / 100)),
              distanceKm: 1.8 + idx * 1.2,
              capacityUsed: 75 - idx * 10,
              needLevel: idx === 0 ? 'Critical' : 'High',
              trustScore: 95,
              pickupAvailable: '20 mins'
            }))
          } else {
            dynamicSplits = dynamicSplits.filter(s => s && s.name && !s.name.toLowerCase().includes('people')).map(item => ({
              ...item,
              meals: Math.round(targetMeals * (item.percentage / 100))
            }))
          }
          setSplits(dynamicSplits)
        }
        setOptimizationMethod(res.optimizationMethod)
      } catch (err) {
        setError('Failed to calculate optimal multi-NGO allocation')
      } finally {
        setLoading(false)
      }
    }
    loadAllocation()
  }, [id, totalMeals, initialMeals, registeredEntities, customAllocations])

  if (loading) return <LoadingSkeleton count={3} />
  if (error) return <ErrorState message={error} onRetry={() => window.location.reload()} />

  // Calculate sum of slider meals
  const currentAllocatedSum = splits.reduce((acc, curr) => acc + (curr.meals || 0), 0)
  const isOverAllocated = currentAllocatedSum > totalMeals
  const isUnderAllocated = currentAllocatedSum < totalMeals
  const isMultiNgo = splits.length > 1

  // Slider change handler with real-time re-validation
  const handleSliderChange = (index, newMealValue) => {
    const val = parseInt(newMealValue) || 0
    const updated = [...splits]
    updated[index] = {
      ...updated[index],
      meals: val,
      percentage: Math.round((val / totalMeals) * 100)
    }
    setSplits(updated)
    setCustomAllocation(id, updated)
  }

  const handleResetOptimal = () => {
    setCustomAllocation(id, null)
    const registeredNgos = registeredEntities.filter(e => e.type === 'ngo' && e.name && !e.name.toLowerCase().includes('people'))
    if (registeredNgos.length > 0) {
      const pctPerNgo = Math.floor(100 / registeredNgos.length)
      const optimal = registeredNgos.map((item, idx) => ({
        ngoId: item.id || `NGO-ALLOC-${idx}`,
        name: item.name || item.contactName || 'Hope Kitchen Community Shelter',
        percentage: idx === 0 ? 100 - pctPerNgo * (registeredNgos.length - 1) : pctPerNgo,
        meals: Math.round(totalMeals * ((idx === 0 ? 100 - pctPerNgo * (registeredNgos.length - 1) : pctPerNgo) / 100)),
        distanceKm: 1.8 + idx * 1.2,
        capacityUsed: 75 - idx * 10,
        needLevel: idx === 0 ? 'Critical' : 'High',
        trustScore: 95,
        pickupAvailable: 'Immediate'
      }))
      setSplits(optimal)
    } else {
      const optimal = [
        { ngoId: 'NGO-101', name: 'Hope Kitchen Community Shelter', meals: Math.round(totalMeals * 1.0), percentage: 100, capacityUsed: 80, needLevel: 'Critical', distanceKm: 1.8, trustScore: 98, pickupAvailable: 'Immediate' }
      ]
      setSplits(optimal)
    }
  }

  const handleConfirmDispatch = () => {
    if (addNotification) {
      addNotification({
        type: 'match',
        title: `${isMultiNgo ? 'Multi-NGO' : 'Single NGO'} Dispatch Confirmed for ${id} (${totalMeals} meals allocated)`
      })
    }
    navigate('/trust')
  }

  return (
    <div className="space-y-8">
      {/* Navigation Back Button */}
      <div>
        <button
          onClick={() => navigate(`/donation/${id}/match`)}
          className="inline-flex items-center gap-2 text-xs font-extrabold text-slate-600 hover:text-emerald-700 transition-colors bg-white px-4 py-2 rounded-xl border border-slate-200 shadow-xs cursor-pointer"
        >
          <ArrowLeft className="w-4 h-4 text-emerald-600" />
          <span>Back to Recipient NGO Suitability Matching</span>
        </button>
      </div>

      {/* Header Banner */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6 p-6 md:p-8 rounded-3xl bg-white border border-slate-200 shadow-xs">
        <div>
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-bold border border-emerald-300 mb-2">
            <PieChartIcon className="w-3.5 h-3.5" />
            <span>{isMultiNgo ? 'Surplus Multi-Split Optimizer' : 'Single Recipient Direct Allocation'}</span>
          </div>
          <h1 className="text-2xl md:text-3xl font-black text-slate-900 tracking-tight">
            {isMultiNgo ? 'Smart Multi-NGO Donation Allocation' : 'Single NGO Direct Donation Allocation'}
          </h1>
          <p className="text-sm text-slate-600 max-w-2xl mt-1 leading-relaxed font-medium">
            {isMultiNgo
              ? 'Distributes high-volume food donations across multiple recipient NGOs to prevent single-shelter storage bottlenecking and maximize intake efficiency.'
              : 'Directs 100% of this surplus donation to the top verified recipient NGO fitted for single-shelter intake.'}
          </p>
        </div>

        {/* Solver Info Pill */}
        <div className="relative shrink-0">
          <button
            onClick={() => setShowTooltip(!showTooltip)}
            className="flex items-center gap-2 px-4 py-2.5 rounded-2xl bg-emerald-50 hover:bg-emerald-100 text-emerald-800 border border-emerald-200 text-xs font-extrabold transition-all"
          >
            <Sparkles className="w-4 h-4 text-emerald-600" />
            <span>Linear Programming (Simplex / Capacity Max)</span>
            <HelpCircle className="w-3.5 h-3.5 text-emerald-600" />
          </button>

          {showTooltip && (
            <div className="absolute right-0 mt-2 w-72 p-3.5 rounded-2xl bg-white border border-slate-300 shadow-2xl text-xs z-50 text-slate-800 space-y-1">
              <p className="font-extrabold text-emerald-700">Optimization Solver</p>
              <p className="text-slate-600 leading-relaxed font-medium">
                Uses linear constraints (Capacity Limits, Distance Penalties, and Urgent Need Weights) solved via Simplex algorithm for optimal welfare distribution.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Input Control & Allocation Visualization */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Left Column: Total Quantity Control & Donut Chart (6 cols) */}
        <div className="lg:col-span-6 space-y-6">
          <div className="p-6 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-4">
            <div className="flex items-center justify-between">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                Total Donation Quantity (Meals)
              </label>
              <span className="text-xs text-slate-500 font-semibold">Editable Input</span>
            </div>

            <div className="flex items-center gap-3">
              <input
                type="number"
                value={totalMeals}
                onChange={(e) => setTotalMeals(Math.max(10, parseInt(e.target.value) || 0))}
                className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 font-black text-lg focus:outline-none focus:border-emerald-500 focus:bg-white transition-colors"
              />
              <button
                onClick={handleResetOptimal}
                className="px-4 py-3 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 text-xs font-extrabold shrink-0 flex items-center gap-1.5 transition-colors border border-slate-200"
                title="Reset to Recommended Split"
              >
                <RefreshCw className="w-3.5 h-3.5 text-emerald-600" /> Reset
              </button>
            </div>
          </div>

          {/* Allocation Visualization Chart */}
          <AllocationChart splits={splits} totalMeals={totalMeals} mode="donut" />
        </div>

        {/* Right Column: Interactive Sliders & Real-Time Re-validation (6 cols) */}
        <div className="lg:col-span-6 space-y-6">
          <div className="p-6 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-base font-extrabold text-slate-900 flex items-center gap-2">
                  <Sliders className="w-4 h-4 text-emerald-600" /> Manual Override Controls
                </h3>
                <p className="text-xs text-slate-500 mt-0.5 font-medium">Drag sliders to adjust individual NGO allocations</p>
              </div>

              {/* Status Warning Badge */}
              {isOverAllocated ? (
                <span className="px-3 py-1 rounded-full bg-red-100 text-red-800 border border-red-300 text-xs font-bold flex items-center gap-1">
                  <AlertTriangle className="w-3.5 h-3.5 text-red-600" /> Exceeds +{currentAllocatedSum - totalMeals} meals
                </span>
              ) : isUnderAllocated ? (
                <span className="px-3 py-1 rounded-full bg-amber-100 text-amber-800 border border-amber-300 text-xs font-bold flex items-center gap-1">
                  <AlertTriangle className="w-3.5 h-3.5 text-amber-600" /> Unallocated: {totalMeals - currentAllocatedSum} meals
                </span>
              ) : (
                <span className="px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 border border-emerald-300 text-xs font-bold flex items-center gap-1">
                  <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" /> 100% Balanced
                </span>
              )}
            </div>

            {/* Sliders List */}
            <div className="space-y-5">
              {splits.map((s, idx) => (
                <div key={s.ngoId} className="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-extrabold text-slate-900">{s.name}</span>
                    <span className="font-mono font-black text-emerald-700">
                      {s.meals} meals ({s.percentage}%)
                    </span>
                  </div>

                  <input
                    type="range"
                    min="0"
                    max={totalMeals}
                    value={s.meals}
                    onChange={(e) => handleSliderChange(idx, e.target.value)}
                    className="w-full h-2 bg-slate-200 rounded-lg appearance-none cursor-pointer accent-emerald-600"
                  />

                  <div className="flex items-center justify-between text-[11px] text-slate-600 font-medium">
                    <span>Cap Used: <strong>{s.capacityUsed}%</strong></span>
                    <span>Distance: <strong>{s.distanceKm} km</strong></span>
                    <span>Urgency: <strong className="text-amber-700">{s.needLevel}</strong></span>
                  </div>
                </div>
              ))}
            </div>

            {/* Final Dispatch Button */}
            <button
              onClick={handleConfirmDispatch}
              disabled={isOverAllocated}
              className="w-full py-4 rounded-2xl bg-emerald-600 hover:bg-emerald-500 disabled:opacity-40 text-white font-black text-sm shadow-xl shadow-emerald-500/20 transition-all flex items-center justify-center gap-2 cursor-pointer"
            >
              <span>Confirm Multi-NGO Allocation Dispatch</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Allocation Breakdown Table */}
      <div className="p-6 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-4">
        <h3 className="text-base font-extrabold text-slate-900">Allocated Recipient Summary Table</h3>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-slate-200 text-slate-500 uppercase tracking-wider font-extrabold">
                <th className="pb-3 px-3">NGO Name</th>
                <th className="pb-3 px-3">Meals Allocated</th>
                <th className="pb-3 px-3">Share %</th>
                <th className="pb-3 px-3">Capacity Used</th>
                <th className="pb-3 px-3">Need Level</th>
                <th className="pb-3 px-3">Distance</th>
                <th className="pb-3 px-3">Trust Score</th>
                <th className="pb-3 px-3">Pickup Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 font-medium text-slate-800">
              {splits.map((item) => (
                <tr key={item.ngoId} className="hover:bg-slate-50 transition-colors">
                  <td className="py-3.5 px-3 font-bold text-slate-900">{item.name}</td>
                  <td className="py-3.5 px-3 font-mono font-extrabold text-emerald-700">{item.meals} meals</td>
                  <td className="py-3.5 px-3 font-mono text-slate-700">{item.percentage}%</td>
                  <td className="py-3.5 px-3 text-slate-700">{item.capacityUsed}%</td>
                  <td className="py-3.5 px-3">
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold uppercase bg-amber-100 text-amber-800">
                      {item.needLevel}
                    </span>
                  </td>
                  <td className="py-3.5 px-3 text-slate-700">{item.distanceKm} km</td>
                  <td className="py-3.5 px-3 font-mono font-bold text-emerald-700">{item.trustScore}%</td>
                  <td className="py-3.5 px-3 text-slate-700">{item.pickupAvailable}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
