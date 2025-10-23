# Printamos

## Purpose

Simple frontend app and server to upload printing jobs in a home internal network.

## UI

Frontend app lets the user add the printers and print the files with
a simple select window or drag and drop.

![FE Application](img/ui.png)

## Docker Compose

```yaml
  printamos:
    container_name: "printamos"
    image: "ghcr.io/przemyslawswiderski/printamos:latest"
    ports:
      - "8097:8080"
    environment:
      PUBLIC_HOSTNAME: "printamos-example.com" # Public host name for the CSRF allowance
```

