package com.ullink

import com.ullink.gradle.nunit.NUnit
import com.ullink.gradle.nunit.ReportGenerator
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.process.internal.ExecException
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NUnitPluginTest extends Specification {
    private Project project

    def setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'nunit'
    }

    def "nunit plugin adds nunit task to project"() {
        expect:
            project.tasks.nunit instanceof NUnit
    }

    def "nunit plugin adds nunitReport task to project"() {
        expect:
        project.tasks.nunitReport instanceof ReportGenerator
    }

    def "nunit task throws exception when no project is specified"() {
        when:
            project.tasks.nunit
            {
                nunitVersion = '2.6.4'
            }.build()
        then:
            GradleException exception = thrown()
            exception.message.isEmpty() == false
    }

    def "setting report folder works"() {
        when:
            project.nunit {
                testAssemblies = ['TestA.dll']
                reportFolder = './foo'
            }
        then:
            'foo' == project.nunit.testReportPath.parentFile.name
    }
}
