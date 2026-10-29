import React, { useState } from 'react'
import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  PlusCircle,
  ShieldCheck,
  GitMerge,
  PieChart,
  HelpCircle,
  Leaf,
  ChevronLeft,
  ChevronRight,
  Sparkles,
  Building2,
  HeartHandshake,
  Cpu,
  Award,
  Clock
} from 'lucide-react'
import { useStore } from '../../store/useStore'

export default function Sidebar() {
  const [collapsed, setCollapsed] = useState(false)
  const { userRole } = useStore()

  // Role-Specific Categorized Navigation Definitions
  const roleNavs = {
    restaurant: [
      {
        category: 'Donation Management',
        items: [
          { label: 'Overview Dashboard', path: '/dashboard', icon: LayoutDashboard },
          { label: 'Submit New Donation', path: '/donation/new', icon: PlusCircle },
          { label: 'Donation History Log', path: '/history', icon: Clock },
          { label: 'Readiness & Risk', path: '/donation/DON-9042/risk', icon: ShieldCheck },
          { label: 'NGO Matching Status', path: '/donation/DON-9042/match', icon: GitMerge },
          { label: 'Donation Splitting', path: '/donation/DON-9042/allocate', icon: PieChart },
        ]
      },
      {
        category: 'Trust & Impact',
        items: [
          { label: 'Donor Trust Score', path: '/trust', icon: Award },
          { label: 'Sustainability Impact', path: '/impact', icon: Leaf },
        ]
      }
    ],

    ngo: [
      {
        category: 'Recipient Operations',
        items: [
          { label: 'Recipient Dashboard', path: '/dashboard', icon: LayoutDashboard },
          { label: 'Incoming Assigned Queue', path: '/queue', icon: GitMerge },
          { label: 'Donation History Log', path: '/history', icon: Clock },
        ]
      },
      {
        category: 'Trust & Impact',
        items: [
          { label: 'NGO Trust Profile', path: '/trust', icon: Award },
          { label: 'Impact & Beneficiaries', path: '/impact', icon: Leaf },
        ]
      }
    ],

    admin: [
      {
        category: 'Admin Control Center',
        items: [
          { label: 'Overview Dashboard', path: '/dashboard', icon: LayoutDashboard },
          { label: 'Entity Verifications (/admin)', path: '/admin', icon: ShieldCheck },
          { label: 'Donation History Registry', path: '/history', icon: Clock },
        ]
      },
      {
        category: 'Intelligence & Impact',
        items: [
          { label: 'Trust & Reliability Ratings', path: '/trust', icon: Award },
          { label: 'Sustainability Impact', path: '/impact', icon: Leaf },
        ]
      },
      {
        category: 'Public Portals',
        items: [
          { label: 'Landing Page', path: '/landing', icon: Sparkles },
          { label: 'Register Partner', path: '/register', icon: Building2 },
          { label: 'Sign In Page', path: '/login', icon: HeartHandshake },
        ]
      }
    ]
  }

  const currentNav = roleNavs[userRole] || roleNavs.admin

  return (
    <aside
      className={`fixed left-0 top-0 bottom-0 z-40 bg-white/95 border-r border-slate-200/90 backdrop-blur-xl transition-all duration-300 flex flex-col shadow-sm ${
        collapsed ? 'w-20' : 'w-64'
      }`}
    >
      {/* Brand Header */}
      <div className="h-16 flex items-center justify-between px-4 border-b border-slate-200/90">
        <NavLink to="/" className="flex items-center overflow-hidden">
          {!collapsed ? (
            <span className="font-black text-xl tracking-tight text-gradient-emerald">Food Flow</span>
          ) : (
            <span className="font-black text-lg tracking-tight text-gradient-emerald">FF</span>
          )}
        </NavLink>

        <button
          onClick={() => setCollapsed(!collapsed)}
          className="p-1.5 rounded-lg bg-slate-100 border border-slate-200 text-slate-500 hover:text-slate-900 transition-colors"
        >
          {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
        </button>
      </div>



      {/* Navigation Links Grouped by Role Workflow */}
      <nav className="flex-1 px-3 py-3 space-y-4 overflow-y-auto">
        {currentNav.map((section, sIdx) => (
          <div key={sIdx} className="space-y-1">
            {!collapsed && (
              <h4 className="px-3 text-[10px] font-black uppercase tracking-wider text-slate-400 mb-1">
                {section.category}
              </h4>
            )}
            {section.items.map((item) => {
              const Icon = item.icon
              return (
                <NavLink
                  key={item.path + item.label}
                  to={item.path}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-bold transition-all ${
                      isActive
                        ? 'bg-emerald-600 text-white font-extrabold shadow-md shadow-emerald-500/20'
                        : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
                    }`
                  }
                  title={collapsed ? item.label : undefined}
                >
                  <Icon className="w-4 h-4 shrink-0" />
                  {!collapsed && <span className="truncate">{item.label}</span>}
                </NavLink>
              )
            })}
          </div>
        ))}
      </nav>


    </aside>
  )
}
