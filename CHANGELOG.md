## [5.1.0](https://github.com/FilippoVissani/portfolium/compare/5.0.5...5.1.0) (2026-01-16)

### Features

* add PDF export functionality for portfolio data ([0dbdeb9](https://github.com/FilippoVissani/portfolium/commit/0dbdeb9c65b95102dfc26efda115efbd7832efdf))

### Dependency updates

* **deps:** update node.js to 24.13 ([#69](https://github.com/FilippoVissani/portfolium/issues/69)) ([b6d224b](https://github.com/FilippoVissani/portfolium/commit/b6d224bf105ea2ace8cc4f45c780434719685811))

### Bug Fixes

* update percentage formatting in PDF report exporter ([1805d0c](https://github.com/FilippoVissani/portfolium/commit/1805d0c5e703398a8818204a7904944a6b085f20))

### Build and continuous integration

* **deps:** update actions/upload-artifact action to v6 ([ed4f7e9](https://github.com/FilippoVissani/portfolium/commit/ed4f7e9a6ee820b4ec38232c020bada8fb0e8a95))
* **deps:** update dependency node to v24.13.0 ([#71](https://github.com/FilippoVissani/portfolium/issues/71)) ([ec0bd00](https://github.com/FilippoVissani/portfolium/commit/ec0bd00a445a57104243d670731253fb5a96ad2d))
* fix jar build ([37d0503](https://github.com/FilippoVissani/portfolium/commit/37d0503129f973a0f97f7a9044fe3bec9382867d))
* re-enable Windows build configuration in release workflow ([69918a4](https://github.com/FilippoVissani/portfolium/commit/69918a4caa3684f0c133fff53e2b7908723f50fe))

### General maintenance

* change default data directory ([c14cbb2](https://github.com/FilippoVissani/portfolium/commit/c14cbb20a197f0561402f2c1cec707ffa32ca31f))
* format code ([c819ebe](https://github.com/FilippoVissani/portfolium/commit/c819ebec5dea843d9b7ab0db4cb716667f2c04c5))
* remove unused Font import in PdfExporter ([e7907e5](https://github.com/FilippoVissani/portfolium/commit/e7907e50f55d57567009537921201849471ce058))

### Refactoring

* move PDF report export functionality in Controller ([8d09ddf](https://github.com/FilippoVissani/portfolium/commit/8d09ddfa4df838eca0f4dc832f51cba5f9122780))
* rename Console to ConsoleView and update rendering logic ([2296569](https://github.com/FilippoVissani/portfolium/commit/2296569546dc6631fa82c6811036e01263eada4b))

## [5.0.5](https://github.com/FilippoVissani/portfolium/compare/5.0.4...5.0.5) (2026-01-13)

### Documentation

* update README to reflect changes in Gradle tasks and Kover usage ([3b50ae6](https://github.com/FilippoVissani/portfolium/commit/3b50ae6bcfc72dae0f0ec24ce75b0539dc600d73))

### Build and continuous integration

* update release workflow to include Gradle build step ([#67](https://github.com/FilippoVissani/portfolium/issues/67)) ([78e459d](https://github.com/FilippoVissani/portfolium/commit/78e459df54d45cd3417189e979f36b9d923f2056))

## [5.0.4](https://github.com/FilippoVissani/portfolium/compare/5.0.3...5.0.4) (2026-01-12)

### Bug Fixes

* add shutdown hook for graceful exit on Ctrl+C ([6442dc8](https://github.com/FilippoVissani/portfolium/commit/6442dc8d99463129d74d92e8641dece9a68c1b83))

### Build and continuous integration

* add GraalVM native image support and configuration ([eb98a5c](https://github.com/FilippoVissani/portfolium/commit/eb98a5cfb1893660fc32125c417c7c238103ead8))
* **deps:** update actions/download-artifact action to v7 ([#63](https://github.com/FilippoVissani/portfolium/issues/63)) ([ac4da50](https://github.com/FilippoVissani/portfolium/commit/ac4da50679c3f8644e66cc5cf4dce31fb8e08532))
* inherit secrets for release workflow ([2b4c230](https://github.com/FilippoVissani/portfolium/commit/2b4c2301c0d37d4b91fbaa26d35829ff39d41314))
* refactor CI workflows to use GraalVM and modularize checks, tests, and releases ([f2c0325](https://github.com/FilippoVissani/portfolium/commit/f2c03256e5e94c751d9ab1c7bc56c7c542295959))
* remove Diktat checks ([75f08b7](https://github.com/FilippoVissani/portfolium/commit/75f08b7dba46c55791f45e98f49bcbc21caf9102))
* remove Diktat plugin and related configuration ([4052f79](https://github.com/FilippoVissani/portfolium/commit/4052f79d92d17ddc14d948050669ed8109df5390))
* remove OWASP Dependency Check plugin and related configurations ([ec792bf](https://github.com/FilippoVissani/portfolium/commit/ec792bf9a0ec1c55c4cdb82dd74723bb0d9d5b4b))
* remove OWASP dependency check workflow from checks.yml ([bfc7bed](https://github.com/FilippoVissani/portfolium/commit/bfc7bed25881b25006bb6e7c7e5dd9d7d5dd28d4))
* temporarily disable Windows build configuration ([3a626d3](https://github.com/FilippoVissani/portfolium/commit/3a626d3774ebfc5a324d6b51914fc1adc42ee647))

### General maintenance

* format code ([bcd2c82](https://github.com/FilippoVissani/portfolium/commit/bcd2c82ae9d41d4cde3b61ebca95a6380c616f29))

## [5.0.3](https://github.com/FilippoVissani/portfolium/compare/5.0.2...5.0.3) (2026-01-10)

### Documentation

* update README to center logo and remove architecture section ([bc5fa2c](https://github.com/FilippoVissani/portfolium/commit/bc5fa2cb89ab7b25cfb285e23ed01605d99c5310))

## [5.0.2](https://github.com/FilippoVissani/portfolium/compare/5.0.1...5.0.2) (2026-01-10)

### Dependency updates

* **deps:** update dependency com.github.ben-manes.versions to v0.53.0 ([00885bd](https://github.com/FilippoVissani/portfolium/commit/00885bd23bc9acbf4acdd583a8e9b1bb542e28b8))
* **deps:** update dependency com.lemonappdev:konsist to v0.17.3 ([e4c4d73](https://github.com/FilippoVissani/portfolium/commit/e4c4d73ef9bf30e9645b8b3a576ae1c6404e738a))
* **deps:** update dependency org.jetbrains.kotlinx.kover to v0.9.4 ([6ce9e17](https://github.com/FilippoVissani/portfolium/commit/6ce9e17d72cbfbc0aec7995a4a4bdb85c37f8bec))
* **deps:** update dependency org.owasp.dependencycheck to v11 ([d22c599](https://github.com/FilippoVissani/portfolium/commit/d22c599b0495b09193b1a7b46ed0e95998944b0b))
* **deps:** update dependency org.owasp.dependencycheck to v12 ([f15d440](https://github.com/FilippoVissani/portfolium/commit/f15d44006864f3de86cd94681562fcfbd787fa34))

### Bug Fixes

* retrigger release ([62c6704](https://github.com/FilippoVissani/portfolium/commit/62c670423200cf5e6788910686d90b1911ab0eb9))

### Build and continuous integration

* remove output directory for OWASP Dependency Check reports ([5465ce2](https://github.com/FilippoVissani/portfolium/commit/5465ce206f757945876d9c751f266aa2efb63314))

## [5.0.1](https://github.com/FilippoVissani/portfolium/compare/5.0.0...5.0.1) (2026-01-10)

### Dependency updates

* **deps:** update plugin ktlint to v12.3.0 ([#52](https://github.com/FilippoVissani/portfolium/issues/52)) ([56b8581](https://github.com/FilippoVissani/portfolium/commit/56b85810d22205787fc8455735d710a0941a5fbb))
* **deps:** update plugin ktlint to v13 ([#53](https://github.com/FilippoVissani/portfolium/issues/53)) ([433a254](https://github.com/FilippoVissani/portfolium/commit/433a254f8583f8bc2b3ff13ae6dee2a15fce0912))
* **deps:** update plugin ktlint to v14 ([#54](https://github.com/FilippoVissani/portfolium/issues/54)) ([ecc7549](https://github.com/FilippoVissani/portfolium/commit/ecc7549ea18841e5a76622ade6a6f9d11d3c4068))

### Bug Fixes

* fix bugs, add build tools and refactor code ([aa70316](https://github.com/FilippoVissani/portfolium/commit/aa7031655518b5feb83fed8f7eab28d85fec62e9))

## [5.0.0](https://github.com/FilippoVissani/portfolium/compare/4.0.2...5.0.0) (2026-01-09)

### ⚠ BREAKING CHANGES

* re-implement everything using YAML files and providing new view

### Features

* re-implement everything using YAML files and providing new view ([615fcc8](https://github.com/FilippoVissani/portfolium/commit/615fcc85561edc45d2fb0f535494d220e38ea5e1))

## [4.0.2](https://github.com/FilippoVissani/portfolium/compare/4.0.1...4.0.2) (2026-01-08)

### Documentation

* update README ([8c10181](https://github.com/FilippoVissani/portfolium/commit/8c10181bee8469c7ca1750a135614176d8eb16aa))

### Build and continuous integration

* **deps:** update actions/upload-artifact action to v6 ([cf28735](https://github.com/FilippoVissani/portfolium/commit/cf28735fdffc1072023804c204a8ec8813ed9a30))
* **deps:** update gradle/actions action to v5 ([514274a](https://github.com/FilippoVissani/portfolium/commit/514274ad6dc5e20da21b617ac3b92300a7f6de2f))

## [4.0.1](https://github.com/FilippoVissani/portfolium/compare/4.0.0...4.0.1) (2026-01-08)

### Documentation

* update README ([2e4f668](https://github.com/FilippoVissani/portfolium/commit/2e4f668fa8d44c54ab3a83c28878722f05c3c373))

## [4.0.0](https://github.com/FilippoVissani/portfolium/compare/3.4.0...4.0.0) (2026-01-08)

### ⚠ BREAKING CHANGES

* add configuration management with application.properties

### Features

* add configuration management with application.properties ([5a16e48](https://github.com/FilippoVissani/portfolium/commit/5a16e48d1dd0b52ec369e4dcba90cd533d27d6c9))

## [3.4.0](https://github.com/FilippoVissani/portfolium/compare/3.3.0...3.4.0) (2026-01-08)

### Features

* enhance chart tooltips to display monetary ([9ac73c9](https://github.com/FilippoVissani/portfolium/commit/9ac73c9f4c003aecdbd2f993b509b50442b7f229))

### Build and continuous integration

* **deps:** update actions/cache action to v5 ([#25](https://github.com/FilippoVissani/portfolium/issues/25)) ([1f0d502](https://github.com/FilippoVissani/portfolium/commit/1f0d502715dd6a6665b60bda7e5a1066d5f123a0))
* fix versioning ([8de504e](https://github.com/FilippoVissani/portfolium/commit/8de504ed9532e0c6748129f02063b040492b9d97))

## [3.3.0](https://github.com/FilippoVissani/portfolium/compare/3.2.4...3.3.0) (2026-01-08)

### Features

* implement caching for price data source with CSV persistence ([4b0a1db](https://github.com/FilippoVissani/portfolium/commit/4b0a1db728670f5bee0200696ae78cab9d818b64))

## [3.2.4](https://github.com/FilippoVissani/portfolium/compare/3.2.3...3.2.4) (2026-01-08)

### Dependency updates

* **deps:** update kotest to v6 ([14e5c80](https://github.com/FilippoVissani/portfolium/commit/14e5c80e0e4494d28a990a7bb8d2312e92713c80))

### Bug Fixes

* implement data downsampling for history chart ([#43](https://github.com/FilippoVissani/portfolium/issues/43)) ([33e6fee](https://github.com/FilippoVissani/portfolium/commit/33e6fee75317bb66c4f7f3036892752301590025))

### Build and continuous integration

* **deps:** update gradle/actions action to v4 ([#36](https://github.com/FilippoVissani/portfolium/issues/36)) ([fc42e2a](https://github.com/FilippoVissani/portfolium/commit/fc42e2ac43ddf00eb80ac9a9567d371172f547bc))
* update release configuration to modify version in build.gradle.kts ([d9646d1](https://github.com/FilippoVissani/portfolium/commit/d9646d1109169c9b21e9d2e570fc9db87a7f165b))

## [3.2.3](https://github.com/FilippoVissani/portfolium/compare/3.2.2...3.2.3) (2026-01-08)

### Dependency updates

* **deps:** add renovate.json ([4903366](https://github.com/FilippoVissani/portfolium/commit/4903366c562ce87ef8ced3987ac295595f5f3c93))
* **deps:** switch kotlinCsv dependency to a new repository ([16d19f9](https://github.com/FilippoVissani/portfolium/commit/16d19f90ac1aea0b61a614812231fb04a39f3737))
* **deps:** update dependency ch.qos.logback:logback-classic to v1.5.23 ([#16](https://github.com/FilippoVissani/portfolium/issues/16)) ([736c451](https://github.com/FilippoVissani/portfolium/commit/736c451ca93e399022b60c74b641feb747a98439))
* **deps:** update dependency ch.qos.logback:logback-classic to v1.5.24 ([50df52e](https://github.com/FilippoVissani/portfolium/commit/50df52e8b62a0463cd6bbe0af366115178a3b1cf))
* **deps:** update dependency com.github.doyaaaaaken:kotlin-csv-jvm to v1.10.0 ([#18](https://github.com/FilippoVissani/portfolium/issues/18)) ([3d5f212](https://github.com/FilippoVissani/portfolium/commit/3d5f2128fce619d81d92354bcc2af4d47f97f7dd))
* **deps:** update gradle to v8.14.3 ([#17](https://github.com/FilippoVissani/portfolium/issues/17)) ([46d3e9b](https://github.com/FilippoVissani/portfolium/commit/46d3e9b1075ce7b4162fed629feb0444689c4104))
* **deps:** update gradle to v9 ([#22](https://github.com/FilippoVissani/portfolium/issues/22)) ([0f2c5de](https://github.com/FilippoVissani/portfolium/commit/0f2c5de3e380cdd17a3610e475854436deccb0fc))
* **deps:** update JaCoCo version to 0.8.14 ([8bfbfbf](https://github.com/FilippoVissani/portfolium/commit/8bfbfbfdea90888752ff05242a800541d84a9913))
* **deps:** update kotlin monorepo to v2.3.0 ([#19](https://github.com/FilippoVissani/portfolium/issues/19)) ([ba4c572](https://github.com/FilippoVissani/portfolium/commit/ba4c572709dc4ff6b2a065b60c6338bdb71f32a5))
* **deps:** update ktor monorepo to v3.3.3 ([#20](https://github.com/FilippoVissani/portfolium/issues/20)) ([e49fdee](https://github.com/FilippoVissani/portfolium/commit/e49fdee82e0f34983397c032a51c44aa0abe8cc2))
* **deps:** update plugin org.gradle.toolchains.foojay-resolver-convention to v0.10.0 ([#21](https://github.com/FilippoVissani/portfolium/issues/21)) ([ab0c083](https://github.com/FilippoVissani/portfolium/commit/ab0c083f22cd9692bde9788fc50548a872e224c5))
* **deps:** update plugin org.gradle.toolchains.foojay-resolver-convention to v1 ([#24](https://github.com/FilippoVissani/portfolium/issues/24)) ([b4e3fa3](https://github.com/FilippoVissani/portfolium/commit/b4e3fa3b67f3976d8811c358af5a0010674955ca))

### Documentation

* add CI badge to README ([6e855ec](https://github.com/FilippoVissani/portfolium/commit/6e855ecdac36b518e80d619b31f1e3c848c2363d))
* update Java version badge in README to 25 ([e9689da](https://github.com/FilippoVissani/portfolium/commit/e9689da569e3d2a9794aa13f074b1d18cc77308e))

### Build and continuous integration

* add coverage report generation and publishing to CI workflow ([9e2001b](https://github.com/FilippoVissani/portfolium/commit/9e2001bb98fbcde0bacf5179f1e27ab122e4a332))
* add JaCoCo integration ([4499620](https://github.com/FilippoVissani/portfolium/commit/449962078a11b70bfef818414549f901dcda4f92))
* **deps:** update actions/checkout action to v5 ([#26](https://github.com/FilippoVissani/portfolium/issues/26)) ([4d24a9d](https://github.com/FilippoVissani/portfolium/commit/4d24a9ddc994e915427306783f2a046811e64f0b))
* **deps:** update actions/checkout action to v6 ([#27](https://github.com/FilippoVissani/portfolium/issues/27)) ([513f186](https://github.com/FilippoVissani/portfolium/commit/513f1860e9ccfee569be0c1fe062beb44190c22c))
* **deps:** update actions/setup-java action to v5 ([#28](https://github.com/FilippoVissani/portfolium/issues/28)) ([241bdfb](https://github.com/FilippoVissani/portfolium/commit/241bdfb16cc3e3fbe3b778fe55c302e7f599f614))
* **deps:** update actions/upload-artifact action to v5 ([#29](https://github.com/FilippoVissani/portfolium/issues/29)) ([4eace2f](https://github.com/FilippoVissani/portfolium/commit/4eace2f655232c53af644dff70b453bf260369c6))
* **deps:** update gradle/wrapper-validation-action action to v3 ([fc12e6e](https://github.com/FilippoVissani/portfolium/commit/fc12e6e6622dc10111f856d49a97bd4bc7fb3fdc))
* remove renovate config ([b8e1e9c](https://github.com/FilippoVissani/portfolium/commit/b8e1e9c6bdea49915e5296d2ebac98e234530815))
* update Gradle wrapper validation action to use the new repository path ([594f2fd](https://github.com/FilippoVissani/portfolium/commit/594f2fd1cfb7aa57e34a1e23f027088a3801ffd4))
* update JaCoCo tool version to use version from libs.versions.toml ([942afbe](https://github.com/FilippoVissani/portfolium/commit/942afbe006b1e39cd2b60deeac818e5136c0e0d6))
* upgrade JDK from 21 to 25 in build configuration and CI workflow ([f10fe61](https://github.com/FilippoVissani/portfolium/commit/f10fe61c9eba407b1e9959d5b8999959c9a9c8f6))

## [3.2.2](https://github.com/FilippoVissani/portfolium/compare/3.2.1...3.2.2) (2026-01-07)

### Documentation

* remove contributing link from README ([9ac977d](https://github.com/FilippoVissani/portfolium/commit/9ac977d6f539b7a009d38e657335e27d2f502cc7))

## [3.2.1](https://github.com/FilippoVissani/portfolium/compare/3.2.0...3.2.1) (2026-01-07)

### Documentation

* update README with new features and improved formatting ([a3541a9](https://github.com/FilippoVissani/portfolium/commit/a3541a94921978f8f53a660339cf119adace822d))

## [3.2.0](https://github.com/FilippoVissani/portfolium/compare/3.1.0...3.2.0) (2026-01-07)

### Features

* implement logging system with SLF4J for improved error handling and information tracking ([2e11427](https://github.com/FilippoVissani/portfolium/commit/2e11427192aad825d5368a5692967083ba6658a3))

## [3.1.0](https://github.com/FilippoVissani/portfolium/compare/3.0.0...3.1.0) (2026-01-07)

### Features

* implement time period selection for historical performance chart ([966e442](https://github.com/FilippoVissani/portfolium/commit/966e442b2802a8d0f14cf4730ef51c98158f9bfd))

## [3.0.0](https://github.com/FilippoVissani/portfolium/compare/2.2.0...3.0.0) (2026-01-07)

### ⚠ BREAKING CHANGES

* enhance liquid and invested capital calculations in financial summaries

### Features

* enhance liquid and invested capital calculations in financial summaries ([dc0cf29](https://github.com/FilippoVissani/portfolium/commit/dc0cf29ff7140e4ce4b93f969b1c3e2334b85135))

### Tests

* add unit tests for PlannedExpense and EmergencyFundConfig models ([30641f6](https://github.com/FilippoVissani/portfolium/commit/30641f6931a2f3d104c5ecef93ec80e694364d03))

## [2.2.0](https://github.com/FilippoVissani/portfolium/compare/2.1.0...2.2.0) (2026-01-07)

### Features

* implement historical performance tracking and visualization ([2f407be](https://github.com/FilippoVissani/portfolium/commit/2f407bea74e8745294e9f9c23918a87db3c2c56a))

## [2.1.0](https://github.com/FilippoVissani/portfolium/compare/2.0.0...2.1.0) (2026-01-06)

### Features

* add Ktor web server and view for portfolio dashboard ([a88db37](https://github.com/FilippoVissani/portfolium/commit/a88db370ec649fdd9e96e05634b8a93a4c7ddb1c))

## [2.0.0](https://github.com/FilippoVissani/portfolium/compare/1.0.1...2.0.0) (2026-01-06)

### ⚠ BREAKING CHANGES

* restructure project architecture

### Tests

* migrate tests to use Kotest framework ([d2098f6](https://github.com/FilippoVissani/portfolium/commit/d2098f691412c7613ae098eedb880df51427443b))

### Refactoring

* restructure project architecture ([e26ca17](https://github.com/FilippoVissani/portfolium/commit/e26ca17edd0a6e87af143d15e9e9a73f6ab3c4f8))

## [1.0.1](https://github.com/FilippoVissani/portfolium/compare/1.0.0...1.0.1) (2026-01-06)

### Documentation

* remove FAQ section from README ([04dd4cd](https://github.com/FilippoVissani/portfolium/commit/04dd4cd356d4722b093b99b85ee48004bf9eb858))

### Build and continuous integration

* migrate dependencies to version catalog ([94356f1](https://github.com/FilippoVissani/portfolium/commit/94356f1ce8d6b7005a75c9d69134f30431815999))

## 1.0.0 (2026-01-06)

### Features

* add investment transaction summarization and loading functionality ([eeb61d0](https://github.com/FilippoVissani/portfolium/commit/eeb61d0f540141b4837853b719b48e17885fd0a3))

### Documentation

* add README with project overview, features, and usage instructions ([90cdafe](https://github.com/FilippoVissani/portfolium/commit/90cdafe138f8dd39581d8aabf9c47a2ccfa55a9c))
* update usage instructions to include current_prices.csv ([fa2ccad](https://github.com/FilippoVissani/portfolium/commit/fa2ccad7b0b8ce72008735b5a2b968c8dee1f58e))

### Tests

* add unit tests for Calculators functionality ([5422f09](https://github.com/FilippoVissani/portfolium/commit/5422f09b2c30c707f890a0f94159d97bb06fb1df))
* add unit tests for CsvUtils and Loaders functionality ([eb22f80](https://github.com/FilippoVissani/portfolium/commit/eb22f80ef66a84bace6990a12bcbd8adfad51787))
* update PlannedExpense instantiation in unit tests ([cd83dee](https://github.com/FilippoVissani/portfolium/commit/cd83deef8521e25022b3db8262b8d10386647659))

### Build and continuous integration

* add GitHub Actions workflow for CI/CD pipeline ([39bca86](https://github.com/FilippoVissani/portfolium/commit/39bca86461a1b7301abdb6063ec464b86646589c))
* fix release ([c81f5f1](https://github.com/FilippoVissani/portfolium/commit/c81f5f1dd4de296eb93e2138c4b9fd4b9549ea6f))

### General maintenance

* add license ([51f85b4](https://github.com/FilippoVissani/portfolium/commit/51f85b4d5614cd13e510b55a70275e30f8096978))
* initialize project ([efe7c62](https://github.com/FilippoVissani/portfolium/commit/efe7c62413755c73ff258dfd4c5a86002f3ea842))
* refactor package structure and update imports ([c224af9](https://github.com/FilippoVissani/portfolium/commit/c224af97513de41f9ce7970688ed060709b300e0))
* update package structure and rename files ([7868404](https://github.com/FilippoVissani/portfolium/commit/786840491fde93332181c506a52de713fb5baf58))

### Refactoring

* remove unused 'instrument' field from data classes ([8cc7c00](https://github.com/FilippoVissani/portfolium/commit/8cc7c007539c2767b67ff04c461981e0aec81029))
