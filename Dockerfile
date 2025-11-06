FROM eclipse-temurin:25-jre-alpine

# install runtime packages and tini (small init)
RUN apk add --no-cache \
    cups \
    cups-filters \
    brlaser \
    epson-inkjet-printer-escpr \
    dbus \
    ipptool \
    tini \
    && addgroup -S appgroup \
    && adduser -S -G appgroup appuser \
    && addgroup appuser lp \
    && addgroup appuser lpadmin \
    && mkdir -p /var/run/cups /var/spool/cups /var/log/cups /app \
    && chown -R appuser:appgroup /var/run/cups /var/spool/cups /var/log/cups /usr/lib/cups /etc/cups /app

WORKDIR /app

# copy jar and config, set ownership in one step
COPY --chown=appuser:appgroup build/libs/printamos-all.jar /app/server.jar
COPY --chown=appuser:appgroup cupsd.conf /etc/cups/cupsd.conf

# Add a small entrypoint script (create it or COPY it from repo)
COPY --chown=appuser:appgroup docker/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

USER appuser

# Printamos Web UI
EXPOSE 8080
# Cups Admin
EXPOSE 631

ENTRYPOINT ["/sbin/tini", "--", "/app/entrypoint.sh"]