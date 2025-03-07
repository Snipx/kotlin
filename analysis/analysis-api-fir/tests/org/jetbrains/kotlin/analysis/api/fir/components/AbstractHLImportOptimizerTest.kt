/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.components

import org.jetbrains.kotlin.analysis.api.fir.test.framework.AbstractHLApiSingleModuleTest
import org.jetbrains.kotlin.analysis.api.analyse
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractHLImportOptimizerTest : AbstractHLApiSingleModuleTest() {
    override fun doTestByFileStructure(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices) {
        val mainKtFile = ktFiles.singleOrNull() ?: ktFiles.first { it.name == "main.kt" }
        val unusedImports = analyse(mainKtFile) { analyseImports(mainKtFile).unusedImports }

        val unusedImportPaths = unusedImports
            .map { it.importPath ?: error("Import $it should have an import path, instead was ${it.text}") }
            .sortedBy { it.toString() } // for stable results

        val actualUnusedImports = buildString {
            unusedImportPaths.forEach(::appendLine)
        }

        val expectedUnusedImports = testDataFileSibling(".imports")

        testServices.assertions.assertEqualsToFile(expectedUnusedImports, actualUnusedImports)
    }
}