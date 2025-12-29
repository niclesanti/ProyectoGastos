# Sección /Movimientos

Los siguientes cambios que se proponen en esta sección es para que el mismo sea mas profesional y se valide las entradas de los usuarios.

## Campos de entrada:

### Años

- Agregar los años desde el 2024 hasta el 2030, quitar el resto

### Filtrar por motivos

- Poblar el selector con los motivos de la base de datos de la misma forma que has poblado los demás selectores.

### Filtrar por motivos

- Poblar el selector con los contactos de la base de datos de la misma forma que has poblado los demás selectores.

### Botón buscar
- Implementar un botón "Buscar" al lado del filtro de contactos
- Usar componentes shadcn/ui ya definidos
- Al presionar buscar se debe consumir la siguiente API de la misma forma que se ha hecho en toda la aplicación y mantener en el cache.
  - POST "/api/transaccion/buscar"
  - public ResponseEntity<List<TransaccionDTOResponse>> buscarTransaccion(@Valid @RequestBody TransaccionBusquedaDTO datosBusqueda)
- Todos los campos son opcionales
- Si el usuario deja un campo por defecto (Todos los meses, Todos los años, Todos los motivos, Todos los contactos) se envia ese campo en el JSON como null.
- Si no se encuentra ninguna transacción o no se ha buscado aún y la lista está vacía poner un mensaje.

# MUY IMPORTANTE
No modificar ningún componente actual (salvo agregar el botón "Buscar").


# Notificaciones
- Usar un metodo mas moderno como mensajes mediante toust. Mensajes de exito, fracaso, advertencia, información.
