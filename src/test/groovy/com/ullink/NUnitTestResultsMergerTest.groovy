package com.ullink
import com.ullink.gradle.nunit.NUnitTestResultsMerger
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier
import org.junit.Assert
import org.junit.Test

class NUnitTestResultsMergerTest {
    @Test
    public void givenMultipleTestResultFilesForSameAssembly_merge_mergesIntoExpectedOutputResult()
    {
        def testResults = [
                'TestResult_integrationTest_ILRepack.IntegrationTests.NuGet',
                'TestResult_integrationTest_ILRepack.IntegrationTests.WPF',
                'TestResult_unitTests'
                ].collect { getTestResult(it) }

        String testResult = new NUnitTestResultsMerger().merge(testResults)

        System.err.println(testResult)

        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreAttributeOrder(true)
        def diff = XMLUnit.compareXML(
                getTestResult('TestResults_merged'),
                testResult)
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        Assert.assertEquals(String.join('\n', new DetailedDiff(diff).allDifferences.collect {it.toString()}), "");
    }

    private String getTestResult(String fileName) {
        getClass().getResource("/sample-testresults/${fileName}.xml").text
    }
}
