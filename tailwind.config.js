/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        'dark-bg': '#0a0e1a',
        'dark-sidebar': '#0f1419',
        'dark-card': '#1a2332',
        'dark-border': '#1f2937',
        'accent-blue': '#2d4263',
        'accent-red': '#e74c3c',
        'accent-emerald': '#10b981',
        'accent-amber': '#f59e0b',
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
