terraform {
  required_version = ">= 1.10, < 2.0"
  required_providers {
    aws = { source = "hashicorp/aws", version = "~> 6.55" }
  }
}

provider "aws" {
  region = "us-east-1"
  default_tags {
    tags = { Project = "mecanica-fase4", Environment = "lab", ManagedBy = "terraform" }
  }
}

variable "admin_principal_arn" {
  description = "IAM role ARN used by the local operator and GitHub deployment (not an STS session ARN)."
  type        = string
  validation {
    condition     = can(regex("^arn:aws:iam::[0-9]{12}:role/.+$", var.admin_principal_arn))
    error_message = "Use the IAM role ARN, not the assumed-role session ARN."
  }
}

variable "api_allowed_cidrs" {
  description = "Public EKS API allowlist. Include the deployment runner's egress network."
  type        = list(string)
  validation {
    condition     = length(var.api_allowed_cidrs) > 0 && alltrue([for c in var.api_allowed_cidrs : can(cidrnetmask(c))])
    error_message = "Provide at least one valid IPv4 CIDR."
  }
}

data "aws_iam_role" "lab" { name = "LabRole" }
data "aws_vpc" "default" { default = true }
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "default-for-az"
    values = ["true"]
  }
}
data "aws_subnet" "default" {
  for_each = toset(data.aws_subnets.default.ids)
  id       = each.value
}
locals {
  subnets = sort([for s in data.aws_subnet.default : s.id if s.availability_zone != "us-east-1e" && s.map_public_ip_on_launch])
}

resource "aws_eks_cluster" "lab" {
  name     = "mecanica-fase4"
  role_arn = data.aws_iam_role.lab.arn
  version  = "1.36"
  upgrade_policy { support_type = "STANDARD" }
  access_config {
    authentication_mode                         = "API"
    bootstrap_cluster_creator_admin_permissions = false
  }
  vpc_config {
    subnet_ids              = local.subnets
    endpoint_private_access = true
    endpoint_public_access  = true
    public_access_cidrs     = var.api_allowed_cidrs
  }
  lifecycle {
    precondition {
      condition     = length(local.subnets) >= 2
      error_message = "At least two default public subnets in supported AZs are required."
    }
  }
}
resource "aws_eks_access_entry" "operator" {
  cluster_name  = aws_eks_cluster.lab.name
  principal_arn = var.admin_principal_arn
  type          = "STANDARD"
}
resource "aws_eks_access_policy_association" "operator" {
  cluster_name  = aws_eks_cluster.lab.name
  principal_arn = aws_eks_access_entry.operator.principal_arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSClusterAdminPolicy"
  access_scope { type = "cluster" }
}

resource "aws_launch_template" "node" {
  name_prefix = "mecanica-fase4-"
  # Standard credits prevent unbounded T3 Unlimited surplus-credit charges.
  credit_specification { cpu_credits = "standard" }
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 2
  }
  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size           = 30
      volume_type           = "gp3"
      encrypted             = true
      delete_on_termination = true
    }
  }
}
resource "aws_eks_node_group" "lab" {
  cluster_name    = aws_eks_cluster.lab.name
  node_group_name = "demonstracao"
  node_role_arn   = data.aws_iam_role.lab.arn
  # One AZ keeps replacements compatible with the zonal EBS volumes.
  subnet_ids     = [local.subnets[0]]
  instance_types = ["t3.xlarge"]
  capacity_type  = "ON_DEMAND"
  ami_type       = "AL2023_x86_64_STANDARD"
  launch_template {
    id      = aws_launch_template.node.id
    version = aws_launch_template.node.latest_version
  }
  scaling_config {
    min_size     = 1
    desired_size = 1
    max_size     = 1
  }
  update_config { max_unavailable = 1 }
  depends_on = [aws_eks_access_policy_association.operator]
}

data "aws_eks_addon_version" "addon" {
  for_each           = toset(["aws-ebs-csi-driver", "metrics-server"])
  addon_name         = each.value
  kubernetes_version = aws_eks_cluster.lab.version
  most_recent        = true
}
resource "aws_eks_addon" "addon" {
  for_each                    = data.aws_eks_addon_version.addon
  cluster_name                = aws_eks_cluster.lab.name
  addon_name                  = each.key
  addon_version               = each.value.version
  resolve_conflicts_on_update = "PRESERVE"
  depends_on                  = [aws_eks_node_group.lab]
}

resource "aws_ecr_repository" "service" {
  for_each             = toset(["mecanica-os-service", "mecanica-billing-service", "mecanica-execution-service"])
  name                 = each.value
  image_tag_mutability = "IMMUTABLE"
  force_delete         = false
  image_scanning_configuration { scan_on_push = true }
}
resource "aws_ecr_lifecycle_policy" "service" {
  for_each   = aws_ecr_repository.service
  repository = each.value.name
  policy = jsonencode({ rules = [{ rulePriority = 1, description = "Keep last ten images", selection = {
    tagStatus = "any", countType = "imageCountMoreThan", countNumber = 10
  }, action = { type = "expire" } }] })
}
output "cluster_name" { value = aws_eks_cluster.lab.name }
output "repositories" { value = { for name, repository in aws_ecr_repository.service : name => repository.repository_url } }
