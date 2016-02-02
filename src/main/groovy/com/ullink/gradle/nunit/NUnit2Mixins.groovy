package com.ullink.gradle.nunit

class NUnit2Mixins {

    def run
    def runList
    def include
    def exclude

    def useX86
    def shadowCopy

    File getNunitExec() {
        File nunitExec = this.nunitBinFile("nunit-console${useX86 ? '-x86' : ''}.exe")
        assert nunitExec.isFile(), "You must install NUnit and set nunit.home property or NUNIT_HOME env variable"
        return nunitExec
    }

    def getTest() {
        run
    }

    def buildAdditionalCommandArgs(def test, def testReportPath) {
        def commandLineArgs = []

        if (exclude) {
            commandLineArgs += "-exclude:$exclude"
        }
        if (include) {
            commandLineArgs += "-include:$include"
        }
        if (!shadowCopy) {
            commandLineArgs += '-noshadow'
        }
        if (runList) {
            commandLineArgs += "-runList:$runList"
        }
        if (run) {
            commandLineArgs += "-run:$run"
        }
        commandLineArgs += "-xml:$testReportPath"

        commandLineArgs
    }
}
