-- NOTA: Con spring.jpa.hibernate.ddl-auto=update, Spring Boot crea automáticamente las tablas
-- Solo ejecuta estos índices manualmente si necesitas optimización adicional

-- Índices para mejorar rendimiento
CREATE INDEX idx_user_connections_status ON user_connections(status);
CREATE INDEX idx_user_connections_requester ON user_connections(requester_id);
CREATE INDEX idx_user_connections_receiver ON user_connections(receiver_id);

CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

CREATE INDEX idx_project_members_project ON project_members(project_id);
CREATE INDEX idx_project_members_user ON project_members(user_id);

CREATE INDEX idx_tasks_created_by ON tasks(created_by_id);
CREATE INDEX idx_tasks_created_at ON tasks(created_at);
