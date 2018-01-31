package com.redhat.mqe.maven

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
        return if (poms.isEmpty()) {
            listOf(artifactId())
        } else {
            poms.flatMap { it.leafArtifactIds(profile) }.sorted()
        }
    }
}

fun <R> NodeList.map(transform: (Node) -> R): List<R> {
    val list = ArrayList<R>(length)
    for (i in 0 until length) {
        list.add(transform(item(i)))
    }
    return list
}
