package com.ullink
import com.ullink.gradle.nunit.NUnitTestResultsMerger
import org.custommonkey.xmlunit.DetailedDiff
import org.custommonkey.xmlunit.XMLUnit
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier
import org.junit.Assert
import org.junit.Test

class NUnitTestResultsMergerTest {
    @Test
    public void givenMultipleTestResultFilesForSameAssemblyWithoutTakingCultureFromFile_merge_mergesIntoExpectedOutputResult(){
        givenMultipleTestResultFilesForSameAssembly_merge_mergesIntoExpectedOutputResult(false);
    }

    @Test
    public void givenMultipleTestResultFilesForSameAssemblyTakingCultureFromFile_merge_mergesIntoExpectedOutputResult(){
        givenMultipleTestResultFilesForSameAssembly_merge_mergesIntoExpectedOutputResult(true);
    }

    public void givenMultipleTestResultFilesForSameAssembly_merge_mergesIntoExpectedOutputResult(boolean takeCultureFromFile)
    {
        def testResults = [
                'TestResult_integrationTest_ILRepack.IntegrationTests.NuGet',
                'TestResult_integrationTest_ILRepack.IntegrationTests.WPF',
                'TestResult_unitTests'
                ].collect { getTestResult(it) }

        String testResult = new NUnitTestResultsMerger().merge(testResults, takeCultureFromFile)

        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreAttributeOrder(true)
        def diff = XMLUnit.compareXML(
                getTestResult('TestResults_merged'),
                testResult)
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        Assert.assertEquals('', new DetailedDiff(diff).allDifferences.join('\n'));
    }

    private String getTestResult(String fileName) {
        getClass().getResource("/sample-testresults/${fileName}.xml")
                .text
                // remove stuff like ?
                .replaceAll("[^\\x20-\\x7e\\x0A]", "")
    }

    @Test
    public void givenAReportGeneratedWithFrenchLocalization_merge_timeShouldBeParsedProperly()
    {
        String testResult = new NUnitTestResultsMerger().merge([getTestResult('TestResult_withFrenchCultureInfo')], true)
        XMLUnit.setIgnoreComments(true)
        XMLUnit.setIgnoreWhitespace(true)
        XMLUnit.setIgnoreAttributeOrder(true)
        def diff = XMLUnit.compareXML(
                getTestResult('TestResult_withFrenchCultureInfo_merged'),
                testResult)
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
        String result = new DetailedDiff(diff).allDifferences.join('\n')
        Assert.assertEquals('', result);
    }
}
