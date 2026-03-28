$ErrorActionPreference = "Stop"

$root = "C:\Users\Andre\Desktop\iboi\iboi"
Set-Location $root

$env:GRADLE_USER_HOME = "$root\.gradle-user"
$env:APP_SEED_ENABLED = "true"
$env:ASAAS_ENABLED = "true"
$env:ASAAS_BASE_URL = "https://api-sandbox.asaas.com"
$env:ASAAS_API_KEY = '$aact_hmlg_000MzkwODA2MWY2OGM3MWRlMDU2NWM3MzJlNzZmNGZhZGY6OmFjM2ZkZDg2LTczZjAtNGZmNS05NmRlLWQ2NmJiNGRlMjA1NTo6JGFhY2hfNzQ4MmFiNzQtNjhmMC00YzZhLTg0NWMtMGM4NWQyMjNkOGFl'
$env:ASAAS_WEBHOOK_TOKEN = "whsec_he9J1HvvMyyDFUy2ETTGE906l5eqeQiwXDEoGZDl4R8"

& ".\gradlew.bat" "bootRun"
