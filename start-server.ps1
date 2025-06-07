param (
    [string]$envFilePath = "./.env"
)

# Function to read specific keys from .env
function Get-EnvValue($path, $key) {
    if (-not (Test-Path $path)) {
        Write-Error "Env file not found at path: $path"
        exit 1
    }

    $line = Select-String -Path $path -Pattern "^$key\s*=" | Select-Object -First 1

    if (-not $line) {
        Write-Error "Property '$key' not found in $path"
        exit 1
    }

    $value = $line.Line -replace "^$key\s*=\s*", ""
    return $value.Trim('"')
}

# Manually specify which properties you want to read
$ENVIRONMENT = Get-EnvValue $envFilePath "ENVIRONMENT"
$DOPPLER_PROJECT_NAME = Get-EnvValue $envFilePath "DOPPLER_PROJECT_NAME"
$DOPPLER_SERVICE_TOKEN = Get-EnvValue $envFilePath "DOPPLER_SERVICE_TOKEN"
# --- Print the properties ---
Write-Host "`n=== Loaded Properties from .env ==="
Write-Host "ENVIRONMENT: $ENVIRONMENT"
Write-Host "DOPPLER_PROJECT_NAME: $DOPPLER_PROJECT_NAME"
Write-Host "DOPPLER_SERVICE_TOKEN: ***"
Write-Host "==================================`n"



# Checkout stage branch
Write-Host "Checking out 'stage' branch..."
git checkout stage
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Pulling latest changes..."
git pull origin stage
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# Build project
Write-Host "Building Maven project..."
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# Move into the app directory
Set-Location ".\ngo-nabarun-app"

# Locate JAR file
$jarFile = Get-ChildItem -Path .\target\*.jar | Where-Object {
    $_.Name -notlike "*sources.jar" -and $_.Name -notlike "*javadoc.jar"
} | Select-Object -First 1

if (-not $jarFile) {
    Write-Error "No executable JAR found."
    exit 1
}



Write-Host "Starting Spring Boot application with: $($jarFile.Name)"

# Run the JAR with system properties
java -DENVIRONMENT="$ENVIRONMENT" -DDOPPLER_PROJECT_NAME="$DOPPLER_PROJECT_NAME" -DDOPPLER_SERVICE_TOKEN="$DOPPLER_SERVICE_TOKEN" -jar $jarFile.FullName
