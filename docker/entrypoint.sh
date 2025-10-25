#!/bin/sh
# Minimal entrypoint: start cupsd (if present) and exec Java.
# This does not wait for cupsd to become ready.

if command -v cupsd >/dev/null 2>&1; then
  echo "Starting cupsd..."
  # Start cupsd and allow it to daemonize; fail the container if it cannot start.
  if ! cupsd >/dev/null 2>&1; then
    echo "Failed to start cupsd" >&2
    exit 1
  fi
fi

# Replace the shell with the Java process so it receives signals correctly.
exec java --enable-native-access=ALL-UNNAMED -jar /app/server.jar