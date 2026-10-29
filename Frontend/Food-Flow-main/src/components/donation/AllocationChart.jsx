import React from 'react'
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, BarChart, Bar, XAxis, YAxis } from 'recharts'

export default function AllocationChart({ splits = [], totalMeals = 800, mode = 'donut' }) {
  const COLORS = ['#059669', '#0891B2', '#D97706', '#7C3AED', '#DB2777']

  const chartData = splits.map((s, idx) => ({
    name: s.name,
    meals: s.meals,
    percentage: s.percentage || Math.round((s.meals / totalMeals) * 100),
    color: COLORS[idx % COLORS.length]
  }))

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload
      return (
        <div className="bg-white border border-slate-200 p-3 rounded-xl shadow-xl text-xs space-y-1">
          <p className="font-bold text-slate-800">{data.name}</p>
          <p className="text-emerald-700 font-extrabold">{data.meals} meals ({data.percentage}%)</p>
        </div>
      )
    }
    return null
  }

  if (mode === 'donut') {
    return (
      <div className="flex flex-col md:flex-row items-center justify-between gap-6 p-5 rounded-3xl bg-white border border-slate-200 shadow-xs">
        <div className="w-full md:w-1/2 h-56 relative flex items-center justify-center">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                innerRadius={60}
                outerRadius={85}
                paddingAngle={4}
                dataKey="meals"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} stroke="#ffffff" strokeWidth={2} />
                ))}
              </Pie>
              <Tooltip content={<CustomTooltip />} />
            </PieChart>
          </ResponsiveContainer>
          <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none text-center">
            <span className="text-2xl font-black text-slate-900">{totalMeals}</span>
            <span className="text-[10px] text-slate-500 uppercase tracking-wider font-bold">Total Meals</span>
          </div>
        </div>

        {/* Legend */}
        <div className="w-full md:w-1/2 space-y-2.5">
          {chartData.map((item, i) => (
            <div key={i} className="flex items-center justify-between p-2.5 rounded-xl bg-slate-50 border border-slate-200">
              <div className="flex items-center gap-2.5 min-w-0">
                <span className="w-3 h-3 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                <span className="text-xs font-bold text-slate-800 truncate">{item.name}</span>
              </div>
              <div className="text-right shrink-0">
                <span className="text-xs font-black text-emerald-700">{item.meals} meals</span>
                <span className="text-[10px] text-slate-500 ml-1.5 font-semibold">({item.percentage}%)</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="w-full h-40 p-4 rounded-2xl bg-white border border-slate-200 shadow-xs">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} layout="vertical" margin={{ top: 5, right: 20, left: 40, bottom: 5 }}>
          <XAxis type="number" stroke="#64748b" fontSize={11} />
          <YAxis dataKey="name" type="category" stroke="#64748b" fontSize={11} width={120} />
          <Tooltip content={<CustomTooltip />} />
          <Bar dataKey="meals" radius={[0, 6, 6, 0]}>
            {chartData.map((entry, index) => (
              <Cell key={`bar-cell-${index}`} fill={entry.color} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}
