'use client'

import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

type Redemption = { id:string; user_id:string; reward:string; cost:number; destination:string; status:string; created_at:string }
const statuses = ['pending','approved','paid','rejected']

export default function AdminPage() {
  const client = supabase()
  const [session, setSession] = useState<any>(null)
  const [email, setEmail] = useState('sramanan602@gmail.com')
  const [password, setPassword] = useState('')
  const [rows, setRows] = useState<Redemption[]>([])
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  async function refresh(current = session) {
    if (!current) return
    setLoading(true)
    const response = await fetch('/api/admin', { method:'POST', headers:{ authorization:`Bearer ${current.access_token}`, 'content-type':'application/json' }, body:JSON.stringify({ action:'list' }) })
    const result = await response.json()
    setRows(result.data ?? [])
    setMessage(result.error ?? '')
    setLoading(false)
  }

  useEffect(() => { client.auth.getSession().then(({ data }) => { setSession(data.session); refresh(data.session) }) }, [])

  async function signIn(event: React.FormEvent) {
    event.preventDefault(); setMessage('')
    const { data, error } = await client.auth.signInWithPassword({ email, password })
    if (error) return setMessage('Invalid email or password.')
    if (data.user?.email?.toLowerCase() !== 'sramanan602@gmail.com') { await client.auth.signOut(); return setMessage('This account is not allowlisted.') }
    setSession(data.session); refresh(data.session)
  }

  async function update(id:string, status:string) {
    if (!session) return
    const response = await fetch('/api/admin', { method:'POST', headers:{ authorization:`Bearer ${session.access_token}`, 'content-type':'application/json' }, body:JSON.stringify({ action:'update', id, status }) })
    const result = await response.json()
    if (result.error) return setMessage(result.error)
    setRows((items) => items.map((item) => item.id === id ? { ...item, status } : item))
  }

  if (!session) return <main className="min-h-screen px-6 py-12"><div className="mx-auto max-w-md rounded-3xl border border-[var(--line)] bg-[var(--panel)] p-8 shadow-2xl"><p className="mb-3 text-sm uppercase tracking-[0.22em] text-[var(--accent)]">L Rewards / Operations</p><h1 className="text-3xl font-semibold">Admin sign in</h1><p className="mt-3 leading-6 text-[var(--muted)]">Private console for reviewing manual gift-card and UPI withdrawals.</p><form onSubmit={signIn} className="mt-8 flex flex-col gap-4"><input aria-label="Email" value={email} onChange={(e)=>setEmail(e.target.value)} className="rounded-xl border border-[var(--line)] bg-[#0b1220] px-4 py-3" type="email" /><input aria-label="Password" value={password} onChange={(e)=>setPassword(e.target.value)} className="rounded-xl border border-[var(--line)] bg-[#0b1220] px-4 py-3" type="password" placeholder="Password" required /><button className="rounded-xl bg-[var(--accent)] px-4 py-3 font-semibold text-[#07131a]">Sign in</button></form>{message && <p className="mt-4 text-sm text-[var(--warn)]">{message}</p>}</div></main>

  return <main className="min-h-screen px-4 py-6 md:px-8"><header className="mx-auto flex max-w-7xl items-center justify-between gap-4 border-b border-[var(--line)] pb-6"><div><p className="text-sm uppercase tracking-[0.22em] text-[var(--accent)]">L Rewards / Operations</p><h1 className="mt-2 text-3xl font-semibold">Withdrawal desk</h1><p className="mt-2 text-[var(--muted)]">Review requests, verify delivery, then mark them paid.</p></div><button onClick={async()=>{await client.auth.signOut();setSession(null)}} className="rounded-xl border border-[var(--line)] px-4 py-2 text-sm">Sign out</button></header><section className="mx-auto mt-8 grid max-w-7xl gap-4 md:grid-cols-3"><div className="rounded-2xl border border-[var(--line)] bg-[var(--panel)] p-5"><p className="text-sm text-[var(--muted)]">Open requests</p><strong className="mt-2 block text-3xl">{rows.filter(r=>r.status==='pending').length}</strong></div><div className="rounded-2xl border border-[var(--line)] bg-[var(--panel)] p-5"><p className="text-sm text-[var(--muted)]">Approved</p><strong className="mt-2 block text-3xl">{rows.filter(r=>r.status==='approved').length}</strong></div><div className="rounded-2xl border border-[var(--line)] bg-[var(--panel)] p-5"><p className="text-sm text-[var(--muted)]">Paid</p><strong className="mt-2 block text-3xl">{rows.filter(r=>r.status==='paid').length}</strong></div></section><section className="mx-auto mt-8 max-w-7xl overflow-hidden rounded-2xl border border-[var(--line)] bg-[var(--panel)]"><div className="flex items-center justify-between border-b border-[var(--line)] p-5"><h2 className="text-xl font-semibold">Requests</h2><button onClick={()=>refresh()} className="text-sm text-[var(--accent)]">{loading?'Refreshing…':'Refresh'}</button></div><div className="overflow-x-auto"><table className="w-full min-w-[850px] text-left text-sm"><thead className="text-[var(--muted)]"><tr><th className="p-4">Created</th><th className="p-4">User</th><th className="p-4">Reward</th><th className="p-4">Coins</th><th className="p-4">Destination</th><th className="p-4">Status</th><th className="p-4">Action</th></tr></thead><tbody>{rows.map((row)=><tr key={row.id} className="border-t border-[var(--line)]"><td className="p-4">{new Date(row.created_at).toLocaleString()}</td><td className="p-4 font-mono text-xs">{row.user_id.slice(0,8)}…</td><td className="p-4">{row.reward}</td><td className="p-4">{row.cost}</td><td className="max-w-[220px] truncate p-4">{row.destination}</td><td className="p-4"><span className="rounded-full border border-[var(--line)] px-3 py-1 text-xs">{row.status}</span></td><td className="p-4"><select value={row.status} onChange={(e)=>update(row.id,e.target.value)} className="rounded-lg border border-[var(--line)] bg-[#0b1220] px-2 py-2">{statuses.map((status)=><option key={status}>{status}</option>)}</select></td></tr>)}{rows.length===0&&<tr><td colSpan={7} className="p-10 text-center text-[var(--muted)]">No withdrawal requests yet.</td></tr>}</tbody></table></div></section>{message&&<p className="mx-auto mt-4 max-w-7xl text-sm text-[var(--warn)]">{message}</p>}</main>
}
