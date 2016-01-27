# gradle-nunit-plugin changelog

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

