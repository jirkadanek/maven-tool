package pom

import maven.POM
import org.junit.Test
import kotlin.test.assertEquals

class POMTest {
    private val projectPom = POM("selfTestData/project/pom.xml")
    private val module1Pom = POM("selfTestData/project/module1/pom.xml")
    private val module3Pom = POM("selfTestData/project/module3/pom.xml")

    @Test fun getArtifactId() {
        assertEquals("leaf1", module1Pom.artifactId())
    }

    @Test fun getModuleNames() {
        assertEquals(listOf("module1", "module2", "module3"), projectPom.moduleNames())
        assertEquals(listOf("module1", "module2", "module3"), projectPom.moduleNames("someProfile"))
        assertEquals(listOf("mpb"), module3Pom.moduleNames("pb"))
    }

    @Test fun getLeafArtifactIds() {
        assertEquals(listOf("leaf1"), module1Pom.leafArtifactIds())
        assertEquals(listOf("leaf1", "leaf2a", "leaf3"), projectPom.leafArtifactIds())
        assertEquals(listOf("leaf1", "leaf2a", "leafmpb"), projectPom.leafArtifactIds("pb"))
    }
}