package com.ullink.gradle.nunit

class NUnit2Mixins {

    def run
    def runList
    def include
    def exclude

    void setRun(def run) {
        this.run = run
        this.setTest(run)
    }

    void setRunList(def runList) {
        this.runList = runList
        this.setTestList(runList)
    }

    File getNunitExec() {
        File nunitExec = this.nunitBinFile("nunit-console${this.useX86 ? '-x86' : ''}.exe")
        assert nunitExec.isFile(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        return nunitExec
    }

    def buildAdditionalCommandArgs(def testNames, def testReportPath) {
        def commandLineArgs = []

        if (exclude) {
            commandLineArgs += "-exclude:$exclude"
        }
        if (include) {
            commandLineArgs += "-include:$include"
        }
        if (!this.shadowCopy) {
            commandLineArgs += '-noshadow'
        }
        if (runList) {
            commandLineArgs += "-runList:$runList"
        }
        if (testNames) {
            commandLineArgs += "-run:$testNames"
        }
        commandLineArgs += "-xml:$testReportPath"

        commandLineArgs
    }
}
