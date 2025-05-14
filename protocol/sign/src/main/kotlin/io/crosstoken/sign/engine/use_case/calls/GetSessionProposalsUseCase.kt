package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.utils.CoreValidator.isExpired
import io.crosstoken.sign.common.model.vo.proposal.ProposalVO
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toEngineDO
import io.crosstoken.sign.storage.proposal.ProposalStorageRepository
import kotlinx.coroutines.supervisorScope

internal class GetSessionProposalsUseCase(private val proposalStorageRepository: ProposalStorageRepository) : GetSessionProposalsUseCaseInterface {
    override suspend fun getSessionProposals(): List<EngineDO.SessionProposal> =
        supervisorScope {
            proposalStorageRepository.getProposals().filter { proposal -> proposal.expiry?.let { !it.isExpired() } ?: true }.map(ProposalVO::toEngineDO)
        }
}

internal interface GetSessionProposalsUseCaseInterface {
    suspend fun getSessionProposals(): List<EngineDO.SessionProposal>
}