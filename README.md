# 🛒 EcommerceApp - Android

Una aplicación móvil completa de comercio electrónico desarrollada en Android con Jetpack Compose, que incluye funcionalidades tanto para usuarios finales como para administradores.

## 📱 Características Principales

### Para Usuarios
- **🔐 Autenticación completa** - Registro e inicio de sesión
- **📦 Catálogo de productos** - Navegación, búsqueda y filtros avanzados
- **🛍️ Carrito de compras** - Agregar, modificar y eliminar productos
- **💳 Sistema de checkout** - Confirmación y procesamiento de pedidos
- **📋 Historial de pedidos** - Visualización detallada de compras anteriores
- **👤 Gestión de perfil** - Edición de datos personales y cambio de contraseña

### Para Administradores
- **👥 Gestión de usuarios** - Administración de usuarios y roles
- **🏷️ Gestión de categorías** - CRUD completo de categorías
- **📦 Gestión de productos** - CRUD de productos, precios, stock e imágenes
- **🌍 Gestión de ubicaciones** - Administración de países, estados y ciudades
- **📊 Panel administrativo** - Acceso a todas las funcionalidades administrativas

## 🏗️ Arquitectura y Tecnologías

### Arquitectura
- **MVVM (Model-View-ViewModel)** - Patrón de arquitectura principal
- **Repository Pattern** - Separación de lógica de datos
- **Reactive Programming** - Estados reactivos con Compose

### Stack Tecnológico
- **Kotlin** - Lenguaje principal
- **Jetpack Compose** - UI moderna y declarativa
- **Navigation Compose** - Navegación entre pantallas
- **ViewModel & LiveData** - Gestión de estados
- **Retrofit** - Cliente HTTP para API REST
- **Gson** - Serialización/deserialización JSON
- **DataStore** - Almacenamiento de preferencias y sesión
- **OkHttp** - Cliente HTTP con interceptores de logging
- **Material Design 3** - Sistema de diseño

## 📁 Estructura del Proyecto

```
app/src/main/java/com/ecommerce/ecommerceapp/
├── models/                 # Modelos de datos y DTOs
│   ├── AuthResponse.kt
│   ├── Cart.kt
│   ├── Product.kt
│   ├── User.kt
│   └── ...
├── network/               # Configuración de red y API
│   ├── ApiClient.kt
│   └── ApiService.kt
├── ui/
│   ├── components/        # Componentes reutilizables
│   ├── screens/          # Pantallas principales
│   └── theme/            # Configuración de tema
├── utils/                # Utilidades
│   └── SessionManager.kt
├── viewmodels/           # ViewModels para cada pantalla
└── MainActivity.kt       # Actividad principal
```

## 🚀 Configuración e Instalación

### Prerrequisitos
- Android Studio Hedgehog (2023.1.1) o superior
- JDK 11 o superior
- SDK de Android 24+ (compileSdk 35)
- Servidor backend corriendo en puerto 5050

### Instalación

1. **Clonar el repositorio**
   ```bash
   git clone <repository-url>
   cd EcommerceApp
   ```

2. **Configurar el servidor API**
    - Asegúrate de que tu backend esté corriendo en `http://localhost:5050`
    - Para emulador Android: `http://10.0.2.2:5050`
    - Para dispositivo físico: `http://[TU_IP]:5050`

3. **Configurar la URL del servidor**
   ```kotlin
   // En ApiClient.kt
   private const val BASE_URL = "http://10.0.2.2:5050/"
   ```

4. **Compilar y ejecutar**
   ```bash
   ./gradlew clean build
   ./gradlew installDebug
   ```

## 📖 Uso de la Aplicación

### Credenciales por Defecto
- **Email:** `admin@ecommerce.com`
- **Contraseña:** `admin123`

### Flujo de Usuario

1. **Acceso libre al catálogo** - Los usuarios anónimos pueden navegar productos
2. **Registro/Login** - Crear cuenta o iniciar sesión para comprar
3. **Agregar al carrito** - Solo usuarios autenticados
4. **Realizar pedido** - Proceso de checkout con confirmación
5. **Gestión de perfil** - Editar datos personales

### Flujo de Administrador

1. **Login con credenciales de admin**
2. **Panel administrativo** - Acceso a todas las herramientas
3. **Gestión de productos** - CRUD completo
4. **Gestión de usuarios** - Administrar roles y permisos
5. **Gestión de ubicaciones** - Países, estados y ciudades

## 🔌 API Endpoints

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar usuario
- `PUT /api/auth/change-password` - Cambiar contraseña

### Productos
- `GET /api/products/` - Listar productos (con paginación y filtros)
- `GET /api/products/{id}` - Obtener producto específico
- `GET /api/products/featured` - Productos destacados
- `POST /api/products/` - Crear producto (Admin)
- `PUT /api/products/{id}` - Actualizar producto (Admin)
- `DELETE /api/products/{id}` - Eliminar producto (Admin)

### Carrito y Ventas
- `GET /api/sales/cart` - Obtener carrito actual
- `POST /api/sales/cart/add` - Agregar producto al carrito
- `PUT /api/sales/cart/update/{id}` - Actualizar cantidad
- `DELETE /api/sales/cart/remove/{id}` - Eliminar del carrito
- `POST /api/sales/checkout` - Procesar pedido
- `GET /api/sales/` - Historial de pedidos

### Administración
- `GET /api/users/` - Listar usuarios (Admin)
- `GET /api/categories/` - Listar categorías
- `POST /api/categories/` - Crear categoría (Admin)
- `GET /api/locations/countries` - Listar países

## 📱 Pantallas Principales

### Pantallas Públicas
- **ProductCatalogScreen** - Catálogo con búsqueda y filtros
- **LoginScreen** - Inicio de sesión
- **RegisterScreen** - Registro de usuarios

### Pantallas de Usuario
- **WelcomeScreen** - Dashboard principal del usuario
- **CartScreen** - Gestión del carrito de compras
- **ProfileScreen** - Edición de perfil
- **SalesHistoryScreen** - Historial de pedidos
- **OrderSuccessScreen** - Confirmación de pedido

### Pantallas de Admin
- **UserManagementScreen** - Gestión de usuarios
- **ProductManagementScreen** - CRUD de productos
- **CategoryManagementScreen** - CRUD de categorías
- **LocationManagementScreen** - Gestión de ubicaciones

## 🎨 Características de UI/UX

- **Material Design 3** - Diseño moderno y consistente
- **Tema personalizable** - Colores y tipografías adaptables
- **Responsive Design** - Adaptable a diferentes tamaños de pantalla
- **Estados de carga** - Indicadores visuales durante operaciones
- **Manejo de errores** - Mensajes informativos para el usuario
- **Navegación intuitiva** - Flujo de navegación lógico y accesible

## 🔧 Configuración Avanzada

### Network Security
```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

### Permisos Requeridos
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🐛 Troubleshooting

### Problemas Comunes

1. **Error de conexión a la API**
    - Verificar que el servidor backend esté ejecutándose
    - Comprobar la URL en `ApiClient.kt`
    - Revisar configuración de red en el emulador

2. **Problemas de autenticación**
    - Limpiar datos de la app
    - Verificar formato del token en las cabeceras
    - Comprobar expiración de sesión

3. **Errores de deserialización JSON**
    - Verificar que los modelos coincidan con la respuesta de la API
    - Revisar anotaciones `@SerializedName`

4. **Carrito vacío después de agregar productos**
    - Verificar mapeo de campos en el modelo `Cart`
    - Comprobar logs de debug del `CartViewModel`

## 🔄 Estados de la Aplicación

### Gestión de Sesión
- **SessionManager** utiliza DataStore para persistencia
- **Tokens JWT** para autenticación con la API
- **Auto-logout** en caso de token expirado

### Estados Reactivos
- Todos los ViewModels implementan estados reactivos
- Estados de carga, error y éxito bien definidos
- Sincronización automática entre pantallas

## 📦 Dependencias Principales

```kotlin
// Compose BOM
implementation(platform(libs.androidx.compose.bom))

// Retrofit para API
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Navigation Compose
implementation("androidx.navigation:navigation-compose:2.7.6")

// ViewModel Compose
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Corrutinas
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## 👥 Contribución

1. Fork el proyecto
2. Crea una rama feature (`git checkout -b feature/nueva-caracteristica`)
3. Commit tus cambios (`git commit -m 'Agregar nueva característica'`)
4. Push a la rama (`git push origin feature/nueva-caracteristica`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 📞 Soporte

Para reportar bugs o solicitar nuevas características, por favor abre un issue en el repositorio.

---

**Desarrollado con ❤️ usando Kotlin y Jetpack Compose**