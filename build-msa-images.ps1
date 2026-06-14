# CNAPP MSA Docker Image Build Script (Windows PowerShell)

Write-Host "Starting Gradle Build for all MSA modules..." -ForegroundColor Cyan
.\gradlew :msa-version:msa-common:jar :msa-version:msa-ingestion:bootJar :msa-version:msa-analysis:bootJar :msa-version:msa-query:bootJar

if ($LASTEXITCODE -ne 0) {
    Write-Host "Gradle build failed. Exiting..." -ForegroundColor Red
    exit 1
}

Write-Host "Building Docker images..." -ForegroundColor Cyan

# 1. Ingestion Service
Write-Host "Building msa-ingestion..." -ForegroundColor Yellow
docker build -t cnapp-msa-ingestion:latest ./msa-version/msa-ingestion

# 2. Analysis Service
Write-Host "Building msa-analysis..." -ForegroundColor Yellow
docker build -t cnapp-msa-analysis:latest ./msa-version/msa-analysis

# 3. Query Service
Write-Host "Building msa-query..." -ForegroundColor Yellow
docker build -t cnapp-msa-query:latest ./msa-version/msa-query

Write-Host "All MSA Docker images built successfully!" -ForegroundColor Green
Write-Host "You can now install them using Helm:" -ForegroundColor Cyan
Write-Host "helm install cnapp-msa ./msa-version/msa-infra/helm/cnapp-msa"
