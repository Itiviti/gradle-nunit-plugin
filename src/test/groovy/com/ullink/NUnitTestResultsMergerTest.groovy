package com.ullink

import com.ullink.gradle.nunit.NUnitTestResultsMerger
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class NUnitTestResultsMergerTest extends Specification {

    def "multiple test result files for same assembly are merged into expected output"() {
        given:
            def testResultFiles = [
                    'TestResult_integrationTest_ILRepack.IntegrationTests.NuGet',
                    'TestResult_integrationTest_ILRepack.IntegrationTests.WPF',
                    'TestResult_unitTests'
            ].collect { getTestResultFile(it) }
            def file = File.createTempFile('nunit-plugin-test-results', '.xml')
            file.deleteOnExit()
        when:
            new NUnitTestResultsMerger().merge(testResultFiles, file)
            XMLUnit.setIgnoreComments(true)
            XMLUnit.setIgnoreWhitespace(true)
            XMLUnit.setIgnoreAttributeOrder(true)
            def diff = XMLUnit.compareXML(
                    getContent(getTestResultFile('TestResults_merged')),
                    getContent(file))
            diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier())
        then:
            '' == new DetailedDiff(diff).allDifferences.join('\n')
            AssertContentIsLoadable(file)
    }

    def "test result with accent and other kind of characters merges correctly"() {
        given:
            def file = File.createTempFile('nunit-plugin-test-results', '.xml')
            file.deleteOnExit()
        when:
            new NUnitTestResultsMerger().merge([getTestResultFile('TestResult_withAccents')], file)
            XMLUnit.setIgnoreComments(true)
            XMLUnit.setIgnoreWhitespace(true)
            XMLUnit.setIgnoreAttributeOrder(true)
            def diff = XMLUnit.compareXML(
                    getContent(getTestResultFile('TestResult_withAccents_merged')),
                    getContent(file))
            diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        then:
            '' == new DetailedDiff(diff).allDifferences.join('\n')
            AssertContentIsLoadable(file)
    }

    def "test result with ignored tests is merged correctly"() {
        given:
            def testResultFiles = [
                    'TestResult_ignored'
            ].collect { getTestResultFile(it) }
            def file = File.createTempFile('nunit-plugin-test-results', '.xml')
        when:
            new NUnitTestResultsMerger().merge(testResultFiles, file)
            XMLUnit.setIgnoreComments(true)
            XMLUnit.setIgnoreWhitespace(true)
            XMLUnit.setIgnoreAttributeOrder(true)
            def diff = XMLUnit.compareXML(
                    getContent(getTestResultFile('TestResult_ignored_merged')),
                    getContent(file))
            diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier())
        then:
            '' == new DetailedDiff(diff).allDifferences.join('\n')
            AssertContentIsLoadable(file)
    }

    def "nunit3 test results"() {
        given:
        def testResultFiles = [
                'nunit3_1',
                'nunit3_2'
        ].collect { getTestResultFile(it) }
        def file = File.createTempFile('nunit-plugin-test-results', '.xml')
        file.deleteOnExit()

        when:
        new NUnitTestResultsMerger().merge(testResultFiles, file)
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreAttributeOrder(true)
        def diff = XMLUnit.compareXML(
                getContent(getTestResultFile('nunit3_merged')),
                getContent(file))
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier())

        then:
        '' == new DetailedDiff(diff).allDifferences.join('\n')
        AssertContentIsLoadable(file)
    }

    void AssertContentIsLoadable(File file) {
        null != new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(file)
    }

    private File getTestResultFile(String fileName) {
        new File(getClass().getResource("/sample-testresults/${fileName}.xml").toURI())
    }

    private String getContent(File file) {
        Files.readAllLines(Paths.get(file.toURI()), StandardCharsets.UTF_8).join('\n')
    }
}
