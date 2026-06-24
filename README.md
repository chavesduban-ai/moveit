# MoveIt! - Spring Boot (Java)

Sistema de gestión de domicilios migrado de PHP a **Java 17 + Spring Boot 3.2**.

---

## GUÍA DE INSTALACIÓN PASO A PASO

### 1. Instalar Java JDK 17

1. Ir a: https://adoptium.net/es/
2. Descargar **Temurin JDK 17** (Windows x64 .msi)
3. Ejecutar el instalador → Marcar **"Set JAVA_HOME variable"** y **"Add to PATH"**
4. Verificar en CMD:
   ```
   java -version
   ```
   Debe mostrar algo como: `openjdk version "17.x.x"`

### 2. Instalar Maven

1. Ir a: https://maven.apache.org/download.cgi
2. Descargar el archivo **Binary zip archive** (apache-maven-3.9.x-bin.zip)
3. Descomprimir en `C:\maven`
4. Agregar al PATH de Windows:
   - Buscar "Variables de entorno" en Windows
   - En "Variables del sistema" → Editar `Path`
   - Agregar: `C:\maven\apache-maven-3.9.9\bin` (o la versión que descargaste)
5. Verificar en CMD:
   ```
   mvn -version
   ```

### 3. Instalar Visual Studio Code + Extensiones

1. Si no tienes VS Code: https://code.visualstudio.com/
2. Abrir VS Code → Ir a Extensiones (Ctrl+Shift+X)
3. Instalar estas extensiones:
   - **Extension Pack for Java** (Microsoft) — incluye todo lo de Java
   - **Spring Boot Extension Pack** (VMware/Pivotal) — soporte Spring Boot
   - **Lombok Annotations Support** (Gabriel B.) — para que no marque errores con @Data, @Builder, etc.
   - **Thymeleaf** (Daniel Escapa) — soporte para las plantillas HTML

### 4. Crear la Base de Datos

1. Abrir XAMPP → Iniciar **MySQL**
2. Ir a `http://localhost/phpmyadmin`
3. Crear base de datos: `movelt_db`
   - O importar el archivo `src/main/resources/db/schema.sql`
   - **NOTA:** Spring Boot crea las tablas automáticamente, pero si quieres datos de prueba, importa el schema

### 5. Configurar el Proyecto

1. Abrir la carpeta `movelt-spring` en VS Code
2. Editar `src/main/resources/application.properties`:
   ```properties
   # Si tu MySQL tiene contraseña, ponla aquí:
   spring.datasource.password=tu_password_mysql

   # Para correos masivos (Gmail SMTP):
   spring.mail.username=tu_correo@gmail.com
   spring.mail.password=xxxx xxxx xxxx xxxx
   ```

### 6. Ejecutar el Proyecto

**Opción A — Desde terminal:**
```bash
cd movelt-spring
mvn spring-boot:run
```

**Opción B — Desde VS Code:**
1. Abrir `src/main/java/com/movelt/MoveltApplication.java`
2. Click en el botón **▶ Run** que aparece encima de `main()`
3. O presionar `F5`

**La primera vez** Maven descarga todas las dependencias (puede tardar 2-5 minutos). Después es instantáneo.

### 7. Abrir en el Navegador

```
http://localhost:8080
```

- **Landing page:** http://localhost:8080/
- **Login:** http://localhost:8080/login
- **Registro:** http://localhost:8080/register

### 8. Usuarios de Prueba (se crean automáticamente)

| Usuario | Contraseña | Rol           |
|---------|------------|---------------|
| admin   | admin123   | Administrador |
| andres  | 123456     | Repartidor    |
| kevin   | 123456     | Repartidor    |
| duban   | 123456     | Cliente       |
| mario   | 123456     | Cliente       |

---

## Estructura del Proyecto

```
movelt-spring/
├── pom.xml                          # Dependencias Maven (como composer.json en PHP)
│
├── src/main/java/com/movelt/
│   ├── MoveltApplication.java       # Clase principal (punto de entrada)
│   │
│   ├── config/
│   │   ├── SecurityConfig.java      # Login, roles, CSRF, sesiones
│   │   └── DataInitializer.java     # Datos semilla al primer arranque
│   │
│   ├── model/                       # Entidades JPA (equivale a las tablas MySQL)
│   │   ├── Usuario.java             # @Entity → tabla 'usuarios'
│   │   ├── Pedido.java              # @Entity → tabla 'pedidos'
│   │   ├── Calificacion.java        # @Entity → tabla 'calificaciones'
│   │   └── enums/
│   │       ├── Rol.java
│   │       ├── EstadoPedido.java
│   │       ├── Servicio.java
│   │       └── EstadoUsuario.java
│   │
│   ├── repository/                  # Acceso a BD (equivale a los queries SQL)
│   │   ├── UsuarioRepository.java
│   │   ├── PedidoRepository.java
│   │   └── CalificacionRepository.java
│   │
│   ├── service/                     # Lógica de negocio
│   │   ├── UsuarioService.java
│   │   ├── UsuarioDetailsService.java  # Conecta Spring Security con BD
│   │   ├── PedidoService.java
│   │   ├── CalificacionService.java
│   │   ├── EmailService.java           # Correos masivos (SMTP)
│   │   └── WebServiceClient.java       # Consume APIs externas
│   │
│   └── controller/                  # Controladores (equivale a los .php)
│       ├── AuthController.java      # /, /login, /register
│       ├── ClienteController.java   # /cliente/*
│       ├── RepartidorController.java # /repartidor/*
│       └── AdminController.java     # /admin/*
│
├── src/main/resources/
│   ├── application.properties       # Configuración (BD, email, etc.)
│   ├── db/schema.sql                # Schema MySQL de referencia
│   ├── static/                      # CSS, JS, imágenes (mismos del PHP)
│   │   ├── css/
│   │   ├── js/
│   │   └── img/
│   └── templates/                   # Vistas HTML con Thymeleaf
│       ├── index.html               # Landing page
│       ├── auth/login.html          # Login
│       ├── auth/register.html       # Registro
│       ├── cliente/dashboard.html   # Panel cliente
│       ├── repartidor/dashboard.html # Panel repartidor
│       ├── admin/dashboard.html     # Panel admin
│       ├── admin/correos.html       # Correos masivos
│       └── admin/webservices.html   # Web Services
│
└── src/test/java/com/movelt/
    └── MoveltApplicationTests.java
```

---

## Equivalencias PHP → Java Spring Boot

| Concepto PHP           | Equivalente Spring Boot             |
|------------------------|-------------------------------------|
| `conecxion.php`        | `application.properties` + JPA      |
| `session_start()`      | Spring Security (automático)        |
| `$_SESSION`            | `@AuthenticationPrincipal`          |
| `$_POST`               | `@RequestParam`                     |
| `mysqli_prepare()`     | JPA Repository (sin SQL manual)     |
| `password_hash()`      | `BCryptPasswordEncoder`             |
| `header("Location:")` | `return "redirect:/url"`            |
| `csrf_token()`         | Spring Security CSRF (automático)   |
| `include 'archivo.php'`| `@Autowired` / inyección por constructor |
| `echo json_encode()`  | `@ResponseBody` + `ResponseEntity`  |
| `require 'middleware'` | `@PreAuthorize` / `SecurityConfig`  |
| `.htaccess`            | `SecurityConfig` (programático)     |
| `file_get_contents()`  | `WebClient` (Spring WebFlux)        |
| `mail()`               | `JavaMailSender`                    |

---

## Tecnologías Usadas

- **Java 17** — Lenguaje
- **Spring Boot 3.2.5** — Framework
- **Spring Security** — Autenticación, roles, CSRF
- **Spring Data JPA + Hibernate** — ORM (acceso a BD sin SQL manual)
- **Thymeleaf** — Motor de plantillas HTML (reemplaza PHP embebido)
- **MySQL** — Base de datos (misma que el proyecto PHP)
- **Spring Mail** — Envío de correos SMTP
- **Spring WebFlux (WebClient)** — Consumo de APIs externas
- **Bootstrap 5** — UI del panel admin
- **Lombok** — Reduce código repetitivo en entidades
