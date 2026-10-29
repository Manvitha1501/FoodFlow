import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ShieldCheck,
  Building2,
  HeartHandshake,
  CheckCircle2,
  XCircle,
  PlusCircle,
  UserCheck,
  Lock,
  ArrowRight,
  Sparkles,
  Search,
  Check,
  AlertCircle,
  Award,
  Utensils,
  LayoutDashboard,
  KeyRound
} from 'lucide-react'
import { useStore } from '../store/useStore'

export default function AdminPage() {
  const navigate = useNavigate()
  const {
    userRole,
    currentUser,
    pendingRegistrations,
    registeredEntities,
    approveRegistration,
    rejectRegistration,
    createEntityByAdmin,
    loginUser
  } = useStore()

  // Security Auth State
  const isAdminAuthenticated = userRole === 'admin' && currentUser?.role === 'admin'
  const [passcode, setPasscode] = useState('')
  const [authError, setAuthError] = useState('')

  // Admin Panel Tab State
  const [activeTab, setActiveTab] = useState('pending') // 'pending' | 'precreate' | 'active'
  const [filterType, setFilterType] = useState('all') // 'all' | 'restaurant' | 'ngo'
  const [searchQuery, setSearchQuery] = useState('')

  // New Entity Pre-creation form state
  const [newType, setNewType] = useState('restaurant')
  const [newName, setNewName] = useState('')
  const [newEmail, setNewEmail] = useState('')
  const [newLicenseNo, setNewLicenseNo] = useState('')
  const [newAddress, setNewAddress] = useState('')
  const [newCapacity, setNewCapacity] = useState('')
  const [creationSuccess, setCreationSuccess] = useState('')

  const handleAdminLoginSubmit = (e) => {
    e.preventDefault()
    if (passcode === 'admin123' || passcode === 'admin') {
      loginUser('admin', 'admin@foodflow.org', 'System Administrator')
      setAuthError('')
    } else {
      setAuthError('Access Denied: Invalid Master Admin Passcode.')
    }
  }

  const handleApprove = (id) => {
    approveRegistration(id)
  }

  const handleReject = (id) => {
    rejectRegistration(id)
  }

  const handleAdminCreate = (e) => {
    e.preventDefault()
    if (!newName || !newEmail) return

    createEntityByAdmin({
      type: newType,
      name: newName,
      email: newEmail,
      licenseNo: newLicenseNo || 'ADMIN-PRECREATED',
      address: newAddress || 'Metro Area',
      capacity: newCapacity || (newType === 'restaurant' ? '35 kg/day' : '400 meals/day')
    })

    setCreationSuccess(`Account "${newName}" successfully created & activated!`)
    setNewName('')
    setNewEmail('')
    setNewLicenseNo('')
    setNewAddress('')
    setNewCapacity('')

    setTimeout(() => {
      setCreationSuccess('')
      setActiveTab('active')
    }, 1200)
  }

  const handleLaunchDashboard = () => {
    navigate('/dashboard')
  }

  const filteredPending = pendingRegistrations.filter(r => {
    const matchesType = filterType === 'all' || r.type === filterType
    const matchesSearch = r.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          r.email.toLowerCase().includes(searchQuery.toLowerCase())
    return matchesType && matchesSearch
  })

  const filteredActive = registeredEntities.filter(e => {
    const matchesType = filterType === 'all' || e.type === filterType
    const matchesSearch = e.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                          e.email.toLowerCase().includes(searchQuery.toLowerCase())
    return matchesType && matchesSearch
  })

  // SECURITY GUARD SCREEN (Shown if not logged in as Admin)
  if (!isAdminAuthenticated) {
    return (
      <div className="min-h-screen bg-slate-100 text-slate-900 font-sans selection:bg-cyan-600 selection:text-white flex flex-col justify-between p-6">
        <header className="max-w-7xl mx-auto w-full flex items-center justify-between py-4">
          <Link to="/landing" className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-cyan-700 flex items-center justify-center shadow-md">
              <ShieldCheck className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-black text-slate-900">FoodFlow <span className="text-cyan-700">Admin Security</span></span>
          </Link>
          <Link to="/landing" className="text-xs font-bold text-slate-600 hover:text-slate-900">
            Return to Landing Page
          </Link>
        </header>

        <main className="max-w-md w-full mx-auto my-auto py-8">
          <div className="p-8 rounded-3xl bg-white border border-slate-200 shadow-xl space-y-6">
            <div className="text-center space-y-2">
              <div className="w-14 h-14 rounded-2xl bg-cyan-50 border border-cyan-200 text-cyan-700 flex items-center justify-center mx-auto mb-2">
                <Lock className="w-7 h-7" />
              </div>
              <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Restricted Admin Endpoint</h1>
              <p className="text-xs text-slate-500">
                This endpoint requires System Administrator security passcode verification.
              </p>
            </div>

            <form onSubmit={handleAdminLoginSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1.5">Master Security Passcode</label>
                <div className="relative">
                  <KeyRound className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                  <input
                    type="password"
                    required
                    value={passcode}
                    onChange={(e) => setPasscode(e.target.value)}
                    placeholder="Enter Admin Passcode (e.g. admin123)"
                    className="w-full pl-10 pr-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-cyan-700 focus:bg-white transition-colors"
                  />
                </div>
              </div>

              {authError && (
                <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold flex items-center gap-2">
                  <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
                  <span>{authError}</span>
                </div>
              )}

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-cyan-700 hover:bg-cyan-800 text-white font-extrabold text-xs uppercase tracking-wider shadow-md shadow-cyan-700/20 transition-all flex items-center justify-center gap-2"
              >
                <Lock className="w-4 h-4" />
                <span>Verify & Unlock Admin Endpoint</span>
              </button>

              <div className="pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setPasscode('admin123')
                    setAuthError('')
                  }}
                  className="w-full py-2 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs border border-slate-200 transition-colors"
                >
                  Quick Fill Demo Passcode (`admin123`)
                </button>
              </div>
            </form>
          </div>
        </main>

        <footer className="text-center text-xs text-slate-500 py-4">
          Restricted Endpoint &bull; FoodFlow Administrator Security Portal
        </footer>
      </div>
    )
  }

  // AUTHORIZED ADMIN CONTROL HUB SCREEN
  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 font-sans selection:bg-cyan-600 selection:text-white pb-16">
      {/* Top Navbar */}
      <nav className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-200/90 px-6 py-4 shadow-xs">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Link to="/landing" className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-xl bg-cyan-700 flex items-center justify-center shadow-md">
                <ShieldCheck className="w-5 h-5 text-white" />
              </div>
              <span className="text-xl font-black text-slate-900">FoodFlow <span className="text-cyan-700">Admin Portal</span></span>
            </Link>
            <span className="hidden sm:inline-block text-[10px] font-mono font-bold bg-cyan-50 text-cyan-800 px-2.5 py-1 rounded-full border border-cyan-200">
              Authenticated Admin Access
            </span>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={handleLaunchDashboard}
              className="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs shadow-md shadow-emerald-600/20 flex items-center gap-1.5 transition-all"
            >
              <LayoutDashboard className="w-4 h-4" />
              <span>Launch Dashboard</span>
            </button>
            <Link
              to="/landing"
              className="px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs transition-colors"
            >
              Landing Page
            </Link>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-6 pt-8 space-y-8">
        {/* Banner */}
        <div className="p-8 rounded-3xl bg-white border border-slate-200 shadow-md relative overflow-hidden space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-cyan-50 border border-cyan-200 text-cyan-800 text-xs font-bold">
            <ShieldCheck className="w-3.5 h-3.5 text-cyan-700" />
            <span>Administrator Control Hub</span>
          </div>
          <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
            Organization Verification & Account Management
          </h1>
          <p className="text-slate-600 text-sm max-w-3xl leading-relaxed">
            Review incoming partner applications submitted through the registration portal, or pre-create verified accounts directly into the system directory.
          </p>

          <div className="flex flex-wrap gap-4 pt-2">
            <div className="flex items-center gap-2 bg-slate-50 px-4 py-2 rounded-xl border border-slate-200 text-xs">
              <span className="text-slate-500 font-semibold">Pending Applications:</span>
              <span className="font-extrabold text-amber-700 text-sm">{pendingRegistrations.length}</span>
            </div>
            <div className="flex items-center gap-2 bg-slate-50 px-4 py-2 rounded-xl border border-slate-200 text-xs">
              <span className="text-slate-500 font-semibold">Verified Active Partners:</span>
              <span className="font-extrabold text-emerald-700 text-sm">{registeredEntities.length}</span>
            </div>
          </div>
        </div>

        {/* Tab Selection */}
        <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-200 pb-4">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setActiveTab('pending')}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-xs transition-all ${
                activeTab === 'pending'
                  ? 'bg-amber-600 text-white shadow-md'
                  : 'bg-white text-slate-600 hover:text-slate-900 border border-slate-200'
              }`}
            >
              <UserCheck className="w-4 h-4" />
              <span>Pending Applications ({pendingRegistrations.length})</span>
            </button>

            <button
              onClick={() => setActiveTab('precreate')}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-xs transition-all ${
                activeTab === 'precreate'
                  ? 'bg-cyan-700 text-white shadow-md'
                  : 'bg-white text-slate-600 hover:text-slate-900 border border-slate-200'
              }`}
            >
              <PlusCircle className="w-4 h-4" />
              <span>Pre-Create Account</span>
            </button>

            <button
              onClick={() => setActiveTab('active')}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl font-bold text-xs transition-all ${
                activeTab === 'active'
                  ? 'bg-emerald-600 text-white shadow-md'
                  : 'bg-white text-slate-600 hover:text-slate-900 border border-slate-200'
              }`}
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Verified Directory ({registeredEntities.length})</span>
            </button>
          </div>

          {/* Search & Type Filter */}
          <div className="flex items-center gap-3">
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                placeholder="Search..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9 pr-4 py-2 rounded-xl bg-white border border-slate-200 text-xs text-slate-900 placeholder-slate-400 focus:outline-none focus:border-cyan-700 w-48 sm:w-64"
              />
            </div>

            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="px-3 py-2 rounded-xl bg-white border border-slate-200 text-xs font-semibold text-slate-700 focus:outline-none focus:border-cyan-700"
            >
              <option value="all">All Types</option>
              <option value="restaurant">Restaurants Only</option>
              <option value="ngo">NGOs Only</option>
            </select>
          </div>
        </div>

        {/* TAB 1: PENDING APPROVALS QUEUE */}
        {activeTab === 'pending' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <UserCheck className="w-5 h-5 text-amber-600" />
                <span>Pending Applications Queue</span>
              </h2>
              <span className="text-xs text-slate-500">Requires Admin Verification Action</span>
            </div>

            {filteredPending.length === 0 ? (
              <div className="p-12 text-center rounded-3xl bg-white border border-slate-200 shadow-sm space-y-3">
                <CheckCircle2 className="w-12 h-12 text-emerald-600 mx-auto" />
                <h3 className="text-base font-bold text-slate-900">No Applications Pending</h3>
                <p className="text-xs text-slate-500 max-w-sm mx-auto">
                  All submitted applications have been reviewed. You can pre-create new partner accounts or test registering a new NGO/Restaurant from the Register Page.
                </p>
                <Link
                  to="/register"
                  className="inline-block mt-2 px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-emerald-700 font-bold text-xs border border-emerald-300"
                >
                  Go to Register Page
                </Link>
              </div>
            ) : (
              <div className="grid gap-4">
                {filteredPending.map((item) => (
                  <div
                    key={item.id}
                    className="p-6 rounded-2xl bg-white border border-slate-200 shadow-sm flex flex-col lg:flex-row items-start lg:items-center justify-between gap-6"
                  >
                    <div className="space-y-3 flex-1">
                      <div className="flex items-center gap-3">
                        <span className={`p-2 rounded-xl font-bold text-xs flex items-center gap-1.5 ${
                          item.type === 'restaurant'
                            ? 'bg-amber-50 text-amber-800 border border-amber-200'
                            : 'bg-emerald-50 text-emerald-800 border border-emerald-200'
                        }`}>
                          {item.type === 'restaurant' ? <Building2 className="w-4 h-4" /> : <HeartHandshake className="w-4 h-4" />}
                          <span className="uppercase">{item.type}</span>
                        </span>
                        <h3 className="text-xl font-bold text-slate-900">{item.name}</h3>
                        <span className="text-[10px] text-slate-500 font-mono bg-slate-100 px-2 py-0.5 rounded border border-slate-200">
                          {item.id}
                        </span>
                      </div>

                      <div className="grid sm:grid-cols-3 gap-3 text-xs text-slate-700 pt-1">
                        <div>
                          <span className="text-slate-400 block">Contact:</span>
                          <span className="font-semibold text-slate-900">{item.contactName}</span> ({item.email})
                        </div>
                        <div>
                          <span className="text-slate-400 block">License / Reg ID:</span>
                          <span className="font-mono text-cyan-800 font-semibold">{item.licenseNo || item.regId}</span>
                        </div>
                        <div>
                          <span className="text-slate-400 block">Location:</span>
                          <span className="font-semibold">{item.address}</span>
                        </div>
                      </div>

                      <div className="flex items-center gap-4 text-xs text-slate-500 pt-1">
                        <span>Phone: <strong className="text-slate-800">{item.phone}</strong></span>
                        <span>&bull;</span>
                        <span>{item.type === 'restaurant' ? 'Est Surplus: ' : 'Meal Capacity: '}
                          <strong className="text-emerald-700">{item.surplusVolumeKg || item.servingCapacity}</strong>
                        </span>
                        <span>&bull;</span>
                        <span className="text-slate-400">{item.submittedAt}</span>
                      </div>
                    </div>

                    <div className="flex items-center gap-3 shrink-0 w-full lg:w-auto border-t lg:border-t-0 pt-4 lg:pt-0 border-slate-100">
                      <button
                        onClick={() => handleApprove(item.id)}
                        className="flex-1 lg:flex-initial px-5 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-xs shadow-md shadow-emerald-600/20 flex items-center justify-center gap-1.5 transition-all"
                      >
                        <Check className="w-4 h-4" />
                        <span>Approve Account</span>
                      </button>

                      <button
                        onClick={() => handleReject(item.id)}
                        className="px-4 py-3 rounded-xl bg-rose-50 hover:bg-rose-100 border border-rose-200 text-rose-700 font-bold text-xs flex items-center justify-center gap-1.5 transition-colors"
                      >
                        <XCircle className="w-4 h-4" />
                        <span>Reject</span>
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: ADMIN PRE-CREATE FORM */}
        {activeTab === 'precreate' && (
          <div className="max-w-2xl mx-auto p-8 rounded-3xl bg-white border border-slate-200 shadow-md space-y-6">
            <div className="space-y-2">
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-cyan-50 border border-cyan-200 text-cyan-800 text-xs font-bold">
                <PlusCircle className="w-3.5 h-3.5" />
                <span>Admin Direct Account Setup</span>
              </div>
              <h2 className="text-2xl font-extrabold text-slate-900">Pre-Create Verified Account</h2>
              <p className="text-xs text-slate-500">
                Directly register and activate a verified partner account in the platform database.
              </p>
            </div>

            {creationSuccess && (
              <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs font-semibold flex items-center gap-2">
                <CheckCircle2 className="w-5 h-5 shrink-0 text-emerald-600" />
                <span>{creationSuccess}</span>
              </div>
            )}

            <form onSubmit={handleAdminCreate} className="space-y-4">
              <div className="grid grid-cols-2 gap-3 p-1.5 rounded-2xl bg-slate-100 border border-slate-200">
                <button
                  type="button"
                  onClick={() => setNewType('restaurant')}
                  className={`flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold transition-all ${
                    newType === 'restaurant'
                      ? 'bg-amber-600 text-white shadow-xs'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <Building2 className="w-4 h-4" />
                  <span>Restaurant Donor</span>
                </button>
                <button
                  type="button"
                  onClick={() => setNewType('ngo')}
                  className={`flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold transition-all ${
                    newType === 'ngo'
                      ? 'bg-emerald-600 text-white shadow-xs'
                      : 'text-slate-600 hover:text-slate-900'
                  }`}
                >
                  <HeartHandshake className="w-4 h-4" />
                  <span>NGO Recipient</span>
                </button>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1.5">
                  {newType === 'restaurant' ? 'Restaurant Name' : 'NGO Name'} *
                </label>
                <input
                  type="text"
                  required
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder={newType === 'restaurant' ? 'e.g. Sapphire Banquet Hall' : 'e.g. City Community Kitchen'}
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-cyan-700 focus:bg-white"
                />
              </div>

              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Official Email *</label>
                  <input
                    type="email"
                    required
                    value={newEmail}
                    onChange={(e) => setNewEmail(e.target.value)}
                    placeholder="partner@foodflow.org"
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-cyan-700 focus:bg-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">
                    {newType === 'restaurant' ? 'FSSAI License ID' : 'Registration ID'}
                  </label>
                  <input
                    type="text"
                    value={newLicenseNo}
                    onChange={(e) => setNewLicenseNo(e.target.value)}
                    placeholder={newType === 'restaurant' ? 'FSSAI-883920101' : 'NGO-80G-33129'}
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-cyan-700 focus:bg-white"
                  />
                </div>
              </div>

              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Address / City</label>
                  <input
                    type="text"
                    value={newAddress}
                    onChange={(e) => setNewAddress(e.target.value)}
                    placeholder="City / Region"
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-cyan-700 focus:bg-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">
                    {newType === 'restaurant' ? 'Est Surplus (kg)' : 'Meal Capacity'}
                  </label>
                  <input
                    type="text"
                    value={newCapacity}
                    onChange={(e) => setNewCapacity(e.target.value)}
                    placeholder={newType === 'restaurant' ? '35 kg/day' : '500 meals/day'}
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-cyan-700 focus:bg-white"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="w-full py-3.5 rounded-xl bg-cyan-700 hover:bg-cyan-800 text-white font-extrabold text-xs uppercase tracking-wider shadow-md shadow-cyan-700/20 transition-all flex items-center justify-center gap-2"
              >
                <PlusCircle className="w-4 h-4" />
                <span>Pre-Create & Activate Account</span>
              </button>
            </form>
          </div>
        )}

        {/* TAB 3: VERIFIED DIRECTORY */}
        {activeTab === 'active' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-bold text-slate-900 flex items-center gap-2">
                <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                <span>Verified System Entities</span>
              </h2>
              <span className="text-xs text-slate-500">{filteredActive.length} Verified Accounts</span>
            </div>

            <div className="grid md:grid-cols-2 gap-4">
              {filteredActive.map((entity) => (
                <div
                  key={entity.id}
                  className="p-6 rounded-2xl bg-white border border-slate-200 shadow-sm flex items-start justify-between gap-4"
                >
                  <div className="space-y-2">
                    <div className="flex items-center gap-2">
                      <span className={`p-1.5 rounded-lg text-xs font-bold ${
                        entity.type === 'restaurant'
                          ? 'bg-amber-50 text-amber-800 border border-amber-200'
                          : 'bg-emerald-50 text-emerald-800 border border-emerald-200'
                      }`}>
                        {entity.type === 'restaurant' ? <Building2 className="w-3.5 h-3.5" /> : <HeartHandshake className="w-3.5 h-3.5" />}
                      </span>
                      <h3 className="text-lg font-bold text-slate-900">{entity.name}</h3>
                      <span className="text-[10px] font-mono text-emerald-800 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200 font-bold">
                        VERIFIED
                      </span>
                    </div>

                    <p className="text-xs text-slate-500 font-mono">{entity.email} &bull; {entity.licenseNo}</p>
                    <p className="text-xs text-slate-700">{entity.address}</p>

                    <div className="flex items-center gap-3 pt-2">
                      <span className="text-xs font-bold text-emerald-700 flex items-center gap-1">
                        <Award className="w-4 h-4 text-emerald-600" />
                        <span>Trust Score: {entity.trustScore}%</span>
                      </span>
                    </div>
                  </div>

                  <button
                    onClick={() => {
                      loginUser(entity.type, entity.email, entity.name)
                      navigate('/dashboard')
                    }}
                    className="px-3.5 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs transition-colors shrink-0"
                  >
                    View Portal
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
