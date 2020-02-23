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
                try (Writer w = server.getWriter(rspns)) {
                    w.write("Ahoj!");
                }
            }
        }, "/hi");
        server.start();

        int realPort = server.getPort();
        assertTrue(realPort <= max && realPort >= min, "Port from range (" + min + ", " + max + ") selected: " + realPort);

        URL url = new URL("http://localhost:" + realPort + "/hi");
        URLConnection conn = url.openConnection();
        byte[] arr = new byte[8192];
        int len = conn.getInputStream().read(arr);
        assertNotEquals(len, -1, "Something shall be read");

        String txt = new String(arr, 0, len, StandardCharsets.UTF_8);

        server.shutdownNow();

        assertEquals(txt, "Ahoj!", "Message from the handler delivered");
    }
}
