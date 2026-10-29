import React from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Utensils,
  HeartHandshake,
  ShieldCheck,
  Zap,
  Sparkles,
  Award,
  ArrowRight,
  CheckCircle2,
  Building2,
  Lock,
  BarChart3,
  Bot,
  Clock,
  Shield,
  Leaf,
  Globe,
  TrendingUp,
  Truck,
  Check,
  FileCheck
} from 'lucide-react'
import { useStore } from '../store/useStore'

export default function LandingPage() {
  const navigate = useNavigate()
  const { loginUser } = useStore()

  const handleQuickDemo = (role) => {
    loginUser(role)
    if (role === 'admin') {
      navigate('/admin')
    } else {
      navigate('/dashboard')
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 font-sans selection:bg-emerald-500 selection:text-white scroll-smooth">
      {/* Top Navbar */}
      <nav className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-200/80 shadow-xs">
        <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
          <Link to="/landing" className="flex items-center gap-3 group">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center shadow-md shadow-emerald-500/20 group-hover:scale-105 transition-transform">
              <Utensils className="w-5 h-5 text-white" />
            </div>
            <div>
              <span className="text-xl font-black tracking-tight text-slate-900">Food<span className="text-emerald-600">Flow</span></span>
            </div>
          </Link>

          <div className="hidden md:flex items-center gap-8 text-sm font-bold text-slate-600">
            <a href="#features" className="hover:text-emerald-600 transition-colors">Features</a>
            <a href="#how-it-works" className="hover:text-emerald-600 transition-colors">How it Works</a>
            <a href="#portals" className="hover:text-emerald-600 transition-colors">Registration Portals</a>
            <a href="#impact" className="hover:text-emerald-600 transition-colors">ESG Impact</a>
          </div>

          <div className="flex items-center gap-3">
            <Link
              to="/login"
              className="px-4 py-2 text-xs sm:text-sm font-bold text-slate-700 hover:text-emerald-600 transition-colors"
            >
              Sign In
            </Link>
            <Link
              to="/register"
              className="px-5 py-2.5 text-xs sm:text-sm font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-md shadow-emerald-600/20 transition-all hover:scale-[1.02]"
            >
              Register NGO / Restaurant
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative pt-12 pb-20 md:pt-20 md:pb-32 overflow-hidden">
        <div className="max-w-7xl mx-auto px-6 text-center space-y-8 relative z-10">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-emerald-50 text-emerald-800 border border-emerald-200 text-xs font-bold shadow-xs">
            <Sparkles className="w-4 h-4 text-emerald-600" />
            <span>Zero Food Waste & Redistribution Platform</span>
          </div>

          <h1 className="text-4xl sm:text-6xl font-extrabold tracking-tight text-slate-900 leading-tight max-w-4xl mx-auto">
            Transform Commercial Food Surplus into <span className="text-transparent bg-clip-text bg-gradient-to-r from-emerald-600 to-teal-600">Zero-Hunger Impact</span>
          </h1>

          <p className="mt-6 text-base sm:text-lg text-slate-600 max-w-2xl mx-auto font-normal leading-relaxed">
            Connect surplus food donors (Restaurants, Hotels, Caterers) with verified NGOs. Features automated safety risk prediction, instant logistics matching, and multi-recipient allocation.
          </p>

          {/* Action CTAs */}
          <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
            <Link
              to="/register"
              className="px-8 py-4 rounded-2xl bg-emerald-600 hover:bg-emerald-700 text-white font-extrabold text-sm sm:text-base shadow-xl shadow-emerald-600/25 hover:scale-[1.02] transition-all flex items-center gap-3"
            >
              <span>Register NGO or Restaurant</span>
              <ArrowRight className="w-5 h-5" />
            </Link>

            <Link
              to="/login"
              className="px-8 py-4 rounded-2xl bg-white border border-slate-300 hover:border-slate-400 text-slate-800 font-extrabold text-sm sm:text-base shadow-xs hover:bg-slate-50 transition-all"
            >
              Sign In to Portal
            </Link>

            <button
              onClick={() => handleQuickDemo('restaurant')}
              className="px-6 py-4 rounded-2xl bg-slate-100 hover:bg-slate-200 text-emerald-700 font-bold text-sm transition-all"
            >
              Explore Live App
            </button>
          </div>

          {/* Metrics Overview Bar */}
          <div className="mt-16 grid grid-cols-2 md:grid-cols-4 gap-4 max-w-5xl mx-auto">
            <div className="p-6 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
              <p className="text-3xl font-black text-emerald-600">142,500+</p>
              <p className="text-xs text-slate-500 font-bold mt-1 uppercase tracking-wider">Meals Saved</p>
            </div>
            <div className="p-6 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
              <p className="text-3xl font-black text-teal-600">18.4 mins</p>
              <p className="text-xs text-slate-500 font-bold mt-1 uppercase tracking-wider">Avg Match Speed</p>
            </div>
            <div className="p-6 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
              <p className="text-3xl font-black text-emerald-700">99.2%</p>
              <p className="text-xs text-slate-500 font-bold mt-1 uppercase tracking-wider">Safety Accuracy</p>
            </div>
            <div className="p-6 rounded-2xl bg-white border border-slate-200/90 shadow-sm">
              <p className="text-3xl font-black text-amber-600">312 Tons</p>
              <p className="text-xs text-slate-500 font-bold mt-1 uppercase tracking-wider">CO2 Emissions Saved</p>
            </div>
          </div>
        </div>
      </section>

      {/* Feature Showcase Grid */}
      <section id="features" className="py-20 bg-white border-b border-slate-200/80">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-14">
            <span className="text-xs font-extrabold text-emerald-700 uppercase tracking-wider bg-emerald-100/80 px-3.5 py-1 rounded-full border border-emerald-200">
              Core Capabilities
            </span>
            <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight mt-3">
              AI-Driven Redistribution Engine
            </h2>
            <p className="text-slate-600 mt-2 max-w-xl mx-auto text-sm">
              Ensuring maximum speed, safety compliance, and equitable meal distribution.
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 hover:bg-white hover:shadow-md transition-all">
              <div className="w-12 h-12 rounded-xl bg-emerald-100 text-emerald-700 flex items-center justify-center mb-4">
                <Zap className="w-6 h-6" />
              </div>
              <h4 className="text-base font-bold text-slate-900 mb-2">Surplus Readiness</h4>
              <p className="text-slate-600 text-xs leading-relaxed">
                Calculates precise safe-holding windows based on prep time, food item type, and temperature controls.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 hover:bg-white hover:shadow-md transition-all">
              <div className="w-12 h-12 rounded-xl bg-teal-100 text-teal-700 flex items-center justify-center mb-4">
                <Bot className="w-6 h-6" />
              </div>
              <h4 className="text-base font-bold text-slate-900 mb-2">Smart NGO Matcher</h4>
              <p className="text-slate-600 text-xs leading-relaxed">
                Ranks candidate NGOs by proximity, meal capacity, dietary preferences, and historical responsiveness.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 hover:bg-white hover:shadow-md transition-all">
              <div className="w-12 h-12 rounded-xl bg-blue-100 text-blue-700 flex items-center justify-center mb-4">
                <BarChart3 className="w-6 h-6" />
              </div>
              <h4 className="text-base font-bold text-slate-900 mb-2">Multi-NGO Splitter</h4>
              <p className="text-slate-600 text-xs leading-relaxed">
                Intelligently divides large catering/hotel donations among multiple recipient NGOs to eliminate food spoilage.
              </p>
            </div>

            <div className="p-6 rounded-2xl bg-slate-50 border border-slate-200/80 hover:bg-white hover:shadow-md transition-all">
              <div className="w-12 h-12 rounded-xl bg-amber-100 text-amber-700 flex items-center justify-center mb-4">
                <Award className="w-6 h-6" />
              </div>
              <h4 className="text-base font-bold text-slate-900 mb-2">Trust Scoring</h4>
              <p className="text-slate-600 text-xs leading-relaxed">
                Evaluates donors and NGOs with dynamic 0-100% Trust Scores based on past pickup punctuality and safety logs.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* SECTION: HOW IT WORKS */}
      <section id="how-it-works" className="py-20 bg-slate-100/70 border-b border-slate-200/80 scroll-mt-20">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <span className="text-xs font-extrabold text-teal-700 uppercase tracking-wider bg-teal-100/80 px-3.5 py-1 rounded-full border border-teal-200">
              Automated Process
            </span>
            <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight mt-3">
              How FoodFlow Rescue Works in 4 Steps
            </h2>
            <p className="text-slate-600 mt-2 max-w-xl mx-auto text-sm">
              From commercial surplus declaration to doorstep delivery at local community kitchens.
            </p>
          </div>

          <div className="grid md:grid-cols-4 gap-6 relative">
            {/* Step 1 */}
            <div className="p-6 rounded-2xl bg-white border border-slate-200 shadow-sm relative">
              <div className="w-10 h-10 rounded-full bg-emerald-600 text-white font-black flex items-center justify-center text-sm mb-4 shadow-md shadow-emerald-600/20">
                1
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Surplus Declaration</h3>
              <p className="text-slate-600 text-xs leading-relaxed">
                Commercial kitchens, hotels, or restaurants log surplus quantities, food temperature, and preparation time into the donor portal.
              </p>
            </div>

            {/* Step 2 */}
            <div className="p-6 rounded-2xl bg-white border border-slate-200 shadow-sm relative">
              <div className="w-10 h-10 rounded-full bg-teal-600 text-white font-black flex items-center justify-center text-sm mb-4 shadow-md shadow-teal-600/20">
                2
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">AI Risk Assessment</h3>
              <p className="text-slate-600 text-xs leading-relaxed">
                Machine learning models compute safe holding windows, micro-bacterial risk factors, and recommended consumption deadlines.
              </p>
            </div>

            {/* Step 3 */}
            <div className="p-6 rounded-2xl bg-white border border-slate-200 shadow-sm relative">
              <div className="w-10 h-10 rounded-full bg-blue-600 text-white font-black flex items-center justify-center text-sm mb-4 shadow-md shadow-blue-600/20">
                3
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Proximity Matching</h3>
              <p className="text-slate-600 text-xs leading-relaxed">
                The optimization engine pairs the batch with nearby verified NGOs or splits large donations across multiple recipient shelters.
              </p>
            </div>

            {/* Step 4 */}
            <div className="p-6 rounded-2xl bg-white border border-slate-200 shadow-sm relative">
              <div className="w-10 h-10 rounded-full bg-indigo-600 text-white font-black flex items-center justify-center text-sm mb-4 shadow-md shadow-indigo-600/20">
                4
              </div>
              <h3 className="text-lg font-bold text-slate-900 mb-2">Verified Dispatch</h3>
              <p className="text-slate-600 text-xs leading-relaxed">
                Volunteers accept dispatch alerts, confirm food temperature on pickup, and deliver meals with transparent ESG tracking logs.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Registration Portals Section */}
      <section id="portals" className="py-20 max-w-7xl mx-auto px-6 scroll-mt-20">
        <div className="text-center mb-14">
          <span className="text-xs font-extrabold text-emerald-700 uppercase tracking-wider bg-emerald-100/80 px-3.5 py-1 rounded-full border border-emerald-200">
            Partner Portals
          </span>
          <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight mt-3">
            Simple Onboarding for Donors & Recipients
          </h2>
          <p className="text-slate-600 mt-2 max-w-xl mx-auto text-sm">
            Register your organization to start donating surplus meals or receiving food allocations.
          </p>
        </div>

        <div className="grid md:grid-cols-2 gap-8 max-w-4xl mx-auto">
          {/* Restaurant Card */}
          <div className="p-8 rounded-3xl bg-white border border-slate-200/90 hover:border-amber-400/80 transition-all flex flex-col justify-between shadow-lg shadow-slate-200/50 group">
            <div>
              <div className="w-14 h-14 rounded-2xl bg-amber-50 border border-amber-200 text-amber-600 flex items-center justify-center mb-6 group-hover:scale-105 transition-transform">
                <Building2 className="w-7 h-7" />
              </div>
              <h3 className="text-2xl font-bold text-slate-900 mb-3">For Restaurants & Donors</h3>
              <p className="text-slate-600 text-xs sm:text-sm leading-relaxed mb-6">
                Restaurants, Hotels, Caterers, and Supermarkets. Log surplus food batches with AI shelf-life forecasting & automated NGO pick-up dispatch.
              </p>
              <ul className="space-y-3 text-xs text-slate-700 mb-8 font-medium">
                <li className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-amber-600 shrink-0" />
                  <span>Automated Safe Holding Window Calculation</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-amber-600 shrink-0" />
                  <span>Instant Verified NGO Match & Logistics Dispatch</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-amber-600 shrink-0" />
                  <span>ESG Tax & Sustainability Impact Reporting</span>
                </li>
              </ul>
            </div>
            <Link
              to="/register?type=restaurant"
              className="w-full py-3.5 rounded-xl bg-amber-600 hover:bg-amber-700 text-white font-bold text-xs uppercase tracking-wider text-center shadow-md shadow-amber-600/20 transition-all"
            >
              Register as Donor / Restaurant
            </Link>
          </div>

          {/* NGO Card */}
          <div className="p-8 rounded-3xl bg-white border border-slate-200/90 hover:border-emerald-500/80 transition-all flex flex-col justify-between shadow-lg shadow-slate-200/50 group">
            <div>
              <div className="w-14 h-14 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-600 flex items-center justify-center mb-6 group-hover:scale-105 transition-transform">
                <HeartHandshake className="w-7 h-7" />
              </div>
              <h3 className="text-2xl font-bold text-slate-900 mb-3">For NGO Recipients</h3>
              <p className="text-slate-600 text-xs sm:text-sm leading-relaxed mb-6">
                Shelters, Food Banks, and Non-Profits. Receive real-time food surplus alerts matched directly to your daily meal capacity and location.
              </p>
              <ul className="space-y-3 text-xs text-slate-700 mb-8 font-medium">
                <li className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>Capacity-Based Multi-NGO Batch Splitting</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>Optimized Route & Proximity Delivery Matching</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>Food Safety Inspection Verification</span>
                </li>
              </ul>
            </div>
            <Link
              to="/register?type=ngo"
              className="w-full py-3.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs uppercase tracking-wider text-center shadow-md shadow-emerald-600/20 transition-all"
            >
              Register as NGO Recipient
            </Link>
          </div>
        </div>
      </section>

      {/* SECTION: ESG IMPACT ANALYTICS */}
      <section id="impact" className="py-20 bg-white border-t border-slate-200/80 scroll-mt-20">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center mb-16">
            <span className="text-xs font-extrabold text-emerald-700 uppercase tracking-wider bg-emerald-100/80 px-3.5 py-1 rounded-full border border-emerald-200">
              Sustainability & ESG Metrics
            </span>
            <h2 className="text-3xl font-extrabold text-slate-900 tracking-tight mt-3">
              Measurable Environmental & Social Impact
            </h2>
            <p className="text-slate-600 mt-2 max-w-xl mx-auto text-sm">
              Real-time carbon offset accounting and food waste diversion analytics verified by SHAP explainable models.
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-8">
            <div className="p-8 rounded-3xl bg-slate-50 border border-slate-200 shadow-sm space-y-4">
              <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center">
                <Leaf className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-slate-900">Carbon Offsets</h3>
              <p className="text-3xl font-black text-emerald-600">312 Metric Tons</p>
              <p className="text-xs text-slate-600 leading-relaxed">
                Prevented methane emissions from commercial food decomposing in municipal landfills.
              </p>
            </div>

            <div className="p-8 rounded-3xl bg-slate-50 border border-slate-200 shadow-sm space-y-4">
              <div className="w-12 h-12 rounded-2xl bg-teal-100 text-teal-700 flex items-center justify-center">
                <Globe className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-slate-900">Water Conservation</h3>
              <p className="text-3xl font-black text-teal-600">1.2M Liters</p>
              <p className="text-xs text-slate-600 leading-relaxed">
                Saved embedded agricultural water used in surplus food production and processing.
              </p>
            </div>

            <div className="p-8 rounded-3xl bg-slate-50 border border-slate-200 shadow-sm space-y-4">
              <div className="w-12 h-12 rounded-2xl bg-indigo-100 text-indigo-700 flex items-center justify-center">
                <FileCheck className="w-6 h-6" />
              </div>
              <h3 className="text-xl font-bold text-slate-900">ESG Compliance</h3>
              <p className="text-3xl font-black text-indigo-600">100% Audit Ready</p>
              <p className="text-xs text-slate-600 leading-relaxed">
                Automated tax deduction certificates and CSR sustainability reporting for commercial donors.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-10 border-t border-slate-200/80 bg-slate-900 text-slate-400 text-xs">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-emerald-600 flex items-center justify-center text-white font-black text-sm">
              FF
            </div>
            <span className="text-sm font-bold text-white">FoodFlow AI Engine</span>
            <span className="text-slate-600">|</span>
            <span>Zero-Hunger Redistribution Network</span>
          </div>

          <div className="flex items-center gap-6 font-semibold">
            <Link to="/login" className="hover:text-white transition-colors">Sign In</Link>
            <Link to="/register" className="hover:text-white transition-colors">Register NGO / Restaurant</Link>
            <Link to="/admin" className="text-slate-400 hover:text-white transition-colors flex items-center gap-1">
              <Lock className="w-3.5 h-3.5" />
              <span>Admin Security Portal</span>
            </Link>
          </div>
        </div>
      </footer>
    </div>
  )
}
