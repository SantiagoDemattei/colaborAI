package com.colaborai.colaborai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseIndexInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            createIndexSafely("CREATE INDEX idx_user_connections_status ON user_connections(status)");
            createIndexSafely("CREATE INDEX idx_user_connections_requester ON user_connections(requester_id)");
            createIndexSafely("CREATE INDEX idx_user_connections_receiver ON user_connections(receiver_id)");
            
            createIndexSafely("CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read)");
            createIndexSafely("CREATE INDEX idx_notifications_created_at ON notifications(created_at)");
            
            createIndexSafely("CREATE INDEX idx_project_members_project ON project_members(project_id)");
            createIndexSafely("CREATE INDEX idx_project_members_user ON project_members(user_id)");
            
            createIndexSafely("CREATE INDEX idx_tasks_created_by ON tasks(created_by_id)");
            createIndexSafely("CREATE INDEX idx_tasks_created_at ON tasks(created_at)");
            
            System.out.println("✅ Índices de base de datos verificados/creados exitosamente");
        } catch (Exception e) {
            System.out.println("⚠️ Algunos índices ya existían o hubo un problema: " + e.getMessage());
        }
    }

    private void createIndexSafely(String sql) {
        try {
            jdbcTemplate.execute(sql);
            System.out.println("✅ Índice creado: " + sql.substring(0, Math.min(50, sql.length())) + "...");
        } catch (Exception e) {
            System.out.println("⚠️ Índice ya existe o error: " + sql.substring(0, Math.min(50, sql.length())) + "...");
        }
    }
}
