package com.ullink.gradle.nunit

import org.gradle.api.internal.ConventionTask
import groovyx.gpars.GParsPool
import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import static org.apache.tools.ant.taskdefs.condition.Os.*

class Wrapper {
    def value;

    Wrapper() {
        value = ""
    }
}


class NUnit extends ConventionTask {
    def nunitHome
    def nunitVersion
    def nunitDownloadUrl
    List testAssemblies

    def framework
    def verbosity
    def config
    def timeout
    def labels

    def reportFolder
    boolean useX86 = false
    boolean shadowCopy = false
    String reportFileName = 'TestResult.xml'
    static final String DEFAULT_REPORT_FILE_NAME = 'TestResult.xml'
    def logFile
    boolean ignoreFailures = false
    boolean parallelForks = true

    def test = Wrapper.newInstance()
    def where = Wrapper.newInstance()
    def testList
    def nunitCommandModifier = { nunitBin, args ->
        [nunitBin, *args]
    }

    Map<String, Object> env = [:]

    NUnit() {
        conventionMapping.map "reportFolder", { new File(outputFolder, 'reports') }
        inputs.files {
            getTestAssemblies()
        }
        doFirst {
            // ensure NUnit is downloaded before performing TaskAction for parallel run
            ensureNunitInstalled()
        }
    }

    boolean getIsV3() {
        getIsV3(getNunitVersion())
    }

    static boolean getIsV3(def version) {
        version.startsWith('3.')
    }

    boolean getIsV35OrAbove() {
        def (major, minor, patch) = getNunitVersion().tokenize('.')*.toInteger()
        major == 3 && minor >= 5
    }

    boolean getIsV39() {
        def (major, minor, patch) = getNunitVersion().tokenize('.')*.toInteger()
        major == 3 && minor == 9
    }

    String getGitHubRepoName() {
        if (isV35OrAbove) {
            return 'nunit-console'
        }
        if (isV3) {
            return 'nunit'
        }
        return 'nunitv2'
    }

    void setNunitVersion(def version) {
        this.nunitVersion = version
        if (getIsV3(version)) {
            this.metaClass.mixin NUnit3Mixins
        }
        else {
            this.metaClass.mixin NUnit2Mixins
        }
    }

    void setTest(def input) {
        setTestInternal(test, where, input)
    }

    File nunitBinFile(String file) {
        def nunitFolder
        if (getNunitHome()) {
            nunitFolder = getNunitHome()
        } else {
            ensureNunitInstalled()
            nunitFolder = getCachedNunitDir()
        }
        new File(project.file(nunitFolder), "${isV35OrAbove ? '' : 'bin/'}${file}")
    }

    void ensureNunitInstalled() {
        if (getNunitHome()) {
            return;
        }

        def nunitCacheDir = getCacheDir()
        if (!nunitCacheDir.exists()) {
            nunitCacheDir.mkdirs()
        }
        def nunitFolder = getCachedNunitDir()
        if (!nunitFolder.exists()) {
            downloadNUnit()
        }
    }

    File getCachedNunitDir() {
        new File(getCacheDir(), getNunitName())
    }

    File getCacheDir() {
        new File(new File(project.gradle.gradleUserHomeDir, 'caches'), 'nunit')
    }

    String getNunitName() {
        if (isV35OrAbove) {
            return "NUnit.Console-${getNunitVersion()}"
        }
        "NUnit-${getNunitVersion()}"
    }

    String getFixedDownloadVersion() {
        String version = getNunitVersion()
        if (isV35OrAbove && version.endsWith('.0')) {
            version = version.take(version.length() - 2)
            if (isV39) {
                version = "v${version}"
            }
        }

        version
    }

    void downloadNUnit() {
        def NUnitZipFile = getNunitName() + '.zip'
        def downloadedFile = new File(getTemporaryDir(), NUnitZipFile)
        def nunitCacheDirForVersion = getCachedNunitDir()
        def version = getNunitVersion()
        def nunitDownloadUrl = "${getNunitDownloadUrl()}/${fixedDownloadVersion}/$NUnitZipFile"
        // special handling for nunit3 flat zip file
        def zipOutputDir = isV3 ? nunitCacheDirForVersion : getCacheDir();
        project.logger.info "Downloading & Unpacking NUnit ${version} from ${nunitDownloadUrl}"
        project.download {
            src "$nunitDownloadUrl"
            dest downloadedFile
        }
        project.copy {
            from project.zipTree(downloadedFile)
            into zipOutputDir
        }
    }

    File getOutputFolder() {
        new File(project.buildDir, 'nunit')
    }

    File getReportFolderImpl() {
        project.file(getReportFolder())
    }

    @OutputFile
    File getTestReportPath() {
        // for the non-default nunit tasks, ensure we write the report in a separate file
        if (reportFileName.equals(DEFAULT_REPORT_FILE_NAME)) {
            def reportFileNamePrefix = name == 'nunit' ? '' : name
            new File(getReportFolderImpl(), reportFileNamePrefix + reportFileName)
        } else {
            new File(getReportFolderImpl(), reportFileName)
        }
    }

    File getTestLogFile() {
        project.file(getLogFile())
    }

    @TaskAction
    def build() {
        decideExecutionPath(this.&singleRunExecute, this.&multipleRunsExecute)
    }

    def decideExecutionPath(Closure singleRunAction, Closure multipleRunsAction) {
        def input = getRunActionInput();

        if (!parallelForks || !input) {
            return singleRunAction(input)
        } else {
            return multipleRunsAction(input)
        }
    }

    def singleRunExecute(def input) {
        def runs = getTestInputsAsString(input)
        run(runs, getTestReportPath())
    }

    def multipleRunsExecute(def input) {
        def intermediateReportsPath = new File(getReportFolderImpl(), "intermediate-results-" + name)
        intermediateReportsPath.mkdirs()

        def runs = getTestInputAsList(input)
        GParsPool.withPool {
            runs.eachParallel {
                def fileName = toFileName(it)
                logger.info("Filename generated for the \'$it\' input was \'$fileName\'")
                run(it, new File(intermediateReportsPath, fileName + ".xml"))
            }
        }

        def files =  intermediateReportsPath.listFiles().toList()
        def outputFile = getTestReportPath()
        logger.info("Merging test reports $files into $outputFile ...")
        new NUnitTestResultsMerger().merge(files, outputFile)
    }

    // Used by gradle-opencover-plugin
    def getCommandArgs() {
        def testRuns = getTestInputsAsString(getRunActionInput())
        buildCommandArgs (testRuns, getTestReportPath())
    }

    List<String> getTestInputAsList(def testInput) {
        if (!testInput) {
            return []
        }

        if (testInput instanceof List) {
            return testInput
        }

        // Behave like NUnit
        if (isACommaSeparatedList(testInput)) {
            return testInput.tokenize(',')
        }

        return [testInput]
    }

    Boolean isACommaSeparatedList(def input) {
        return input != null && input.contains(',');
    }

    String getTestInputsAsString(def testInput) {
        if (!testInput) {
            return ''
        }

        if (testInput instanceof String) {
            return testInput
        }

        return combine(testInput)
    }

    def getCommandLine(input, reportPath){
        return nunitCommandModifier(getNunitExec().absolutePath, buildCommandArgs(input, reportPath))
    }

    def run(def input, def reportPath) {
        def cmdLine = getCommandLine(input, reportPath)
        if (!isFamily(FAMILY_WINDOWS)) {
            cmdLine = ["mono", *cmdLine]
        }
        execute(cmdLine)
    }

    // Return values of nunit v2 and v3 are defined in
    // https://github.com/nunit/nunitv2/blob/master/src/ConsoleRunner/nunit-console/ConsoleUi.cs and
    // https://github.com/nunit/nunit/blob/master/src/NUnitConsole/nunit-console/ConsoleRunner.cs
    def execute(commandLineExec) {
        prepareExecute()

        def mbr = project.exec {
            if (env)
                environment env
            commandLine = commandLineExec
            ignoreExitValue = ignoreFailures
        }

        int exitValue = mbr.exitValue
        if (exitValue == 0) {
            return
        }

        boolean anyTestFailing = exitValue > 0
        if (anyTestFailing && ignoreFailures) {
            return
        }

        throw new GradleException("${getNunitExec()} execution failed (ret=${mbr.exitValue})");
    }

    def prepareExecute() {
        getReportFolderImpl().mkdirs()
        if (logFile)
            getTestLogFile().getParentFile().mkdirs()
    }

    def buildCommandArgs(def testInput, def testReportPath) {
        def commandLineArgs = []

        String verb = verbosity
        if (!verb) {
            if (logger.debugEnabled) {
                verb = 'Verbose'
            } else if (logger.infoEnabled) {
                verb = 'Info'
            } else {
                // 'quiet'
                verb = 'Warning'
            }
        }
        if (verb) {
            commandLineArgs += "-trace=$verb"
        }
        if (framework) {
            commandLineArgs += "-framework:$framework"
        }
        if (config) {
            commandLineArgs += "-config:$config"
        }
        if (labels) {
            commandLineArgs += "-labels:$labels"
        }
        if (timeout) {
            commandLineArgs += "-timeout:$timeout"
        }
        if (logFile) {
            commandLineArgs += "-output:${getTestLogFile().getPath()}"
        }
        commandLineArgs += "-work:$outputFolder"

        commandLineArgs += buildAdditionalCommandArgs(testInput, testReportPath)

        getTestAssemblies().each {
            def file = project.file(it)
            if (file.exists())
                commandLineArgs += file
            else
                commandLineArgs += it
        }

        commandLineArgs
    }
}
