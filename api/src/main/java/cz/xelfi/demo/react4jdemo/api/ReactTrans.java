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
package cz.xelfi.demo.react4jdemo.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import net.java.html.js.JavaScriptBody;
import net.java.html.json.Models;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.JSONCall;
import org.netbeans.html.json.spi.Transfer;

/** This is an implementation package
 */
@Contexts.Id("react4j")
final class ReactTrans implements Transfer {
    ReactTrans() {
    }

    @Override
    public void extract(Object obj, String[] props, Object[] values) {
        if (obj instanceof JSObjToStr) {
            obj = ((JSObjToStr)obj).obj;
        }
        extractJSON(obj, props, values);
    }

    @Override
    public void loadJSON(final JSONCall call) {
        if (call.isJSONP()) {
            String me = createJSONP(call);
            loadJSONP(call.composeURL(me), me);
        } else {
            String data = null;
            if (call.isDoOutput()) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    call.writeData(bos);
                    data = new String(bos.toByteArray(), "UTF-8");
                } catch (IOException ex) {
                    call.notifyError(ex);
                }
            }
            List<String> headerPairs = Models.asList();
            String h = call.getHeaders();
            if (h != null) {
                int pos = 0;
                while (pos < h.length()) {
                    int tagEnd = h.indexOf(':', pos);
                    if (tagEnd == -1) {
                        break;
                    }
                    int r = h.indexOf('\r', tagEnd);
                    int n = h.indexOf('\n', tagEnd);
                    if (r == -1) {
                        r = h.length();
                    }
                    if (n == -1) {
                        n = h.length();
                    }
                    headerPairs.add(h.substring(pos, tagEnd).trim());
                    headerPairs.add(h.substring(tagEnd + 1, Math.min(r, n)).trim());
                    pos = Math.max(r, n);
                }
            }
            loadJSON(call.composeURL(null), call, call.getMethod(), data, headerPairs.toArray());
        }
    }

    @Override
    public Object toJSON(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader r = new InputStreamReader(is, "UTF-8");
        for (;;) {
            int ch = r.read();
            if (ch == -1) {
                break;
            }
            sb.append((char)ch);
        }
        return parse(sb.toString());
    }

    static void notifySuccess(Object done, Object str, Object data) {
        Object notifyObj;
        if (data instanceof Object[]) {
            Object[] arr = (Object[]) data;
            for (int i = 0; i < arr.length; i++) {
                arr[i] = new JSObjToStr(str, arr[i]);
            }
            notifyObj = arr;
        } else {
            notifyObj = new JSObjToStr(str, data);
        }
        ((JSONCall)done).notifySuccess(notifyObj);
    }

    static void notifyError(Object done, Object msg) {
        ((JSONCall)done).notifyError(new Exception(msg.toString()));
    }

    private static final class JSObjToStr {
        final String str;
        final Object obj;

        public JSObjToStr(Object str, Object obj) {
            this.str = str == null ? "" : str.toString();
            this.obj = obj;
        }

        @Override
        public String toString() {
            return str;
        }
    }


    static String createJSONP(JSONCall whenDone) {
        int h = whenDone.hashCode();
        String name;
        for (;;) {
            name = "jsonp" + Integer.toHexString(h);
            if (defineIfUnused(name, whenDone)) {
                return name;
            }
            h++;
        }
    }

    @JavaScriptBody(args = {"name", "done"}, javacall = true, body
        = "if (window[name]) return false;\n "
        + "window[name] = function(data) {\n "
        + "  delete window[name];\n"
        + "  var el = window.document.getElementById(name);\n"
        + "  el.parentNode.removeChild(el);\n"
        + "  done.@org.netbeans.html.json.spi.JSONCall::notifySuccess(Ljava/lang/Object;)(data);\n"
        + "};\n"
        + "return true;\n"
    )
    private static native boolean defineIfUnused(String name, JSONCall done);

    @JavaScriptBody(args = {"s"}, body = "return eval('(' + s + ')');")
    static Object parse(String s) {
        return s;
    }

    @JavaScriptBody(args = {"url", "done", "method", "data", "hp"}, javacall = true, body = ""
        + "var request = new XMLHttpRequest();\n"
        + "if (!method) method = 'GET';\n"
        + "request.open(method, url, true);\n"
        + "request.setRequestHeader('Content-Type', 'application/json; charset=utf-8');\n"
        + "for (var i = 0; i < hp.length; i += 2) {\n"
        + "  var h = hp[i];\n"
        + "  var v = hp[i + 1];\n"
        + "  request.setRequestHeader(h, v);\n"
        + "}\n"
        + "request.onreadystatechange = function() {\n"
        + "  if (request.readyState !== 4) return;\n"
        + "  var r = request.response || request.responseText;\n"
        + "  try {\n"
        + "    var str = r;\n"
        + "    if (request.status !== 0)\n"
        + "      if (request.status < 100 || request.status >= 400) throw request.status + ': ' + request.statusText;"
        + "    try { r = eval('(' + r + ')'); } catch (ignore) { }"
        + "    @cz.xelfi.demo.react4jdemo.api.ReactTrans::notifySuccess(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)(done, str, r);\n"
        + "  } catch (error) {;\n"
        + "    @cz.xelfi.demo.react4jdemo.api.ReactTrans::notifyError(Ljava/lang/Object;Ljava/lang/Object;)(done, error);\n"
        + "  }\n"
        + "};\n"
        + "request.onerror = function (e) {\n"
        + "  @cz.xelfi.demo.react4jdemo.api.ReactTrans::notifyError(Ljava/lang/Object;Ljava/lang/Object;)(done, e.type + ' status ' + request.status);\n"
        + "};\n"
        + "if (data) request.send(data);\n"
        + "else request.send();\n"
    )
    static native void loadJSON(
        String url, JSONCall done, String method, String data, Object[] hp
    );

    @JavaScriptBody(args = {"url", "jsonp"}, body
        = "var scrpt = window.document.createElement('script');\n "
        + "scrpt.setAttribute('src', url);\n "
        + "scrpt.setAttribute('id', jsonp);\n "
        + "scrpt.setAttribute('type', 'text/javascript');\n "
        + "var body = document.getElementsByTagName('body')[0];\n "
        + "body.appendChild(scrpt);\n"
    )
    static void loadJSONP(String url, String jsonp) {

    }

    static void extractJSON(Object jsonObject, String[] props, Object[] values) {
        for (int i = 0; i < props.length; i++) {
            values[i] = getProperty(jsonObject, props[i]);
        }
    }

    @JavaScriptBody(args = {"object", "property"}, body
            = "var ret;\n"
            + "if (property === null) ret = object;\n"
            + "else if (object === null) ret = null;\n"
            + "else ret = object[property];\n"
            + "if (typeof ret !== 'undefined' && ret !== null) {\n"
            + "  if (typeof ko !== 'undefined' && ko['utils'] && ko['utils']['unwrapObservable']) {\n"
            + "    return ko['utils']['unwrapObservable'](ret);\n"
            + "  }\n"
            + "  return ret;\n"
            + "}\n"
            + "return null;\n"
    )
    static Object getProperty(Object object, String property) {
        return null;
    }

}
