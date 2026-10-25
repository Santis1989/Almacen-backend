# Registro de Decisiones de Arquitectura y Diseño

## Decisión 01: Formato y Validación de DNI para Clientes

### Contexto
La entidad `Cliente` requiere identificar unívocamente a cada persona física en el sistema de almacén. En la implementación inicial, el campo `dni` aceptaba cualquier cadena numérica con la expresión regular `\d+`, sin restricciones de longitud mínima o máxima ni restricción de unicidad en la base de datos.

### Decisión
1. **Formato numérico conservado:** Se conserva el formato estrictamente numérico del código fuente existente (`^\d{7,15}$`), sin caracteres alfanuméricos ni símbolos, cubriendo:
   - DNIs argentinos tradicionales de 7 y 8 dígitos.
   - Identificadores numéricos especiales o extranjeros de hasta 15 dígitos.
2. **Restricción de unicidad en Base de Datos:** Se definió `@Column(name = "dni", nullable = false, unique = true, length = 20)` en la entidad JPA `Cliente` para que el motor MySQL imponga la constraint de unicidad a nivel físico, protegiendo al sistema frente a condiciones de carrera o peticiones concurrentes.
3. **Validación de unicidad en lógica de negocio (`ClienteService`):**
   - **Al crear (`guardar`):** Se verifica preventivamente mediante `clienteRepository.existsByDni(dni)`. Si ya existe, se rechaza inmediatamente arrojando `ConflictException` (HTTP 409 Conflict).
   - **Al actualizar (`actualizar`):** Se verifica mediante `clienteRepository.existsByDniAndIdClienteNot(dni, id)`. Si el nuevo DNI le pertenece a *otro* cliente, se rechaza con HTTP 409 Conflict. Si el cliente conserva su propio DNI, la operación se procesa exitosamente.

---

## Decisión 02: Protección de Integridad Referencial en Ventas Históricas

### Contexto
La entidad `Venta` mantiene una relación `@ManyToOne` obligatoria hacia `Cliente`. La eliminación física directa de un cliente que registre operaciones de venta previas vulnera la integridad contable e histórica del almacén y genera fallos de clave foránea en la base de datos.

### Decisión
1. **Protección preventiva:** Antes de proceder con la eliminación, `ClienteService.eliminar()` consulta `ventaRepository.existsByCliente_IdCliente(idCliente)`.
2. **Respuesta ante conflicto:** Si el cliente tiene una o más ventas registradas, la operación se cancela arrojando `ConflictException` (HTTP 409 Conflict), preservando tanto al cliente como sus comprobantes comerciales asociados.
3. **Eliminación limpia:** Si el cliente no registra ventas, se procede con la eliminación física habitual respondiendo HTTP 204 No Content.
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
## Detalles de venta — precisión propuesta para la issue #8

Se propone admitir cantidades positivas con hasta 9 dígitos
enteros y 3 decimales, para permitir ventas de productos por peso.

Esta restricción se incorporó en DetalleVentaRequest mediante
@NotNull, @Positive y @Digits(integer = 9, fraction = 3).

La precisión todavía está pendiente de confirmación del equipo.
También falta alinear la entidad y el servicio con esta propuesta
y coordinarla con el stock de Producto, que actualmente usa Double.

Esta decisión no define todavía la política de redondeo
de los importes.
### Importes del detalle — propuesta pendiente de confirmación

Se implementa provisionalmente la obtención del precio desde
Producto.precioVenta. Ese valor se almacena en DetalleVenta.precio
como precio aplicado al momento de la venta.

El subtotal se calcula en el backend mediante cantidad por precio,
usando BigDecimal y redondeando a 2 decimales con HALF_UP.

Precio y subtotal deben ser no negativos y representables en
DECIMAL(12,2). El precio no se redondea silenciosamente.

La fuente del precio y el modo de redondeo siguen pendientes
de confirmación del equipo.
