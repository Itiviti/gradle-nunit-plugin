# gradle-nunit-plugin changelog
## 1.9
### Added
* 'labels' attribute is added to NUnit
* Added support for multiple 'where' clauses for NUnit v3, redirected 'test' to 'where'

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

