# Build MSA services and save full log to build-log.txt
# Usage: .\build-and-log.ps1
.\gradlew :msa-version:ingestion:application:build `
          :msa-version:auth:application:build `
          :msa-version:query:application:build `
          :msa-version:analysis:application:build `
          --stacktrace --console=plain *> build-log.txt

Write-Host "Done. Log: build-log.txt (exit code: $LASTEXITCODE)"
