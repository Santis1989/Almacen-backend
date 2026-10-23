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


---

# API REST - Módulo de Ventas

## 1. Descripción general

El módulo Ventas permite registrar y consultar ventas
realizadas en el sistema de almacén.

Cada venta puede incluir uno o varios productos y
puede estar asociada a un cliente o realizarse sin
cliente registrado.

El backend se encarga de:

- Validar los productos y sus cantidades.
- Comprobar el stock disponible.
- Obtener los precios desde la base de datos.
- Calcular subtotal, descuento y total.
- Descontar el stock de los productos vendidos.
- Guardar la venta y sus detalles.
- Revertir los cambios si ocurre un error.

## 2. Ruta base

`/api/ventas`

La API recibe y devuelve datos en formato JSON.

## 3. DTO de entrada: VentaRequest

Campos:

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| idCliente | Integer | No | ID del cliente asociado. Puede ser null. |
| porcentajeDescuento | BigDecimal | No | Descuento entre 0 y 100. Si no se envía, se utiliza 0. |
| detalles | List | Sí | Lista de productos. Debe contener al menos un detalle. |

Cada elemento de `detalles` utiliza
`DetalleVentaRequest`.

### DetalleVentaRequest

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---|---|
| idProducto | Integer | Sí | ID del producto. |
| cantidad | BigDecimal | Sí | Cantidad solicitada, mayor que cero. |

Los precios y los importes no se reciben desde el
cliente: se calculan en el servidor.

## 4. Registrar una venta

**POST** `/api/ventas`

Encabezado:

`Content-Type: application/json`

### Ejemplo de solicitud

```json
{
  "idCliente": null,
  "porcentajeDescuento": 10,
  "detalles": [
    {
      "idProducto": 1,
      "cantidad": 2
    },
    {
      "idProducto": 2,
      "cantidad": 3
    }
  ]
}
```

El ejemplo supone que los productos 1 y 2 existen
y tienen stock suficiente.

### Respuesta exitosa

**201 Created**

Ejemplo ilustrativo, suponiendo que el producto 1
cuesta $1800 y el producto 2 cuesta $1200:

```json
{
  "idVenta": 1,
  "fecha": "2026-10-22T20:00:00",
  "idCliente": null,
  "subtotal": 7200.00,
  "porcentajeDescuento": 10.00,
  "montoDescuento": 720.00,
  "total": 6480.00,
  "detalles": [
    {
      "idDetalle": 1,
      "idProducto": 1,
      "nombreProducto": "Arroz 1kg",
      "cantidad": 2,
      "precio": 1800.00,
      "subtotal": 3600.00
    },
    {
      "idDetalle": 2,
      "idProducto": 2,
      "nombreProducto": "Azucar 1kg",
      "cantidad": 3,
      "precio": 1200.00,
      "subtotal": 3600.00
    }
  ]
}
```

Los identificadores y la fecha son generados
por el sistema y pueden variar.

### Respuestas de error

- 400 Bad Request: datos inválidos, lista vacía,
  cantidad menor o igual a cero o descuento fuera
  del rango permitido.
- 404 Not Found: producto o cliente inexistente.
- 409 Conflict: stock insuficiente.

## 5. Listar ventas

**GET** `/api/ventas`

### Respuesta

**200 OK**

Devuelve una lista de objetos `VentaResponse`.

Si no existen ventas, devuelve una lista vacía.

Cada venta incluye sus datos generales y sus detalles.

## 6. Buscar venta por ID

**GET** `/api/ventas/{id}`

Parámetro:

- id: identificador de la venta.

### Respuestas

- 200 OK: devuelve la venta solicitada.
- 404 Not Found: la venta no existe.

## 7. Consultar detalles de una venta

**GET** `/api/ventas/{id}/detalles`

### Respuestas

- 200 OK: devuelve los detalles asociados.
- 404 Not Found: la venta no existe.

Ejemplo de respuesta:

```json
[
  {
    "idDetalle": 1,
    "idProducto": 1,
    "nombreProducto": "Arroz 1kg",
    "cantidad": 2,
    "precio": 1800.00,
    "subtotal": 3600.00
  }
]
```

## 8. Reglas de cálculo

Los cálculos monetarios se realizan utilizando
`BigDecimal`.

### Subtotal de cada detalle

cantidad * precioVenta

El resultado se redondea a dos decimales
utilizando `RoundingMode.HALF_UP`.

### Subtotal de la venta

Suma de los subtotales de todos los detalles.

### Monto de descuento

subtotalVenta * porcentajeDescuento / 100

Se redondea a dos decimales con HALF_UP.

### Total final

total = subtotalVenta - montoDescuento

Se expresa con dos decimales utilizando HALF_UP.

El porcentaje de descuento debe estar entre
0 y 100, inclusive.

Si no se proporciona un descuento, se utiliza 0.

## 9. Validaciones

Antes de completar el registro, el sistema verifica:

- Que la venta tenga al menos un detalle.
- Que cada detalle indique un producto.
- Que la cantidad sea mayor que cero.
- Que el producto exista.
- Que exista stock suficiente.
- Que el precio de venta sea válido.
- Que el cliente exista, si se indicó uno.
- Que el descuento esté entre 0 y 100.

Si alguna validación falla, la venta no debe
registrarse parcialmente.

## 10. Transacciones y stock

El método de registro utiliza `@Transactional`.

Dentro de la transacción se realizan las operaciones
necesarias para registrar la venta y actualizar
el stock.

Si ocurre una excepción que provoca rollback,
se revierten las modificaciones realizadas.

Se verificó mediante pruebas de integración que:

- Una venta exitosa guarda la venta y sus detalles.
- El stock se actualiza correctamente.
- Una venta con stock insuficiente no se registra.
- Si falla el segundo producto, se revierte el
  descuento de stock del primero.
- Si el mismo producto aparece repetido y la
  cantidad acumulada supera el stock, se rechaza
  la operación y se conserva el stock original.

### Limitación conocida

No se ha implementado ni verificado todavía
un mecanismo específico para impedir conflictos
entre ventas simultáneas.

Esta mejora queda pendiente.

## 11. Estado de verificación

### Pruebas manuales realizadas con Postman

- POST exitoso: 201 Created.
- Producto inexistente: 404 Not Found.
- Cliente inexistente: 404 Not Found.
- Stock insuficiente: 409 Conflict.
- Cantidad cero: 400 Bad Request.
- Lista de detalles vacía: 400 Bad Request.
- Consultas GET de ventas y detalles realizadas.

### Pruebas automatizadas

`VentaServiceTest`

- 7 pruebas unitarias aprobadas.

`VentaIntegracionTest`

- 3 pruebas de integración aprobadas.
- Base de datos H2 en memoria.
- Sin modificar los datos de MySQL.

`AlmacenApplicationTests`

- 2 pruebas aprobadas.
- Verificada la conexión a H2.

### Estado actual

Las pruebas mencionadas se ejecutaron
correctamente por separado.

Queda pendiente ejecutar la suite completa
del proyecto mediante:

`.\mvnw.cmd clean test`

También queda pendiente preparar el commit
y el Pull Request correspondiente al Issue #7.
