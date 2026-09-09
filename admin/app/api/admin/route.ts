import { NextResponse } from 'next/server'
import { createClient } from '@supabase/supabase-js'

const allowedEmail = 'sramanan602@gmail.com'
const admin = createClient(process.env.SUPABASE_URL_2!, process.env.SUPABASE_SECRET_KEY_2!, { auth: { autoRefreshToken:false, persistSession:false } })

export async function POST(request: Request) {
  const body = await request.json()
  const token = request.headers.get('authorization')?.replace('Bearer ', '')
  if (!token) return NextResponse.json({ error:'Unauthorized' }, { status:401 })
  const { data: { user }, error } = await admin.auth.getUser(token)
  if (error || user?.email?.toLowerCase() !== allowedEmail) return NextResponse.json({ error:'Forbidden' }, { status:403 })
  if (body.action === 'list') {
    const { data, error: queryError } = await admin.from('redemptions').select('id,user_id,reward,cost,destination,status,created_at').order('created_at',{ ascending:false }).limit(100)
    return NextResponse.json({ data, error: queryError?.message })
  }
  if (body.action === 'update') {
    const allowed = ['pending','approved','paid','rejected']
    if (!allowed.includes(body.status)) return NextResponse.json({ error:'Invalid status' }, { status:400 })
    const { data, error: updateError } = await admin.from('redemptions').update({ status: body.status }).eq('id', body.id).select('id,status').single()
    return NextResponse.json({ data, error: updateError?.message })
  }
  return NextResponse.json({ error:'Unknown action' }, { status:400 })
}
