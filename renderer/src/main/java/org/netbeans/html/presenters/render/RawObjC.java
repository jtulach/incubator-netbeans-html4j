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
package org.netbeans.html.presenters.render;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.word.PointerBase;
import org.graalvm.word.WordFactory;
import org.netbeans.html.presenters.render.Cocoa.ObjC;

@CLibrary("objc")
@CContext(RawObjC.ObjcDirectives.class)
public final class RawObjC implements ObjC {

    @CFunction
    private static native PointerBase objc_getClass(CCharPointer name);

    @Override
    public boolean class_addMethod(Pointer cls, Pointer name, Callback imp, String types) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String class_getName(Pointer cls) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String object_getClassName(Pointer cls) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Pointer class_copyMethodList(Class cls, IntByReference outCount) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @CFunction
    private static native PointerBase objc_allocateClassPair(PointerBase cls, CCharPointer name, int additionalBytes);

    @Override
    public Pointer objc_allocateClassPair(Pointer cls, String name, int additionalBytes) {
        CTypeConversion.CCharPointerHolder cName = CTypeConversion.toCString(name);
        try {
            PointerBase clsPtr = WordFactory.pointer(Pointer.nativeValue(cls));
            PointerBase ptr = objc_allocateClassPair(clsPtr, cName.get(), additionalBytes);
            return new Pointer(ptr.rawValue());
        } finally {
            cName.close();
        }
    }

    @Override
    public Pointer objc_getClass(String name) {
        CTypeConversion.CCharPointerHolder cName = CTypeConversion.toCString(name);
        try {
            PointerBase ptr = objc_getClass(cName.get());
            if (ptr.isNull()) {
                throw new IllegalStateException("Cannot find class " + name);
            }
            return new Pointer(ptr.rawValue());
        } finally {
            cName.close();
        }
    }

    @CFunction
    private static native long objc_msgSend(PointerBase theReceiver, PointerBase theSelector, PointerBase p0, int i1);

    @CFunction
    private static native long objc_msgSend(PointerBase theReceiver, PointerBase theSelector, PointerBase p0, PointerBase p1, int i2);

    @CFunction
    private static native long objc_msgSend(PointerBase theReceiver, PointerBase theSelector, long l0, PointerBase p1, int i2);

    @CFunction
    private static native long objc_msgSend(PointerBase theReceiver, PointerBase theSelector, int i0);

    @Override
    public long objc_msgSend(Pointer theReceiver, Pointer theSelector, Object... arguments) {
        PointerBase ptrReceiver = toPtr(theReceiver);
        PointerBase ptrSelector = toPtr(theSelector);
        Thread.dumpStack();

        System.err.println("receiver: " + theReceiver);
        System.err.println("selector: " + theSelector);
        for (Object a : arguments) {
            if (a == null) {
                System.err.println("  type is null");
                continue;
            }
            System.err.println("  type: " + a.getClass().getName());
        }
        System.err.println("arguments: " + Arrays.toString(arguments));

        long ret = msgDispatch(arguments, ptrReceiver, ptrSelector);

        System.err.println("  return: " + ret);
        return ret;
    }

    private long msgDispatch(Object[] arguments, PointerBase ptrReceiver, PointerBase ptrSelector) {
        if (arguments.length == 0) {
            return objc_msgSend(ptrReceiver, ptrSelector, 0);
        }

        if (arguments.length == 1) {
            if (arguments[0] instanceof Integer) {
                int i0 = (Integer) arguments[0];
                return objc_msgSend(ptrReceiver, ptrSelector, i0);
            }
            if (arguments[0] instanceof Pointer) {
                PointerBase ptr0 = toPtr((Pointer) arguments[0]);
                return objc_msgSend(ptrReceiver, ptrSelector, ptr0, 0);
            }
        }

        if (arguments.length == 2) {
            if (arguments[0] instanceof String && arguments[1] instanceof Integer) {
                CTypeConversion.CCharPointerHolder s0 = CTypeConversion.toCString((String) arguments[0]);
                try {
                    int i1 = (Integer) arguments[1];
                    return objc_msgSend(ptrReceiver, ptrSelector, s0.get(), i1);
                } finally {
                    s0.close();
                }
            }
            if (arguments[0] instanceof Pointer && arguments[1] instanceof Pointer) {
                PointerBase ptr0 = toPtr((Pointer) arguments[0]);
                PointerBase ptr1 = toPtr((Pointer) arguments[1]);
                return objc_msgSend(ptrReceiver, ptrSelector, ptr0, ptr1, 0);
            }
            if (arguments[0] instanceof Long && arguments[1] instanceof Pointer) {
                long l0 = (Long) arguments[0];
                PointerBase ptr1 = toPtr((Pointer) arguments[1]);
                return objc_msgSend(ptrReceiver, ptrSelector, l0, ptr1, 0);
            }
        }

        System.err.println("#########################");
        return objc_msgSend(ptrReceiver, ptrSelector, -1);
    }

    private static PointerBase toPtr(Pointer theReceiver) {
        return WordFactory.pointer(Pointer.nativeValue(theReceiver));
    }

    @Override
    public Cocoa.Rct objc_msgSend_stret(Pointer theReceiver, Pointer theSelector, Object... arguments) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void objc_registerClassPair(Pointer cls) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @CFunction
    private static native PointerBase sel_getUid(CCharPointer name);

    @Override
    public Pointer sel_getUid(String name) {
        CTypeConversion.CCharPointerHolder cName = CTypeConversion.toCString(name);
        try {
            PointerBase ptr = sel_getUid(cName.get());
            return new Pointer(ptr.rawValue());
        } finally {
            cName.close();
        }
    }

    public static void main(String... args) throws Exception {
        String p = "Cocoa";
        if (args.length > 0) {
            p = args[0];
        }
        Show.show(p, new URI("http://netbeans.org"));
    }

    public static final class ObjcDirectives implements CContext.Directives {
        public ObjcDirectives() {
            Thread.dumpStack();
        }

        @Override
        public List<String> getHeaderFiles() {
            new Exception("getHeaderFiles").printStackTrace();
            return Arrays.asList("<objc/objc.h>");
        }

        @Override
        public List<String> getLibraries() {
            new Exception("objc").printStackTrace();
            return Arrays.asList("objc");
        }
    }
}
