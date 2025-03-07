/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.scopes

import org.jetbrains.kotlin.fir.scopes.impl.FirAbstractSimpleImportingScope
import org.jetbrains.kotlin.fir.scopes.impl.FirDefaultSimpleImportingScope
import org.jetbrains.kotlin.analysis.api.ValidityTokenOwner
import org.jetbrains.kotlin.analysis.api.tokens.ValidityToken
import org.jetbrains.kotlin.analysis.api.fir.KtSymbolByFirBuilder
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.fir.utils.weakRef
import org.jetbrains.kotlin.analysis.api.scopes.KtNonStarImportingScope
import org.jetbrains.kotlin.analysis.api.scopes.KtScopeNameFilter
import org.jetbrains.kotlin.analysis.api.scopes.NonStarImport
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtClassifierSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtConstructorSymbol
import org.jetbrains.kotlin.analysis.api.withValidityAssertion
import org.jetbrains.kotlin.name.Name

internal class KtFirNonStarImportingScope(
    firScope: FirAbstractSimpleImportingScope,
    private val builder: KtSymbolByFirBuilder,
    override val token: ValidityToken
) : KtNonStarImportingScope, ValidityTokenOwner {
    private val firScope: FirAbstractSimpleImportingScope by weakRef(firScope)

    @OptIn(ExperimentalStdlibApi::class)
    override val imports: List<NonStarImport> by cached {
        buildList {
            firScope.simpleImports.values.forEach { imports ->
                imports.forEach { import ->
                    NonStarImport(
                        import.packageFqName,
                        import.relativeClassName,
                        import.resolvedClassId,
                        import.importedName
                    ).let(::add)
                }
            }
        }
    }

    override fun getCallableSymbols(nameFilter: KtScopeNameFilter): Sequence<KtCallableSymbol> = withValidityAssertion {
        firScope.getCallableSymbols(getPossibleCallableNames().filter(nameFilter), builder)
    }

    override fun getClassifierSymbols(nameFilter: KtScopeNameFilter): Sequence<KtClassifierSymbol> = withValidityAssertion {
        firScope.getClassifierSymbols(getPossibleClassifierNames().filter(nameFilter), builder)
    }

    override fun getConstructors(): Sequence<KtConstructorSymbol> = emptySequence()

    override fun getPossibleCallableNames(): Set<Name> = withValidityAssertion {
        imports.mapNotNullTo(hashSetOf()) { it.callableName }
    }

    override fun getPossibleClassifierNames(): Set<Name> = withValidityAssertion {
        imports.mapNotNullTo((hashSetOf())) { it.relativeClassName?.shortName() }
    }

    override val isDefaultImportingScope: Boolean = withValidityAssertion { firScope is FirDefaultSimpleImportingScope }
}
