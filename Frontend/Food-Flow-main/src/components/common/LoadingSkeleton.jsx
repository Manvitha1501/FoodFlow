import React from 'react'
import { Inbox, AlertCircle, RefreshCw } from 'lucide-react'

export function LoadingSkeleton({ type = 'card', count = 3 }) {
  return (
    <div className="space-y-4 animate-pulse">
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="p-5 rounded-2xl bg-white border border-slate-200 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <div className="h-4 bg-slate-200 rounded w-1/3"></div>
            <div className="h-6 bg-slate-200 rounded-full w-20"></div>
          </div>
          <div className="h-3 bg-slate-200 rounded w-3/4"></div>
          <div className="h-10 bg-slate-100 rounded-xl w-full"></div>
        </div>
      ))}
    </div>
  )
}

export function EmptyState({ title = 'No data available', message = 'There are no active entries found for this view.', onRetry = null }) {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-center rounded-2xl bg-white border border-dashed border-slate-300 shadow-xs">
      <div className="p-4 rounded-full bg-slate-100 text-slate-500 mb-4">
        <Inbox className="w-8 h-8" />
      </div>
      <h3 className="text-lg font-bold text-slate-800">{title}</h3>
      <p className="text-sm text-slate-600 max-w-sm mt-1 mb-4 leading-relaxed">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-sm font-semibold text-slate-800 transition-colors"
        >
          <RefreshCw className="w-4 h-4" /> Refresh Data
        </button>
      )}
    </div>
  )
}

export function ErrorState({ message = 'An error occurred while loading data.', onRetry }) {
  return (
    <div className="p-6 rounded-2xl bg-red-50 border border-red-200 text-center flex flex-col items-center shadow-xs">
      <AlertCircle className="w-10 h-10 text-red-600 mb-2" />
      <h4 className="text-base font-bold text-red-900">Failed to Load Content</h4>
      <p className="text-sm text-red-700 mt-1 max-w-md">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-4 px-4 py-2 rounded-xl bg-red-100 hover:bg-red-200 border border-red-300 text-xs font-bold text-red-900 transition-colors"
        >
          Retry Request
        </button>
      )}
    </div>
  )
}
