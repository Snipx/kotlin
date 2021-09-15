/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.api

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider

abstract class PackagePartProviderFactory {
    abstract fun createPackagePartProvider(scope: GlobalSearchScope): PackagePartProvider
}

internal fun Project.createPackagePartProvider(scope: GlobalSearchScope): PackagePartProvider =
    getService(PackagePartProviderFactory::class.java).createPackagePartProvider(scope)