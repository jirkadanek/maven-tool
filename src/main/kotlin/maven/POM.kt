package maven

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.nio.file.Paths
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * POM provides access to information contained in a pom.xml file.
 */
class POM(val path: String) {
    private val document: Document
    private val xpath: XPath

    init {
        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(path))
        document.normalize()
        xpath = XPathFactory.newInstance().newXPath()
    }

    fun artifactId(): String {
        return xpath.evaluate("/project/artifactId", document)
    }

    fun moduleNames(profile: String? = null): List<String> {
        return if (profile == null ) {
            xpath.evaluate("/project/modules/module", document, XPathConstants.NODESET) as NodeList
        } else {
            assert(profile != "")
            xpath.evaluate("/project/modules/module | /project/profiles/profile[id=\"$profile\"]/modules/module",
                    document, XPathConstants.NODESET) as NodeList
        }.map { it.textContent }
    }

    private fun modulePOMs(profile: String?): List<POM> {
        return moduleNames(profile).map { POM(Paths.get(path).parent.resolve("$it/pom.xml").toString()) }
    }

    /**
     * Method leafArtifactIds returns artifactIds of all POMs in the leaves of the pom files hierarchy,
     * i.e. do not define submodules.
     *
     * @param profile: analogous to `mvn -P` switch. It may contain only one profile name.
     */
    fun leafArtifactIds(profile: String? = null): List<String> {
        val poms = modulePOMs(profile)
        return if (poms.size == 0) {
            listOf(artifactId())
        } else {
            poms.flatMap { it.leafArtifactIds(profile) }.sorted()
        }
    }
}

fun <R> NodeList.map(transform: (Node) -> R): List<R> {
    val list = ArrayList<R>(length)
    for (i in 0..length - 1) {
        list.add(transform(item(i)))
    }
    return list
}



// got stuck on http://stackoverflow.com/questions/8887869/building-effective-model-with-defaultmodelbuilder-build
//import org.apache.maven.model.Parent
//import org.apache.maven.model.Repository
//import org.apache.maven.model.building.*
//import org.apache.maven.model.composition.DefaultDependencyManagementImporter
//import org.apache.maven.model.inheritance.DefaultInheritanceAssembler
//import org.apache.maven.model.interpolation.StringSearchModelInterpolator
//import org.apache.maven.model.io.DefaultModelReader
//import org.apache.maven.model.locator.DefaultModelLocator
//import org.apache.maven.model.management.DefaultDependencyManagementInjector
//import org.apache.maven.model.management.DefaultPluginManagementInjector
//import org.apache.maven.model.normalization.DefaultModelNormalizer
//import org.apache.maven.model.path.DefaultModelPathTranslator
//import org.apache.maven.model.path.DefaultModelUrlNormalizer
//import org.apache.maven.model.path.DefaultPathTranslator
//import org.apache.maven.model.path.DefaultUrlNormalizer
//import org.apache.maven.model.plugin.DefaultLifecycleBindingsInjector
//import org.apache.maven.model.plugin.DefaultPluginConfigurationExpander
//import org.apache.maven.model.plugin.DefaultReportConfigurationExpander
//import org.apache.maven.model.plugin.DefaultReportingConverter
//import org.apache.maven.model.profile.DefaultProfileInjector
//import org.apache.maven.model.profile.DefaultProfileSelector
//import org.apache.maven.model.resolution.ModelResolver
//import org.apache.maven.model.resolution.WorkspaceModelResolver
//import org.apache.maven.model.superpom.DefaultSuperPomProvider
//import org.apache.maven.model.validation.DefaultModelValidator
//import org.apache.maven.project.DefaultProjectBuildingRequest
//import org.apache.maven.project.MavenProject
//import org.apache.maven.project.ProjectBuildingRequest
//import org.apache.maven.repository.internal.DefaultVersionRangeResolver
//import org.eclipse.aether.internal.impl.DefaultArtifactResolver
//import org.eclipse.aether.internal.impl.DefaultRemoteRepositoryManager
//import org.eclipse.aether.repository.RemoteRepository
//class POM(val path: String) {
//    private val model: ModelBuildingResult
//
//    init {
//        val request = DefaultModelBuildingRequest()
////        request.modelResolver = org.apache.maven.repository.externalized.DefaultModelResolver(
////                DefaultProjectBuildingRequest().repositorySession,
////                null, null,
////                DefaultArtifactResolver(),
////                DefaultVersionRangeResolver(),
////                DefaultRemoteRepositoryManager(),
////                emptyList<RemoteRepository>())
//        request.pomFile = File(path)
//        val builder = DefaultModelBuilder()
//        builder.setDefaults()
//        model = builder.build(request)
//    }
//
//    fun artifactId(): String {
//        return model.effectiveModel.artifactId
//    }
//
//    fun moduleNames(): List<String> {
//        return model.effectiveModel.modules
//    }
//
//    fun modulePOMs(): List<POM> {
//        return moduleNames().map { POM(Paths.get(path).parent.resolve("$it/pom.xml").toString()) }
//    }
//
//    fun leafArtifactIds(): List<String> {
//        val poms = modulePOMs()
//        return if (poms.size == 0) {
//            listOf(artifactId())
//        } else {
//            poms.flatMap { it.leafArtifactIds() }
//        }
//    }
//}
//
//fun DefaultModelBuilder.setDefaults() {
//    // http://stackoverflow.com/questions/8887869/building-effective-model-with-defaultmodelbuilder-build
//    val modelProcessor = DefaultModelProcessor()
//            .setModelLocator(DefaultModelLocator())
//            .setModelReader(DefaultModelReader());
//
//    val modelInterpolator = StringSearchModelInterpolator()
//            .setPathTranslator(DefaultPathTranslator())
//            .setUrlNormalizer(DefaultUrlNormalizer());
//
//    setProfileSelector(DefaultProfileSelector());
//    setModelProcessor(modelProcessor);
//    setModelValidator(DefaultModelValidator());
//    setSuperPomProvider(DefaultSuperPomProvider().setModelProcessor(modelProcessor));
//    setModelNormalizer(DefaultModelNormalizer());
//    setInheritanceAssembler(DefaultInheritanceAssembler());
//    setModelInterpolator(modelInterpolator);
//    setModelUrlNormalizer(DefaultModelUrlNormalizer().setUrlNormalizer(DefaultUrlNormalizer()));
//    setModelPathTranslator(DefaultModelPathTranslator().setPathTranslator(DefaultPathTranslator()));
//    setPluginManagementInjector(DefaultPluginManagementInjector());
//    setLifecycleBindingsInjector(DefaultLifecycleBindingsInjector());
//    setDependencyManagementInjector(DefaultDependencyManagementInjector());
//    setReportConfigurationExpander(DefaultReportConfigurationExpander());
//    setReportingConverter(DefaultReportingConverter());
//    setPluginConfigurationExpander(DefaultPluginConfigurationExpander());
//    setDependencyManagementImporter(DefaultDependencyManagementImporter());
//    setProfileInjector(DefaultProfileInjector());
//}
