
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
