FROM eclipse-temurin:25-jre-alpine

# Install cups and dbus, clean up apk cache to reduce image size
RUN apk add --no-cache \
    cups \
    dbus \
    && rm -rf /var/cache/apk/*

# Create non-root user and group, add appuser to lp group
RUN addgroup -S appgroup && adduser -S -G appgroup appuser \
    && addgroup appuser lp \
    && addgroup appuser lpadmin

# Adjust CUPS configuration permissions for non-root user
RUN mkdir -p /var/run/cups /var/spool/cups /var/log/cups \
    && chown -R appuser:appgroup /var/run/cups /var/spool/cups /var/log/cups /usr/lib/cups /etc/cups

# Create application directory and set ownership
WORKDIR /app
RUN chown appuser:appgroup /app

# Copy the server JAR and set ownership
COPY --chown=appuser:appgroup build/libs/printamos-all.jar /app/server.jar
COPY --chown=appuser:appgroup cupsd.conf /etc/cups/cupsd.conf

# Switch to non-root user
USER appuser

# Expose the port for the Java server (8080)
EXPOSE 8080

CMD ["sh", "-c", "cupsd -c /etc/cups/cupsd.conf -s /etc/cups/cups-files.conf && java --enable-native-access=ALL-UNNAMED -jar /app/server.jar"]
