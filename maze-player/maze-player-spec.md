# Maze Player – Funksjonell spesifikasjon

## Formål

Implementer en autonom spiller («maze-player») som navigerer gjennom en labyrint ved hjelp av et API definert i `openapi-spec.yaml` i maze-server.

Løs en og en oppgave.

# Oppgaver
## Oppgave 1
Bruk API´et i `openapi-spec.yaml` til å opprette en spiller i backend.
Når en spiller er registrert vises navnet i maze-frontend

## Oppgave 2
Bruk API´et til å flytte spilleren rundt i labyrinten.
Hvert trekk gir tilbake informasjon om hvilke retninger som er åpne

## Oppgave 3
Start automatisk utforsking av labyrinten.
Bygg opp et internt kart over oppdagede områder i labyrinten.

## Oppgave 4
Send via API´et for hvert trekk, informasjon om alle områder som fortløppende er avdekket i labyrinten.

## Oppgave 5
Finn labyrintens MÅL.

## Oppgave 6
Når MÅL er funnet, gå raskeste vei tilbake til START poisjonen
Den som har funnet kortest vei mellom START og MÅL har vunnet spillet

---

# Hovedkomponenter

## API-klient

Ansvar:

* Kommunikasjon med maze-serveren.
* Opprettelse av spiller.
* Henting av synsfelt.
* Utførelse av trekk.
* Henting av spillerstatus.

Komponenten skal ikke inneholde navigasjonslogikk eller kartdata.

---

## Kartminne

Ansvar:

* Huske startposisjon.
* Holde rede på spillerens nåværende posisjon.
* Huske hvilke posisjoner som er besøkt.
* Holde oversikt over alle oppdagede felt.
* Huske målposisjon dersom denne er oppdaget.

Komponenten skal ikke kommunisere direkte med maze-server-backend.

---

## Ruteberegning

Ansvar:

* Beregne korteste vei mellom to kjente posisjoner.

Krav:

* Bredde-først-søk (BFS) skal benyttes.
* Samme karttilstand skal alltid gi samme resultat.
* Korteste gyldige rute skal velges.

---

## Navigasjonsstrategi

Ansvar:

* Velge neste trekk.

Prioritet:

1. Utforske ukjente områder.
2. Navigere mot kjent målposisjon.
3. Navigere mot nærmeste frontier.
4. Velge annen gyldig retning dersom ingen av de ovennevnte er tilgjengelige.

Definisjon:

En frontier er et kjent gangbart felt som grenser til minst ett ukjent felt.

---

## Spillmotor

Ansvar:

Koordinere hele spillflyten.

Arbeidsflyt:

1. Hent synsfelt.
2. Oppdater kartet.
3. Beregn neste trekk.
4. Utfør trekket.
5. Oppdater intern tilstand.
6. Vent konfigurert tidsintervall.
7. Gjenta til utgangen er nådd.
8. Returner til startposisjonen.

---

# Kartlegging av labyrinten

Startposisjonen skal behandles som koordinat:

* x = 0
* y = 0

Symboler fra backend skal tolkes slik:

| Symbol    | Betydning     |
| --------- | ------------- |
| #         | Vegg          |
| F         | Gangbart felt |
| mellomrom | Gangbart felt |
| S         | Start         |
| E         | Utgang        |

Felt som ennå ikke er observert skal behandles som ukjente.

---

# Navigasjonsregler

* Spilleren skal aldri forsøke å gå inn i en kjent vegg.
* Når utgangen er kjent, skal korteste vei til utgangen beregnes.
* Når utgangen ikke er kjent, skal nærmeste frontier oppsøkes.
* Når utgangen er nådd, skal korteste vei tilbake til startposisjonen beregnes og følges.

Spilleren skal prioritere utforsking fremfor å returnere til tidligere besøkte områder.

---

# Konfigurasjon

Løsningen skal støtte følgende konfigurasjon:

* Spillernavn, maks 10 tegn
* Adresse til backend

# Feilhåndtering

Nettverksfeil:

* Operasjonen skal forsøkes på nytt et begrenset antall ganger.
* Ventetiden mellom forsøk skal øke for hvert forsøk.

Ugyldige svar:

* Feilen skal logges.
* Programmet skal avsluttes kontrollert.

Mislykket trekk:

* Destinasjonen skal behandles som en vegg.
* Ny rute skal beregnes.

---

# Testkrav

Det skal verifiseres at:

* Synsfelt tolkes korrekt.
* Kartet oppdateres korrekt.
* Vegger registreres korrekt.
* Utgangen registreres korrekt.
* Korteste vei beregnes korrekt.
* Utilgjengelige mål håndteres korrekt.
* Utforsking prioriteres foran revisitering.
* Utgangen prioriteres når den er kjent.
* Spilleren stopper når målet er nådd.
* Spilleren stopper når maksimal grense for trekk er nådd.
* Midlertidige kommunikasjonsfeil håndteres korrekt.

Tester skal ikke være avhengige av en kjørende backend.

---

# Akseptansekriterier

Løsningen anses som ferdig når:

* Alle definerte tester består.
* Spilleren kan løse ukjente labyrinter.
* Spilleren unngår uendelige løkker.
* Spilleren finner utgangen når en gyldig rute eksisterer.
* Spilleren returnerer til startposisjonen etter å ha nådd utgangen.
* Løsningen kan bygges, testes og kjøres i valgt teknologi uten feil.
