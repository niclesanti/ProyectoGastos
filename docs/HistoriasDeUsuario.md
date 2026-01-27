# HISTORIAS DE USUARIO: Sistema de gestión de gastos personales

## 1. Gestión de transacciones de dinero
* **Número**: 1
* **Nombre**: Gestión de transacciones de dinero
* **Usuario**: Usuario
* **Prioridad**: Alta
* **Iteración**: 1
* **Descripción**:
    * Necesito poder llevar un registro de entrada y salida del dinero.
    * Cada vez que realizo una transacción, que esta puede ser un pago (salida de dinero) o recibir un pago (entrada de dinero), necesito registrar estos movimientos que me permita llevar una trazabilidad de los movimientos.
    * Además, necesito registrar la fecha, el motivo o categoría de la transacción, el monto en pesos argentinos, el contacto emisor/destinatario (opcional) y una descripción (opcional).
    * Debe permitir también registrar un nuevo “motivo” y un “contacto” si estos no existen.

## 2. Generar lista de transacciones
* **Número**: 2
* **Nombre**: Generar lista de transacciones
* **Usuario**: Usuario
* **Prioridad**: Alta
* **Iteración**: 1
* **Descripción**:
    * Quiero poder visualizar una lista con las transacciones realizadas con los campos: fecha, motivo, destinatario, monto.
    * Las transacciones se deben visualizar por defecto las más recientes, es decir, las del mes y año actual.
    * Se debe poder buscar transacciones por mes, año, motivo y destinatario.
    * Todos los campos de búsqueda son opcionales, es decir, si no ingresa ningún campo te debe devolver todas las transacciones almacenadas.
    * Esta lista resultante se debe poder ordenar por monto (ascendente/descendente) y por fecha (más antiguas/más recientes).
    * Debe permitirse seleccionar una transacción de las encontradas y mostrar los datos completos de esta.

## 3. Análisis de datos (gastos/ingresos)
* **Número**: 3
* **Nombre**: Análisis de datos (gastos/ingresos)
* **Usuario**: Usuario
* **Prioridad**: Alta
* **Iteración**: 1
* **Descripción**:
    * Quiero poder visualizar una pantalla de control donde se observe diferentes gráficos de variables que puedan ser útiles para la toma de decisiones.
    * Algunos gráficos útiles son:
        * Gráfico de ingresos vs gastos mensuales (barras).
        * Distribución de gastos por motivos (pastel o donut).

## 4. Autenticación en el sistema
* **Número**: 4
* **Nombre**: Autenticación en el sistema
* **Usuario**: Usuario
* **Prioridad**: Alta
* **Iteración**: 1
* **Descripción**:
    * Como usuario debo poder autenticarme en el sistema. Esta autenticación puede ser mediante un proveedor externo, como Google, Facebook, etc.
    * Esto me permitiría ingresar al sistema sin la necesidad de recordar mi usuario y contraseña específica para esta aplicación.

## 5. Compartir datos entre usuarios
* **Número**: 5
* **Nombre**: Compartir datos entre usuarios
* **Usuario**: Usuario
* **Prioridad**: Alta
* **Iteración**: 1
* **Descripción**:
    * Como usuario, quiero poder crear espacios de trabajo (por ejemplo, "Gastos Negocio X") que representen una unidad independiente de gestión económica dentro del sistema, con su propio conjunto de transacciones, saldo y análisis.
    * Cada espacio debe tener un nombre identificador y mantener sus datos completamente aislados de otros entornos que pueda tener el mismo usuario.
    * Además, quiero poder compartir un espacio específico con otros usuarios del sistema, de modo que puedan acceder, visualizar y operar sobre ese espacio como si fuera propio.
    * Esto incluye agregar, modificar o eliminar transacciones, así como ver gráficos y reportes asociados a ese entorno.
    * Cada usuario podrá tener uno o más espacios propios y/o compartidos, y los datos de un entorno no deben afectar en absoluto a los de otros entornos, garantizando una gestión segmentada, organizada y colaborativa del negocio.

## 6. Remover una transacción
* **Número**: 6
* **Nombre**: Remover una transacción
* **Usuario**: Usuario
* **Prioridad**: Media
* **Iteración**: 1
* **Descripción**:
    * El sistema debe permitirme eliminar una transacción en caso de haber registrado una y considerar que es una equivocación.
    * La función de buscar transacción debe tener una forma de seleccionar una de las transacciones encontradas y removerla.
    * Solo se puede seleccionar de a una.

## 7. Historial de auditorías
* **Número**: 7
* **Nombre**: Historial de auditorías
* **Usuario**: Usuario
* **Prioridad**: Alta
* **Iteración**: 1
* **Descripción**:
    * Como auditor, necesito visualizar un registro de quién generó una transacción, con usuario y sello de tiempo, para garantizar trazabilidad.

## 8. Anotar movimientos en cuentas bancarias
* **Número**: 8
* **Nombre**: Anotar movimientos en cuentas bancarias
* **Usuario**: Usuario
* **Prioridad**: Alta
* **Iteración**: 1
* **Descripción**:
    * Como usuario, me gustaría que cada vez que anoto una transaccion de ingreso/salida de dinero, pueda además agregarle a que, o desde cual cuenta bancaria se hace el movimiento (opcional). Me interesa que cada cuenta tenga algun nombre que la identifique (por ejemplo, cuenta-raul, cuenta-agustin, etc), la entidad financiera y el saldo actual que corresonde al espacio en el cual estamos trabajando.

## 9. Compras con crédito
* **Número**: 9
* **Nombre**: Anotar una compra con crédito
* **Usuario**: Usuario
* **Prioridad**: Alta
* **Iteración**: 1
* **Descripción**:
    * Como usuario, me gustaría poder registrar cuando hago una compra con crédito, ya que la compra de este mes debería pagarla a partir del mes siguiente, para saber por adelantado lo que voy gastando.

## 10. Remover una compra con crédito
* **Número**: 10
* **Nombre**: Remover una compra con crédito erronea
* **Usuario**: Usuario
* **Prioridad**: Media
* **Iteración**: 1
* **Descripción**:
    * El sistema debe permitirme eliminar una compra con crédito en caso de haber registrado una y considerar que es una equivocación.

## 11. Notificaciones
* **Número**: 11
* **Nombre**: Recibir notificaciones de eventos
* **Usuario**: Usuario
* **Prioridad**: Media
* **Iteración**: 2
* **Descripción**:
    * Como usuario, me gustaría recibir notificaciones de eventos como cuando alguien me comparte a su espacio de trabajo, cuando cierra el resumen de tarjeta, etc.
    * En principio que pueda ver las notificaciones desde la app, en el futuro se podría implementar notificaciones por mail o en el dispositivo si existiese la posibilidad.

## 12. Exportar datos
* **Número**: 12
* **Nombre**: Exportar datos
* **Usuario**: Usuario
* **Prioridad**: Baja
* **Iteración**: 2
* **Descripción**:
    * Como usuario, necesito exportar un listado de transacciones (que puede ser generado por una búsqueda) o reportes en una planilla de cálculo o PDF.

## 13. Implementar IA
* **Número**: 13
* **Nombre**: Interactuar con IA
* **Usuario**: Usuario
* **Prioridad**: Baja
* **Iteración**: 2
* **Descripción**:
    * Como usuario, me gustaría que alguna IA pueda interactuar con los datos de la app y sea capaz de generarme resultados que le solicite en base a mis datos.


## Características importantes del sistema:
* Debe poder ser utilizado desde cualquier plataforma: Computadora, celular, Tablet.
* Debe tener un diseño muy simple (usabilidad) y una estética moderna y minimalista.
* Debe tener fuentes legibles.
* No debe tener animaciones ni nada muy complejo, pantallas simples, con la información justa y visible.

## ¿Por qué una Web App en lugar de una hoja de cálculo?
* Si bien herramientas como Excel pueden ser útil, una web app ofrece una solución mucho más robusta, eficiente y escalable para llevar una mejor administración de tus finanzas personales o si la utilizas para administrar algún pequeño negocio.
* A través de una aplicación web, es posible registrar y visualizar ingresos y egresos de forma estructurada, generar reportes visuales automatizados (gráficos, proyecciones, saldo acumulado), compartir los datos con otros usuarios en tiempo real, y acceder desde cualquier dispositivo (PC, celular o tablet) en cualquier lugar sin depender de archivos locales.
* Todo esto mejora la trazabilidad, reduce errores manuales y optimiza la toma de decisiones basada en datos actualizados, algo difícil de lograr de forma confiable en una hoja de cálculo tradicional.