package com.ullink

import com.ullink.gradle.nunit.NUnitTestResultsMerger
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier
import org.junit.Assert
import org.junit.Test

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

class NUnitTestResultsMergerTest {
    @Test
    public void givenMultipleTestResultFilesForSameAssembly_merge_mergesIntoExpectedOutputResult()
    {
        def testResultFiles = [
                'TestResult_integrationTest_ILRepack.IntegrationTests.NuGet',
                'TestResult_integrationTest_ILRepack.IntegrationTests.WPF',
                'TestResult_unitTests'
                ].collect { getTestResultFile(it) }
        File.createTempFile('nunit-plugin-test-results', '.xml').with {
            new NUnitTestResultsMerger().merge(testResultFiles, it)
            XMLUnit.setIgnoreComments(true)
            XMLUnit.setIgnoreWhitespace(true)
            XMLUnit.setIgnoreAttributeOrder(true)
            def diff = XMLUnit.compareXML(
                    getContent(getTestResultFile('TestResults_merged')),
                    getContent(it))
            diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
            Assert.assertEquals('', new DetailedDiff(diff).allDifferences.join('\n'));
            AssertContentIsLoadable(it)
        }
    }

    void AssertContentIsLoadable(File file) {
        Assert.assertNotNull(new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(file))
    }

    private File getTestResultFile(String fileName) {
        new File(getClass().getResource("/sample-testresults/${fileName}.xml").toURI())
    }

    private String getContent(File file) {
        Files.readAllLines(Paths.get(file.toURI()), StandardCharsets.UTF_8).join('\n')
    }

    @Test
    public void givenATestResultWithAccentAndOtherKindOfCharacters_merge_mergesIntoExpectedOutputResult() {
        File.createTempFile('nunit-plugin-test-results', '.xml').with {
            new NUnitTestResultsMerger().merge([getTestResultFile('TestResult_withAccents')], it)
            XMLUnit.setIgnoreComments(true)
            XMLUnit.setIgnoreWhitespace(true)
            XMLUnit.setIgnoreAttributeOrder(true)
            def diff = XMLUnit.compareXML(
                    getContent(getTestResultFile('TestResult_withAccents_merged')),
                    getContent(it))
            diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
            Assert.assertEquals('', new DetailedDiff(diff).allDifferences.join('\n'));
            AssertContentIsLoadable(it)
        }
    }
}
