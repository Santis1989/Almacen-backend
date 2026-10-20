# Decisiones del módulo de Productos

## 1. Objetivo

Definir las reglas de negocio del catálogo de productos del sistema Gestión de Almacén.

Estas decisiones se utilizan como referencia para implementar los DTO, las validaciones y las operaciones CRUD de la API REST.

## 2. Campos de Producto

| Campo | Regla | Restricción |
|---|---|---|
| nombre | Obligatorio | Entre 1 y 100 caracteres, sin contar espacios en blanco como nombre válido |
| descripcion | Opcional | Máximo 500 caracteres |
| precioCompra | Obligatorio | BigDecimal, dos decimales |
| precioVenta | Obligatorio | BigDecimal, dos decimales |
| stock | Obligatorio | Admite cantidades fraccionarias |
| stockMinimo | Obligatorio | Cantidad entera |

El identificador `idProducto` es generado por la base de datos. El cliente no puede establecerlo al crear o actualizar un producto.

Las asociaciones JPA, como `detallesVenta`, no forman parte del DTO de entrada ni del DTO de salida.

## 3. Reglas de stock

El sistema debe permitir registrar productos con cantidades fraccionarias, ya que algunos artículos se venden por peso.

Se mantiene como regla propuesta del sistema permitir ventas con stock cero o negativo, mostrando advertencias cuando corresponda. La implementación de ventas queda fuera de la Issue #5.

## 4. Eliminación de productos

**Propuesta pendiente de confirmación con el equipo:**

Si un producto está asociado a detalles de ventas, se impedirá su eliminación para preservar el historial.

En ese caso, la API devolverá HTTP `409 Conflict`.

Si el producto no existe, devolverá HTTP `404 Not Found`.

Si existe y puede eliminarse, devolverá HTTP `204 No Content`.

Esta política no debe implementarse como definitiva hasta su aprobación.

## 5. Decisiones pendientes

Antes de cerrar la implementación, confirmar con el equipo:

- Si los precios deben ser mayores que cero o pueden valer cero.
- Si el stock mínimo debe admitir únicamente valores mayores o iguales a cero.
- Si se permite registrar stock inicial negativo.
- La política de eliminación de productos asociados a ventas.
- La compatibilidad de estas reglas con las restricciones reales de MySQL.

## 6. Alcance

Esta documentación corresponde a la Issue #5, CRUD de Productos.

No se incorporan nuevas entidades, paginación, búsqueda ni modificaciones al módulo de Ventas.