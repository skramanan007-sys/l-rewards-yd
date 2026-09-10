import { createBrowserClient } from '@supabase/ssr'

export function supabase() {
  const url = process.env.NEXT_PUBLIC_SUPABASE_URL ?? process.env.SUPABASE_URL_2
  const publishableKey = process.env.NEXT_PUBLIC_SUPABASE_PUBLISHABLE_KEY ?? process.env.SUPABASE_PUBLISHABLE_KEY_2

  if (!url || !publishableKey) {
    throw new Error('Supabase environment variables are not configured.')
  }

  return createBrowserClient(url, publishableKey)
}
