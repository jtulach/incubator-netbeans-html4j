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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class SimpleServer extends HttpServer<SimpleServer.Req, SimpleServer.Res, Object> implements Runnable {

    private Map<String, Handler> maps = new LinkedHashMap<>();
    private final int max;
    private final int min;
    private ServerSocketChannel server;
    private Selector connection;
    private Thread processor;

    private static final Pattern PATTERN_GET = Pattern.compile("(HEAD|GET) */([^ \\?]*)(\\?[^ ]*)?");
    private static final Pattern PATTERN_LANGS = Pattern.compile(".*^Accept-Language:(.*)$", Pattern.MULTILINE);
    static final Logger LOG = Logger.getLogger(SimpleServer.class.getName());

    SimpleServer(int min, int max) throws IOException {
        this.min = min;
        this.max = max;
    }

    @Override
    void addHttpHandler(Handler h, String path) {
        if (!path.startsWith("/")) {
            throw new IllegalStateException("Shall start with /: " + path);
        }
        maps.put(path.substring(1), h);
    }

    @Override
    void start() throws IOException {
        connection = Selector.open();

        LOG.log(Level.INFO, "Listening for HTTP connections on port {0}", getServer().socket().getLocalPort());
        processor = new Thread(this, "HTTP server");
        processor.start();
    }

    @Override
    String getRequestURI(Req r) {
        throw new UnsupportedOperationException();
    }

    @Override
    String getServerName(Req r) {
        throw new UnsupportedOperationException();
    }

    @Override
    int getServerPort(Req r) {
        throw new UnsupportedOperationException();
    }

    @Override
    String getParameter(Req r, String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    String getMethod(Req r) {
        throw new UnsupportedOperationException();
    }

    @Override
    Reader getReader(Req r) {
        throw new UnsupportedOperationException();
    }

    @Override
    String getHeader(Req r, String substring) {
        throw new UnsupportedOperationException();
    }

    @Override
    Writer getWriter(Res r) {
        return r.writer;
    }

    @Override
    void setContentType(Res r, String contentType) {
        r.contentType = contentType;
    }

    @Override
    void setStatus(Res r, int status) {
        r.status = status;
    }

    @Override
    OutputStream getOutputStream(Res r) {
        return r.os;
    }

    @Override
    void suspend(Res r) {
        throw new UnsupportedOperationException();
    }

    @Override
    void resume(Res r) {
        throw new UnsupportedOperationException();
    }

    @Override
    void setCharacterEncoding(Res r, String utF8) {
        throw new UnsupportedOperationException();
    }

    @Override
    void addHeader(Res r, String accessControlAllowOrigin, String string) {
        throw new UnsupportedOperationException();
    }

    @Override
    <WebSocket> void send(WebSocket socket, String s) {
    }

    static final class Req {
    }

    static final class Res {

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        String contentType;
        int status;
    }

    /**
     * @return the port to listen to
     */
    @Override
    public int getPort() {
        try {
            return getServer().socket().getLocalPort();
        } catch (IOException ex) {
            return -1;
        }
    }

    @Override
    public void run() {
        ByteBuffer bb = ByteBuffer.allocate(2048);
        int sleep = 10;

        while (Thread.currentThread() == processor) {
            ServerSocketChannel localServer;
            Selector localConnection;

            SocketChannel toClose = null;
            try {
                synchronized (this) {
                    localServer = this.getServer();
                    localConnection = this.connection;
                }

                LOG.log(Level.FINE, "Before select {0}", localConnection.isOpen());
                LOG.log(Level.FINE, "Server {0}", localServer.isOpen());

                int amount = localConnection.select();

                LOG.log(Level.FINE, "After select: {0}", amount);
                if (amount == 0) {
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException ex) {
                    }
                    sleep *= 2;
                    if (sleep > 1000) {
                        sleep = 1000;
                    }
                } else {
                    sleep = 10;
                }

                Set<SelectionKey> readyKeys = localConnection.selectedKeys();
                Iterator<SelectionKey> it = readyKeys.iterator();
                PROCESS:
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    LOG.log(Level.FINEST, "Handling key {0}", key.attachment());
                    it.remove();

                    if (key.isAcceptable()) {
                        try {
                            SocketChannel channel = localServer.accept();
                            channel.configureBlocking(false);
                            SelectionKey another = channel.register(
                                    localConnection, SelectionKey.OP_READ
                            );
                        } catch (ClosedByInterruptException ex) {
                            LOG.log(Level.WARNING, "Interrupted while accepting", ex);
                            server.close();
                            server = null;
                            LOG.log(Level.INFO, "Accept server reset");
                        }
                        continue PROCESS;
                    }

                    if (key.isReadable()) {
                        bb.clear();
                        SocketChannel channel = (SocketChannel) key.channel();
                        toClose = channel;
                        channel.read(bb);
                        String header = new String(bb.array(), 0, bb.position());
                        Matcher m = PATTERN_GET.matcher(header);
                        String url = m.find() ? m.group(2) : null;
                        String args = url != null && m.groupCount() == 3 ? m.group(3) : null;
                        boolean head = url != null && "HEAD".equals(m.group(1));

                        Map<String, String> context;
                        if (args != null) {
                            Map<String, String> c = new HashMap<String, String>();
                            parseArgs(c, args);
                            context = Collections.unmodifiableMap(c);
                        } else {
                            context = Collections.emptyMap();
                        }
                        Request req = findRequest(url, context, header, head);
                        key.attach(req);
                        key.interestOps(SelectionKey.OP_WRITE);
                        continue PROCESS;
                    }

                    if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        toClose = channel;
                        Request reply = (Request) key.attachment();
                        if (reply == null) {
                            continue PROCESS;
                        }
                        reply.handle(key, channel);
                    }
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Exception while handling request", t);
                if (toClose != null) {
                    try {
                        toClose.close();
                    } catch (IOException ioEx) {
                        LOG.log(Level.INFO, "While closing", ioEx);
                    }
                }
            }
        }

        try {
            LOG.fine("Closing connection");
            this.connection.close();
            LOG.fine("Closing server");
            this.getServer().close();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }

        synchronized (this) {
            notifyAll();
        }
        LOG.fine("All notified, exiting server");
    }

    private Request findRequest(String url, Map<String, ? extends Object> args, String header, boolean justHead) {
        if (url != null) {
            LOG.log(Level.INFO, "Searching for page {0}", url);
            Matcher m = PATTERN_LANGS.matcher(header);
            String langs = m.find() ? m.group(1) : null;
            if (langs != null) {
                LOG.log(Level.FINE, "Accepted languages {0}", langs);
            }

            String pref = url;
            int last = pref.length() - 1;
            for (Map.Entry<String, Handler> entry : maps.entrySet()) {
                if (url.startsWith(entry.getKey())) {
                    final Handler h = entry.getValue();
                    Req req = new Req();
                    Res res = new Res();
                    UnknownPageRequest upr = UnknownPageRequest.create(new HeaderProvider() {
                        @Override
                        public void replyHeader(Header header, Response response) throws IOException {
                            h.service(SimpleServer.this, req, res);
                        }
                    }, new ContentProvider() {
                        @Override
                        public void replyTo(Header header, SocketChannel ch, SelectionKey key) throws IOException {
                            if (key.attachment() == null) {
                                ByteBuffer out = ByteBuffer.wrap(res.os.toByteArray());
                                key.attach(out);
                            }
                            ByteBuffer bb = (ByteBuffer) key.attachment();
                            ch.write(bb);
                        }
                    });
                    return new DynamicRequest(upr, null, url.substring(last + 1), args, langs, justHead);
                }
            }

            while (pref != null) {
                LOG.log(Level.INFO, "Page not found trying {0}", url);
                Object obj = null;
                if (obj != null) {
                    return new DynamicRequest((UnknownPageRequest) obj, null, url.substring(last + 1), args, langs, justHead);
                }
                if (pref.length() > 0) {
                    last = pref.lastIndexOf('/');
                    if (last < 0) {
                        pref = "";
                    } else {
                        pref = pref.substring(0, last);
                    }
                } else {
                    pref = null;
                }
            }
        }

        String msg = "<h1>Strange HTTP Request</h1>\n"
                + "Url: <code>" + url + "</code><p>"
                + "Header: <pre>" + header + "</pre>";
        LOG.warning(msg);
        return new MsgRequest(msg);
    }

    private static void parseArgs(final Map<String, ? super String> context, final String args) {
        if (args != null) {
            for (String arg : args.substring(1).split("&")) {
                String[] valueAndKey = arg.split("=");

                String key = valueAndKey[1].replaceAll("\\+", " ");
                for (int idx = 0;;) {
                    idx = key.indexOf("%", idx);
                    if (idx == -1) {
                        break;
                    }
                    int ch = Integer.parseInt(key.substring(idx + 1, idx + 3), 16);
                    key = key.substring(0, idx) + (char) ch + key.substring(idx + 3);
                    idx++;
                }

                context.put(valueAndKey[0], key);
            }
        }
    }

    @Override
    public synchronized void shutdownNow() {
        Thread inter = processor;
        if (inter != null) {
            processor = null;
            LOG.fine("Processor cleaned");
            inter.interrupt();
            LOG.fine("Processor interrupted");
            try {
                wait(5000);
            } catch (InterruptedException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
            LOG.fine("After waiting");
        }
    }

    /**
     * Computes todays's date .
     */
    static byte[] date(Date date) {
        return date("Date: ", date != null ? date : new Date());
    }

    static byte[] date(String prefix, Date date) {
        try {
            DateFormat f = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("GMT")); // NOI18N
            return (prefix + f.format(date)).getBytes("utf-8");
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.WARNING, ex.getMessage(), ex);
            return new byte[0];
        }
    }

    /**
     * @return the server
     */
    public ServerSocketChannel getServer() throws IOException {
        if (server == null) {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);

            Random random = new Random();
            for (int i = min; i < max; i++) {
                int at = min + random.nextInt(max - min);
                InetSocketAddress address = new InetSocketAddress(at);
                try {
                    server.socket().bind(address);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "Cannot bind to " + at, ex);
                }
            }

            server.register(this.connection, SelectionKey.OP_ACCEPT);
        }
        return server;
    }

    private static interface Request {

        public void handle(SelectionKey key, SocketChannel channel) throws IOException;
    }

    private final class DynamicRequest extends SelectionKey
            implements Request {

        private final UnknownPageRequest upr;
        private final String url;
        private final Map<String, ? extends Object> context;
        private final String langs;
        private final boolean justHead;
        private ByteBuffer bb = ByteBuffer.allocate(8192);
        private SelectionKey delegate;
        private Header header;

        public DynamicRequest(
                UnknownPageRequest v,
                Object pages,
                String u,
                Map<String, ? extends Object> a,
                String langs,
                boolean justHead
        ) {
            this.upr = v;
            this.url = u;
            this.context = a;
            this.justHead = justHead;
            this.langs = langs;
        }

        public void handle(SelectionKey key, SocketChannel channel) throws IOException {
            ContentProvider h = upr.handler;
            delegate = key;

            if (bb != null) {
                header = new Header(url, context, langs);

                String mime = upr.mimeType;
                if (mime == null) {
                    HeaderImpl map = new HeaderImpl();
                    Response response = new Response(map);
                    upr.header.replyHeader(header, response);

                    if (map.redirect != null) {
                        Request req = findRequest(map.redirect, map.args, "", justHead);
                        key.attach(req);
                        return;
                    }

                    mime = map.mimeType;
                }
                if (mime == null) {
                    mime = "content/unknown"; // NOI18N
                }
                bb.clear();

                LOG.log(Level.INFO, "Found page request {0}", url); // NOI18N
                bb.clear();
                bb.put("HTTP/1.1 200 OK\r\n".getBytes());
                bb.put("Connection: close\r\n".getBytes());
                bb.put("Server: http://dvbcentral.sf.net\r\n".getBytes());
                bb.put(date(null));
                bb.put("\r\n".getBytes());
                bb.put(("Content-Type: " + mime + "\r\n").getBytes());
                bb.put("Pragma: no-cache\r\nCache-control: no-cache\r\n".getBytes());
                bb.put("\r\n".getBytes());
                bb.flip();
                channel.write(bb);
                LOG.log(Level.FINER, "Written header, type {0}", mime);
                bb = null;

                if (justHead) {
                    LOG.fine("Writer flushed and closed, closing channel");
                    channel.close();
                    return;
                }
            }

            LOG.log(Level.FINE, "delegating to handler: {0}", h.getClass().getName());
            try {
                h.replyTo(header, channel, this);
                LOG.log(Level.FINE, "replyTo delegated, is channel open: {0}", channel.isOpen());
            } finally {
                if (!channel.isOpen()) {
                    LOG.log(Level.FINE, "channel not open, closing");
                    key.attach(null);
                    key.cancel();
                }
            }
        }

        @Override
        public String toString() {
            return "DynamicRequest[" + url + ", " + upr.getClass().getName() + "]";
        }

        public SelectableChannel channel() {
            return delegate.channel();
        }

        public Selector selector() {
            return delegate.selector();
        }

        public boolean isValid() {
            return delegate.isValid();
        }

        public void cancel() {
            delegate.cancel();
        }

        public int interestOps() {
            return delegate.interestOps();
        }

        public SelectionKey interestOps(int arg0) {
            return delegate.interestOps(arg0);
        }

        public int readyOps() {
            return delegate.readyOps();
        }
    } // end of DynamicRequest

    private static final class MsgRequest implements Request {

        private final String msg;
        private ByteBuffer bb;
        private int index;

        public MsgRequest(String a) {
            this.msg = a;
        }

        public void handle(SelectionKey key, SocketChannel channel) throws IOException {
            if (bb == null) {
                bb = ByteBuffer.allocate(8192);
                bb.put("HTTP/1.1 200 OK\r\n".getBytes());
                bb.put("Connection: close\r\n".getBytes());
                bb.put("Server: http://dvbcentral.sf.net\r\n".getBytes());
                bb.put(date(null));
                bb.put("\r\n".getBytes());
                bb.put(("Content-Type: text/html\r\n").getBytes());
                bb.put("Pragma: no-cache\r\nCache-control: no-cache\r\n".getBytes());
                bb.put("\r\n".getBytes());
                bb.flip();
                channel.write(bb);
                index = 0;
            } else {
                if (index == 0) {
                    LOG.warning(msg);
                }
                LOG.log(Level.FINE, "writing at {0}", index);
                bb.clear();
                byte[] arr = msg.getBytes();
                bb.put(arr, index, arr.length - index);
                index += bb.position();
                LOG.log(Level.FINE, "something written new index at {0}", index);
                bb.flip();
                channel.write(bb);
                if (index == arr.length) {
                    LOG.fine("msg written, closing channel");
                    channel.close();
                }
            }

        }

        @Override
        public String toString() {
            return "MsgRequest[" + msg + "]";
        }
    } // end of MsgRequest

    static final class Response {

        private final HeaderImpl map;

        Response(HeaderImpl map) {
            this.map = map;
        }

        public void setMimeType(String mime) {
            map.setMimeType(mime);
        }

        public void redirect(String redirect, Map<String, ? extends Object> args) {
            map.redirect(redirect, args);
        }
    }

    static class HeaderImpl {

        String mimeType;
        String redirect;
        Map<String, ? extends Object> args;

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public void redirect(String page, Map<String, ? extends Object> args) {
            this.redirect = page;
            this.args = args;
        }
    }

    static final class UnknownPageRequest {

        final String mimeType;
        final ContentProvider handler;
        final HeaderProvider header;

        UnknownPageRequest(String mimeType, ContentProvider h, HeaderProvider head) {
            this.mimeType = mimeType;
            this.handler = h;
            this.header = head;
        }

        static UnknownPageRequest create(String mimeType, ContentProvider h) {
            return new UnknownPageRequest(mimeType, h, null);
        }

        static UnknownPageRequest create(HeaderProvider header, ContentProvider h) {
            return new UnknownPageRequest(null, h, header);
        }
    }

    interface ContentProvider {

        public void replyTo(
                Header header,
                SocketChannel ch,
                SelectionKey key
        ) throws IOException;
    }

    interface HeaderProvider {

        public void replyHeader(Header header, Response response) throws IOException;
    }

    static final class Header {

        private final Map<String, String> args;
        private final String path;
        private final List<Locale> langs;

        Header(String path, Map<String, ? extends Object> args, String acceptLanguages) {
            this.path = path;
            this.args = (Map<String, String>) args;

            List<Locale> arr = new ArrayList<Locale>();
            if (acceptLanguages != null) {
                for (String lang : acceptLanguages.split("[ ,]+")) {
                    if (lang.length() > 0) {
                        arr.add(new Locale(lang));
                    }
                }
            }
            langs = arr;
        }

        public Map<String, String> getArgs() {
            return args;
        }

        public String getPath() {
            return path;
        }

        public List<Locale> getLocales() {
            return langs;
        }
    }

}
