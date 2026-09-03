package com.campito.backend.common.test;

/**
 * Fachada central para creación de datos de prueba.
 * Delega a factories específicas por módulo.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    // Usuarios — acceso directo a la clase (solo tiene métodos estáticos)
    // Usar: UsuariosTestDataFactory.crearUsuario(...)

    // Transacciones — acceso directo a la clase
    // Usar: TransaccionesTestDataFactory.crearTransaccionResponse(...)

    // Notificaciones — acceso directo a la clase
    // Usar: NotificacionesTestDataFactory.crearNotificacionResponse(...)

    // Descuentos — acceso directo a la clase
    // Usar: DescuentosTestDataFactory.crearDescuentoResponse(...)

    // Dashboard — acceso directo a la clase
    // Usar: DashboardTestDataFactory.crearDistribucionGasto(...)

    // Common — acceso directo a la clase
    // Usar: CommonTestDataFactory.crearDistribucionGasto(...)
}
