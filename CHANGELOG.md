# Detekt - Changelog

#### 2.5.0 - 2022-01-09

##### Changelog

- Based on detekt 1.19.0
- Configured `sonarQubeMinVersion` to 8.9

##### Dependencies Update

- Kotlin to 1.6.10

#### 2.4.0 - 2021-10-31

##### Changelog

- Based on detekt 1.18.1
- Fixed definition of prop `inputPaths` using sonar.sources
- Fixed usage of `Path` for Java 8

##### Dependencies Update

- Sonar API to 9.1.0.47736
- Kotlin to 1.5.31
- AssertJ to 3.21.0
- JCommander to 1.81
- Tested on CI against Java 16

#### 2.3.0 - 2020-09-26

- Based on detekt 1.14.1

#### 2.2.0 - 2020-08-16

- Based on detekt 1.11.0

##### Changelog

- Update to sonar-api 7.9.4 - [#129](https://github.com/detekt/sonar-kotlin/pull/129)
- Exclude MultiLineIfElse as detekt has a similar rule - [#127](https://github.com/detekt/sonar-kotlin/pull/127)
- Exclude FinalNewline KtLint rule, use detekt's - [#126](https://github.com/detekt/sonar-kotlin/pull/126)
- Use detekt's tooling api - [#125](https://github.com/detekt/sonar-kotlin/pull/125)
- Exclude NoWildcardImports KtLint rule - [#124](https://github.com/detekt/sonar-kotlin/pull/124)
- Rewrite detekt sensor test to spek - [#123](https://github.com/detekt/sonar-kotlin/pull/123)
- Exclude KtLint rules which detect same issues as detekt itself - [#122](https://github.com/detekt/sonar-kotlin/pull/122)
- Consider removing duplicated rules from KtLint - [#119](https://github.com/detekt/sonar-kotlin/issues/119)

See all issues at: [2.2.0](https://github.com/detekt/sonar-kotlin/milestone/5)

#### 2.1.0

- Based on detekt 1.9.1

#### 2.0.0

- Rename to sonar-detekt
- Based on detekt 1.8.0
- All redundant features to official SonarKotlin plugin got dropped
- Compatible with SonarKotlin, allows mixing rulesets

#### 1.5.0

- Updated to detekt 1.7.4
- Updated to Kotlin 1.3.71

#### 1.4.0

- Updated to detekt 1.6.0

#### 1.3.2

- Updated to detekt 1.5.1
- Turn off autoCorrect value for subconfigs too - [#110](https://github.com/detekt/sonar-kotlin/pull/110)
- [IndexOutOfBoundsException] Wrong offset xxx. Should be in range: [0, yyy] - [#106](https://github.com/detekt/sonar-kotlin/issues/106)

See all issues at: [1.3.2](https://github.com/detekt/sonar-kotlin/milestone/2)

#### 1.3.1

- Errors in syntax highlighting should print a warning and not crash sonar-kotlin.

#### 1.3.0

- Based on detekt 1.5.0

#### 1.2.0

- Based on detekt 1.3.0
- Based on Kotlin 1.3.61

#### 1.1.0

- Based on detekt 1.1.1
- Based on Kotlin 1.3.50
- Rewrote syntax highlighting to make use of kotlinc internal offset to line/column translation

#### 1.0.0

- Based on sonar-api 6.7.7 and detekt 1.0.0
- Gradle Plugin: removed report consolidation. It was flawed and some users were stuck with RC14. It will be replaced in a further version.
- Gradle Plugin: `autoCorrect` property is now allowed on the detekt extension. No need to create a new task anymore.
- Formatting: updated to KtLint 0.34.2 which removed the two rules `NoItParamInMultilineLambda` and `SpacingAroundUnaryOperators`. 

#### 0.6.0

- Based on sonar-api 6.7.7 and detekt RC16, see rc15 and rc16 for breaking changes
- Renamed default profile to "detekt active", containing 80 rules marked active in detekt
- Introduced a second quality profile "detekt all", containing 164 rules
- Integrated detekt-formatting, a wrapper over KtLint, provides 30+ additional rules
- Tested on sonaqube 7.7, 7.8 and 7.9

#### 0.5.3

- Based on sonar 6.7.7, kotlin 1.3.21 and detekt RC14

#### 0.5.2

- Upgrade to detekt 1.0.0-RC10

#### 0.5.1

- based on detekt RC9.2
- jacoco fix by @kzaikin
- jacoco update for 7.3 by @underyx

#### 0.5.0

- based on detekt 1.0.0.RC7-3 (by @grevolution)
- parsing Surefire/JUinit reports (by @vexdev)

#### 0.4.2

- based on detekt RC7, see https://detekt.github.io/detekt/changelog.html for closed Issues

#### 0.4.1

- Based on detekt rc6-3

#### 0.4.0 

- Based on detekt rc6-2
- Migration to maven (by @johnou)

#### 0.3.0

- Based on detekt RC5-6
- Support for detekt's baseline feature
- Better support for detekt's configuration files
- Resolved Call ofType() first defect for jacoco on sonar 5.6 LTS 

#### 0.2.4

- Based on detekt RC4-3
- Using lines of code and complexity metrics from detekt (by @schalkms)
- JaCoCo support (by @vexdev)

#### 0.2.3

- Fixed issue with invalid locations - #20

#### 0.2.2

- Fixed RuleKey mandatory issue
- Based on detekt RC3

#### 0.2.1

- Based on detekt RC2 (~20 new rules, not in detekt way default profile)
- Metric values and thresholds are printed next to the description (contributed by rror)

#### 0.2.0

- First public release based on detekt M13
