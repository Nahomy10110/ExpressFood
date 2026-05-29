# ExpressFood

Aplicación Android nativa de delivery — Proyecto Final del curso Diseño y Programación de Dispositivos Móviles, Universidad Nacional.

## Equipo
- Nahomy Jiménez
- Ian
- Isaac
- Luci
- Santi

## Stack
- Kotlin + Android (API 24+)
- Arquitectura MVVM + Repository
- Room (SQLite) como fuente de verdad local — **offline-first**
- Firebase Authentication (Google SSO) + Firestore como backup remoto
- WorkManager para sincronización
- JUnit + Mockito + Robolectric (cobertura ≥ 60%)
- CI/CD con GitHub Actions + SonarCloud

## Roles
- **Cliente**: cualquier usuario con cuenta @gmail.com.
- **Admin**: una única cuenta predefinida (UID hardcodeado en reglas de Firestore).

## Configuración local
1. Clonar el repositorio.
2. Solicitar `google-services.json` por canal privado y colocarlo en `app/google-services.json`.
3. Abrir en Android Studio Hedgehog o superior con JDK 17.
4. Sync Gradle y correr en dispositivo/emulador API 24+.
5. Obtener el SHA-1 propio con `./gradlew signingReport` y enviarlo a quien gestione Firebase.

## Build y tests
```bash
./gradlew testDebugUnitTest    # pruebas unitarias
./gradlew lintDebug            # análisis estático
./gradlew assembleDebug        # APK debug
./gradlew jacocoTestReport     # reporte de cobertura
./gradlew signingReport        # ver SHA-1 (para registrar en Firebase)
```

## CI/CD
Cada push y PR a `main`/`develop` dispara: lint, tests, JaCoCo, SonarCloud y build de APK firmado. El APK queda disponible como artifact descargable desde la pestaña Actions.

## Estado
🚧 En desarrollo — entrega 13 de junio 2026.