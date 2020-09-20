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

import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Technology;
import org.netbeans.html.json.spi.Transfer;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Contexts.Provider.class)
public final class React4J implements Contexts.Provider {
    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        context.register(Technology.class, new ReactTech(), 5);
        context.register(Transfer.class, new ReactTrans(), 5);
    }

    public static abstract class Callback {
        protected abstract void callback(Object[] args);
    }

    @net.java.html.js.JavaScriptBody(args = {"c"}, javacall = true, body
    = "return function() {\n"
    + "   var args = Array.prototype.slice.call(arguments);\n"
    + "   @cz.xelfi.demo.react4jdemo.api.React4J::handleCallback(Ljava/lang/Object;Ljava/lang/Object;)(c, args);\n"
    + "};\n"
    )
    public static native Object wrapCallback(Callback c);

    static void handleCallback(Object c, Object arguments) {
        Callback callback = (Callback) c;
        Object[] args = (Object[]) arguments;
        callback.callback(args);
    }
}
