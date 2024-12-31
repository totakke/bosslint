# Changelog

## [Unreleased]

### Added

- Add an option `-C/--directory` to change working directory. ([1d5c123](https://github.com/totakke/bosslint/commit/1d5c123))
- Consider exit codes of linter executions. ([145810f](https://github.com/totakke/bosslint/commit/145810f))

### Changed

- Use `clojure.java.process` instead of `clojure.java.shell`. ([7b84acf](https://github.com/totakke/bosslint/commit/7b84acf))

### Fixed

- Fix clojure aliases on eastwood run. ([4750a14](https://github.com/totakke/bosslint/commit/4750a14))
- eastwood: use only aliases having :extra-paths. ([5d12519](https://github.com/totakke/bosslint/commit/5d12519))
- eastwood: fix run in a subdirectory. ([e216feb](https://github.com/totakke/bosslint/commit/e216feb))

## [0.4.1] - 2024-10-06

### Added

- Add `:disabled?` config. ([bdf5d1c](https://github.com/totakke/bosslint/commit/bdf5d1c))

### Fixed

- Return an error when unknown ref is supplied. ([37134e1](https://github.com/totakke/bosslint/commit/37134e1))
- Fix cljfmt group. ([fac5ed0](https://github.com/totakke/bosslint/commit/fac5ed0))

## [0.4.0] - 2023-03-28

### Added

- Add markdownlint linter. ([7da0a60](https://github.com/totakke/bosslint/commit/7da0a60))
- Add sql-lint linter. ([ece062d](https://github.com/totakke/bosslint/commit/ece062d))

## [0.3.0] - 2021-09-26

### Added

- Add tflint linter. ([d57bed9](https://github.com/totakke/bosslint/commit/d57bed9))
- Add kubeval linter. ([e982151](https://github.com/totakke/bosslint/commit/e982151))
- Add jsonlint linter. ([669111e](https://github.com/totakke/bosslint/commit/669111e))

### Fixed

- Fix cljfmt args. ([372fc22](https://github.com/totakke/bosslint/commit/372fc22))

## [0.2.1] - 2020-03-28

### Fixed

- Fix path->ns. ([e2a2e68](https://github.com/totakke/bosslint/commit/e2a2e68))
- Sort linters alphabetically. ([8c2f799](https://github.com/totakke/bosslint/commit/8c2f799))

## [0.2.0] - 2020-03-20

### Added

- Add dartanalyzer linter. ([6e8f2d5](https://github.com/totakke/bosslint/commit/6e8f2d5))
- Add yamllint linter. ([c4664d5](https://github.com/totakke/bosslint/commit/c4664d5))

### Changed

- Support diff between two commits. ([c1019b5](https://github.com/totakke/bosslint/commit/c1019b5))

## [0.1.0] - 2020-03-15

First release.

[Unreleased]: https://github.com/totakke/bosslint/compare/0.4.1...HEAD
[0.4.1]: https://github.com/totakke/bosslint/compare/0.4.0...0.4.1
[0.4.0]: https://github.com/totakke/bosslint/compare/0.3.0...0.4.0
[0.3.0]: https://github.com/totakke/bosslint/compare/0.2.1...0.3.0
[0.2.1]: https://github.com/totakke/bosslint/compare/0.2.0...0.2.1
[0.2.0]: https://github.com/totakke/bosslint/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/totakke/bosslint/compare/b32d91e...0.1.0
