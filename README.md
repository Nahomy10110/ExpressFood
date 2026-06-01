#ExpressFood

Aplicación Android nativa de delivery — Proyecto Final del curso Diseño y Programación de Dispositivos Móviles, Universidad Nacional de Costa Rica.

---

## Equipo

| Nombre                   | Rol principal |
|--------------------------|--------|
| Nahomy Gonzalez Jiménez  |Frontend|
| Ian Fabricio Hernandez   | Base de Datos |
| Isaac Aburto Torres      | Desarrollador |
| Luciana Chacon Castillo  | Scrum Master |
| Santiago Benavides Arana | QA/Tester |

---


## Funcionalidades implementadas

### Rol Cliente
- Login con Google SSO — solo cuentas @gmail.com

- Menú con RecyclerView, búsqueda por nombre e ingrediente y filtros por categoría

- Popup de detalle del producto con ingredientes, tiempo estimado y selector de cantidad

- Carrito persistente con cálculo automático de impuestos (13%) y total

- Historial de órdenes con filtros por estado y popup de detalle

- Reportes de gastos agrupados por día con total mensual

### Rol Administrador
- Panel de gestión de todas las órdenes del sistema

- Cambio de estado con máquina de estados: PENDIENTE → EN_CAMINO → ENTREGADA

- Filtros por estado de orden

- Reportes de ventas agrupados por día con total mensual

---

## Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Kotlin |
| IDE | Android Studio Hedgehog 2023.1.1+ |
| SDK mínimo | API 24 (Android 7.0) |
| Autenticación | Firebase Authentication + Google Sign-In (Credential Manager API) |
| Base local | Room (SQLite) — offline-first, fuente de verdad |
| Base remota | Firebase Firestore — backup remoto |
| Imágenes | Glide + Cloudinary |
| Arquitectura | MVVM + Repository Pattern |
| UI | Material Design 3, RecyclerView, ViewBinding, Navigation Component |
| Sincronización | WorkManager + Coroutines |
| Testing | JUnit 4 + Mockito + Robolectric — cobertura 85% |
| CI/CD | GitHub Actions |
| Build | Gradle 8.10.2 con Kotlin DSL |

---

## Arquitectura

```
UI (Activities / Fragments)
        ↕  StateFlow / LiveData
    ViewModels
        ↕
    Repositories
        ↕                   ↕
Room (SQLite)           Firestore
fuente de verdad        backup remoto
```

**Offline-first:** Room es la única fuente de verdad inmediata. Las órdenes se crean localmente con `synced=false` y se sincronizan a Firestore mediante WorkManager cuando hay conectividad. Los errores de sincronización fallan silenciosamente — el usuario nunca ve un error por falta de red.

---

## Roles

- **Cliente** — cualquier cuenta @gmail.com puede iniciar sesión como cliente.

- **Admin** — una única cuenta predefinida identificada por UID hardcodeado. No existe sistema de gestión de roles en la UI.

---

## Configuración local

1. Clonar el repositorio:
```bash
git clone https://github.com/Nahomy10110/ExpressFood.git
cd ExpressFood
```

2. Solicitar `google-services.json` a Nahomy por canal privado y colocarlo en:
```
app/google-services.json
```

3. Abrir en **Android Studio Hedgehog (2023.1.1) o superior** con JDK 17.

4. Sincronizar Gradle y esperar que termine.

5. Obtener el SHA-1 del keystore de debug y enviarlo a Nahomy para registrarlo en Firebase:
```bash
./gradlew signingReport
```

6. Verificar que compila:
```bash
./gradlew assembleDebug
```

---

## Build y tests

```bash
# Compilar APK debug
./gradlew assembleDebug

# Pruebas unitarias
./gradlew testDebugUnitTest

# Análisis estático
./gradlew lintDebug

# Reporte de cobertura (abre en app/build/reports/jacoco/.../index.html)
./gradlew testDebugUnitTest jacocoTestReport

# Ver SHA-1 del keystore (para registrar en Firebase)
./gradlew signingReport
```

**Cobertura actual: 85%** sobre clases con lógica de dominio.

| Paquete | Cobertura |
|---|---|
| `domain.model` | 80% |
| `domain.usecase` | 98% |
| **Total** | **85%** |

---

## CI/CD

Cada push y PR a `main` / `develop` dispara automáticamente:

1. **Lint** — análisis estático del código
2. **Unit tests** — pruebas unitarias con JUnit + Robolectric
3. **JaCoCo** — reporte de cobertura
4. **Build APK** — generación del APK release firmado con el keystore

El APK firmado queda disponible como artifact descargable desde la pestaña **Actions** de GitHub.

Para publicar una release:
```bash
git tag v1.0.0
git push origin v1.0.0
```
GitHub Actions detecta el tag y publica el APK automáticamente en la pestaña **Releases**.

---

## Estructura del proyecto

```
app/src/main/java/cr/una/expressfood/
├── data/
│   ├── local/           # Room: entidades, DAOs, AppDatabase, Converters
│   ├── remote/          # Firestore: fuente remota
│   ├── repository/      # AuthRepository, ProductRepository, OrderRepository, CartRepository
│   └── sync/            # SyncWorker, ConnectivityObserver
├── domain/
│   ├── model/           # User, Product, Order, OrderItem, CartItem + mapeos
│   └── usecase/         # CalculateTotalUseCase, ReportUseCase
├── ui/
│   ├── login/           # LoginActivity, LoginViewModel
│   ├── client/
│   │   ├── menu/        # MenuFragment, MenuViewModel, MenuAdapter, ProductFilter
│   │   ├── cart/        # CartFragment, CartViewModel, CartAdapter
│   │   ├── orders/      # MyOrdersFragment, MyOrdersViewModel, MyOrdersAdapter
│   │   └── reports/     # ClientReportsFragment, ClientReportsViewModel
│   └── admin/
│       ├── orders/      # AdminOrdersFragment, AdminOrdersViewModel, AdminOrdersAdapter
│       └── reports/     # AdminReportsFragment, AdminReportsViewModel
└── util/                # Constants, Result
```

---

##  Flujo de Git



| Rama | Uso |
|---|---|
| `main` | Código en producción, siempre estable |
| `develop` | Rama de integración — todas las features van aquí |
| `feature/*` | Una rama por funcionalidad |

Convención de commits: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`

---
*Universidad Nacional — Curso de Diseño y Programación de Dispositivos Móviles — Prof. Darin Mauricio Gamboa — 2026*
