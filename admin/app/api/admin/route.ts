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
  if (error || user?.email?.toLowerCase() !== allowedEmail) return null
  return user
}

export async function POST(request: Request) {
  const user = await requireAdmin(request)
  if (!user) return NextResponse.json({ error: 'Forbidden' }, { status: 403 })
  const admin = getAdminClient()

  const body = await request.json()
  if (body.action === 'list') {
    const { data, error } = await admin
      .from('redemptions')
      .select('id,user_id,reward,cost,destination,status,admin_note,created_at')
      .order('created_at', { ascending: false })
      .limit(100)
    if (error) return NextResponse.json({ error: error.message }, { status: 500 })

    const userIds = [...new Set((data ?? []).map((row) => row.user_id))]
    const users = await Promise.all(userIds.map(async (userId) => {
      const { data: authUser } = await admin.auth.admin.getUserById(userId)
      const { data: earned } = await admin.from('transactions').select('amount').eq('user_id', userId)
      const { data: spent } = await admin.from('redemptions').select('cost,status').eq('user_id', userId).neq('status', 'rejected')
      const balance = (earned ?? []).reduce((sum, item) => sum + item.amount, 0) - (spent ?? []).reduce((sum, item) => sum + item.cost, 0)
      return [userId, { email: authUser.user?.email ?? 'Unknown user', balance: Math.max(balance, 0) }] as const
    }))
    return NextResponse.json({ data: data?.map((row) => ({ ...row, user: Object.fromEntries(users)[row.user_id] })) ?? [] })
  }

  if (body.action === 'update') {
    const allowedStatuses = ['pending', 'approved', 'paid', 'rejected']
    if (typeof body.id !== 'string' || !allowedStatuses.includes(body.status)) {
      return NextResponse.json({ error: 'Invalid request' }, { status: 400 })
    }
    const note = typeof body.adminNote === 'string' ? body.adminNote.trim().slice(0, 1000) : null
    const { data, error } = await admin
      .from('redemptions')
      .update({ status: body.status, admin_note: note })
      .eq('id', body.id)
      .select('id,status,admin_note')
      .single()
    if (error) return NextResponse.json({ error: error.message }, { status: 500 })
    return NextResponse.json({ data })
  }

  return NextResponse.json({ error: 'Unknown action' }, { status: 400 })
}

export const runtime = 'nodejs'
export const dynamic = 'force-dynamic'
