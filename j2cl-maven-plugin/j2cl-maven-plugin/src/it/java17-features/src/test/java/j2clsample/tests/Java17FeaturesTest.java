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

package j2clsample.tests;

import com.google.j2cl.junit.apt.J2clTestInput;

import j2clsample.java17.InterfaceFeatures;
import j2clsample.java17.PatternMatching;
import j2clsample.java17.RecordFeatures;
import j2clsample.java17.SealedClasses;
import j2clsample.java17.SwitchExpressions;
import j2clsample.java17.TextBlocks;
import j2clsample.java17.VarFeatures;

import org.junit.Assert;
import org.junit.Test;

@J2clTestInput(Java17FeaturesTest.class)
public class Java17FeaturesTest {

    // Java 9: private methods in interfaces
    @Test
    public void testInterfacePrivateMethod() {
        InterfaceFeatures impl = new InterfaceFeatures() {};
        Assert.assertEquals("Hello, j2cl!", impl.greet("j2cl"));
    }

    // Java 10: var local variable type inference
    @Test
    public void testVarLocalVariable() {
        Assert.assertEquals("a,b", VarFeatures.localVar());
    }

    // Java 11: var in lambda parameters
    @Test
    public void testVarInLambda() {
        Assert.assertEquals(7, VarFeatures.lambdaVar());
    }

    // Java 14: switch expressions with arrow syntax
    @Test
    public void testSwitchExpressionArrow() {
        Assert.assertEquals("weekend", SwitchExpressions.dayType(1));
        Assert.assertEquals("weekday", SwitchExpressions.dayType(3));
        Assert.assertEquals("unknown", SwitchExpressions.dayType(0));
    }

    // Java 14: switch expression with yield
    @Test
    public void testSwitchExpressionYield() {
        Assert.assertEquals(100, SwitchExpressions.score("A"));
        Assert.assertEquals(60, SwitchExpressions.score("C"));
        Assert.assertEquals(0, SwitchExpressions.score("F"));
    }

    // Java 15: text blocks
    @Test
    public void testTextBlock() {
        Assert.assertEquals("{\"name\":\"j2cl\",\"version\":17}", TextBlocks.json());
    }

// Java 16: records - basic
    @Test
    public void testRecordBasic() {
        var point = new RecordFeatures.Point(3, 4);
        Assert.assertEquals(3, point.x());
        Assert.assertEquals(4, point.y());
        Assert.assertEquals(25, point.distanceFromOriginSquared());
    }

    // Java 16: records - equals/hashCode
    @Test
    public void testRecordEquality() {
        var p1 = new RecordFeatures.Point(1, 2);
        var p2 = new RecordFeatures.Point(1, 2);
        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p1.hashCode(), p2.hashCode());
    }

    // Java 16: records - toString
    @Test
    public void testRecordToString() {
        var point = new RecordFeatures.Point(3, 4);
        String s = point.toString();
        Assert.assertTrue(s.contains("3"));
        Assert.assertTrue(s.contains("4"));
    }

    // Java 16: record implementing interface
    @Test
    public void testRecordImplementsInterface() {
        RecordFeatures.Named named = new RecordFeatures.Person("Alice", 30);
        Assert.assertEquals("Alice", named.name());
    }

    // Java 16: record with compact constructor
    @Test
    public void testRecordCompactConstructor() {
        var pos = new RecordFeatures.PositiveValue(-5);
        Assert.assertEquals(0, pos.value());
        var pos2 = new RecordFeatures.PositiveValue(10);
        Assert.assertEquals(10, pos2.value());
    }

    // Java 16: pattern matching for instanceof
    @Test
    public void testPatternMatchingInstanceof() {
        Assert.assertEquals("string:5", PatternMatching.describe("hello"));
        Assert.assertEquals("int:42", PatternMatching.describe(42));
        Assert.assertEquals("other", PatternMatching.describe(3.14));
    }

    // Java 16: pattern matching with guard
    @Test
    public void testPatternMatchingGuard() {
        Assert.assertTrue(PatternMatching.isLongString("abcdef"));
        Assert.assertFalse(PatternMatching.isLongString("abc"));
        Assert.assertFalse(PatternMatching.isLongString(123));
    }

    // Java 17: sealed classes
    @Test
    public void testSealedClasses() {
        SealedClasses.Shape circle = new SealedClasses.Circle(5.0);
        SealedClasses.Shape rect = new SealedClasses.Rectangle(3.0, 4.0);
        Assert.assertEquals(Math.PI * 25.0, circle.area(), 0.001);
        Assert.assertEquals(12.0, rect.area(), 0.001);
    }

    // Java 17: sealed classes + pattern matching
    @Test
    public void testSealedWithPatternMatching() {
        String circleDesc = SealedClasses.describeShape(new SealedClasses.Circle(5.0));
        Assert.assertTrue(circleDesc.startsWith("circle:r="));
        Assert.assertTrue(circleDesc.contains("5"));

        String rectDesc = SealedClasses.describeShape(new SealedClasses.Rectangle(3.0, 4.0));
        Assert.assertTrue(rectDesc.startsWith("rect:"));
        Assert.assertTrue(rectDesc.contains("3"));
        Assert.assertTrue(rectDesc.contains("4"));
    }
}
