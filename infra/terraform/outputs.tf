output "rds_endpoint" {
  description = "Endpoint de conexión al RDS (host:port)"
  value       = aws_db_instance.franchises_rds.endpoint
}

output "rds_r2dbc_url" {
  description = "URL R2DBC lista para usar en la variable de entorno R2DBC_URL"
  value       = "r2dbc:postgresql://${aws_db_instance.franchises_rds.endpoint}/${var.db_name}"
}

output "rds_flyway_url" {
  description = "URL JDBC lista para usar en la variable de entorno FLYWAY_URL"
  value       = "jdbc:postgresql://${aws_db_instance.franchises_rds.endpoint}/${var.db_name}"
}

output "rds_instance_id" {
  description = "Identificador de la instancia RDS"
  value       = aws_db_instance.franchises_rds.id
}
