# Distributed Drinks Sales System

This project is now a Java desktop GUI client and socket server backed by a MySQL/XAMPP database.

## What Changed
- Branch users now place orders from a Swing GUI with aligned fields, quantity controls, and live server actions.
- Administrators now use a GUI dashboard with sales, orders, stock, and low-stock alerts.
- The HQ server now persists orders, stock, alerts, and reports in MySQL instead of in-memory state.
- Branch and admin clients still communicate with the HQ server over Java sockets.

## Main Entry Points
- Server: `com.dds.server.HQServer`
- Branch GUI: `com.dds.client.CustomerClient`
- Admin GUI: `com.dds.client.AdminClient`
- Combined GUI: `com.dds.client.DesktopLauncher`

## Database
1. Start MySQL from XAMPP.
2. Import `xampp_schema.sql` into MySQL.
3. Make sure the MySQL JDBC driver JAR is available when you run the server and clients.

Default database connection values used by the server:
- URL: `jdbc:mysql://127.0.0.1:3306/distributed_drinks_business?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- User: `root`
- Password: empty

You can override them with:
- `-Ddds.db.url=...`
- `-Ddds.db.user=...`
- `-Ddds.db.password=...`

## Compile
```powershell
$files = Get-ChildItem -Recurse .\src -Filter *.java | Select-Object -ExpandProperty FullName
javac -d .\out $files
```

## Run
Example with MySQL Connector JAR at `.\lib\mysql-connector-j-9.3.0.jar`:

Start server:
```powershell
java -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.server.HQServer 5050
```

Run combined GUI:
```powershell
java -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.client.DesktopLauncher
```

Run branch GUI only:
```powershell
java -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.client.CustomerClient
```

Run admin GUI only:
```powershell
java -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.client.AdminClient
```
