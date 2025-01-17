package io.hahnsoftware.emp;

import io.hahnsoftware.emp.api.ApiServer;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException {
        ApiServer.startServer();
    }
}
