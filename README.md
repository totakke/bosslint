# Bosslint

![build](https://github.com/totakke/bosslint/workflows/build/badge.svg)
![release](https://img.shields.io/badge/release-v0.2.1-blue.svg)

Meta linter for easily checking

> Good health is the most important thing. More than success, more than money, more than power.
>
> &mdash; Hyman Roth / The Godfather Part II

## Installation

### Mac OS

```console
$ curl -sSL https://github.com/totakke/bosslint/releases/download/0.2.1/bosslint_macos -o bosslint
$ chmod +x bosslint
$ mv bosslint [/your/PATH/dir/]
```

### Linux

```console
$ curl -sSL https://github.com/totakke/bosslint/releases/download/0.2.1/bosslint_linux -o bosslint
$ chmod +x bosslint
$ mv bosslint [/your/PATH/dir/]
```

### Build

You must setup GraalVM's `native-image` and set `GRAALVM_HOME` environment
variable.

```console
$ clojure -M:native-image
$ mv bosslint [/your/PATH/dir/]
```

## Usage

Bosslint collects changed files under Git version control and checks them with
appropriate linters.

```console
$ bosslint check HEAD~1
clj-kondo:
linting took 281ms, errors: 0, warnings: 0

cljfmt:
All source files formatted correctly

eastwood:
...
== Linting done in 1887 ms ==
== Warnings: 0 (not including reflection warnings)  Exceptions thrown: 0
```

Use `:all` to check all files under a Git project.

```console
$ bosslint check :all
```

### Configuration

Bosslint implicitly loads `~/.bosslint/config.edn` file.

Alternatively, you can specify a configuration file with `--config` option.

```console
$ bosslint check --config [/path/to/config.edn] master
```

Look at the [configuration example](example/config.edn).

### Supported Linters

- [Checkstyle](https://checkstyle.org/)
- [clj-kondo](https://github.com/borkdude/clj-kondo)
- [cljfmt](https://github.com/weavejester/cljfmt)
- [dartanalyzer](https://dart.dev/tools/dartanalyzer)
- [Eastwood](https://github.com/jonase/eastwood)
- [Flake8](https://flake8.pycqa.org/)
- [hadolint](https://github.com/hadolint/hadolint)
- [stylelint](https://stylelint.io/)
- [SwiftLint](https://realm.github.io/SwiftLint/)
- [yamllint](https://yamllint.readthedocs.io/)

## License

Copyright © 2020-2021 [Toshiki Takeuchi](https://totakke.net/)

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
