'use client'

import { useEffect, useMemo, useState } from 'react'
import { supabase } from '../lib/supabase'

type Section = 'overview' | 'users' | 'transactions' | 'games' | 'rewards' | 'withdrawals'
type Row = Record<string, any> & { status?: string; admin_note?: string | null }
type Dashboard = { redemptions: Row[]; transactions: Row[]; rewards: Row[]; plays: Row[]; users: Row[] }
const sections: Array<[Section, string]> = [['overview', 'Overview'], ['users', 'Users'], ['transactions', 'Transactions'], ['games', 'Game plays'], ['rewards', 'Rewards catalog'], ['withdrawals', 'Withdrawals']]

export default function AdminPage() {
  const [session, setSession] = useState<any>(null)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] = useState('')
  const [authMode, setAuthMode] = useState<'login' | 'signup' | 'forgot' | 'reset'>('login')
  const [section, setSection] = useState<Section>('overview')
  const [dashboard, setDashboard] = useState<Dashboard>({ redemptions: [], transactions: [], rewards: [], plays: [], users: [] })
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  async function load(current = session) {
    if (!current) return
    setLoading(true)
    const response = await fetch('/api/admin', { method: 'POST', headers: { authorization: `Bearer ${current.access_token}`, 'content-type': 'application/json' }, body: JSON.stringify({ action: 'overview' }) })
    const result = await response.json()
    if (result.data) setDashboard(result.data)
    setMessage(result.error ?? '')
    setLoading(false)
  }

  useEffect(() => {
    const client = supabase()
    client.auth.getSession().then(({ data }) => { setSession(data.session); if (data.session) load(data.session) })
    const onHash = () => { if (window.location.hash.includes('type=recovery')) setAuthMode('reset') }
    onHash(); window.addEventListener('hashchange', onHash)
    return () => window.removeEventListener('hashchange', onHash)
  }, [])

  async function login(event: React.FormEvent) {
    event.preventDefault(); setMessage('')
    const { data, error } = await supabase().auth.signInWithPassword({ email, password })
    if (error) return setMessage('Invalid email or password.')
    setSession(data.session); load(data.session)
  }

  async function signup(event: React.FormEvent) {
    event.preventDefault(); setMessage('')
    if (password !== confirmPassword) return setMessage('Passwords do not match.')
    const { data, error } = await supabase().auth.signUp({
      email,
      password,
      options: {
        emailRedirectTo:
          process.env.NEXT_PUBLIC_DEV_SUPABASE_REDIRECT_URL ??
          `${window.location.origin}/auth/callback`,
        data: { role: 'admin' },
      },
    })
    if (error) return setMessage(error.message)
    if (data.session) { setSession(data.session); load(data.session) } else setMessage('Account created. Check your email to confirm, then sign in.')
  }

  async function forgot(event: React.FormEvent) {
    event.preventDefault(); const { error } = await supabase().auth.resetPasswordForEmail(email, { redirectTo: `${window.location.origin}/` })
    setMessage(error ? error.message : 'Check your email for a secure reset link.'); if (!error) setAuthMode('login')
  }

  async function reset(event: React.FormEvent) {
    event.preventDefault(); if (newPassword.length < 8) return setMessage('Password must be at least 8 characters.')
    if (newPassword !== confirmNewPassword) return setMessage('New passwords do not match.')
    const { error } = await supabase().auth.updateUser({ password: newPassword })
    if (error) return setMessage(error.message)
    await supabase().auth.signOut(); setSession(null); setAuthMode('login'); setMessage('Password set. Sign in with your new password.')
  }

  async function update(row: Row, status: string) {
    const note = window.prompt('Payment reference or admin note', row.admin_note ?? '')
    if (note === null || !session) return
    const response = await fetch('/api/admin', { method: 'POST', headers: { authorization: `Bearer ${session.access_token}`, 'content-type': 'application/json' }, body: JSON.stringify({ action: 'update', id: row.id, status, adminNote: note }) })
    const result = await response.json(); setMessage(result.error ?? 'Withdrawal updated.'); if (!result.error) load()
  }

  const stats = useMemo(() => ({ users: dashboard.users.length, transactions: dashboard.transactions.length, plays: dashboard.plays.length, pending: dashboard.redemptions.filter((row) => row.status === 'pending').length, paid: dashboard.redemptions.filter((row) => row.status === 'paid').length }), [dashboard])

  if (!session) return <main className="auth-shell"><div className="auth-card"><p className="eyebrow">L Rewards / Control center</p><h1>{authMode === 'signup' ? 'Create admin account' : authMode === 'forgot' ? 'Reset password' : authMode === 'reset' ? 'Choose new password' : 'Admin sign in'}</h1><p className="muted">Private operations dashboard for your Android rewards app.</p>{authMode === 'reset' ? <form onSubmit={reset}><input aria-label="New password" type="password" minLength={8} placeholder="New password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required /><input aria-label="Confirm new password" type="password" minLength={8} placeholder="Confirm new password" value={confirmNewPassword} onChange={(e) => setConfirmNewPassword(e.target.value)} required /><button>Save new password</button></form> : authMode === 'forgot' ? <form onSubmit={forgot}><input aria-label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /><button>Email reset link</button><button type="button" className="link-button" onClick={() => setAuthMode('login')}>Back to sign in</button></form> : <form onSubmit={authMode === 'signup' ? signup : login}><input aria-label="Email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required /><input aria-label="Password" type="password" minLength={8} placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required />{authMode === 'signup' && <input aria-label="Confirm password" type="password" minLength={8} placeholder="Confirm password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />}<button>{authMode === 'signup' ? 'Create account' : 'Sign in'}</button><div className="auth-links"><button type="button" className="link-button" onClick={() => setAuthMode(authMode === 'signup' ? 'login' : 'signup')}>{authMode === 'signup' ? 'Back to sign in' : 'Create admin account'}</button><button type="button" className="link-button" onClick={() => setAuthMode('forgot')}>Forgot password?</button></div></form>}{message && <p className="message">{message}</p>}</div></main>

  const data = section === 'users' ? dashboard.users : section === 'transactions' ? dashboard.transactions : section === 'games' ? dashboard.plays : section === 'rewards' ? dashboard.rewards : dashboard.redemptions
  return <main className="app-shell"><aside><div className="brand"><span className="brand-mark">L</span><div><strong>L Rewards</strong><small>Admin control center</small></div></div><nav>{sections.map(([key, label]) => <button key={key} className={section === key ? 'active' : ''} onClick={() => setSection(key)}>{label}</button>)}</nav><button className="signout" onClick={async () => { await supabase().auth.signOut(); setSession(null) }}>Sign out</button></aside><section className="workspace"><header className="topbar"><div><p className="eyebrow">Operations</p><h1>{sections.find(([key]) => key === section)?.[1]}</h1></div><button className="secondary" onClick={() => load()}>{loading ? 'Refreshing…' : 'Refresh data'}</button></header>{section === 'overview' ? <><div className="stats">{[['Total users', stats.users], ['Transactions', stats.transactions], ['Game plays', stats.plays], ['Pending withdrawals', stats.pending], ['Paid withdrawals', stats.paid]].map(([label, value]) => <article key={label}><span>{label}</span><strong>{value}</strong></article>)}</div><div className="panel-grid"><section className="panel"><h2>Recent withdrawals</h2><Table rows={dashboard.redemptions.slice(0, 8)} fields={['reward', 'status', 'created_at']} /></section><section className="panel"><h2>Recent activity</h2><Table rows={dashboard.transactions.slice(0, 8)} fields={['user_id', 'amount', 'created_at']} /></section></div></> : <section className="panel"><div className="panel-heading"><div><h2>{sections.find(([key]) => key === section)?.[1]}</h2><p className="muted">Live data from the Android app through Supabase.</p></div><span className="count">{data.length} records</span></div>{section === 'withdrawals' ? <Table rows={data} fields={['reward', 'cost', 'destination', 'status', 'created_at']} action={update} /> : <Table rows={data} fields={Object.keys(data[0] ?? {}).filter((key) => !['id', 'user'].includes(key)).slice(0, 6)} />}</section>}{message && <p className="message">{message}</p>}</section></main>
}

function Table({ rows, fields, action }: { rows: Row[]; fields: string[]; action?: (row: Row, status: string) => void }) { return <div className="table-wrap"><table><thead><tr>{fields.map((field) => <th key={field}>{field.replaceAll('_', ' ')}</th>)}{action && <th>Actions</th>}</tr></thead><tbody>{rows.map((row, index) => <tr key={row.id ?? index}>{fields.map((field) => <td key={field}>{field === 'created_at' && row[field] ? new Date(row[field]).toLocaleString() : String(row[field] ?? '—')}</td>)}{action && <td><div className="actions"><button onClick={() => action(row, 'approved')}>Approve</button><button onClick={() => action(row, 'paid')}>Mark paid</button><button onClick={() => action(row, 'rejected')}>Reject</button></div></td>}</tr>)}</tbody></table>{!rows.length && <p className="empty">No records found.</p>}</div> }
