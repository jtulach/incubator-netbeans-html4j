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
import cz.xelfi.demo.react4jdemo.api.React;
import cz.xelfi.demo.react4jdemo.api.React.Props;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public final class LikeButton extends React.Component<LikeButton.LikeState>  {
    public LikeButton(Props props) {
        super(props);
        setState(new LikeState(false));
    }
    
    static final class LikeState implements FXBeanInfo.Provider {
        final BooleanProperty liked = new SimpleBooleanProperty(this, "liked", false);
        private final FXBeanInfo info = FXBeanInfo.newBuilder(this)
                .property(liked)
                .build();

        public LikeState(boolean liked) {
            this.liked.set(liked);
        }
        
        @Override
        public FXBeanInfo getFXBeanInfo() {
            return info;
        }
    }


    void doLike() {
        setState(new LikeState(true));
    }
    
    @Override
    protected Object render() {
        if (this.state().liked.get()) {
            return "You like React for Java!";
        }

        class ButtonState implements FXBeanInfo.Provider {
            private final FXBeanInfo info = FXBeanInfo.newBuilder(this)
                    .action("onClick", LikeButton.this::doLike)
                    .build();

            @Override
            public FXBeanInfo getFXBeanInfo() {
                return info;
            }
        }
        
        return React.createElement(
          "button",
          new ButtonState(),
          "Like"
        );
    }

    public static void onPageLoad() {
        React.register("LikeButton", LikeButton::new);
        React.render("LikeButton", "like_button_container");
    }
}
