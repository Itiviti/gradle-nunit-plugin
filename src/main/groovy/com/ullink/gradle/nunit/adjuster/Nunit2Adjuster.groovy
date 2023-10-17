package com.ullink.gradle.nunit.adjuster

import groovy.xml.XmlUtil
import org.slf4j.Logger

class Nunit2Adjuster {

    static void UpdateReportFileForNunit2(File testResultsFile, Logger logger) {
        logger.info("Checking if adjustments on the test results for Nunit 2 are needed.")

        def xmlFile = new XmlParser().parse(testResultsFile)

        if (!isNumberOfFailedTestsConsistentWithOverallResult(xmlFile)) {
            logger.info("The overall result of the Test Results is not consistent with the number of failed tests. Adjusting test results...")

            AppendFailingTestCase(xmlFile)

            testResultsFile.write(XmlUtil.serialize(xmlFile))
        } else {
            logger.info("The overall result of the Test Results is consistent with the number of failed tests so no adjusting needed.")
        }
    }

    private static void AppendFailingTestCase(Node xmlFile) {
        def failingTestToAppend = new XmlParser(false, true).parseText(getFailingTestCase())

        def resultsXmlNode = xmlFile.children()
                                    .find { it.name() == "test-suite" }
                                    .find { it.name() == 'results' }
                                    .children()

        resultsXmlNode.add(resultsXmlNode.size(), failingTestToAppend)
    }

    private static boolean isNumberOfFailedTestsConsistentWithOverallResult(Node xmlFile) {
        if (isOverallResultFailed(xmlFile)) {
            def numberOfFailedTests =  xmlFile.attributes().find { it.key == "failures" }.value
            if (numberOfFailedTests == "0")
                return false
            return true
        }
        return true
    }

    private static boolean isOverallResultFailed(Node xmlFile){
        boolean isFailedResult = false

        def rootNodeForTests = xmlFile.children().find {it.name() == "test-suite"}
        def assemblyType = rootNodeForTests.attributes().find {it.key=="type"}
        if (assemblyType.value == "Assembly")
        {
            def resultForAssembly = rootNodeForTests.attributes().find {it.key == "result"}
            if (resultForAssembly.value == "Failure") {
                isFailedResult = true
            }
        }
        return isFailedResult
    }

    private static String getFailingTestCase()
    {
        return  '''
  <test-suite type="TestSuite" name="FailingTest" executed="True" result="Failure" success="False" time="0.091" asserts="2">
                <failure>
                  <message><![CDATA[One or more child tests had errors]]></message>
                  <stack-trace />
                </failure>
                <results>
                  <test-case name="FailingTestDueToFailingTestResult" executed="True" result="Failure" success="False" time="0.077" asserts="1">
                    <failure>
                      <message><![CDATA[One or more child tests had errors]]></message>
                    </failure>
                  </test-case>
                </results>
              </test-suite>
              '''
    }
}
