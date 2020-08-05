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

import cz.xelfi.demo.react4jdemo.api.React;
import java.net.MalformedURLException;
import java.net.URL;
import net.java.html.js.JavaScriptBody;
import net.java.html.json.Model;
import net.java.html.json.OnReceive;
import net.java.html.json.Property;

@Model(className = "LoadingUrl", builder = "with", properties = {
    @Property(name = "name", type = String.class),
    @Property(name = "url", type = String.class),
    @Property(name = "code", type = String.class),
})
final class CodeComponent extends React.Component<LoadingUrl> {
    private static final Object CLAZZ = React.register("CodeComponent", CodeComponent::new);
    private static final String REMOTE = "https://raw.githubusercontent.com/jtulach/netbeans-html4j/react4jdemo/src/main/java/cz/xelfi/demo/react4jdemo/";
    private static final String VIEW = "https://github.com/jtulach/netbeans-html4j/blob/react4jdemo/src/main/java/cz/xelfi/demo/react4jdemo/";

    CodeComponent(React.Props props) {
        super(props);
        LoadingUrl loadUrl = props.as(LoadingUrl.class);
        loadUrl.loadCode(loadUrl.getUrl(), this);
        setState(loadUrl);
    }

    @OnReceive(url = "{url}", onError = "loadCodeFailed")
    static void loadCode(LoadingUrl model, String code, CodeComponent component) {
        if (code.startsWith("/*")) {
            int commentEnd = code.indexOf("*/");
            if (commentEnd != -1) {
                code = code.substring(commentEnd + 2);
            }
        }
        component.setState(model.clone().withCode(code));
    }

    static void loadCodeFailed(LoadingUrl model, Throwable ex, CodeComponent component) {
        if (model.getUrl().startsWith(REMOTE)) {
            model.setUrl(ex.getMessage());
        } else {
            model.setUrl(REMOTE + model.getName());
            model.loadCode(model.getUrl(), component);
        }
        component.setState(model);
    }

    @Override
    protected Object render() {
        Object content;
        if (state().getCode() == null) {
            content = React.createElement("div", null, "Loading " + state().getUrl());
        } else {
            content = React.createElement("pre", null, state().getCode());
        }
        return React.createElement("div", null,
            React.createElement("table", React.props("width", "100%"),
                React.createElement("tr", null,
                    React.createElement("td", React.props("align", "center"),
                        React.createElement("a", React.props("target", "_blank", "href", VIEW + state().getName()),
                            state().getName()
                        )
                    )
                )
            ),
            content
        );
    }


    @JavaScriptBody(args = {  }, body = "return window.location.href;")
    private static native String homePageUrl();

    static void loadCode(String code) {
        try {
            URL url = new URL(new URL(homePageUrl()), code);
            Object codeComponent = React.createElement(CLAZZ, new LoadingUrl().
                withUrl(url.toExternalForm()).
                withName(code)
            );
            React.render(codeComponent, "codecontainer");
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
    }
}
