import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
  Clock,
  CheckCircle2,
  AlertTriangle,
  Search,
  Filter,
  Building2,
  HeartHandshake,
  ArrowRight,
  ShieldCheck,
  Calendar,
  Utensils,
  Leaf,
  Sparkles,
  Download
} from 'lucide-react'
import { useStore } from '../store/useStore'
import RiskBadge from '../components/common/RiskBadge'

export default function DonationHistory() {
  const navigate = useNavigate()
  const { customDonations, userRole, currentUser } = useStore()

  const [searchQuery, setSearchQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('all') // 'all' | 'completed' | 'intransit' | 'expired'
  const [exportSuccess, setExportSuccess] = useState(false)

  const handleExportCSV = () => {
    try {
      const csvRows = [
        ['FoodFlow Donation History Registry Audit Log'],
        ['Exported At', new Date().toLocaleString()],
        [],
        ['Donation ID', 'Food Category Description', 'Donor / Restaurant', 'Location', 'Quantity (kg)', 'Estimated Meals', 'Readiness Score', 'Status', 'NGO Matched / Recipient'],
        ...filteredHistory.map(item => [
          item.id,
          `"${item.foodType}"`,
          `"${item.restaurant || 'Royal Bistro'}"`,
          `"${item.location || 'Metro Area'}"`,
          item.quantityKg || '180',
          item.mealsCount || '450',
          `${item.readinessScore || 94}%`,
          `"${item.status}"`,
          `"${item.ngoMatched || 'Hope Kitchen Community Shelter'}"`
        ])
      ]

      const csvString = csvRows.map(row => row.join(',')).join('\n')
      const blob = new Blob([csvString], { type: 'text/csv;charset=utf-8;' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `FoodFlow_Donation_History_${new Date().toISOString().slice(0, 10)}.csv`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)

      setExportSuccess(true)
      setTimeout(() => setExportSuccess(false), 3000)
    } catch (err) {
      console.error('Export CSV error:', err)
    }
  }

  // Combine custom donations with historical logs
  const baseHistory = customDonations.length > 0 ? customDonations : [
    {
      id: 'DON-9042',
      foodType: 'Prepared Hot Meals (Catering Surplus)',
      restaurant: currentUser?.name || 'Royal Bistro Restaurant',
      location: 'Downtown District, Zone 4',
      mealsCount: '600',
      quantityKg: '180',
      readinessScore: 94,
      readinessLevel: 'High',
      riskLevel: 'Low',
      preparedTime: '2026-08-09 12:30',
      status: 'Delivered & Completed',
      ngoMatched: 'Hope Kitchen Community Shelter'
    },
    {
      id: 'DON-8819',
      foodType: 'Fresh Baked Artisan Breads & Pastries',
      restaurant: currentUser?.name || 'Royal Bistro Restaurant',
      location: 'Westside Hub',
      mealsCount: '240',
      quantityKg: '96',
      readinessScore: 86,
      readinessLevel: 'High',
      riskLevel: 'Low',
      preparedTime: '2026-08-08 18:10',
      status: 'Delivered & Completed',
      ngoMatched: 'Metropolitan Food Rescuers'
    },
    {
      id: 'DON-7741',
      foodType: 'Chilled Prepped Seafood & Salad Trays',
      restaurant: currentUser?.name || 'Royal Bistro Restaurant',
      location: 'Harbor Wharf',
      mealsCount: '310',
      quantityKg: '124',
      readinessScore: 68,
      readinessLevel: 'Medium',
      riskLevel: 'Medium',
      preparedTime: '2026-08-07 14:00',
      status: '15h+ Safe Window Expired',
      ngoMatched: 'Sunrise Elderly Care'
    }
  ]

  const activeRestaurantName = currentUser?.name || currentUser?.restaurantName || 'Royal Bistro'

  // Filter history per role: Admin sees ALL, Restaurant owner sees ONLY their own donations
  const roleFilteredHistory = userRole === 'admin'
    ? baseHistory
    : baseHistory.filter(item => {
        if (!item.restaurant) return true
        const rName = item.restaurant.toLowerCase()
        const uName = activeRestaurantName.toLowerCase()
        return rName.includes(uName) || uName.includes(rName)
      })

  const filteredHistory = roleFilteredHistory.filter(item => {
    const matchesSearch = item.foodType.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          item.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          (item.restaurant && item.restaurant.toLowerCase().includes(searchQuery.toLowerCase()))

    if (!matchesSearch) return false

    if (statusFilter === 'completed') return item.status.includes('Completed') || item.status.includes('Delivered')
    if (statusFilter === 'intransit') return item.status.includes('Pending') || item.status.includes('Transit')
    if (statusFilter === 'expired') return item.status.includes('Expired') || (parseFloat(item.foodAgeHours || 0) > 15)
    return true
  })

  return (
    <div className="space-y-8">
      {/* Header Banner */}
      <div className="p-6 md:p-8 rounded-3xl bg-white border border-slate-200 shadow-xs space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-bold border border-emerald-300 mb-2">
              <Clock className="w-3.5 h-3.5" />
              <span>Full Audit Trail & History</span>
            </div>
            <h1 className="text-2xl md:text-3xl font-black text-slate-900 tracking-tight">
              Donation Activity History & Log
            </h1>
            <p className="text-sm text-slate-600 max-w-2xl mt-1 leading-relaxed font-medium">
              Complete chronological registry of food rescue submissions, readiness scores, NGO allocation transfers, and completed deliveries.
            </p>
          </div>

          <div className="flex items-center gap-3 shrink-0">
            <button
              onClick={handleExportCSV}
              className="px-4 py-3 rounded-2xl bg-slate-100 hover:bg-slate-200 text-slate-800 border border-slate-200 font-extrabold text-xs flex items-center justify-center gap-2 cursor-pointer transition-colors"
            >
              <Download className="w-4 h-4 text-emerald-600" />
              <span>{exportSuccess ? 'CSV Exported!' : 'Export CSV Registry'}</span>
            </button>

            {userRole === 'restaurant' && (
              <button
                onClick={() => navigate('/donation/new')}
                className="px-5 py-3 rounded-2xl bg-emerald-600 hover:bg-emerald-500 text-white font-extrabold text-xs shadow-md shadow-emerald-500/20 flex items-center justify-center gap-2 cursor-pointer"
              >
                <Utensils className="w-4 h-4" />
                <span>Submit New Donation</span>
              </button>
            )}
          </div>
        </div>

        {/* Filter Controls Bar */}
        <div className="flex flex-wrap items-center justify-between gap-4 pt-4 border-t border-slate-100">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setStatusFilter('all')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                statusFilter === 'all'
                  ? 'bg-slate-900 text-white shadow-md'
                  : 'bg-slate-100 text-slate-600 hover:text-slate-900'
              }`}
            >
              All Records ({baseHistory.length})
            </button>
            <button
              onClick={() => setStatusFilter('completed')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                statusFilter === 'completed'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'bg-slate-100 text-slate-600 hover:text-slate-900'
              }`}
            >
              Delivered & Completed
            </button>
            <button
              onClick={() => setStatusFilter('expired')}
              className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
                statusFilter === 'expired'
                  ? 'bg-red-600 text-white shadow-md'
                  : 'bg-slate-100 text-slate-600 hover:text-slate-900'
              }`}
            >
              15h+ Expired Log
            </button>
          </div>

          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search history..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9 pr-4 py-2 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:border-emerald-500 w-48 sm:w-64"
            />
          </div>
        </div>
      </div>

      {/* History Records List */}
      {filteredHistory.length === 0 ? (
        <div className="p-12 text-center rounded-3xl bg-white border border-slate-200 shadow-xs space-y-3">
          <Clock className="w-12 h-12 text-slate-400 mx-auto" />
          <h3 className="text-base font-bold text-slate-900">No Historical Records Found</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto">
            There are no donation history entries matching your selected status filter.
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredHistory.map((item) => {
            const cleanTitle = (item.foodType || '')
              .replace(/AI Visual Match/g, 'Visual Match')
              .replace(/AI /g, '')

            return (
              <motion.div
                key={item.id}
                whileHover={{ y: -2 }}
                onClick={() => navigate(`/donation/${item.id}/risk`)}
                className="p-6 rounded-3xl bg-white border border-slate-200 hover:border-slate-300 shadow-xs space-y-4 cursor-pointer transition-all"
              >
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100">
                  <div className="flex items-center gap-3">
                    <span className="text-xs font-mono font-bold px-2.5 py-1 rounded bg-slate-100 text-slate-800 border border-slate-200">
                      {item.id}
                    </span>
                    <h3 className="text-base font-extrabold text-slate-900">{cleanTitle}</h3>
                  </div>

                <div className="flex items-center gap-2">
                  <span className={`px-3 py-1 rounded-full text-xs font-bold border ${
                    item.status.includes('Completed') || item.status.includes('Delivered')
                      ? 'bg-emerald-100 text-emerald-800 border-emerald-300'
                      : item.status.includes('Expired')
                      ? 'bg-red-100 text-red-800 border-red-300'
                      : 'bg-amber-100 text-amber-800 border-amber-300'
                  }`}>
                    {item.status}
                  </span>
                  <button
                    onClick={(e) => {
                      e.stopPropagation()
                      navigate(`/donation/${item.id}/risk`)
                    }}
                    className="px-3 py-1 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold transition-all shadow-xs flex items-center gap-1.5"
                  >
                    <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
                    View Risk Matrix
                  </button>
                </div>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-xs">
                <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100">
                  <span className="text-slate-500 block text-[10px] uppercase font-bold">Donor Partner</span>
                  <span className="font-extrabold text-slate-900">{item.restaurant || 'Royal Bistro'}</span>
                </div>
                <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100">
                  <span className="text-slate-500 block text-[10px] uppercase font-bold">Meal Quantity</span>
                  <span className="font-extrabold text-slate-900">{item.mealsCount} meals ({item.quantityKg} kg)</span>
                </div>
                <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100">
                  <span className="text-slate-500 block text-[10px] uppercase font-bold">Readiness Score</span>
                  <span className="font-extrabold text-emerald-700">{item.readinessScore}% ({item.readinessLevel || 'High'})</span>
                </div>
                <div className="p-3 rounded-2xl bg-slate-50 border border-slate-100">
                  <span className="text-slate-500 block text-[10px] uppercase font-bold">Recipient NGO</span>
                  <span className="font-extrabold text-slate-800">{item.ngoMatched || 'Hope Kitchen Shelter'}</span>
                </div>
              </div>
            </motion.div>
          )
        })}
        </div>
      )}
    </div>
  )
}
