# maze-player – Oppgave

Din oppgave er å implementere en autonom bot som navigerer gjennom en labyrint ved å kommunisere med en kjørende
`maze-server`.

## Kom i gang

### 1. Aktiver Copilot-skillen (valgfritt, men anbefalt)

Kopier skill-filen slik at Copilot automatisk forstår oppgaven:

```bash
mkdir -p ~/.copilot/skills/maze-player
cp SKILL.md ~/.copilot/skills/maze-player/SKILL.md
```

### 2. Krav

- Java 21
- `./gradlew` (Gradle wrapper er inkludert)
- Kjørende `maze-server` på `http://localhost:8080` (får du av kursleder)

### 3. Kjøring

```bash
# Kjør tester
./gradlew test

# Kjør boten
./gradlew run --args='--name=MittNavn --backend=http://localhost:8080'
```

## Hva du skal implementere

Se `SKILL.md` for fullstendig beskrivelse. Kort oppsummert:

| Fil                       | Hva                                         |
|---------------------------|---------------------------------------------|
| `api/HttpMazeApi.kt`      | HTTP-kall til backend via openapi-spec.yaml |
| `player/MazeMemory.kt`    | Intern kartmodell og navigasjonslogikk      |
| `player/Pathfinder.kt`    | BFS-basert veifinner                        |
| `player/MazePlayer.kt`    | Game-loop som styrer boten                  |
| `player/*Test.kt`         | Tester med minst 80 % dekning               |

**Disse filene er allerede ferdig – ikke endre dem:**

- `Main.kt`, `api/MazeApi.kt`, `domain/Models.kt`



