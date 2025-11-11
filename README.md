[![🐳 Build + Publish Multi-Platform Image](https://github.com/PrzemyslawSwiderski/printamos/actions/workflows/docker-push.yml/badge.svg)](https://github.com/PrzemyslawSwiderski/printamos/actions/workflows/docker-push.yml)

# Printamos

## Purpose

Simple app to upload printing jobs in a home internal network with a web UI.

## Usage

Frontend app lets the user printing the files with a simple select window or drag and drop.

![FE Application](img/ui.png)

## Features

- Auto-discovers the printer with Avahi.
- Bootstrap based fully responsive styling.
- Uses the [OpenPrinting CUPS](https://openprinting.github.io/cups/) tool as backend.
- Drag & Drop files printing.
- Minimal Docker Alpine image ~400MB.
- Ktor Server API.

## Docker Compose

It is possible to easily add the Printamos service as follows:

```yaml
  printamos:
    container_name: "printamos"
    image: "ghcr.io/przemyslawswiderski/printamos:latest"
    volumes:
      - "printamos-data:/etc/cups"
    network_mode: "host"
    environment:
      PUBLIC_HOSTNAME: "printamos-example.com" # Public host name for the CSRF allowance
      TZ: "Europe/Warsaw"
      KTOR_PORT: "8097"
    restart: "always"
    tmpfs:
      - "/run" # Needed for the clean restart possibility
      - "/tmp"
```

The Printamos Web UI will be available at `http://localhost:8097` on host.
Standard CUPS Admin Web UI to manage printers should be also available at `http://localhost:631`.

## [Releases](https://github.com/PrzemyslawSwiderski/printamos/releases)