package com.ullink.gradle.nunit.adjuster

import com.ullink.gradle.nunit.NUnit
import org.gradle.api.tasks.Exec

class NunitTestResultAdjuster extends Exec {

    @Override
    protected void exec() {
        NUnit nunitTask = project.tasks.nunit
        File testReportPath = nunitTask.getTestReportPath()

        if (!nunitTask.adjustTestResults) {
            project.logger.info("No adjustments on the generated tests results will be made.")
            return
        }

        if (isTestResultFormatInNunit3(nunitTask)) {
            Nunit3Adjuster.UpdateReportFileForNunit3(testReportPath, project.logger)
        } else {
            Nunit2Adjuster.UpdateReportFileForNunit2(testReportPath, project.logger)
        }
    }

    private boolean isTestResultFormatInNunit3(NUnit nUnit) {
        def nunitResultFormat = nUnit.resultFormat
        return nunitResultFormat == null || nunitResultFormat != 'nunit2'
    }
}
