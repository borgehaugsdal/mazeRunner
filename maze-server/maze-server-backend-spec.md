# SPEC – Maze Challenge Backend

## Rolle

Du er en erfaren Kotlin-utvikler som skal implementere backend for spillet **"Maze Challenge"**.

Løsningen bygges med:

| Komponent    | Teknologi                           |
|--------------|-------------------------------------|
| Språk        | Kotlin                              |
| Rammeverk    | Spring Boot 3                       |
| Byggesystem  | Gradle Kotlin DSL                   |
| Java-versjon | Java 21                             |
| API          | REST + SpringDoc (OpenAPI auto-gen) |
| Sanntid      | STOMP over WebSocket                |
| Lagring      | In-memory (ingen database)          |
| Labyrint     | `resources/Maze1.txt` (tekstfil)    |

---

## Spill-livssyklus

```
VENTER_PÅ_SPILLERE → PÅGÅR → AVSLUTTET
```

| Tilstand             | Beskrivelse                                                          |
|----------------------|----------------------------------------------------------------------|
| `VENTER_PÅ_SPILLERE` | Server er klar. Spillere kan registrere seg via `POST /game`.        |
| `PÅGÅR`              | Minst én spiller er registrert og beveger seg i labyrinten.          |
| `AVSLUTTET`          | Alle spillere har nådd utgangen, eller spillet er manuelt avsluttet. |

> **Merk:** Spillet har ingen eksplisitt start-knapp på serversiden. Spillet er aktivt fra første spiller melder seg på.
> Frontend håndterer 30-sekunders påmeldingsvindu selv.

---

## Domenemodell

### Labyrint

Lastes fra `resources/Maze1.txt`. Koordinatsystem:

- `(0, 0)` er øverste venstre hjørne
- `x` øker mot høyre, `y` øker nedover

| Tegn | Betydning                |
|------|--------------------------|
| `#`  | Vegg – kan ikke passeres |
| ` `  | Gulv – fri bevegelse     |
| `S`  | Startposisjon            |
| `E`  | Utgang (mål)             |

Alle spillere starter på `S`-posisjonen og kan stå på samme felt.

### Spiller

```kotlin
data class Player(
    val id: Int,          // Tildelt av server, starter på 1, teller opp
    val name: String,     // Maks 10 tegn
    var position: Pair<Int, Int>,  // (x, y) – starter på maze.startPosition
    val color: String     // Tildelt basert på spillernummer (se farger nedenfor)
)
```

### Farger (tildeles automatisk av server)

| Spillernummer (id % 6) | Farge      |
|------------------------|------------|
| 1                      | `"red"`    |
| 2                      | `"blue"`   |
| 3                      | `"green"`  |
| 4                      | `"yellow"` |
| 5                      | `"purple"` |
| 0                      | `"orange"` |

---

## Regler

- Spillernavn: maks 10 tegn. Avvis med `400 Bad Request` hvis lengre.
- Maks 6 spillere. Avvis med `409 Conflict` ved forsøk på å registrere spiller 7+.
- Spilleren kan flyttes ett steg om gangen i fire retninger: `NORTH`, `SOUTH`, `EAST`, `WEST`.
- Bevegelse gjennom vegg (`#`) er ikke tillatt – returner `{ "success": false }` uten feilmelding.
- Spillere kan passere hverandre (ingen blokkering mellom spillere).
- Synsfelt: stråle i fire retninger fra spillerens posisjon, maks **4 steg** per retning. Strålen stopper ved første
  vegg (veggen inkluderes i listen).
- Spiller markeres som `escaped = true` når posisjonen er lik `maze.exitPosition`.
- Fartsbegrensning håndheves **ikke** server-side. Klienten er ansvarlig for 500ms-paus mellom trekk.

---

## API-endepunkter

### `POST /game?name=<navn>`

Registrer ny spiller og returner tildelt spiller-ID.

**Request:** query-parameter `name` (valgfri, maks 10 tegn)  
**Response `200`:**

```json
{
  "playerId": "1"
}
```

**Response `400`:** navn for langt  
**Response `409`:** spillet er fullt (6 spillere)

Sender WebSocket-melding `PLAYER_CREATED` etter vellykket registrering.

---


### `GET /game/maze`

Hent labyrintdata (brukes av frontend for å tegne kartet).

**Response `200`:**

```json
{
  "width": 32,
  "height": 32,
  "layout": [
    "#E######...",
    "..."
  ],
  "startPosition": {
    "first": 31,
    "second": 31
  },
  "exitPosition": {
    "first": 1,
    "second": 0
  }
}
```

`layout` er en liste av strenger, én streng per rad, der hvert tegn er `#`, ` `, `S` eller `E`.

---

### `GET /game/{playerId}/view`

Hent synsfelt for spiller. Returnerer stråler i fire retninger fra spillerens posisjon.  
Hver stråle er en liste av enkelt-tegn-strenger fra nærmeste til fjerneste celle.  
Strålen inkluderer veggen den stopper på, og stopper etter veggen.

**Response `200`:**

```json
{
  "north": [
    " ",
    " ",
    "#"
  ],
  "south": [
    "E"
  ],
  "east": [
    " ",
    " ",
    " ",
    " "
  ],
  "west": [
    "#"
  ]
}
```

**Response `404`:** ugyldig `playerId`

---

### `POST /game/{playerId}/move`

Flytt spiller ett steg i angitt retning.

**Request body:**

```json
{
  "direction": "NORTH"
}
```

Gyldige verdier: `NORTH`, `SOUTH`, `EAST`, `WEST`

**Response `200` – vellykket trekk:**

```json
{
  "success": true
}
```

**Response `200` – vegg eller ugyldig retning:**

```json
{
  "success": false
}
```

**Response `404`:** ugyldig `playerId`

Sender WebSocket-melding `PLAYER_MOVED` etter vellykket trekk.  
Sender WebSocket-melding `PLAYER_EXITED` i tillegg hvis spilleren nådde `exitPosition`.

---

### `GET /game/{playerId}/status`

Sjekk om spiller har nådd utgangen.

**Response `200`:**

```json
{
  "escaped": false
}
```

**Response `404`:** ugyldig `playerId`

---

## WebSocket

**Protokoll:** STOMP over WebSocket  
**Endepunkt:** `ws://localhost:8080/ws`  
**Topic:** `/topic/game-state`

Alle meldinger sendes som JSON med feltet `event` som diskriminator.

### Melding: `PLAYER_CREATED`

```json
{
  "event": "PLAYER_CREATED",
  "playerId": 1,
  "playerName": "Alice",
  "position": {
    "first": 31,
    "second": 31
  }
}
```

### Melding: `PLAYER_MOVED`

```json
{
  "event": "PLAYER_MOVED",
  "playerId": 1,
  "playerName": "Alice",
  "position": {
    "first": 30,
    "second": 31
  }
}
```

### Melding: `PLAYER_EXITED`

```json
{
  "event": "PLAYER_EXITED",
  "playerId": 1,
  "playerName": "Alice",
  "position": {
    "first": 1,
    "second": 0
  }
}
```

---

## Arkitektur

Anbefalt pakkestruktur under `com.mazechallenge`:

```
controller/   – REST-kontrollere (@RestController)
service/      – Forretningslogikk (PlayerService, MazeService)
domain/       – Domeneobjekter (Player, Maze, GameState)
dto/          – Request/Response-objekter
mapper/       – Konvertering mellom domain og dto
config/       – WebSocket-konfigurasjon, CORS
websocket/    – WebSocket-konfigurasjon og event-sending
```

---

## Feilhåndtering

| Situasjon                   | HTTP-statuskode            |
|-----------------------------|----------------------------|
| Ugyldig `playerId`          | `404 Not Found`            |
| Navn lengre enn 10 tegn     | `400 Bad Request`          |
| Mer enn 6 spillere          | `409 Conflict`             |
| Ugyldig retning i move-kall | `200` med `success: false` |
| Bevegelse mot vegg          | `200` med `success: false` |

---

## Tester

Lag følgende tester:

| Type                   | Hva testes                                                       |
|------------------------|------------------------------------------------------------------|
| Unit – `MazeService`   | Parsing av `Maze.txt`, korrekt identifikasjon av `S` og `E`      |
| Unit – `PlayerService` | Registrering, fargetildeling, synsfelt-logikk, escaped-markering |
| Unit – `Maze`          | `isValidMove` med vegger og grenser                              |
| Integrasjon            | Full request/response for alle endepunkter via `MockMvc`         |
| Integrasjon            | WebSocket-meldinger sendes ved riktige hendelser                 |

**Mål:** Minimum 80 % kodedekning.
