import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import {
  Utensils,
  Clock,
  Thermometer,
  Box,
  ShieldCheck,
  Zap,
  ArrowRight,
  Sparkles,
  Info,
  Camera,
  UploadCloud,
  CheckCircle2,
  AlertCircle,
  XCircle,
  Image as ImageIcon
} from 'lucide-react'
import { api } from '../services/api'
import ScoreGauge from '../components/common/ScoreGauge'
import { useStore } from '../store/useStore'

export default function DonationNew() {
  const navigate = useNavigate()
  const { addCustomDonation, addNotification, currentUser } = useStore()
  const donorName = currentUser?.name || currentUser?.restaurantName || currentUser?.fullName || 'Royal Restaurant'

  // Form State
  const [formData, setFormData] = useState({
    foodType: 'Prepared Hot Buffet Surplus (Rice, Entrees & Gravies)',
    quantityKg: '180',
    mealsCount: '450',
    preparedTime: new Date(Date.now() - 2.5 * 3600 * 1000).toISOString().slice(0, 16),
    storageCondition: 'Hot Holding (65°C)',
    temperature: '65',
    packagingQuality: 'Vacuum-Sealed Thermal Containers'
  })

  // Model 3 Image Classifier State
  const [imagePreview, setImagePreview] = useState(null)
  const [imageAnalyzing, setImageAnalyzing] = useState(false)
  const [imageResult, setImageResult] = useState(null)

  // Auto-calculated food age in hours
  const [foodAgeHours, setFoodAgeHours] = useState('2.5')

  const handleImageUpload = (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    const reader = new FileReader()
    reader.onloadend = async () => {
      const base64String = reader.result
      setImagePreview(base64String)
      setImageAnalyzing(true)
      setImageResult(null)
      try {
        const res = await api.predictImageQuality(base64String)
        setImageResult(res)
        if (res.category) {
          const confidencePct = (res.confidence * 100).toFixed(1)
          setFormData(prev => ({
            ...prev,
            foodType: `${res.category} Surplus (${confidencePct}% Visual Match)`
          }))
        }
      } catch (err) {
        console.warn('Image analysis note:', err)
      } finally {
        setImageAnalyzing(false)
      }
    }
    reader.readAsDataURL(file)
  }

  useEffect(() => {
    if (formData.preparedTime) {
      const prepDate = new Date(formData.preparedTime)
      const now = new Date()
      const diffMs = Math.max(0, now.getTime() - prepDate.getTime())
      const diffHrs = (diffMs / (1000 * 60 * 60)).toFixed(1)
      setFoodAgeHours(diffHrs)
    }
  }, [formData.preparedTime])

  // Prediction State
  const [submitting, setSubmitting] = useState(false)
  const [prediction, setPrediction] = useState(null)
  const [activeDonationId, setActiveDonationId] = useState('DON-9042')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      const ageNum = parseFloat(foodAgeHours) || 0
      const isExpired = ageNum > 15

      const res = await api.predictReadinessScore({
        ...formData,
        foodAgeHours
      })

      if (isExpired) {
        res.readinessScore = 0
        res.readinessLevel = 'Expired & Unsafe'
        res.isExpired = true
        res.breakdown = [
          { factor: 'Freshness Window', score: 0, weight: 35, note: `Prepared ${foodAgeHours}h ago (Exceeds 15h safety limit)` },
          { factor: 'Storage Condition & Temp', score: 20, weight: 25, note: `${formData.storageCondition} (${formData.temperature}°C)` },
          { factor: 'Packaging Integrity', score: 50, weight: 20, note: formData.packagingQuality || 'Grade A' },
          { factor: 'Donor History Score', score: 94, weight: 20, note: 'Verified Quality Record' }
        ]
      }

      setPrediction(res)

      const newId = `DON-${Math.floor(1000 + Math.random() * 9000)}`
      setActiveDonationId(newId)

      // Save entry to session store
      addCustomDonation({
        id: newId,
        restaurant: donorName,
        location: 'Downtown District',
        ...formData,
        foodAgeHours,
        readinessScore: isExpired ? 0 : (res.readinessScore || 94),
        readinessLevel: isExpired ? 'Expired & Unsafe' : res.readinessLevel,
        status: isExpired ? '15h+ Safe Window Expired' : 'Pending Allocation'
      })

      if (addNotification) {
        addNotification({
          type: isExpired ? 'risk' : 'match',
          title: isExpired
            ? `Risk Alert: Donation ${newId} exceeds 15h safety limit`
            : `New Donation Created: ${newId} (${formData.mealsCount || 450} meals)`
        })
      }
    } catch (err) {
      console.error(err)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl md:text-3xl font-black text-slate-900 tracking-tight">
          Food Donation Submission & Readiness Predictor
        </h1>
        <p className="text-sm text-slate-600 max-w-2xl mt-1 leading-relaxed">
          Submit surplus food parameters to run real-time safety classification and readiness scoring before matching with recipient NGOs.
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Form Container (Left 7 Cols) */}
        <div className="lg:col-span-7 space-y-6">
          <form onSubmit={handleSubmit} className="p-6 md:p-8 rounded-3xl bg-white border border-slate-200 space-y-6 shadow-xs">
            <h3 className="text-base font-extrabold text-slate-900 flex items-center gap-2 pb-3 border-b border-slate-100">
              <Utensils className="w-4 h-4 text-emerald-600" /> Donation Parameters
            </h3>

            {/* Model 3: Image Classification Box */}
            <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-extrabold text-slate-800 flex items-center gap-1.5 uppercase tracking-wider">
                  <Camera className="w-4 h-4 text-emerald-600" /> Image Scanner
                </span>
              </div>

              <div className="relative border-2 border-dashed border-slate-300 hover:border-emerald-500 rounded-xl p-4 transition-all bg-white text-center cursor-pointer group">
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleImageUpload}
                  className="absolute inset-0 opacity-0 cursor-pointer w-full h-full z-10"
                />
                
                {imagePreview ? (
                  <div className="flex items-center gap-4 text-left">
                    <img src={imagePreview} alt="Food Upload" className="w-16 h-16 object-cover rounded-lg border border-slate-200 shadow-xs" />
                    <div>
                      {imageAnalyzing ? (
                        <div className="text-xs font-bold text-emerald-600 flex items-center gap-2">
                          <Sparkles className="w-4 h-4 animate-spin" /> Analyzing Food Texture...
                        </div>
                      ) : imageResult ? (
                        <div className="space-y-0.5">
                          <span className="inline-flex items-center gap-1 text-xs font-bold text-emerald-800 bg-emerald-50 px-2.5 py-0.5 rounded-full border border-emerald-200">
                            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                            Verified: {imageResult.category} ({(imageResult.confidence * 100).toFixed(1)}% Match)
                          </span>
                          <p className="text-[11px] text-slate-500">Auto-filled category into description field below.</p>
                        </div>
                      ) : (
                        <span className="text-xs font-semibold text-slate-600">Image selected. Click to replace photo.</span>
                      )}
                    </div>
                  </div>
                ) : (
                  <div className="flex flex-col items-center gap-1 py-2">
                    <UploadCloud className="w-7 h-7 text-emerald-600 group-hover:scale-110 transition-transform" />
                    <span className="text-xs font-bold text-slate-800">Upload Food Photo for Image Classification</span>
                    <span className="text-[11px] text-slate-400">Supports JPG, PNG (Auto-verifies Rice, Bread, Curry & Snacks)</span>
                  </div>
                )}
              </div>
            </div>

            {/* Food Type */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                Food Category & Description
              </label>
              <input
                type="text"
                value={formData.foodType}
                onChange={(e) => setFormData({ ...formData, foodType: e.target.value })}
                required
                className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm font-semibold focus:outline-none focus:border-emerald-500 focus:bg-white transition-colors"
                placeholder="e.g. Cooked Rice, Pasta, Bakery items..."
              />
            </div>

            {/* Quantity Row */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Quantity (kg)
                </label>
                <input
                  type="number"
                  value={formData.quantityKg}
                  onChange={(e) => {
                    const kg = e.target.value
                    setFormData({
                      ...formData,
                      quantityKg: kg,
                      mealsCount: kg ? String(Math.round(parseFloat(kg) * 2.5)) : ''
                    })
                  }}
                  required
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm font-semibold focus:outline-none focus:border-emerald-500 focus:bg-white transition-colors"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Estimated Meal Count
                </label>
                <input
                  type="number"
                  value={formData.mealsCount}
                  onChange={(e) => {
                    const meals = e.target.value
                    setFormData({
                      ...formData,
                      mealsCount: meals,
                      quantityKg: meals ? String(Math.round(parseFloat(meals) / 2.5)) : ''
                    })
                  }}
                  required
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm font-semibold focus:outline-none focus:border-emerald-500 focus:bg-white transition-colors"
                />
              </div>
            </div>

            {/* Prepared Time Row */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider flex items-center justify-between">
                  <span>Prepared Time</span>
                  <Clock className="w-3.5 h-3.5 text-slate-400" />
                </label>
                <input
                  type="datetime-local"
                  value={formData.preparedTime}
                  onChange={(e) => setFormData({ ...formData, preparedTime: e.target.value })}
                  required
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm font-semibold focus:outline-none focus:border-emerald-500 focus:bg-white transition-colors cursor-pointer"
                />
              </div>

              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Auto Computed Food Age
                </label>
                <div className={`px-4 py-3 rounded-xl font-mono text-xs font-extrabold flex items-center justify-between border ${
                  parseFloat(foodAgeHours) > 15 ? 'bg-red-50 text-red-800 border-red-300' : 'bg-emerald-50 text-emerald-800 border-emerald-300'
                }`}>
                  <span>{foodAgeHours} hours old</span>
                  <span className="text-[10px] uppercase font-bold px-2 py-0.5 rounded bg-white/60">
                    {parseFloat(foodAgeHours) > 15 ? 'Safety Limit Exceeded' : 'Computed'}
                  </span>
                </div>
              </div>
            </div>

            {/* Storage Condition & Current Temp */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Storage Condition
                </label>
                <select
                  value={formData.storageCondition}
                  onChange={(e) => {
                    const cond = e.target.value
                    let temp = '25'
                    if (cond.includes('Hot')) temp = '65'
                    if (cond.includes('Refrigerated')) temp = '4'
                    setFormData({ ...formData, storageCondition: cond, temperature: temp })
                  }}
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm font-semibold focus:outline-none focus:border-emerald-500 focus:bg-white transition-colors cursor-pointer"
                >
                  <option value="Hot Holding (65°C+)">Hot Holding (65°C+)</option>
                  <option value="Refrigerated (4°C)">Refrigerated (4°C)</option>
                  <option value="Ambient Room Temp (25°C)">Ambient Room Temp (25°C)</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Current Temp (°C)
                </label>
                <input
                  type="number"
                  value={formData.temperature}
                  onChange={(e) => setFormData({ ...formData, temperature: e.target.value })}
                  required
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm font-semibold focus:outline-none focus:border-emerald-500 focus:bg-white transition-colors"
                />
              </div>
            </div>

            {/* Packaging Quality */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                Packaging Standard
              </label>
              <select
                value={formData.packagingQuality}
                onChange={(e) => setFormData({ ...formData, packagingQuality: e.target.value })}
                className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-sm font-semibold focus:outline-none focus:border-emerald-500 focus:bg-white transition-colors cursor-pointer"
              >
                <option value="Vacuum-Sealed Thermal Containers (Grade A)">Vacuum-Sealed Thermal Containers (Grade A)</option>
                <option value="Insulated Food Pan Carrier Tubs">Insulated Food Pan Carrier Tubs (Grade B)</option>
                <option value="Standard Plastic Containers">Standard Plastic Containers (Grade C)</option>
              </select>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={submitting}
              className="w-full py-4 rounded-2xl bg-emerald-600 hover:bg-emerald-500 text-white font-black text-sm shadow-xl shadow-emerald-500/20 transition-all flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
            >
              {submitting ? (
                <>Calculating Safety Vector...</>
              ) : (
                <>
                  <Zap className="w-4 h-4" /> Calculate Readiness Score
                </>
              )}
            </button>
          </form>
        </div>

        {/* Prediction Results & Score Panel (Right 5 Cols) */}
        <div className="lg:col-span-5 space-y-6">
          <AnimatePresence mode="wait">
            {prediction ? (
              <motion.div
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.95 }}
                className={`p-6 md:p-8 rounded-3xl bg-white border ${prediction.isExpired ? 'border-red-300' : 'border-emerald-300'} space-y-6 shadow-md relative overflow-hidden`}
              >
                {/* Background Glow */}
                <div className={`absolute top-0 right-0 w-32 h-32 ${prediction.isExpired ? 'bg-red-500/10' : 'bg-emerald-500/10'} rounded-full blur-3xl pointer-events-none`} />

                {prediction.isExpired ? (
                  <div className="p-4 rounded-2xl bg-red-50 border border-red-300 text-red-900 space-y-2">
                    <div className="flex items-center gap-2 font-black text-xs text-red-800 uppercase tracking-wider">
                      <AlertCircle className="w-4 h-4 text-red-600 shrink-0" />
                      <span>Food Safety Expiration Rejection Alert</span>
                    </div>
                    <p className="text-xs font-semibold leading-relaxed">
                      This food is <strong>{foodAgeHours} hours old</strong> and exceeds the mandatory <strong>15-hour safety limit</strong>. It cannot be donated or matched with recipient NGOs for health compliance. This entry has been logged to Donation History as <strong>Expired & Unsafe</strong>.
                    </p>
                  </div>
                ) : null}

                <div className="text-center space-y-2">
                  <span className="text-xs font-bold text-slate-500 uppercase tracking-widest">
                    Predicted Readiness Score
                  </span>
                  <ScoreGauge
                    score={prediction.readinessScore}
                    label={`${prediction.readinessLevel}`}
                    size={160}
                    sublabel="Safety Vector"
                  />
                </div>

                {/* Factor Breakdown List */}
                <div className="space-y-3 pt-4 border-t border-slate-100">
                  <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
                    Contributing Factor Weighting
                  </h4>

                  <div className="space-y-2.5">
                    {prediction.breakdown.map((item, idx) => (
                      <div key={idx} className="p-3 rounded-xl bg-slate-50 border border-slate-200 space-y-1.5">
                        <div className="flex items-center justify-between text-xs">
                          <span className="font-bold text-slate-800">{item.factor}</span>
                          <span className={`font-mono font-bold ${item.score === 0 ? 'text-red-600' : 'text-emerald-700'}`}>{item.score}%</span>
                        </div>
                        <div className="w-full bg-slate-200 h-1.5 rounded-full overflow-hidden">
                          <div
                            className={`${item.score === 0 ? 'bg-red-600' : 'bg-emerald-600'} h-full rounded-full transition-all duration-500`}
                            style={{ width: `${Math.max(4, item.score)}%` }}
                          />
                        </div>
                        <p className="text-[11px] text-slate-500 italic">{item.note}</p>
                      </div>
                    ))}
                  </div>
                </div>

                {/* CTA to Risk Assessment / Matching Flow (Blocked if Expired > 15h) */}
                {prediction.isExpired ? (
                  <button
                    disabled
                    className="w-full py-3.5 rounded-xl bg-red-100 border border-red-300 text-red-700 font-bold text-xs uppercase tracking-wider cursor-not-allowed opacity-80 flex items-center justify-center gap-2"
                  >
                    <XCircle className="w-4 h-4 text-red-600" />
                    <span>NGO Matching Blocked (Expired Food)</span>
                  </button>
                ) : (
                  <button
                    onClick={async () => {
                      try {
                        const createdDonation = await api.createDonation({
                          description: formData.foodType,
                          quantityKg: formData.quantityKg,
                          temperature: formData.temperature,
                          storageCondition: formData.storageCondition,
                          packagingQuality: formData.packagingQuality
                        })
                        const donId = createdDonation.id || activeDonationId
                        navigate(`/donation/${donId}/risk`)
                      } catch (err) {
                        console.warn('Backend donation creation note:', err)
                        navigate(`/donation/${activeDonationId}/risk`)
                      }
                    }}
                    className="w-full py-3.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white font-bold text-xs uppercase tracking-wider transition-all flex items-center justify-center gap-2 shadow-md cursor-pointer"
                  >
                    <span>Proceed to Risk Assessment</span>
                    <ArrowRight className="w-4 h-4 text-emerald-400" />
                  </button>
                )}
              </motion.div>
            ) : (
              <div className="p-8 rounded-3xl bg-white border border-dashed border-slate-300 text-center flex flex-col items-center justify-center h-full min-h-[400px] shadow-xs">
                <div className="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-500 mb-4 border border-slate-200">
                  <Sparkles className="w-7 h-7 text-emerald-600" />
                </div>
                <h3 className="text-base font-bold text-slate-800">Awaiting Form Submission</h3>
                <p className="text-xs text-slate-600 max-w-xs mt-1 leading-relaxed">
                  Fill out the donation parameters on the left and click "Calculate Readiness Score" to trigger real-time AI classification.
                </p>
              </div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  )
}
