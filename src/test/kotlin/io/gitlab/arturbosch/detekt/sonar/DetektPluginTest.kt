package io.gitlab.arturbosch.detekt.sonar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.sonar.api.SonarEdition
import org.sonar.api.SonarQubeSide
import org.sonar.api.internal.PluginContextImpl
import org.sonar.api.internal.SonarRuntimeImpl
import org.sonar.api.utils.Version

class DetektPluginTest {

    @Test
    fun `plugin extensions compatible with 9_9`() {
        val runtime = SonarRuntimeImpl.forSonarQube(
            Version.create(9, 9),
            SonarQubeSide.SERVER,
            SonarEdition.COMMUNITY
        )

        val context = PluginContextImpl.Builder().setSonarRuntime(runtime).build()
        DetektPlugin().define(context)

        assertThat(context.extensions).hasSize(6)
    }
}
