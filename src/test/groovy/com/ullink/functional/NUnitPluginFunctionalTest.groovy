package com.ullink.functional

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

class NUnitPluginFunctionalTest extends Specification {
    @Rule TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """
            plugins {
                id 'base'
                id 'nunit'
            }
        """
    }

    def "nunit task successfully delegates -help to nunit-console-runner"() {
        given: "-help command and the default nunit version"
            buildFile << """
                nunit {
                    testAssemblies = ['-help']
                }
            """
        when: "clean and nunit tasks are executed"
            def result = GradleRunner.create()
                    .withProjectDir(testProjectDir.root)
                    .withArguments('clean', 'nunit')
                    .withPluginClasspath()
                    .withDebug(true)
                    .build()
        then: "help command was written for the default nunit version"
            result.output.contains('NUnit Console Runner 3.9.0')
            result.task(':clean').outcome == TaskOutcome.UP_TO_DATE
            result.task(':nunit').outcome == TaskOutcome.SUCCESS
    }

    @Unroll
    def "execute help works for nunit #version"() {
        given: "gradle build file with specific version"
            buildFile << """
                    nunit {
                        testAssemblies = ['-help']
                        nunitVersion = '$version'
                    }
                """
        when: "nunit task is executed"
            def result = GradleRunner.create()
                    .withProjectDir(testProjectDir.root)
                    .withArguments( 'nunit')
                    .withPluginClasspath()
                    .withDebug(true)
                    .build()
        then: "help command was written for the specified nunit version"
            result.output.contains("NUnit Console Runner $version")
            result.output.contains("NUNIT3-CONSOLE [inputfiles] [options]")
            result.task(':nunit').outcome == TaskOutcome.SUCCESS
        where:
            version << ['3.5.0', '3.6.0', '3.6.1']
    }

    def "execute help works for 3.0.0 version"() {
        given:
            buildFile << """
                    nunit {
                        testAssemblies = ['-help']
                        nunitVersion = '3.0.0'
                    }
                """
        when:
            def result = GradleRunner.create()
                    .withProjectDir(testProjectDir.root)
                    .withArguments( 'nunit')
                    .withPluginClasspath()
                    .withDebug(true)
                    .build()
        then:
            result.output.contains("NUnit Console Runner 3.0.5797")
            result.task(':nunit').outcome == TaskOutcome.SUCCESS
    }
}
