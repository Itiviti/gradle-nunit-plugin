# gradle-nunit-plugin changelog

## 1.14
### Fixed
* test binaries are now correctly found

## 1.13
### Changed
* increase default NUnit runner version from 2.6.4 to 3.9.0
* built with gradle 4.7 (above won't work till we migrate out of plugindev)
* take as test assemblies only those containing "test.dll" or "tests.dll"
to not require explicit config for projects like "test-framework.dll"

### Fixed
* Upgrade `gradle-download-task` to 3.4.3 to fix 'Invalid cookie expiry' on download
* Allow download of NUnit 3.9 runner

## 1.12
### Changed
* upgrade gradle-download-plugin to 3.2
* upgrade net.researchgate.release to 2.6.0
* built with gradle 4.2.1

### Fixed
* Apply default parameters on plugin `com.ullink.msbuild` instead of `msbuild`
which is required starting from `gradle-msbuild-plugin` 2.17

## 1.11
### Added
* New parameter logFile for defining the output file name
* New paramter env for passing environment variable to nunit runner

### Changed
* built with gradle 3.5
* upgrade gradle-download-plugin to 3.2

### Fixed
* Support nunit-console v3.6.0

## 1.10
### Fixed
* NUnit 3.5+ can now be used

## 1.9
### Added
* 'labels' attribute is added to NUnit
* Added support for multiple 'where' clauses for NUnit v3, redirected 'test' to 'where'

### Changed
* built (and compatible) with gradle 2.14

## 1.8
### Fixed
* opencover-nunit could not be run if nunit is not yet cached or manually set

## 1.7
### Added
* `resultFormat` is added to NUnit task. It allows to set the test report format (nunit2 or nunit3) for NUnit v3.

### Fixed
* Downloading NUnit is done in the task itself and not in the closure to get NUnit folder. `parallelForks = true` feature can bring failure when NUnit is not already downloaded.
* Encode merged file like NUnit.

## 1.6
### Added
* `getCommandArgs` is added to NUnit task

### Changed
* Default working directory of NUnit v3 to build\nunit\
* Renamed parallel_forks to parallelForks in NUnit task
* Refactored for NUnit3 support and log a warnings if deprecated parameters `run` or `runList` has been specified

## 1.5
### Fixed
* Fix NUnit v3 console argument in specifying test result output

### Added
* The `nuget-base` plugin allows proper NUnit-based tasks creation without creating the default `nunit` task

## 1.4
### Added
* Support overriding the report file name
* Added support for NUnit v3 (#11 #12)
* Support overriding the report folder (#10)

### Changed
* noShadow option has been replaced by shadowCopy one, which defaults to false. Which means behavior will change when upgrading, but this is better matching NUnit v3 defaults, so it's for the best (as long as you read this changelog).

