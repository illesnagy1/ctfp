package com.ctfp.service

import com.ctfp.domain.dao.SubmissionDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.domain.model.FlagTable
import com.ctfp.domain.model.TeamTable
import com.ctfp.domain.model.UserTable
import com.ctfp.dto.Submission
import org.jetbrains.exposed.v1.core.dao.id.EntityID


class SubmissionService {
    suspend fun getSubmission(id: Int): Submission = withTransaction {
        SubmissionDAO[id].toModel()
    }

    suspend fun getAllSubmissions(): List<Submission> = withTransaction {
        SubmissionDAO.all().map { it.toModel() }
    }

    suspend fun createSubmission(submission: Submission): Int = withTransaction {
        val newSubmission = SubmissionDAO.new {
            userId = EntityID(submission.userId, UserTable)
            teamId = submission.teamId?.let { EntityID(it, TeamTable) }
            challengeId = EntityID(submission.challengeId, ChallengeTable)
            flagId = submission.flagId?.let { EntityID(it, FlagTable) }
            submittedAt = submission.submittedAt
        }
        newSubmission.id.value
    }

    suspend fun updateSubmission(id: Int, submission: Submission) = withTransaction {
        val dbSubmission = SubmissionDAO[id]
        dbSubmission.run {
            userId = EntityID(submission.userId, UserTable)
            teamId = submission.teamId?.let { EntityID(it, TeamTable) }
            challengeId = EntityID(submission.challengeId, ChallengeTable)
            flagId = submission.flagId?.let { EntityID(it, FlagTable) }
            submittedAt = submission.submittedAt
        }
    }

    suspend fun deleteSubmission(id: Int) = withTransaction {
        SubmissionDAO[id].delete()
    }
}
