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
# API REST — Detalles de venta (issue #8)

## Alcance

Los detalles se registran dentro del POST de ventas y de su misma
transacción. No se agregan operaciones CRUD independientes para detalles.

Las relaciones con Venta y Producto son obligatorias y se representan
en la entidad mediante asociaciones JPA.

## Entrada de cada detalle

DetalleVentaRequest contiene únicamente:

| Campo | Tipo | Obligatorio | Restricciones |
|---|---|---|---|
| idProducto | Integer | Sí | Positivo y correspondiente a un producto existente |
| cantidad | BigDecimal | Sí | Positiva, hasta 9 dígitos enteros y 3 decimales |

El cliente de la API no determina el precio ni el subtotal del detalle.

## Registro dentro de una venta

POST /api/ventas

Content-Type: application/json

Ejemplo, utilizando un producto existente con precioVenta 10.50:

```json
{
  "venta": {
    "subtotal": 2.63,
    "porcentajeDescuento": 0.00,
    "montoDescuento": 0.00,
    "total": 2.63
  },
  "detalles": [
    {
      "idProducto": 1,
      "cantidad": 0.250
    }
  ]
}
```

El ID 1 es ilustrativo: debe reemplazarse por un producto existente.

La propiedad venta conserva el contrato existente del módulo.
Actualmente sus importes generales se reciben en la petición;
esta issue no implementa el cálculo general de descuentos y totales.
No debe enviarse idVenta al registrar una venta nueva.

Respuestas:

- 200 OK: devuelve la venta registrada según el contrato existente.
- 400 Bad Request: cuerpo ausente o ilegible, venta ausente,
  lista de detalles nula o vacía, elementos nulos, datos de detalle
  inválidos o importes fuera del rango admitido.
- 404 Not Found: producto inexistente o cliente inexistente
  cuando se haya indicado un cliente.

## Cálculo y persistencia

El servicio busca el producto y obtiene su precioVenta.
El precio aplicado queda almacenado en el detalle, conservando
su valor aunque cambie posteriormente el precio del producto.

Se calcula cantidad por precio usando BigDecimal.
Provisionalmente, el subtotal se redondea a 2 decimales con HALF_UP.
Ejemplo: 0.250 por 10.50 produce 2.625 y se guarda como 2.63.

Precio y subtotal deben ser no negativos y representables
en DECIMAL(12,2). El precio no se redondea silenciosamente.
Cantidad se almacena como DECIMAL(12,3), sin redondear valores
que excedan la precisión admitida.

La precisión de cantidad, la fuente del precio y el redondeo
siguen pendientes de confirmación formal del equipo.

Se conserva el comportamiento existente que permite descontar
stock aunque el resultado sea negativo. Producto.stock continúa
utilizando Double; no se migró su tipo en esta issue.

## Consulta de detalles

GET /api/ventas/{id}/detalles

Respuestas:

- 200 OK: lista de DetalleVentaResponse.
- 200 OK con []: la venta existe pero no tiene detalles.
- 404 Not Found: la venta no existe.

Ejemplo de respuesta:

```json
[
  {
    "idDetalle": 1,
    "idProducto": 1,
    "nombreProducto": "Producto prueba detalle",
    "cantidad": 0.250,
    "precio": 10.50,
    "subtotal": 2.63
  }
]
```

La respuesta utiliza DTOs. No incluye objetos JPA Venta o Producto
anidados ni referencias circulares.

## Transacción

VentaService coordina el registro con @Transactional.
Un fallo en un detalle debe revertir la venta, los detalles
y las modificaciones de stock de esa operación.

## Verificación realizada

Pruebas manuales con Postman y MySQL:

- Registro y consulta de un detalle válido.
- Redondeo de 2.625 a 2.63.
- Descuento de stock de 10 a 9.75.
- Rechazo de cantidades cero, negativas y nulas.
- Respuesta 404 al consultar una venta inexistente.
- Respuesta 400 para cuerpo ausente o request vacío.
- Fallo en el segundo producto: conteos de ventas y detalles
  y stock sin cambios después del rechazo.

Pruebas automatizadas en H2:

- Registro válido, consulta, precio, subtotal y stock.
- Rollback ante producto inexistente en el segundo renglón.
- Rechazo de cantidades nula, cero, negativa, con exceso
  de decimales y fuera del rango entero.

Comando utilizado desde Git Bash:

```bash
./mvnw.cmd -Dspring.profiles.active=test test
```

Resultado observado: 24 pruebas, 0 fallos, 0 errores,
0 omitidas y BUILD SUCCESS.
Siete ejecuciones pertenecen a DetalleVentaIntegrationTest.

H2 no reproduce todas las particularidades de MySQL.
La comprobación manual del esquema y del flujo en MySQL
complementa estas pruebas.