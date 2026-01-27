# Dificultades durante el desarrollo y como fueron solucionados

## Problema 1: Autenticacion de usuario mediante servicios de Google

### Problema

La intención era implementar esta solución para que el usuario pueda autenticarse en el sistema sin tener que recordar con que mail se registró y una clave, puesto que seguro debe recordar muchas otras. Nunca antes he implementado este servicio, ni ningún otro, por lo tanto desconozco las formas de hacerlo y sus buenas prácticas. He intentado implementar este servicio con la ayuda de IA, la cual me generó un conjunto de clases y configuraciones. Ingresando a la direccion del endpoint configurado te permitía elegir la cuenta de Google para autenticarse, aceptar permisos, pero luego no se guardaba el usuario en la base de datos.

### Solución

1. No he podido encontrar solución al problema, todo parecía estar bien codificado. La IA no ha podido encontrar el problema tampoco. Decidí borrar todas las clases y configuraciones relacionadas a este servicio, e implementar los endpoint para el registro manual de un usuario (nombre, email, clave) almacenando la clave hasheada y otro para autenticar al usuario  (basico, corrobora email y clave). Esta solución es temporal, para no retrasar el avance del proyecto.
2. De momento la idea es aprender algunas cosas nuevas, como el de usar logs, que me permita encontrar donde puede estar el problema. Luego mas adelante si intentará implementar este servicio de autenticación mediante un proveedor como Google, para cumplir con los requerimientos solicitados.

## Problema 2: Buscar por campos fecha en la base de datos

### Problema

En la búsqueda de transacciones, un requerimiento era que el usuario desea buscar transacciones por mes y año (campos opcionales). La idea es que el usuario selecciones de una lista desplegable el mes y año para filtrar transacciones almacenadas. Cada transacción tiene una fecha y su tipo de datos es LocalDate. Tuve dificultades para filtrar estos datos en la Query porque mes y año eran Integer, porque usaba unas funciones que despues al traducir la consulta no funcionaban. Cuando usaba la API de buscarTransacciones me devolvia Error 500. Me ha pasado en varios proyectos de tener esta dificultad a la hora de comparar fechas en la base de datos.

### Solución

Pude solucionar el problema, lo que hice fue transformar el mes y año que son integer en dos fechas LocalDate, por ejemplo, si el usuario ingresa mes = 1 y año = 2025, se generan dos fechas:
- 01/01/2025
- 31/01/2025
Luego utiliza un between en la query para buscar todas las transacciones que sucedieron dentro de esa fecha.
El unico inconveniente es que como está implementada la solución, si no se ingresa un año no se filtra por mes tampoco. En el front agregaré alguna restriccion que si no se ingresa un año no se puede elegir un mes.

## Problema 3: Autenticación de usuario mediante la API de Google

### Problema

Estuve intentando implementar en la aplicación la autenticación de Google sin conocimientos previos algunos, con mucha ayuda de la IA (se utilizó Gemini CLI para esto). Se configuró las credenciales en la página de Google Cloud, se implementó las clases y metodos necesarios para este propósito, incluso de modificó el SecurityConfig.java para que funcionara. El inconveniente que se presentó es que el servicio autenticaba el usuario, pero el mismo no se guardaba en la base de datos. Probablemente es el inconveniente mas grande que tuve durante el desarrollo de este proyecto. Es un requerimiento clave para este sistema, por lo tanto se trabajó en la resolución de este problema.

### Solución

Finalmente, luego de muchas iteraciones con la IA y muchas hipótesis que fuimos armando y descartando, hallamos el problema. Le pedí a la IA que implemente algunos logs que nos permita ver que estaba pasando entre la aplicación y la base de datos. Luego de correr la aplicación e intentar autenticarme pude obtener un registro de todo lo que ocurría. Generé un archivo con todas las salidas del sistema para evaluarlo junto con la IA. Luego esta dió con la solución, basicamente se tuvo que modificar la clase service que se encargaba de la autenticación de usuario, para que esta extienda de OidcUserService y la clase de modelo CustomOAuth2User implemente OidcUser. Antes de esto, estas clases extendían e implementaban respectivamente otras cosas las cuales no tenían compatibilidad con los datos que me brindaba el servicio de google.

## Problema 4: Problemas de precisión

### Problema
Este problema surge desde la concepción del diseño del software, puesto que no se tuvieron en cuenta algunas cuestiones desde el principio en cuanto a tipos de dato. 
En un principio cuando el software se encontraba en etapa de diseño, producto del desconocimiento, se decidió utilizar para los saldos y montos el tipo de dato Float en Java y Real para la base de datos. No se tuvo en cuenta que estábamos desarrollando una aplicación financiera y requería un mayor nivel de precisión para que esta tenga un funcionamiento mas realista y profesional. Estas impresiciones se notan sobre todo en la ejecución de los casos de prueba, donde probamos los límites de la aplicación. Al ingresar montos o saldos muy grandes, al usar tipos de datos incorrecto para el dominio, se producen redondeos incorrectos o que no deberían suceder y los límites superiores de estos montos que se le permite ingresar a los usuarios no es lo suficientemente alto, limitado por el tipo de dato.

### Solución

Para solucionar esto, de decidió cambiar el tipo de dato de los montos y saldos tanto en el backend en Java como en la base de datos. En primer lugar se cambió el tipo de dato REAL en la base de datos por NUMERIC(15, 2) que permite almacenar 15 dígitos enteros y 2 decimales. Esta precisión está relativamente bien para el caso, aunque podría ser mayor, también priorizamos los costos de almacenamiento.
Por lo pronto se agregó anotaciones para que el ORM mapea Float con NUMERIC, porque cambiar toda la logica de negocio requiere tiempo y se hará en una próxima iteración. Lo que sigue es cambiar el tipo de dato Float por BigDecimal. Con estos cambios deberíamos lograr mayor nivel de precisión.
Finalmente la solución de estos problemas se traduce en un aprendizaje del costo que conlleva tomar malas decisiones de diseño, que serán tenidas en cuenta para desarrollos futuros.


## Problema 4: Problemas de autenticación

### Problema
Luego de implementar el nuevo frontend separado del backend, utilizando React y TypeScript, tuve muchas dificultades para conectar el frontend con el backend y viceversa. 
Se configuró lo CORS en un principio, pero al probar el despliegue en unos hosting de staging e intentar loguear un usuario, este se creaba en la base de datos, pero no redirigía al dashboard de la app, sino que volvía al login. Por lo tanto había un problema de comunicación entre el frontend y el backend.

### Solución
Al investigar detectamos que el problema era cross‑domain: las cookies de sesión no funcionaban entre el hosting del frontend y el backend (políticas SameSite/CORS). Para resolverlo migramos a un flujo basado en JWT: el backend genera un token al completar la autenticación (OAuth2) y redirige al frontend con el token en la URL; el frontend lo extrae, lo guarda en `localStorage` y un interceptor de Axios añade `Authorization: Bearer <token>` a cada petición. En el backend dejamos la configuración como stateless (`SessionCreationPolicy.STATELESS`) y añadimos un filtro para validar los JWT en cada petición. Con este cambio las redirecciones y la comunicación entre front y backend funcionaron correctamente y el usuario queda autenticado y redirigido al dashboard.

