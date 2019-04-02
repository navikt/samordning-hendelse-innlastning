path "sys/renew/*" {
  capabilities = ["update"]
}

path "secrets/test/creds/postgres-user" {
  capabilities = ["read"]
}