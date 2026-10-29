import { create } from 'zustand'

export const useStore = create((set, get) => ({
  // User Role State & Auth (Persisted to localStorage across page reloads)
  userRole: (() => {
    try {
      const saved = localStorage.getItem('foodflow_user_role')
      return saved || 'admin'
    } catch (e) {
      return 'admin'
    }
  })(),
  setUserRole: (role) => {
    try { localStorage.setItem('foodflow_user_role', role) } catch (e) {}
    set({ userRole: role })
  },

  currentUser: (() => {
    try {
      const saved = localStorage.getItem('foodflow_current_user')
      return saved ? JSON.parse(saved) : {
        name: 'Admin Command Center',
        email: 'admin@foodflow.org',
        role: 'admin',
        verified: true
      }
    } catch (e) {
      return {
        name: 'Admin Command Center',
        email: 'admin@foodflow.org',
        role: 'admin',
        verified: true
      }
    }
  })(),

  loginUser: (role, email, name) => {
    const userObj = {
      name: name || (role === 'restaurant' ? 'Grand Horizon Hotel' : role === 'ngo' ? 'Care Food Network' : 'System Administrator'),
      email: email || `${role}@foodflow.org`,
      role: role,
      verified: true
    }
    try {
      localStorage.setItem('foodflow_user_role', role)
      localStorage.setItem('foodflow_current_user', JSON.stringify(userObj))
    } catch (e) {}
    set({
      userRole: role,
      currentUser: userObj
    })
  },

  logoutUser: () => {
    try {
      localStorage.removeItem('foodflow_user_role')
      localStorage.removeItem('foodflow_current_user')
    } catch (e) {}
    set({
      userRole: 'admin',
      currentUser: null
    })
  },

  // Pending Registrations Queue for Admin Approval (Synced to localStorage)
  pendingRegistrations: (() => {
    try {
      const saved = localStorage.getItem('foodflow_pending_registrations')
      return saved ? JSON.parse(saved) : []
    } catch (e) {
      return []
    }
  })(),

  // Registered Approved Entities List (Synced to localStorage)
  registeredEntities: (() => {
    try {
      const saved = localStorage.getItem('foodflow_registered_entities')
      if (!saved) return []
      const parsed = JSON.parse(saved)
      return parsed.filter(e => e && e.name && !e.name.toLowerCase().includes('people'))
    } catch (e) {
      return []
    }
  })(),

  // Submit new registration (From Register Page)
  registerEntity: (newRegistration) => set((state) => {
    const regItem = {
      id: `REG-${Date.now().toString().slice(-4)}`,
      ...newRegistration,
      submittedAt: 'Just now',
      status: 'pending'
    }
    const updatedPending = [regItem, ...state.pendingRegistrations.filter(r => r.email !== newRegistration.email)]
    try {
      localStorage.setItem('foodflow_pending_registrations', JSON.stringify(updatedPending))
    } catch (e) {}

    return {
      pendingRegistrations: updatedPending,
      notifications: [
        {
          id: Date.now(),
          type: 'trust',
          title: `New ${newRegistration.type.toUpperCase()} Registration: ${newRegistration.name || newRegistration.contactName}`,
          time: 'Just now',
          read: false
        },
        ...state.notifications
      ]
    }
  }),

  // Admin Approve Registration
  approveRegistration: (regId) => set((state) => {
    const item = state.pendingRegistrations.find(r => r.id === regId)
    if (!item) return state

    const newApprovedEntity = {
      id: `ENT-${Date.now().toString().slice(-4)}`,
      type: item.type,
      name: item.name || item.contactName || 'Verified Partner',
      email: item.email,
      licenseNo: item.licenseNo || item.regId || 'VERIFIED-ID',
      address: item.address || 'Metro Area',
      trustScore: 95,
      status: 'active'
    }

    const updatedPending = state.pendingRegistrations.filter(r => r.id !== regId)
    const updatedEntities = [newApprovedEntity, ...state.registeredEntities.filter(e => e.email !== item.email)]

    try {
      localStorage.setItem('foodflow_pending_registrations', JSON.stringify(updatedPending))
      localStorage.setItem('foodflow_registered_entities', JSON.stringify(updatedEntities))
    } catch (e) {}

    return {
      pendingRegistrations: updatedPending,
      registeredEntities: updatedEntities,
      notifications: [
        {
          id: Date.now(),
          type: 'trust',
          title: `Admin Approved Account: ${item.name || item.contactName}`,
          time: 'Just now',
          read: false
        },
        ...state.notifications
      ]
    }
  }),

  // Admin Reject Registration
  rejectRegistration: (regId) => set((state) => {
    const updatedPending = state.pendingRegistrations.filter(r => r.id !== regId)
    try {
      localStorage.setItem('foodflow_pending_registrations', JSON.stringify(updatedPending))
    } catch (e) {}
    return { pendingRegistrations: updatedPending }
  }),

  // Admin Pre-create New Entity Directly
  createEntityByAdmin: (entityData) => set((state) => {
    const newEntity = {
      id: `ENT-${Date.now().toString().slice(-4)}`,
      status: 'active',
      trustScore: 95,
      ...entityData
    }
    return {
      registeredEntities: [newEntity, ...state.registeredEntities],
      notifications: [
        {
          id: Date.now(),
          type: 'trust',
          title: `Admin Pre-Created Account: ${newEntity.name} (${newEntity.type.toUpperCase()})`,
          time: 'Just now',
          read: false
        },
        ...state.notifications
      ]
    }
  }),

  // Notification Feed
  notifications: [
    { id: 1, type: 'match', title: 'New High Suitability NGO Found: Hope Kitchen Community Shelter', time: '2 mins ago', read: false },
    { id: 2, type: 'risk', title: 'Risk Alert: DON-9042 Holding Window Valid (14.2h left)', time: '14 mins ago', read: false },
    { id: 3, type: 'trust', title: 'Hope Kitchen Trust Rating Upgraded to 98%', time: '1 hour ago', read: true }
  ],
  addNotification: (notif) => set((state) => ({
    notifications: [
      {
        id: Date.now(),
        time: 'Just now',
        read: false,
        ...notif
      },
      ...state.notifications
    ]
  })),
  markNotificationRead: (id) => set((state) => ({
    notifications: state.notifications.map(n => n.id === id ? { ...n, read: true } : n)
  })),
  markAllNotificationsRead: () => set((state) => ({
    notifications: state.notifications.map(n => ({ ...n, read: true }))
  })),

  // Session State Persistence for new submitted donations (Synced to localStorage across accounts)
  customDonations: (() => {
    try {
      const saved = localStorage.getItem('foodflow_custom_donations')
      return saved ? JSON.parse(saved) : []
    } catch (e) {
      return []
    }
  })(),
  addCustomDonation: (donation) => set((state) => {
    const updated = [donation, ...state.customDonations.filter(d => d.id !== donation.id)]
    try {
      localStorage.setItem('foodflow_custom_donations', JSON.stringify(updated))
    } catch (e) {}
    return { customDonations: updated }
  }),
  clearCustomDonations: () => set(() => {
    try {
      localStorage.removeItem('foodflow_custom_donations')
    } catch (e) {}
    return { customDonations: [] }
  }),

  // Allocation State
  customAllocations: {},
  setCustomAllocation: (donationId, splits) => set((state) => {
    if (!splits) {
      const { [donationId]: _, ...rest } = state.customAllocations
      return { customAllocations: rest }
    }
    return { customAllocations: { ...state.customAllocations, [donationId]: splits } }
  }),

  // NGO Match Action State (Accept/Decline)
  ngoActions: {},
  setNgoAction: (donationId, status) => set((state) => ({
    ngoActions: { ...state.ngoActions, [donationId]: status }
  }))
}))

