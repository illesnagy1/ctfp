package com.ctfp.repository.postgres

import com.ctfp.domain.dao.SubmissionDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.domain.model.TeamTable
import com.ctfp.dto.Submission
import com.ctfp.repository.SubmissionRepository
import org.jetbrains.exposed.v1.core.dao.id.EntityID


class PostgresSubmissionRepository : SubmissionRepository {
    override suspend fun getSubmission(id: Int): Submission = withTransaction {
        SubmissionDAO[id].toModel()
    }

    override suspend fun getAllSubmissions(): List<Submission> = withTransaction {
        SubmissionDAO.all().map { it.toModel() }
    }

    override suspend fun createSubmission(submission: Submission): Int = withTransaction {
        val newSubmission = SubmissionDAO.new {
            userId = submission.userId
            teamId = submission.teamId
            challengeId = submission.challengeId
            flagId = submission.flagId
            submittedFlag = submission.submittedFlag
            submittedAt = submission.submittedAt
        }
        newSubmission.id.value
    }

    override suspend fun updateSubmission(id: Int, submission: Submission) = withTransaction {
        val dbSubmission = SubmissionDAO[id]
        dbSubmission.run {
            userId = submission.userId
            teamId = submission.teamId
            challengeId = submission.challengeId
            flagId = submission.flagId
            submittedFlag = submission.submittedFlag
            submittedAt = submission.submittedAt
        }
    }

    override suspend fun deleteSubmission(id: Int) = withTransaction {
        SubmissionDAO[id].delete()
    }
}
