# CNAPP MSA & Agent Docker Image Build & Push Script (Windows PowerShell)

$DOCKER_ID = "ckstj8027"

Write-Host "Please ensure you are logged in to Docker Hub via 'docker login' before running this script.`n" -ForegroundColor Magenta

# 0. Pre-pull Base Images
Write-Host "Step 0: Pulling necessary base images..." -ForegroundColor Cyan
docker pull gradle:8.5-jdk17
docker pull eclipse-temurin:17-jre-focal
docker pull apache/kafka:3.7.0
docker pull node:20-alpine
docker pull nginx:stable-alpine

# 1. Gradle Build for Backend
Write-Host "`nStep 1: Starting Gradle Build for all modules..." -ForegroundColor Cyan
.\gradlew :agent:bootJar `
          :msa-version:ingestion:application:bootJar `
          :msa-version:analysis:application:bootJar `
          :msa-version:query:application:bootJar `
          :msa-version:auth:application:bootJar

if ($LASTEXITCODE -ne 0) {
    Write-Host "Gradle build failed. Exiting..." -ForegroundColor Red
    exit 1
}

# 2. Docker Image Build & Push
Write-Host "`nStep 2: Building and Pushing Docker images to $DOCKER_ID..." -ForegroundColor Cyan

$images = @(
    @{name="cnapp-msa-ingestion"; path="./msa-version/ingestion"},
    @{name="cnapp-msa-analysis";  path="./msa-version/analysis"},
    @{name="cnapp-msa-query";     path="./msa-version/query"},
    @{name="msa-auth";            path="./msa-version/auth"},
    @{name="cnapp-agent";         path="./agent"},
    @{name="cnapp-front";         path="./cnapp-front"}
)

foreach ($img in $images) {
    $fullName = "$DOCKER_ID/$($img.name):latest"
    Write-Host "`nProcessing $($img.name)..." -ForegroundColor Yellow
    
    # 빌드
    docker build -t $fullName $($img.path)
    
    if ($LASTEXITCODE -eq 0) {
        # 푸시
        Write-Host "Pushing $fullName..." -ForegroundColor Blue
        docker push $fullName
    } else {
        Write-Host "Build failed for $($img.name). Skipping push." -ForegroundColor Red
    }
}

Write-Host "`nAll process completed successfully!" -ForegroundColor Green
Write-Host "=========================================================="
Write-Host "1. Update dependencies: helm dependency update ./msa-version/msa-infra/helm/cnapp-msa"
Write-Host "2. Install MSA:         helm install cnapp-msa ./msa-version/msa-infra/helm/cnapp-msa"
Write-Host "3. Install Agent:       helm install cnapp-agent ./msa-version/msa-infra/helm/cnapp-agent"
Write-Host "=========================================================="
