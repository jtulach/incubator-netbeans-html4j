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
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.glassfish.grizzly.PortRange;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

final class GrizzlyServer extends HttpServer<Request, Response, Object> {
    private final org.glassfish.grizzly.http.server.HttpServer server;

    public GrizzlyServer(int from, int to) {
        server = org.glassfish.grizzly.http.server.HttpServer.createSimpleServer(null, new PortRange(from, to));
    }

    @Override
    void shutdownNow() {
        server.shutdownNow();
    }

    @Override
    void addHttpHandler(Handler r, String mapping) {
        server.getServerConfiguration().addHttpHandler(new HttpHandler() {
            @Override
            public void service(Request request, Response response) throws Exception {
                r.service(GrizzlyServer.this, request, response);
            }
        }, mapping);
    }

    @Override
    int getPort() {
        return server.getListeners().iterator().next().getPort();
    }

    @Override
    void start() throws IOException {
        server.start();
    }

    @Override
    String getRequestURI(Request r) {
        return r.getRequestURI();
    }

    @Override
    String getServerName(Request r) {
        return r.getServerName();
    }

    @Override
    int getServerPort(Request r) {
        return r.getServerPort();
    }

    @Override
    String getParameter(Request r, String id) {
        return r.getParameter(id);
    }

    @Override
    String getMethod(Request r) {
        return r.getMethod().getMethodString();
    }

    @Override
    Reader getReader(Request r) {
        return r.getReader();
    }

    @Override
    String getHeader(Request r, String header) {
        return r.getHeader(header);
    }

    @Override
    Writer getWriter(Response r) {
        return r.getWriter();
    }

    @Override
    void setContentType(Response r, String type) {
        r.setContentType(type);
    }

    @Override
    void setStatus(Response r, int code) {
        r.setStatus(code);
    }

    @Override
    OutputStream getOutputStream(Response r) {
        return r.getOutputStream();
    }

    @Override
    void suspend(Response r) {
        r.suspend();
    }

    @Override
    void resume(Response r) {
        r.resume();
    }

    @Override
    void setCharacterEncoding(Response r, String set) {
        r.setCharacterEncoding(set);
    }

    @Override
    void addHeader(Response r, String name, String value) {
        r.addHeader(name, value);
    }

    @Override
    <WebSocket> void send(WebSocket socket, String s) {
    }

}
