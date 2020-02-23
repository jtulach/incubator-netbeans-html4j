/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.html.presenters.browser;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import static org.testng.Assert.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleServerTest {
    public SimpleServerTest() {
    }

    @DataProvider(name = "serverFactories")
    public static Object[][] serverFactories() {
        Supplier<HttpServer<?,?,?>> simple = SimpleServer::new;
        Supplier<HttpServer<?,?,?>> grizzly = GrizzlyServer::new;
        return new Object[][]{{simple}, {grizzly}};
    }

    @Test(dataProvider = "serverFactories")
    public void testConnectionToTheServer(Supplier<HttpServer<?,?,?>> serverProvider) throws IOException {
        int min = 42343;
        int max = 49343;
        HttpServer<?, ?, ?> server = serverProvider.get();
        server.init(min, max);
        server.addHttpHandler(new HttpServer.Handler() {
            @Override
            <Request, Response> void service(HttpServer<Request, Response, ?> server, Request rqst, Response rspns) throws IOException {
                assertEquals(server.getServerName(rqst), "localhost", "Connecting from localhost");
                assertEquals(server.getServerPort(rqst), server.getPort(), "Connecting via local port");

                server.setCharacterEncoding(rspns, "UTF-8");
                server.setContentType(rspns, "text/x-test");
                Browser.cors(server, rspns);
                try (Writer w = server.getWriter(rspns)) {
                    final String n = server.getParameter(rqst, "name");
                    final String reply;
                    switch (server.getRequestURI(rqst)) {
                        case "/reply/hi": reply = "Ahoj " + n + "!"; break;
                        case "/reply/tchus": reply = "Ciao " + n + "!"; break;
                        default: reply = "What?";
                    }
                    w.write(reply);
                }
            }
        }, "/reply");
        server.start();

        int realPort = server.getPort();
        assertTrue(realPort <= max && realPort >= min, "Port from range (" + min + ", " + max + ") selected: " + realPort);

        final String baseUri = "http://localhost:" + realPort;
        assertURL("Ahoj John!", baseUri, "/reply/hi?name=John");
        assertURL("Ciao John!", baseUri, "/reply/tchus?name=John");

        server.shutdownNow();

    }

    private static void assertURL(String msg, String baseUri, final String path) throws IOException, MalformedURLException {
        URL url = new URL(baseUri + path);
        URLConnection conn = url.openConnection();

        final String contentAndAttribs = conn.getContentType();
        assertNotNull(contentAndAttribs, "Content-Type specified");
        int semicolon = contentAndAttribs.indexOf(';');
        final String content = semicolon == -1 ? contentAndAttribs : contentAndAttribs.substring(0, semicolon);
        assertEquals(content, "text/x-test");

        byte[] arr = new byte[8192];
        int len = conn.getInputStream().read(arr);
        assertNotEquals(len, -1, "Something shall be read");

        String txt = new String(arr, 0, len, StandardCharsets.UTF_8);
        assertEquals(txt, msg, "Message from the handler delivered");

        assertEquals(conn.getHeaderField("Access-Control-Allow-Origin"), "*");
    }
}
