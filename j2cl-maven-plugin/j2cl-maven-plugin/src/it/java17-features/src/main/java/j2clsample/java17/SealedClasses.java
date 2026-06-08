/*
 * Copyright © 2026 j2cl-maven-plugin authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package j2clsample.java17;

public class SealedClasses {

    // Java 17: sealed class hierarchy
    public sealed interface Shape permits Circle, Rectangle {
        double area();
    }

    public record Circle(double radius) implements Shape {
        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    public record Rectangle(double width, double height) implements Shape {
        @Override
        public double area() {
            return width * height;
        }
    }

    public static String describeShape(Shape shape) {
        if (shape instanceof Circle c) {
            return "circle:r=" + c.radius();
        } else if (shape instanceof Rectangle r) {
            return "rect:" + r.width() + "x" + r.height();
        }
        return "unknown";
    }
}
