'use client'

import { useEffect, useMemo, useState } from 'react'
import { supabase } from '../lib/supabase'

type Row = {
  id: string
  user_id: string
  reward: string
  cost: number
  destination: string
  status: 'pending' | 'approved' | 'paid' | 'rejected'
  admin_note: string | null
  created_at: string
  user?: { email: string; balance: number }
}

const statuses: Row['status'][] = ['pending', 'approved', 'paid', 'rejected']

export default function AdminPage() {
  const client = supabase()
  const [session, setSession] = useState<any>(null)
  const [email, setEmail] = useState('sramanan602@gmail.com')
  const [password, setPassword] = useState('')
  const [rows, setRows] = useState<Row[]>([])
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState<string | null>(null)

  async function refresh(current = session) {
    if (!current) return
    setLoading(true)
    const response = await fetch('/api/admin', {
      method: 'POST',
      headers: { authorization: `Bearer ${current.access_token}`, 'content-type': 'application/json' },
      body: JSON.stringify({ action: 'list' }),
    })
    const result = await response.json()
    setRows(result.data ?? [])
    setMessage(result.error ?? '')
    setLoading(false)
  }

  useEffect(() => {
    client.auth.getSession().then(({ data }) => {
      setSession(data.session)
      if (data.session) refresh(data.session)
    })
  }, [])

  async function signIn(event: React.FormEvent) {
    event.preventDefault()
    setMessage('')
    const { data, error } = await client.auth.signInWithPassword({ email, password })
    if (error) return setMessage('Invalid email or password.')
    if (data.user?.email?.toLowerCase() !== 'sramanan602@gmail.com') {
      await client.auth.signOut()
      return setMessage('This account is not allowlisted.')
    }
    setSession(data.session)
    refresh(data.session)
  }

  async function update(row: Row, status: Row['status']) {
    if (!session) return
    setSaving(row.id)
    const note = window.prompt('Optional admin note', row.admin_note ?? '')
    if (note === null) {
      setSaving(null)
      return
    }
    const response = await fetch('/api/admin', {
      method: 'POST',
      headers: { authorization: `Bearer ${session.access_token}`, 'content-type': 'application/json' },
      body: JSON.stringify({ action: 'update', id: row.id, status, adminNote: note }),
    })
    const result = await response.json()
    if (result.error) setMessage(result.error)
    else setRows((items) => items.map((item) => item.id === row.id ? { ...item, status, admin_note: result.data.admin_note } : item))
    setSaving(null)
  }

  const totals = useMemo(() => ({
    pending: rows.filter((row) => row.status === 'pending').length,
    approved: rows.filter((row) => row.status === 'approved').length,
    paid: rows.filter((row) => row.status === 'paid').length,
  }), [rows])

  if (!session) return (
    <main className="min-h-screen px-6 py-12">
      <div className="mx-auto max-w-md rounded-3xl border border-[var(--line)] bg-[var(--panel)] p-8 shadow-2xl">
        <p className="text-sm uppercase tracking-[0.22em] text-[var(--accent)]">L Rewards / Operations</p>
        <h1 className="mt-3 text-3xl font-semibold">Admin sign in</h1>
        <p className="mt-3 leading-6 text-[var(--muted)]">Private console for reviewing manual gift-card and UPI withdrawals.</p>
        <form onSubmit={signIn} className="mt-8 flex flex-col gap-4">
          <input aria-label="Email" value={email} onChange={(event) => setEmail(event.target.value)} className="rounded-xl border border-[var(--line)] bg-[var(--field)] px-4 py-3" type="email" required />
          <input aria-label="Password" value={password} onChange={(event) => setPassword(event.target.value)} className="rounded-xl border border-[var(--line)] bg-[var(--field)] px-4 py-3" type="password" placeholder="Password" required />
          <button className="rounded-xl bg-[var(--accent)] px-4 py-3 font-semibold text-[var(--ink)]">Sign in</button>
        </form>
        {message && <p className="mt-4 text-sm text-[var(--warn)]">{message}</p>}
      </div>
    </main>
  )

  return (
    <main className="min-h-screen px-4 py-6 md:px-8">
      <header className="mx-auto flex max-w-7xl items-center justify-between gap-4 border-b border-[var(--line)] pb-6">
        <div><p className="text-sm uppercase tracking-[0.22em] text-[var(--accent)]">L Rewards / Operations</p><h1 className="mt-2 text-3xl font-semibold">Withdrawal desk</h1><p className="mt-2 text-[var(--muted)]">Review requests, send the reward manually, then mark it paid.</p></div>
        <div className="flex items-center gap-3"><button onClick={() => refresh()} className="rounded-xl border border-[var(--line)] px-4 py-2 text-sm">{loading ? 'Refreshing…' : 'Refresh'}</button><button onClick={async () => { await client.auth.signOut(); setSession(null) }} className="rounded-xl border border-[var(--line)] px-4 py-2 text-sm">Sign out</button></div>
      </header>
      <section className="mx-auto mt-8 grid max-w-7xl gap-4 md:grid-cols-3">
        {([['Open requests', totals.pending], ['Approved to send', totals.approved], ['Paid', totals.paid]] as const).map(([label, value]) => <div key={label} className="rounded-2xl border border-[var(--line)] bg-[var(--panel)] p-5"><p className="text-sm text-[var(--muted)]">{label}</p><strong className="mt-2 block text-3xl">{value}</strong></div>)}
      </section>
      <section className="mx-auto mt-8 max-w-7xl overflow-hidden rounded-2xl border border-[var(--line)] bg-[var(--panel)]">
        <div className="border-b border-[var(--line)] p-5"><h2 className="text-xl font-semibold">Manual withdrawal requests</h2><p className="mt-1 text-sm text-[var(--muted)]">Every status change and note is saved in Supabase.</p></div>
        <div className="overflow-x-auto"><table className="w-full min-w-[1050px] text-left text-sm"><thead className="text-[var(--muted)]"><tr><th className="p-4">Created</th><th className="p-4">User</th><th className="p-4">Reward</th><th className="p-4">Destination</th><th className="p-4">Status</th><th className="p-4">Action</th></tr></thead><tbody>{rows.map((row) => <tr key={row.id} className="border-t border-[var(--line)] align-top"><td className="p-4 whitespace-nowrap">{new Date(row.created_at).toLocaleString()}</td><td className="p-4"><div>{row.user?.email ?? row.user_id}</div><div className="mt-1 text-xs text-[var(--muted)]">Balance: {row.user?.balance ?? 0} coins</div></td><td className="p-4"><div className="font-medium">{row.reward}</div><div className="text-xs text-[var(--muted)]">{row.cost} coins</div></td><td className="max-w-xs break-words p-4">{row.destination}</td><td className="p-4"><span className="rounded-full border border-[var(--line)] px-3 py-1 text-xs uppercase">{row.status}</span>{row.admin_note && <p className="mt-2 max-w-xs text-xs text-[var(--muted)]">Note: {row.admin_note}</p>}</td><td className="p-4"><div className="flex flex-wrap gap-2">{statuses.filter((status) => status !== row.status).map((status) => <button key={status} disabled={saving === row.id} onClick={() => update(row, status)} className="rounded-lg border border-[var(--line)] px-3 py-2 text-xs capitalize disabled:opacity-50">{saving === row.id ? 'Saving…' : status === 'approved' ? 'Approve' : status === 'paid' ? 'Mark paid' : status}</button>)}</div></td></tr>)}</tbody></table>{!rows.length && <p className="p-8 text-center text-[var(--muted)]">No withdrawal requests yet.</p>}</div>
      </section>
      {message && <p className="mx-auto mt-4 max-w-7xl text-sm text-[var(--warn)]">{message}</p>}
    </main>
  )
}
