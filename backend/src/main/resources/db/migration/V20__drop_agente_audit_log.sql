-- Eliminación de la tabla de auditoría del agente IA
-- El agente IA fue removido de la plataforma, se elimina su tabla y índices asociados

DROP INDEX IF EXISTS idx_agente_audit_timestamp;
DROP INDEX IF EXISTS idx_agente_audit_workspace_id;
DROP INDEX IF EXISTS idx_agente_audit_user_id;

DROP TABLE IF EXISTS agente_audit_log;
