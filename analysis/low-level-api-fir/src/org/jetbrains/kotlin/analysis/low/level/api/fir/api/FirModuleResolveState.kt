/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.api

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirPsiDiagnostic
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.analysis.low.level.api.fir.annotations.InternalForInline
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.builder.ModuleFileCache
import org.jetbrains.kotlin.analysis.low.level.api.fir.lazy.resolve.ResolveType
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtLambdaExpression

abstract class FirModuleResolveState {
    abstract val project: Project

    abstract val rootModuleSession: FirSession

    abstract val moduleInfo: ModuleInfo

    internal abstract fun getSessionFor(moduleInfo: ModuleInfo): FirSession

    /**
     * Build fully resolved FIR node for requested element.
     * This operation could be performance affective because it create FIleStructureElement and resolve non-local declaration into BODY phase, use
     * @see tryGetCachedFirFile to get [FirFile] in undefined phase
     */
    internal abstract fun getOrBuildFirFor(element: KtElement): FirElement?

    /**
     * Get or build or get cached [FirFile] for requested file in undefined phase
     */
    internal abstract fun getOrBuildFirFile(ktFile: KtFile): FirFile

    /**
     * Try get [FirFile] from the cache in undefined phase
     */
    internal abstract fun tryGetCachedFirFile(declaration: FirDeclaration, cache: ModuleFileCache): FirFile?

    internal abstract fun getDiagnostics(element: KtElement, filter: DiagnosticCheckerFilter): List<FirPsiDiagnostic>

    internal abstract fun collectDiagnosticsForFile(ktFile: KtFile, filter: DiagnosticCheckerFilter): Collection<FirPsiDiagnostic>

    @InternalForInline
    abstract fun findSourceFirDeclaration(
        ktDeclaration: KtDeclaration,
    ): FirDeclaration

    @InternalForInline
    abstract fun findSourceFirDeclaration(
        ktDeclaration: KtLambdaExpression,
    ): FirDeclaration

    /**
     * Looks for compiled non-local [ktDeclaration] declaration by querying its classId/callableId from the SymbolProvider.
     *
     * Works only if [ktDeclaration] is compiled (i.e. comes from .class file).
     */
    @InternalForInline
    abstract fun findSourceFirCompiledDeclaration(
        ktDeclaration: KtDeclaration
    ): FirDeclaration


    internal abstract fun <D : FirDeclaration> resolveFirToPhase(declaration: D, toPhase: FirResolvePhase): D

    internal abstract fun <D : FirDeclaration> resolveFirToResolveType(declaration: D, type: ResolveType): D
}
