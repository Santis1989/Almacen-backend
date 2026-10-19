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
