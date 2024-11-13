package edu.drexel.se320;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;
import java.util.List;
import java.io.IOException;

public class MockTests {

    public MockTests() {}

    /**
     * Demonstrate a working mock from the Mockito documentation.
     * https://static.javadoc.io/org.mockito/mockito-core/latest/org/mockito/Mockito.html#1
     */
    @Test
    public void testMockDemo() {
         List<String> mockedList = (List<String>)mock(List.class);

         mockedList.add("one");
         mockedList.clear();

         verify(mockedList).add("one");
         verify(mockedList).clear();
    }

    @Test
    public void testServerConnectionFailureGivesNull() throws IOException {
        Client c = new Client();
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(false);

        // If you change the code to pass the mock above to the client (based on your choice of
        // refactoring), this test should pass.  Until then, it will fail.
        assertNull(c.requestFile("DUMMY", "DUMMY"));
    }

    /**
     * Test 1: If connectTo fails, no further methods should be called
     */
    @Test
    public void test1() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(false);

        Client c = new Client(sc);
        c.requestFile("server", "file");

        verify(sc).connectTo(anyString());
        verifyNoMoreInteractions(sc);
    }

    /**
     * Test 2: If connection succeeds but file is invalid, only closeConnection should be called after
     */
    @Test
    public void test2() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(false);

        Client c = new Client(sc);
        c.requestFile("server", "file");

        InOrder inOrder = inOrder(sc);
        inOrder.verify(sc).connectTo(anyString());
        inOrder.verify(sc).requestFileContents(anyString());
        verifyNoMoreInteractions(sc);  // No closeConnection should be called
    }

    /**
     * Test 3: If connection and file are valid, client should request file contents
     */
    @Test
    public void test3() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(false);

        Client c = new Client(sc);
        c.requestFile("server", "file");

        verify(sc).requestFileContents(anyString());
    }

    /**
     * Test 4: Empty file should return empty string
     */
    @Test
    public void test4() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(false);

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertEquals("", result);
    }

    /**
     * Test 5: IOException during read should return null
     */
    @Test
    public void test5() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true);
        when(sc.read()).thenThrow(new IOException());

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertNull(result);
    }

    /**
     * Test 6: IOException during read should still close connection
     */
    @Test
    public void test6() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true);
        when(sc.read()).thenThrow(new IOException());

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertNull(result);

        InOrder inOrder = inOrder(sc);
        inOrder.verify(sc).connectTo("server");
        inOrder.verify(sc).requestFileContents("file");
        inOrder.verify(sc).moreBytes();
        inOrder.verify(sc).read();
        verifyNoMoreInteractions(sc);
    }

    /**
     * Test 7: File starting with "override" should be returned unmodified
     */
    @Test
    public void test7() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, false);
        when(sc.read()).thenReturn("override test");

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertEquals("override test", result);
    }

    /**
     * Test 8: Four pieces should be concatenated in correct order
     */
    @Test
    public void test8() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, true, true, true, false);
        when(sc.read()).thenReturn("1", "2", "3", "4");

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertEquals("1234", result);
    }

    /**
     * Test 9: Null read should be treated as empty string
     */
    @Test
    public void test9() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true, true, false);
        when(sc.read()).thenReturn("test", (String) null);

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertEquals("test", result);
    }

    /**
     * Test 10a: IOException on connectTo should return null
     */
    @Test
    public void test10a() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenThrow(new IOException());

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertNull(result);
    }

    /**
     * Test 10b: IOException on requestFileContents should return null
     */
    @Test
    public void test10b() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenThrow(new IOException());

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertNull(result);
    }

    /**
     * Test 10c: IOException on read should return null
     */
    @Test
    public void test10c() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(true);
        when(sc.read()).thenThrow(new IOException());

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertNull(result);
    }

    /**
     * Test 10d: IOException on moreBytes should return null
     */
    @Test
    public void test10d() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenThrow(new IOException());

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertNull(result);
    }

    /**
     * Test 10e: IOException on closeConnection should return null
     */
    @Test
    public void test10e() throws IOException {
        ServerConnection sc = mock(ServerConnection.class);
        when(sc.connectTo(anyString())).thenReturn(true);
        when(sc.requestFileContents(anyString())).thenReturn(true);
        when(sc.moreBytes()).thenReturn(false);
        doThrow(new IOException()).when(sc).closeConnection();

        Client c = new Client(sc);
        String result = c.requestFile("server", "file");

        assertNull(result);
    }
}
