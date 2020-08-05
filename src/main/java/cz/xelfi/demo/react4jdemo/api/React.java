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

import java.util.HashMap;
import java.util.Map;
import net.java.html.BrwsrCtx;
import net.java.html.js.JavaScriptBody;
import net.java.html.json.Models;

public class React {
    private static final Map<String,Object> FACTORIES = new HashMap<>();

    public static Object createElement(Object type, Object attrs, Object... children) {
        Object rawModel = null;
        if (attrs == null) {
            rawModel = toJS(new Object[0]);
        } else if (Models.isModel(attrs.getClass())) {
            rawModel = Models.toRaw(attrs);
        } else if (attrs instanceof Props) {
            rawModel = ((Props) attrs).js;
        }
        return createElement0(type, rawModel, children);
    }

    @JavaScriptBody(args = { "type", "model", "children" }, body =
        "return React.createElement(type, model, children);\n" +
        "\n"
    )
    private static native Object createElement0(Object type, Object model, Object... children);

    public static Object register(String name, ComponentFactory cf) {
        Object jsClass = register0(name, cf);
        FACTORIES.put(name, jsClass);
        return jsClass;
    }

    @JavaScriptBody(args = { "name", "factory" }, javacall = true, body = "\n" +
        "    let JavaReactWrapper = class __ extends React.Component {\n" +
        "      constructor(props) {\n" +
        "        super(props);\n" +
        "        let both = @cz.xelfi.demo.react4jdemo.api.React::factory(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)(this, props, factory);\n" +
        "        this.java = both[0];\n" +
        "        this.state = both[1];\n" +
        "      }\n" +
        "\n" +
        "      render() {\n" +
        "        return @cz.xelfi.demo.react4jdemo.api.React::render(Ljava/lang/Object;)(this.java);\n" +
        "      }\n" +
        "    };\n" +
        "\n" +
        "    let glob = (0 || eval)('this');\n" +
        "    glob[name] = JavaReactWrapper;\n" +
        "    return JavaReactWrapper;\n" +
        "")
    private static native Object register0(String name, ComponentFactory factory);

    public static void render(String name, String id) {
        Object jsClass = FACTORIES.get(name);
        if (jsClass == null) {
            jsClass = name;
        }
        render0(null, jsClass, id);
    }

    public static void render(Object reactElement, String id) {
        render0(reactElement, null, id);
    }

    @JavaScriptBody(args = { "reactElem", "clazz", "id" }, body = "" +
        "let elem = document.getElementById(id);\n" +
        "if (!elem) throw 'Cannot find element ' + id;\n" +
        "if (!reactElem) {\n" +
        "  reactElem = React.createElement(clazz);\n" +
        "}\n" +
        "ReactDOM.render(reactElem, elem);\n" +
        "\n"
    )
    private static native void render0(Object reactElem, Object clazz, String id);

    @JavaScriptBody(args = { "comp" }, body = "" +
        "comp.forceUpdate();\n" +
        "\n"
    )
    private static native void forceUpdate(Object comp);

    static Object[] factory(Object jsThis, Object props, Object rawFactory) {
        ComponentFactory factory = (ComponentFactory) rawFactory;
        Component<?> component = factory.create(new Props(jsThis, props));
        return new Object[] { component, Models.toRaw(component.state()) };
    }

    static Object render(Object rawComponent) {
        Component<?> component = (Component<?>) rawComponent;
        return component.render();
    }

    public interface ComponentFactory {
        Component<?> create(Props props);
    }

    public static Props props(String... keyAndValue) {
        return new Props(null, toJS(keyAndValue));
    }

    @JavaScriptBody(args = { "keysAndValues" }, body = ""
        + "var obj = {};\n"
        + "for (let i = 0; i < keysAndValues.length; i += 2) {\n"
        + "  obj[keysAndValues[i]] = keysAndValues[i + 1];\n"
        + "}\n"
        + "return obj;\n"
    )
    private static native Object toJS(Object[] keysAndValues);

    public static final class Props {
        final Object thiz;
        final Object js;

        Props(Object thiz, Object js) {
            this.thiz = thiz;
            this.js = js;
        }

        public <T> T as(Class<T> type) {
            if (Models.isModel(type)) {
                BrwsrCtx ctx = BrwsrCtx.findDefault(Props.class);
                return Models.fromRaw(ctx, type, js);
            }
            throw new ClassCastException();
        }

        public Object get(String name) {
            return readProperty(js, name);
        }

        @JavaScriptBody(args = { "obj", "prop" }, body = ""
                + "let val = obj[prop];\n"
                + "if (typeof val === 'function') {\n"
                + "  val = val();\n"
                + "}\n"
                + "return val;\n"
        )
        private static native Object readProperty(Object obj, String prop);
    }

    public static abstract class Component<State> {
        private final Props props;
        private State state;

        protected Component(Props props) {
            this.props = props;
        }

        protected abstract Object render();

        protected final State state() {
            return this.state;
        }

        protected final void setState(State model) {
            if (this.state == null) {
                this.state = model;
                return;
            }
            this.state = model;
            forceUpdate();
        }

        protected final void forceUpdate() {
            React.forceUpdate(props.thiz);
        }
    }
}
