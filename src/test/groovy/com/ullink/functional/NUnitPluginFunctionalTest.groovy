package com.ullink.functional

import com.ullink.DownloadCacheCleaner
import org.gradle.internal.impldep.org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

class NUnitPluginFunctionalTest extends Specification {
    @TempDir
    File testProjectDir
    File buildFile

    def setup() {
        DownloadCacheCleaner.clear()

        buildFile = testProjectDir.toPath().resolve('build.gradle').toFile()
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
                    .withProjectDir(testProjectDir)
                    .withArguments('clean', 'nunit')
                    .withPluginClasspath()
                    .withDebug(true)
                    .build()
        then: "help command was written for the default nunit version"
            result.output.contains('NUnit Console Runner 3.20.1')
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
                    .withProjectDir(testProjectDir)
                    .withArguments( 'nunit')
                    .withPluginClasspath()
                    .withDebug(true)
                    .build()
        then: "help command was written for the specified nunit version"
            result.output.contains("NUnit Console")
            result.output.contains(version)
            result.output.contains("NUNIT3-CONSOLE [inputfiles] [options]")
            result.task(':nunit').outcome == TaskOutcome.SUCCESS
        where:
            version << ['3.16.1', '3.14.0', '3.13.2', '3.13.0', '3.12.0', '3.11.1', '3.11.0', '3.10.0', '3.9.0', '3.8.0']
    }

    def "nunit for two parallel namespaces successfully creates the merged TestResult.xml"() {
        given:
        FileUtils.copyDirectory(
                new File(getClass().getResource("/mock-assemblies").toURI()),
                testProjectDir)
        buildFile << """
                    final List filterExpressions = [
                        "test == 'MockAssembly.FirstNamespace'",
                        "test == 'MockAssembly.SecondNamespace'"
                    ]
                    nunit {
                        testAssemblies = ['MockAssembly.dll']
                        where = filterExpressions
                        parallelForks = true
                        resultFormat = 'nunit2'
                    }
                """
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments( 'nunit')
                .withPluginClasspath()
                .withDebug(true)
                .build()
        then:
        new File(testProjectDir.path + '/build/nunit/reports/TestResult.xml').exists()
        result.output.contains('NUnit Console')
        result.task(':nunit').outcome == TaskOutcome.SUCCESS
    }
}
