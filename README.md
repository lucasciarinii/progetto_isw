# MESOS

## Build

```zsh
mvn -DskipTests package
```

Artifacts will be generated under `target/`:
- `mesos-client.jar`
- `mesos-server.jar`

## Run

Server:

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

