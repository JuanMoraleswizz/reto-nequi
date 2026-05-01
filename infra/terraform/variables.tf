variable "aws_region" {
  description = "AWS region donde se despliega RDS"
  type        = string
  default     = "us-east-1"
}

variable "db_name" {
  description = "Nombre de la base de datos"
  type        = string
  default     = "franchises_db"
}

variable "db_username" {
  description = "Usuario maestro de RDS"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Contraseña maestra de RDS"
  type        = string
  sensitive   = true
}

variable "vpc_id" {
  description = "ID del VPC donde se creará el RDS"
  type        = string
}

variable "subnet_ids" {
  description = "Lista de subnet IDs para el subnet group (mínimo 2 en distintas AZs)"
  type        = list(string)
}

variable "allowed_cidr_blocks" {
  description = "CIDRs con acceso al puerto 5432"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "environment" {
  description = "Nombre del entorno (production, staging, etc.)"
  type        = string
  default     = "production"
}
