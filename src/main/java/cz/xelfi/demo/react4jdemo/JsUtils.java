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
package cz.xelfi.demo.react4jdemo;

import com.dukescript.api.javafx.beans.FXBeanInfo;
import net.java.html.js.JavaScriptBody;

final class JsUtils {
    private JsUtils() {
    }
    
    @JavaScriptBody(args = { "msg" }, body = "alert(msg);")
    public static native void alert(String msg);
    

    @JavaScriptBody(args = {}, body = "debugger;")
    public static native void debugger();

    public static OnButton onButton(String className, Runnable action) {
        return new OnButton(className, action);
    }
    
    @FXBeanInfo.Generate
    static final class OnButton extends OnButtonBase {
        final String className;
        private final Runnable action;

        private OnButton(String className, Runnable action) {
            this.className = className;
            this.action = action;
        }
        
        void onClick() {
            action.run();
        }
    }
}