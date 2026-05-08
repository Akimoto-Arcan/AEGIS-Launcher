package com.jarvis.launcher.domain.usecase

import com.jarvis.launcher.data.model.AppInfo
import com.jarvis.launcher.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetInstalledAppsUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    operator fun invoke(): Flow<List<AppInfo>> = appRepository.getInstalledApps()
}
