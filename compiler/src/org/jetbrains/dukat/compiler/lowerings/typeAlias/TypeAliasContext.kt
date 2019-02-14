package org.jetbrains.dukat.compiler.lowerings.typeAlias

import org.jetbrains.dukat.ast.model.nodes.DynamicTypeNode
import org.jetbrains.dukat.tsmodel.HeritageClauseDeclaration
import org.jetbrains.dukat.tsmodel.HeritageSymbolDeclaration
import org.jetbrains.dukat.tsmodel.IdentifierDeclaration
import org.jetbrains.dukat.tsmodel.PropertyAccessDeclaration
import org.jetbrains.dukat.tsmodel.TypeAliasDeclaration
import org.jetbrains.dukat.tsmodel.types.ParameterValueDeclaration
import org.jetbrains.dukat.tsmodel.types.TypeDeclaration
import org.jetbrains.dukat.tsmodel.types.UnionTypeDeclaration

// TODO: TypeAliases should be revisited
private fun IdentifierDeclaration.translate() = value

private fun HeritageSymbolDeclaration.translate(): String {
    return when (this) {
        is IdentifierDeclaration -> translate()
        is PropertyAccessDeclaration -> expression.translate() + "." + name.translate()
        else -> throw Exception("unknown heritage clause ${this}")
    }
}

class TypeAliasContext {

    private fun TypeAliasDeclaration.canSusbtitute(heritageClause: HeritageClauseDeclaration): Boolean {
        return aliasName == heritageClause.name.translate()
    }

    private fun ParameterValueDeclaration.specify(aliasParamsMap: Map<String, ParameterValueDeclaration>): ParameterValueDeclaration {
        return when (this) {
            is TypeDeclaration -> {
                val paramsSpecified = params.map { param ->
                     when(param) {
                        is TypeDeclaration -> {
                            resolveTypeAlias(aliasParamsMap.getOrDefault(param.value, param.specify(aliasParamsMap)))
                        }
                        else -> param
                    }
                }
                copy(params = paramsSpecified)
            }
            is DynamicTypeNode -> {
                val projectedTypeResolved = projectedType.specify(aliasParamsMap)
                copy(projectedType = projectedTypeResolved)
            }
            is UnionTypeDeclaration -> {
                copy(params = params.map {param -> resolveTypeAlias(param).specify(aliasParamsMap)})
            }
            else -> this
        }
    }

    private fun TypeAliasDeclaration.substitute(type: ParameterValueDeclaration): ParameterValueDeclaration? {
        return when (type) {
            is TypeDeclaration -> {
                if (aliasName == type.value) {
                    if (typeParameters.size == type.params.size) {
                        if (typeReference is TypeDeclaration) {
                            val aliasParamsMap = typeParameters.zip(type.params).associateBy({ it.first.value }, { it.second })
                            return typeReference.specify(aliasParamsMap)
                        } else if (typeReference is DynamicTypeNode) {
                            val aliasParamsMap = typeParameters.zip(type.params).associateBy({ it.first.value }, { it.second })
                            return typeReference.specify(aliasParamsMap)
                        }
                    }
                }

                null
            }
            is DynamicTypeNode -> {
                when (type.projectedType) {
                    is UnionTypeDeclaration -> {
                        val unionTypeDeclaration = type.projectedType as UnionTypeDeclaration
                        type.copy(projectedType = unionTypeDeclaration.copy(params = unionTypeDeclaration.params.map { param ->
                            resolveTypeAlias(param)
                        }))
                    }
                    else -> null
                }
            }
            else -> null
        }
    }

    private val myTypeAliasDeclaration: MutableSet<TypeAliasDeclaration> = mutableSetOf()

    fun registerTypeAlias(typeAlias: TypeAliasDeclaration) {
        myTypeAliasDeclaration.add(typeAlias)
    }

    fun resolveTypeAlias(heritageClause: HeritageClauseDeclaration): ParameterValueDeclaration? {
        myTypeAliasDeclaration.forEach { typeAlias ->
            if (typeAlias.canSusbtitute(heritageClause)) {
                return typeAlias.typeReference
            }
        }

        return null
    }

    fun resolveTypeAlias(type: ParameterValueDeclaration): ParameterValueDeclaration {


        myTypeAliasDeclaration.forEach { typeAlias ->
            typeAlias.substitute(type)?.let {
                return it
            }
        }

        return type
    }
}