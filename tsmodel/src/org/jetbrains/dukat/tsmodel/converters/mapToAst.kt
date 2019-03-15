package org.jetbrains.dukat.tsmodel.converters

import org.jetbrains.dukat.astCommon.AstNode
import org.jetbrains.dukat.astCommon.Declaration
import org.jetbrains.dukat.tsmodel.CallSignatureDeclaration
import org.jetbrains.dukat.tsmodel.ClassDeclaration
import org.jetbrains.dukat.tsmodel.ConstructorDeclaration
import org.jetbrains.dukat.tsmodel.DefinitionInfoDeclaration
import org.jetbrains.dukat.tsmodel.EnumDeclaration
import org.jetbrains.dukat.tsmodel.EnumTokenDeclaration
import org.jetbrains.dukat.tsmodel.ExportAssignmentDeclaration
import org.jetbrains.dukat.tsmodel.ExpressionDeclaration
import org.jetbrains.dukat.tsmodel.FunctionDeclaration
import org.jetbrains.dukat.tsmodel.HeritageClauseDeclaration
import org.jetbrains.dukat.tsmodel.IdentifierDeclaration
import org.jetbrains.dukat.tsmodel.ImportEqualsDeclaration
import org.jetbrains.dukat.tsmodel.InterfaceDeclaration
import org.jetbrains.dukat.tsmodel.MethodSignatureDeclaration
import org.jetbrains.dukat.tsmodel.ModifierDeclaration
import org.jetbrains.dukat.tsmodel.PackageDeclaration
import org.jetbrains.dukat.tsmodel.ParameterDeclaration
import org.jetbrains.dukat.tsmodel.PropertyAccessDeclaration
import org.jetbrains.dukat.tsmodel.PropertyDeclaration
import org.jetbrains.dukat.tsmodel.QualifiedNamedDeclaration
import org.jetbrains.dukat.tsmodel.SourceFileDeclaration
import org.jetbrains.dukat.tsmodel.SourceSetDeclaration
import org.jetbrains.dukat.tsmodel.ThisTypeDeclaration
import org.jetbrains.dukat.tsmodel.TypeAliasDeclaration
import org.jetbrains.dukat.tsmodel.TypeParameterDeclaration
import org.jetbrains.dukat.tsmodel.VariableDeclaration
import org.jetbrains.dukat.tsmodel.types.FunctionTypeDeclaration
import org.jetbrains.dukat.tsmodel.types.IndexSignatureDeclaration
import org.jetbrains.dukat.tsmodel.types.IntersectionTypeDeclaration
import org.jetbrains.dukat.tsmodel.types.ObjectLiteralDeclaration
import org.jetbrains.dukat.tsmodel.types.StringTypeDeclaration
import org.jetbrains.dukat.tsmodel.types.TupleDeclaration
import org.jetbrains.dukat.tsmodel.types.TypeDeclaration
import org.jetbrains.dukat.tsmodel.types.UnionTypeDeclaration


@Suppress("UNCHECKED_CAST")
private fun <T: AstNode> Map<String, Any?>.getEntity(key: String) = (get(key) as Map<String, Any?>?)!!.toAst<T>()

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.getEntitiesList(key: String) = get(key) as List<Map<String, Any?>>

private fun <T : Declaration> Map<String, Any?>.getEntities(key: String, mapper: (Map<String, Any?>) -> T = {
    it.toAst()
} ) =
        getEntitiesList(key).map(mapper)

@Suppress("UNCHECKED_CAST")
private fun Map<String, Any?>.getInitializerExpression(): ExpressionDeclaration? {
    val initializer = get("initializer") as Map<String, Any?>?
    return initializer?.let {
        val expression = it.toAst<Declaration>()

        if (expression is ExpressionDeclaration) {
            if (expression.kind.value == "definedExternally") {
                expression
            } else throw Exception("unkown initializer")
        } else null
    }
}

private fun Map<String, Any?>.parameterDeclarationToAst() =
        ParameterDeclaration(
                get("name") as String,
                (getEntity("type")),
                getInitializerExpression(),
                get("vararg") as Boolean,
                get("optional") as Boolean
        )

@Suppress("UNCHECKED_CAST")
fun <T : AstNode> Map<String, Any?>.toAst(): T {
    val reflectionType = get("reflection") as String
    val res = when (reflectionType) {
        TupleDeclaration::class.simpleName -> TupleDeclaration(getEntities("params"))
        ThisTypeDeclaration::class.simpleName -> ThisTypeDeclaration()
        EnumDeclaration::class.simpleName -> EnumDeclaration(
            get("name") as String,
            getEntities("values")
        )
        EnumTokenDeclaration::class.simpleName -> EnumTokenDeclaration(get("value") as String, get("meta") as String)
        ExportAssignmentDeclaration::class.simpleName -> ExportAssignmentDeclaration(
                get("name") as String,
                get("isExportEquals") as Boolean
        )
        IntersectionTypeDeclaration::class.simpleName -> IntersectionTypeDeclaration(
                getEntities("params")
        )
        UnionTypeDeclaration::class.simpleName -> UnionTypeDeclaration(
            getEntities("params")
        )
        HeritageClauseDeclaration::class.simpleName -> HeritageClauseDeclaration(
                getEntity(HeritageClauseDeclaration::name.name),
                getEntities(HeritageClauseDeclaration::typeArguments.name),
                get(HeritageClauseDeclaration::extending.name) as Boolean
        )
        TypeAliasDeclaration::class.simpleName -> TypeAliasDeclaration(
                get("aliasName") as String,
                getEntities("typeParameters"),
                getEntity("typeReference")
        )
        StringTypeDeclaration::class.simpleName -> StringTypeDeclaration(
                get(StringTypeDeclaration::tokens.name) as List<String>
        )
        IndexSignatureDeclaration::class.simpleName -> IndexSignatureDeclaration(
                getEntities(IndexSignatureDeclaration::indexTypes.name),
                getEntity(IndexSignatureDeclaration::returnType.name)
        )
        CallSignatureDeclaration::class.simpleName -> CallSignatureDeclaration(
                getEntities("parameters"),
                getEntity("type"),
                getEntities("typeParameters")
        )
        ExpressionDeclaration::class.simpleName -> ExpressionDeclaration(
                (getEntity("kind")),
                get("meta") as String
        )
        ModifierDeclaration::class.simpleName -> ModifierDeclaration(get("token") as String)
        TypeDeclaration::class.simpleName -> TypeDeclaration(if (get("value") is String) {
            get("value") as String
        } else {
            throw Exception("failed to create type declaration from ${this}")
        }, getEntities("params"))
        ConstructorDeclaration::class.simpleName -> ConstructorDeclaration(
                getEntities("parameters"),
                getEntity("type"),
                getEntities("typeParameters"),
                getEntities("modifiers")
        )
        FunctionDeclaration::class.simpleName -> FunctionDeclaration(
                get("name") as String,
                getEntities("parameters"),
                getEntity("type"),
                getEntities("typeParameters"),
                getEntities("modifiers"),
                get("uid") as String
        )
        PropertyAccessDeclaration::class.simpleName -> PropertyAccessDeclaration(
                getEntity("name"),
                getEntity("expression")
        )
        MethodSignatureDeclaration::class.simpleName -> MethodSignatureDeclaration(
                get(MethodSignatureDeclaration::name.name) as String,
                getEntities(MethodSignatureDeclaration::parameters.name),
                getEntity(MethodSignatureDeclaration::type.name),
                getEntities(MethodSignatureDeclaration::typeParameters.name),
                get(MethodSignatureDeclaration::optional.name) as Boolean,
                getEntities(MethodSignatureDeclaration::modifiers.name)
        )
        FunctionTypeDeclaration::class.simpleName -> FunctionTypeDeclaration(
                getEntities("parameters"),
                getEntity("type")
        )
        ParameterDeclaration::class.simpleName -> parameterDeclarationToAst()
        VariableDeclaration::class.simpleName -> VariableDeclaration(
                get("name") as String,
                getEntity("type"),
                getEntities("modifiers"),
                get("uid") as String
        )
        PropertyDeclaration::class.simpleName -> PropertyDeclaration(
                get("name") as String,
                getEntity("type"),
                getEntities("typeParameters"),
                get("optional") as Boolean,
                getEntities("modifiers")
        )
        SourceFileDeclaration::class.simpleName -> SourceFileDeclaration(
                get("fileName") as String,
                getEntity("root"),
                getEntities("referencedFiles")
        )
        SourceSetDeclaration::class.simpleName -> SourceSetDeclaration(
                getEntities("sources")
        )
        PackageDeclaration::class.simpleName -> PackageDeclaration(
                get("packageName") as String,
                getEntities("declarations"),
                getEntities("modifiers"),
                getEntities("definitionsInfo"),
                get("uid") as String,
                get("resourceName") as String
        )
        TypeParameterDeclaration::class.simpleName -> TypeParameterDeclaration(get("name") as String, getEntities("constraints"))
        ClassDeclaration::class.simpleName -> ClassDeclaration(
                get("name") as String,
                getEntities("members"),
                getEntities("typeParameters"),
                getEntities("parentEntities"),
                getEntities("modifiers"),
                get("uid") as String
        )
        ImportEqualsDeclaration::class.simpleName -> ImportEqualsDeclaration(
            get("name") as String,
            getEntity("moduleReference")
        )
        IdentifierDeclaration::class.simpleName -> IdentifierDeclaration(
                get("value") as String
        )
        InterfaceDeclaration::class.simpleName -> InterfaceDeclaration(
                get("name") as String,
                getEntities("members"),
                getEntities("typeParameters"),
                getEntities("parentEntities"),
                getEntities("definitionsInfo"),
                get("uid") as String
        )
        DefinitionInfoDeclaration::class.simpleName -> DefinitionInfoDeclaration(
                get("fileName") as String
        )
        ObjectLiteralDeclaration::class.simpleName -> ObjectLiteralDeclaration(getEntities("members"))
        QualifiedNamedDeclaration::class.simpleName -> QualifiedNamedDeclaration(
           getEntity("left"),
           getEntity("right")
        )
        else -> throw Exception("failed to create declaration from mapper: ${this}")
    }

    return res as T
}