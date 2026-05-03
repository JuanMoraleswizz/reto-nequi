variable "gcp_project" {
  description = "ID del proyecto de Google Cloud"
  type        = string
}

variable "gcp_region" {
  description = "Región de GCP donde se despliega Cloud SQL"
  type        = string
  default     = "us-central1"
}

variable "vpc_network" {
  description = "Self-link o nombre del VPC donde se conectará Cloud SQL (p.ej. projects/MY_PROJECT/global/networks/default)"
  type        = string
}

variable "db_name" {
  description = "Nombre de la base de datos"
  type        = string
  default     = "franchises_db"
}

variable "db_username" {
  description = "Usuario de la base de datos"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Contraseña del usuario de la base de datos"
  type        = string
  sensitive   = true
}

variable "allowed_cidr_blocks" {
  description = "CIDRs con acceso al puerto 5432 (firewall rule)"
  type        = list(string)
  default     = ["10.0.0.0/8"]
}

variable "environment" {
  description = "Nombre del entorno (production, staging, etc.)"
  type        = string
  default     = "production"
}
