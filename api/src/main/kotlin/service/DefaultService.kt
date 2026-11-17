package com.ctfp.service

import com.ctfp.domain.db.withTransaction
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.dao.DaoEntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.dao.exceptions.EntityNotFoundException
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere

abstract class DefaultService<ID : Any, DAO : Entity<ID>, RequestDTO : Any, ResponseDTO : Any>(
    private val dao: EntityClass<ID, DAO>,
    private val table: IdTable<ID>,
    protected val apply: DAO.(RequestDTO) -> Unit,
    protected val toDto: DAO.() -> ResponseDTO,
) {
    open suspend fun get(id: ID): ResponseDTO =
        withTransaction {
            dao[id].toDto()
        }

    open suspend fun getAll(): List<ResponseDTO> =
        withTransaction {
            dao.all().map { it.toDto() }
        }

    open suspend fun create(dto: RequestDTO): ID =
        withTransaction {
            val newDao =
                dao.new {
                    apply(dto)
                }
            newDao.id.value
        }

    open suspend fun update(
        id: ID,
        dto: RequestDTO,
    ) = withTransaction {
        dao.findByIdAndUpdate(id) { it.apply(dto) }
            ?: throw EntityNotFoundException(DaoEntityID(id, table), dao)
    }

    open suspend fun updateMany(dtos: Map<ID, RequestDTO>) =
        withTransaction {
            dao.findAll(dtos.keys).forEach { it.apply(dtos.getValue(it.id.value)) }
        }

    open suspend fun delete(id: ID) =
        withTransaction {
            dao[id].delete()
        }

    open suspend fun deleteMany(ids: List<ID>) =
        withTransaction {
            table.deleteWhere { table.id inList ids }
        }

    open suspend fun deleteAll() =
        withTransaction {
            table.deleteAll()
        }
}

/**
 * Loads all [ids] in one query, throwing [EntityNotFoundException] (404) if any is missing. Use it before bulk updates:
 * `dao[id]` in a loop runs a SELECT per row, and each one flushes the previous row's update instead of batching them.
 */
fun <ID : Any, T : Entity<ID>> EntityClass<ID, T>.findAll(ids: Collection<ID>): List<T> {
    val found = forIds(ids.toList()).toList()
    if (found.size != ids.size) {
        val missing = ids.first { id -> found.none { it.id.value == id } }
        throw EntityNotFoundException(DaoEntityID(missing, table), this)
    }
    return found
}
