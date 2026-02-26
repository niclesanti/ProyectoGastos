-- Tabla de auditoría para el agente IA
-- Registra todas las interacciones: usuario, workspace, mensaje, respuesta, funciones llamadas, tokens

CREATE TABLE agente_audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL,
    workspace_id UUID NOT NULL,
    user_message VARCHAR(500) NOT NULL,
    agent_response TEXT,
    functions_called VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tokens_used INTEGER,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    
    CONSTRAINT fk_agente_audit_usuario 
        FOREIGN KEY (user_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_agente_audit_espacio 
        FOREIGN KEY (workspace_id) REFERENCES espacios_trabajo(id) ON DELETE CASCADE
);

-- Índices para optimizar consultas de auditoría
CREATE INDEX idx_agente_audit_user_id ON agente_audit_log(user_id);
CREATE INDEX idx_agente_audit_workspace_id ON agente_audit_log(workspace_id);
CREATE INDEX idx_agente_audit_timestamp ON agente_audit_log(timestamp DESC);

-- Comentarios para documentación
COMMENT ON TABLE agente_audit_log IS 'Auditoría completa de interacciones con el agente IA para compliance y analytics';
COMMENT ON COLUMN agente_audit_log.functions_called IS 'Nombres de las tools/funciones que el LLM ejecutó (comma-separated)';
COMMENT ON COLUMN agente_audit_log.tokens_used IS 'Tokens consumidos en la interacción (input + output)';
