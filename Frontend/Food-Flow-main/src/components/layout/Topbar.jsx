import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Bell, User, Check, Building2, HeartHandshake, ShieldCheck, LogOut, Sparkles } from 'lucide-react'
import { useStore } from '../../store/useStore'

export default function Topbar() {
  const navigate = useNavigate()
  const { userRole, currentUser, logoutUser, notifications, markNotificationRead, markAllNotificationsRead } = useStore()
  const [showNotifs, setShowNotifs] = useState(false)
  const [showProfile, setShowProfile] = useState(false)

  const unreadCount = notifications.filter(n => !n.read).length

  const handleSignOut = () => {
    logoutUser()
    setShowProfile(false)
    navigate('/login')
  }

  return (
    <header className="sticky top-0 z-30 h-16 bg-white/90 backdrop-blur-md border-b border-slate-200/90 px-6 flex items-center justify-between shadow-xs">
      {/* Active Isolated Portal Badge */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2">
          {userRole === 'restaurant' && (
            <span className="flex items-center gap-1.5 px-3 py-1 rounded-xl bg-amber-50 text-amber-800 border border-amber-200 text-xs font-extrabold uppercase tracking-wider">
              <Building2 className="w-4 h-4 text-amber-600" />
              <span>Restaurant Donor Portal</span>
            </span>
          )}
          {userRole === 'ngo' && (
            <span className="flex items-center gap-1.5 px-3 py-1 rounded-xl bg-emerald-50 text-emerald-800 border border-emerald-200 text-xs font-extrabold uppercase tracking-wider">
              <HeartHandshake className="w-4 h-4 text-emerald-600" />
              <span>NGO Recipient Portal</span>
            </span>
          )}
          {userRole === 'admin' && (
            <span className="flex items-center gap-1.5 px-3 py-1 rounded-xl bg-cyan-50 text-cyan-800 border border-cyan-200 text-xs font-extrabold uppercase tracking-wider">
              <ShieldCheck className="w-4 h-4 text-cyan-700" />
              <span>Admin Operations Center</span>
            </span>
          )}
        </div>

        {userRole === 'admin' && (
          <Link
            to="/admin"
            className="hidden md:flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200 text-xs font-bold transition-colors"
          >
            <ShieldCheck className="w-3.5 h-3.5 text-cyan-700" />
            <span>Admin Endpoint (/admin)</span>
          </Link>
        )}
      </div>

      {/* Right Action Bar */}
      <div className="flex items-center gap-4">
        {/* Notifications Bell */}
        <div className="relative">
          <button
            onClick={() => setShowNotifs(!showNotifs)}
            className="relative p-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 border border-slate-200 text-slate-700 transition-colors cursor-pointer"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-emerald-500 animate-ping" />
            )}
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-emerald-600 text-white font-bold text-[10px] flex items-center justify-center">
                {unreadCount}
              </span>
            )}
          </button>

          {/* Notifications Dropdown */}
          {showNotifs && (
            <div className="absolute right-0 mt-2 w-80 p-3 rounded-2xl bg-white border border-slate-200 shadow-2xl z-50 space-y-2">
              <div className="flex items-center justify-between pb-2 border-b border-slate-100">
                <span className="text-xs font-bold text-slate-900">Notifications Queue</span>
                <div className="flex items-center gap-2">
                  <span className="text-[10px] text-slate-500 font-semibold">{unreadCount} unread</span>
                  {unreadCount > 0 && (
                    <button
                      onClick={() => markAllNotificationsRead && markAllNotificationsRead()}
                      className="text-[10px] text-emerald-600 hover:underline font-bold"
                    >
                      Mark all read
                    </button>
                  )}
                </div>
              </div>
              <div className="space-y-1.5 max-h-64 overflow-y-auto">
                {notifications.map((n) => (
                  <div
                    key={n.id}
                    onClick={() => markNotificationRead(n.id)}
                    className={`p-2.5 rounded-xl border text-xs cursor-pointer transition-colors ${
                      n.read
                        ? 'bg-slate-50/60 border-slate-200/60 opacity-60'
                        : 'bg-emerald-50/60 border-emerald-200 text-slate-900 font-semibold'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <p className="leading-tight">{n.title}</p>
                      {!n.read && <Check className="w-3.5 h-3.5 text-emerald-600 shrink-0" />}
                    </div>
                    <span className="text-[10px] text-slate-500 mt-1 block font-normal">{n.time}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Isolated User Profile Menu */}
        <div className="relative">
          <button
            onClick={() => setShowProfile(!showProfile)}
            className="flex items-center gap-2.5 pl-2 pr-3 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 border border-slate-200 transition-colors"
          >
            <div className="w-7 h-7 rounded-lg bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold text-xs border border-emerald-300">
              <User className="w-4 h-4" />
            </div>
            <div className="text-left hidden lg:block">
              <p className="text-xs font-bold text-slate-900 leading-none">
                {currentUser?.name || (userRole === 'restaurant' ? 'Grand Horizon Hotel' : userRole === 'ngo' ? 'Care Food Network' : 'System Admin')}
              </p>
              <p className="text-[10px] text-slate-500 capitalize">{userRole} Account</p>
            </div>
          </button>

          {showProfile && (
            <div className="absolute right-0 mt-2 w-60 p-3 rounded-2xl bg-white border border-slate-200 shadow-2xl z-50 text-xs space-y-2">
              <div className="pb-2 border-b border-slate-100">
                <p className="font-bold text-slate-900">
                  {currentUser?.name || (userRole === 'restaurant' ? 'Grand Horizon Hotel' : userRole === 'ngo' ? 'Care Food Network' : 'System Admin')}
                </p>
                <p className="text-slate-500 text-[11px]">{currentUser?.email || `${userRole}@foodflow.org`}</p>
                <span className="inline-block mt-1 text-[10px] font-bold uppercase tracking-wider text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                  Role: {userRole}
                </span>
              </div>
              <div className="space-y-1">
                <Link
                  to="/landing"
                  onClick={() => setShowProfile(false)}
                  className="block text-slate-700 hover:text-slate-900 py-1.5 px-2 rounded-lg hover:bg-slate-100 font-medium"
                >
                  Landing Page
                </Link>
                <Link
                  to="/register"
                  onClick={() => setShowProfile(false)}
                  className="block text-slate-700 hover:text-slate-900 py-1.5 px-2 rounded-lg hover:bg-slate-100 font-medium"
                >
                  Register Partner Account
                </Link>
                <button
                  type="button"
                  onClick={handleSignOut}
                  className="w-full text-left text-rose-700 hover:text-rose-800 py-1.5 px-2 rounded-lg hover:bg-rose-50 font-bold flex items-center gap-1.5"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span>Sign Out / Switch Account</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
