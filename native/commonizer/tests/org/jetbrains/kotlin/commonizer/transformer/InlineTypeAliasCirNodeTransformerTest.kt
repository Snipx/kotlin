package org.jetbrains.kotlin.commonizer.transformer

import org.jetbrains.kotlin.commonizer.LeafCommonizerTarget
import org.jetbrains.kotlin.commonizer.TargetDependent
import org.jetbrains.kotlin.commonizer.cir.CirClassType
import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.cir.CirPackageName
import org.jetbrains.kotlin.commonizer.mapValue
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.tree.CirTreeRoot
import org.jetbrains.kotlin.commonizer.tree.mergeCirTree
import org.jetbrains.kotlin.commonizer.utils.InlineSourceBuilder
import org.jetbrains.kotlin.commonizer.utils.KtInlineSourceCommonizerTestCase
import org.jetbrains.kotlin.commonizer.utils.createCirTree
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.storage.LockBasedStorageManager

class InlineTypeAliasCirNodeTransformerTest : KtInlineSourceCommonizerTestCase() {

    fun `test inlining typealias to common dependencies`() {

        fun InlineSourceBuilder.ModuleBuilder.withDependencies() = dependency {
            source(
                """
                package dep
                class ClassA
                class ClassB: ClassA()
                """.trimIndent()
            )
        }

        val targetARoot = CirTreeRoot(
            modules = listOf(
                createCirTree {
                    withDependencies()
                    source(
                        """
                       package pkg
                       import dep.*
                       class X : ClassA()
                    """.trimIndent()
                    )
                }
            )
        )

        val targetBRoot = CirTreeRoot(
            modules = listOf(
                createCirTree {
                    withDependencies()
                    source(
                        """
                        package pkg
                        import dep.*
                        typealias X = ClassB
                    """.trimIndent()
                    )
                }
            )
        )

        val roots = TargetDependent(
            LeafCommonizerTarget("a") to targetARoot,
            LeafCommonizerTarget("b") to targetBRoot
        )

        val classifiers = CirKnownClassifiers(
            classifierIndices = roots.mapValue(::CirClassifierIndex),
            targetDependencies = roots.mapValue(CirTreeRoot::dependencies),
            commonizedNodes = CirCommonizedClassifierNodes.default(),
            commonDependencies = CirProvidedClassifiersByModules(
                true, mapOf(
                    CirEntityId.create("dep/ClassA") to CirProvided.RegularClass(
                        typeParameters = emptyList(),
                        kind = ClassKind.CLASS,
                        visibility = Visibilities.Public,
                        supertypes = emptyList()
                    ),
                    CirEntityId.create("dep/ClassB") to CirProvided.RegularClass(
                        typeParameters = emptyList(),
                        kind = ClassKind.CLASS,
                        visibility = Visibilities.Public,
                        supertypes = listOf(
                            CirProvided.ClassType(
                                classId = CirEntityId.create("dep/ClassA"),
                                outerType = null,
                                arguments = emptyList(),
                                isMarkedNullable = false
                            )
                        )
                    )
                )
            )
        )

        val mergedTree = mergeCirTree(LockBasedStorageManager.NO_LOCKS, classifiers, roots)
        InlineTypeAliasCirNodeTransformer(LockBasedStorageManager.NO_LOCKS, classifiers).invoke(mergedTree)

        val pkg = mergedTree.modules.values.single().packages.getValue(CirPackageName.create("pkg"))
        val xClassNode = kotlin.test.assertNotNull(pkg.classes[CirName.create("X")])
        val inlinedXClass = kotlin.test.assertNotNull(xClassNode.targetDeclarations[1])

        kotlin.test.assertEquals(
            setOf(CirEntityId.create("dep/ClassA")),
            inlinedXClass.supertypes.map { (it as? CirClassType)?.classifierId }.toSet()
        )
    }


    fun `test inlining typealias to target dependencies`() {

        fun InlineSourceBuilder.ModuleBuilder.withDependencies() = dependency {
            source(
                """
                package dep
                class ClassA
                class ClassB: ClassA()
                """.trimIndent()
            )
        }

        val targetARoot = CirTreeRoot(
            modules = listOf(
                createCirTree {
                    withDependencies()
                    source(
                        """
                       package pkg
                       import dep.*
                       class X : ClassA()
                    """.trimIndent()
                    )
                }
            )
        )

        val targetBRoot = CirTreeRoot(
            dependencies = CirProvidedClassifiersByModules(
                true, mapOf(
                    CirEntityId.create("dep/ClassA") to CirProvided.RegularClass(
                        typeParameters = emptyList(),
                        kind = ClassKind.CLASS,
                        visibility = Visibilities.Public,
                        supertypes = emptyList()
                    ),
                    CirEntityId.create("dep/ClassB") to CirProvided.RegularClass(
                        typeParameters = emptyList(),
                        kind = ClassKind.CLASS,
                        visibility = Visibilities.Public,
                        supertypes = listOf(
                            CirProvided.ClassType(
                                classId = CirEntityId.create("dep/ClassA"),
                                outerType = null,
                                arguments = emptyList(),
                                isMarkedNullable = false
                            )
                        )
                    )
                )
            ),
            modules = listOf(
                createCirTree {
                    withDependencies()
                    source(
                        """
                        package pkg
                        import dep.*
                        typealias X = ClassB
                    """.trimIndent()
                    )
                }
            )
        )

        val roots = TargetDependent(
            LeafCommonizerTarget("a") to targetARoot,
            LeafCommonizerTarget("b") to targetBRoot
        )

        val classifiers = CirKnownClassifiers(
            classifierIndices = roots.mapValue(::CirClassifierIndex),
            targetDependencies = roots.mapValue(CirTreeRoot::dependencies),
            commonizedNodes = CirCommonizedClassifierNodes.default(),
            commonDependencies = CirProvidedClassifiers.EMPTY
        )

        val mergedTree = mergeCirTree(LockBasedStorageManager.NO_LOCKS, classifiers, roots)
        InlineTypeAliasCirNodeTransformer(LockBasedStorageManager.NO_LOCKS, classifiers).invoke(mergedTree)

        val pkg = mergedTree.modules.values.single().packages.getValue(CirPackageName.create("pkg"))
        val xClassNode = kotlin.test.assertNotNull(pkg.classes[CirName.create("X")])
        val inlinedXClass = kotlin.test.assertNotNull(xClassNode.targetDeclarations[1])

        kotlin.test.assertEquals(
            setOf(CirEntityId.create("dep/ClassA")),
            inlinedXClass.supertypes.map { (it as? CirClassType)?.classifierId }.toSet()
        )
    }
}

