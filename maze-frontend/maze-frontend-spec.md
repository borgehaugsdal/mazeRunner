# SPEC – Maze Challenge Frontend

## Rolle

Du er en erfaren frontend-utvikler som skal implementere brukergrensesnittet for spillet **"Maze Challenge"**.

Løsningen bygges med:

| Komponent       | Teknologi                              |
|-----------------|----------------------------------------|
| Språk           | TypeScript                             |
| Rammeverk       | React 18                               |
| Byggverktøy     | Vite                                   |
| WebSocket       | STOMP over SockJS (`@stomp/stompjs`)   |
| HTTP            | Native `fetch`                         |
| Styling         | CSS (én global `styles.css`)           |
| Testing         | Vitest + Testing Library               |
| Backend-URL     | `VITE_API_BASE` (default: `http://localhost:8080`) |

---

## Mål

Implementer en React-applikasjon som:

- Henter og tegner labyrinten fra backend
- Viser alle tilkoblede spillere som 🐭-ikoner i riktig farge
- Mottar sanntidsoppdateringer via WebSocket og oppdaterer visningen
- Viser spillerliste med navn, farge og posisjon
- Frontend applikasjonen skal ikke brukes til å registrere eller styre spillere.
- 
---

## TypeScript-typer (`types.ts`)

Typene speiler backend-responsene direkte:

```typescript
type Position = { first: number; second: number };  // first=x, second=y

type Player = {
  id: number;
  name: string;
  color: string;   // "red" | "blue" | "green" | "yellow" | "purple" | "orange"
  position: Position;
};

type Maze = {
  width: number;
  height: number;
  layout: string[];        // Én streng per rad: '#', ' ', 'S', 'E'
  startPosition: Position;
  exitPosition: Position;
};

type GameState = {
  players: Player[];
  maze: Maze | null;
  activePlayerId: number | null;
  escaped?: boolean;
};

type MoveDirection = "NORTH" | "SOUTH" | "EAST" | "WEST";
```

---

## API-klient (`api.ts`)

Alle kall bruker en felles `request<T>(path, init?)` hjelpefunksjon med `fetch`.  
Base-URL hentes fra `import.meta.env.VITE_API_BASE ?? "http://localhost:8080"`.

| Funksjon                                     | HTTP-kall                              | Beskrivelse                              |
|----------------------------------------------|----------------------------------------|------------------------------------------|
| `createPlayer(name?)`                        | `POST /game?name=<navn>`               | Returnerer `playerId` som `number`       |
| `getMaze()`                                  | `GET /game/maze`                       | Returnerer `Maze`                        |
| `getPlayers()`                               | `GET /gamecontrol/players`             | Returnerer `Player[]`                    |
| `getPlayerView(playerId)`                    | `GET /game/{playerId}/view`            | Returnerer synsfelt                      |
| `getGameStatus(playerId)`                    | `GET /game/{playerId}/status`          | Returnerer `{ escaped: boolean }`        |
| `movePlayer(playerId, direction)`            | `POST /game/{playerId}/move`           | Sender trekk, returnerer oppdatert state |
| `getGameState(activePlayerId)`               | Samler `getMaze` + `getPlayers` + evt. view/status | Returnerer `GameState`      |
| `startGame(name?)`                           | `createPlayer` → `getGameState`        | Starter spill og returnerer state        |
| `joinGame(name)`                             | `createPlayer` → `getGameState`        | Melder på spiller og returnerer state    |

---

## WebSocket (`websocket.ts`)

Bruker `@stomp/stompjs` med `SockJS` som transport.

**Tilkobling:**
```
ws://localhost:8080/ws   (via SockJS)
Topic: /topic/game-state
Reconnect-forsinkelse: 3000 ms
```

**Meldingstyper fra backend** – alle har feltet `event` som diskriminator:

| `event`          | Hva skjer                                  | Frontend-respons                     |
|------------------|--------------------------------------------|--------------------------------------|
| `PLAYER_CREATED` | Ny spiller registrert                      | Kall `refreshState()` (silent)       |
| `PLAYER_MOVED`   | Spiller har flyttet seg                    | Kall `refreshState()` (silent)       |
| `PLAYER_EXITED`  | Spiller har nådd utgangen                  | Kall `refreshState()` (silent)       |

`createGameSocket(baseUrl, handlers)` returnerer `{ connect(), disconnect() }`.  
`handlers` har: `onStatusChange(status)`, `onGameEvent(message)`, `onError(message)`.

WebSocket-status (`WsStatus`): `"connecting" | "connected" | "disconnected" | "error"`

---

## Komponentstruktur (`App.tsx`)

Én rot-komponent med lokal React-tilstand. Ingen ekstern state-manager.

### Tilstandsvariabler

| Variabel         | Type                   | Beskrivelse                                   |
|------------------|------------------------|-----------------------------------------------|
| `name`           | `string`               | Spillernavn fra input-felt (maks 10 tegn)     |
| `myPlayerId`     | `number \| null`       | ID til den lokale spilleren etter join/start  |
| `gameState`      | `GameState \| null`    | Komplett spilltilstand fra backend            |
| `isLoading`      | `boolean`              | Deaktiverer knapper under API-kall            |
| `error`          | `string \| null`       | Feilmelding til bruker                        |
| `info`           | `string \| null`       | Statusinformasjon til bruker                  |
| `wsStatus`       | `WsStatus`             | Visning av WebSocket-tilkobling               |

### Polling

State oppdateres hvert **500 ms** via `setInterval` → `refreshState(true)` (i tillegg til WebSocket-events).

### Brukerflyt

```
1. Bruker skriver inn navn og klikker "Start game" eller "Join"
2. Frontend kaller POST /game?name=<navn> og lagrer playerId
3. GameState lastes (maze + players + view + status)
4. Labyrinten tegnes. Spilleren vises som 🐭
5. Bruker klikker retningsknapper → POST /game/{playerId}/move
6. WebSocket-event eller polling oppdaterer visningen
```

---

## Labyrint-rendering

Labyrinten tegnes som et CSS Grid der hver celle er 24×24 px.  
`gridTemplateColumns` settes dynamisk til `repeat(<maze.width>, 24px)`.

### Celle-logikk (prioritert rekkefølge)

| Betingelse                        | CSS-klasse                        | Innhold     |
|-----------------------------------|-----------------------------------|-------------|
| Spiller på denne posisjonen       | `maze-cell player player-<farge>` | `🐭`        |
| Tegn `#`                          | `maze-cell wall`                  | (tom)       |
| Tegn `S`                          | `maze-cell start`                 | `START`     |
| Tegn `E`                          | `maze-cell goal`                  | `MÅL`       |
| Alt annet (gulv / mellomrom)      | `maze-cell floor`                 | (tom)       |

### Spillerfarger (CSS-klasser)

| Klasse              | Bakgrunnsfarge |
|---------------------|----------------|
| `.player-red`       | red            |
| `.player-blue`      | blue           |
| `.player-green`     | green          |
| `.player-yellow`    | yellow         |
| `.player-purple`    | purple         |
| `.player-orange`    | orange         |

---

## UI-seksjoner

### Header
- Applikasjonstittel
- Viser gjeldende backend-URL (`VITE_API_BASE`)
- Viser WebSocket-status
- Knapp: **Start game** – registrerer spiller og laster labyrint
- Knapp: **Oppdater** – manuell refresh av state

### Join game
- Input-felt for spillernavn (maxLength=10)
- Knapp: **Join** – registrerer spiller uten å laste ny labyrint
- Viser lokal spillers navn, ID og posisjon

### Labyrint
- Viser labyrint-grid når `gameState.maze` er lastet
- Viser feil-, info- og lastemelding over griddet
- Retningsknapper: **NORTH**, **SOUTH**, **EAST**, **WEST**

### Spillerliste
- Lister alle `gameState.players` med: `#<nr> <navn> (<farge>) @ <x>,<y>`

---

## Miljøvariabler

| Variabel        | Default                    | Beskrivelse              |
|-----------------|----------------------------|--------------------------|
| `VITE_API_BASE` | `http://localhost:8080`    | Backend base-URL         |

Sett i `.env.local` for lokal utvikling:
```
VITE_API_BASE=http://localhost:8080
```

---

## Tester

| Fil              | Hva testes                                                             |
|------------------|------------------------------------------------------------------------|
| `api.test.ts`    | `getGameState` kaller riktige endepunkter og bygger korrekt `GameState`|
| `api.test.ts`    | `movePlayer` sender riktig retning i request body                      |
| `api.test.ts`    | Feilhåndtering: kast `Error` ved ikke-OK HTTP-svar                     |

Bruk `vi.stubGlobal("fetch", ...)` for å mocke fetch i tester.  
**Mål:** Minimum 80 % dekning av `api.ts`.

### Kjøre tester
```bash
npm test
# Rapport: terminal-output fra Vitest
```

---

## Kjøring

```bash
# Installer avhengigheter
npm install

# Start dev-server (hot reload)
npm run dev        # → http://localhost:5173

# Bygg for produksjon
npm run build      # typecheck + vite build → dist/

# Forhåndsvis prod-bygg
npm run preview
```
