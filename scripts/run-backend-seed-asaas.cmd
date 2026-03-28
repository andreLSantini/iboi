@echo off
setlocal

cd /d C:\Users\Andre\Desktop\iboi\iboi

set GRADLE_USER_HOME=C:\Users\Andre\Desktop\iboi\iboi\.gradle-user
set APP_SEED_ENABLED=true
set ASAAS_ENABLED=true
set ASAAS_BASE_URL=https://api-sandbox.asaas.com
set ASAAS_API_KEY=$aact_hmlg_000MzkwODA2MWY2OGM3MWRlMDU2NWM3MzJlNzZmNGZhZGY6OmFjM2ZkZDg2LTczZjAtNGZmNS05NmRlLWQ2NmJiNGRlMjA1NTo6JGFhY2hfNzQ4MmFiNzQtNjhmMC00YzZhLTg0NWMtMGM4NWQyMjNkOGFl
set ASAAS_WEBHOOK_TOKEN=whsec_he9J1HvvMyyDFUy2ETTGE906l5eqeQiwXDEoGZDl4R8

call gradlew.bat bootRun
