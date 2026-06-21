package it.polimi.ingsw;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientMainTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        // Redirect standard output to capture and verify console messages
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // Always restore the original standard output after each test
        System.setOut(originalOut);
    }

    // ==========================================
    // CLI PARSING TESTS
    // ==========================================

    @Test
    void testMain_NoArguments() {
        // Execute main with empty arguments array
        ClientMain.main(new String[]{});

        String output = outContent.toString();
        // Verify that the usage instructions are printed correctly
        assertTrue(output.contains("Use:"), "Should print the usage block");
        assertTrue(output.contains("java -jar mesos-client.jar tui"), "Should contain TUI example");
    }

    @Test
    void testMain_MissingHost_NotEnoughArguments() {
        // Execute main providing mode and protocol, but skipping the host IP
        ClientMain.main(new String[]{"tui", "rmi"});

        String output = outContent.toString();
        // Verify that it detects the missing host and warns the user
        assertTrue(output.contains("Missing server host"), "Should warn about missing host");
    }

    @Test
    void testMain_MissingHost_BlankHost() {
        // Execute main providing mode, protocol, but a blank string as the host
        ClientMain.main(new String[]{"gui", "socket", "   "});

        String output = outContent.toString();
        // Verify that the isBlank() check catches the empty string
        assertTrue(output.contains("Missing server host"), "Should catch blank host strings");
    }

    // ==========================================
    // EXECUTION FLOW TESTS (MAX COVERAGE)
    // ==========================================

    @Test
    void testMain_ValidArguments_DelegatesToApp() {
        /*
         * TRICK FOR 100% COVERAGE WITHOUT HANGING:
         * We pass a completely fake mode ("invalid_mode").
         * ClientMain will successfully parse it along with the protocol and the IP,
         * and it will correctly trigger the `App.startClient(...)` line (covering it).
         * Inside `App.startClient`, the unknown mode will trigger the default switch case,
         * immediately returning without starting any blocking TUI or GUI lifecycle.
         */
        ClientMain.main(new String[]{"invalid_mode", "rmi", "127.0.0.1"});

        String output = outContent.toString();

        // We verify that App.startClient was actually reached and executed
        // because it prints its own error message for unrecognized modes.
        assertTrue(output.contains("Client mode not recognized: invalid_mode"),
                "Should reach App.startClient and trigger its default fallback");
    }
}