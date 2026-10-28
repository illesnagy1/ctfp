package com.ctfp.service

import com.ctfp.config.enum.UserRole
import com.ctfp.domain.dao.FlagDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.SubmissionTable
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.DecayFunction
import com.ctfp.dto.ScoreboardEntry
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.select
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.time.Instant

/**
 * Ranks players by points: each solved challenge is worth the current value of the flag that solved it. Values decay
 * with the number of players who solved that flag, for everyone who solved it (dynamic scoring), never below its
 * minimum. Hidden, banned and admin accounts aren't ranked and don't count as solvers.
 */
class ScoreboardService {
    suspend fun get(): List<ScoreboardEntry> = withTransaction {
        val players = UserTable.select(UserTable.id, UserTable.username, UserTable.country)
            .where { (UserTable.isHidden eq false) and (UserTable.isBanned eq false) and (UserTable.role neq UserRole.ADMIN) }
            .associate { it[UserTable.id].value to (it[UserTable.username] to it[UserTable.country]) }

        // One correct submission per player and challenge (later guesses are refused once solved).
        val solves = SubmissionTable
            .select(SubmissionTable.userId, SubmissionTable.flagId, SubmissionTable.submittedAt)
            .where { SubmissionTable.isCorrect eq true }
            .map { Solve(it[SubmissionTable.userId].value, it[SubmissionTable.flagId]?.value, it[SubmissionTable.submittedAt]) }
            .filter { it.userId in players }

        val solversPerFlag = solves.mapNotNull { it.flagId }.groupingBy { it }.eachCount()
        val values = FlagDAO.forIds(solversPerFlag.keys.toList())
            .associate { it.id.value to it.value(solversPerFlag.getValue(it.id.value)) }

        val byPlayer = solves.groupBy { it.userId }
        players.map { (id, info) ->
            val mine = byPlayer[id].orEmpty()
            ScoreboardEntry(
                userId = id,
                username = info.first,
                country = info.second,
                solves = mine.size,
                // A solve whose flag was deleted still counts, but its value is gone.
                points = mine.sumOf { solve -> solve.flagId?.let { values[it] } ?: 0 },
                lastSolveAt = mine.maxOfOrNull { it.at },
            )
        }.sortedWith(
            compareByDescending<ScoreboardEntry> { it.points }
                .thenBy(nullsLast()) { it.lastSolveAt }
                .thenBy { it.username.lowercase() },
        )
    }

    private data class Solve(val userId: Int, val flagId: Int?, val at: Instant)

    companion object {
        /**
         * The flag's value once [solvers] players have solved it. The first solver gets the full value; `decayValue`
         * scales how fast it drops: per solve (linear), by the log of the solves, or by their square (parabolic).
         */
        fun FlagDAO.value(solvers: Int): Int {
            val decay = decayValue ?: 0
            val after = (solvers - 1).coerceAtLeast(0).toDouble()
            val decayed = when (DecayFunction.valueOf(decayFun.uppercase())) {
                DecayFunction.STATIC -> return baseValue
                DecayFunction.LINEAR -> baseValue - decay * after
                DecayFunction.LOGARITHMIC -> baseValue - decay * ln(1 + after)
                DecayFunction.PARABOLIC -> baseValue - decay * after * after
            }
            return decayed.roundToInt().coerceAtLeast(minValue ?: 0).coerceAtMost(baseValue)
        }
    }
}
