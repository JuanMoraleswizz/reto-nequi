terraform {
  required_version = ">= 1.5.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.gcp_project
  region  = var.gcp_region
}

# ── Private IP range for VPC peering ─────────────────────────────────────────
# Cloud SQL con IP privada requiere un rango reservado en el VPC
# y una conexión de peering con servicenetworking.googleapis.com

resource "google_compute_global_address" "private_ip_range" {
  name          = "franchises-cloudsql-private-ip"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = var.vpc_network
}

resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = var.vpc_network
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_range.name]
}

# ── Firewall rule — permitir acceso al puerto 5432 ───────────────────────────

resource "google_compute_firewall" "allow_postgres" {
  name    = "franchises-allow-postgres"
  network = var.vpc_network

  allow {
    protocol = "tcp"
    ports    = ["5432"]
  }

  source_ranges = var.allowed_cidr_blocks

  target_tags = ["franchises-app"]

  description = "Permite acceso PostgreSQL a la app de franquicias"
}

# ── Cloud SQL Instance ────────────────────────────────────────────────────────

resource "google_sql_database_instance" "franchises_db" {
  name             = "franchises-cloudsql"
  database_version = "POSTGRES_16"
  region           = var.gcp_region

  # Depende del peering para poder usar IP privada
  depends_on = [google_service_networking_connection.private_vpc_connection]

  settings {
    tier              = "db-f1-micro"
    availability_type = "ZONAL"
    disk_size         = 20
    disk_type         = "PD_SSD"
    disk_autoresize   = true

    ip_configuration {
      ipv4_enabled    = false # Sin IP pública
      private_network = var.vpc_network
    }

    backup_configuration {
      enabled    = true
      start_time = "03:00"
      backup_retention_settings {
        retained_backups = 7
      }
    }

    maintenance_window {
      day  = 1 # Lunes
      hour = 4
    }

    database_flags {
      name  = "max_connections"
      value = "100"
    }
  }

  deletion_protection = false
}

# ── Base de datos ─────────────────────────────────────────────────────────────

resource "google_sql_database" "franchises" {
  name     = var.db_name
  instance = google_sql_database_instance.franchises_db.name
}

# ── Usuario ───────────────────────────────────────────────────────────────────

resource "google_sql_user" "app_user" {
  name     = var.db_username
  instance = google_sql_database_instance.franchises_db.name
  password = var.db_password
}
