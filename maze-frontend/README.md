# maze-frontend

Frontend for the Maze backend, built with React + TypeScript + Vite.

## Features

- Create player/game via `POST /game`
- Get directional view via `GET /game/{playerId}/view`
- Move player via `POST /game/{playerId}/move` with `{ "direction": "NORTH|SOUTH|EAST|WEST" }`
- Get escaped status via `GET /game/{playerId}/status`
- Subscribe to `/topic/game-state` via STOMP/SockJS (`/ws`) for live updates
- Simple player/state UI

## Requirements

- Node.js 20+
- Running backend on `http://localhost:8080` (default)

## Setup

1. Install dependencies
2. Optional: set `VITE_API_BASE` in `.env.local`
3. Start dev server

## Commands

```bash
npm install
npm run dev
npm run test
npm run build
```

## Environment

Copy `.env.example` to `.env.local` if your backend runs on another host:

```bash
cp .env.example .env.local
```

Then edit `.env.local`:

```bash
VITE_API_BASE=http://localhost:8080
```

## Project structure

- `src/App.tsx` - main UI and game interactions
- `src/api.ts` - typed API client
- `src/types.ts` - shared frontend types
- `src/api.test.ts` - tiny test harness for API config

