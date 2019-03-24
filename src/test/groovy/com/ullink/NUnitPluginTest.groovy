package com.ullink

import com.ullink.gradle.nunit.NUnit
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

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

    def "tasks clean and nunit work"() {
        expect:
            project.apply plugin: 'base'
            project.nunit {
                testAssemblies = ['-help']
            }
            project.tasks.clean.execute()
            project.tasks.nunit.execute()
    }

    def "tasks nunit works"() {
        expect:
            project.nunit {
                testAssemblies = ['-help']
            }

            project.tasks.nunit.execute()
    }

    def "nunit task throws exception when no project is specified"() {
        when:
            project.tasks.nunit
            {
                nunitVersion = '2.6.4'
            }.execute()
        then:
            GradleException exception = thrown()
            exception.message == "Execution failed for task ':nunit'."
    }

    @Unroll
    def "execute help works for nunit #version"() {
        expect:
            project.nunit {
                testAssemblies = ['-help']
                nunitVersion = version
            }

            project.tasks.nunit.execute()
        where:
            version << ['3.0.0', '3.5.0', '3.6.0', '3.6.1']
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
