package com.ctfp.repository.postgres

import com.ctfp.dto.Submission
import com.ctfp.repository.SubmissionRepository


class PostgresSubmissionRepository : SubmissionRepository {
    override suspend fun getSubmission(id: Int): Submission {
        TODO("Not yet implemented")
    }

    override suspend fun getAllSubmissions(): List<Submission> {
        TODO("Not yet implemented")
    }

    override suspend fun createSubmission(submission: Submission): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateSubmission(id: Int, submission: Submission) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSubmission(id: Int) {
        TODO("Not yet implemented")
    }
}
