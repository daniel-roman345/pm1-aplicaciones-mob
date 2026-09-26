# Evidencias · PM1 Plan de Mejoramiento

**Aprendiz:** Daniel Salas Roman · **Ficha:** 3223873
**Programa:** Análisis y Desarrollo de Software (228118)

Proyecto: app Android de e-commerce en Kotlin + Jetpack Compose que consume una API REST en Flask.
El plan consiste en recibir el proyecto averiado, diagnosticar por qué falla y dejarlo funcionando.

---

## Estado de las cinco evidencias

| Evidencia | Qué pide la guía | Estado |
|---|---|---|
| **PM1-AA1-EV01** | Esquema del recorrido del dato + 4 hipótesis + reflexión | Documento redactado |
| **PM1-AA1-EV02** | Tabla de endpoints + capturas del cliente HTTP y de Logcat | Tabla y capturas de curl listas · falta Logcat |
| **PM1-AA2-EV03** | Reto 1 corregido en rama propia + video + informe | Código listo · falta el video |
| **PM1-AA2-EV04** | Reto 2 corregido + video + registro de peticiones | Código listo · registro listo · falta el video |
| **PM1-AA3-EV05** | Informe de depuración en PDF + sustentación oral | Informe listo · falta sustentar |

---

## Ramas del repositorio

| Rama | Commits | Qué contiene |
|---|---|---|
| `main` | — | El commit `13f2937` es el proyecto base **sin modificar**: sirve para mostrar el estado «antes». |
| `fix/desplegable-ubicaciones` | 5 | **Reto 1**: desplegables de país, estado y ciudad. |
| `fix/roles-y-perfil` | 8 | **Reto 2**: cambio de rol y edición de perfil. |

---

## La causa raíz, en una frase

El backend Flask **nunca devuelve una lista pelada**: siempre envuelve la colección dentro de un
objeto con su llave y un contador.

```
GET /api/locations/countries  ->  { "countries": [ ... ], "count": 5 }
```

Pero `ApiService.kt` declaraba `Response<List<Country>>`, es decir, le prometía a Retrofit un
arreglo `[`. Al recibir un objeto `{`, Gson lanzaba
`JsonSyntaxException: Expected BEGIN_ARRAY but was BEGIN_OBJECT`, la excepción caía en el `catch`
del ViewModel —que la mostraba como «Error de conexión», un mensaje engañoso porque la conexión
sí había funcionado— y el desplegable se quedaba vacío.

El mismo error estaba repetido en siete endpoints en total: tres en el Reto 1 y cuatro en el Reto 2.

**La prueba de que fue un descuido y no un cambio de la API:** en ese mismo archivo, las colecciones
de usuarios, categorías y productos ya usaban clases envoltorio (`UsersResponse`,
`CategoriesResponse`, `ProductsResponse`). Sólo ubicaciones y roles eran la excepción.

---

## Contenido de estas carpetas

### `PM1-AA1-EV02-Taller/`
- **`PM1-AA1-EV02-Tabla-de-Endpoints.pdf`** — el documento de la evidencia: puesta en marcha del
  backend, tabla de los 10 endpoints y la diferencia entre 401 y 403.
- **`PM1-EV02-captura-endpoints.png`** — captura de las 13 pruebas con `curl`. La guía acepta
  Postman, Thunder Client **o curl**.
- **`PM1-EV02-pruebas-endpoints.txt`** — la misma salida en texto.
- **`hashear_passwords.py`** — script obligatorio para poder iniciar sesión (ver abajo).

### `PM1-AA2-EV04-Reto2/`
- **`PM1-EV04-captura-terminal-backend.png`** — registro de las peticiones tal como llegaron al
  servidor, con los 200, los 401 y los 403 a la vista.
- **`PM1-EV04-terminal-backend.txt`** — lo mismo en texto, con la lectura línea por línea.

### `PM1-AA3-EV05-Informe/`
- **`PM1-AA3-EV05-Informe-de-Depuracion.pdf`** — hipótesis contra hallazgo, causa raíz, código antes
  y después, evidencia de verificación, guion de los 10 minutos de sustentación y las preguntas que
  probablemente hará el instructor, con sus respuestas.

---

## Cómo levantar el backend

```bash
git clone <repositorio-del-backend>
cd Ecommerce-Api-Python
python -m venv venv
venv\Scripts\Activate.ps1          # en Windows
```

El archivo de dependencias se llama **`requirement.txt`**, en singular, e incluye `psycopg2-binary`,
que sólo sirve para el despliegue y en Windows falla pidiendo `pg_config`. Como pip instala de forma
atómica, ese único fallo deja **todo** sin instalar, y por eso después aparece
`ModuleNotFoundError: No module named 'flask'`. Instala sólo lo necesario:

```bash
pip install Flask==2.3.3 Flask-SQLAlchemy==3.0.5 Flask-CORS==4.0.0 \
            python-dotenv==1.0.0 PyJWT==2.8.0 Werkzeug==2.3.7
```

Luego crea la base de datos con `database/create-ecommerce.sql`. Borra antes
`database/ecommerce.db` si ya existe: el script usa `CREATE TABLE` sin `IF NOT EXISTS`.

### El paso que casi todos olvidan

El SQL siembra las contraseñas **en texto plano**, pero `auth_routes.py` las valida con
`check_password_hash` de Werkzeug. Un texto plano nunca coincide con un hash, así que **el login
responde 401 aunque escribas la contraseña correcta**. Copia `hashear_passwords.py` a la carpeta
raíz del proyecto Flask y ejecútalo una vez, con el servidor detenido:

```bash
python hashear_passwords.py
```

Después arranca el servidor con `python app.py`, que queda en `http://127.0.0.1:5050`.

### Credenciales

| Usuario | Correo | Contraseña | Rol |
|---|---|---|---|
| admin | `admin@ecommerce.com` | `admin123` | Administrador |
| juan_perez | `juan@email.com` | `password123` | no es administrador |

Ojo con las llaves del login: el endpoint lee `Email` y `PasswoRDkey`, con esa mayúscula exacta.

```json
{ "Email": "admin@ecommerce.com", "PasswoRDkey": "admin123" }
```

---

## Desde el emulador de Android

La URL base de Retrofit está en `network/ApiClient.kt`. Desde el emulador, la máquina local **no es
`localhost`**: es `10.0.2.2`. Desde un teléfono físico hay que usar la IP del equipo en la red.
