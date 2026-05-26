package com.ctfp.service

import com.ctfp.domain.dao.SubmissionDAO
import com.ctfp.domain.db.withTransaction
import com.ctfp.dto.SubmissionRequest
import com.ctfp.dto.SubmissionResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class SubmissionService {
    suspend fun getSubmission(id: Int): SubmissionResponse = withTransaction {
        SubmissionDAO[id].toDto()
    }

    suspend fun getAllSubmissions(): List<SubmissionResponse> = withTransaction {
        SubmissionDAO.all().map { it.toDto() }
    }

    suspend fun createSubmission(submission: SubmissionRequest): Int = withTransaction {
        val newSubmission = SubmissionDAO.new {
            apply(submission)
        }
        newSubmission.id.value
    }

    suspend fun updateSubmission(id: Int, submission: SubmissionRequest) = withTransaction {
        SubmissionDAO.findByIdAndUpdate(id) {
            it.apply(submission)
        }
    }

    suspend fun deleteSubmission(id: Int) = withTransaction {
        SubmissionDAO[id].delete()
    }
}