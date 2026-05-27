# MESOS

## Build

```zsh
mvn -DskipTests package
```

Artifacts will be generated under `target/`:
- `mesos-client.jar`
- `mesos-server.jar`

## Run

lServer (with DB env vars):

```zsh
DB_URL="jdbc:mysql://localhost:3306/mesos" DB_USER="root" DB_PASSWORD="<password>" java -jar target/mesos-server.jar
```

Server (no inline env vars, uses current shell env):

```zsh
java -jar target/mesos-server.jar
```

Client TUI (RMI):

```zsh
java -jar target/mesos-client.jar tui rmi 1099 <server-ip>
```

Client GUI (SOCKET):

```zsh
java -jar target/mesos-client.jar gui socket 9999 <server-ip>
```

## Notes

- Use `SERVER_HOST` to force the server bind address on LAN.
- RMI default port is `1099`, socket default port is `9999`.
- DB env vars: `DB_URL`, `DB_USER`, `DB_PASSWORD`.
- Find server IP:
  - Linux: `ip -4 addr | grep -E "inet " | grep -v 127.0.0.1`
  - macOS: `ipconfig getifaddr en0` (try `en1` if empty)
  - Windows (PowerShell): `Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -ne "127.0.0.1" }`
