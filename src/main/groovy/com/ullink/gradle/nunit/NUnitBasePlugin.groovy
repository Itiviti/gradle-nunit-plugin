package com.ullink.gradle.nunit
import org.gradle.api.Plugin
import org.gradle.api.Project

class NUnitBasePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.apply plugin: 'de.undercouch.download'
        project.tasks.withType(NUnit).whenTaskAdded { NUnit task ->
            applyNunitConventions(task, project)
            task.metaClass.mixin NUnit3Mixins
        }
    }

    def applyNunitConventions(NUnit task, Project project) {
        task.conventionMapping.map "nunitDownloadUrl", { "https://github.com/nunit/${task.gitHubRepoName}/releases/download" }
        task.conventionMapping.map "nunitVersion", { '3.10.0' }
        task.conventionMapping.map "nunitHome", {
            if (System.getenv()['NUNIT_HOME']) {
                return System.getenv()['NUNIT_HOME']
            }
        }
        project.plugins.withId('com.ullink.msbuild') {
            def msbuildTask = project.tasks.msbuild

            task.dependsOn msbuildTask
            task.conventionMapping.map 'testAssemblies', {
                msbuildTask.projects.findAll {
                    it.key =~ '^.+test(s?)$'
                }
                .collect {
                    it.value.getDotnetAssemblyFile()
                }
            }
        }
    }
}