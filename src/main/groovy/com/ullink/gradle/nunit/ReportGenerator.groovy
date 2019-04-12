package com.ullink.gradle.nunit

import org.gradle.api.tasks.Exec

class ReportGenerator extends Exec {

    def ExtentReportType = 'v3html'
    def ExtentReportDownloadUrl = 'https://github.com/extent-framework/extentreports-dotnet-cli/archive'
    def ExtentReportZipName = 'master.zip'
    def ExtentReportExecutablePath = 'extentreports-dotnet-cli-master/dist/extent.exe'

    @Override
    protected void exec() {
        if (isTestResultFileAvailable()) {
            project.logger.info("Generating the report for the Test Results..")

            ensureExtentReportIsAvailable()
            generateReports()
        } else {
            project.logger.info("There is no available Test Result file based on which the report should be generated!")
        }
    }

    boolean isTestResultFileAvailable() {
        NUnit nunit = project.tasks.nunit

        File testResultPath = nunit.getTestReportPath()
        if (!testResultPath.exists()) {
            return false
        }
        return true
    }

    void ensureExtentReportIsAvailable() {
        if (!isExtentReportAvailable()) {
            project.logger.info "Downloading extent report..."

            getCacheDirForExtentReport().mkdirs()
            downloadExtentReport()
        }
    }

    boolean isExtentReportAvailable() {
        def extentReportCacheDir = getCacheDirForExtentReport()
        if (!extentReportCacheDir.exists()) {
            return false
        }

        def extentReportExecutable = new File(extentReportCacheDir, ExtentReportExecutablePath)
        if (!extentReportExecutable.exists()) {
            return false
        }

        return true
    }

    File getCacheDirForExtentReport() {
        new File(new File(project.gradle.gradleUserHomeDir, 'caches'), 'extent-report')
    }

    private def generateReports() {
        NUnit nunit = project.tasks.nunit

        project.exec {
            commandLine = buildCommandForExtentReport(nunit.getTestReportPath(), nunit.getReportFolder())
        }

        def resultFile = new File(nunit.getReportFolder(), "index.html")
        resultFile.renameTo new File(nunit.getReportFolder(), 'TestResult.html')
    }

    def buildCommandForExtentReport(def testResultPath, def outputFolder) {
        def commandLineArgs = []

        commandLineArgs += getExtentReportExeFile().absolutePath

        commandLineArgs += "-i"
        commandLineArgs += testResultPath

        commandLineArgs += "-o"
        commandLineArgs += outputFolder

        commandLineArgs += "-r"
        commandLineArgs += ExtentReportType

        commandLineArgs
    }

    File getExtentReportExeFile() {
        def extentReportFolder = getCacheDirForExtentReport()
        return new File(extentReportFolder, ExtentReportExecutablePath)
    }

    void downloadExtentReport() {
        def downloadedFile = new File(getTemporaryDir(), ExtentReportZipName)
        def extentReportDownloadUrl = getExtentReportDownloadUrl()
        def zipOutputDir = getCacheDirForExtentReport()

        project.logger.info "Downloading & Unpacking Extent Report from ${extentReportDownloadUrl}"

        project.download {
            src "$extentReportDownloadUrl"
            dest downloadedFile
        }

        project.copy {
            from project.zipTree(downloadedFile)
            into zipOutputDir
        }
    }

    String getExtentReportDownloadUrl() {
        // As there is no current release for extent-report we integrate the master version
        return "${ExtentReportDownloadUrl}/$ExtentReportZipName"
    }
}
