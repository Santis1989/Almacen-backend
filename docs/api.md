# Especificación de API REST

## Módulo: Clientes (`/api/clientes`)

### Descripción General
Gestión integral de clientes para el sistema de almacén. Desacopla la persistencia mediante DTOs y garantiza la integridad de datos (unicidad de DNI e integridad referencial con el módulo de Ventas).

---

### DTOs

#### `ClienteRequest` (Entrada para Creación y Actualización)
| Campo | Tipo | Obligatorio | Reglas de Validación |
| :--- | :--- | :--- | :--- |
| `nombre` | `String` | Sí | No vacío (`@NotBlank`), máximo 100 caracteres (`@Size(max=100)`). |
| `apellido` | `String` | Sí | No vacío (`@NotBlank`), máximo 100 caracteres (`@Size(max=100)`). |
| `dni` | `String` | Sí | Solo números, entre 7 y 15 dígitos (`@Pattern(regexp = "^\\d{7,15}$")`). |
| `telefono` | `String` | Sí | No vacío (`@NotBlank`), máximo 50 caracteres (`@Size(max=50)`). |
| `direccion`| `String` | No | Opcional, máximo 255 caracteres (`@Size(max=255)`). |

#### `ClienteResponse` (Salida)
| Campo | Tipo | Descripción |
| :--- | :--- | :--- |
| `idCliente` | `Integer` | Identificador único autogenerado. |
| `nombre` | `String` | Nombre del cliente. |
| `apellido` | `String` | Apellido del cliente. |
| `dni` | `String` | DNI del cliente. |
| `telefono` | `String` | Teléfono de contacto. |
| `direccion`| `String` | Dirección física. |

---

### Endpoints

#### 1. Listar Clientes
* **Método:** `GET`
* **Ruta:** `/api/clientes`
* **Respuesta exitosa:** `200 OK`
```json
[
  {
    "idCliente": 1,
    "nombre": "Juan",
    "apellido": "Perez",
    "dni": "30123456",
    "telefono": "11445566",
    "direccion": "Av. San Martín 123"
  }
]
```

---

#### 2. Buscar Cliente por ID
* **Método:** `GET`
* **Ruta:** `/api/clientes/{id}`
* **Parámetros de ruta:** `id` (`Integer`, identificador del cliente).
* **Respuestas:**
  - `200 OK`: Devuelve el `ClienteResponse`.
  - `404 Not Found`: Si no existe el cliente con el ID solicitado.

---

#### 3. Buscar Cliente por DNI
* **Método:** `GET`
* **Ruta:** `/api/clientes/dni/{dni}`
* **Parámetros de ruta:** `dni` (`String`, DNI numérico del cliente).
* **Respuestas:**
  - `200 OK`: Devuelve el `ClienteResponse`.
  - `404 Not Found`: Si no existe ningún cliente con ese DNI.

---

#### 4. Crear Cliente
* **Método:** `POST`
* **Ruta:** `/api/clientes`
* **Encabezados:** `Content-Type: application/json`
* **Cuerpo (Body):** `ClienteRequest`
* **Respuestas:**
  - `201 Created`: Devuelve el `ClienteResponse` creado y la cabecera `Location: /api/clientes/{id}`.
  - `400 Bad Request`: Si algún campo no cumple las validaciones (ej. DNI no numérico o fuera de rango).
  - `409 Conflict`: Si el DNI ya está registrado en la base de datos.

---

#### 5. Actualizar Cliente
* **Método:** `PUT`
* **Ruta:** `/api/clientes/{id}`
* **Parámetros de ruta:** `id` (`Integer`).
* **Cuerpo (Body):** `ClienteRequest`
* **Respuestas:**
  - `200 OK`: Devuelve el `ClienteResponse` actualizado. Permite conservar el DNI del propio cliente.
  - `400 Bad Request`: Si los datos enviados no cumplen las validaciones.
  - `404 Not Found`: Si el cliente no existe.
  - `409 Conflict`: Si se intenta actualizar el DNI con uno que ya pertenece a otro cliente.

---

#### 6. Eliminar Cliente
* **Método:** `DELETE`
* **Ruta:** `/api/clientes/{id}`
* **Parámetros de ruta:** `id` (`Integer`).
* **Respuestas:**
  - `204 No Content`: Cliente eliminado físicamente (válido únicamente si no tiene ventas asociadas).
  - `404 Not Found`: Si el cliente no existe.
  - `409 Conflict`: Si el cliente registra una o más ventas históricas asociadas (protección de integridad referencial).
