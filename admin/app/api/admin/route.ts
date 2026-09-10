import { NextResponse } from 'next/server'
import { createClient } from '@supabase/supabase-js'

const allowedEmail = 'sramanan602@gmail.com'

function getAdminClient() {
  const url = process.env.SUPABASE_URL_2
  const secretKey = process.env.SUPABASE_SECRET_KEY_2
  if (!url || !secretKey) throw new Error('Supabase admin environment is not configured')
  return createClient(url, secretKey, { auth: { autoRefreshToken: false, persistSession: false } })
}

async function requireAdmin(request: Request) {
  const token = request.headers.get('authorization')?.replace('Bearer ', '')
  if (!token) return null
  const admin = getAdminClient()
  const { data: { user }, error } = await admin.auth.getUser(token)
  return error || user?.email?.toLowerCase() !== allowedEmail ? null : user
}

async function safeQuery<T>(query: PromiseLike<{ data: T | null; error: { message: string } | null }>) {
  const result = await query
  return result.error ? [] : result.data ?? []
}

export async function POST(request: Request) {
  const user = await requireAdmin(request)
  if (!user) return NextResponse.json({ error: 'Forbidden' }, { status: 403 })
  const admin = getAdminClient()
  const body = await request.json()

  if (body.action === 'overview') {
    const [redemptions, transactions, rewards, plays] = await Promise.all([
      safeQuery(admin.from('redemptions').select('id,user_id,reward,cost,destination,status,admin_note,created_at').order('created_at', { ascending: false }).limit(100)),
      safeQuery(admin.from('transactions').select('*').order('created_at', { ascending: false }).limit(100)),
      safeQuery(admin.from('rewards').select('*').limit(100)),
      safeQuery(admin.from('game_plays').select('*').order('created_at', { ascending: false }).limit(100)),
    ])
    const ids = [...new Set((redemptions as Array<{ user_id: string }>).map((row) => row.user_id))]
    const userMap: Record<string, { email: string; balance: number }> = {}
    await Promise.all(ids.map(async (id) => {
      const { data } = await admin.auth.admin.getUserById(id)
      const earned = await safeQuery(admin.from('transactions').select('amount').eq('user_id', id)) as Array<{ amount: number }>
      const spent = await safeQuery(admin.from('redemptions').select('cost,status').eq('user_id', id).neq('status', 'rejected')) as Array<{ cost: number }>
      userMap[id] = { email: data.user?.email ?? 'Unknown user', balance: Math.max(earned.reduce((sum, x) => sum + Number(x.amount || 0), 0) - spent.reduce((sum, x) => sum + Number(x.cost || 0), 0), 0) }
    }))
    const users = Object.entries(userMap).map(([id, value]) => ({ id, ...value }))
    return NextResponse.json({ data: { redemptions: (redemptions as Array<Record<string, unknown>>).map((row) => ({ ...row, user: userMap[row.user_id as string] })), transactions, rewards, plays, users } })
  }

  if (body.action === 'update') {
    const allowedStatuses = ['pending', 'approved', 'paid', 'rejected']
    if (typeof body.id !== 'string' || !allowedStatuses.includes(body.status)) return NextResponse.json({ error: 'Invalid request' }, { status: 400 })
    const note = typeof body.adminNote === 'string' ? body.adminNote.trim().slice(0, 1000) : null
    const { data, error } = await admin.from('redemptions').update({ status: body.status, admin_note: note }).eq('id', body.id).select('id,status,admin_note').single()
    return error ? NextResponse.json({ error: error.message }, { status: 500 }) : NextResponse.json({ data })
  }

  return NextResponse.json({ error: 'Unknown action' }, { status: 400 })
}

export const runtime = 'nodejs'
export const dynamic = 'force-dynamic'
