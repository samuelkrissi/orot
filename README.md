# ביאורי אורות

Application Android qui affiche **livre par livre** tout le contenu de
[ביאורי אורות](https://yhb.org.il/b-orot/) — les commentaires du rav Ze’ev
Sultanovich sur les écrits du Rav Kook, publiés par Yeshivat Har Bracha.

Le texte n’est pas recopié dans le dépôt : l’application le charge en direct
depuis l’API WordPress du site, puis le met en cache pour la relecture hors-ligne.

## Livres

1. אורות ישראל
2. אורות התשובה
3. מידות הראי״ה
4. אורות התחיה
5. אורות מאופל
6. למהלך האידיאות בישראל
7. זרעונים
8. אורות הקודש
9. עקבי הצאן

## Fonctionnalités

- Accueil RTL avec les 9 livres et leurs couvertures officielles
- Sommaire par chapitre et par פסקה
- Lecteur hébreu (nikud, citations, taille de police)
- Navigation פסקה précédente / suivante
- Recherche dans un livre ou dans toute la série
- Reprise de la dernière lecture
- Cache local des sommaires et des pages déjà ouvertes

## Compiler l’application

1. Installer [Android Studio](https://developer.android.com/studio) (JDK 17+, SDK 35).
2. Ouvrir ce dossier.
3. Lancer le module `app` sur un téléphone ou un émulateur.

En ligne de commande :

```bash
./gradlew :app:assembleDebug
```

## Tests (sans SDK Android)

```bash
./gradlew :core:test -PskipAndroid=true
```

## Attribution

Contenu © Yeshivat Har Bracha / הרב זאב סולטנוביץ'.
Cette application est un lecteur non officiel du site public
https://yhb.org.il/b-orot/
