# Séf Bérlési Rendszer - Frontend

Modern webalkalmazás a bérelhető séfek böngészéséhez és kibérléséhez.

## Funkciók

✅ **Séfek listázása** - Valós idejű adatok a Firebase-ből  
✅ **Keresési funkcionalitás** - Szűrés név és konyhatípus alapján  
✅ **Bérlés leadása** - 3-14 napig kibérelhet séfeket  
✅ **Felhasználói visszajelzések** - Sikeres/sikertelen notifikációk  
✅ **Reszponzív design** - Desktop, tablet, mobil kompatibilis  
✅ **Modern UI** - Letisztult, ízléses megjelenés  

## Technológiák

- **HTML5**
- **CSS3** (Vanilla)
- **JavaScript (ES6+)**
- **Firebase** (adatok)
- **Fetch API** (kommunikáció)

## Projekt Struktura

```
.
├── index.html           # Fő HTML oldal
├── styles.css          # Összes CSS stílus
├── js/
│   ├── api.js          # API kommunikáció
│   ├── ui.js           # UI komponensek
│   ├── calendar.js     # Naptár és validáció
│   └── main.js         # Fő logika
├── .gitignore          # Git ignore fájl
└── README.md           # Ez a fájl
```

## Telepítés & Használat

1. **Klónozás:**
   ```bash
   git clone https://github.com/VLivia/szakmaivizsga20260522.git
   cd szakmaivizsga20260522
   ```

2. **Megnyitás böngészőben:**
   - Egyszerűen nyisd meg az `index.html` fájlt egy böngészőben, vagy
   - Használj egy live server-t (pl. VS Code Live Server kiterjesztés)

3. **API Integráció:**
   - Az alkalmazás automatikusan lekéri a séfek adatait a Firebase-ből
   - Backend bérlés leadásához konfiguráld a `BACKEND_URL`-t a `js/api.js`-ben

## Bérlési Szabályok

- **Minimum bérlési idő:** 3 nap
- **Maximum bérlési idő:** 14 nap
- **userId:** Demo célokra 101-es ID-vel küldi az adatokat

## Commit Történet

1. `Séfek listázása + API integráció` - HTML, CSS, API fetch
2. `Bérlés leadása - form, validáció, backend integráció`
3. `Felhasználói visszajelzések - enhanced alert rendszer`
4. `Keresési funkció - azonnali szűrés`
5. `Design finomítások - reszponzív, modern UI`

## Szerző

Készítette: [VLivia]  
Vizsga dátum: 2026. május 22.

---

Minden visszajelzés és fejlesztési ötlet üdvözlött! 🚀
