package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.storage.verify.VerifyContextStorageRepository
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toEngineDO

internal class GetListOfVerifyContextsUseCase(private val verifyContextStorageRepository: VerifyContextStorageRepository) : GetListOfVerifyContextsUseCaseInterface {
    override suspend fun getListOfVerifyContexts(): List<EngineDO.VerifyContext> = verifyContextStorageRepository.getAll().map { verifyContext -> verifyContext.toEngineDO() }
}

internal interface GetListOfVerifyContextsUseCaseInterface {
    suspend fun getListOfVerifyContexts(): List<EngineDO.VerifyContext>
}