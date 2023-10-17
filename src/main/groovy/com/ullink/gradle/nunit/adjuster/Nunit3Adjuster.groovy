package com.ullink.gradle.nunit.adjuster

import groovy.xml.XmlUtil
import org.slf4j.Logger

class Nunit3Adjuster {

    static void UpdateReportFileForNunit3(File testResultsFile, Logger logger) {
        logger.info("Checking if adjustments on the test results for Nunit 3 are needed.")

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
        def xmlNodeChildren = xmlFile.find { it.name() == 'test-suite' }.children()
        xmlNodeChildren.add(xmlNodeChildren.size(), failingTestToAppend)
    }

    private static boolean isNumberOfFailedTestsConsistentWithOverallResult(Node xmlFile) {
        if (isOverallResultFailed(xmlFile)) {
            def numberOfFailedTests =  xmlFile.attributes().find { it.key == "failed" }.value
            if (numberOfFailedTests == "0")
                return false
            return true
        }
        return true
    }

    private static boolean isOverallResultFailed(Node xmlFile) {
        return  xmlFile.attributes().find { it.key == "result" }.value == "Failed"
    }

    private static String getFailingTestCase() {
        return '''
 <test-suite type="TestSuite" id="9999-9996" name="Ullink" fullname="Ullink.Test" runstate="Runnable" testcasecount="1" result="Failed" site="Child" start-time="2019-05-06 07:10:49Z" end-time="2019-05-06 07:10:49Z" duration="0.100747" total="1" passed="0" failed="1" warnings="0" inconclusive="0" skipped="0" asserts="1">
      <test-suite type="TestSuite" id="9999-9997" name="Test" fullname="Ullink.Test" runstate="Runnable" testcasecount="1" result="Failed" site="Child" start-time="2019-05-06 07:10:49Z" end-time="2019-05-06 07:10:49Z" duration="0.100737" total="1" passed="0" failed="1" warnings="0" inconclusive="0" skipped="0" asserts="1">
        <test-suite type="TestFixture" id="9999-9998" name="FailingFixture" fullname="Ullink.Test.FailingFixture" classname="Ullink.Test.FailingFixture" runstate="Runnable" testcasecount="1" result="Failed" site="Child" start-time="2019-05-06 07:10:49Z" end-time="2019-05-06 07:10:49Z" duration="0.067676" total="1" passed="0" failed="1" warnings="0" inconclusive="0" skipped="0" asserts="1">
          <test-case id="9999-9999" name="FailingTest" fullname="Ullink.Test.FailingFixture.FailingTest" methodname="FailingTest" classname="Ullink.Test.FailingFixture" runstate="Runnable" seed="420273222" result="Failed" start-time="2019-05-06 07:10:49Z" end-time="2019-05-06 07:10:49Z" duration="0.067506" asserts="1">
            <failure>
              <message> Appended failing test</message>
            </failure>
          </test-case>
        </test-suite>
      </test-suite>
    </test-suite>
     '''
    }
}
