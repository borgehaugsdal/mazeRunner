# Maze Challenge Backend

Dette prosjektet er en backend-server for Maze Challenge, 100% Vibe kodet med Kotlin og Spring Boot.
Serveren gjør det mulig for flere spillere å koble seg til og navigere gjennom en labyrint i sanntid.
Serveren er kodet i Kotlin, men det er ingenting i veien for å lage en utgave i andre språk. 

## Kom i gang
Jo mer tydelig spesifikasjon er, jo færre tokens antas det at KI bruker på å generere koden.


### Forutsetninger

- Kotlin 1.6 eller nyere
- Java 21
- Gradle 7.0 eller nyere

### Installasjon

1. Klon repositoriet:
   ```bash
   git clone https://github.com/borgehaugsdal/mazeRunner/tree/main
   ```

2. Gå til prosjektmappen:
   ```bash
   cd maze-server
   ```

3. Bygg prosjektet med Gradle:
   ```bash
   ./gradlew build
   ```

4. Start applikasjonen:
   ```bash
   ./gradlew bootRun
   ```

## Bruk

- Koble til WebSocket-endepunktet for å bli med i spillet.
- Spillere kan registrere seg med et navn og får tildelt en unik identifikator.
- Spillere kan navigere gjennom labyrinten ved hjelp av kontrollene i frontend-applikasjonen.

## API-dokumentasjon

API-spesifikasjonene genereres automatisk ved hjelp av OpenAPI. Dokumentasjonen er tilgjengelig på `/v3/api-docs`.

## Testing

Prosjektet inneholder både enhetstester og integrasjonstester. Kjør testene med følgende kommando:

```bash
./gradlew test
```

## Bidrag

Bidrag er velkomne! Opprett gjerne en sak (issue) eller send inn en pull request dersom du har forslag til forbedringer eller feilrettinger.

## Lisens

Dette prosjektet er fullstendig åpent, og til læringsformål
