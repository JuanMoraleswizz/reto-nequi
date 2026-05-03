output "cloudsql_instance_name" {
  description = "Nombre de la instancia Cloud SQL"
  value       = google_sql_database_instance.franchises_db.name
}

output "cloudsql_private_ip" {
  description = "IP privada de Cloud SQL (usar como host en las URLs de conexión)"
  value       = google_sql_database_instance.franchises_db.private_ip_address
}

output "cloudsql_r2dbc_url" {
  description = "URL R2DBC lista para usar en la variable de entorno R2DBC_URL"
  value       = "r2dbc:postgresql://${google_sql_database_instance.franchises_db.private_ip_address}:5432/${var.db_name}"
}

output "cloudsql_flyway_url" {
  description = "URL JDBC lista para usar en la variable de entorno FLYWAY_URL"
  value       = "jdbc:postgresql://${google_sql_database_instance.franchises_db.private_ip_address}:5432/${var.db_name}"
}

output "cloudsql_connection_name" {
  description = "Connection name de Cloud SQL (útil para Cloud SQL Auth Proxy)"
  value       = google_sql_database_instance.franchises_db.connection_name
}
