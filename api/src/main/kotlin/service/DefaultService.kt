package com.ctfp.service

import com.ctfp.domain.db.withTransaction
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.deleteWhere

abstract class DefaultService<ID: Any, DAO: Entity<ID>, RequestDTO: Any, ResponseDTO: Any>(
    private val dao: EntityClass<ID, DAO>,
    private val table: IdTable<ID>,
    protected val apply: DAO.(RequestDTO) -> Unit,
    protected val toDto: DAO.() -> ResponseDTO
) {
    open suspend fun get(id: ID): ResponseDTO = withTransaction {
        dao[id].toDto()
    }

    open suspend fun getAll(): List<ResponseDTO> = withTransaction {
        dao.all().map { it.toDto() }
    }

    open suspend fun create(dto: RequestDTO): ID = withTransaction {
        val newDao = dao.new {
            apply(dto)
        }
        newDao.id.value
    }

    open suspend fun update(id: ID, dto: RequestDTO) = withTransaction {
        dao.findByIdAndUpdate(id) {
            it.apply(dto)
        }
    }

    open suspend fun updateMany(dtos: Map<ID, RequestDTO>) = withTransaction {
        dtos.forEach { (id, dto) ->
            dao[id].apply(dto) //TODO: test if using table would more efficient
        }
    }

    open suspend fun delete(id: ID) = withTransaction {
        dao[id].delete()
    }

    open suspend fun deleteMany(ids: List<ID>) = withTransaction {
        table.deleteWhere { table.id inList ids }
    }

    open suspend fun deleteAll() = withTransaction {
        table.deleteAll()
    }
}