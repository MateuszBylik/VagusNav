# 🗺️ VagusNav
**An adaptive, AI-powered navigation system that learns your paths.**

VagusNav to inteligentna aplikacja nawigacyjna stworzona w celu rozwiązania problemów standardowych map (Google Maps), takich jak brak aktualnych przejść dla pieszych, nieoptymalne trasy miejskie oraz niedokładne dane o komunikacji miejskiej. 

Projekt łączy dane z OpenStreetMap (OSM) z autorskim systemem uczenia się nawyków użytkownika i integracją danych z systemów SIP (System Informacji Pasażerskiej) w czasie rzeczywistym.

## 🚀 Kluczowe funkcjonalności

* **Offline Routing:** Wytyczanie tras bezpośrednio na urządzeniu przy użyciu silnika GraphHopper i danych OSM.
* **AI Path Learning:** Analiza śladów GPS w celu wykrywania "dzikich" przejść i skrótów, które nie istnieją w oficjalnych danych mapowych.
* **Custom Map Overlays:** Możliwość ręcznego oznaczania przejść i modyfikacji grafu drogowego przez użytkownika.
* **Live Public Transport:** Integracja z systemami SIP (aktualnie: Toruń GMV) w celu pozyskiwania rzeczywistych czasów odjazdów.
* **Privacy First:** Wszystkie dane o lokalizacji i wyuczone ścieżki są przechowywane lokalnie lub na prywatnym serwerze użytkownika.

## 🛠️ Stack Techniczny

### Mobile (Android Studio)
- **Language:** Kotlin
- **Map Engine:** [MapLibre GL Native](https://maplibre.org/) (Vector Tiles)
- **Routing Engine:** [GraphHopper](https://www.graphhopper.com/) (Offline Mode)
- **Local DB:** SQLite / Room (Przechowywanie śladów GPS i poprawek)

### Backend
- **OS:** Debian 13 (Trixie)
- **API:** FastAPI (Python) - mostek dla danych Real-time SIP
- **Processing:** BeautifulSoup4 (Scraping danych z systemów miejskich)
- **Storage:** RAM-disk (tmpfs) dla minimalizacji opóźnień i ochrony dysku

## 🏗️ Architektura Systemu

1. **Client (Android):** Rejestruje punkty GPS i wysyła je do analizy. Pobiera statyczny graf (OSM + GTFS) do nawigacji offline.
2. **Analysis Engine:** Skrypty Python analizują anomalie w ruchu (skróty) i generują "łatki" (Custom Models) dla GraphHoppera.
3. **Live Bridge:** Backend na serwerze Fujitsu odpytuje systemy miejskie i serwuje lekkie paczki JSON z opóźnieniami autobusów.

## 🗺️ Roadmap projektu

- [x] Konfiguracja serwera backendowego (FastAPI + Scraping)
- [ ] Implementacja MapLibre w Android Studio
- [ ] Kompilacja grafu GraphHopper dla Torunia (OSM + GTFS)
- [ ] System logowania śladów GPS do lokalnej bazy
- [ ] Moduł "AI Learning" - automatyczna modyfikacja wag krawędzi grafu
- [ ] Skalowanie na kolejne regiony

---
**Autor:** Mateusz Bylica (Comeet)  
**Status:** In Development
