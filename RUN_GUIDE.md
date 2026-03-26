# Direct Commands To Run

Project folder:
```powershell
Set-Location e:\dds
```

## 1. Compile
If `javac` is already on PATH:
```powershell
$files = Get-ChildItem -Recurse .\src -Filter *.java | Select-Object -ExpandProperty FullName
javac -d .\out $files
```

If you want the full JDK path:
```powershell
$files = Get-ChildItem -Recurse .\src -Filter *.java | Select-Object -ExpandProperty FullName
& "C:\Program Files\Java\jdk-26\bin\javac.exe" -d .\out $files
```

## 2. Import Database
Import `xampp_schema.sql` into MySQL/XAMPP.

## 3. Put MySQL JDBC Driver In `.\lib`
Example file:
```powershell
e:\dds\lib\mysql-connector-j-9.3.0.jar
```

## 4. Start The Server
```powershell
java -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.server.HQServer 5050
```

If you need custom database settings:
```powershell
java -Ddds.db.url="jdbc:mysql://127.0.0.1:3306/distributed_drinks_business?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" -Ddds.db.user=root -Ddds.db.password="" -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.server.HQServer 5050
```

## 5. Run The Combined GUI
This opens both the branch desk and the admin dashboard in one window:
```powershell
java -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.client.DesktopLauncher
```

## 6. Or Run Each GUI Separately
Branch GUI:
```powershell
java -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.client.CustomerClient
```

Admin GUI:
```powershell
java -cp ".\out;.\lib\mysql-connector-j-9.3.0.jar" com.dds.client.AdminClient
```

## Notes
- Host in the GUI should normally be `127.0.0.1` on the server machine or the HQ machine IP on branch machines.
- Port should match the server port, for example `5050`.
- The branch and admin GUI both connect to the socket server, and the server writes and reads from MySQL.
