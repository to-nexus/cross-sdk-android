package io.crosstoken.android.internal.common.explorer.domain.usecase

import io.crosstoken.android.internal.common.explorer.ExplorerRepository
import io.crosstoken.android.internal.common.explorer.data.model.Project

class GetProjectsWithPaginationUseCase(
    private val explorerRepository: ExplorerRepository,
) {
    suspend operator fun invoke(page: Int, entries: Int, isVerified: Boolean, isFeatured: Boolean): Result<List<Project>> =
        runCatching { explorerRepository.getProjects(page, entries, isVerified, isFeatured).projects.sortedBy { it.order ?: Long.MAX_VALUE } }
}
