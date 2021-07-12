/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.builder;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("compiler/fir/raw-fir/psi2fir/testData/expressionTree")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class ExpressionTreeTestCaseGenerated extends AbstractExpressionTreeTestCase {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doRawFirTest, this, testDataFilePath);
    }

    public void testAllFilesPresentInExpressionTree() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/fir/raw-fir/psi2fir/testData/expressionTree"), Pattern.compile("^(.+)\\.kt$"), null, true);
    }

    @TestMetadata("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions")
    @TestDataPath("$PROJECT_ROOT")
    @RunWith(JUnit3RunnerWithInners.class)
    public static class Expressions extends AbstractExpressionTreeTestCase {
        private void runTest(String testDataFilePath) throws Exception {
            KotlinTestUtils.runTest(this::doRawFirTest, this, testDataFilePath);
        }

        public void testAllFilesPresentInExpressions() throws Exception {
            KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions"), Pattern.compile("^(.+)\\.kt$"), null, true);
        }

        @TestMetadata("annotated.kt")
        public void testAnnotated() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/annotated.kt");
        }

        @TestMetadata("arrayAccess.kt")
        public void testArrayAccess() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/arrayAccess.kt");
        }

        @TestMetadata("arrayAssignment.kt")
        public void testArrayAssignment() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/arrayAssignment.kt");
        }

        @TestMetadata("branches.kt")
        public void testBranches() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/branches.kt");
        }

        @TestMetadata("callableReferences.kt")
        public void testCallableReferences() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/callableReferences.kt");
        }

        @TestMetadata("calls.kt")
        public void testCalls() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/calls.kt");
        }

        @TestMetadata("classReference.kt")
        public void testClassReference() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/classReference.kt");
        }

        @TestMetadata("destructuring.kt")
        public void testDestructuring() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/destructuring.kt");
        }

        @TestMetadata("for.kt")
        public void testFor() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/for.kt");
        }

        @TestMetadata("genericCalls.kt")
        public void testGenericCalls() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/genericCalls.kt");
        }

        @TestMetadata("in.kt")
        public void testIn() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/in.kt");
        }

        @TestMetadata("inBrackets.kt")
        public void testInBrackets() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/inBrackets.kt");
        }

        @TestMetadata("init.kt")
        public void testInit() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/init.kt");
        }

        @TestMetadata("labelForInfix.kt")
        public void testLabelForInfix() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/labelForInfix.kt");
        }

        @TestMetadata("lambda.kt")
        public void testLambda() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/lambda.kt");
        }

        @TestMetadata("lambdaAndAnonymousFunction.kt")
        public void testLambdaAndAnonymousFunction() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/lambdaAndAnonymousFunction.kt");
        }

        @TestMetadata("locals.kt")
        public void testLocals() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/locals.kt");
        }

        @TestMetadata("modifications.kt")
        public void testModifications() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/modifications.kt");
        }

        @TestMetadata("namedArgument.kt")
        public void testNamedArgument() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/namedArgument.kt");
        }

        @TestMetadata("nullability.kt")
        public void testNullability() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/nullability.kt");
        }

        @TestMetadata("qualifierWithTypeArguments.kt")
        public void testQualifierWithTypeArguments() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/qualifierWithTypeArguments.kt");
        }

        @TestMetadata("simpleReturns.kt")
        public void testSimpleReturns() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/simpleReturns.kt");
        }

        @TestMetadata("super.kt")
        public void testSuper() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/super.kt");
        }

        @TestMetadata("these.kt")
        public void testThese() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/these.kt");
        }

        @TestMetadata("try.kt")
        public void testTry() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/try.kt");
        }

        @TestMetadata("typeOperators.kt")
        public void testTypeOperators() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/typeOperators.kt");
        }

        @TestMetadata("unary.kt")
        public void testUnary() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/unary.kt");
        }

        @TestMetadata("variables.kt")
        public void testVariables() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/variables.kt");
        }

        @TestMetadata("while.kt")
        public void testWhile() throws Exception {
            runTest("compiler/fir/raw-fir/psi2fir/testData/expressionTree/expressions/while.kt");
        }
    }
}
