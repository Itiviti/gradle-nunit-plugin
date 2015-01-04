package com.ullink.gradle.nunit

import com.ullink.RepositoriesPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class NUnitPlugin implements Plugin<Project> {
    // TODO: move to gradle-repositories-plugin
    def setupLaunchpadRepositories(Project project) {
        if (!project.repositories.metaClass.respondsTo(project.repositories, 'launchpad', String, String, Object)) {
            project.logger.debug 'Adding launchpad(String?,Closure?) method to project RepositoryHandler'
            project.repositories.metaClass.launchpad = {
                // /nunitv2/trunk/2.6.3/+download/NUnit-2.6.3.zip
                def subPattern = '[artifact]-[revision](-[classifier]).[ext]'
                RepositoriesPlugin.addRepo(project, delegate, 'launchpad', null, 'https://launchpad.net', '[organization]/trunk/[revision]/+download/' + subPattern, null)
            }
        }
        if (!project.repositories.findByName('launchpad')) {
            project.repositories.launchpad()
        }
    }

    void apply(Project project) {
        project.tasks.withType(NUnit).whenTaskAdded { NUnit task ->
            applyNunitConventions(task, project)
        }

        project.apply plugin: 'repositories'
        Task nunitTask = project.task('nunit', type: NUnit)
        nunitTask.description = 'Executes NUnit tests'
    }

    def applyNunitConventions(NUnit task, Project project) {
        task.conventionMapping.map "nunitVersion", { '2.6.3' }
        task.conventionMapping.map "nunitHome", {
            if (System.getenv()['NUNIT_HOME']) {
                return System.getenv()['NUNIT_HOME']
            }
            setupLaunchpadRepositories(project)
            def version = task.getNunitVersion()
            downloadNUnit(project, version)
        }
        task.conventionMapping.map "testAssemblies", {
            if (project.plugins.hasPlugin('msbuild')) {
                task.dependsOn project.tasks.msbuild
                project.tasks.msbuild.projects.findAll { !(it.key =~ 'test') }.collect {
                    it.value.getProjectPropertyPath('TargetPath')
                }
            }
        }
    }

    File downloadNUnit(Project project, String version) {
        def dest = new File(new File(project.gradle.gradleUserHomeDir, 'caches'), 'nunit')
        if (!dest.exists()) {
            dest.mkdirs()
        }
        def ret = new File(dest, "NUnit-${version}")
        if (!ret.exists()) {
            project.logger.info "Downloading & Unpacking NUnit ${version}"
            def dep = project.dependencies.create(group: 'nunitv2', name: 'NUnit', version: version) {
                artifact {
                    name = 'NUnit'
                    type = 'zip'
                }
            }
            File zip = project.configurations.detachedConfiguration(dep).singleFile
            if (!zip.isFile()) {
                throw new GradleException("NUnit zip file '${zip}' doesn't exist")
            }
            project.ant.unzip(src: zip, dest: dest)
        }
        ret
    }
}
