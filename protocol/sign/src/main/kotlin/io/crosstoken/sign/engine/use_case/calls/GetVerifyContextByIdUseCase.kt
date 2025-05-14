package io.crosstoken.sign.engine.use_case.calls

import io.crosstoken.android.internal.common.storage.verify.VerifyContextStorageRepository
import io.crosstoken.sign.engine.model.EngineDO
import io.crosstoken.sign.engine.model.mapper.toEngineDO

internal class GetVerifyContextByIdUseCase(private val verifyContextStorageRepository: VerifyContextStorageRepository) : GetVerifyContextByIdUseCaseInterface {
    override suspend fun getVerifyContext(id: Long): EngineDO.VerifyContext? = verifyContextStorageRepository.get(id)?.toEngineDO()
}

internal interface GetVerifyContextByIdUseCaseInterface {
    suspend fun getVerifyContext(id: Long): EngineDO.VerifyContext?
}