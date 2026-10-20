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

# API REST - Módulo de Productos

## Ruta base

`/api/productos`

La API recibe y devuelve datos en formato JSON.

## 1. Listar productos

**GET** `/api/productos`

Respuesta:
- 200 OK: devuelve una lista de productos.
- Si no hay productos, devuelve una lista vacía.

## 2. Buscar producto por ID

**GET** `/api/productos/{id}`

Respuestas:
- 200 OK: producto encontrado.
- 404 Not Found: el producto no existe.

## 3. Crear producto

**POST** `/api/productos`

Ejemplo de solicitud:

```json
{
  "nombre": "Yerba Mate 1 kg",
  "descripcion": "Yerba tradicional",
  "precioCompra": 2500.00,
  "precioVenta": 3200.00,
  "stock": 25.5,
  "stockMinimo": 10
}
```

Respuestas:
- 201 Created: producto creado correctamente.
- 400 Bad Request: datos inválidos.

El ID lo genera la base de datos. No debe enviarse.

## 4. Actualizar producto

**PUT** `/api/productos/{id}`

Se deben enviar todos los campos obligatorios.

Ejemplo:

```json
{
  "nombre": "Yerba Mate 1 kg",
  "descripcion": "Nueva presentación",
  "precioCompra": 2700.00,
  "precioVenta": 3500.00,
  "stock": 30.5,
  "stockMinimo": 10
}
```

Respuestas:
- 200 OK: producto actualizado.
- 400 Bad Request: datos inválidos.
- 404 Not Found: producto inexistente.

## 5. Eliminar producto

**DELETE** `/api/productos/{id}`

Respuestas previstas:
- 204 No Content: producto eliminado.
- 404 Not Found: producto inexistente.
- 409 Conflict: producto asociado a una venta.

La regla de impedir la eliminación de productos
asociados a ventas está pendiente de confirmación
con el equipo o profesor.

## 6. Ejemplo de respuesta de producto

```json
{
  "idProducto": 1,
  "nombre": "Yerba Mate 1 kg",
  "descripcion": "Yerba tradicional",
  "precioCompra": 2500.00,
  "precioVenta": 3200.00,
  "stock": 25.5,
  "stockMinimo": 10
}
```

No se exponen relaciones JPA en las respuestas.

## 7. Validaciones implementadas

- Nombre obligatorio, máximo 100 caracteres.
- Descripción opcional, máximo 500 caracteres.
- Precio de compra obligatorio.
- Precio de venta obligatorio.
- Stock obligatorio; admite valores fraccionarios.
- Stock mínimo obligatorio y entero.

Las reglas adicionales sobre valores negativos,
precios y restricciones de base de datos
siguen pendientes de confirmación.

## 8. Estado de verificación

- Compilación Maven: BUILD SUCCESS (19/10/2026).
- Pruebas automatizadas: pendientes.
- Pruebas HTTP de los endpoints: pendientes.
- Verificación de restricciones MySQL: pendiente.
```
