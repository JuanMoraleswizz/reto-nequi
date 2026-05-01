terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# ── Security Group ────────────────────────────────────────────────────────────

resource "aws_security_group" "rds_sg" {
  name        = "franchises-rds-sg"
  description = "Allow PostgreSQL access to Franchises RDS"
  vpc_id      = var.vpc_id

  ingress {
    description = "PostgreSQL"
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidr_blocks
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "franchises-rds-sg"
    Project     = "nequi-franchises"
    Environment = var.environment
  }
}

# ── Subnet Group ──────────────────────────────────────────────────────────────

resource "aws_db_subnet_group" "rds_subnet_group" {
  name       = "franchises-rds-subnet-group"
  subnet_ids = var.subnet_ids

  tags = {
    Name        = "franchises-rds-subnet-group"
    Project     = "nequi-franchises"
    Environment = var.environment
  }
}

# ── RDS Instance ──────────────────────────────────────────────────────────────

resource "aws_db_instance" "franchises_rds" {
  identifier            = "franchises-rds"
  engine                = "postgres"
  engine_version        = "16"
  instance_class        = "db.t3.micro"
  allocated_storage     = 20
  max_allocated_storage = 100
  storage_type          = "gp2"
  storage_encrypted     = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.rds_subnet_group.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]

  publicly_accessible = false
  multi_az            = false
  deletion_protection = false
  skip_final_snapshot = true

  backup_retention_period = 7
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"

  tags = {
    Name        = "franchises-rds"
    Project     = "nequi-franchises"
    Environment = var.environment
  }
}
