# ChefExpenses Java (JavaFX)

Ez a projekt a II. feladat JavaFX verzioja.

## Futtatas

A projekt gyokerebol:

```cmd
set JAVAFX_LIB=C:\javafx-sdk\lib
javac --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -d out src\App.java
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -cp out App
```

Megjegyzes: a `JAVAFX_LIB` erteke a te JavaFX SDK `lib` mappad legyen.
Ha ez nincs beallitva vagy rossz utvonalra mutat, az alkalmazas nem indul el (`Module javafx.controls not found`).

## VS Code Start gomb

1. Nyisd meg a Run and Debug nezetet.
2. Valaszd ki: `Start App (JavaFX)`.
3. Kattints a Start (zold nyil) gombra.

Ehhez a projekt tartalmazza a beallitasokat a `.vscode/launch.json`, `.vscode/tasks.json` es `.vscode/settings.json` fajlokban.

## Fajl

A program a `chef_koltsegek_2025.csv` fajlt olvassa be indulaskor, es minden uj rogzites utan ujrairja a teljes listat ugyanebbe a fajlba.
