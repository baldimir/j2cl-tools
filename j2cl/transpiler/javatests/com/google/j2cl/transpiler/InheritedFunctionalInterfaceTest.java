/*
 * Copyright 2024 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.transpiler;

import static com.google.j2cl.transpiler.TranspilerTester.newTesterWithDefaults;

import junit.framework.TestCase;

public class InheritedFunctionalInterfaceTest extends TestCase {

  public void testEmptyInterfaceExtendingFunctionalInterface() {
    newTesterWithDefaults()
        .addCompilationUnit(
            "test.Provider",
            """
            package test;
            public interface Provider<T> {
              T get();
            }
            """)
        .addCompilationUnit(
            "test.Factory",
            """
            package test;
            public interface Factory<T> extends Provider<T> {
            }
            """)
        .addCompilationUnit(
            "test.Main",
            """
            package test;
            public class Main {
              public static Factory<String> create() {
                return () -> "hello";
              }
            }
            """)
        .assertTranspileSucceeds();
  }

  public void testEmptyInterfaceExtendingParameterizedFunctionalInterface() {
    newTesterWithDefaults()
        .addCompilationUnit(
            "test.Converter",
            """
            package test;
            public interface Converter<F, T> {
              T convert(F from);
            }
            """)
        .addCompilationUnit(
            "test.StringConverter",
            """
            package test;
            public interface StringConverter<T> extends Converter<String, T> {
            }
            """)
        .addCompilationUnit(
            "test.Main",
            """
            package test;
            public class Main {
              public static StringConverter<Integer> create() {
                return s -> s.length();
              }
            }
            """)
        .assertTranspileSucceeds();
  }

  public void testClassImplementingInheritedFunctionalInterface() {
    newTesterWithDefaults()
        .addCompilationUnit(
            "test.Provider",
            """
            package test;
            public interface Provider<T> {
              T get();
            }
            """)
        .addCompilationUnit(
            "test.Factory",
            """
            package test;
            public interface Factory<T> extends Provider<T> {
            }
            """)
        .addCompilationUnit(
            "test.StringFactory",
            """
            package test;
            public class StringFactory implements Factory<String> {
              @Override
              public String get() {
                return "hello";
              }
            }
            """)
        .addCompilationUnit(
            "test.Main",
            """
            package test;
            public class Main {
              public static Provider<String> provider = new StringFactory();
              public static Factory<String> factory = new StringFactory();
            }
            """)
        .assertTranspileSucceeds();
  }
}
