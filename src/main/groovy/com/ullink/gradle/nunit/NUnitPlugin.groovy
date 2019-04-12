package com.ullink.gradle.nunit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class NUnitPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'nunit-base'

        Task defaultNUnitTask = project.task('nunit', type: NUnit)

        Task reportingTask = project.task('nunitReport', type: ReportGenerator)
        defaultNUnitTask.finalizedBy(reportingTask)

        defaultNUnitTask.description = 'Executes NUnit tests'
    }
}
