# FunRun 

 Android aplikacija za sledenje tekom, zgrajena s Kotlinom. FunRun uporablja GPS v realnem času za sledenje tekom, vizualizira tedenski napredek in shranjuje celotno zgodovino tekov lokalno v podatkovni bazi Room.

---
![Android](https://img.shields.io/badge/Platform-Android-brightgreen?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-blue)
![Target SDK](https://img.shields.io/badge/Target%20SDK-34-blue)
![Room](https://img.shields.io/badge/Database-Room-orange)
![Google Maps](https://img.shields.io/badge/Maps-Google%20Maps-red?logo=googlemaps)
![License](https://img.shields.io/badge/License-MIT-yellow)

## Posnetki zaslona

<p float="left">
  <img src="screenshots/scHome.png" width="200"/>
  <img src="screenshots/scMap1.png" width="200"/>
  <img src="screenshots/scDataScreen.png" width="200"/>
  <img src="screenshots/scSettingsScreen.png" width="200"/>
</p>

---

## Funkcionalnosti

###  Sledenje GPS v realnem času
- Neprekinjene posodobitve lokacije z uporabo **Fused Location Provider API**
- Risanje poti v živo na Google Zemljevidih s polilinijami
- Natančen izračun razdalje iz GPS koordinat s filtiranjem šuma (zavrne meritve z natančnostjo > 20 m in premiki < 1 m)


###  Domača nadzorna plošča
- Krožni obroč napredka tedenskega cilja s prikazom **preostalih km**
- Najboljši rezultati zadnjih 7 dni: najdaljša razdalja, najboljši tempo, najdaljše trajanje teka
- **Sledilnik serije** — šteje zaporedne dni z vsaj enim tekom
- **Tedenski aktivnostni koledar** — pas Pon–Ned s piko na dnevih s tekom in označenim današnjim dnem

###  Zgodovina tekov
- Celoten seznam tekov, razvrščen od najnovejšega, z razdaljo, tempom, trajanjem in datumom
- **Povleci levo za brisanje** kateregakoli teka z animiranim rdečim ozadjem
- Skupno število tekov in skupna razdalja prikazana na vrhu


###  Nastavitve
- Preklop med **temnim in svetlim načinom** — shranjuje se med sejami s SharedPreferences
- Nastavitev lastnega **tedenskega cilja razdalje** v km

---

## Tehnološki sklad

| Plast | Tehnologija |
|---|---|
| Jezik | Kotlin |
| Uporabniški vmesnik | XML postavitve, View Binding, Material 3 |
| Navigacija | Fragment Manager + spodnja navigacijska vrstica |
| Zemljevidi | Google Maps SDK, Fused Location Provider |
| Podatkovna baza | **Room (SQLite)** s KSP obdelavo anotacij |
| Nastavitve | SharedPreferences |
| Arhitektura | Ena aktivnost, več fragmentov |
| Gradnja | Gradle s KSP |

---


## Začetek uporabe(Developers)

### Namestitev

1. Kloniraj repozitorij:
   ```bash
   git clone  https://github.com/stanojab/FunRun.git

   ```

2. Dodaj Google Maps API ključ v `local.properties`:
   ```
   MAPS_API_KEY=tvoj_kljuc_tukaj
   ```
3. Sinhroniziraj Gradle in zaženi na fizični napravi ali emulatorju.

---

*Zgrajeno s Kotlinom · Android · Google Maps SDK · Room Database*
