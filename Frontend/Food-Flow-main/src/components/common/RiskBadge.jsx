import React from 'react'
import { ShieldCheck, ShieldAlert, ShieldX } from 'lucide-react'

export default function RiskBadge({ level = 'Low Risk', size = 'md' }) {
  const normalized = level.toLowerCase()

  const config = {
    low: {
      label: 'Low Risk',
      bgColor: 'bg-emerald-100/90 border-emerald-300 text-emerald-800 font-bold',
      icon: ShieldCheck
    },
    medium: {
      label: 'Medium Risk',
      bgColor: 'bg-amber-100/90 border-amber-300 text-amber-800 font-bold',
      icon: ShieldAlert
    },
    high: {
      label: 'High Risk',
      bgColor: 'bg-red-100/90 border-red-300 text-red-800 font-bold',
      icon: ShieldX
    }
  }

  const currentKey = normalized.includes('low') ? 'low' : normalized.includes('high') ? 'high' : 'medium'
  const target = config[currentKey]
  const Icon = target.icon

  const sizeClasses = size === 'sm' 
    ? 'px-2.5 py-0.5 text-xs gap-1' 
    : size === 'lg' 
    ? 'px-4 py-2 text-base gap-2 font-bold' 
    : 'px-3 py-1 text-sm gap-1.5 font-semibold'

  return (
    <span className={`inline-flex items-center rounded-full border shadow-xs ${target.bgColor} ${sizeClasses}`}>
      <Icon className={size === 'sm' ? 'w-3.5 h-3.5' : size === 'lg' ? 'w-5 h-5' : 'w-4 h-4'} />
      <span>{target.label}</span>
    </span>
  )
}
