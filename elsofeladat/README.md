# Elso feladat - Sef statisztikai rendszer (Java konzol)

Ez a konzolos Java alkalmazas a `chef_berlesek_2025.csv` fajl alapjan statisztikakat keszit a Gourmet Moments 2025-os sefberleseirol.

## Fajlok

- `App.java` - fo program (beolvasas, statisztikak, kiiras)
- `Berles.java` - adatmodell es szamitott ertekek
- `chef_berlesek_2025.csv` - bemeneti adatfajl

## Forditas es futtatas (Windows / CMD)

Lepj be az elsofeladat mappaba, majd futtasd:

```cmd
javac App.java Berles.java
java App
```

## Program mukodes

A program bekerni egy honapot (1-12), majd kiirja:

1. Az adott honap bevetelet 
2. A teljes 2025-os eves bevetelt
3. A legdragabb berlest
4. A kulonbozo berelt sefek szamat
5. A legtobbszor berelt sefet
6. Berlesek szamat konyhatipusonkent
7. Az atlagos berlesi idotartamot napban

