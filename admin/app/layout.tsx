import './globals.css'
import type { Metadata } from 'next'

export const metadata: Metadata = { title: 'L Rewards Admin', description: 'Manual rewards operations console' }

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en" className="bg-[#0b1220]"><body>{children}</body></html>
}
