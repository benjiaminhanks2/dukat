package org.jetbrains.dukat.ts.translator

import RemoveDuplicateMembers
import org.jetbrains.dukat.astCommon.NameEntity
import org.jetbrains.dukat.astModel.SourceSetModel
import org.jetbrains.dukat.commonLowerings.AddExplicitGettersAndSetters
import org.jetbrains.dukat.commonLowerings.AddImports
import org.jetbrains.dukat.commonLowerings.AnyfyUnresolvedTypes
import org.jetbrains.dukat.commonLowerings.RemoveUnsupportedJsNames
import org.jetbrains.dukat.commonLowerings.SeparateNonExternalEntities
import org.jetbrains.dukat.commonLowerings.SubstituteTsStdLibEntities
import org.jetbrains.dukat.commonLowerings.merge.MergeClassLikesAndModuleDeclarations
import org.jetbrains.dukat.commonLowerings.merge.MergeVarsAndInterfaces
import org.jetbrains.dukat.commonLowerings.merge.SpecifyTypeNodesWithModuleData
import org.jetbrains.dukat.model.commonLowerings.AddStandardImportsAndAnnotations
import org.jetbrains.dukat.model.commonLowerings.CorrectStdLibTypes
import org.jetbrains.dukat.model.commonLowerings.EscapeIdentificators
import org.jetbrains.dukat.model.commonLowerings.LowerOverrides
import org.jetbrains.dukat.model.commonLowerings.RemoveConflictingOverloads
import org.jetbrains.dukat.model.commonLowerings.RemoveKotlinBuiltIns
import org.jetbrains.dukat.model.commonLowerings.RemoveRedundantTypeParams
import org.jetbrains.dukat.model.commonLowerings.lower
import org.jetbrains.dukat.moduleNameResolver.ModuleNameResolver
import org.jetbrains.dukat.nodeIntroduction.IntroduceNodes
import org.jetbrains.dukat.nodeIntroduction.LowerThisType
import org.jetbrains.dukat.nodeIntroduction.ResolveModuleAnnotations
import org.jetbrains.dukat.tsLowerings.AddPackageName
import org.jetbrains.dukat.tsLowerings.DesugarArrayDeclarations
import org.jetbrains.dukat.tsLowerings.FilterOutNonDeclarations
import org.jetbrains.dukat.tsLowerings.FixImpossibleInheritance
import org.jetbrains.dukat.tsLowerings.GenerateInterfaceReferences
import org.jetbrains.dukat.tsLowerings.IntroduceSyntheticExportModifiers
import org.jetbrains.dukat.tsLowerings.LowerPartialOf
import org.jetbrains.dukat.tsLowerings.LowerPrimitives
import org.jetbrains.dukat.tsLowerings.MergeClassLikes
import org.jetbrains.dukat.tsLowerings.MergeModules
import org.jetbrains.dukat.tsLowerings.RemoveThisParameters
import org.jetbrains.dukat.tsLowerings.RenameImpossibleDeclarations
import org.jetbrains.dukat.tsLowerings.ResolveCollections
import org.jetbrains.dukat.tsLowerings.ResolveDefaultTypeParams
import org.jetbrains.dukat.tsLowerings.ResolveLambdaParents
import org.jetbrains.dukat.tsLowerings.ResolveLoops
import org.jetbrains.dukat.tsLowerings.ResolveTypescriptUtilityTypes
import org.jetbrains.dukat.tsLowerings.lower
import org.jetbrains.dukat.tsmodel.SourceSetDeclaration
import org.jetrbains.dukat.nodeLowering.lowerings.IntroduceMissedOverloads
import org.jetrbains.dukat.nodeLowering.lowerings.RearrangeConstructors
import org.jetrbains.dukat.nodeLowering.lowerings.RemoveUnusedGeneratedEntities
import org.jetrbains.dukat.nodeLowering.lowerings.SpecifyUnionType
import org.jetrbains.dukat.nodeLowering.lowerings.introduceModels
import org.jetrbains.dukat.nodeLowering.lowerings.lower
import org.jetrbains.dukat.nodeLowering.lowerings.typeAlias.ResolveTypeAliases

open class TypescriptLowerer(
        private val moduleNameResolver: ModuleNameResolver,
        private val packageName: NameEntity?
) : ECMAScriptLowerer {
    override fun lower(sourceSet: SourceSetDeclaration): SourceSetModel {
        val declarations = sourceSet
                .lower(
                        AddPackageName(packageName),
                        RemoveThisParameters(),
                        MergeModules(),
                        MergeClassLikes(),
                        IntroduceSyntheticExportModifiers(),
                        ResolveLambdaParents(),
                        FilterOutNonDeclarations(),
                        RenameImpossibleDeclarations(),
                        ResolveTypescriptUtilityTypes(),
                        ResolveDefaultTypeParams(),
                        LowerPrimitives(),
                        GenerateInterfaceReferences(),
                        DesugarArrayDeclarations(),
                        FixImpossibleInheritance(),
                        LowerPartialOf(),
                        ResolveLoops(),
                        ResolveCollections()
                )


        val nodes = IntroduceNodes(moduleNameResolver)
                .lower(declarations)
                .lower(
                        ResolveModuleAnnotations(),
                        LowerThisType(),
                        ResolveTypeAliases(),
                        SpecifyUnionType(),
                        RemoveUnusedGeneratedEntities(),
                        RearrangeConstructors(),
                        IntroduceMissedOverloads()
                )

        val models = nodes
                .introduceModels()
                .lower(
                        RemoveRedundantTypeParams(),
                        RemoveConflictingOverloads(),
                        SubstituteTsStdLibEntities(),
                        EscapeIdentificators(),
                        RemoveUnsupportedJsNames(),
                        MergeClassLikesAndModuleDeclarations(),
                        MergeVarsAndInterfaces(),
                        SeparateNonExternalEntities(),
                        LowerOverrides(),
                        SpecifyTypeNodesWithModuleData(),
                        AddExplicitGettersAndSetters(),
                        AnyfyUnresolvedTypes(),
                        RemoveKotlinBuiltIns(),
                        CorrectStdLibTypes(),
                        RemoveDuplicateMembers(),
                        AddImports(),
                        AddStandardImportsAndAnnotations()
                )

        return models
    }
}