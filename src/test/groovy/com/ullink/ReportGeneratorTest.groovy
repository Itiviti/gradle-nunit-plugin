package com.ullink

import java.nio.file.Paths
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ReportGeneratorTest extends Specification {

    private static File GradleUserHomeDir = new File("path", "GradleHomeDirForTests")

    def "generating reports with Extent Report has the correct build arguments"() {
        given:
            def nunitReportGenerationTask = getReportGenerationTask()
            def testResultFile = new File("TestResult.xml")
            def reportOutputFolder = new File("reports/results")
        when:
            def extentReportCommandArgument = nunitReportGenerationTask.buildCommandForExtentReport(testResultFile, reportOutputFolder)
        then:
            extentReportCommandArgument == [nunitReportGenerationTask.getExtentReportExeFile().absolutePath, '-i', testResultFile, '-o', reportOutputFolder, '-r', 'v3html']
    }

    def "extent report executable has the correct path"() {
        given:
            def nunitReportGenerationTask = getReportGenerationTask()
        when:
            def extentReportExecutable = nunitReportGenerationTask.getExtentReportExeFile()
        then:
            def pathToTheExecutable = Paths.get("path", "GradleHomeDirForTests", "caches", "extent-report", "extentreports-dotnet-cli-master", "dist", "extent.exe")
            extentReportExecutable.absolutePath
                    .contains(pathToTheExecutable.normalize().toString())
    }

    def "extent report download URL is build correctly"() {
        given:
            def nunitReportGenerationTask = getReportGenerationTask()
        when:
            def extentReportDownloadUrl = nunitReportGenerationTask.getExtentReportDownloadUrl()
        then:
            extentReportDownloadUrl == "https://github.com/extent-framework/extentreports-dotnet-cli/archive/master.zip"
    }


    def getReportGenerationTask() {
        def project = ProjectBuilder.builder().withGradleUserHomeDir(GradleUserHomeDir).build()
        project.apply plugin: 'nunit'
        return project.tasks.nunitReport
    }
}
