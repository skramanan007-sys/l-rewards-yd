import { NextResponse } from 'next/server'
import { createClient, type User } from '@supabase/supabase-js'

function getAdminClient() {
  const url = process.env.SUPABASE_URL_2
  const key = process.env.SUPABASE_SECRET_KEY_2
  if (!url || !key) throw new Error('Supabase admin environment is not configured')
  return createClient(url, key, { auth: { autoRefreshToken: false, persistSession: false } })
}

function allowedEmails() {
  return (process.env.ADMIN_EMAILS ?? 'sramanan602@gmail.com').split(',').map((email) => email.trim().toLowerCase()).filter(Boolean)
}

async function requireAdmin(request: Request): Promise<{ client: ReturnType<typeof getAdminClient>; user: User } | null> {
  const token = request.headers.get('authorization')?.replace(/^Bearer\s+/i, '')
  if (!token) return null
  const client = getAdminClient()
  const { data, error } = await client.auth.getUser(token)
  if (error || !data.user || !allowedEmails().includes(data.user.email?.toLowerCase() ?? '')) return null
  return { client, user: data.user }
}

async function readJson(request: Request) {
  try { return await request.json() as Record<string, unknown> } catch { return {} }
}

export async function POST(request: Request) {
  const auth = await requireAdmin(request)
  if (!auth) return NextResponse.json({ error: 'Admin access required' }, { status: 403 })
  const { client } = auth
  const body = await readJson(request)

  if (body.action === 'overview') {
    const [redemptionsResult, transactionsResult, adjustmentsResult, rewardsResult, playsResult, usersResult] = await Promise.all([
      client.from('redemptions').select('id,user_id,reward,cost,destination,status,admin_note,created_at').order('created_at', { ascending: false }).limit(500),
      client.from('transactions').select('id,user_id,game_type,amount,created_at').order('created_at', { ascending: false }).limit(500),
      client.from('balance_adjustments').select('id,user_id,amount,note,created_at').order('created_at', { ascending: false }).limit(500),
      client.from('rewards').select('id,type,label,cost,description,active,created_at').order('type').order('cost'),
      client.from('game_plays').select('id,user_id,game_type,reward_amount,created_at').order('created_at', { ascending: false }).limit(500),
      client.auth.admin.listUsers({ page: 1, perPage: 1000 }),
    ])
    const errors = [redemptionsResult.error, transactionsResult.error, adjustmentsResult.error, rewardsResult.error, playsResult.error, usersResult.error].filter(Boolean)
    if (errors.length) return NextResponse.json({ error: errors[0]?.message }, { status: 500 })
    const users = usersResult.data.users
    const transactions = transactionsResult.data ?? []
    const adjustments = adjustmentsResult.data ?? []
    const redemptions = redemptionsResult.data ?? []
    const plays = playsResult.data ?? []
    const userMap = new Map(users.map((user) => [user.id, { id: user.id, email: user.email ?? 'Unknown', created_at: user.created_at, last_sign_in_at: user.last_sign_in_at, confirmed: Boolean(user.email_confirmed_at), balance: 0, transactions: 0, plays: 0, redemptions: 0 }]))
    for (const row of transactions) { const user = userMap.get(row.user_id); if (user) { user.balance += Number(row.amount); user.transactions += 1 } }
    for (const row of adjustments) { const user = userMap.get(row.user_id); if (user) user.balance += Number(row.amount) }
    for (const row of redemptions) { const user = userMap.get(row.user_id); if (user) { if (row.status !== 'rejected') user.balance -= Number(row.cost); user.redemptions += 1 } }
    for (const row of plays) { const user = userMap.get(row.user_id); if (user) user.plays += 1 }
    const decorate = (row: { user_id: string }) => ({ ...row, user_email: userMap.get(row.user_id)?.email ?? 'Unknown user' })
    return NextResponse.json({ data: { users: [...userMap.values()].map((user) => ({ ...user, balance: Math.max(user.balance, 0) })), transactions: transactions.map(decorate), plays: plays.map(decorate), redemptions: redemptions.map(decorate), rewards: rewardsResult.data ?? [] } })
  }

  if (body.action === 'update_withdrawal') {
    const id = typeof body.id === 'string' ? body.id : ''
    const status = body.status
    if (!id || !['pending', 'approved', 'paid', 'rejected'].includes(String(status))) return NextResponse.json({ error: 'Invalid withdrawal update' }, { status: 400 })
    const adminNote = typeof body.adminNote === 'string' ? body.adminNote.trim().slice(0, 1000) : null
    const { data, error } = await client.from('redemptions').update({ status, admin_note: adminNote }).eq('id', id).select('id,status,admin_note').single()
    return error ? NextResponse.json({ error: error.message }, { status: 500 }) : NextResponse.json({ data })
  }

  if (body.action === 'adjust_balance') {
    const userId = typeof body.userId === 'string' ? body.userId : ''
    const amount = Number(body.amount)
    const note = typeof body.note === 'string' ? body.note.trim().slice(0, 500) : ''
    if (!userId || !Number.isInteger(amount) || amount === 0 || Math.abs(amount) > 1000000 || !note) return NextResponse.json({ error: 'Enter a non-zero whole coin amount and note.' }, { status: 400 })
    const { error } = await client.from('balance_adjustments').insert({ user_id: userId, amount, note })
    return error ? NextResponse.json({ error: error.message }, { status: 500 }) : NextResponse.json({ data: { userId, amount, note } })
  }

  if (body.action === 'update_reward') {
    const id = typeof body.id === 'string' ? body.id : ''
    const active = body.active
    if (!id || typeof active !== 'boolean') return NextResponse.json({ error: 'Invalid reward update' }, { status: 400 })
    const { data, error } = await client.from('rewards').update({ active }).eq('id', id).select('id,active').single()
    return error ? NextResponse.json({ error: error.message }, { status: 500 }) : NextResponse.json({ data })
  }

  return NextResponse.json({ error: 'Unknown action' }, { status: 400 })
}

export const runtime = 'nodejs'
export const dynamic = 'force-dynamic'
