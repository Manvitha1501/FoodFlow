import React from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './components/layout/Layout'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import AdminPage from './pages/AdminPage'
import Dashboard from './pages/Dashboard'
import DonationNew from './pages/DonationNew'
import DonationRisk from './pages/DonationRisk'
import DonationMatch from './pages/DonationMatch'
import DonationAllocate from './pages/DonationAllocate'
import TrustIntelligence from './pages/TrustIntelligence'
import Explainability from './pages/Explainability'
import ImpactDashboard from './pages/ImpactDashboard'
import DonationHistory from './pages/DonationHistory'
import NgoQueue from './pages/NgoQueue'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Default Root Route opens Landing Page first */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/landing" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/admin" element={<AdminPage />} />

        {/* Portal Dashboard & Workflow pages wrapped in Main Layout */}
        <Route
          path="/*"
          element={
            <Layout>
              <Routes>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/donation/new" element={<DonationNew />} />
                <Route path="/risk" element={<DonationRisk />} />
                <Route path="/donation/:id/risk" element={<DonationRisk />} />
                <Route path="/match" element={<DonationMatch />} />
                <Route path="/donation/:id/match" element={<DonationMatch />} />
                <Route path="/allocate" element={<DonationAllocate />} />
                <Route path="/donation/:id/allocate" element={<DonationAllocate />} />
                <Route path="/queue" element={<NgoQueue />} />
                <Route path="/trust" element={<TrustIntelligence />} />
                <Route path="/explainability/:decisionId" element={<Explainability />} />
                <Route path="/impact" element={<ImpactDashboard />} />
                <Route path="/history" element={<DonationHistory />} />
                {/* Fallback route goes to Landing Page */}
                <Route path="*" element={<LandingPage />} />
              </Routes>
            </Layout>
          }
        />
      </Routes>
    </BrowserRouter>
  )
}
