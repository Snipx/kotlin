/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.session

import org.jetbrains.annotations.TestOnly
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.type.TypeCheckers
import org.jetbrains.kotlin.fir.analysis.checkersComponent
import org.jetbrains.kotlin.fir.analysis.extensions.additionalCheckers
import org.jetbrains.kotlin.fir.analysis.jvm.diagnostics.FirJvmDefaultErrorMessages
import org.jetbrains.kotlin.fir.checkers.registerCommonCheckers
import org.jetbrains.kotlin.fir.checkers.registerJvmCheckers
import org.jetbrains.kotlin.fir.deserialization.ModuleDataProvider
import org.jetbrains.kotlin.fir.deserialization.SingleModuleDataProvider
import org.jetbrains.kotlin.fir.extensions.BunchOfRegisteredExtensions
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.extensions.registerExtensions
import org.jetbrains.kotlin.fir.java.FirCliSession
import org.jetbrains.kotlin.fir.java.FirProjectSessionProvider
import org.jetbrains.kotlin.fir.java.JavaSymbolProvider
import org.jetbrains.kotlin.fir.java.deserialization.KotlinDeserializedJvmSymbolsProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirDependenciesSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.*
import org.jetbrains.kotlin.fir.resolve.scopes.wrapScopeWithJvmMapped
import org.jetbrains.kotlin.fir.scopes.FirKotlinScopeProvider
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectEnvironment
import org.jetbrains.kotlin.fir.session.environment.AbstractProjectFileSearchScope
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices

@OptIn(PrivateSessionConstructor::class, SessionConfiguration::class)
object FirSessionFactory {
    class FirSessionConfigurator(private val session: FirSession) {
        private val registeredExtensions: MutableList<BunchOfRegisteredExtensions> = mutableListOf(BunchOfRegisteredExtensions.empty())

        fun registerExtensions(extensions: BunchOfRegisteredExtensions) {
            registeredExtensions += extensions
        }

        fun useCheckers(checkers: ExpressionCheckers) {
            session.checkersComponent.register(checkers)
        }

        fun useCheckers(checkers: DeclarationCheckers) {
            session.checkersComponent.register(checkers)
        }

        fun useCheckers(checkers: TypeCheckers) {
            session.checkersComponent.register(checkers)
        }

        @SessionConfiguration
        fun configure() {
            session.extensionService.registerExtensions(registeredExtensions.reduce(BunchOfRegisteredExtensions::plus))
            session.extensionService.additionalCheckers.forEach(session.checkersComponent::register)
        }
    }

    data class ProviderAndScopeForIncrementalCompilation(
        val packagePartProvider: PackagePartProvider,
        val scope: AbstractProjectFileSearchScope
    )

    inline fun createSessionWithDependencies(
        moduleName: Name,
        platform: TargetPlatform,
        analyzerServices: PlatformDependentAnalyzerServices,
        externalSessionProvider: FirProjectSessionProvider?,
        projectEnvironment: AbstractProjectEnvironment,
        languageVersionSettings: LanguageVersionSettings,
        sourceScope: AbstractProjectFileSearchScope,
        librariesScope: AbstractProjectFileSearchScope,
        lookupTracker: LookupTracker?,
        providerAndScopeForIncrementalCompilation: ProviderAndScopeForIncrementalCompilation?,
        dependenciesConfigurator: DependencyListForCliModule.Builder.() -> Unit = {},
        noinline sessionConfigurator: FirSessionConfigurator.() -> Unit = {},
    ): FirSession {
        val dependencyList = DependencyListForCliModule.build(moduleName, platform, analyzerServices, dependenciesConfigurator)
        val sessionProvider = externalSessionProvider ?: FirProjectSessionProvider()
        createLibrarySession(
            moduleName,
            sessionProvider,
            dependencyList.moduleDataProvider,
            librariesScope,
            projectEnvironment,
            projectEnvironment.getPackagePartProvider(librariesScope),
            languageVersionSettings
        )

        val mainModuleData = FirModuleDataImpl(
            moduleName,
            dependencyList.regularDependencies,
            dependencyList.dependsOnDependencies,
            dependencyList.friendsDependencies,
            dependencyList.platform,
            dependencyList.analyzerServices
        )
        return createJavaModuleBasedSession(
            mainModuleData,
            sessionProvider,
            sourceScope,
            projectEnvironment,
            providerAndScopeForIncrementalCompilation,
            languageVersionSettings = languageVersionSettings,
            lookupTracker = lookupTracker,
            init = sessionConfigurator
        )
    }

    fun createJavaModuleBasedSession(
        moduleData: FirModuleData,
        sessionProvider: FirProjectSessionProvider,
        scope: AbstractProjectFileSearchScope,
        projectEnvironment: AbstractProjectEnvironment,
        providerAndScopeForIncrementalCompilation: ProviderAndScopeForIncrementalCompilation?,
        languageVersionSettings: LanguageVersionSettings = LanguageVersionSettingsImpl.DEFAULT,
        lookupTracker: LookupTracker? = null,
        init: FirSessionConfigurator.() -> Unit = {}
    ): FirSession {
        return FirCliSession(sessionProvider, FirSession.Kind.Source).apply session@{
            moduleData.bindSession(this@session)
            sessionProvider.registerSession(moduleData, this@session)
            registerModuleData(moduleData)
            registerCliCompilerOnlyComponents()
            registerCommonComponents(languageVersionSettings)
            registerCommonJavaComponents(projectEnvironment.getJavaModuleResolver())
            registerResolveComponents(lookupTracker)
            registerJavaSpecificResolveComponents()

            val kotlinScopeProvider = FirKotlinScopeProvider(::wrapScopeWithJvmMapped)

            val firProvider = FirProviderImpl(this, kotlinScopeProvider)
            register(FirProvider::class, firProvider)

            val symbolProviderForBinariesFromIncrementalCompilation = providerAndScopeForIncrementalCompilation?.let {
                KotlinDeserializedJvmSymbolsProvider(
                    this@session,
                    SingleModuleDataProvider(moduleData),
                    kotlinScopeProvider,
                    it.packagePartProvider,
                    projectEnvironment.getKotlinClassFinder(it.scope),
                    projectEnvironment.getJavaClassFinder(it.scope)
                )
            }

            val dependenciesSymbolProvider = FirDependenciesSymbolProviderImpl(this)
            register(
                FirSymbolProvider::class,
                FirCompositeSymbolProvider(
                    this,
                    listOfNotNull(
                        firProvider.symbolProvider,
                        symbolProviderForBinariesFromIncrementalCompilation,
                        JavaSymbolProvider(this, moduleData, projectEnvironment.getJavaClassFinder(scope)),
                        dependenciesSymbolProvider,
                    )
                )
            )

            register(
                FirDependenciesSymbolProvider::class,
                dependenciesSymbolProvider
            )

            FirJvmDefaultErrorMessages.installJvmErrorMessages()
            FirSessionConfigurator(this).apply {
                registerCommonCheckers()
                registerJvmCheckers()
                init()
            }.configure()
            projectEnvironment.registerAsJavaElementFinder(this)
        }
    }

    fun createLibrarySession(
        mainModuleName: Name,
        sessionProvider: FirProjectSessionProvider,
        moduleDataProvider: ModuleDataProvider,
        scope: AbstractProjectFileSearchScope,
        projectEnvironment: AbstractProjectEnvironment,
        packagePartProvider: PackagePartProvider,
        languageVersionSettings: LanguageVersionSettings = LanguageVersionSettingsImpl.DEFAULT,
    ): FirSession {
        return FirCliSession(sessionProvider, FirSession.Kind.Library).apply session@{
            moduleDataProvider.allModuleData.forEach {
                sessionProvider.registerSession(it, this)
                it.bindSession(this)
            }

            registerCliCompilerOnlyComponents()
            registerCommonComponents(languageVersionSettings)
            registerCommonJavaComponents(projectEnvironment.getJavaModuleResolver())

            val kotlinScopeProvider = FirKotlinScopeProvider(::wrapScopeWithJvmMapped)

            val deserializedProviderForIncrementalCompilation = KotlinDeserializedJvmSymbolsProvider(
                this,
                moduleDataProvider,
                kotlinScopeProvider,
                packagePartProvider,
                projectEnvironment.getKotlinClassFinder(scope),
                projectEnvironment.getJavaClassFinder(scope)
            )

            val builtinsModuleData = createModuleDataForBuiltins(
                mainModuleName,
                moduleDataProvider.platform,
                moduleDataProvider.analyzerServices
            ).also { it.bindSession(this@session) }

            val symbolProvider = FirCompositeSymbolProvider(
                this,
                listOf(
                    deserializedProviderForIncrementalCompilation,
                    FirBuiltinSymbolProvider(this, builtinsModuleData, kotlinScopeProvider),
                    FirCloneableSymbolProvider(this, builtinsModuleData, kotlinScopeProvider),
                    FirDependenciesSymbolProviderImpl(this)
                )
            )
            register(FirSymbolProvider::class, symbolProvider)
            register(FirProvider::class, FirLibrarySessionProvider(symbolProvider))
        }
    }

    @TestOnly
    fun createEmptySession(): FirSession {
        return object : FirSession(null, Kind.Source) {}.apply {
            val moduleData = FirModuleDataImpl(
                Name.identifier("<stub module>"),
                dependencies = emptyList(),
                dependsOnDependencies = emptyList(),
                friendDependencies = emptyList(),
                platform = JvmPlatforms.unspecifiedJvmPlatform,
                analyzerServices = JvmPlatformAnalyzerServices
            )
            registerModuleData(moduleData)
            moduleData.bindSession(this)
        }
    }

    fun createModuleDataForBuiltins(
        parentModuleName: Name,
        platform: TargetPlatform,
        analyzerServices: PlatformDependentAnalyzerServices
    ): FirModuleData {
        return DependencyListForCliModule.createDependencyModuleData(
            Name.special("<builtins of ${parentModuleName.identifier}"),
            platform,
            analyzerServices,
        )
    }
}
