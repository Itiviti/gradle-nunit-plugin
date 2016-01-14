package com.ullink.gradle.nunit

import org.bouncycastle.math.raw.Nat
import org.gradle.api.GradleException
import org.gradle.api.internal.ConventionTask
import groovyx.gpars.GParsPool
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

import static org.apache.tools.ant.taskdefs.condition.Os.*

class NUnit extends ConventionTask {
    final String testResultPlaceholder = "<<TEST_RESULT>>"

    def nunitHome
    def nunitVersion
    def nunitDownloadUrl
    List testAssemblies
    def include
    def exclude
    def where
    def framework
    def verbosity
    def config
    def timeout
    def runList
    def run
    def testList
    def test
    def reportFolder
    boolean useX86 = false
    boolean shadowCopy = false
    def reportFileName  = "TestResult_${testResultPlaceholder}.xml"
    boolean ignoreFailures = false
    boolean parallel_forks = true

    NUnit() {
        conventionMapping.map "reportFolder", { new File(outputFolder, 'reports') }
        inputs.files {
            getTestAssemblies()
        }
    }

    File nunitBinFile(String file) {
        new File(project.file(getNunitHome()), "bin/${file}")
    }

    boolean getIsV3() {
        getNunitVersion().startsWith("3.")
    }

    File getNunitExec() {
        assert getNunitHome(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        File nunitExec = isV3
            ? nunitBinFile('nunit3-console.exe')
            : nunitBinFile("nunit-console${useX86 ? '-x86' : ''}.exe")
        assert nunitExec.isFile(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        return nunitExec
    }

    File getOutputFolder() {
        new File(project.buildDir, 'nunit')
    }

    File getReportFolderImpl() {
        project.file(getReportFolder())
    }

    File getTestReportPath() {
        new File(getReportFolderImpl(), reportFileName)
    }

    File getTestReportPath(def test) {
        new File(getReportFolderImpl(), reportFileName.replace(testResultPlaceholder, test))
    }

    @TaskAction
    def build() {
        decideExecutionPath(this.&singleRunExecute, this.&multipleRunsExecute)
    }

    @OutputFiles
    def getOutputFiles(){
        return decideExecutionPath(this.&singleRunGetOutput, this.&multipleRunsGetOutput)
    }

    def decideExecutionPath(singleRunAction, multipleRunsAction){
        if (!test && run) {
            test = run
        }
        if (!parallel_forks || !test) {
            return singleRunAction(test)
        }
        else {
            return multipleRunsAction(test)
        }
    }

    def singleRunExecute(test)
    {
        def testRuns = getTestInputsAsString(test)
        testRun(testRuns, getTestReportPath())
    }

    def multipleRunsExecute(test)
    {
        def testRuns = getTestInputAsList(test)
        GParsPool.withPool {
            testRuns.eachParallel { t -> testRun(t, getTestReportPath(t)) }
        }
    }

    def singleRunGetOutput(test)
    {
        return [getTestReportPath()]
    }

    def multipleRunsGetOutput(test)
    {
        def testRuns = getTestInputAsList(test)
        def out = []
        testRuns.each { t -> out.add(getTestReportPath(t))}
        return out
    }

    List getTestInputAsList(testInput)
    {
        if (!testInput){
            return []
        }

        if (testInput instanceof List) {
            return testInput
        }

        if (testInput.contains(',')) {
            return testInput.tokenize(',')
        }

        return [testInput]
    }

    String getTestInputsAsString(testInput)
    {
        if (!testInput){
            return ''
        }

        if (testInput instanceof String) {
            return testInput
        }

        return testInput.join(',')
    }

    def testRun(def test, def reportPath) {
        def cmdLine = [nunitExec.absolutePath, *buildCommandArgs(test, reportPath)]
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

        throw new GradleException("${nunitExec} execution failed (ret=${mbr.exitValue})");
    }

    def prepareExecute() {
        getReportFolderImpl().mkdirs()
    }

    def buildCommandArgs(def test, def testReportPath) {
        def commandLineArgs = []

        String verb = verbosity
        if (!verb) {
            if (logger.debugEnabled) {
                verb = 'Verbose'
            } else if (logger.infoEnabled) {
                verb = 'Info'
            } else { // 'quiet'
                verb = 'Warning'
            }
        }
        if (verb) {
            commandLineArgs += "-trace=$verb"
        }
        if (isV3) {
            if (useX86) {
                commandLineArgs += '-x86'
            }
        }
        if (isV3) {
            if (include || exclude) {
                throw new GradleException("'include'/'exclude' options aren't supported on NUnit v3, use 'where' option instead")
            }
            if (where) {
                commandLineArgs += "-where:$where"
            }
        } else {
            if (where) {
                throw new GradleException("'where' isn't supported on NUnit v2, you need to set the NUnit version to v3+")
            }
            if (exclude) {
                commandLineArgs += "-exclude:$exclude"
            }
            if (include) {
                commandLineArgs += "-include:$include"
            }
        }
        if (framework) {
            commandLineArgs += "-framework:$framework"
        }
        if (isV3) {
            if (shadowCopy) {
                commandLineArgs += '-shadowcopy'
            }
        } else {
            if (!shadowCopy) {
                commandLineArgs += '-noshadow'
            }
        }
        // Maintain backward compatibility with old (nunit 2.x) gradle files.
        if (!testList && runList) {
            testList = runList
        }

        if (testList) {
            if (isV3) {
                commandLineArgs += "-testlist:$testList"
            } else {
                commandLineArgs += "-runList:$testList"
            }
        }
        if (test) {
            if (isV3) {
                commandLineArgs += "-test:$test"
            } else {
                commandLineArgs += "-run:$test"
            }
        }
        if (config){
            commandLineArgs += "-config:$config"
        }
        if (timeout){
            commandLineArgs += "-timeout:$timeout"
        }
        if (isV3) {
            commandLineArgs += "-result:$testReportPath"
        } else {
            commandLineArgs += "-xml:$testReportPath"
        }
        getTestAssemblies().each {
            def file = project.file(it)
            if (file.exists() )
                commandLineArgs += file
            else
                commandLineArgs += it
        }
        commandLineArgs
    }
}
