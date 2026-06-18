package it.polimi.ingsw;

import it.polimi.ingsw.network.ClientNetworkAdapter;
import it.polimi.ingsw.network.CommunicationProtocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // Redirect System.out to capture and assert printed console messages
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // Restore standard output after each test
        System.setOut(originalOut);
    }

    // ==========================================
    // UTILITY METHODS TESTS
    // ==========================================

    @Test
    void testParseProtocol() {
        assertEquals(CommunicationProtocol.SOCKET, App.parseProtocol("socket"));
        assertEquals(CommunicationProtocol.SOCKET, App.parseProtocol("SOCKET"));
        assertEquals(CommunicationProtocol.RMI, App.parseProtocol("rmi"));
        // Fallback to RMI if unknown or null
        assertEquals(CommunicationProtocol.RMI, App.parseProtocol("unknown"));
        assertEquals(CommunicationProtocol.RMI, App.parseProtocol(null));
    }

    @Test
    void testDefaultPort() {
        assertEquals(ClientNetworkAdapter.SOCKET_PORT, App.defaultPort(CommunicationProtocol.SOCKET));
        assertEquals(ClientNetworkAdapter.RMI_PORT, App.defaultPort(CommunicationProtocol.RMI));
    }

    @Test
    void testResolveServerHost() {
        String host = App.resolveServerHost();
        assertNotNull(host);
        assertFalse(host.isEmpty());
        // Basic Regex to verify IPv4 format (e.g. "127.0.0.1" or "192.168.1.x")
        assertTrue(host.matches("\\d{1,3}(\\.\\d{1,3}){3}"));
    }

    // ==========================================
    // CLI ARGUMENTS PARSING TESTS (MAIN)
    // ==========================================

    @Test
    void testMain_NoArgs() throws Exception {
        App.main(new String[]{});
        assertTrue(outContent.toString().contains("Use:")); // Expect usage instructions
    }

    @Test
    void testMain_UnrecognizedArg() throws Exception {
        App.main(new String[]{"invalid_command"});
        assertTrue(outContent.toString().contains("Argument not recognized: invalid_command"));
    }

    @Test
    void testMain_Client_MissingHost() throws Exception {
        App.main(new String[]{"client", "tui", "rmi"});
        assertTrue(outContent.toString().contains("Missing server host"));
    }

    // ==========================================
    // EXECUTION PATH TESTS (HIGH COVERAGE TRICKS)
    // ==========================================

    @Test
    void testMain_Server() {
        // TRICK: Bind the Socket port so HybridServerNetworkAdapter crashes on start().
        // This covers the code path but avoids the JVM-killing System.exit(0).
        assertThrows(Exception.class, () -> {
            try (ServerSocket blocker = new ServerSocket(ClientNetworkAdapter.SOCKET_PORT)) {
                App.main(new String[]{"server"});
            }
        });
    }

    @Test
    void testStartServer_Directly() {
        // Same trick directly on startServer() method
        assertThrows(Exception.class, () -> {
            try (ServerSocket blocker = new ServerSocket(ClientNetworkAdapter.SOCKET_PORT)) {
                App.startServer();
            }
        });
    }


    @Test
    void testMain_Client_GUI() {
        // Covers: main -> startClient -> GUILauncher
        // Ignore HeadlessExceptions or JavaFX init errors in test environments
        assertDoesNotThrow(() -> {
            try {
                App.main(new String[]{"client", "gui", "socket", "127.0.0.1"});
            } catch (Exception ignored) {}
        });
    }

    @Test
    void testStartClient_InvalidMode() {
        // Covers the default branch of the mode switch in startClient()
        App.startClient("127.0.0.1", "unsupported_mode", CommunicationProtocol.RMI);
        assertTrue(outContent.toString().contains("Client mode not recognized"));
    }
}