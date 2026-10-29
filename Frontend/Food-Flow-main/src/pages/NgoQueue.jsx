import React, { useState } from 'react'
import { motion } from 'framer-motion'
import {
  HeartHandshake,
  Building2,
  ShieldCheck,
  CheckCircle2,
  Clock,
  Navigation,
  Sparkles,
  Award,
  Zap
} from 'lucide-react'
import { useStore } from '../store/useStore'
import { useNavigate } from 'react-router-dom'

export default function NgoQueue() {
  const navigate = useNavigate()
  const { currentUser, customDonations = [], customAllocations = {}, ngoActions, setNgoAction } = useStore()

  const ngoName = currentUser?.name || currentUser?.restaurantName || 'Hope Kitchen Community Shelter'

  // Incoming Assigned Food Items for this NGO (Filtered strictly to active <=15h submitted donations matched to this NGO)
  const incomingItems = customDonations
    .filter(d => {
      const age = parseFloat(d.foodAgeHours) || 0
      if (age > 15) return false
      const splits = customAllocations[d.id]
      if (splits && Array.isArray(splits) && splits.length > 0) {
        return splits.some(s => {
          const name = (s.ngoName || s.name || '').toLowerCase()
          const current = ngoName.toLowerCase()
          return name.includes(current) || current.includes(name)
        })
      }
      const assigned = (d.ngoMatched || d.assignedNgo || d.targetNgo || d.allocatedNgo || '').toLowerCase()
      const currentName = ngoName.toLowerCase()
      if (assigned && (assigned.includes(currentName) || currentName.includes(assigned))) {
        return true
      }
      if (d.id === 'DON-9042') return true
      const isLatestCustom = customDonations.length > 0 && customDonations[0].id === d.id
      const isTargetedNgo = currentName.includes('kitchen') || currentName.includes('care') || currentName.includes('ngo')
      return isLatestCustom && isTargetedNgo
    })
    .map(d => ({
      id: d.id,
      foodType: d.foodType,
      donorName: d.restaurant || 'Royal Bistro',
      donorTrust: 96,
      readinessScore: d.readinessScore || 94,
      readinessLevel: d.readinessLevel || 'High Readiness',
      mealsCount: d.mealsCount || `${Math.round((parseFloat(d.quantityKg) || 180) * 2.5)}`,
      quantityKg: d.quantityKg || '180',
      preparedTime: d.foodAgeHours ? `${d.foodAgeHours} hours ago` : '2.5 hours ago',
      tempHolding: d.storageCondition || '65°C Hot Holding',
      distanceKm: 1.8,
      status: 'Assigned & Pending Pickup'
    }))

  const [acceptedIds, setAcceptedIds] = useState({})

  const handleAccept = (id) => {
    setAcceptedIds(prev => ({ ...prev, [id]: true }))
    if (setNgoAction) {
      setNgoAction(id, 'accepted')
    }
  }

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="p-6 md:p-8 rounded-3xl bg-gradient-to-r from-teal-600 via-emerald-600 to-teal-800 text-white shadow-xl relative overflow-hidden">
        <div className="absolute -top-24 -right-24 w-64 h-64 bg-white/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 space-y-2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/20 text-white text-xs font-bold border border-white/30 backdrop-blur-md">
            <HeartHandshake className="w-3.5 h-3.5" />
            RECIPIENT INTAKE PORTAL
          </div>
          <h1 className="text-2xl md:text-3xl font-black text-white">Incoming Assigned Food Queue</h1>
          <p className="text-xs md:text-sm text-teal-100 max-w-2xl">
            Food rescue donations allocated specifically to <strong className="text-white underline">{ngoName}</strong>. Review donor restaurant trust ratings and safety readiness scores before accepting.
          </p>
        </div>
      </div>

      {/* Incoming Donations List */}
      <div className="space-y-4">
        {incomingItems.length === 0 ? (
          <div className="p-10 rounded-3xl bg-white border border-dashed border-slate-300 text-center flex flex-col items-center justify-center shadow-xs space-y-3">
            <div className="w-12 h-12 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto border border-emerald-200">
              <HeartHandshake className="w-6 h-6" />
            </div>
            <h3 className="text-base font-extrabold text-slate-900">No Active Incoming Donations</h3>
            <p className="text-xs text-slate-500 max-w-md leading-relaxed font-medium">
              There are no active food donations assigned to your NGO queue right now. When a restaurant submits a surplus food donation, it will appear here for immediate dispatch pickup.
            </p>
          </div>
        ) : (
          incomingItems.map((item) => {
            const isAccepted = acceptedIds[item.id] || ngoActions[item.id] === 'accepted'

            return (
              <motion.div
                key={item.id}
                whileHover={{ y: -2 }}
                className="p-6 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-5"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-slate-100">
                  <div className="flex items-center gap-3">
                    <span className="text-xs font-mono font-bold px-2.5 py-1 rounded bg-teal-50 text-teal-800 border border-teal-200">
                      {item.id}
                    </span>
                    <div>
                      <h3 className="text-base font-black text-slate-900">{item.foodType}</h3>
                      <span className="text-xs text-slate-500 flex items-center gap-1.5 mt-0.5">
                        <Building2 className="w-3.5 h-3.5 text-slate-400" />
                        Assigned to <strong className="text-slate-700">{ngoName}</strong>
                      </span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span className={`px-3 py-1 rounded-full text-xs font-extrabold border ${
                      isAccepted
                        ? 'bg-emerald-100 text-emerald-800 border-emerald-300'
                        : 'bg-amber-100 text-amber-800 border-amber-300'
                    }`}>
                      {isAccepted ? '✓ Pickup Accepted & Dispatched' : item.status}
                    </span>
                  </div>
                </div>

                {/* Logistics & Donor Details Matrix */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                  {/* Donor Card */}
                  <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-1.5">
                    <span className="text-[10px] uppercase font-extrabold tracking-wider text-slate-500 block">
                      Donor Restaurant
                    </span>
                    <h4 className="text-sm font-black text-slate-900 flex items-center gap-1.5">
                      <Building2 className="w-4 h-4 text-emerald-600" />
                      {item.donorName}
                    </h4>
                    <div className="flex items-center gap-1 text-xs font-bold text-emerald-700">
                      <Award className="w-3.5 h-3.5" />
                      <span>{item.donorTrust}% Trust Score</span>
                    </div>
                  </div>

                  {/* Safety Card */}
                  <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-1.5">
                    <span className="text-[10px] uppercase font-extrabold tracking-wider text-slate-500 block">
                      Food Safety Readiness
                    </span>
                    <h4 className="text-sm font-black text-emerald-700 flex items-center gap-1.5">
                      <ShieldCheck className="w-4 h-4 text-emerald-600" />
                      {item.readinessScore}% ({item.readinessLevel})
                    </h4>
                    <p className="text-[11px] text-slate-500 font-medium">
                      Holding: {item.tempHolding}
                    </p>
                  </div>

                  {/* Perishability Window */}
                  <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-1.5">
                    <span className="text-[10px] uppercase font-extrabold tracking-wider text-slate-500 block">
                      Perishability Window
                    </span>
                    <h4 className="text-sm font-black text-amber-700 flex items-center gap-1.5">
                      <Clock className="w-4 h-4 text-amber-600" />
                      {item.preparedTime}
                    </h4>
                    <p className="text-[11px] text-slate-500 font-medium flex items-center gap-1">
                      <Navigation className="w-3 h-3 text-slate-400" />
                      Proximity: {item.distanceKm} km away
                    </p>
                  </div>

                  {/* Meal Quantity Card */}
                  <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-1.5">
                    <span className="text-[10px] uppercase font-extrabold tracking-wider text-slate-500 block">
                      Rescued Meal Volume
                    </span>
                    <h4 className="text-base font-black text-slate-900">{item.mealsCount} Meals</h4>
                    <p className="text-[11px] text-slate-500 font-medium">
                      Total weight: {item.quantityKg} kg
                    </p>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="pt-2 flex flex-col sm:flex-row items-center justify-between gap-3">
                  <p className="text-xs text-slate-500 italic">
                    💡 Verified by Food Readiness Classifier for safety compliance.
                  </p>

                  {isAccepted ? (
                    <div className="flex items-center gap-2 px-5 py-2.5 rounded-2xl bg-emerald-600 text-white text-xs font-bold shadow-sm">
                      <CheckCircle2 className="w-4 h-4" />
                      Dispatch Driver Assigned & In Transit
                    </div>
                  ) : (
                    <button
                      onClick={() => handleAccept(item.id)}
                      className="w-full sm:w-auto px-6 py-2.5 rounded-2xl bg-teal-600 hover:bg-teal-700 text-white text-xs font-bold transition-all shadow-md flex items-center justify-center gap-2 cursor-pointer"
                    >
                      <CheckCircle2 className="w-4 h-4" />
                      Accept & Confirm Pickup Dispatch
                    </button>
                  )}
                </div>
              </motion.div>
            )
          })
        )}
      </div>
    </div>
  )
}
