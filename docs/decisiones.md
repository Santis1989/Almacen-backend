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


Decisiones del módulo de Ventas – Issue #7

Decisión 03: Política de descuentos y redondeo

Contexto

El módulo Ventas debe calcular los importes de cada operación desde el backend, utilizando los precios vigentes de los productos y evitando confiar en precios o totales enviados por el cliente.

Implementación actual

Descuento opcional: Se admite un porcentaje de descuento entre 0 % y 100 %, inclusive.

Descuento predeterminado: Cuando no se informa un porcentaje, se utiliza 0 %.

Cálculo de importes: Se utiliza BigDecimal para calcular subtotales, descuento y total.

Redondeo: Se utiliza RoundingMode.HALF_UP a dos decimales.

Precio de venta: Se obtiene desde la entidad Producto, no desde la solicitud del cliente.

Estado: Implementado y probado. Pendiente de confirmación con el equipo respecto de quién puede establecer el porcentaje de descuento y si esta política es la acordada.

Decisión 04: Cantidades y control de stock

Contexto

El módulo Productos representa el stock mediante Double, mientras que los detalles de Ventas utilizan BigDecimal para las cantidades.

El sistema admite productos vendidos en cantidades fraccionarias.

Implementación actual

Cada cantidad solicitada debe ser mayor que cero.

El producto debe existir y disponer de stock suficiente.

El sistema rechaza la venta si la cantidad solicitada supera el stock.

Cuando un producto aparece repetido en una venta, el stock disponible se verifica durante el procesamiento de cada detalle.

La venta, sus detalles y las modificaciones del stock se procesan mediante una transacción.

Si ocurre un error que provoca rollback, se revierten las modificaciones realizadas.

Estado: Implementado y probado para los casos indicados.

Pendiente: Acordar la cantidad máxima de decimales permitidos en las cantidades y la política de conversión entre BigDecimal y Double.

También queda pendiente evaluar la protección del stock ante ventas simultáneas.

Decisión 05: Cliente opcional en Ventas

Contexto

La Issue #7 establece que una venta puede registrarse sin asociarla a un cliente.

Implementación actual

El campo idCliente es opcional.

Si no se proporciona, la venta se registra sin cliente asociado.

Si se proporciona, el sistema verifica que el cliente exista.

Si el cliente no existe, se devuelve HTTP 404 Not Found.

Estado: Implementado y probado, de acuerdo con el contrato actual de la Issue #7.