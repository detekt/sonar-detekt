# Detekt - Changelog

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

- based on detekt RC7, see https://arturbosch.github.io/detekt/changelog.html for closed Issues

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
