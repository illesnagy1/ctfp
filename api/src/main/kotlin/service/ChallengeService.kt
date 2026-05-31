package com.ctfp.service

import com.ctfp.domain.dao.ChallengeDAO
import com.ctfp.domain.dao.SubmissionDAO
import com.ctfp.domain.model.ChallengeTable
import com.ctfp.dto.ChallengeRequest
import com.ctfp.dto.ChallengeResponse
import com.ctfp.dto.apply
import com.ctfp.dto.toDto


class ChallengeService : DefaultService<Int, ChallengeDAO, ChallengeRequest, ChallengeResponse>(
    dao = ChallengeDAO,
    table = ChallengeTable,
    apply = ChallengeDAO::apply,
    toDto = ChallengeDAO::toDto
) {
    fun getAllFlags(id: Int) = ChallengeDAO[id].flags
    fun getAllSubmissions(id: Int) = ChallengeDAO[id].submissions
    fun getAllHints(id: Int) = ChallengeDAO[id].hints
}
