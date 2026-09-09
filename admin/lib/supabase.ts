import { createBrowserClient } from '@supabase/ssr'

export function supabase() {
  return createBrowserClient(process.env.NEXT_PUBLIC_SUPABASE_URL_2!, process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY_2!)
}
