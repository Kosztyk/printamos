[![🐳 Build + Publish Multi-Platform Image](https://github.com/PrzemyslawSwiderski/printamos/actions/workflows/docker-push.yml/badge.svg)](https://github.com/PrzemyslawSwiderski/printamos/actions/workflows/docker-push.yml)

# Printamos

## Purpose

Simple app to upload printing jobs in a home internal network with a web UI.

## Features

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
    ports:
      - "8097:8080"
      - "8098:631"
    volumes:
      - "printamos-data:/etc/cups"
    environment:
      PUBLIC_HOSTNAME: "printamos-example.com" # Public host name for the CSRF allowance
```

The Printamos Web UI will be available at `http://localhost:8097` on host.
Standard CUPS Admin Web UI to manage printers should be also available at `http://localhost:8098`.

## Usage

Frontend app lets the user printing the files with a simple select window or drag and drop.

![FE Application](img/ui.png)

Managing the printers or checking active jobs can still be done with CUPS Admin Web UI.

## [Releases](https://github.com/PrzemyslawSwiderski/printamos/releases)