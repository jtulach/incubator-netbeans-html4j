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

import net.java.html.js.JavaScriptBody;
import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.FunctionBinding;
import org.netbeans.html.json.spi.PropertyBinding;
import org.netbeans.html.json.spi.Technology;

@Contexts.Id("react4j")
final class ReactTech implements Technology.BatchInit<Object> {
    @Override
    public Object wrapModel(Object model) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <M> M toModel(Class<M> modelClass, Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void bind(PropertyBinding b, Object model, Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void valueHasMutated(Object data, String propertyName) {
    }

    @Override
    public void expose(FunctionBinding fb, Object model, Object d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void applyBindings(Object data) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object wrapArray(Object[] arr) {
        return arr;
    }

    @Override
    public void runSafe(Runnable r) {
        r.run();
    }

    @Override
    public Object wrapModel(Object model, PropertyBinding[] propArr, FunctionBinding[] funcArr) {
        String[] propNames = new String[propArr.length];
        Object[] propValues = new Object[propArr.length];
        for (int i = 0; i < propValues.length; i++) {
            propNames[i] = propArr[i].getPropertyName();
            propValues[i] = propArr[i].getValue();
        }
        String[] funcNames = new String[funcArr.length];
        for (int i = 0; i < funcNames.length; i++) {
            funcNames[i] = funcArr[i].getFunctionName();
            funcArr[i] = funcArr[i].weak();
        }
        Object js = defineJs(propNames, propValues, funcNames, funcArr);
        return js;
    }

    @JavaScriptBody(args = { "propNames", "propValues", "funcNames", "funcArr" }, javacall = true, body =
        "\n" +
        "let obj = {};\n" +
        "for (let i = 0; i < propNames.length; i++) {\n" +
        "  obj[propNames[i]] = propValues[i];\n" +
        "}\n" +
        "for (let i = 0; i < funcNames.length; i++) {\n" +
        "  obj[funcNames[i]] = function(a, b) {\n" +
        "    let fn = funcArr[i];\n" +
        "    fn.@org.netbeans.html.json.spi.FunctionBinding::call(Ljava/lang/Object;Ljava/lang/Object;)(a, b);\n" +
        "  };\n" +
        "}\n" +
        "return obj;\n"
    )
    private static native Object defineJs(String[] propNames, Object[] propValues, String[] funcNames, FunctionBinding[] funcArr);
}
