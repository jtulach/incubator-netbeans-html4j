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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

final class ComponentHandle implements AutoCloseable {
    private static final ReferenceQueue<React.Component<?>> QUEUE = new ReferenceQueue<>();
    private final Ref ref;
    private final React.Component<?> component;
    private static Ref lastRef;

    private static final class Ref extends WeakReference<React.Component<?>> {

        private Ref next;
        private boolean explicitlyClosed;
        private final String id;

        Ref(React.Component<?> component, String id) {
            super(component, QUEUE);
            this.id = id;
            this.next = lastRef;
            lastRef = this;
        }
    }

    private ComponentHandle(React.Component<?> component, String id) {
        this.component = component;
        this.ref = new Ref(component, id);
    }

    @Override
    public void close() throws Exception {
        this.ref.explicitlyClosed = true;
    }


    synchronized static AutoCloseable register(React.Component<?> component, String id) {
        return new ComponentHandle(component, id);
    }

    private synchronized static Ref removeRef(Reference<? extends React.Component<?>> handle) {
        if (handle == lastRef) {
            Ref h = lastRef;
            lastRef = h.next;
            return h;
        }
        for (Ref it = lastRef;; it = it.next) {
            if (it.next == handle) {
                Ref h = it.next;
                it.next = h.next;
                return h;
            }
        }
    }

    static void clean() {
        for (;;) {
            Reference<? extends React.Component<?>> handle = QUEUE.poll();
            if (handle == null) {
                return;
            }
            Ref componentRef = removeRef(handle);
            if (!componentRef.explicitlyClosed) {
                System.err.println("Component at ID " + componentRef.id + " has been garbage collected prior to calling close()!");
            }
            handle.clear();
        }
    }
}
