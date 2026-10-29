import React, { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import {
  Building2,
  HeartHandshake,
  ShieldCheck,
  Utensils,
  ArrowRight,
  CheckCircle2,
  XCircle,
  Sparkles,
  Info,
  Lock,
  Eye,
  EyeOff,
  AlertCircle
} from 'lucide-react'
import { useStore } from '../store/useStore'
import { api } from '../services/api'

export default function RegisterPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const initialType = searchParams.get('type') || 'restaurant'

  const [type, setType] = useState(initialType)
  const [submitted, setSubmitted] = useState(false)
  const [submittedItem, setSubmittedItem] = useState(null)
  const [errorAlert, setErrorAlert] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const { registerEntity, loginUser, registeredEntities } = useStore()

  // Form fields
  const [name, setName] = useState('')
  const [contactName, setContactName] = useState('')
  const [email, setEmail] = useState('')
  const [phone, setPhone] = useState('')
  const [address, setAddress] = useState('')

  // Password state
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  // Restaurant specific
  const [licenseNo, setLicenseNo] = useState('')
  const [surplusVolumeKg, setSurplusVolumeKg] = useState('20-40 kg/day')
  const [foodType, setFoodType] = useState('Prepared Meals & Buffet Surplus')

  // NGO specific
  const [regId, setRegId] = useState('')
  const [servingCapacity, setServingCapacity] = useState('100-300 meals/day')
  const [beneficiaries, setBeneficiaries] = useState('Shelters & Children Homes')

  // Live password validation criteria
  const passwordRules = {
    minLength: password.length >= 8,
    hasUppercase: /[A-Z]/.test(password),
    hasNumber: /[0-9]/.test(password),
    hasSpecial: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password),
    isMatched: password === confirmPassword && confirmPassword !== ''
  }

  const isPasswordValid =
    passwordRules.minLength &&
    passwordRules.hasUppercase &&
    passwordRules.hasNumber &&
    passwordRules.hasSpecial &&
    passwordRules.isMatched

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErrorAlert('')

    // 0. Duplicate email & phone check in local session
    if (registeredEntities.some(e => e.email && e.email.toLowerCase() === email.trim().toLowerCase())) {
      setErrorAlert(`An account with email '${email}' is already registered. Please sign in instead.`)
      return
    }

    const digitsOnly = phone.replace(/\D/g, '')
    if (registeredEntities.some(e => e.phone && e.phone.replace(/\D/g, '') === digitsOnly)) {
      setErrorAlert(`An account with phone number '${phone}' is already registered. Please enter a unique phone number.`)
      return
    }

    // 1. Email format check
    if (!/\S+@\S+\.\S+/.test(email)) {
      setErrorAlert('Please provide a valid email address (e.g. contact@domain.com).')
      return
    }

    // 2. Phone validation
    if (digitsOnly.length < 10) {
      setErrorAlert('Phone number must contain at least 10 digits.')
      return
    }

    // 3. License / Reg ID validation
    const targetId = type === 'restaurant' ? licenseNo : regId
    if (!targetId || targetId.trim().length < 5) {
      setErrorAlert(`Please enter a valid ${type === 'restaurant' ? 'FSSAI / Food License Number' : 'NGO Registration ID'} (min 5 characters).`)
      return
    }

    // 4. Password validation check
    if (!isPasswordValid) {
      if (!passwordRules.minLength) {
        setErrorAlert('Password must be at least 8 characters long.')
      } else if (!passwordRules.hasUppercase) {
        setErrorAlert('Password must contain at least one capital letter (A-Z).')
      } else if (!passwordRules.hasNumber) {
        setErrorAlert('Password must contain at least one number (0-9).')
      } else if (!passwordRules.hasSpecial) {
        setErrorAlert('Password must contain at least one special character (!@#$%...).')
      } else if (!passwordRules.isMatched) {
        setErrorAlert('Passwords do not match. Please re-enter your password.')
      }
      return
    }

    const registrationData = {
      type,
      name,
      contactName,
      email,
      phone,
      address,
      password,
      licenseNo: type === 'restaurant' ? licenseNo : undefined,
      surplusVolumeKg: type === 'restaurant' ? surplusVolumeKg : undefined,
      foodType: type === 'restaurant' ? foodType : undefined,
      regId: type === 'ngo' ? regId : undefined,
      servingCapacity: type === 'ngo' ? servingCapacity : undefined,
      beneficiaries: type === 'ngo' ? beneficiaries : undefined
    }

    setIsSubmitting(true)
    try {
      // Call Live Backend REST API to save document in MongoDB
      await api.register(registrationData)
      registerEntity(registrationData)
      setSubmittedItem(registrationData)
      setSubmitted(true)
    } catch (err) {
      console.warn('Backend registration response note:', err)
      const serverMsg = err.response?.data?.message || err.response?.data?.error
      const status = err.response?.status

      if (serverMsg || status === 409 || status === 400) {
        setErrorAlert(serverMsg || `An account with email '${email}' is already registered. Please Sign In.`)
        return
      }

      // Fallback local save only if server unreachable and email not duplicated
      registerEntity(registrationData)
      setSubmittedItem(registrationData)
      setSubmitted(true)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-emerald-50/50 via-white to-slate-100 text-slate-900 p-6 font-sans selection:bg-emerald-500 selection:text-white flex flex-col justify-between">
      {/* Top Header */}
      <header className="max-w-7xl mx-auto w-full flex items-center justify-between z-10 py-4">
        <Link to="/landing" className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center shadow-md shadow-emerald-500/20">
            <Utensils className="w-5 h-5 text-white" />
          </div>
          <span className="text-xl font-black text-slate-900">Food<span className="text-emerald-600">Flow</span></span>
        </Link>

        <div className="flex items-center gap-4 text-xs font-bold">
          <Link to="/login" className="text-slate-600 hover:text-emerald-600 transition-colors">
            Already registered? Sign In
          </Link>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-2xl w-full mx-auto my-auto z-10 py-8">
        {!submitted ? (
          <div className="p-8 rounded-3xl bg-white border border-slate-200 shadow-xl space-y-6">
            <div className="text-center space-y-2">
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-100/80 border border-emerald-200 text-emerald-800 text-xs font-bold">
                <Sparkles className="w-3.5 h-3.5" />
                <span>Partner Onboarding Portal</span>
              </div>
              <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
                Register Your Organization
              </h1>
              <p className="text-xs text-slate-500 max-w-lg mx-auto">
                Submit your details for account activation. Applications are validated and queued for Admin verification.
              </p>
            </div>

            {/* Type Switcher */}
            <div className="grid grid-cols-2 gap-3 p-1.5 rounded-2xl bg-slate-100 border border-slate-200">
              <button
                type="button"
                onClick={() => setType('restaurant')}
                className={`flex items-center justify-center gap-2 py-3 px-4 rounded-xl text-xs font-bold transition-all ${
                  type === 'restaurant'
                    ? 'bg-amber-600 text-white shadow-md'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <Building2 className="w-4 h-4" />
                <span>Restaurant / Donor</span>
              </button>

              <button
                type="button"
                onClick={() => setType('ngo')}
                className={`flex items-center justify-center gap-2 py-3 px-4 rounded-xl text-xs font-bold transition-all ${
                  type === 'ngo'
                    ? 'bg-emerald-600 text-white shadow-md'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                <HeartHandshake className="w-4 h-4" />
                <span>NGO / Recipient</span>
              </button>
            </div>

            {errorAlert && (
              <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-semibold flex items-center gap-2.5">
                <AlertCircle className="w-4 h-4 shrink-0 text-rose-600" />
                <span>{errorAlert}</span>
              </div>
            )}

            {/* Registration Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">
                    {type === 'restaurant' ? 'Restaurant / Business Name' : 'NGO / Non-Profit Name'} *
                  </label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder={type === 'restaurant' ? 'e.g. Royal Pavilion Restaurant' : 'e.g. Hope Alliance Shelter'}
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">
                    {type === 'restaurant' ? 'FSSAI / Food License No.' : 'NGO Reg / FCRA / 80G ID'} *
                  </label>
                  <input
                    type="text"
                    required
                    value={type === 'restaurant' ? licenseNo : regId}
                    onChange={(e) => type === 'restaurant' ? setLicenseNo(e.target.value) : setRegId(e.target.value)}
                    placeholder={type === 'restaurant' ? 'FSSAI-1129983746' : 'NGO-80G-77412'}
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                  />
                </div>
              </div>

              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Contact Person Name *</label>
                  <input
                    type="text"
                    required
                    value={contactName}
                    onChange={(e) => setContactName(e.target.value)}
                    placeholder="e.g. Sarah Jenkins"
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Official Email *</label>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="contact@organization.org"
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                  />
                </div>
              </div>

              <div className="grid sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Phone Number *</label>
                  <input
                    type="tel"
                    required
                    value={phone}
                    onChange={(e) => setPhone(e.target.value)}
                    placeholder="+91 98765 43210"
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">
                    {type === 'restaurant' ? 'Estimated Daily Surplus (kg)' : 'Daily Meal Capacity'}
                  </label>
                  <input
                    type="text"
                    value={type === 'restaurant' ? surplusVolumeKg : servingCapacity}
                    onChange={(e) => type === 'restaurant' ? setSurplusVolumeKg(e.target.value) : setServingCapacity(e.target.value)}
                    placeholder={type === 'restaurant' ? 'e.g. 25-50 kg' : 'e.g. 500 meals'}
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1.5">Physical Address / City *</label>
                <input
                  type="text"
                  required
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="Street address, District, City, Pincode"
                  className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 placeholder-slate-400 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                />
              </div>

              {/* Password Fields with Real-Time Validation Requirements */}
              <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200 space-y-3">
                <div className="flex items-center justify-between">
                  <label className="text-xs font-bold text-slate-900">Create Account Password *</label>
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="text-[11px] font-bold text-emerald-700 hover:underline flex items-center gap-1"
                  >
                    {showPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                    <span>{showPassword ? 'Hide' : 'Show'} Password</span>
                  </button>
                </div>

                <div className="grid sm:grid-cols-2 gap-3">
                  <div>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      required
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="Password"
                      className="w-full px-3.5 py-2.5 rounded-xl bg-white border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-emerald-600"
                    />
                  </div>

                  <div>
                    <input
                      type={showPassword ? 'text' : 'password'}
                      required
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      placeholder="Confirm Password"
                      className="w-full px-3.5 py-2.5 rounded-xl bg-white border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-emerald-600"
                    />
                  </div>
                </div>

                {/* Live Password Checklist Requirements */}
                <div className="pt-2 grid grid-cols-2 gap-2 text-[11px] font-semibold border-t border-slate-200">
                  <div className={`flex items-center gap-1.5 ${passwordRules.minLength ? 'text-emerald-700 font-bold' : 'text-slate-400'}`}>
                    {passwordRules.minLength ? <CheckCircle2 className="w-3.5 h-3.5 shrink-0 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 shrink-0" />}
                    <span>At least 8 characters</span>
                  </div>

                  <div className={`flex items-center gap-1.5 ${passwordRules.hasUppercase ? 'text-emerald-700 font-bold' : 'text-slate-400'}`}>
                    {passwordRules.hasUppercase ? <CheckCircle2 className="w-3.5 h-3.5 shrink-0 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 shrink-0" />}
                    <span>1 Capital letter (A-Z)</span>
                  </div>

                  <div className={`flex items-center gap-1.5 ${passwordRules.hasNumber ? 'text-emerald-700 font-bold' : 'text-slate-400'}`}>
                    {passwordRules.hasNumber ? <CheckCircle2 className="w-3.5 h-3.5 shrink-0 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 shrink-0" />}
                    <span>1 Number (0-9)</span>
                  </div>

                  <div className={`flex items-center gap-1.5 ${passwordRules.hasSpecial ? 'text-emerald-700 font-bold' : 'text-slate-400'}`}>
                    {passwordRules.hasSpecial ? <CheckCircle2 className="w-3.5 h-3.5 shrink-0 text-emerald-600" /> : <XCircle className="w-3.5 h-3.5 shrink-0" />}
                    <span>1 Special char (!@#$)</span>
                  </div>
                </div>
              </div>

              {type === 'restaurant' ? (
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Food Surplus Category</label>
                  <select
                    value={foodType}
                    onChange={(e) => setFoodType(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                  >
                    <option>Prepared Meals & Buffet Surplus</option>
                    <option>Bakery & Pastry Products</option>
                    <option>Fresh Fruits & Vegetables</option>
                    <option>Packaged / Dry Pantry Items</option>
                  </select>
                </div>
              ) : (
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1.5">Beneficiary Focus Areas</label>
                  <select
                    value={beneficiaries}
                    onChange={(e) => setBeneficiaries(e.target.value)}
                    className="w-full px-4 py-3 rounded-xl bg-slate-50 border border-slate-200 text-slate-900 text-xs font-medium focus:outline-none focus:border-emerald-600 focus:bg-white transition-colors"
                  >
                    <option>Shelters & Community Kitchens</option>
                    <option>Children & Orphanages</option>
                    <option>Elderly Care Homes</option>
                    <option>Disaster Relief & Street Outreach</option>
                  </select>
                </div>
              )}

              <div className="p-3 rounded-xl bg-slate-50 border border-slate-200 text-xs text-slate-600 flex items-start gap-2.5">
                <Info className="w-4 h-4 text-emerald-600 shrink-0 mt-0.5" />
                <p className="leading-relaxed">
                  Upon submission, your application will be routed for administrative verification.
                </p>
              </div>

              <button
                type="submit"
                disabled={password.length > 0 && !isPasswordValid}
                className="w-full py-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-xs uppercase tracking-wider shadow-md shadow-emerald-600/20 transition-all flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span>Submit {type.toUpperCase()} Registration</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </form>
          </div>
        ) : (
          /* Submission Success State */
          <div className="p-8 rounded-3xl bg-white border border-emerald-200 shadow-xl text-center space-y-6">
            <div className="w-16 h-16 rounded-3xl bg-emerald-100 text-emerald-600 border border-emerald-300 flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-8 h-8" />
            </div>

            <div className="space-y-2">
              <h2 className="text-2xl font-extrabold text-slate-900">Registration Submitted!</h2>
              <p className="text-xs text-slate-600 max-w-md mx-auto">
                Thank you for registering <span className="text-emerald-700 font-bold">{submittedItem?.name}</span>. Your application has been received and queued for Admin verification.
              </p>
            </div>

            <div className="p-4 rounded-2xl bg-slate-50 border border-slate-200 text-left text-xs space-y-2">
              <div className="flex justify-between border-b border-slate-200 pb-2">
                <span className="text-slate-500">Account Type:</span>
                <span className="font-bold text-slate-900 uppercase">{submittedItem?.type}</span>
              </div>
              <div className="flex justify-between border-b border-slate-200 pb-2">
                <span className="text-slate-500">Contact Email:</span>
                <span className="font-bold text-emerald-700">{submittedItem?.email}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Verification Status:</span>
                <span className="font-bold text-amber-700 flex items-center gap-1">
                  <Lock className="w-3 h-3" />
                  <span>Pending Admin Verification</span>
                </span>
              </div>
            </div>

            <div className="flex flex-col sm:flex-row gap-3 pt-2">
              <Link
                to="/login"
                className="flex-1 py-3.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs text-center shadow-md shadow-emerald-600/20 transition-all"
              >
                Proceed to Sign In
              </Link>
              <Link
                to="/landing"
                className="flex-1 py-3.5 px-4 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 font-bold text-xs text-center border border-slate-200 transition-all"
              >
                Return to Landing Page
              </Link>
            </div>
          </div>
        )}
      </main>

      {/* Footer */}
      <footer className="text-center text-xs text-slate-500 z-10 py-4">
        FoodFlow Zero Waste Verification &bull; All Registrations Verified by Admin
      </footer>
    </div>
  )
}
