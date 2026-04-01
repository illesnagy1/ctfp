package com.ctfp.repository

import com.ctfp.dto.Submission


interface SubmissionRepository {
    suspend fun getSubmission(id: Int): Submission
    suspend fun getAllSubmissions(): List<Submission>
    suspend fun createSubmission(submission: Submission): Int
    suspend fun updateSubmission(id: Int, submission: Submission)
    suspend fun deleteSubmission(id: Int)
}
