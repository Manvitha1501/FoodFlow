import React, { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import {
  Building2,
  HeartHandshake,
  ShieldCheck,
  Lock,
  Mail,
  ArrowRight,
  Utensils,
  CheckCircle2,
  AlertCircle,
  Sparkles
} from 'lucide-react'
import { useStore } from '../store/useStore'
import { api } from '../services/api'

export default function LoginPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const defaultRole = searchParams.get('role') || 'restaurant'

  const [role, setRole] = useState(defaultRole)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')

  const { loginUser, pendingRegistrations, registeredEntities = [] } = useStore()

  const roles = [
    { id: 'restaurant', label: 'Restaurant Donor', icon: Building2 },
    { id: 'ngo', label: 'NGO Recipient', icon: HeartHandshake },
    { id: 'admin', label: 'Admin Security', icon: ShieldCheck }
  ]

  const handleQuickFill = (targetRole) => {
    setRole(targetRole)
    setErrorMsg('')
    if (targetRole === 'restaurant') {
      setEmail('grandhorizon@foodflow.org')
      setPassword('DonorPass#2026')
    } else if (targetRole === 'ngo') {
      setEmail('carefood@ngo.org')
      setPassword('NgoPass#2026')
    } else {
      setEmail('admin@foodflow.org')
      setPassword('admin123')
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsSubmitting(true)
    setErrorMsg('')

    // 1. Email format validation
    if (!/\S+@\S+\.\S+/.test(email)) {
      setIsSubmitting(false)
      setErrorMsg('Please enter a valid email address (e.g. name@domain.com).')
      return
    }

    // 2. Check pending Admin verification
    const isPending = pendingRegistrations.some(r => r.email.toLowerCase() === email.trim().toLowerCase())
    if (isPending && role !== 'admin') {
      setIsSubmitting(false)
      setErrorMsg('Your account application is pending Admin verification. Please wait for an administrator to approve your account.')
      return
    }

    try {
      // Call Live Backend REST API for JWT Login
      const data = await api.login({ email, password })
      const userRole = data.role ? data.role.toLowerCase().replace('role_', '') : role
      const userName = data.user?.fullName || data.user?.name || (userRole === 'restaurant' ? 'Grand Horizon Hotel' : 'Care Food Network')

      loginUser(userRole, email, userName)

      if (userRole === 'admin') {
        navigate('/admin')
      } else {
        navigate('/dashboard')
      }
    } catch (err) {
      console.warn('Backend login auth check failed:', err)

      // If backend explicitly responded with an HTTP status code (e.g. 401 Unauthorized)
      if (err.response) {
        setErrorMsg(err.response.data?.message || 'Invalid email or password. Please check your credentials.')
        return
      }

      // Offline mode fallback ONLY if backend server is completely unreachable (Network Error)
      const knownAuths = [
        { email: 'admin@foodflow.org', pass: 'admin123', role: 'admin', name: 'System Administrator' },
        { email: 'grandhorizon@foodflow.org', pass: 'DonorPass#2026', role: 'restaurant', name: 'Grand Horizon Hotel' },
        { email: 'carefood@ngo.org', pass: 'NgoPass#2026', role: 'ngo', name: 'Care Food Network' }
      ]

      const matchedKnown = knownAuths.find(k => k.email.toLowerCase() === email.trim().toLowerCase())
      if (matchedKnown) {
        if (password !== matchedKnown.pass) {
          setErrorMsg('Invalid password. Please enter the correct password for this account.')
          return
        }
        loginUser(matchedKnown.role, email, matchedKnown.name)
        navigate(matchedKnown.role === 'admin' ? '/admin' : '/dashboard')
        return
      }

      const matchedRegistered = registeredEntities.find(r => r.email.toLowerCase() === email.trim().toLowerCase())
      if (matchedRegistered) {
        const storedPass = matchedRegistered.password || matchedRegistered.pass
        if (storedPass && password !== storedPass) {
          setErrorMsg('Invalid password. Please enter the correct password for your account.')
          return
        }
        if (!storedPass) {
          setErrorMsg('Invalid credentials. Password verification failed.')
          return
        }
        const partnerName = matchedRegistered.name || matchedRegistered.contactName || 'Partner User'
        loginUser(matchedRegistered.type || role, email, partnerName)
        navigate('/dashboard')
        return
      }

      setErrorMsg('Invalid email or password. Please check your credentials.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-emerald-50/50 via-white to-slate-100 text-slate-900 flex flex-col justify-between p-6 font-sans selection:bg-emerald-500 selection:text-white">
      {/* Top Header */}
      <header className="max-w-7xl mx-auto w-full flex items-center justify-between z-10 py-4">
        <Link to="/landing" className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center shadow-md shadow-emerald-500/20">
            <Utensils className="w-5 h-5 text-white" />
          </div>
          <span className="text-xl font-black text-slate-900">Food<span className="text-emerald-600">Flow</span></span>
        </Link>

        <Link
          to="/register"
          className="text-xs font-bold text-slate-600 hover:text-emerald-600 transition-colors flex items-center gap-1.5"
        >
          <span>Need an account? Register</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>
      </header>

      {/* Main Login Box */}
      <main className="max-w-md w-full mx-auto my-auto z-10 py-8">
        <div className="p-8 rounded-3xl bg-white border border-slate-200 shadow-xl space-y-6">
          <div className="text-center space-y-2">
            <div className="inline-flex items-center gap-1.5 px-3.5 py-1 rounded-full bg-emerald-100/80 border border-emerald-200 text-emerald-800 text-xs font-bold">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Platform Portal Access</span>
            </div>
            <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight">Sign In to FoodFlow</h1>
            <p className="text-xs text-slate-500">Select your account role to proceed</p>
          </div>

          {/* Role Tabs */}
          <div className="grid grid-cols-3 gap-2 p-1.5 rounded-2xl bg-slate-100 border border-slate-200">
            {roles.map((r) => {
              const Icon = r.icon
              const isSelected = role === r.id
              return (
                <button
                  key={r.id}
                  type="button"
                  onClick={() => { setRole(r.id); setErrorMsg(''); }}
                  className={`flex flex-col items-center justify-center p-2.5 rounded-xl text-xs font-bold transition-all ${
                    isSelected
                      ? r.id === 'admin'
                        ? 'bg-cyan-700 text-white shadow-md'
                        : 'bg-emerald-600 text-white shadow-md'
                      : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/60'
                  }`}
                >
                  <Icon className="w-4 h-4 mb-1" />
                  <span className="text-[11px] leading-none">{r.id === 'restaurant' ? 'Donor' : r.id === 'ngo' ? 'NGO' : 'Admin'}</span>
                </button>
              )
            })}
          </div>

          {/* Quick Demo Fill Buttons */}
          <div className="space-y-1.5">
            <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Quick Fill Test Credentials:</p>
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => handleQuickFill('restaurant')}
                className="flex-1 py-1.5 px-2 rounded-lg bg-amber-50 hover:bg-amber-100 text-[11px] font-semibold text-amber-800 border border-amber-200"
              >
                Donor Demo
              </button>
              <button
                type="button"
                onClick={() => handleQuickFill('ngo')}
                className="flex-1 py-1.5 px-2 rounded-lg bg-emerald-50 hover:bg-emerald-100 text-[11px] font-semibold text-emerald-800 border border-emerald-200"
              >
                NGO Demo
              </button>
              <button
                type="button"
                onClick={() => handleQuickFill('admin')}
                className="flex-1 py-1.5 px-2 rounded-lg bg-cyan-50 hover:bg-cyan-100 text-[11px] font-semibold text-cyan-800 border border-cyan-200"
              >
                Admin Security
              </button>
            </div>
          </div>

          {/* Login Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">
                {role === 'admin' ? 'Admin Master Email' : 'Account Email Address'}
              </label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder={role === 'admin' ? 'admin@foodflow.org' : role === 'ngo' ? 'ngo@foodflow.org' : 'donor@restaurant.com'}
                  className="w-full pl-10 pr-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1.5">
                {role === 'admin' ? 'Admin Master Passcode' : 'Password'}
              </label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                />
              </div>
              {role !== 'admin' ? (
                <p className="text-[10px] text-slate-500 mt-1">
                  * Password must be 8+ chars, with 1 uppercase, 1 number & 1 special char.
                </p>
              ) : (
                <p className="text-[10px] text-slate-500 mt-1">
                  * Master Passcode is required to access Admin Security controls.
                </p>
              )}
            </div>

            {errorMsg && (
              <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-semibold flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
                <span>{errorMsg}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
              className={`w-full py-3.5 rounded-xl font-extrabold text-xs uppercase tracking-wider text-white shadow-md transition-all flex items-center justify-center gap-2 disabled:opacity-50 ${
                role === 'admin'
                  ? 'bg-cyan-700 hover:bg-cyan-800 shadow-cyan-700/20'
                  : 'bg-emerald-600 hover:bg-emerald-700 shadow-emerald-600/20'
              }`}
            >
              {isSubmitting ? (
                <span>Authenticating...</span>
              ) : (
                <>
                  <span>Sign In as {role.toUpperCase()}</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          <div className="pt-2 text-center border-t border-slate-100">
            <p className="text-xs text-slate-500">
              Back to public site?{' '}
              <Link to="/landing" className="text-emerald-700 hover:underline font-bold">
                Return to Landing Page
              </Link>
            </p>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="text-center text-xs text-slate-500 z-10 py-4">
        FoodFlow AI Platform &copy; 2026. Secure Food Redistribution Portal.
      </footer>
    </div>
  )
}
