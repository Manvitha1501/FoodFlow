import React from 'react'
import { motion } from 'framer-motion'

export default function ScoreGauge({ 
  score = 0, 
  size = 140, 
  strokeWidth = 10, 
  label = '', 
  showLabel = true,
  sublabel = '' 
}) {
  const radius = (size - strokeWidth) / 2
  const circumference = 2 * Math.PI * radius
  const strokeDashoffset = circumference - (score / 100) * circumference

  // Color mapping for Light Theme high contrast
  const getColor = (val) => {
    if (val >= 80) return { stroke: '#059669', text: 'text-emerald-700', bg: 'bg-emerald-100/80', border: 'border-emerald-300' }
    if (val >= 60) return { stroke: '#D97706', text: 'text-amber-700', bg: 'bg-amber-100/80', border: 'border-amber-300' }
    return { stroke: '#DC2626', text: 'text-red-700', bg: 'bg-red-100/80', border: 'border-red-300' }
  }

  const theme = getColor(score)

  return (
    <div className="flex flex-col items-center justify-center">
      <div className="relative flex items-center justify-center" style={{ width: size, height: size }}>
        <svg width={size} height={size} className="transform -rotate-90">
          {/* Light Track */}
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="#e2e8f0"
            strokeWidth={strokeWidth}
            fill="transparent"
          />
          {/* Animated Value Ring */}
          <motion.circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke={theme.stroke}
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            initial={{ strokeDashoffset: circumference }}
            animate={{ strokeDashoffset }}
            transition={{ duration: 1.2, ease: "easeOut" }}
            strokeLinecap="round"
            fill="transparent"
          />
        </svg>

        {/* Center Content */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <motion.span 
            initial={{ opacity: 0, scale: 0.5 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className={`font-black ${theme.text}`}
            style={{ fontSize: size * 0.25 }}
          >
            {score}%
          </motion.span>
          {sublabel && (
            <span className="text-xs font-semibold text-slate-500 mt-0.5">{sublabel}</span>
          )}
        </div>
      </div>

      {showLabel && label && (
        <span 
          className={`mt-3 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider border ${theme.bg} ${theme.text} ${theme.border}`}
        >
          {label}
        </span>
      )}
    </div>
  )
}
