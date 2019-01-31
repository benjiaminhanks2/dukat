package org.jetbrains.dukat.compiler.lowerings

import org.jetbrains.dukat.ast.model.duplicate
import org.jetbrains.dukat.ast.model.nodes.AnnotationNode
import org.jetbrains.dukat.ast.model.nodes.ClassNode
import org.jetbrains.dukat.ast.model.nodes.ConstructorNode
import org.jetbrains.dukat.ast.model.nodes.InterfaceNode
import org.jetbrains.dukat.ast.model.nodes.MethodNode
import org.jetbrains.dukat.ast.model.nodes.PropertyNode
import org.jetbrains.dukat.astCommon.MemberDeclaration
import org.jetbrains.dukat.astCommon.TopLevelDeclaration
import org.jetbrains.dukat.compiler.converters.convertIndexSignatureDeclaration
import org.jetbrains.dukat.compiler.converters.convertPropertyDeclaration
import org.jetbrains.dukat.tsmodel.CallSignatureDeclaration
import org.jetbrains.dukat.tsmodel.ClassDeclaration
import org.jetbrains.dukat.tsmodel.ClassLikeDeclaration
import org.jetbrains.dukat.tsmodel.ConstructorDeclaration
import org.jetbrains.dukat.tsmodel.DocumentRootDeclaration
import org.jetbrains.dukat.tsmodel.FunctionDeclaration
import org.jetbrains.dukat.tsmodel.InterfaceDeclaration
import org.jetbrains.dukat.tsmodel.MethodSignatureDeclaration
import org.jetbrains.dukat.tsmodel.ModifierDeclaration
import org.jetbrains.dukat.tsmodel.PropertyDeclaration
import org.jetbrains.dukat.tsmodel.types.FunctionTypeDeclaration
import org.jetbrains.dukat.tsmodel.types.IndexSignatureDeclaration
import org.jetbrains.dukat.tsmodel.types.ParameterValueDeclaration
import org.jetbrains.dukat.tsmodel.types.TypeDeclaration

private fun FunctionDeclaration.isStatic() = modifiers.contains(ModifierDeclaration.STATIC_KEYWORD)

private fun CallSignatureDeclaration.convert(owner: ClassLikeDeclaration): MethodNode {
    return MethodNode(
            "invoke",
            parameters,
            type,
            typeParameters,
            owner,
            false,
            false,
            true,
            listOf(AnnotationNode("nativeInvoke")))
}

private fun ParameterValueDeclaration.convertNullable(): ParameterValueDeclaration {
    return when (this) {
        is TypeDeclaration -> copy(nullable = true)
        is FunctionTypeDeclaration -> copy(nullable = true)
        else -> duplicate()
    }
}



private fun MethodSignatureDeclaration.convert(owner: ClassLikeDeclaration): MemberDeclaration {
    return if (optional) {
        PropertyNode(
                name,
                FunctionTypeDeclaration(
                        parameters,
                        type,
                        true,
                        null
                ),
                typeParameters,
                owner,
                false,
                false,
                true,
                false
        )
    } else {
        MethodNode(
                name,
                parameters,
                type,
                typeParameters,
                owner,
                false, //TODO: remove static, we don't need it for MethodSignatures
                false,
                false,
                emptyList()
        )
    }
}

private fun ClassDeclaration.convert(): ClassNode {
    return ClassNode(
            name,
            members,
            typeParameters,
            parentEntities,
            null
    )
}

private fun InterfaceDeclaration.convert(): InterfaceNode {
    return InterfaceNode(
            name,
            members,
            typeParameters,
            parentEntities
    )
}

private fun ConstructorDeclaration.convert(owner: ClassLikeDeclaration): ConstructorNode {
    return ConstructorNode(
            parameters,
            typeParameters
    )
}

private class LowerDeclarationsToNodes {
    fun lowerMemberDeclaration(declaration: MemberDeclaration, owner: ClassLikeDeclaration): List<MemberDeclaration> {
        return when (declaration) {
            is FunctionDeclaration -> listOf(MethodNode(
                    declaration.name,
                    declaration.parameters,
                    declaration.type,
                    declaration.typeParameters,
                    owner,
                    declaration.isStatic(),
                    false,
                    false,
                    emptyList()
            ))
            is MethodSignatureDeclaration -> listOf(declaration.convert(owner))
            is CallSignatureDeclaration -> listOf(declaration.convert(owner))
            is PropertyDeclaration -> listOf(convertPropertyDeclaration(declaration, owner))
            is IndexSignatureDeclaration -> convertIndexSignatureDeclaration(declaration, owner)
            is ConstructorDeclaration -> listOf(declaration.convert(owner))
            else -> listOf(declaration)
        }
    }

    fun lowerInterfaceNode(declaration: InterfaceNode): InterfaceNode {
        return declaration.copy(
                members = declaration.members.flatMap { member -> lowerMemberDeclaration(member, declaration) }
        )
    }

    fun lowerClassNode(declaration: ClassNode): ClassNode {
        return declaration.copy(
                members = declaration.members.flatMap { member -> lowerMemberDeclaration(member, declaration) }
        )
    }

    fun lowerTopLevelDeclaration(declaration: TopLevelDeclaration): TopLevelDeclaration {
        return when (declaration) {
            is ClassDeclaration -> lowerClassNode(declaration.convert())
            is InterfaceDeclaration -> lowerInterfaceNode(declaration.convert())
            is DocumentRootDeclaration -> lowerDocumentRoot(declaration)
            else -> declaration
        }
    }

    fun lowerTopLevelDeclarations(declarations: List<TopLevelDeclaration>): List<TopLevelDeclaration> {
        return declarations.map { declaration ->
            lowerTopLevelDeclaration(declaration)
        }
    }

    fun lowerDocumentRoot(documenRoot: DocumentRootDeclaration): DocumentRootDeclaration {
        return documenRoot.copy(declarations = lowerTopLevelDeclarations(documenRoot.declarations))
    }
}

fun DocumentRootDeclaration.introduceNodes(): DocumentRootDeclaration {
    return LowerDeclarationsToNodes().lowerDocumentRoot(this)
}