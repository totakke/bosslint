# Bosslint

[![build](https://github.com/totakke/bosslint/actions/workflows/build.yml/badge.svg)](https://github.com/totakke/bosslint/actions/workflows/build.yml)
[![release](https://img.shields.io/badge/release-v0.4.0-blue.svg)](https://github.com/totakke/bosslint/releases/tag/0.4.0)

Multiple linters manager for easily checking

> Good health is the most important thing. More than success, more than money,
> more than power.
>
> &mdash; Hyman Roth / The Godfather Part II

## Installation

### OS & arch

- `os`:
  - Mac OS - `macos`
  - Linux - `linux`
- `arch`:
  - amd64 / x86_64  - `amd64`
  - aarch64 / arm64  - `aarch64`

### Download & install

```sh
curl -sSL https://github.com/totakke/bosslint/releases/download/0.4.0/bosslint_[os]_[arch] -o bosslint
chmod +x bosslint
mv bosslint [/your/PATH/dir/]
```

### Build

To build Bosslint yourself, you must install [Clojure CLI tools](https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools),
[GraalVM](https://www.graalvm.org/docs/getting-started/#install-graalvm) and
GraalVM's [`native-image`](https://www.graalvm.org/docs/getting-started/#native-images).
Additionally, `GRAALVM_HOME` environment variable must be set.

```console
$ clojure -M:native-image
Compiling bosslint.main
 ...
Finished generating 'bosslint' in 17.8s.
$ mv bosslint [/your/PATH/dir/]
```

## Usage

### Basics

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

```sh
bosslint check :all
```

Bosslint does not contain any linters themselves. You have to install the
[supported linters](#supported-linters) preliminarily.

### Configuration

Bosslint implicitly loads `~/.bosslint/config.edn` file.

Alternatively, you can specify a configuration file with `--config` option.

```sh
bosslint check --config [/path/to/config.edn] master
```

Look at the [configuration example](example/config.edn).

### Supported Linters

| Linter | Bosslint linter name |
| ------ | -------------------- |
| [Checkstyle](https://checkstyle.org/) | `checkstyle` |
| [clj-kondo](https://github.com/borkdude/clj-kondo) | `clj-kondo` |
| [cljfmt](https://github.com/weavejester/cljfmt) | `cljfmt` |
| [dartanalyzer](https://dart.dev/tools/dartanalyzer) | `dartanalyzer` |
| [Eastwood](https://github.com/jonase/eastwood) | `eastwood` |
| [Flake8](https://flake8.pycqa.org/) | `flake8` |
| [hadolint](https://github.com/hadolint/hadolint) | `hadolint` |
| [jsonlint](https://github.com/zaach/jsonlint) | `jsonlint` |
| [kubeval](https://www.kubeval.com/) | `kubeval` |
| [markdownlint-cli](https://github.com/igorshubovych/markdownlint-cli) | `markdownlint` |
| [sql-lint](https://github.com/joereynolds/sql-lint) | `sql-lint` |
| [stylelint](https://stylelint.io/) | `stylelint` |
| [SwiftLint](https://realm.github.io/SwiftLint/) | `swiftlint` |
| [tflint](https://github.com/terraform-linters/tflint) | `tflint` |
| [yamllint](https://yamllint.readthedocs.io/) | `yamllint` |

## License

Copyright © 2020 [Toshiki Takeuchi](https://totakke.net/)

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
<http://www.eclipse.org/legal/epl-2.0>.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at <https://www.gnu.org/software/classpath/license.html>.
