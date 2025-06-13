package com.ullink

import com.ullink.gradle.nunit.NUnit
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NUnitPluginTest extends Specification {
    private Project project

    def setup() {
        DownloadCacheCleaner.clear()
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'nunit'
    }

    def "nunit plugin adds nunit task to project"() {
        expect:
            project.tasks.nunit instanceof NUnit
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
